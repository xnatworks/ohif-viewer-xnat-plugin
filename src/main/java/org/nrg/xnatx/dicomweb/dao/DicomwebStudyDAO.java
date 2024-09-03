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
import org.nrg.xnatx.dicomweb.entity.DwStudy;
import org.nrg.xnatx.dicomweb.service.query.Query;
import org.nrg.xnatx.dicomweb.service.query.impl.RetrieveQuery;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class DicomwebStudyDAO extends DicomwebAbstractDAO<DwStudy>
{
	public static final String[] EXCLUSION_PROPERTIES =
		{"encodedAttributes",
			"enabled", "created", "timestamp", "disabled"};

	public static final String[] PROJECTION_PROPERTIES =
		{"sessionId", "studyInstanceUid", "studyId",
			"studyDate", "studyTime", "accessionNumber",
			"studyDescription", "patient", "numberOfStudyRelatedInstances",
			"numberOfStudyRelatedSeries", "modalitiesInStudy", "sopClassesInStudy"};

	public static final ProjectionList projectionList =
		DicomwebAbstractDAO.getProjectionList(PROJECTION_PROPERTIES);

	public void fetchCount(Query query)
	{
		query.executeCountQuery(getSession());
	}

	public void fetchQuery(Query query, int limit)
	{
		query.executeQuery(getSession(), limit);
	}

	public void fetchRetrieveQuery(RetrieveQuery query) throws IOException
	{
		query.execute(getSession());
	}

	public List<DwStudy> getAll(DwStudy example, boolean isEager)
	{
		List<Criterion> extraCriteria = new ArrayList<>();
		if (example.getPatient() != null)
		{
			extraCriteria.add(
				Restrictions.eq("patient", example.getPatient()));
		}

		return super.getAll(example, EXCLUSION_PROPERTIES, projectionList,
			isEager, extraCriteria);
	}
}
