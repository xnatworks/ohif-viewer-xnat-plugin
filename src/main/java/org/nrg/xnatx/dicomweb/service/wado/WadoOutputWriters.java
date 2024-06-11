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
package org.nrg.xnatx.dicomweb.service.wado;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.BulkData;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.json.JSONWriter;
import org.dcm4che3.util.AttributesFormat;
import org.nrg.xnatx.dicomweb.conf.DicomwebDeviceConfiguration;
import org.nrg.xnatx.dicomweb.resteasy.MultipartRelatedOutput;
import org.nrg.xnatx.dicomweb.resteasy.OutputPart;
import org.nrg.xnatx.dicomweb.toolkit.DicomwebUtils;
import org.nrg.xnatx.dicomweb.toolkit.MediaTypeUtils;
import org.nrg.xnatx.dicomweb.toolkit.MediaTypes;
import org.nrg.xnatx.dicomweb.wado.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author mo.alsad
 * <p>
 * Some parts of code are refactored from dcm4chee. Refer to DCM4CHEE_LICENSE.
 */
// ToDo: Check whether it is required to explicitly add Content-Length,
//  Content-Range and Transfer-Encoding.
//  Ref: https://dicom.nema.org/medical/dicom/current/output/chtml/part18/sect_8.4.3.html
//  https://dicom.nema.org/medical/dicom/current/output/chtml/part18/sect_8.6.html (8.6.1.2)
public class WadoOutputWriters
{
	public static void writeBulkdata(MultipartRelatedOutput output,
		WadoRsContext ctx, InstanceInfo inst, int[] attributePath)
	{
		StringBuffer bulkdataUrl = ctx.getRequestUrl();
		StreamingResponseBody entity = new BulkdataOutput(ctx, inst, attributePath);
		MediaType mediaType =
			ctx.getSelectedMediaTypes().get(inst.getSopInstanceUID());
		OutputPart outputPart = output.addPart(entity, mediaType);
		// Contains a URL that references the specific resource corresponding
		// to the representation in the payload. Shall be present if the payload
		// contains a representation of a resource.
		outputPart.getHeaders().put(HttpHeaders.CONTENT_LOCATION,
			Collections.singletonList(bulkdataUrl.toString()));
	}

	public static void writeBulkdata(MultipartRelatedOutput output,
		WadoRsContext ctx, InstanceInfo inst)
	{
		MediaType mediaType =
			ctx.getSelectedMediaTypes().get(inst.getSopInstanceUID());
		StringBuffer bulkdataUrl = ctx.getRequestUrl();
		mkInstanceURL(bulkdataUrl, inst);
		StreamingResponseBody entity;
		ObjectType objectType = ObjectType.objectTypeOf(ctx, inst, 0);
		switch (objectType)
		{
			case UncompressedSingleFrameImage:
			case UncompressedMultiFrameImage:
				entity = new BulkdataOutput(ctx, inst, Tag.PixelData);
				break;
			case CompressedMultiFrameImage:
				if (mediaType == MediaType.APPLICATION_OCTET_STREAM)
				{
					entity = new DecompressPixelDataOutput(ctx, inst);
					break;
				}
				writeCompressedMultiFrameImage(output, ctx, inst, mediaType,
					bulkdataUrl);
				return;
			case CompressedSingleFrameImage:
				if (mediaType == MediaType.APPLICATION_OCTET_STREAM)
				{
					entity = new DecompressPixelDataOutput(ctx, inst);
					break;
				}
			case MPEG2Video:
			case MPEG4Video:
				entity = new CompressedPixelDataOutput(ctx, inst);
				break;
			case EncapsulatedPDF:
			case EncapsulatedCDA:
			case EncapsulatedSTL:
			case EncapsulatedOBJ:
			case EncapsulatedMTL:
			case EncapsulatedGenozip:
				entity = new BulkdataOutput(ctx, inst, Tag.EncapsulatedDocument);
				break;
			default:
				throw new AssertionError("Unexpected object type: " + objectType);
		}
		OutputPart outputPart = output.addPart(entity, mediaType);
		outputPart.getHeaders().put(HttpHeaders.CONTENT_LOCATION,
			Collections.singletonList(bulkdataUrl.toString()));
	}

	public static void writeDICOM(MultipartRelatedOutput output,
		WadoRsContext ctx, InstanceInfo inst)
	{
		Collection<String> acceptableTransferSyntaxes =
			ctx.getAcceptableTransferSyntaxes();
		String selectTransferSyntax = MediaTypeUtils.selectTransferSyntax(
			acceptableTransferSyntaxes, inst.getTransferSyntaxUID());
		output.addPart(
			new DicomObjectOutput(ctx, inst, acceptableTransferSyntaxes),
			MediaTypes.applicationDicomWithTransferSyntax(selectTransferSyntax));
	}

