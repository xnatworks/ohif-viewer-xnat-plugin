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

import org.springframework.http.MediaType;

import java.util.Map;

/**
 * @author m.alsad
 *
 * A wrapper class of org.springframework.http.MediaType
 * Used to avoid throwing the IllegalArgumentException when a parameter
 * contains illegal characters.
 */
public class MediaTypeWrapper
{
	private final MediaType mediaType;
	private final Map<String,String> parameters;

	public MediaTypeWrapper(MediaType mediaType, Map<String,String> parameters)
	{
		this.mediaType = mediaType;
		this.parameters = parameters;
	}

	protected void appendTo(StringBuilder builder)
	{
		builder.append(mediaType.getType());
		builder.append('/');
		builder.append(mediaType.getSubtype());
		appendTo(parameters, builder);
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		appendTo(builder);
		return builder.toString();
	}

	private void appendTo(Map<String,String> map, StringBuilder builder)
	{
		for (Map.Entry<String,String> entry : map.entrySet())
		{
			builder.append(';');
			builder.append(entry.getKey());
			builder.append('=');
			builder.append(entry.getValue());
		}
	}
}
