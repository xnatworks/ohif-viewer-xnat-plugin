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
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.nrg.xnatx.dicomweb.conf.Availability;
import org.nrg.xnatx.dicomweb.conf.DicomwebDeviceConfiguration;
import org.nrg.xnatx.dicomweb.conf.Entity;
import org.nrg.xnatx.dicomweb.conf.privateelements.PrivateTag;
import org.nrg.xnatx.dicomweb.service.qido.QidoRsContext;
import org.nrg.xnatx.dicomweb.service.query.Query;
import org.nrg.xnatx.dicomweb.toolkit.DicomwebConstants;

import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

/**
 * @author mo.alsad
 * <p>
 * Some parts of code are refactored from dcm4chee. Refer to DCM4CHEE_LICENSE.
 */
abstract class AbstractQuery implements Query
{
	final static String DEFAULT_CACHE_REGION = "nrg";
	final Map<Long,Attributes> cachedStudyAttributes = new HashMap<>();
	final Map<Long,Attributes> cachedSeriesAttributes = new HashMap<>();
	final List<String> propertyList = new ArrayList<>();
	final QidoRsContext context;
	private final int offset;
	long count;
	private Stream<?> resultStream;
	private Iterator<?> results;

	public AbstractQuery(QidoRsContext context, String[]... propertyArrays)
	{
		this.context = context;
		this.offset = context.getOffset();
		this.count = 0;

		for (String[] properties : propertyArrays)
		{
			Collections.addAll(propertyList, properties);
		}
	}

	static void addExtraAttributes(QidoRsContext context, Attributes attrs,
		String xnatScanId)
	{
		Map<String,String> xnatIds = context.getXnatIds();
		String sharedProjectId =
			xnatIds.get(DicomwebConstants.XNAT_SHARED_PROJECT_ID);
		String projectId = sharedProjectId != null
												 ? sharedProjectId
												 : xnatIds.get(DicomwebConstants.XNAT_PROJECT_ID);

		attrs.setString(Tag.RetrieveAETitle, VR.AE, projectId);
		attrs.setString(Tag.InstanceAvailability, VR.CS,
			Availability.ONLINE.toString());

		attrs.setString(PrivateTag.PrivateCreator, PrivateTag.XNATProjectID,
			VR.LO, projectId);
		attrs.setString(PrivateTag.PrivateCreator, PrivateTag.XNATSubjectID,
			VR.LO, xnatIds.get(DicomwebConstants.XNAT_SUBJECT_ID));
		attrs.setString(PrivateTag.PrivateCreator, PrivateTag.XNATExperimentID,
			VR.LO, xnatIds.get(DicomwebConstants.XNAT_SESSION_ID));

		if (xnatScanId != null)
		{
			attrs.setString(PrivateTag.PrivateCreator, PrivateTag.XNATScanID,
				VR.LO, xnatScanId);
		}
	}

	@Override
	public Attributes adjust(Attributes match)
	{
		if (match == null)
		{
			return null;
		}

		Attributes returnKeys = context.getReturnKeys();
		if (returnKeys == null)
		{
			return match;
		}

		Attributes filtered = new Attributes(returnKeys.size());
		filtered.addSelected(match, returnKeys);
		filtered.supplementEmpty(returnKeys);
		return filtered;
	}

	@Override
	public void close()
	{
		close(resultStream);
	}

	@Override
	public void executeQuery(final Session session)
	{
		executeQuery(session, 0);
	}

	@Override
	public void executeQuery(final Session session, int limit)
	{
		close(resultStream);

		DetachedCriteria detachedCriteria = multiselect();
		Criteria criteria = getExecutableCriteria(session, detachedCriteria);

		if (offset > 0)
		{
			criteria.setFirstResult(offset);
		}
		if (limit > 0)
		{
			criteria.setMaxResults(limit);
		}

		int fetchSize = DicomwebDeviceConfiguration.QUERY_FETCH_SIZE;
		criteria.setFetchSize(fetchSize);

		resultStream = criteria.list().stream();
		results = resultStream.iterator();
	}

	@Override
	public long getCount()
	{
		return count;
	}

	protected static Criteria getExecutableCriteria(Session session,
		DetachedCriteria detachedCriteria)
	{
		Criteria criteria = detachedCriteria.getExecutableCriteria(session);
		criteria.setCacheable(true);
		criteria.setCacheRegion(DEFAULT_CACHE_REGION);

		return criteria;
	}

	@Override
	public QidoRsContext getQueryContext()
	{
		return context;
	}

	@Override
	public boolean hasMoreMatches()
	{
		return results.hasNext();
	}

	protected abstract DetachedCriteria multiselect();

	@Override
	public Attributes nextMatch() throws IOException
	{
		return toAttributes(results.next());
	}

	protected abstract <E> Attributes toAttributes(E results)
		throws IOException;

	private void close(Stream<?> resultStream)
	{
		if (resultStream != null)
		{
			resultStream.close();
		}
	}
}
