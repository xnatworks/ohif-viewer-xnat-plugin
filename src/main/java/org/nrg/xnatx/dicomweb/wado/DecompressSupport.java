/********************************************************************
 * Copyright (c) 2023, Institute of Cancer Research
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * (1) Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 * (2) Redistributions in binary form must reproduce the above
 *     copyright notice, this list of conditions and the following
 *     disclaimer in the documentation and/or other materials provided
 *     with the distribution.
 *
 * (3) Neither the name of the Institute of Cancer Research nor the
 *     names of its contributors may be used to endorse or promote
 *     products derived from this software without specific prior
 *     written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *********************************************************************/
package org.nrg.xnatx.dicomweb.wado;

import lombok.extern.slf4j.Slf4j;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.imageio.codec.ImageDescriptor;
import org.dcm4che3.imageio.codec.ImageReaderFactory;
import org.dcm4che3.imageio.codec.TransferSyntaxType;
import org.dcm4che3.imageio.codec.jpeg.PatchJPEGLSImageInputStream;
import org.dcm4che3.imageio.stream.EncapsulatedPixelDataImageInputStream;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.util.SafeClose;
import org.nrg.xnatx.dicomweb.service.wado.WadoRsContext;

import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.*;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author mo.alsad
 * <p>
 * Some parts of code are refactored from dcm4chee. Refer to DCM4CHEE_LICENSE.
 */
@Slf4j
class DecompressSupport implements Closeable
{
	protected final InstanceInfo inst;
	private final WadoRsContext ctx;
	protected EncapsulatedPixelDataImageInputStream encapsulatedPixelData;
	private ImageReaderFactory.ImageReaderParam decompressorParam;
	private ImageReader decompressor;
	private ImageReadParam decompressParam;
	private BufferedImage bi;
	private DicomInputStream dis;

	DecompressSupport(WadoRsContext ctx, InstanceInfo inst)
	{
		this.ctx = ctx;
		this.inst = inst;
	}

	private static void bgr2rgb(byte[] bs)
	{
		for (int i = 0, j = 2; j < bs.length; i += 3, j += 3)
		{
			byte b = bs[i];
			bs[i] = bs[j];
			bs[j] = b;
		}
	}

	private static int sizeOf(BufferedImage bi)
	{
		DataBuffer db = bi.getData().getDataBuffer();
		return db.getSize() * db.getNumBanks() * (DataBuffer.getDataTypeSize(
			db.getDataType()) / 8);
	}

	private static void writeTo(SampleModel sm, short[] data, OutputStream out)
		throws IOException
	{
		int h = sm.getHeight();
		int w = sm.getWidth();
		int stride = ((ComponentSampleModel) sm).getScanlineStride();
		byte[] b = new byte[w * 2];
		for (int y = 0; y < h; ++y)
		{
			for (int i = 0, j = y * stride; i < b.length; )
			{
				short s = data[j++];
				b[i++] = (byte) s;
				b[i++] = (byte) (s >> 8);
			}
			out.write(b);
		}
	}

	private static void writeTo(SampleModel sm, int[] data, OutputStream out)
		throws IOException
	{
		int h = sm.getHeight();
		int w = sm.getWidth();
		int stride = ((SinglePixelPackedSampleModel) sm).getScanlineStride();
		byte[] b = new byte[w * 3];
		for (int y = 0; y < h; ++y)
		{
			for (int i = 0, j = y * stride; i < b.length; )
			{
				int s = data[j++];
				b[i++] = (byte) (s >> 16);
				b[i++] = (byte) (s >> 8);
				b[i++] = (byte) s;
			}
			out.write(b);
		}
	}

	@Override
	public void close()
	{
		SafeClose.close(encapsulatedPixelData);
		encapsulatedPixelData = null;
		SafeClose.close(dis);
		dis = null;
		if (decompressor != null)
		{
			decompressor.dispose();
			decompressor = null;
		}
	}

	protected BufferedImage decompressFrame(int frameIndex) throws IOException
	{
		if (encapsulatedPixelData.isEndOfStream())
		{
			throw new IOException(
				"Number of data fragments not sufficient for number of frames in requested object");
		}

		decompressor.setInput(decompressorParam.patchJPEGLS != null
														? new PatchJPEGLSImageInputStream(
			encapsulatedPixelData, decompressorParam.patchJPEGLS)
														: encapsulatedPixelData);
		decompressParam.setDestination(bi);
		long start = System.currentTimeMillis();
		bi = decompressor.read(0, decompressParam);
		long end = System.currentTimeMillis();
		if (log.isDebugEnabled())
		{
			log.debug("Decompressed frame #{} 1:{} in {} ms",
				frameIndex + 1,
				(float) sizeOf(bi) / encapsulatedPixelData.getStreamPosition(),
				end - start);
		}
		encapsulatedPixelData.seekNextFrame();
		return bi;
	}

