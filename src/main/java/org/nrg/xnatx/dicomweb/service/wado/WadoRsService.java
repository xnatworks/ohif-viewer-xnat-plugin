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

import lombok.extern.slf4j.Slf4j;
import org.dcm4che3.data.Tag;
import org.nrg.xnatx.dicomweb.conf.DicomwebDeviceConfiguration;
import org.nrg.xnatx.dicomweb.service.query.DicomwebDataService;
import org.nrg.xnatx.dicomweb.service.query.impl.RetrieveQuery;
import org.nrg.xnatx.dicomweb.toolkit.HttpUtils;
import org.nrg.xnatx.dicomweb.toolkit.WebApplicationException;
import org.nrg.xnatx.dicomweb.wado.InstanceInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;

/**
 * @author mo.alsad
 * <p>
 * Some parts of code are refactored from dcm4chee. Refer to DCM4CHEE_LICENSE.
 */
@Service
@Slf4j
public class WadoRsService
{
	private final DicomwebDataService dwDataService;

	@Autowired
	public WadoRsService(DicomwebDataService dwDataService) {
		this.dwDataService = dwDataService;
	}

	public DeferredResult<ResponseEntity<StreamingResponseBody>> retrieve(
		final Map<String,String> xnatIds, final HttpServletRequest request,
		final MultiValueMap<String,String> httpQueryParams, WadoRsTarget target,
		String studyUid, String seriesUid, String instanceUid,
		int[] frameList, int[] attributePath)
	{
		StringBuffer requestUrl = HttpUtils.fixUrlScheme(
			request.getRequestURL().toString());

		final WadoRsContext ctx = new WadoRsContext(target, xnatIds, studyUid,
			seriesUid, instanceUid, frameList, attributePath);
		ctx.setRequestUrl(requestUrl);
		ctx.setIncludePrivateAttributes(
			DicomwebDeviceConfiguration.WADO_INCLUDE_PRIVATE);
		ctx.initAcceptableMediaTypes(request, httpQueryParams);

		DeferredResult<ResponseEntity<StreamingResponseBody>> response = new DeferredResult<>();

		response.onCompletion(() -> {
			ctx.closeOutputStreams();
			ctx.purgeSpoolDirectory();
		});

		ForkJoinPool.commonPool().submit(() -> {
			buildResponse(target, ctx, response);
		});

		return response;
	}

	private void buildResponse(WadoRsTarget target, WadoRsContext ctx,
		DeferredResult<ResponseEntity<StreamingResponseBody>> response)
	{
		try
		{
			WadoRsOutput output = selectOutput(ctx);
			log.debug("Query for matching {} with output type {}", target, output);
			RetrieveQuery query = new RetrieveQuery(ctx);
			dwDataService.fetchRetrieveQuery(query);
			List<InstanceInfo> matches = ctx.getMatches();

			if (matches.isEmpty())
			{
				throw new WebApplicationException(
					HttpUtils.errorResponse("No matches found",
					HttpStatus.NOT_FOUND));
			}

			if (ctx.getFrameList() != null)
			{
				InstanceInfo firstMatch = matches.get(0);
				if (!firstMatch.isImage())
				{
					throw new WebApplicationException(
						HttpUtils.errorResponse("Not an image",
						HttpStatus.NOT_FOUND));
				}
				ctx.adjustFrameList(
					firstMatch.getMetadata().getInt(Tag.NumberOfFrames, 1));
				int[] adjustedFrameList = ctx.getFrameList();
				if (adjustedFrameList.length == 0)
				{
					throw new WebApplicationException(
						HttpUtils.errorResponse("Invalid frame number",
						HttpStatus.NOT_FOUND));
				}
			}

			List<InstanceInfo> notAccepted = output.removeNotAcceptedMatches(ctx);
			// Check matches again as they may have been adjusted
			if (ctx.getMatches().isEmpty())
			{
				ResponseEntity<?> errorResponse =
					notAccepted.isEmpty()
						? HttpUtils.errorResponse(
							"No matches found", HttpStatus.NOT_FOUND)
						: HttpUtils.errorResponse(
							"Not accepted instances present", HttpStatus.NOT_ACCEPTABLE);
				throw new WebApplicationException(errorResponse);
			}

			if (!notAccepted.isEmpty())
			{
				ctx.setPartialContent(true);
			}

			StreamingResponseBody entity = output.entity(ctx);
			response.setResult(output.response(entity, ctx));
		}
		catch (Exception ex)
		{
			log.debug("WADO-RS Error", ex);
			if (ex instanceof WebApplicationException)
			{
				response.setErrorResult(((WebApplicationException) ex).getResponse());
			}
			else
			{
				response.setErrorResult(HttpUtils.errorResponse(ex.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR));
			}
		}
	}

	private WadoRsOutput selectOutput(WadoRsContext ctx)
	{
		WadoRsTarget target = ctx.getTarget();

		switch (target)
		{
			case Study:
			case Series:
			case Instance:
				return ctx.dicomOrBulkdataOrZip();
			case Frame:
				return ctx.bulkdataFrame();
			case Bulkdata:
				return ctx.bulkdataPath();
			case StudyMetadata:
			case SeriesMetadata:
			case InstanceMetadata:
				return ctx.metadataJSONorXML();
		}

		throw new WebApplicationException("Unsupported Target or Media type",
			HttpStatus.NOT_ACCEPTABLE);
	}
}
