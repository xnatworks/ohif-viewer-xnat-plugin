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
package org.nrg.xnatx.dicomweb.resteasy;

import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author m.alsad
 * <p>
 * Refactored from the resteasy library
 */
abstract public class MultipartOutput implements StreamingResponseBody
{
	protected List<OutputPart> parts = new ArrayList<OutputPart>();
	protected String boundary = UUID.randomUUID().toString();

	public OutputPart addPart(Object entity, MediaType mediaType)
	{
		OutputPart outputPart = new OutputPart(entity, entity.getClass(), null,
			mediaType);
		parts.add(outputPart);
		return outputPart;
	}

	public OutputPart addPart(Object entity, MediaType mediaType, String filename)
	{
		return addPart(entity, mediaType, filename, false);
	}

	public OutputPart addPart(Object entity, MediaType mediaType, String filename,
		boolean utf8Encode)
	{
		OutputPart outputPart = new OutputPart(entity, entity.getClass(), null,
			mediaType, filename, utf8Encode);
		parts.add(outputPart);
		return outputPart;
	}

	public OutputPart addPart(Object entity, Class<?> type, Type genericType,
		MediaType mediaType)
	{
		OutputPart outputPart = new OutputPart(entity, type, genericType,
			mediaType);
		parts.add(outputPart);
		return outputPart;
	}

	public OutputPart addPart(Object entity, Class<?> type, Type genericType,
		MediaType mediaType, String filename)
	{
		return addPart(entity, type, genericType, mediaType, filename, false);
	}

	public OutputPart addPart(Object entity, Class<?> type, Type genericType,
		MediaType mediaType, String filename, boolean utf8Encode)
	{
		OutputPart outputPart = new OutputPart(entity, type, genericType, mediaType,
			filename, utf8Encode);
		parts.add(outputPart);
		return outputPart;
	}

	public String getBoundary()
	{
		return boundary;
	}

	public void setBoundary(String boundary)
	{
		this.boundary = boundary;
	}

	public List<OutputPart> getParts()
	{
		return parts;
	}
}
