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

import org.dcm4che3.data.*;
import org.nrg.xnatx.dicomweb.conf.QueryRetrieveLevel2;

/**
 * @author m.alsad
 */
public enum QidoRsModel
{
	PATIENT(QueryRetrieveLevel2.PATIENT,
		UID.PatientRootQueryRetrieveInformationModelFind)
		{
			@Override
			public void addRetrieveURL(String requestURL, Attributes match)
			{}
		},
	STUDY(QueryRetrieveLevel2.STUDY,
		UID.StudyRootQueryRetrieveInformationModelFind)
		{
			@Override
			public StringBuffer retrieveURL(String requestURL, Attributes match)
			{
				return super.retrieveURL(requestURL, match)
										.append("/studies/")
										.append(match.getString(Tag.StudyInstanceUID));
			}
		},
	SERIES(QueryRetrieveLevel2.SERIES,
		UID.StudyRootQueryRetrieveInformationModelFind)
		{
			@Override
			StringBuffer retrieveURL(String requestURL, Attributes match)
			{
				return STUDY.retrieveURL(requestURL, match)
										.append("/series/")
										.append(match.getString(Tag.SeriesInstanceUID));
			}
		},
	INSTANCE(QueryRetrieveLevel2.IMAGE,
		UID.StudyRootQueryRetrieveInformationModelFind)
		{
			@Override
			StringBuffer retrieveURL(String requestURL, Attributes match)
			{
				return SERIES.retrieveURL(requestURL, match)
										 .append("/instances/")
										 .append(match.getString(Tag.SOPInstanceUID));
			}
		};

	final QueryRetrieveLevel2 qrLevel;
	// ToDo: attributes coercion - left to be implemented in the future
	final String sopClassUid;
	Attributes returnKeys;
	boolean includeAll;

	QidoRsModel(QueryRetrieveLevel2 qrLevel, String sopClassUid)
	{
		this.qrLevel = qrLevel;
		this.sopClassUid = sopClassUid;
	}

	void addRetrieveURL(String requestURL, Attributes match)
	{
		match.setString(Tag.RetrieveURL, VR.UR,
			retrieveURL(requestURL, match).toString());
	}

	QueryRetrieveLevel2 getQueryRetrieveLevel()
	{
		return qrLevel;
	}

	String getSOPClassUid()
	{
		return sopClassUid;
	}

	StringBuffer retrieveURL(String requestURL, Attributes match)
	{
		StringBuffer sb = new StringBuffer(requestURL);
		sb.setLength(sb.lastIndexOf("/rs/") + 3);
		return sb;
	}

	void setIncludeAll(boolean includeAll)
	{
		this.includeAll = includeAll;
	}

	void setReturnKeys(Attributes returnKeys)
	{
		this.returnKeys = returnKeys;
	}
}

