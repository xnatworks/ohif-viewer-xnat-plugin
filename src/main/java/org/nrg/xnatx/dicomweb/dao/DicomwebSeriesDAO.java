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
package org.nrg.xnatx.dicomweb.dao;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Restrictions;
import org.nrg.xnatx.dicomweb.entity.DwSeries;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class DicomwebSeriesDAO extends DicomwebAbstractDAO<DwSeries>
{
	public static final String[] EXCLUSION_PROPERTIES =
		{"encodedAttributes",
			"enabled", "created", "timestamp", "disabled"};

	public static final String[] PROJECTION_PROPERTIES =
		{"seriesInstanceUid", "seriesNumber", "seriesDescription",
			"modality", "sopClassUid", "institutionName",
			"institutionalDepartmentName", "stationName", "bodyPartExamined",
			"laterality", "performedProcedureStepInstanceUid", "performedProcedureStepClassUid",
			"performedProcedureStepStartDate", "performedProcedureStepStartTime", "study",
			"numberOfSeriesRelatedInstances", "availableTransferSyntaxUid", "sopClassesInSeries"};

	public static final ProjectionList projectionList =
		DicomwebAbstractDAO.getProjectionList(PROJECTION_PROPERTIES);

	public List<DwSeries> getAll(DwSeries example, boolean isEager)
	{
		List<Criterion> extraCriteria = new ArrayList<>();
		if (example.getStudy() != null)
		{
			extraCriteria.add(
				Restrictions.eq("study", example.getStudy()));
		}

		return super.getAll(example, EXCLUSION_PROPERTIES, projectionList,
			isEager, extraCriteria);
	}
}
