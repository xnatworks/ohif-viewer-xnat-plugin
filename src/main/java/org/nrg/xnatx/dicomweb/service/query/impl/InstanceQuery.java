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
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.nrg.xnatx.dicomweb.entity.DwInstance;
import org.nrg.xnatx.dicomweb.entity.DwSeries;
import org.nrg.xnatx.dicomweb.service.qido.QidoRsContext;
import org.nrg.xnatx.dicomweb.toolkit.DicomwebUtils;

import java.io.IOException;
import java.util.Map;

/**
 * @author m.alsad
 */
public class InstanceQuery extends AbstractQuery
{
	public InstanceQuery(QidoRsContext context, String[]... propertyArrays)
	{
		super(context, propertyArrays);
	}

	@Override
	public void executeCountQuery(Session session)
	{
		DetachedCriteria instance =
			DetachedCriteria.forClass(DwInstance.class, "instance");

		restrict(instance);

		Criteria criteria = getExecutableCriteria(session, instance);
		criteria.setProjection(Projections.rowCount());
		count = (long) criteria.uniqueResult();
	}

	@Override
	protected DetachedCriteria multiselect()
	{
		DetachedCriteria instance =
			DetachedCriteria.forClass(DwInstance.class, "instance");

		restrict(instance);
		order(instance);

		ProjectionList projectionList = Projections.projectionList();
		ProjectionUtils.fillProjectionList(projectionList, propertyList);
		instance.setProjection(projectionList);

		return instance;
	}

	protected void order(DetachedCriteria instance)
	{
		QueryBuilder.orderInstances(instance, context.getOrderByTags());
	}

	@Override
	protected <E> Attributes toAttributes(E results) throws IOException
	{
		Object[] resultArray = (Object[]) results;
		Map<String,Object> pathValueMap =
			ProjectionUtils.mapResultsToPaths(resultArray, propertyList);

		Long seriesPk = (Long) pathValueMap.get("series.id");
		Attributes seriesAttrs = cachedSeriesAttributes.get(seriesPk);
		if (seriesAttrs == null)
		{
			seriesAttrs = SeriesQuery.fetchSeriesAttributes(context, seriesPk);
			cachedSeriesAttributes.put(seriesPk, seriesAttrs);
		}
		Attributes instAttrs = DicomwebUtils.decodeAttributes(
			(byte[]) pathValueMap.get("instance.encodedAttributes"));
		Attributes.unifyCharacterSets(seriesAttrs, instAttrs);
		Attributes attrs = new Attributes(
			seriesAttrs.size() + instAttrs.size() + 10);
		attrs.addAll(seriesAttrs);
		attrs.addAll(instAttrs, true);

		return attrs;
	}

	private void restrict(DetachedCriteria instance)
	{
		QueryBuilder.instancePredicates(instance, context.getPatientIds(),
			context.getQueryKeys());
	}
}
