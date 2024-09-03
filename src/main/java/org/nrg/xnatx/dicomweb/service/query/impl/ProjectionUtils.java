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

import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author m.alsad
 */
public class ProjectionUtils
{
	protected static final String[] PATIENT_PROJECTION_PROPERTIES =
		{	"patient.id",
			"patient.subjectId",
			"patient.numberOfStudies",
			"patient.encodedAttributes" };
	protected static final String[] STUDY_PROJECTION_PROPERTIES =
		{ "study.id",
			"study.sessionId",
			"study.numberOfStudyRelatedInstances",
			"study.numberOfStudyRelatedSeries",
			"study.modalitiesInStudy",
			"study.sopClassesInStudy",
			"study.encodedAttributes" };
	protected static final String[] SERIES_PROJECTION_PROPERTIES =
		{ "series.id",
			"series.scanId",
			"series.numberOfSeriesRelatedInstances",
			"series.availableTransferSyntaxUid",
			"series.sopClassesInSeries",
			"series.encodedAttributes" };
	protected static final String[] INSTANCE_PROJECTION_PROPERTIES =
		{ "instance.id",
			"instance.encodedAttributes",
			//
			"series.id" };

	protected static void fillProjectionList(ProjectionList projectionList,
		final List<String> properties)
	{
		for (String prop : properties)
		{
			projectionList.add(Projections.property(prop));
		}
	}

	protected static Map<String, Object> mapResultsToPaths(
		Object[] results,	List<String> properties)
	{
		if (results == null)
		{
			return null;
		}

		Map<String, Object> pathValueMap = new HashMap<>();
		int resultSize = results.length;
		for (int i = 0; i < properties.size(); i++)
		{
			Object value = i < resultSize ? results[i] : null;
			pathValueMap.put(properties.get(i), value);
		}

		return pathValueMap;
	}
}
