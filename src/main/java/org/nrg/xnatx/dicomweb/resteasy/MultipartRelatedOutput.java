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
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * @author m.alsad
 * <p>
 * Refactored from the resteasy library
 */
public class MultipartRelatedOutput extends MultipartOutput
{
	private String startInfo;

	public OutputPart addPart(Object entity, MediaType mediaType,
		String contentId, String contentTransferEncoding)
	{
		OutputPart outputPart = super.addPart(entity, mediaType);
		if (contentTransferEncoding != null)
		{
			outputPart.getHeaders().add("Content-Transfer-Encoding",
				contentTransferEncoding);
		}
		if (contentId != null)
		{
			outputPart.getHeaders().add("Content-ID", contentId);
		}
		return outputPart;
	}

	public OutputPart getRootPart()
	{
		return getParts().get(0);
	}

	public String getStartInfo()
	{
		return startInfo;
	}

	public void setStartInfo(String startInfo)
	{
		this.startInfo = startInfo;
	}

	/**
	 * <a href="https://dicom.nema.org/medical/dicom/current/output/chtml/part18/sect_8.6.html#sect_8.6.1.2.1">
	 *   8.6.1.2.1 Multipart Payload Syntax
	 * </a>
	 */
	@Override
	public void writeTo(OutputStream outputStream) throws IOException
	{
		byte[] boundaryBytes =
			("--" + boundary).getBytes(StandardCharsets.US_ASCII);

		writeParts(outputStream, boundaryBytes);
		outputStream.write(boundaryBytes);
		outputStream.write(RestEasyUtils.DOUBLE_DASH_BYTES);
	}

	protected void writeParts(OutputStream outputStream, byte[] boundaryBytes)
		throws IOException
	{
		for (OutputPart outputPart : parts)
		{
			writePart(outputStream, boundaryBytes, outputPart);
		}
	}

	protected void writePart(OutputStream outputStream, byte[] boundaryBytes,
		OutputPart part) throws IOException
	{
		outputStream.write(boundaryBytes);
		outputStream.write(RestEasyUtils.LINE_SEPARATOR_BYTES);
		HttpHeaders headers = part.getHeaders();
		headers.setContentType(part.getMediaType());

		StreamingResponseBody entity = (StreamingResponseBody) part.getEntity();

		HeaderFlushedOutputStream partStream = new HeaderFlushedOutputStream(
			headers, outputStream)
		{
			@Override
			public void close()
			{
				// no close
				// super.close();
			}
		};
		entity.writeTo(partStream);
		outputStream.write(RestEasyUtils.LINE_SEPARATOR_BYTES);
	}
}
