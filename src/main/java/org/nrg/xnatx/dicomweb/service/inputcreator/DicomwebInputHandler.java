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

import lombok.extern.slf4j.Slf4j;
import org.nrg.xdat.om.XnatImagesessiondata;
import org.nrg.xft.security.UserI;

import org.nrg.xnatx.dicomweb.conf.DicomwebDeviceConfiguration;
import org.nrg.xnatx.dicomweb.entity.DwStudy;
import org.nrg.xnatx.dicomweb.service.query.DicomwebDataService;
import org.nrg.xnatx.dicomweb.toolkit.DicomwebEntityValidator;
import org.nrg.xnatx.dicomweb.toolkit.DicomwebUtils;
import org.nrg.xnatx.plugin.PluginCode;
import org.nrg.xnatx.plugin.PluginException;
import org.nrg.xnatx.plugin.PluginUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author m.alsad
 */
@Service
@Slf4j
public class DicomwebInputHandler
{
	private final DicomwebDataService dwDataService;

	@Autowired
	public DicomwebInputHandler(final DicomwebDataService dwDataService)
	{
		this.dwDataService = dwDataService;
	}

	public void createDicomwebData(String sessionId, UserI user,
		boolean overwriteExisting)	throws PluginException
	{
		if (user == null)
		{
			throw new PluginException("User must not be null",
				PluginCode.HttpUnprocessableEntity);
		}
		XnatImagesessiondata sessionData = PluginUtils.getImageSessionData(
			sessionId, user);
		createDicomwebData(sessionData, overwriteExisting);
	}

	public void createDicomwebData(XnatImagesessiondata sessionData)
		throws PluginException
	{
		createDicomwebData(sessionData, false);
	}

	public void createDicomwebData(XnatImagesessiondata sessionData,
		boolean overwriteExisting) throws PluginException
	{
		if (sessionData == null)
		{
			throw new PluginException("SessionData must not be null",
				PluginCode.HttpUnprocessableEntity);
		}

		String sessionId = sessionData.getId();

		if (!DicomwebUtils.isSessionValidForDicomweb(sessionData))
		{
			log.warn(
				"Session {} is not supported for DICOMweb data generation", sessionId);
			return;
		}

		DwStudy prevStudy = dwDataService.getStudyBySessionId(sessionId, false);

		// Do not recreate if a valid study is available
		if (!overwriteExisting && DicomwebEntityValidator.isValidEntity(prevStudy))
		{
			return;
		}

		Map<String,String> xnatIds = DicomwebUtils.getXnatIds(sessionData);

		DicomwebInputCreator dwiCreator = new DicomwebInputCreator(sessionData,
			xnatIds);
		DicomwebInput dwi = dwiCreator.scanPathAndCreateInput();

		try
		{
			dwDataService.createOrUpdate(dwi);
		}
		catch (Exception e)
		{
			throw new PluginException(
				"Unable to create DICOMweb data for session " + sessionId, e);
		}
	}

	public void deleteDicomwebData(String sessionId)
		throws PluginException
	{
		if (sessionId == null)
		{
			throw new PluginException("Session ID must not be null",
				PluginCode.HttpUnprocessableEntity);
		}

		DwStudy study = dwDataService.getStudyBySessionId(sessionId, false);
		if (study == null)
		{
			throw new PluginException("Session "+sessionId+" has no DICOMweb data",
				PluginCode.HttpNotFound);
		}

		dwDataService.deleteStudy(study);
	}

	public void deleteDicomwebData(XnatImagesessiondata sessionData)
		throws PluginException
	{
		if (sessionData == null)
		{
			throw new PluginException("SessionData must not be null",
				PluginCode.HttpUnprocessableEntity);
		}

		deleteDicomwebData(sessionData.getId());
	}

	public boolean hasValidDicomwebData(XnatImagesessiondata sessionData)
		throws PluginException
	{
		checkValidDicomwebConfiguration(sessionData);

		DwStudy study = dwDataService.getStudyBySessionId(sessionData.getId(),
			false);

		return DicomwebEntityValidator.isValidEntity(study);
	}

	private void checkValidDicomwebConfiguration(XnatImagesessiondata sessionData)
		throws PluginException
	{
		String modality = PluginUtils.getImageSessionModality(sessionData);
		if (!DicomwebDeviceConfiguration.isDicomwebModality(modality))
		{
			throw new PluginException(
				"Modality " + modality + " is not supported for DICOMweb data generation",
				PluginCode.DICOMWebNotSupported);
		}
	}
}
