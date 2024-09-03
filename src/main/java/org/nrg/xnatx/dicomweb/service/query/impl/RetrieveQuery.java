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
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.*;
import org.nrg.xnatx.dicomweb.entity.DwInstance;
import org.nrg.xnatx.dicomweb.entity.DwSeries;
import org.nrg.xnatx.dicomweb.entity.DwStudy;
import org.nrg.xnatx.dicomweb.service.wado.WadoRsContext;
import org.nrg.xnatx.dicomweb.toolkit.DicomwebConstants;
import org.nrg.xnatx.dicomweb.toolkit.DicomwebUtils;
import org.nrg.xnatx.dicomweb.wado.InstanceInfo;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author m.alsad
 */
public class RetrieveQuery
{
	final WadoRsContext context;
	final Map<Long,String> studyStoragePathMap = new HashMap<>();
	final Map<Long,String> seriesScanIdMap = new HashMap<>();
	List<String> instancePropertyList = new ArrayList<>();
	List<String> seriesPropertyList = new ArrayList<>();
	List<String> studyPropertyList = new ArrayList<>();

	public RetrieveQuery(WadoRsContext context)
	{
		this.context = context;
		fillPropertyList();
	}

	public void execute(Session session) throws IOException
	{
		runInstanceQuery(session);

		if (!context.isMetadataQuery())
		{
			for (InstanceInfo instInfo : context.getMatches())
			{
				String scanId = getScanId(instInfo.getSeriesPk(), session);
				String studyStoragePath =
					getStudyStoragePath(instInfo.getStudyPk(), session);
				Path filepath = Paths.get(studyStoragePath,
					"SCANS", scanId, "DICOM", instInfo.getFilename());
				instInfo.setStoragePath(filepath);
			}
		}
	}

	private String getStudyStoragePath(Long studyPk, Session session)
		throws IOException
	{
		String storagePath = studyStoragePathMap.get(studyPk);
		if (storagePath != null)
		{
			return storagePath;
		}

		// Study Query
		DetachedCriteria study =
			DetachedCriteria.forClass(DwStudy.class, "study");
		study.add(Restrictions.eq("study.id", studyPk));

		ProjectionList projectionList = Projections.projectionList();
		ProjectionUtils.fillProjectionList(projectionList, studyPropertyList);
		study.setProjection(projectionList);

		Criteria criteria =
			AbstractQuery.getExecutableCriteria(session, study);

		List<Object[]> resultsList = criteria.list();
		if (resultsList != null && resultsList.size() == 1)
		{
			Map<String,Object> pathValueMap = ProjectionUtils.mapResultsToPaths(
				resultsList.get(0), studyPropertyList);
			storagePath = (String) pathValueMap.get("study.storagePath");
			studyStoragePathMap.put(studyPk, storagePath);
			return storagePath;
		}

		throw new IOException(
			"Could not query Study " + studyPk + " from the database");

	}

	private String getScanId(Long seriesPk, Session session) throws IOException
	{
		String scanId = seriesScanIdMap.get(seriesPk);
		if (scanId != null)
		{
			return scanId;
		}

		// Series Query
		DetachedCriteria series =
			DetachedCriteria.forClass(DwSeries.class, "series");
		series.add(Restrictions.eq("series.id", seriesPk));

		ProjectionList projectionList = Projections.projectionList();
		ProjectionUtils.fillProjectionList(projectionList, seriesPropertyList);
		series.setProjection(projectionList);

		Criteria criteria =
			AbstractQuery.getExecutableCriteria(session, series);

		List<Object[]> resultsList = criteria.list();
		if (resultsList != null && resultsList.size() == 1)
		{
			Map<String,Object> pathValueMap = ProjectionUtils.mapResultsToPaths(
				resultsList.get(0), seriesPropertyList);
			scanId = (String) pathValueMap.get("series.scanId");
			seriesScanIdMap.put(seriesPk, scanId);
			return scanId;
		}

		throw new IOException(
			"Could not query Series " + seriesPk + " from the database");
	}

	private void fillPropertyList()
	{
		// Instance
		instancePropertyList.add("study.id");
		instancePropertyList.add("series.id");
		instancePropertyList.add("instance.id");
		instancePropertyList.add("instance.filename");
		instancePropertyList.add("instance.encodedMetadata");

		// Series
		seriesPropertyList.add("series.id");
		seriesPropertyList.add("series.scanId");

		// Study
		studyPropertyList.add("study.id");
		studyPropertyList.add("study.storagePath");
	}

	private DetachedCriteria instanceMultiselect()
	{
		List<Criterion> predicates = new ArrayList<>();

		DetachedCriteria instance =
			DetachedCriteria.forClass(DwInstance.class, "instance");
		DetachedCriteria series =
			instance.createAlias("instance.series", "series");
		DetachedCriteria study =
			series.createAlias("series.study", "study");

		Map<String,String> xnatIds = context.getXnatIds();
		predicates.add(Restrictions.eq("study.sessionId",
			xnatIds.get(DicomwebConstants.XNAT_SESSION_ID)));

		QueryBuilder.uidsPredicate(predicates, "study.studyInstanceUid",
			context.getStudyInstanceUid());
		QueryBuilder.uidsPredicate(predicates, "series.seriesInstanceUid",
			context.getSeriesInstanceUid());
		QueryBuilder.uidsPredicate(predicates, "instance.sopInstanceUid",
			context.getSopInstanceUid());

		QueryBuilder.addPredicatesToCriteria(instance, predicates);

		instance.addOrder(Order.asc("instance.instanceNumber"));

		ProjectionList projectionList = Projections.projectionList();
		ProjectionUtils.fillProjectionList(projectionList, instancePropertyList);
		instance.setProjection(projectionList);

		return instance;
	}

	private void runInstanceQuery(Session session) throws IOException
	{
		DetachedCriteria detachedCriteria = instanceMultiselect();
		Criteria criteria =
			AbstractQuery.getExecutableCriteria(session, detachedCriteria);

		List<Object[]> resultsList = criteria.list();
		List<InstanceInfo> matches = context.getMatches();
		for (Object[] results : resultsList)
		{
			Map<String,Object> pathValueMap =
				ProjectionUtils.mapResultsToPaths(results, instancePropertyList);

			Long instancePk = (Long) pathValueMap.get("instance.id");
			Long seriesPk = (Long) pathValueMap.get("series.id");
			Long studyPk = (Long) pathValueMap.get("study.id");
			String filename = (String) pathValueMap.get("instance.filename");

			Attributes metadata = new Attributes(0);
			metadata = DicomwebUtils.decodeMetadata(
				(byte[]) pathValueMap.get("instance.encodedMetadata"));

			InstanceInfo instInfo =
				new InstanceInfo(studyPk, seriesPk, instancePk, filename, metadata);
			matches.add(instInfo);
		}
	}
}
