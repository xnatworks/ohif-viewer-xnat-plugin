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

import org.dcm4che3.data.Tag;

import java.util.*;

public class ServiceAttributes
{
	static public final int[] QIDO_STUDY_ATTRS = {
		Tag.StudyDate,
		Tag.StudyTime,
		Tag.AccessionNumber,
		Tag.ModalitiesInStudy,
		Tag.ReferringPhysicianName,
		Tag.PatientName,
		Tag.PatientID,
		Tag.PatientBirthDate,
		Tag.PatientSex,
		Tag.StudyID,
		Tag.StudyInstanceUID,
		Tag.StudyDescription,
		Tag.NumberOfStudyRelatedSeries,
		Tag.NumberOfStudyRelatedInstances
	};

	static public final int[] WADO_RS;

	static {
		WADO_RS = union(
			EntityAttributes.PATIENT_ATTRS,
			EntityAttributes.STUDY_ATTRS,
			EntityAttributes.SERIES_ATTRS,
			EntityAttributes.INSTANCE_ATTRS);
	}

	private static int[] union(int[]... attrs)
	{
		// Store unique tags only
		Set<Integer> c = new TreeSet<>();
		for (int[] attr : attrs)
			for (int i : attr)
				c.add(i);

		int[] a = new int[c.size()];
		int j = 0;
		for (int i : c)
			a[j++] = i;

		return a;
	}
}
