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

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * @author m.alsad
 * <p>
 * Refactored from the resteasy library
 */
public class HeaderFlushedOutputStream extends OutputStream
{
	private final HttpHeaders headers;
	private final OutputStream stream;
	private boolean headersFlushed = false;

	public HeaderFlushedOutputStream(final HttpHeaders headers,
		final OutputStream delegate)
	{
		this.headers = headers;
		this.stream = delegate;
	}

	@Override
	public void close() throws IOException
	{
		stream.close();
	}

	@Override
	public void flush() throws IOException
	{
		stream.flush();
	}

	protected void flushHeaders() throws IOException
	{
		if (headersFlushed)
		{
			return;
		}

		headersFlushed = true;

		for (String key : headers.keySet())
		{
			for (Object obj : headers.get(key))
			{
				String value = obj.toString();
				stream.write(key.getBytes(StandardCharsets.US_ASCII));
				stream.write(RestEasyUtils.COLON_SPACE_BYTES);
				stream.write(value.getBytes(StandardCharsets.US_ASCII));
				stream.write(RestEasyUtils.LINE_SEPARATOR_BYTES);
			}
		}
		stream.write(RestEasyUtils.LINE_SEPARATOR_BYTES);
	}

	@Override
	public void write(byte[] bytes, int i, int i1) throws IOException
	{
		flushHeaders();
		stream.write(bytes, i, i1);
	}

	@Override
	public void write(int i) throws IOException
	{
		flushHeaders();
		stream.write(i);
	}

	@Override
	public void write(byte[] bytes) throws IOException
	{
		flushHeaders();
		stream.write(bytes);
	}
}