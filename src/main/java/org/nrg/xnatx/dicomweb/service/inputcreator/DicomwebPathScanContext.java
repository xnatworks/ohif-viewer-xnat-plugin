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
package org.nrg.xnatx.dicomweb.service.inputcreator;

import icr.etherj.PathScanContext;
import lombok.extern.slf4j.Slf4j;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.nrg.xnatx.dicomweb.conf.DicomwebDeviceConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

/**
 * @author m.alsad
 */
@Slf4j
public class DicomwebPathScanContext implements PathScanContext<Attributes>
{
	private final DicomwebInput dicomwebInput;

	public DicomwebPathScanContext(final Map<String,String> xnatIds,
		final String experimentPath)
	{
		this.dicomwebInput = new DicomwebInput(xnatIds, experimentPath);
	}

	public DicomwebInput getDicomwebInput()
	{
		return dicomwebInput;
	}

	public void notifyItemFound(File file, Attributes attrs)
	{
		processInstance(attrs, file.toPath());
	}

	@Override
	public void notifyScanFinish()
	{
		dicomwebInput.validateAndUpdateQueryAttributes();
	}

	@Override
	public void notifyScanStart() {}

	private void processInstance(Attributes instAttrs, Path instPath)
	{
		String cuid = instAttrs.getString(Tag.SOPClassUID);

		if (!DicomwebDeviceConfiguration.isDicomwebSopClass(cuid))
		{
			return;
		}

		try
		{
			dicomwebInput.addInstance(instAttrs, instPath);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}
}
