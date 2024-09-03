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
import org.nrg.xnatx.dicomweb.service.wado.WadoRsContext;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author mo.alsad
 * <p>
 * Some parts of code are refactored from dcm4chee. Refer to DCM4CHEE_LICENSE.
 */
@Slf4j
public class DecompressFramesOutput extends DecompressSupport
	implements StreamingResponseBody
{
	private final int[] frameList;
	private final Path[] spoolFiles;
	private final Path spoolDirectory;
	private int frame = 1;
	private int frameListIndex;

	public DecompressFramesOutput(WadoRsContext ctx, InstanceInfo inst,
		int[] frameList, Path spoolDirectory)
	{
		super(ctx, inst);
		this.frameList = frameList;
		this.spoolDirectory = spoolDirectory;
		this.spoolFiles = spoolDirectory != null ? new Path[this.frameList.length] : null;
	}

	@Override
	public void writeTo(OutputStream out) throws IOException
	{
		try
		{
			if (frameListIndex == 0)
			{
				initEncapsulatedPixelData();
			}

			if (encapsulatedPixelData == null)
			{
				Files.copy(spoolFiles[frameListIndex++], out);
				return;
			}
			int nextFrame = frameList[frameListIndex++];
			while (frame < nextFrame)
			{
				skipFrame();
				frame++;
			}

			decompressFrame(frame);
			log.debug("Start writing decompressed frame of {}", inst);
			writeFrameTo(out);
			log.debug("Finished writing decompressed frame of {}", inst);
			frame++;
			if (allFramesRead())
			{
				close();
			}
		}
		catch (IOException e)
		{
			close();
			throw e;
		}
	}

	private boolean allFramesRead()
	{
		for (int i = frameListIndex; i < frameList.length; i++)
		{
			if (frame <= frameList[i])
			{
				return false;
			}
		}
		return true;
	}

	private void skipFrame() throws IOException
	{
		for (int i = frameListIndex; i < frameList.length; i++)
		{
			if (frame == frameList[i])
			{
				spoolFiles[i] = Files.createTempFile(spoolDirectory, null, null);
				try (OutputStream out = Files.newOutputStream(spoolFiles[i]))
				{
					decompressFrame(frame);
					writeFrameTo(out);
				}
				return;
			}
		}
		if (!encapsulatedPixelData.seekNextFrame())
		{
			throw new IOException(
				"Number of data fragments not sufficient for number of frames in requested object");
		}
	}

}
