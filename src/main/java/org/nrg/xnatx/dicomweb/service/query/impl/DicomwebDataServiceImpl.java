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
package org.nrg.xnatx.dicomweb.service.query.impl;

import org.nrg.xnatx.dicomweb.service.query.Query;
import org.nrg.xnatx.dicomweb.entity.*;
import org.nrg.xnatx.dicomweb.service.query.*;
import org.nrg.xnatx.dicomweb.service.qido.QidoRsContext;
import org.nrg.xnatx.dicomweb.service.inputcreator.DicomwebInput;
import org.nrg.xnatx.plugin.PluginCode;
import org.nrg.xnatx.plugin.PluginException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author m.alsad
 */
@Service
public class DicomwebDataServiceImpl implements DicomwebDataService
{
	private final DwInstanceDataService instanceDataService;
	private final DwPatientDataService patientDataService;
	private final DwSeriesDataService seriesDataService;
	private final DwStudyDataService studyDataService;

	@Autowired
	public DicomwebDataServiceImpl(DwInstanceDataService instanceDataService,
		DwPatientDataService patientDataService,
		DwSeriesDataService seriesDataService, DwStudyDataService studyDataService)
	{
		this.instanceDataService = instanceDataService;
		this.patientDataService = patientDataService;
		this.seriesDataService = seriesDataService;
		this.studyDataService = studyDataService;
	}

	@Override
	@Transactional
	public void createOrUpdate(DicomwebInput dwi) throws IOException
	{
		DwPatient dwiPatient = dwi.getPatient();
		DwStudy dwiStudy = dwi.getStudy();
		List<DwSeries> seriesList = dwi.getSeriesList();
		Map<String,List<DwInstance>> seriesInstancesMap =
			dwi.getSeriesInstancesMap();

		boolean isExistingPatient = true;
		DwPatient patient = getPatientBySubjectId(dwiPatient.getSubjectId(),
			true);
		if (patient == null)
		{
			patient = patientDataService.createOrUpdate(dwiPatient);
			isExistingPatient = false;
		}

		boolean isExistingStudy = studyDataService.exists(
			"sessionId", dwiStudy.getSessionId());
		dwiStudy.setPatient(patient);
		DwStudy study = studyDataService.createOrUpdate(dwiStudy);

		if (isExistingStudy)
		{
			// Remove all series and instances of the existing study
			List<DwSeries> existingSeriesList = getAllSeries(study, false);
			for (DwSeries existingSeries : existingSeriesList)
			{
				deleteSeries(existingSeries);
			}
		}
		else if (isExistingPatient) // !isExistingStudy && isExistingPatient
		{
			// Update patient's query attributes
			patient.incrementNumberOfStudies();
			patientDataService.update(patient);
		}

		// Create new series and instances
		for (DwSeries series : seriesList)
		{
			series.setStudy(study);
			DwSeries newSeries = seriesDataService.createOrUpdate(series);
			// Create instances
			List<DwInstance> instanceList = seriesInstancesMap.get(
				series.getSeriesInstanceUid());
			for (DwInstance instance : instanceList)
			{
				instance.setSeries(newSeries);
				instanceDataService.createOrUpdate(instance);
			}
		}
	}

	@Override
	@Transactional
	public void deletePatient(DwPatient patient)
	{
		List<DwStudy> matches = getAllStudies(patient, false);
		for (DwStudy entity : matches)
		{
			deleteStudy(entity);
		}
		patientDataService.delete(patient);
	}

	@Override
	@Transactional
	public void deleteSeries(DwSeries series)
	{
		instanceDataService.deleteAll(series);
		seriesDataService.delete(series);
	}

	@Override
	@Transactional
	public void deleteStudy(DwStudy study)
	{
		List<DwSeries> matches = getAllSeries(study, false);
		for (DwSeries entity : matches)
		{
			deleteSeries(entity);
		}

		// Get Patient's number of studies
		DwPatient patient = study.getPatient();
		long numStudies = studyDataService.countByProperty(
			"patient", patient);

		studyDataService.delete(study);

		if (numStudies <= 1)
		{
			// Remove the Patient record as it has no associated studies
			patientDataService.delete(patient);
		}
		else
		{
			// Update patient's query attributes
			patient.decrementNumberOfStudies();
			patientDataService.update(patient);
		}
	}

	@Override
	@Transactional(readOnly = true)
	public List<DwSeries> getAllSeries(DwStudy study, boolean isEager)
	{
		DwSeries example = new DwSeries();
		example.setStudy(study);

		return seriesDataService.getAll(example, isEager);
	}

	@Override
	@Transactional(readOnly = true)
	public List<DwStudy> getAllStudies(DwPatient patient, boolean isEager)
	{
		DwStudy example = new DwStudy();
		example.setPatient(patient);

		return studyDataService.getAll(example, isEager);
	}

	@Override
	@Transactional(readOnly = true)
	public DwPatient getPatientBySubjectId(String subjectId, boolean isEager)
	{
		return patientDataService.get("subjectId", subjectId, isEager);
	}

	@Override
	@Transactional(readOnly = true)
	public DwStudy getStudyBySessionId(String sessionId, boolean isEager)
	{
		return studyDataService.get("sessionId", sessionId, isEager);
	}

	@Override
	public Query createQidoRsQuery(QidoRsContext ctx) throws PluginException
	{
		switch (ctx.getQueryRetrieveLevel())
		{
			case STUDY:
				return new StudyQuery(ctx,
					ProjectionUtils.PATIENT_PROJECTION_PROPERTIES,
					ProjectionUtils.STUDY_PROJECTION_PROPERTIES);
			case SERIES:
				return new SeriesQuery(ctx,
					ProjectionUtils.PATIENT_PROJECTION_PROPERTIES,
					ProjectionUtils.STUDY_PROJECTION_PROPERTIES,
					ProjectionUtils.SERIES_PROJECTION_PROPERTIES);
			case IMAGE:
				return new InstanceQuery(ctx,
					ProjectionUtils.INSTANCE_PROJECTION_PROPERTIES);
			default:
				throw new PluginException("Unsupported Query Retrieve Level",
					PluginCode.DICOMWebNotSupported);
		}
	}

	@Override
	@Transactional(readOnly = true)
	public void fetchCount(Query query)
	{
		studyDataService.fetchCount(query);
	}

	@Override
	@Transactional(readOnly = true)
	public void fetchQuery(Query query, int limit)
	{
		studyDataService.fetchQuery(query, limit);
	}

	@Override
	@Transactional(readOnly = true)
	public void fetchRetrieveQuery(RetrieveQuery query) throws IOException
	{
		studyDataService.fetchRetrieveQuery(query);
	}

	@Override
	@Transactional(readOnly = true)
	public DwSeries getSeriesByProperty(String propertyName,
		Object propertyValue, boolean isEager)
	{
		return seriesDataService.get(propertyName, propertyValue, isEager);
	}
}
