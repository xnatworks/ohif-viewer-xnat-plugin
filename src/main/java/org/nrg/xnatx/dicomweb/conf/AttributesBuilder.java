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
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.VR;

/**
 * @author mo.alsad
 *
 * Some parts of code are refactored from dcm4chee. Refer to DCM4CHEE_LICENSE.
 */
public class AttributesBuilder
{

	private static final ElementDictionary DICT = ElementDictionary.getStandardElementDictionary();

	private final Attributes attrs;

	public AttributesBuilder(Attributes attrs)
	{
		this.attrs = attrs;
	}

	public void setString(int[] tagPath, String... ss)
	{
		int tag = tagPath[tagPath.length - 1];
		VR vr = DICT.vrOf(tag);
		nestedKeys(tagPath).setString(tag, vr, ss);
	}

	public void setNullIfAbsent(int... tagPath)
	{
		int tag = tagPath[tagPath.length - 1];
		Attributes item = nestedKeys(tagPath);
		setNullIfAbsent(item, tag);
	}

	public static void setNullIfAbsent(Attributes item, int... tags)
	{
		for (int tag : tags)
		{
			setNullIfAbsent(item, tag);
		}
	}

	public static void setNullIfAbsent(Attributes item, int tag)
	{
		if (!item.contains(tag))
		{
			VR vr = DICT.vrOf(tag);
			if (vr == VR.SQ)
				item.newSequence(tag, 1).add(new Attributes(0));
			else
				item.setNull(tag, vr);
		}
	}

	private Attributes nestedKeys(int[] tags)
	{
		Attributes item = attrs;
		for (int i = 0; i < tags.length - 1; i++)
		{
			int tag = tags[i];
			Sequence sq = item.getSequence(tag);
			if (sq == null)
				sq = item.newSequence(tag, 1);
			if (sq.isEmpty())
				sq.add(new Attributes());
			item = sq.get(0);
		}
		return item;
	}
}