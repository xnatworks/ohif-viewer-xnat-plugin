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
package org.nrg.xnatx.dicomweb.service.qido;

import lombok.extern.slf4j.Slf4j;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.IDWithIssuer;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.nrg.xnatx.dicomweb.conf.AttributeSet;
import org.nrg.xnatx.dicomweb.conf.DicomwebDeviceConfiguration;
import org.nrg.xnatx.dicomweb.service.query.Query;
import org.nrg.xnatx.dicomweb.service.query.DicomwebDataService;
import org.nrg.xnatx.dicomweb.toolkit.DicomwebUtils;
import org.nrg.xnatx.dicomweb.toolkit.MediaTypeUtils;
import org.nrg.xnatx.dicomweb.toolkit.MediaTypes;
import org.nrg.xnatx.dicomweb.conf.QIDO;
import org.nrg.xnatx.dicomweb.conf.QueryAttributes;
import org.nrg.xnatx.plugin.PluginCode;
import org.nrg.xnatx.plugin.PluginException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;


/**
 * @author mo.alsad
 *
 * Some parts of code are refactored from dcm4chee. Refer to DCM4CHEE_LICENSE.
 */
@Service
@Slf4j
public class QidoRsService
{
	private final DicomwebDataService dwDataService;

	@Autowired
	public QidoRsService(final DicomwebDataService dwDataService)
	{
		this.dwDataService = dwDataService;
	}

	public ResponseEntity<StreamingResponseBody> search(
		final Map<String,String> xnatIds,
		final HttpServletRequest request,
		final MultiValueMap<String,String> httpQueryParams, QidoRsModel model,
		String studyInstanceUid, String seriesInstanceUid, QIDO qido)
		throws PluginException
	{
		StringBuffer requestUrl = request.getRequestURL();
		QidoRsOutput output = selectOutput(request, httpQueryParams);
		output.setRequestUrl(requestUrl.toString());

		try
		{
			Map<String,AttributeSet> attributeSetMap =
				DicomwebDeviceConfiguration.getAttributeSet(AttributeSet.Type.QIDO_RS);
			QueryAttributes queryAttrs = new QueryAttributes(httpQueryParams,
				attributeSetMap, xnatIds);
			final QidoRsContext ctx = newQueryContext(httpQueryParams, queryAttrs,
				model, studyInstanceUid, seriesInstanceUid, qido);
			ctx.setXnatIds(xnatIds);

			try (Query query = dwDataService.createQidoRsQuery(ctx))
			{
				int maxResults = DicomwebDeviceConfiguration.QIDO_MAX_NUMBER_OF_RESULTS;
				int offset = ctx.getOffset();
				int limit = ctx.getLimit();
				int remaining = 0;
				if (limit == 0 || limit > maxResults)
				{
					log.debug("QIDO-RS query for number of matching {}s", model);
					dwDataService.fetchCount(query);
					long matches = query.getCount();
					log.debug("Number of matching {}s: {}", model, matches);
					int numResults = (int) (matches - offset);
					if (numResults <= 0)
					{
						log.debug("Offset {} >= {}", offset, matches);
						return new ResponseEntity<>(HttpStatus.NO_CONTENT);
					}
					remaining = numResults - maxResults;
				}
				log.debug("QIDO-RS query for matching {}s", model);
				dwDataService.fetchQuery(query, remaining > 0 ? maxResults : limit);
				if (!query.hasMoreMatches())
				{
					log.debug("No matching {}s found", model);
					return new ResponseEntity<>(HttpStatus.NO_CONTENT);
				}

				// Add headers
				HttpHeaders headers = new HttpHeaders();
				if (remaining > 0)
				{
					String warning = "299 There are " + remaining +
										 " additional results that can be requested subsequently.";
					headers.add("Warning", warning);
				}
				headers.add("Cache-Control", "no-cache");
				headers.setContentType(output.type());

				// Add body
				StreamingResponseBody body = output.entity(query, model);

				return new ResponseEntity<>(body, headers, HttpStatus.OK);
			}
		}
		catch (Exception e)
		{
			throw new PluginException(PluginCode.HttpInternalError, e);
		}
	}

	private QidoRsContext newQueryContext(
		MultiValueMap<String,String> httpQueryParams, QueryAttributes queryAttrs,
		QidoRsModel model, String studyInstanceUid, String seriesInstanceUid,
		QIDO qido) throws Exception
	{
		QidoRsContext ctx = new QidoRsContext(dwDataService);

		ctx.setQueryRetrieveLevel(model.getQueryRetrieveLevel());

		// ToDo - fuzzymatching is not supported
		// ToDo - search by date/time range is not supported
		int offset = DicomwebUtils.parseInt(httpQueryParams.getFirst("offset"),
			"0|([1-9]\\d{0,4})");
		ctx.setOffset(offset);

		int limit = DicomwebUtils.parseInt(httpQueryParams.getFirst("limit"),
			"[1-9]\\d{0,4}");
		ctx.setLimit(limit);

		boolean fuzzymatching = Boolean.parseBoolean(
			httpQueryParams.getFirst("fuzzymatching"));
		ctx.setFuzzymatching(fuzzymatching);

		Attributes queryKeys = queryAttrs.getQueryKeys();
		ctx.setQueryKeys(queryKeys);

		IDWithIssuer idWithIssuer = IDWithIssuer.pidOf(queryKeys);
		if (idWithIssuer != null && !idWithIssuer.getID().equals("*"))
		{
			ctx.setPatientIds(idWithIssuer);
		}
		ctx.setOrderByTags(queryAttrs.getOrderByTags());
		ctx.setReturnPrivate(queryAttrs.isIncludePrivate());
		ctx.setReturnKeys(queryAttrs.isIncludeAll()
												? null
												: queryAttrs.isIncludeDefaults() || queryKeys.isEmpty()
														? queryAttrs.getReturnKeys(qido.includedTags)
														: queryKeys);

		if (studyInstanceUid != null)
		{
			queryKeys.setString(Tag.StudyInstanceUID, VR.UI, studyInstanceUid);
		}
		if (seriesInstanceUid != null)
		{
			queryKeys.setString(Tag.SeriesInstanceUID, VR.UI, seriesInstanceUid);
		}

		return ctx;
	}

	private QidoRsOutput selectOutput(final HttpServletRequest request,
		final MultiValueMap<String,String> httpQueryParams) throws PluginException
	{
		List<String> headers = Collections.list(request.getHeaders("accept"));
		List<String> accept = httpQueryParams.get("accept");

		List<MediaType> acceptableMediaTypes = MediaTypeUtils.acceptableMediaTypesOf(
			headers, accept);

		if (acceptableMediaTypes.stream().anyMatch(
			((Predicate<MediaType>) MediaTypes.APPLICATION_DICOM_JSON::isCompatibleWith)
				.or(MediaType.APPLICATION_JSON::isCompatibleWith)))
		{
			return QidoRsOutput.JSON;
		}

		throw new PluginException("Media type in request is not acceptable",
			PluginCode.HttpMethodNotAcceptable);
	}
}
