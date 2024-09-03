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

import lombok.extern.slf4j.Slf4j;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.ElementDictionary;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.util.StringUtils;
import org.dcm4che3.util.TagUtils;
import org.nrg.xnatx.plugin.PluginCode;
import org.nrg.xnatx.plugin.PluginException;

import java.util.EnumSet;

/**
 * @author mo.alsad
 *
 * Some parts of code are refactored from dcm4chee. Refer to DCM4CHEE_LICENSE.
 */
@Slf4j
public enum QueryRetrieveLevel2
{
	PATIENT(Tag.PatientID, VR.LO),
	STUDY(Tag.StudyInstanceUID, VR.UI),
	SERIES(Tag.SeriesInstanceUID, VR.UI),
	IMAGE(Tag.SOPInstanceUID, VR.UI);

	private static ElementDictionary DICT = ElementDictionary.getStandardElementDictionary();
	private final int uniqueKey;
	private final VR vrOfUniqueKey;

	QueryRetrieveLevel2(int uniqueKey, VR vrOfUniqueKey)
	{
		this.uniqueKey = uniqueKey;
		this.vrOfUniqueKey = vrOfUniqueKey;
	}

	public static QueryRetrieveLevel2 validateQueryIdentifier(
		Attributes keys, EnumSet<QueryRetrieveLevel2> levels, boolean relational)
		throws PluginException
	{
		return validateIdentifier(keys, levels, relational, false, true);
	}

	public static QueryRetrieveLevel2 validateQueryIdentifier(
		Attributes keys, EnumSet<QueryRetrieveLevel2> levels, boolean relational,
		boolean lenient)
		throws PluginException
	{
		return validateIdentifier(keys, levels, relational, lenient, true);
	}

	public static QueryRetrieveLevel2 validateRetrieveIdentifier(
		Attributes keys, EnumSet<QueryRetrieveLevel2> levels, boolean relational)
		throws PluginException
	{
		return validateIdentifier(keys, levels, relational, false, false);
	}

	public static QueryRetrieveLevel2 validateRetrieveIdentifier(
		Attributes keys, EnumSet<QueryRetrieveLevel2> levels, boolean relational,
		boolean lenient)
		throws PluginException
	{
		return validateIdentifier(keys, levels, relational, lenient, false);
	}

	private static PluginException identifierDoesNotMatchSOPClass(
		String comment, int tag)
	{
		return new PluginException("Identifier does not match SOPClass",
			PluginCode.IllegalArgument);
	}

	private static PluginException invalidAttributeValue(int tag,
		String value)
	{
		return identifierDoesNotMatchSOPClass(
			"Invalid " + DICT.keywordOf(tag) + " " + TagUtils.toString(
				tag) + " - " + value,
			Tag.QueryRetrieveLevel);
	}

	private static PluginException missingAttribute(int tag)
	{
		return identifierDoesNotMatchSOPClass(
			"Missing " + DICT.keywordOf(tag) + " " + TagUtils.toString(tag), tag);
	}

	private static QueryRetrieveLevel2 validateIdentifier(
		Attributes keys, EnumSet<QueryRetrieveLevel2> levels, boolean relational,
		boolean lenient, boolean query)
		throws PluginException
	{
		String value = keys.getString(Tag.QueryRetrieveLevel);
		if (value == null)
		{
			throw missingAttribute(Tag.QueryRetrieveLevel);
		}

		QueryRetrieveLevel2 level;
		try
		{
			level = QueryRetrieveLevel2.valueOf(value);
		}
		catch (IllegalArgumentException e)
		{
			throw invalidAttributeValue(Tag.QueryRetrieveLevel, value);
		}
		if (!levels.contains(level))
		{
			throw invalidAttributeValue(Tag.QueryRetrieveLevel, value);
		}

		for (QueryRetrieveLevel2 level2 : levels)
		{
			if (level2 == level)
			{
				level.checkUniqueKey(keys, query, false,
					level != QueryRetrieveLevel2.PATIENT);
				break;
			}
			level2.checkUniqueKey(keys, relational, lenient, false);
		}

		return level;
	}

	public int uniqueKey()
	{
		return uniqueKey;
	}

	public VR vrOfUniqueKey()
	{
		return vrOfUniqueKey;
	}

	private void checkUniqueKey(Attributes keys, boolean optional,
		boolean lenient, boolean multiple)
		throws PluginException
	{
		String[] ids = keys.getStrings(uniqueKey);
		if (ids == null || ids.length == 0)
		{
			if (!optional)
			{
				if (lenient)
				{
					log.info(
						"Missing " + DICT.keywordOf(uniqueKey) + " " + TagUtils.toString(
							uniqueKey) + " in Query/Retrieve Identifier");
				}
				else
				{
					throw missingAttribute(uniqueKey);
				}
			}
		}
		else
		{
			if (!multiple && ids.length > 1)
			{
				throw invalidAttributeValue(uniqueKey, StringUtils.concat(ids, '\\'));
			}
		}
	}
}
