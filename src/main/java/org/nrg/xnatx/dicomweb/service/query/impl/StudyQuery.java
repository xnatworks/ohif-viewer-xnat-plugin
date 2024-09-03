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
import org.dcm4che3.util.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.nrg.xnatx.dicomweb.entity.DwStudy;
import org.nrg.xnatx.dicomweb.service.qido.QidoRsContext;
import org.nrg.xnatx.dicomweb.toolkit.DicomwebUtils;

import java.io.IOException;
import java.util.Map;

/**
 * @author m.alsad
 */
public class StudyQuery extends AbstractQuery
{
	public StudyQuery(QidoRsContext context, String[]... propertyArrays)
	{
		super(context, propertyArrays);
	}

	static void addStudyQRAttrs(QidoRsContext context,
		int numberOfStudyRelatedInstances, int numberOfStudyRelatedSeries,
		String modalitiesInStudy, String sopClassesInStudy, Attributes attrs)
	{
		attrs.setString(Tag.ModalitiesInStudy, VR.CS,
			StringUtils.split(modalitiesInStudy, '\\'));
		attrs.setString(Tag.SOPClassesInStudy, VR.UI,
			StringUtils.split(sopClassesInStudy, '\\'));
		attrs.setInt(Tag.NumberOfStudyRelatedSeries, VR.IS,
			numberOfStudyRelatedSeries);
		attrs.setInt(Tag.NumberOfStudyRelatedInstances, VR.IS,
			numberOfStudyRelatedInstances);
	}

	@Override
	public void executeCountQuery(final Session session)
	{
		boolean hasPatientLevelCriteria = QueryBuilder.hasPatientLevelCriteria(
			context.getPatientIds(), context.getQueryKeys());

		DetachedCriteria study =
			DetachedCriteria.forClass(DwStudy.class, "study");

		restrict(study, hasPatientLevelCriteria);

		Criteria criteria = getExecutableCriteria(session, study);
		criteria.setProjection(Projections.rowCount());
		count = (long) criteria.uniqueResult();
	}

	@Override
	protected DetachedCriteria multiselect()
	{
		DetachedCriteria study = DetachedCriteria.forClass(DwStudy.class, "study");

		restrict(study);
		order(study);

		ProjectionList projectionList = Projections.projectionList();
		ProjectionUtils.fillProjectionList(projectionList, propertyList);
		study.setProjection(projectionList);

		return study;
	}

	protected void order(DetachedCriteria study)
	{
		QueryBuilder.orderStudies(study, context.getOrderByTags());
	}

	@Override
	protected <E> Attributes toAttributes(E results) throws IOException
	{
		Object[] resultArray = (Object[]) results;
		Map<String,Object> pathValueMap =
			ProjectionUtils.mapResultsToPaths(resultArray, propertyList);

		Integer numberOfInstancesI =
			(Integer) pathValueMap.get("study.numberOfStudyRelatedInstances");
		int numberOfStudyRelatedInstances;
		int numberOfStudyRelatedSeries;
		String modalitiesInStudy;
		String sopClassesInStudy;
		if (numberOfInstancesI != null)
		{
			numberOfStudyRelatedInstances = numberOfInstancesI;
			if (numberOfStudyRelatedInstances == 0)
			{
				return null;
			}
			numberOfStudyRelatedSeries =
				(int) pathValueMap.get("study.numberOfStudyRelatedSeries");
			modalitiesInStudy = (String) pathValueMap.get("study.modalitiesInStudy");
			sopClassesInStudy = (String) pathValueMap.get("study.sopClassesInStudy");
		}
		else
		{
			return null;
		}
		Attributes studyAttrs = DicomwebUtils.decodeAttributes(
			(byte[]) pathValueMap.get("study.encodedAttributes"));
		Attributes patAttrs = DicomwebUtils.decodeAttributes(
			(byte[]) pathValueMap.get("patient.encodedAttributes"));
		Attributes.unifyCharacterSets(patAttrs, studyAttrs);
		Attributes attrs =
			new Attributes(patAttrs.size() + studyAttrs.size() + 10);
		attrs.addAll(patAttrs);
		attrs.addAll(studyAttrs, true);
		PatientQuery.addPatientQRAttrs(pathValueMap, attrs);
		addStudyQRAttrs(context, numberOfStudyRelatedInstances,
			numberOfStudyRelatedSeries, modalitiesInStudy, sopClassesInStudy, attrs);

		addExtraAttributes(context, attrs, null);
		return attrs;
	}

	private void restrict(DetachedCriteria study)
	{
		restrict(study, true);
	}

	private void restrict(DetachedCriteria study, boolean queryPatient)
	{
		QueryBuilder.studyPredicates(study, context.getPatientIds(),
			context.getQueryKeys(), queryPatient);
	}
}
