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
package org.nrg.xnatx.dicomweb.wado;

import lombok.extern.slf4j.Slf4j;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.util.StreamUtils;
import org.dcm4che3.util.TagUtils;
import org.nrg.xnatx.dicomweb.service.wado.WadoRsContext;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author mo.alsad
 * <p>
 * Some parts of code are refactored from dcm4chee. Refer to DCM4CHEE_LICENSE.
 */
@Slf4j
public class BulkdataOutput implements StreamingResponseBody
{
	private final WadoRsContext ctx;
	private final InstanceInfo inst;
	private final int[] attributePath;

	public BulkdataOutput(WadoRsContext ctx, InstanceInfo inst,
		int... attributePath)
	{
		this.ctx = ctx;
		this.inst = inst;
		this.attributePath = attributePath;
	}

	@Override
	public void writeTo(final OutputStream out) throws IOException
	{
		try (DicomInputStream dis = WadoUtils.openDicomInputStream(ctx, inst))
		{
			Attributes attrs = null;
			for (int level = 0; level < attributePath.length; level++)
			{
				if ((level & 1) == 0)
				{
					int stopTag = attributePath[level];
					if (attrs == null)
					{
						attrs = dis.readDataset(-1, stopTag);
					}
					else
					{
						dis.readAttributes(attrs, -1, stopTag);
					}
					if (dis.tag() != stopTag)
					{
						throw new IOException(missingBulkdata());
					}
				}
				else
				{
					int index = attributePath[level];
					int i = 0;
					while (i < index && dis.readItemHeader())
					{
						int len = dis.length();
						boolean undefLen = len == -1;
						if (undefLen)
						{
							Attributes item = new Attributes(attrs.bigEndian());
							dis.readAttributes(item, len, Tag.ItemDelimitationItem);
						}
						else
						{
							dis.skipFully(len);
						}
						++i;
					}
					if (i < index || !dis.readItemHeader())
					{
						throw new IOException(missingBulkdata());
					}
				}
			}
			log.debug("Start writing {} bytes", dis.length());
			StreamUtils.copy(dis, out, dis.length());
			log.debug("Finished writing {} bytes", dis.length());
		}
	}

	private String missingBulkdata()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("No bulkdata ");
		for (int i = 0; i < attributePath.length; i++)
		{
			if ((i & 1) == 0)
			{
				sb.append(TagUtils.toString(attributePath[i]));
			}
			else
			{
				sb.append('[').append(attributePath[i]).append(']');
			}
		}
		sb.append(" in requested object");
		return sb.toString();
	}
}
