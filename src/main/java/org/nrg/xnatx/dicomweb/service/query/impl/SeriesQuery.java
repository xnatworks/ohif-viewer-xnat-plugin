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

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.nrg.xnatx.dicomweb.entity.DwPatient;
import org.nrg.xnatx.dicomweb.entity.DwSeries;
import org.nrg.xnatx.dicomweb.entity.DwStudy;
import org.nrg.xnatx.dicomweb.service.qido.QidoRsContext;
import org.nrg.xnatx.dicomweb.toolkit.DicomwebUtils;

import java.io.IOException;
import java.util.Map;

/**
 * @author m.alsad
 */
public class SeriesQuery extends AbstractQuery
{
	public SeriesQuery(QidoRsContext context, String[]... propertyArrays)
	{
		super(context, propertyArrays);
	}

	static void addSeriesQRAttrs(Map<String,Object> pathValueMap,
		QidoRsContext context, int numberOfSeriesRelatedInstances, Attributes attrs)
	{
		attrs.setInt(Tag.NumberOfSeriesRelatedInstances, VR.IS,
			numberOfSeriesRelatedInstances);
		String transferSyntaxUID =
			(String) pathValueMap.get("series.transferSyntaxUID");
		attrs.setString(Tag.AvailableTransferSyntaxUID, VR.UI, transferSyntaxUID);
	}

	@Override
	public void executeCountQuery(Session session)
	{
		DetachedCriteria series =
			DetachedCriteria.forClass(DwSeries.class, "series");

		restrict(series);

		Criteria criteria = getExecutableCriteria(session, series);
		criteria.setProjection(Projections.rowCount());
		count = (long) criteria.uniqueResult();
	}

	static Attributes fetchSeriesAttributes(QidoRsContext ctx, Long seriesPk)
		throws IOException
	{
		DwSeries series = ctx.getDwDataService().getSeriesByProperty(
			"id", seriesPk, true);
		if (series == null)
		{
			return new Attributes(0);
		}
		DwStudy study = series.getStudy();
		DwPatient patient = study.getPatient();

		// Series attributes
		Attributes seriesAttrs = series.getAttributes();
		Attributes seriesQRAttrs = series.getQueryAttributes();

		// Study attributes
		Attributes studyAttrs = study.getAttributes();
		Attributes studyQRAttrs = study.getQueryAttributes();

		// Patient attributes
		Attributes patAttrs = patient.getAttributes();
		Attributes patientQRAttrs = patient.getQueryAttributes();

		// Unify and add attributes
		Attributes.unifyCharacterSets(patAttrs, studyAttrs, seriesAttrs);
		Attributes attrs = new Attributes(patAttrs.size()
																				+ studyAttrs.size()
																				+ seriesAttrs.size() + 20);
		attrs.addAll(patAttrs);
		attrs.addAll(studyAttrs, true);
		attrs.addAll(seriesAttrs, true);

		// Unify and add QR attributes
		Attributes.unifyCharacterSets(patientQRAttrs, studyQRAttrs, seriesQRAttrs);
		attrs.addAll(patientQRAttrs, true);
		attrs.addAll(studyQRAttrs, true);
		attrs.addAll(seriesQRAttrs, true);

		addExtraAttributes(ctx, attrs, series.getScanId());

		return attrs;
	}

	@Override
	protected DetachedCriteria multiselect()
	{
		DetachedCriteria series =
			DetachedCriteria.forClass(DwSeries.class, "series");

		restrict(series);
		order(series);

		ProjectionList projectionList = Projections.projectionList();
		ProjectionUtils.fillProjectionList(projectionList, propertyList);
		series.setProjection(projectionList);

		return series;
	}

	protected void order(DetachedCriteria series)
	{
		QueryBuilder.orderSeries(series, context.getOrderByTags());
	}

	@Override
	protected <E> Attributes toAttributes(E results) throws IOException
	{
		Object[] resultArray = (Object[]) results;
		Map<String,Object> pathValueMap =
			ProjectionUtils.mapResultsToPaths(resultArray, propertyList);

		Long studyPk = (Long) pathValueMap.get("study.id");

		Integer numberOfInstancesI = (Integer) pathValueMap.get(
			"series.numberOfSeriesRelatedInstances");
		int numberOfSeriesRelatedInstances;
		if (numberOfInstancesI != null)
		{
			numberOfSeriesRelatedInstances = numberOfInstancesI;
			if (numberOfSeriesRelatedInstances == 0)
			{
				return null;
			}
		}
		else
		{
			return null;
		}

		Attributes studyAttrs = cachedStudyAttributes.get(studyPk);
		if (studyAttrs == null)
		{
			studyAttrs = toStudyAttributes(pathValueMap, studyPk);
			cachedStudyAttributes.put(studyPk, studyAttrs);
		}
		Attributes seriesAttrs = DicomwebUtils.decodeAttributes(
			(byte[]) pathValueMap.get("series.encodedAttributes"));
		Attributes.unifyCharacterSets(studyAttrs, seriesAttrs);
		Attributes attrs = new Attributes(
			studyAttrs.size() + seriesAttrs.size() + 10);
		attrs.addAll(studyAttrs);
		attrs.addAll(seriesAttrs, true);
		addSeriesQRAttrs(pathValueMap, context, numberOfSeriesRelatedInstances,
			attrs);

		String scanId = (String) pathValueMap.get("series.scanId");
		addExtraAttributes(context, attrs, scanId);
		return attrs;
	}

	private void restrict(DetachedCriteria series)
	{
		QueryBuilder.seriesPredicates(series, context.getPatientIds(),
			context.getQueryKeys());
	}

	private Attributes toStudyAttributes(Map<String,Object> pathValueMap,
		Long studyPk) throws IOException
	{
		Integer numberOfInstancesI = (Integer) pathValueMap.get(
			"study.numberOfStudyRelatedInstances");
		int numberOfStudyRelatedInstances;
		int numberOfStudyRelatedSeries;
		String modalitiesInStudy;
		String sopClassesInStudy;
		if (numberOfInstancesI != null)
		{
			numberOfStudyRelatedInstances = numberOfInstancesI;
			numberOfStudyRelatedSeries = (int) pathValueMap.get(
				"study.numberOfStudyRelatedInstances");
			modalitiesInStudy = (String) pathValueMap.get("study.modalitiesInStudy");
			sopClassesInStudy = (String) pathValueMap.get("study.sopClassesInStudy");
		}
		else
		{
			numberOfStudyRelatedInstances = 0;
			numberOfStudyRelatedSeries = 0;
			modalitiesInStudy = "";
			sopClassesInStudy = "";
		}

		Attributes studyAttrs = DicomwebUtils.decodeAttributes(
			(byte[]) pathValueMap.get("study.encodedAttributes"));
		Attributes patAttrs = DicomwebUtils.decodeAttributes(
			(byte[]) pathValueMap.get("study.encodedAttributes"));
		Attributes.unifyCharacterSets(patAttrs, studyAttrs);
		Attributes attrs =
			new Attributes(patAttrs.size() + studyAttrs.size() + 10);
		attrs.addAll(patAttrs);
		attrs.addAll(studyAttrs, true);
		PatientQuery.addPatientQRAttrs(pathValueMap, attrs);
		StudyQuery.addStudyQRAttrs(context,
			numberOfStudyRelatedInstances, numberOfStudyRelatedSeries,
			modalitiesInStudy, sopClassesInStudy, attrs);
		return attrs;
	}
}