	protected void initEncapsulatedPixelData() throws IOException
	{
		dis = WadoUtils.openDicomInputStream(ctx, inst);
		Attributes attrs = dis.readDataset(-1, Tag.PixelData);
		if (dis.tag() != Tag.PixelData || dis.length() != -1)
		{
			throw new IOException(
				"No or incorrect encapsulated compressed pixel data in requested object");
		}

		ImageDescriptor imageDescriptor = new ImageDescriptor(attrs);
		String tsuid = dis.getTransferSyntax();
		TransferSyntaxType tsType = TransferSyntaxType.forUID(tsuid);
		encapsulatedPixelData = new EncapsulatedPixelDataImageInputStream(dis,
			imageDescriptor, tsType);
		initDecompressor(tsuid, tsType, imageDescriptor);
		if (tsType == TransferSyntaxType.RLE)
		{
			initBufferedImage(imageDescriptor);
		}
	}

	protected void writeFrameTo(OutputStream out) throws IOException
	{
		WritableRaster raster = bi.getRaster();
		SampleModel sm = raster.getSampleModel();
		DataBuffer db = raster.getDataBuffer();
		switch (db.getDataType())
		{
			case DataBuffer.TYPE_BYTE:
				writeTo(sm, ((DataBufferByte) db).getBankData(), out);
				break;
			case DataBuffer.TYPE_USHORT:
				writeTo(sm, ((DataBufferUShort) db).getData(), out);
				break;
			case DataBuffer.TYPE_SHORT:
				writeTo(sm, ((DataBufferShort) db).getData(), out);
				break;
			case DataBuffer.TYPE_INT:
				writeTo(sm, ((DataBufferInt) db).getData(), out);
				break;
			default:
				throw new UnsupportedOperationException(
					"Unsupported Datatype: " + db.getDataType());
		}
	}

	private void initBufferedImage(ImageDescriptor imageDescriptor)
	{
		int rows = imageDescriptor.getRows();
		int cols = imageDescriptor.getColumns();
		int samples = imageDescriptor.getSamples();
		int bitsAllocated = imageDescriptor.getBitsAllocated();
		int bitsStored = imageDescriptor.getBitsStored();
		boolean signed = imageDescriptor.isSigned();
		int dataType = bitsAllocated > 8
										 ? (signed ? DataBuffer.TYPE_SHORT : DataBuffer.TYPE_USHORT)
										 : DataBuffer.TYPE_BYTE;
		ComponentColorModel cm = samples == 1
															 ? new ComponentColorModel(
			ColorSpace.getInstance(ColorSpace.CS_GRAY),
			new int[]{bitsStored},
			false, // hasAlpha
			false, // isAlphaPremultiplied,
			Transparency.OPAQUE,
			dataType)
															 : new ComponentColorModel(
			ColorSpace.getInstance(ColorSpace.CS_sRGB),
			new int[]{bitsStored, bitsStored, bitsStored},
			false, // hasAlpha
			false, // isAlphaPremultiplied,
			Transparency.OPAQUE,
			dataType);

		SampleModel sm = new BandedSampleModel(dataType, cols, rows, samples);
		WritableRaster raster = Raster.createWritableRaster(sm, null);
		bi = new BufferedImage(cm, raster, false, null);
	}

	private void initDecompressor(String tsuid, TransferSyntaxType tsType,
		ImageDescriptor imageDescriptor)
	{
		decompressorParam = ImageReaderFactory.getImageReaderParam(tsuid);
		if (decompressorParam == null)
		{
			throw new UnsupportedOperationException(
				"Unsupported Transfer Syntax: " + tsuid);
		}

		this.decompressor = ImageReaderFactory.getImageReader(decompressorParam);
		this.decompressParam = decompressor.getDefaultReadParam();
	}

	private void writeTo(SampleModel sm, byte[][] bankData, OutputStream out)
		throws IOException
	{
		int h = sm.getHeight();
		int w = sm.getWidth();
		ComponentSampleModel csm = (ComponentSampleModel) sm;
		int len = w * csm.getPixelStride();
		int stride = csm.getScanlineStride();
		if (csm.getBandOffsets()[0] != 0)
		{
			bgr2rgb(bankData[0]);
		}
		for (byte[] b : bankData)
			for (int y = 0, off = 0; y < h; ++y, off += stride)
				out.write(b, off, len);
	}
}
