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

import org.hibernate.ObjectNotFoundException;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.xnatx.dicomweb.entity.DwSeries;
import org.nrg.xnatx.dicomweb.dao.DicomwebSeriesDAO;
import org.nrg.xnatx.dicomweb.entity.util.EntityProperties;
import org.nrg.xnatx.dicomweb.service.query.DwSeriesDataService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author m.alsad
 */
@Service
public class DwSeriesDataServiceImpl
	extends AbstractHibernateEntityService<DwSeries,DicomwebSeriesDAO>
	implements DwSeriesDataService
{
	@Override
	@Transactional
	public DwSeries createOrUpdate(DwSeries series) throws IOException
	{
		DwSeries example = new DwSeries();
		EntityProperties.setExampleProps(example, EntityProperties.newPropsMap(
			new String[]{"study", "seriesInstanceUid"},
			new Object[]{series.getStudy(), series.getSeriesInstanceUid()}));

		DwSeries existing = this.get(example, true);
		if (existing == null)
		{
			return create(series);
		}

		existing.setData(series.getAttributes());
		existing.setStudy(series.getStudy());
		updateQueryAttributes(existing, series);
		update(existing);

		return existing;
	}

	@Override
	@Transactional(readOnly = true)
	public DwSeries get(DwSeries example, boolean isEager)
	{
		List<DwSeries> matches = getAll(example, isEager);

		return matches.isEmpty() ? null : matches.get(0);
	}

	@Override
	@Transactional(readOnly = true)
	public DwSeries get(String propertyName, Object propertyValue,
		boolean isEager)
	{
		DwSeries example = new DwSeries();
		EntityProperties.setExampleProp(example, propertyName, propertyValue);

		return get(example, isEager);
	}

	@Override
	@Transactional(readOnly = true)
	public List<DwSeries> getAll(DwSeries example, boolean isEager)
	{
		try
		{
			return getDao().getAll(example, isEager);
		}
		catch (ObjectNotFoundException e)
		{
			return new ArrayList<>();
		}
	}

	private void updateQueryAttributes(DwSeries existing, DwSeries series)
	{
		existing.setNumberOfSeriesRelatedInstances(
			series.getNumberOfSeriesRelatedInstances());
		existing.setAvailableTransferSyntaxUid(
			series.getAvailableTransferSyntaxUid());
		existing.setSopClassesInSeries(series.getSopClassesInSeries());
	}
}
