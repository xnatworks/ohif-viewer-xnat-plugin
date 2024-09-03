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
import org.nrg.xnatx.dicomweb.entity.DwInstance;
import org.nrg.xnatx.dicomweb.dao.DicomwebInstanceDAO;
import org.nrg.xnatx.dicomweb.entity.DwSeries;
import org.nrg.xnatx.dicomweb.entity.util.EntityProperties;
import org.nrg.xnatx.dicomweb.service.query.DwInstanceDataService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author m.alsad
 */
@Service
public class DwInstanceDataServiceImpl
	extends AbstractHibernateEntityService<DwInstance,DicomwebInstanceDAO>
	implements DwInstanceDataService
{
	@Override
	@Transactional
	public DwInstance createOrUpdate(DwInstance instance)
		throws IOException
	{
		DwInstance example = new DwInstance();
		EntityProperties.setExampleProps(example, EntityProperties.newPropsMap(
			new String[]{"series", "sopInstanceUid"},
			new Object[]{instance.getSeries(), instance.getSopInstanceUid()}));

		DwInstance existing = this.get(example, true);
		if (existing == null)
		{
			return create(instance);
		}

		existing.setData(instance.getAttributes());
		existing.setMetadata(instance.getMetadata());
		existing.setSeries(instance.getSeries());
		update(existing);

		return existing;
	}

	@Override
	public int deleteAll(DwSeries series)
	{
		return getDao().deleteAll(series);
	}

	@Override
	@Transactional(readOnly = true)
	public DwInstance get(DwInstance example, boolean isEager)
	{
		List<DwInstance> matches = getAll(example, isEager);

		return matches.isEmpty() ? null : matches.get(0);
	}

	@Override
	@Transactional(readOnly = true)
	public DwInstance get(String propertyName, Object propertyValue,
		boolean isEager)
	{
		DwInstance example = new DwInstance();
		EntityProperties.setExampleProp(example, propertyName, propertyValue);

		return get(example, isEager);
	}

	@Override
	@Transactional(readOnly = true)
	public List<DwInstance> getAll(DwInstance example, boolean isEager)
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
}
