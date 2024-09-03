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

import org.dcm4che3.data.Tag;
import org.nrg.xnatx.dicomweb.resteasy.MultipartRelatedOutput;
import org.nrg.xnatx.dicomweb.resteasy.OutputPart;
import org.nrg.xnatx.dicomweb.resteasy.RestEasyUtils;
import org.nrg.xnatx.dicomweb.toolkit.HttpUtils;
import org.nrg.xnatx.dicomweb.toolkit.MediaTypeWrapper;
import org.nrg.xnatx.dicomweb.toolkit.MediaTypes;
import org.nrg.xnatx.dicomweb.toolkit.WebApplicationException;
import org.nrg.xnatx.dicomweb.wado.InstanceInfo;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author mo.alsad
 * <p>
 * Some parts of code are refactored from dcm4chee. Refer to DCM4CHEE_LICENSE.
 */
public enum WadoRsOutput
{
	/**
	 * DICOM Instances encoded in PS3.10 format
	 */
	DICOM
		{
			@Override
			List<InstanceInfo> removeNotAcceptedMatches(WadoRsContext ctx)
			{
				return Collections.EMPTY_LIST;
			}

			@Override
			void addPart(MultipartRelatedOutput output, WadoRsContext ctx,
				InstanceInfo inst)
			{
				WadoOutputWriters.writeDICOM(output, ctx, inst);
			}

			@Override
			StreamingResponseBody entity(WadoRsContext ctx)
			{
				MultipartRelatedOutput output = new MultipartRelatedOutput();
				for (InstanceInfo inst : ctx.getMatches())
				{
					addPart(output, ctx, inst);
				}
				return output;
			}

			@Override
			ResponseEntity<StreamingResponseBody> response(
				StreamingResponseBody entity, WadoRsContext ctx)
			{
				return buildMultiPartRelatedResponse(
					(MultipartRelatedOutput) entity, ctx);
			}
		},
	ZIP
		{
			@Override
			List<InstanceInfo> removeNotAcceptedMatches(WadoRsContext ctx)
			{
				return Collections.EMPTY_LIST;
			}

			@Override
			StreamingResponseBody entity(WadoRsContext ctx)
			{
				return WadoOutputWriters.writeZIP(ctx);
			}

			@Override
			ResponseEntity<StreamingResponseBody> response(
				StreamingResponseBody entity, WadoRsContext ctx)
			{
				return buildResponse(entity, ctx, MediaTypes.APPLICATION_ZIP);
			}
		},
	BULKDATA
		{
			@Override
			void addPart(MultipartRelatedOutput output, WadoRsContext ctx,
				InstanceInfo inst)
			{
				WadoOutputWriters.writeBulkdata(output, ctx, inst);
			}

			@Override
			ResponseEntity<StreamingResponseBody> response(
				StreamingResponseBody entity, WadoRsContext ctx)
			{
				return buildMultiPartRelatedResponse(
					(MultipartRelatedOutput) entity, ctx);
			}
		},
	BULKDATA_FRAME
		{
			@Override
			MediaType[] mediaTypesFor(WadoRsContext ctx, InstanceInfo inst,
				int frame)
			{
				ObjectType objectType = ObjectType.objectTypeOf(ctx, inst, frame);
				return objectType.isImage()
								 ? objectType.getBulkdataContentTypes(inst)
								 : null;
			}

			@Override
			void addPart(MultipartRelatedOutput output, WadoRsContext ctx,
				InstanceInfo inst) throws IOException
			{
				WadoOutputWriters.writeFrames(output, ctx, inst);
			}

			@Override
			ResponseEntity<StreamingResponseBody> response(
				StreamingResponseBody entity, WadoRsContext ctx)
			{
				return buildMultiPartRelatedResponse(
					(MultipartRelatedOutput) entity, ctx);
			}
		},
	BULKDATA_PATH
		{
			@Override
			MediaType[] mediaTypesFor(WadoRsContext ctx, InstanceInfo inst,
				int frame)
			{
				return isEncapsulatedDocument(ctx.getAttributePath())
								 ? super.mediaTypesFor(ctx, inst, 0)
								 : new MediaType[]{MediaType.APPLICATION_OCTET_STREAM};
			}

			@Override
			void addPart(MultipartRelatedOutput output, WadoRsContext ctx,
				InstanceInfo inst)
			{
				WadoOutputWriters.writeBulkdata(output, ctx, inst,
					ctx.getAttributePath());
			}

			@Override
			ResponseEntity<StreamingResponseBody> response(
				StreamingResponseBody entity, WadoRsContext ctx)
			{
				return buildMultiPartRelatedResponse(
					(MultipartRelatedOutput) entity, ctx);
			}
		},
	METADATA_JSON
		{
			@Override
			List<InstanceInfo> removeNotAcceptedMatches(WadoRsContext ctx)
			{
				return Collections.EMPTY_LIST;
			}

			@Override
			public StreamingResponseBody entity(WadoRsContext ctx) throws IOException
			{
				return out -> WadoOutputWriters.writeMetadataJSON(ctx, out);
			}

			@Override
			ResponseEntity<StreamingResponseBody> response(
				StreamingResponseBody entity, WadoRsContext ctx)
			{
				return buildResponse(entity, ctx, MediaTypes.APPLICATION_DICOM_JSON);
			}
		};

	void addPart(MultipartRelatedOutput output, WadoRsContext ctx,
		InstanceInfo inst) throws IOException
	{
		throw new WebApplicationException(
			HttpUtils.errorResponse("Not implemented",
				HttpStatus.SERVICE_UNAVAILABLE));
	}

