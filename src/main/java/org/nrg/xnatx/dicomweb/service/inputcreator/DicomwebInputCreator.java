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

import icr.etherj.PathScan;
import lombok.extern.slf4j.Slf4j;
import org.dcm4che3.data.Attributes;
import org.nrg.xdat.om.XnatImagesessiondata;
import org.nrg.xnatx.plugin.PluginCode;
import org.nrg.xnatx.plugin.PluginException;
import org.nrg.xnatx.plugin.PluginUtils;

import java.io.IOException;
import java.util.Map;

/**
 * @author m.alsad
 */
@Slf4j
public class DicomwebInputCreator
{
	private final XnatImagesessiondata sessionData;

	private final Map<String,String> xnatIds;
	private final String experimentPath;

	public DicomwebInputCreator(XnatImagesessiondata sessionData,
		Map<String,String> xnatIds) throws PluginException
	{
		if (sessionData == null)
		{
			throw new PluginException("SessionData must not be null",
				PluginCode.HttpUnprocessableEntity);
		}

		this.sessionData = sessionData;
		this.xnatIds = xnatIds;
		experimentPath = PluginUtils.getExperimentPath(sessionData);
	}

	public DicomwebInput scanPathAndCreateInput() throws PluginException
	{
		String xnatScanPath = experimentPath + "SCANS";
		log.info("Creating DICOMweb data for {}", xnatScanPath);

		DicomwebPathScanContext ctx =
			new DicomwebPathScanContext(xnatIds, experimentPath);
		PathScan<Attributes> pathScan = new DicomwebPathScan();
		pathScan.addContext(ctx);
		try
		{
			pathScan.scan(xnatScanPath, true);
			return ctx.getDicomwebInput();
		}
		catch (IOException e)
		{
			log.error("DICOMweb data creation exception:\n" + e.getMessage());
			throw new PluginException(
				"DICOMweb data creation exception:\n" + e.getMessage(),
				PluginCode.IO, e);
		}
	}
}
