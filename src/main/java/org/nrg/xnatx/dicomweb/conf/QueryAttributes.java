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
package org.nrg.xnatx.dicomweb.conf;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.ElementDictionary;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.util.StringUtils;
import org.dcm4che3.util.TagUtils;

import org.nrg.xnatx.dicomweb.conf.AttributesBuilder;
import org.nrg.xnatx.dicomweb.conf.AttributeSet;
import org.nrg.xnatx.dicomweb.conf.privateelements.PrivateTag;

import org.nrg.xnatx.dicomweb.service.query.impl.OrderByTag;
import org.nrg.xnatx.dicomweb.toolkit.DicomwebConstants;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author mo.alsad
 * <p>
 * Some parts of code are refactored from dcm4chee. Refer to DCM4CHEE_LICENSE.
 */
public class QueryAttributes
{
	private static final ElementDictionary DICT = ElementDictionary.getStandardElementDictionary();

	private final Attributes keys = new Attributes();
	private final AttributesBuilder builder = new AttributesBuilder(keys);
	private final ArrayList<OrderByTag> orderByTags = new ArrayList<>();
	private final Map<String,String> xnatIds;
	private boolean includeAll;
	private boolean includeDefaults = true;

	public QueryAttributes(MultiValueMap<String,String> queryParameters,
		Map<String,AttributeSet> attributeSetMap, Map<String,String> xnatIds)
	{
		this.xnatIds = xnatIds;
		addXnatIds();
		parseQueryParameters(splitAndDecode(queryParameters), attributeSetMap);
	}

	private void addXnatIds() {
		keys.setString(PrivateTag.PrivateCreator, PrivateTag.XNATProjectID,
			VR.LO, xnatIds.get(DicomwebConstants.XNAT_PROJECT_ID));
		keys.setString(PrivateTag.PrivateCreator, PrivateTag.XNATSubjectID,
			VR.LO, xnatIds.get(DicomwebConstants.XNAT_SUBJECT_ID));
		keys.setString(PrivateTag.PrivateCreator, PrivateTag.XNATExperimentID,
			VR.LO, xnatIds.get(DicomwebConstants.XNAT_SESSION_ID));
		keys.setNull(PrivateTag.PrivateCreator, PrivateTag.XNATScanID, VR.LO);
	}

	private static String decodeURL(String s)
	{
		try
		{
			return URLDecoder.decode(s, StandardCharsets.UTF_8.name());
		}
		catch (UnsupportedEncodingException e)
		{
			throw new AssertionError(e);
		}
	}

	private static MultiValueMap<String,String> splitAndDecode(
		MultiValueMap<String,String> queryParameters)
	{
		MultiValueMap<String,String> map = new LinkedMultiValueMap<>();
		for (Map.Entry<String,List<String>> entry : queryParameters.entrySet())
			for (String values : entry.getValue())
				for (String value : StringUtils.split(values, ','))
					map.add(entry.getKey(), decodeURL(value));
		return map;
	}

	public void addReturnTags(int... tags)
	{
		for (int tag : tags)
			builder.setNullIfAbsent(tag);
	}

	public ArrayList<OrderByTag> getOrderByTags()
	{
		return orderByTags;
	}

	public Attributes getQueryKeys()
	{
		return keys;
	}

	public Attributes getReturnKeys(int[] includetags)
	{
		Attributes returnKeys = new Attributes(
			keys.size() + 4 + includetags.length);
		returnKeys.addAll(keys);
		returnKeys.setNull(Tag.SpecificCharacterSet, VR.CS);
		returnKeys.setNull(Tag.RetrieveAETitle, VR.AE);
		returnKeys.setNull(Tag.InstanceAvailability, VR.CS);
		returnKeys.setNull(Tag.TimezoneOffsetFromUTC, VR.SH);
		for (int tag : includetags)
			returnKeys.setNull(tag, DICT.vrOf(tag));
		return returnKeys;
	}

	public boolean isIncludeAll()
	{
		return includeAll;
	}

	public boolean isIncludeDefaults()
	{
		return includeDefaults;
	}

	public boolean isIncludePrivate()
	{
		return includeAll || keys.containsTagInRange(PrivateTag.XNATProjectID,
			PrivateTag.XNATScanID);
	}

	private void addIncludeTag(List<String> includefields,
		Map<String,AttributeSet> attributeSetMap)
	{
		for (String s : includefields)
		{
			if (s.equals("all"))
			{
				includeAll = true;
				break;
			}

			// Enable to only return the attributes specified by includefield
			// without including the default set of attributes
			// specified by DICOM Part 18 (6.7.1.2.2 Query Result Attributes)
			// https://github.com/dcm4che/dcm4chee-arc-light/issues/1690
			includeDefaults = false;

			for (String field : StringUtils.split(s, ','))
				if (!includeAttributeSet(s, attributeSetMap))
				{
					try
					{
						int[] tagPath = TagUtils.parseTagPath(field);
						builder.setNullIfAbsent(tagPath);
					}
					catch (IllegalArgumentException e2)
					{
						throw new IllegalArgumentException("includefield=" + s);
					}
				}
		}
	}

	private void addOrderByTag(List<String> orderby)
	{
		for (String s : orderby)
		{
			try
			{
				for (String field : StringUtils.split(s, ','))
				{
					boolean desc = field.charAt(0) == '-';
					int tags[] = TagUtils.parseTagPath(desc ? field.substring(1) : field);
					int tag = tags[tags.length - 1];
					orderByTags.add(desc ? OrderByTag.desc(tag) : OrderByTag.asc(tag));
				}
			}
			catch (IllegalArgumentException e)
			{
				throw new IllegalArgumentException("orderby=" + s);
			}
		}
	}

	private void addQueryKey(String attrPath, List<String> values)
	{
		try
		{
			builder.setString(TagUtils.parseTagPath(attrPath),
				values.toArray(new String[0]));
		}
		catch (IllegalArgumentException e)
		{
			throw new IllegalArgumentException(
				"Illegal query attribute: "+ attrPath + "=" + values.get(0));
		}
	}

	private boolean includeAttributeSet(String includefield,
		Map<String,AttributeSet> attributeSetMap)
	{
		if (attributeSetMap != null)
		{
			AttributeSet attributeSet = attributeSetMap.get(includefield);
			if (attributeSet != null)
			{
				for (int tag : attributeSet.getSelection())
					builder.setNullIfAbsent(tag);
				return true;
			}
		}
		return false;
	}

	private void parseQueryParameters(MultiValueMap<String,String> map,
		Map<String,AttributeSet> attributeSetMap)
	{
		for (Map.Entry<String,List<String>> entry : map.entrySet())
		{
			String key = entry.getKey();
			switch (key)
			{
				case "includefield":
					addIncludeTag(entry.getValue(), attributeSetMap);
					break;
				case "orderby":
					addOrderByTag(entry.getValue());
					break;
				case "accept":
				case "charset":
				case "count":
				case "different":
				case "missing":
				case "offset":
				case "limit":
				case "fuzzymatching":
				case "XNATExperimentID":
				case "XNATSubjectID":
				case "XNATProjectID":
					break;
				default:
					addQueryKey(key, entry.getValue());
					break;
			}
		}
	}

}
