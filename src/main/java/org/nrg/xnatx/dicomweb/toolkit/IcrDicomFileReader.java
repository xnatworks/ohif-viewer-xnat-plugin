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
package org.nrg.xnatx.dicomweb.toolkit;

import lombok.extern.slf4j.Slf4j;
import org.dcm4che3.data.*;
import org.dcm4che3.imageio.codec.ImageDescriptor;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomStreamException;
import org.dcm4che3.util.SafeClose;
import org.nrg.xnatx.dicomweb.conf.DicomwebDeviceConfiguration;
import org.nrg.xnatx.dicomweb.conf.privateelements.PrivateTag;

import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * @author m.alsad
 */
@Slf4j
public class IcrDicomFileReader
{
	private File file;
	private DicomInputStream dis;
	private Attributes attrs;
	private long dataOffset[];
	private long dataLength[];

	public IcrDicomFileReader(File file) throws IOException
	{
		this.file = file;
	}

	public Attributes read() throws IOException
	{
		try
		{
			dis = new DicomInputStream(file);
			dis.setBulkDataDescriptor(
				DicomwebDeviceConfiguration.getBulkDataDescriptor());
			dis.setIncludeBulkData(DicomInputStream.IncludeBulkData.URI);
			dis.setURI("");
			attrs = dis.readDataset();

			attrs.setString(Tag.TransferSyntaxUID, VR.UI, dis.getTransferSyntax());

			// Do not store data offsets and lengths
			// checkForAndAddPixelDataOffsets();

			Object pixelData = attrs.getValue(Tag.PixelData);
			if (pixelData != null)
			{
				// Set PixelData to BulkData
				VR vr = attrs.getVR(Tag.PixelData);
				attrs.setValue(Tag.PixelData, vr,
					new BulkData(null, "", attrs.bigEndian()));
			}

			// Set BulkData to a relative retrieve URL
			try
			{
				attrs.accept(new Attributes.ItemPointerVisitor()
				{
					@Override
					public boolean visit(Attributes attrs, int tag, VR vr,
						Object value)
					{
						if (value instanceof BulkData)
						{
							BulkData bulkData = (BulkData) value;
							if (tag == Tag.PixelData && itemPointers.isEmpty())
							{
								bulkData.setURI("");
							}
							else
							{
								bulkData.setURI("/bulkdata"
										+ DicomInputStream.toAttributePath(itemPointers, tag));
							}
						}
						return true;
					}
				}, true);
			}
			catch (Exception e)
			{
				log.debug("Could not set retrieveURL: {}", file.getPath());
				attrs = null;
			}
		}
		catch (DicomStreamException exDC)
		{
			// Dcm4Che throws this on scanning some non-DICOM files
			log.debug(
				"DicomStreamException reading non-DICOM file: {}",
				file.getPath());
			attrs = null;
		}
		catch (FileNotFoundException ex)
		{
			// This can be thrown if the file is locked e.g. by Excel
			log.debug(
				"FileNotFoundException reading file: " + file.getPath() +
					" (" + ex.getMessage() + ")");
			attrs = null;
		}
		catch (EOFException exEOF)
		{
			// Dcm4Che throws this on scanning some non-DICOM files
			log.debug("EOFException reading non-DICOM file: {}",
				file.getPath());
			attrs = null;
		}
		catch (IndexOutOfBoundsException exIOOB)
		{
			// Dcm4Che throws this on scanning some non-DICOM files
			log.debug(
				"IndexOutOfBoundsException reading non-DICOM file: {}",
				file.getPath());
			attrs = null;
		}
		catch (NegativeArraySizeException exNAS)
		{
			// Dcm4Che throws this on scanning some non-DICOM files
			log.debug(
				"NegativeArraySizeException reading non-DICOM file: {}",
				file.getPath());
			attrs = null;
		}
		catch (NumberFormatException exNF)
		{
			// Dcm4Che throws this on scanning some non-DICOM files
			log.debug(
				"NumberFormatException reading non-DICOM file: {}",
				file.getPath());
			attrs = null;
		}
		catch (UnsupportedOperationException exUO)
		{
			// Dcm4Che throws this on scanning some non-DICOM files
			log.debug(
				"UnsupportedOperationException reading non-DICOM file: {}",
				file.getPath());
			attrs = null;
		}
		catch (OutOfMemoryError erOOM)
		{
			attrs = null;
			// Dcm4Che can throw this on scanning some non-DICOM files by
			// trying to allocate a stupidly large byte array. Try and recover.
			// Generally not an advisable thing to catch :(
			log.error("OutOfMemoryError reading file: {}",
				file.getPath());
		}
//		catch (InvalidPixelDataException e)
//		{
//			attrs = null;
//			log.error(e.getMessage() + " File: " + file.getPath());
//		}
		catch (Error er)
		{
			attrs = null;
			// Shouldn't happen, log in case it's swallowed
			log.error("Error reading file: " + file.getPath(), er);
			throw er;
		}
		finally
		{
			if (dis != null)
			{
				SafeClose.close(dis);
			}
		}

		if ((attrs != null) && attrs.isEmpty())
		{
			attrs = null;
			log.debug("Zero Attributes found: {}", file.getPath());
		}

		return attrs;
	}

	private void checkForAndAddPixelDataOffsets()
		throws InvalidPixelDataException
	{
		if (!DicomwebUtils.isImage(attrs))
		{
			return;
		}

		ImageDescriptor imageDescriptor = new ImageDescriptor(attrs);

		int numFrames = imageDescriptor.getFrames();
		if (numFrames == 1)
		{
			return;
		}

		Object pixelData = attrs.getValue(Tag.PixelData);

		if (pixelData instanceof Fragments)
		{
			Fragments fragments = (Fragments) pixelData;
			int numFragments = fragments.size() - 1;
			if (numFragments < numFrames)
			{
				throw new InvalidPixelDataException(
					"Invalid fragmentation of frames");
			}

			dataOffset = new long[numFragments];
			dataLength = new long[numFragments];
			int i = 0;
			for (Object fragment : fragments.subList(1, fragments.size()))
			{
				BulkData bulkData = (BulkData) fragment;
				dataOffset[i] = bulkData.offset();
				dataLength[i] = bulkData.longLength();
				i++;
			}
		}
		else
		{
			if (pixelData instanceof BulkData)
			{
				BulkData bulkData = (BulkData) pixelData;

				long frameLength = imageDescriptor.getFrameLength();
				long expectedLength = numFrames * frameLength;
				if (bulkData.longLength() != expectedLength)
				{
					throw new InvalidPixelDataException(
						"Invalid length of PixelData");
				}

				long offset = bulkData.offset();
				dataOffset = new long[] { offset };
				dataLength = new long[] { frameLength };
			}
		}

		if (dataOffset != null && dataOffset.length > 0)
		{
			attrs.setLong(PrivateTag.PrivateCreator, PrivateTag.DataOffsets,
				VR.UL, dataOffset);
			attrs.setLong(PrivateTag.PrivateCreator, PrivateTag.DataLengths,
				VR.UL, dataLength);
		}
	}
}