	public static void writeFrames(MultipartRelatedOutput output,
		WadoRsContext ctx, InstanceInfo inst) throws IOException
	{
		int[] frameList = ctx.getFrameList();
		int numFrames = inst.getMetadata().getInt(Tag.NumberOfFrames, 1);
		MediaType mediaType =
			ctx.getSelectedMediaTypes().get(inst.getSopInstanceUID());
		StringBuffer bulkdataUrl = ctx.getRequestUrl();
		bulkdataUrl.setLength(bulkdataUrl.lastIndexOf("/frames/") + 8);
		StreamingResponseBody entity;
		ObjectType objectType = ObjectType.objectTypeOf(ctx, inst, 0);
		switch (objectType)
		{
			case UncompressedMultiFrameImage:
				writeUncompressedFrames(output, ctx, inst, frameList, bulkdataUrl);
				return;
			case CompressedMultiFrameImage:
				if (mediaType == MediaType.APPLICATION_OCTET_STREAM)
				{
					writeDecompressedFrames(output, ctx, inst, frameList, bulkdataUrl);
				}
				else
				{
					writeCompressedFrames(output, ctx, inst, frameList, mediaType,
						bulkdataUrl);
				}
				return;
			case UncompressedSingleFrameImage:
				entity = new BulkdataOutput(ctx, inst, Tag.PixelData);
				break;
			case CompressedSingleFrameImage:
				entity = mediaType == MediaType.APPLICATION_OCTET_STREAM
									 ? new DecompressPixelDataOutput(ctx, inst)
									 : new CompressedPixelDataOutput(ctx, inst);
				break;
			default:
				throw new AssertionError("Unexcepted object type: " + objectType);
		}
		OutputPart outputPart = output.addPart(entity, mediaType);
		bulkdataUrl.append('1');
		outputPart.getHeaders().put(HttpHeaders.CONTENT_LOCATION,
			Collections.singletonList(bulkdataUrl.toString()));
	}

	public static void writeMetadataJSON(WadoRsContext ctx,
		OutputStream out) throws IOException
	{
		try
		{
			JsonGenerator gen = Json.createGenerator(out);
			JSONWriter writer =
				DicomwebUtils.encodeAsJSONNumber(new JSONWriter(gen));
			gen.writeStartArray();
			for (InstanceInfo inst : ctx.getMatches())
			{
				writer.write(loadMetadata(ctx, inst));
			}
			gen.writeEnd();
			gen.flush();
		}
		catch (Exception e)
		{
			throw new IOException("Error in processing metadata", e);
		}
	}

	public static StreamingResponseBody writeZIP(WadoRsContext ctx)
	{
		Collection<String> acceptableZipTransferSyntaxes =
			ctx.getAcceptableZipTransferSyntaxes();

		AttributesFormat pathFormat = new AttributesFormat(
			DicomwebDeviceConfiguration.DEFAULT_WADO_ZIP_ENTRY_NAME_FORMAT);
		List<InstanceInfo> matches = ctx.getMatches();
		return out -> {
			try
			{
				Set<String> dirPaths = new HashSet<>();
				ZipOutputStream zip = new ZipOutputStream(out);
				for (InstanceInfo inst : matches)
				{
					String name = pathFormat.format(inst.getMetadata());
					addDirEntries(zip, name, dirPaths);
					zip.putNextEntry(new ZipEntry(name));
					DicomObjectOutput output =
						new DicomObjectOutput(ctx, inst, acceptableZipTransferSyntaxes);
					output.writeTo(zip);
					zip.closeEntry();
				}
				zip.finish();
				zip.flush();
			}
			catch (Exception ex)
			{
				throw new IOException("Error creating a ZIP file", ex);
			}
		};
	}

	private static void addDirEntries(ZipOutputStream zip, String name,
		Set<String> added) throws IOException
	{
		int endIndex = 0;
		int i;
		while ((i = name.indexOf('/', endIndex)) >= 0)
		{
			String entry = name.substring(0, endIndex = (i + 1));
			if (added.add(entry))
			{
				zip.putNextEntry(new ZipEntry(entry));
				zip.closeEntry();
			}
		}
	}

	private static Attributes loadMetadata(WadoRsContext ctx, InstanceInfo inst)
		throws Exception
	{
		Attributes metadata = inst.getMetadata();
		StringBuffer bulkdataUrl = new StringBuffer(ctx.getRequestUrl());
		bulkdataUrl.setLength(bulkdataUrl.lastIndexOf("/metadata"));
		mkInstanceURL(bulkdataUrl, inst);
		if (!ctx.isIncludePrivateAttributes())
		{
			metadata.removePrivateAttributes();
		}
		setBulkdataURI(metadata, bulkdataUrl.toString());
		return metadata;
	}

