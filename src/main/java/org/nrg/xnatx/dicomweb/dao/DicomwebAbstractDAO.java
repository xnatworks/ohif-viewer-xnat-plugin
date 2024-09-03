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

import org.hibernate.Criteria;
import org.hibernate.criterion.*;
import org.hibernate.transform.Transformers;
import org.nrg.framework.generics.GenericUtils;
import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.framework.orm.hibernate.BaseHibernateEntity;

import java.util.List;

public abstract class DicomwebAbstractDAO<E extends BaseHibernateEntity> extends AbstractHibernateDAO<E>
{
	public static final String[] COMMON_PROJECTION_PROPERTIES =
		{"id", "revision", "enabled", "created", "timestamp", "disabled"};

	public static ProjectionList getProjectionList(final String... properties)
	{
		ProjectionList projectionList = Projections.projectionList();

		for (String prop: COMMON_PROJECTION_PROPERTIES)
		{
			projectionList.add(Projections.property(prop), prop);
		}

		for (String prop : properties)
		{
			projectionList.add(Projections.property(prop), prop);
		}

		return projectionList;
	}

	public long countByProperty(String propertyName, Object propertyValue)
	{
		final Criteria criteria = getCriteriaForType();
		criteria.setProjection(Projections.rowCount());
		criteria.add(Restrictions.eq(propertyName, propertyValue));
		return (long) criteria.uniqueResult();
	}

	public List<E> getAll(final E example, final String[] excludeProperty,
		final ProjectionList projectionList, boolean isEager,
		List<Criterion> extraCriteria)
	{
		if (isEager)
		{
			return getAllByExample(example, excludeProperty,
				null, extraCriteria);
		}
		else
		{
			return getAllByExample(example, excludeProperty,
				projectionList, extraCriteria);
		}
	}

	protected List<E> getAllByExample(final E exampleEntity,
		final String[] excludeProperty,	final ProjectionList projectionList,
		List<Criterion> extraCriteria)
	{
		final Criteria criteria = getCriteriaForType();
		// Create Example
		final Example example = Example.create(exampleEntity);
		for (final String exclude : excludeProperty)
		{
			example.excludeProperty(exclude);
		}

		example.excludeNone().excludeZeroes();
		criteria.add(example);

		for (Criterion extraCriterion : extraCriteria)
		{
			criteria.add(extraCriterion);
		}

		// Add projection
		if (projectionList != null)
		{
			criteria.setProjection(projectionList);
			criteria.setResultTransformer(
				Transformers.aliasToBean(exampleEntity.getClass()));
		}

		return GenericUtils.convertToTypedList(criteria.list(),
			getParameterizedType());
	}
}
