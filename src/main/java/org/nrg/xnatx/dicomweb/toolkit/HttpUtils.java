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
import org.nrg.xdat.XDAT;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * @author m.alsad
 */
@Slf4j
public class HttpUtils
{
	public static ResponseEntity<?> errorResponse(String errorMessage,
		HttpStatus status)
	{
		log.warn("Response {} caused by {}", status, errorMessage);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.TEXT_PLAIN);
		String body = "Error: " + errorMessage;
		return new ResponseEntity<>(body, headers, status);
	}

	public static StringBuffer fixUrlScheme(String requestUrl)
	{
		String siteUrl = XDAT.getSiteUrl();
		String modifiedSiteUrl = siteUrl + requestUrl.substring(requestUrl.indexOf("/xapi/viewerDicomweb"));

		return new StringBuffer(modifiedSiteUrl);
	}
}
