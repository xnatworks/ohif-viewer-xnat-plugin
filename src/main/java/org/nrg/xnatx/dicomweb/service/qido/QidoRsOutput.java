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
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.json.JSONWriter;
import org.nrg.xnatx.dicomweb.service.query.Query;
import org.nrg.xnatx.dicomweb.toolkit.DicomwebUtils;
import org.nrg.xnatx.dicomweb.toolkit.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author mo.alsad
 *
 * Some parts of code are refactored from dcm4chee. Refer to DCM4CHEE_LICENSE.
 */
public enum QidoRsOutput
{
	JSON
		{
			@Override
			StreamingResponseBody entity(Query query, QidoRsModel model) throws Exception
			{
				List<Attributes> matches = this.matches(query, model);

				return out -> {
					JsonGenerator gen = Json.createGenerator(out);
					JSONWriter writer =
						DicomwebUtils.encodeAsJSONNumber(new JSONWriter(gen));
					gen.writeStartArray();
					for (Attributes match : matches)
						writer.write(match);
					gen.writeEnd();
					gen.flush();
				};
			}

			@Override
			MediaType type()
			{
				return MediaTypes.APPLICATION_DICOM_JSON;
			}
		};


	private String requestUrl;

	void setRequestUrl(String requestUrl)
	{
		this.requestUrl = requestUrl;
	}

	abstract MediaType type();

	abstract StreamingResponseBody entity(Query query, QidoRsModel model)
		throws Exception;

	Attributes adjust(Attributes match, Query query, QidoRsModel model)
	{
		StringBuffer retrieveURL = model.retrieveURL(requestUrl, match);
		match = query.adjust(match);
		if (model != QidoRsModel.PATIENT)
		{
			if (retrieveURL != null)
			{
				match.setString(Tag.RetrieveURL, VR.UR, retrieveURL.toString());
			}
		}
		return match;
	}

	List<Attributes> matches(Query query, QidoRsModel model)
		throws IOException
	{
		if (query == null)
		{
			return Collections.emptyList();
		}

		final ArrayList<Attributes> matches = new ArrayList<>();
		while (query.hasMoreMatches())
		{
			Attributes tmp = query.nextMatch();
			if (tmp == null)
			{
				continue;
			}
			Attributes match = adjust(tmp, query, model);
			matches.add(match);
		}
		return matches;
	}
}