	ResponseEntity<StreamingResponseBody> buildResponse(
		StreamingResponseBody entity, WadoRsContext ctx, MediaType contentType)
	{
		ResponseEntity.BodyBuilder builder = ResponseEntity.status(
			ctx.isPartialContent() ? HttpStatus.PARTIAL_CONTENT : HttpStatus.OK
		);
		builder.contentType(contentType);

		return builder.body(entity);
	}

	ResponseEntity<StreamingResponseBody> buildMultiPartRelatedResponse(
		MultipartRelatedOutput entity, WadoRsContext ctx)
	{
		ResponseEntity.BodyBuilder builder = ResponseEntity.status(
			ctx.isPartialContent() ? HttpStatus.PARTIAL_CONTENT : HttpStatus.OK
		);

		HttpHeaders headers = buildMultipartRelatedHeaders(entity);
		builder.headers(headers);

		return builder.body(entity);
	}

	HttpHeaders buildMultipartRelatedHeaders(MultipartRelatedOutput entity)
	{
		// Add content IDS
		for (OutputPart outputPart : entity.getParts())
		{
			if (outputPart.getHeaders().get("Content-ID") == null)
			{
				outputPart.getHeaders().put("Content-ID",
					Collections.singletonList(RestEasyUtils.generateContentID()));
			}
		}

		OutputPart rootOutputPart = entity.getRootPart();
		MediaType rootMediaType = rootOutputPart.getMediaType();
		Map<String, String> mediaTypeParameters = new LinkedHashMap<>(
			rootMediaType.getParameters());

		// Add boundary
		String boundary = mediaTypeParameters.get("boundary");
		if (boundary == null)
		{
			boundary = entity.getBoundary();
		}
		else
		{
			entity.setBoundary(boundary);
		}

		// Add type and transfer-syntax
		mediaTypeParameters.put("type",
			rootMediaType.getType() + "/" + rootMediaType.getSubtype());
		String ts = rootMediaType.getParameters().get("transfer-syntax");
		if (ts != null)
		{
			mediaTypeParameters.put("transfer-syntax", ts);
		}

		// Add start
		// Typically, the "start" and "start-info" parameters are not specified,
		// and the "root" is the first body part.
		// ref: https://dicom.nema.org/medical/dicom/current/output/chtml/part18/sect_8.7.html
		mediaTypeParameters.put("start",
			rootOutputPart.getHeaders().getFirst("Content-ID"));
		if (entity.getStartInfo() != null)
		{
			mediaTypeParameters.put("start-info", entity.getStartInfo());
		}

		MediaType multipartType = MediaTypes.MULTIPART_RELATED;
		MediaTypeWrapper multipartTypeWrapper =
			new MediaTypeWrapper(multipartType, mediaTypeParameters);
		mediaTypeParameters.put("boundary", boundary);
		// ToDo: remove commented code below
//		StringBuilder sb = new StringBuilder();
//		sb.append("multipart/related");
//		for (Map.Entry<String, String> param : mediaTypeParameters.entrySet())
//		{
//			sb.append(';');
//			sb.append(param.getKey());
//			sb.append('=');
//			sb.append(param.getValue());
//		}
//		sb.append("; boundary=");
//		sb.append(boundary);

		HttpHeaders headers = new HttpHeaders();
		headers.put(HttpHeaders.CONTENT_TYPE,
			Collections.singletonList(multipartTypeWrapper.toString()));

		return headers;
	}

	// For BULKDATA* output types
	StreamingResponseBody entity(WadoRsContext ctx) throws IOException
	{
		MultipartRelatedOutput out = new MultipartRelatedOutput();
		for (InstanceInfo inst : ctx.getMatches())
		{
			addPart(out, ctx, inst);
		}
		return out;
	}

	boolean isEncapsulatedDocument(int[] attributePath)
	{
		return attributePath.length == 1
						 && attributePath[0] == Tag.EncapsulatedDocument;
	}

	MediaType[] mediaTypesFor(WadoRsContext ctx, InstanceInfo inst,
		int frame)
	{
		ObjectType objectType = ObjectType.objectTypeOf(ctx, inst, frame);
		return objectType.getBulkdataContentTypes(inst);
	}

	List<InstanceInfo> removeNotAcceptedMatches(WadoRsContext ctx)
	{
		int[] frameList = ctx.getFrameList();
		List<InstanceInfo> matches = ctx.getMatches();
		List<InstanceInfo> notAcceptable = new ArrayList<>(matches.size());
		Map<String,MediaType> selectedMediaTypes = ctx.getSelectedMediaTypes();
		Iterator<InstanceInfo> iter = matches.iterator();
		while (iter.hasNext())
		{
			InstanceInfo match = iter.next();
			MediaType[] mediaTypes =
				mediaTypesFor(ctx, match, frameList == null ? 0 : 1);
			if (mediaTypes == null)
			{
				iter.remove();
				continue;
			}
			MediaType mediaType = WadoRsContext.selectMediaType(
				ctx.getAcceptableMultipartRelatedMediaTypes(), mediaTypes);
			if (mediaType != null)
			{
				selectedMediaTypes.put(match.getSopInstanceUID(), mediaType);
			}
			else
			{
				iter.remove();
				notAcceptable.add(match);
			}
		}
		return notAcceptable;
	}

	abstract ResponseEntity<StreamingResponseBody> response(
		StreamingResponseBody entity, WadoRsContext ctx);
}
