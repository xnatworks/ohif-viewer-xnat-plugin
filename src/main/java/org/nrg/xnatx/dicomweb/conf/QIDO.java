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

import java.util.Arrays;

/**
 * @author mo.alsad
 *
 * Some parts of code are refactored from dcm4chee. Refer to DCM4CHEE_LICENSE.
 */
public enum QIDO
{
	PATIENT(
		Tag.PatientName,
		Tag.PatientID,
		Tag.PatientBirthDate,
		Tag.PatientSex
//		Tag.NumberOfPatientRelatedStudies,
//		Tag.NumberOfPatientRelatedSeries,
//		Tag.NumberOfPatientRelatedInstances
	),
	STUDY(
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
		Tag.NumberOfStudyRelatedSeries,
		Tag.NumberOfStudyRelatedInstances
	),
	SERIES(
		Tag.Modality,
		Tag.SeriesDescription,
		Tag.SeriesNumber,
		Tag.SeriesInstanceUID,
		Tag.NumberOfSeriesRelatedInstances,
		Tag.PerformedProcedureStepStartDate,
		Tag.PerformedProcedureStepStartTime,
		Tag.RequestAttributesSequence
	),
	INSTANCE(
		Tag.SOPClassUID,
		Tag.SOPInstanceUID,
		Tag.AvailableTransferSyntaxUID,
		Tag.InstanceNumber,
		Tag.Rows,
		Tag.Columns,
		Tag.BitsAllocated,
		Tag.NumberOfFrames
	),
	STUDY_SERIES(
		catAndSort(STUDY.includedTags, SERIES.includedTags)),
	STUDY_SERIES_INSTANCE(
		catAndSort(STUDY.includedTags, SERIES.includedTags,
			INSTANCE.includedTags)),
	SERIES_INSTANCE(
		catAndSort(SERIES.includedTags, INSTANCE.includedTags));

	public final int[] includedTags;

	QIDO(int... includedTags)
	{
		this.includedTags = includedTags;
	}

	private static int[] catAndSort(int[]... srcs)
	{
		int totlen = 0;
		for (int[] src : srcs)
			totlen += src.length;

		int[] dest = new int[totlen];
		int off = 0;
		for (int[] src : srcs) {
			System.arraycopy(src, 0, dest, off, src.length);
			off += src.length;
		}
		Arrays.sort(dest);
		return dest;
	}

	public void addReturnTags(QueryAttributes queryAttributes)
	{
		queryAttributes.addReturnTags(includedTags);
	}
	}
