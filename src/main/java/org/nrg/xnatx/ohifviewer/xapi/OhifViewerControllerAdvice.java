/*********************************************************************
 * Copyright (c) 2018, Institute of Cancer Research
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
package org.nrg.xnatx.ohifviewer.xapi;

import icr.etherj.ExceptionCode;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xnatx.dicomweb.xapi.OhifDicomwebApi;
import org.nrg.xnatx.plugin.PluginCode;
import org.nrg.xnatx.plugin.PluginException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

/**
 *
 * @author jamesd
 */
@ControllerAdvice(assignableTypes={
	OhifViewerApi.class,OhifAiaaApi.class, OhifDicomwebApi.class})
public class OhifViewerControllerAdvice
{
	private final static Logger logger = LoggerFactory.getLogger(
		OhifViewerControllerAdvice.class);

	@ExceptionHandler(Exception.class)
	public ResponseEntity<?> onException(WebRequest request, Exception ex)
	{
		String message = "Exception - ";
		message = buildMessage(message, ex);
		logger.error(message, ex);
		message = maybeJsonify(message, request.getHeader("accept"));
		return new ResponseEntity<>(message, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(RuntimeException.class)
	public ResponseEntity<?> onRuntimeException(WebRequest request,
		RuntimeException ex)
	{
		Throwable cause = ex.getCause();
		String message = "RuntimeException - ";
		if (cause == null)
		{
			message = buildMessage(message, ex);
			logger.error(message, ex);
			message = maybeJsonify(message, request.getHeader("accept"));
			return new ResponseEntity<>(message, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		ResponseEntity<?> response = checkXnatCause(request, cause);
		if (response != null)
		{
			return response;
		}
		logger.error(message, ex);
		message = maybeJsonify(message, request.getHeader("accept"));
		return new ResponseEntity<>(message, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(PluginException.class)
	public ResponseEntity<?> onPluginException(WebRequest request,
		PluginException ex)
	{
		String message = ex.getMessage();
		Throwable cause = ex.getCause();
		if (cause != null)
		{
			message += " - Cause: "+cause.getMessage();
		}
		logger.error("Error executing request - "+message, ex);
		message = maybeJsonify(message, request.getHeader("accept"));
		ExceptionCode code = ex.getCode();
		if (code.equals(PluginCode.HttpForbidden))
		{
			return new ResponseEntity<>(message, HttpStatus.FORBIDDEN);
		}
		else if (code.equals(PluginCode.HttpUnprocessableEntity))
		{
			return new ResponseEntity<>(message, HttpStatus.UNPROCESSABLE_ENTITY);
		}
		else if (code.equals(PluginCode.HttpNotFound))
		{
			return new ResponseEntity<>(message, HttpStatus.NOT_FOUND);
		}
		else if (code.equals(PluginCode.HttpBadRequest))
		{
			return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
		}
		else if (code.equals(PluginCode.HttpInternalError))
		{
			return new ResponseEntity<>(message, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		else if (code.equals(PluginCode.DICOMWebNotSupported))
		{
			return new ResponseEntity<>(message, HttpStatus.NOT_IMPLEMENTED);
		}
		return new ResponseEntity<>(message, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	private String buildMessage(String message, Exception ex)
	{
		String exMessage = ex.getMessage();
		if ((exMessage == null) || exMessage.isEmpty())
		{
			message += ex.getClass().getSimpleName()+" thrown";
		}
		else
		{
			message += exMessage;
		}
		return message;
	}

	private ResponseEntity<?> checkXnatCause(WebRequest request, Throwable cause)
	{
		ResponseEntity<?> response = null;
		if (cause instanceof NotFoundException)
		{
			String message = cause.getClass().getSimpleName()+" ";
			String exMessage = cause.getMessage();
			message += ((exMessage == null) || exMessage.isEmpty())
				? "thrown" : "- "+exMessage;
			logger.info(message, cause);
			message = maybeJsonify(message, request.getHeader("accept"));
			return new ResponseEntity<>(message, HttpStatus.UNPROCESSABLE_ENTITY);
		}
		return response;
	}

	private String maybeJsonify(String message, String acceptType)
	{
		if ((acceptType != null) && (acceptType.equals("application/json")))
		{
			message = "{ \"ResponseMessage\":\""+message+"\" }";
		}
		return message;
	}

}
