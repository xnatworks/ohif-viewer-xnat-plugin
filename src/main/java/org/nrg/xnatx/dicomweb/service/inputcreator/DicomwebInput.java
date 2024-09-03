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

import org.dcm4che3.data.Tag;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.util.StringUtils;
import org.nrg.xnatx.dicomweb.conf.DicomwebDeviceConfiguration;
import org.nrg.xnatx.dicomweb.entity.DwInstance;
import org.nrg.xnatx.dicomweb.entity.DwPatient;
import org.nrg.xnatx.dicomweb.entity.DwSeries;
import org.nrg.xnatx.dicomweb.entity.DwStudy;
import org.nrg.xnatx.dicomweb.toolkit.DicomwebConstants;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/**
 * @author m.alsad
 */
@SuppressWarnings("FieldMayBeFinal")
public class DicomwebInput
{
	private final Map<String,String> xnatIds;
	private final String experimentPath;

	private boolean isValid = false;
	private Set<String> modalitiesInStudy = new HashSet<>();
	private DwPatient patient;
	private Map<String,List<DwInstance>> seriesInstancesMap = new LinkedHashMap<>();
	private List<DwSeries> seriesList = new ArrayList<>();
	private Map<String,Set<String>> sopClassesInSeriesMap = new LinkedHashMap<>();
	private Set<String> sopClassesInStudy = new HashSet<>();
	private Set<String> sopInstanceUids = new HashSet<>();
	private DwStudy study;
	private Map<String,Set<String>> tsuidsInSeriesMap = new LinkedHashMap<>();

	private static final String SEP = File.separator;

	public DicomwebInput(Map<String,String> xnatIds, final String experimentPath)
	{
		this.xnatIds = xnatIds;
		this.experimentPath = experimentPath;
	}

	public void addInstance(Attributes attrs, Path instPath) throws IOException
	{
		String instanceUid = attrs.getString(Tag.SOPInstanceUID);
		if (sopInstanceUids.contains(instanceUid))
		{
			return;
		}

		Attributes attrsWithoutBulkData = new Attributes(attrs);
		attrsWithoutBulkData.removeAllBulkData();

		newOrUpdatePatient(attrsWithoutBulkData);
		newOrUpdateStudy(attrsWithoutBulkData);

		DwSeries series = newOrGetSeries(attrsWithoutBulkData, instPath);

		DwInstance instance = newInstance(attrsWithoutBulkData, attrs, series);
		instance.setFilename(instPath.getFileName().toString());

		sopInstanceUids.add(instanceUid);
	}

	public DwPatient getPatient()
	{
		return patient;
	}

	public Map<String,List<DwInstance>> getSeriesInstancesMap()
	{
		return seriesInstancesMap;
	}

	public List<DwSeries> getSeriesList()
	{
		return seriesList;
	}

	public DwStudy getStudy()
	{
		return study;
	}

	public boolean isValid()
	{
		return isValid;
	}

	public void validateAndUpdateQueryAttributes()
	{
		isValid = false;

		if (patient == null || study == null)
		{
			return;
		}

		if (seriesList.isEmpty() || seriesInstancesMap.isEmpty())
		{
			return;
		}

		for (String key : seriesInstancesMap.keySet())
		{
			// Instance list
			if (seriesInstancesMap.get(key).isEmpty())
			{
				return;
			}
		}

		// Update Query Attributes
		// Patient - We have one study
		patient.incrementNumberOfStudies();

		// Study
		study.setNumberOfStudyRelatedInstances(sopInstanceUids.size());
		study.setNumberOfStudyRelatedSeries(seriesList.size());
		study.setSopClassesInStudy(StringUtils.concat(sopClassesInStudy, '\\'));
		study.setModalitiesInStudy(StringUtils.concat(modalitiesInStudy, '\\'));

		// Series
		for (DwSeries series : seriesList)
		{
			String seriesUid = series.getSeriesInstanceUid();
			series.setNumberOfSeriesRelatedInstances(
				seriesInstancesMap.get(seriesUid).size());
			series.setAvailableTransferSyntaxUid(
				StringUtils.concat(tsuidsInSeriesMap.get(seriesUid), '\\'));
			series.setSopClassesInSeries(
				StringUtils.concat(sopClassesInSeriesMap.get(seriesUid), '\\'));
		}

		isValid = true;
	}

	private DwInstance newInstance(Attributes attrs, Attributes metadata,
		DwSeries series) throws IOException
	{
		if (!DicomwebDeviceConfiguration.WADO_INCLUDE_PRIVATE)
		{
			metadata.removePrivateAttributes();
		}

		String seriesUid = series.getSeriesInstanceUid();

		List<DwInstance> instanceList = seriesInstancesMap.computeIfAbsent(
			seriesUid, k -> new ArrayList<>());

		DwInstance instance = new DwInstance();
		instance.setData(attrs);
		instance.setMetadata(metadata);

		instanceList.add(instance);

		String cuid = attrs.getString(Tag.SOPClassUID);
		Set<String> seriesCuids = sopClassesInSeriesMap.computeIfAbsent(seriesUid,
			k -> new HashSet<>());

		seriesCuids.add(cuid);
		sopClassesInStudy.add(cuid);
		modalitiesInStudy.add(attrs.getString(Tag.Modality));

		String tsuid = attrs.getString(Tag.TransferSyntaxUID);
		instance.setTransferSyntaxUid(tsuid);
		Set<String> seriesTsuids = tsuidsInSeriesMap.computeIfAbsent(seriesUid,
			k -> new HashSet<>());
		seriesTsuids.add(tsuid);

		return instance;
	}

	private DwSeries newOrGetSeries(Attributes attrs, Path instPath)
		throws IOException
	{
		for (DwSeries series : seriesList)
		{
			if (series.getSeriesInstanceUid().equals(
				attrs.getString(Tag.SeriesInstanceUID)))
			{
				return series;
			}
		}

		DwSeries series = new DwSeries();
		series.setData(attrs);

		// Ex. ".../SCANS/1/DICOM/..."
		String pathStr = instPath.toString();
		String scanId = pathStr.substring(pathStr.indexOf(SEP+"SCANS"+SEP) + 7);
		scanId = scanId.substring(0, scanId.indexOf(SEP));
		series.setScanId(scanId);

		seriesList.add(series);

		return series;
	}

	private void newOrUpdatePatient(Attributes attrs) throws IOException
	{
		if (patient != null)
		{
			return;
		}

		patient = new DwPatient();
		patient.setData(attrs);
		patient.setSubjectId(xnatIds.get(DicomwebConstants.XNAT_SUBJECT_ID));
	}

	private void newOrUpdateStudy(Attributes attrs)
		throws IOException
	{
		if (study != null)
		{
			return;
		}

		study = new DwStudy();
		study.setData(attrs);
		study.setSessionId(xnatIds.get(DicomwebConstants.XNAT_SESSION_ID));
		study.setStoragePath(experimentPath);
	}
}
