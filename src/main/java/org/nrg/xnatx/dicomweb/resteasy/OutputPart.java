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

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.lang.reflect.Type;

/**
 * @author m.alsad
 * <p>
 * Refactored from the resteasy library
 */
public class OutputPart
{
	private HttpHeaders headers = new HttpHeaders();
	private Object entity;
	private Class<?> type;
	private Type genericType;
	private MediaType mediaType;
	private String filename;
	private boolean utf8Encode;

	public OutputPart(final Object entity, final Class<?> type,
		final Type genericType, final MediaType mediaType)
	{
		this(entity, type, genericType, mediaType, null);
	}

	public OutputPart(final Object entity, final Class<?> type,
		final Type genericType, final MediaType mediaType, final String filename)
	{
		this(entity, type, genericType, mediaType, null, false);
	}

	public OutputPart(final Object entity, final Class<?> type,
		final Type genericType, final MediaType mediaType, final String filename,
		final boolean utf8Encode)
	{
		this.entity = entity;
		this.type = type;
		this.genericType = genericType;
		this.mediaType = mediaType;
		this.filename = filename;
		this.utf8Encode = utf8Encode;
	}

	public Object getEntity()
	{
		return entity;
	}

	public String getFilename()
	{
		return filename;
	}

	public Type getGenericType()
	{
		return genericType;
	}

	public HttpHeaders getHeaders()
	{
		return headers;
	}

	public MediaType getMediaType()
	{
		return mediaType;
	}

	public Class<?> getType()
	{
		return type;
	}

	public boolean isUtf8Encode()
	{
		return utf8Encode;
	}
}
