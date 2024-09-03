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
package org.nrg.xnatx.dicomweb.service.qido;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.IDWithIssuer;
import org.nrg.xnatx.dicomweb.service.query.DicomwebDataService;
import org.nrg.xnatx.dicomweb.conf.QueryRetrieveLevel2;
import org.nrg.xnatx.dicomweb.service.query.impl.OrderByTag;

import java.util.List;
import java.util.Map;

/**
 * @author m.alsad
 */
public class QidoRsContext
{
	private final DicomwebDataService dwDataService;
	private Map<String,String> xnatIds;
	private QueryRetrieveLevel2 queryRetrieveLevel;
	private IDWithIssuer[] patientIds = {};
	private Attributes queryKeys;
	private Attributes returnKeys;
	private boolean returnPrivate;
	private List<OrderByTag> orderByTags;
	private int offset;
	private int limit;
	// ToDo - NOTE: fuzzymatching is not supported
	private boolean fuzzymatching;


	public QidoRsContext(DicomwebDataService dwDataService)
	{
		this.dwDataService = dwDataService;
	}

	public DicomwebDataService getDwDataService()
	{
		return dwDataService;
	}

	public int getLimit()
	{
		return limit;
	}

	public void setLimit(int limit)
	{
		this.limit = limit;
	}

	public int getOffset()
	{
		return offset;
	}

	public void setOffset(int offset)
	{
		this.offset = offset;
	}

	public List<OrderByTag> getOrderByTags()
	{
		return orderByTags;
	}

	public void setOrderByTags(
		List<OrderByTag> orderByTags)
	{
		this.orderByTags = orderByTags;
	}

	public IDWithIssuer[] getPatientIds()
	{
		return patientIds;
	}

	public void setPatientIds(IDWithIssuer... patientIds)
	{
		this.patientIds = patientIds != null ? patientIds : IDWithIssuer.EMPTY;
	}

	public Attributes getQueryKeys()
	{
		return queryKeys;
	}

	public void setQueryKeys(Attributes queryKeys)
	{
		this.queryKeys = queryKeys;
	}

	public QueryRetrieveLevel2 getQueryRetrieveLevel()
	{
		return queryRetrieveLevel;
	}

	public void setQueryRetrieveLevel(QueryRetrieveLevel2 queryRetrieveLevel)
	{
		this.queryRetrieveLevel = queryRetrieveLevel;
	}

	public Attributes getReturnKeys()
	{
		return returnKeys;
	}

	public void setReturnKeys(Attributes returnKeys)
	{
		this.returnKeys = returnKeys;
	}

	public Map<String,String> getXnatIds()
	{
		return xnatIds;
	}

	public void setXnatIds(Map<String,String> xnatIds)
	{
		this.xnatIds = xnatIds;
	}

	public boolean isFuzzymatching()
	{
		return fuzzymatching;
	}

	public void setFuzzymatching(boolean fuzzymatching)
	{
		this.fuzzymatching = fuzzymatching;
	}

	public boolean isReturnPrivate()
	{
		return returnPrivate;
	}

	public void setReturnPrivate(boolean returnPrivate)
	{
		this.returnPrivate = returnPrivate;
	}
}
