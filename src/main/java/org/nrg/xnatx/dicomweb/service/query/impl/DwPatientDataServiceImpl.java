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
import org.nrg.xnatx.dicomweb.entity.DwPatient;
import org.nrg.xnatx.dicomweb.dao.DicomwebPatientDAO;
import org.nrg.xnatx.dicomweb.entity.util.EntityProperties;
import org.nrg.xnatx.dicomweb.service.query.DwPatientDataService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author m.alsad
 */
@Service
public class DwPatientDataServiceImpl
	extends AbstractHibernateEntityService<DwPatient,DicomwebPatientDAO>
	implements DwPatientDataService
{
	@Override
	@Transactional
	public DwPatient createOrUpdate(DwPatient patient) throws IOException
	{
		DwPatient existing = this.get("subjectId",
			patient.getSubjectId(), true);
		if (existing == null)
		{
			return create(patient);
		}

		existing.setData(patient.getAttributes());
		updateQueryAttributes(existing, patient);
		update(existing);

		return existing;
	}

	@Override
	@Transactional(readOnly = true)
	public DwPatient get(DwPatient example, boolean isEager)
	{
		List<DwPatient> matches = getAll(example, isEager);

		return matches.isEmpty() ? null : matches.get(0);
	}

	@Override
	@Transactional(readOnly = true)
	public DwPatient get(String propertyName, Object propertyValue,
		boolean isEager)
	{
		DwPatient example = new DwPatient();
		EntityProperties.setExampleProp(example, propertyName, propertyValue);

		return get(example, isEager);
	}

	@Override
	@Transactional(readOnly = true)
	public List<DwPatient> getAll(DwPatient example, boolean isEager)
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

	private void updateQueryAttributes(DwPatient existing, DwPatient patient)
	{
		existing.setNumberOfStudies(patient.getNumberOfStudies());
	}
}