	private static void mkInstanceURL(StringBuffer sb, InstanceInfo inst)
	{
		if (sb.lastIndexOf("/instances/") < 0)
		{
			if (sb.lastIndexOf("/series/") < 0)
			{
				sb.append("/series/")
					.append(inst.getMetadata().getString(Tag.SeriesInstanceUID));
			}
			sb.append("/instances/").append(inst.getSopInstanceUID());
		}
	}

	private static void setBulkdataURI(Attributes attrs, String retrieveURL)
	{
		try
		{
			attrs.accept(new Attributes.ItemPointerVisitor()
			{
				@Override
				public boolean visit(Attributes attrs, int tag, VR vr, Object value)
				{
					if (value instanceof BulkData)
					{
						BulkData bulkData = (BulkData) value;
						if (tag == Tag.PixelData && itemPointers.isEmpty())
						{
							bulkData.setURI(retrieveURL);
						}
						else
						{
							bulkData.setURI(retrieveURL + bulkData.getURI());
						}
					}
					return true;
				}
			}, true);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	private static void writeCompressedFrames(MultipartRelatedOutput output,
		WadoRsContext ctx, InstanceInfo inst, int[] frameList,
		MediaType mediaType, StringBuffer bulkdataUrl) throws IOException
	{
		int length = bulkdataUrl.length();
		ctx.compressedFramesOutput = new CompressedFramesOutput(ctx, inst,
			frameList, ctx.getSpoolDirectory(frameList));
		for (int frame : frameList)
		{
			OutputPart outputPart = output.addPart(
				ctx.compressedFramesOutput, mediaType);
			bulkdataUrl.setLength(length);
			bulkdataUrl.append(frame);
			outputPart.getHeaders().put(HttpHeaders.CONTENT_LOCATION,
				Collections.singletonList(bulkdataUrl.toString()));
		}
	}

	private static void writeCompressedMultiFrameImage(
		MultipartRelatedOutput output, WadoRsContext ctx, InstanceInfo inst,
		MediaType mediaType, StringBuffer bulkdataUrl)
	{
		bulkdataUrl.append("/frames/");
		int length = bulkdataUrl.length();
		int numFrames = inst.getMetadata().getInt(Tag.NumberOfFrames, 1);
		ctx.compressedMFPixelDataOutput = new CompressedMFPixelDataOutput(ctx, inst,
			numFrames);
		for (int i = 1; i <= numFrames; i++)
		{
			OutputPart outputPart = output.addPart(ctx.compressedMFPixelDataOutput,
				mediaType);
			bulkdataUrl.setLength(length);
			bulkdataUrl.append(i);
			outputPart.getHeaders().put(HttpHeaders.CONTENT_LOCATION,
				Collections.singletonList(bulkdataUrl.toString()));
		}
	}

	private static void writeDecompressedFrames(MultipartRelatedOutput output,
		WadoRsContext ctx, InstanceInfo inst, int[] frameList,
		StringBuffer bulkdataUrl) throws IOException
	{
		int length = bulkdataUrl.length();
		ctx.decompressFramesOutput = new DecompressFramesOutput(
			ctx, inst, frameList, ctx.getSpoolDirectory(frameList));
		for (int frame : frameList)
		{
			OutputPart outputPart = output.addPart(ctx.decompressFramesOutput,
				MediaType.APPLICATION_OCTET_STREAM);
			bulkdataUrl.setLength(length);
			bulkdataUrl.append(frame);
			outputPart.getHeaders().put(HttpHeaders.CONTENT_LOCATION,
				Collections.singletonList(bulkdataUrl.toString()));
		}
	}

	private static void writeUncompressedFrames(MultipartRelatedOutput output,
		WadoRsContext ctx, InstanceInfo inst, int[] frameList,
		StringBuffer bulkdataUrl) throws IOException
	{
		int length = bulkdataUrl.length();
		ctx.uncompressedFramesOutput = new UncompressedFramesOutput(ctx, inst,
			frameList, ctx.getSpoolDirectory(frameList));
		for (int frame : frameList)
		{
			OutputPart outputPart = output.addPart(ctx.uncompressedFramesOutput,
				MediaType.APPLICATION_OCTET_STREAM);
			bulkdataUrl.setLength(length);
			bulkdataUrl.append(frame);
			outputPart.getHeaders().put(HttpHeaders.CONTENT_LOCATION,
				Collections.singletonList(bulkdataUrl.toString()));
		}
	}
}
