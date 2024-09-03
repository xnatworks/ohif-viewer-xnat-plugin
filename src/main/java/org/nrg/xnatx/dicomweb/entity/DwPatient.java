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
package org.nrg.xnatx.dicomweb.entity;

import com.google.common.base.MoreObjects;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.nrg.xnatx.dicomweb.conf.AttributeFilter;
import org.nrg.xnatx.dicomweb.conf.DicomwebDeviceConfiguration;
import org.nrg.xnatx.dicomweb.entity.util.QueryAttribute;

import javax.persistence.*;
import java.io.IOException;
import java.util.Objects;

/**
 * @author mo.alsad
 */
@Entity
@Table(
	name = "icr_dicomweb_patient_data",
	uniqueConstraints = {
		@UniqueConstraint(columnNames = {"subject_id"})
	},
	indexes = {
		@Index(columnList = "patient_id"),
		@Index(columnList = "patient_name"),
		@Index(columnList = "patient_birthdate"),
		@Index(columnList = "patient_sex"),
		@Index(columnList = "num_studies")
	}
)
public class DwPatient extends DwEntity
{
	private String subjectId;
	private String patientId;
	private String patientName;
	private String patientBirthDate;
	private String patientSex;

	@QueryAttribute
	private int numberOfStudies;

	public void decrementNumberOfStudies()
	{
		numberOfStudies = Math.max(numberOfStudies - 1, 0);
	}

	@Basic(optional = false)
	@Column(name = "num_studies")
	public int getNumberOfStudies()
	{
		return numberOfStudies;
	}

	public void setNumberOfStudies(int numberOfStudies)
	{
		this.numberOfStudies = numberOfStudies;
	}

	@Basic(optional = false)
	@Column(name = "patient_birthdate")
	public String getPatientBirthDate()
	{
		return patientBirthDate;
	}

	private void setPatientBirthDate(String patientBirthDate)
	{
		this.patientBirthDate = patientBirthDate;
	}

	@Basic(optional = false)
	@Column(name = "patient_id")
	public String getPatientId()
	{
		return patientId;
	}

	private void setPatientId(String patientID)
	{
		this.patientId = patientID;
	}

	@Basic(optional = false)
	@Column(name = "patient_name")
	public String getPatientName()
	{
		return patientName;
	}

	private void setPatientName(String patientName)
	{
		this.patientName = patientName;
	}

	@Basic(optional = false)
	@Column(name = "patient_sex")
	public String getPatientSex()
	{
		return patientSex;
	}

	private void setPatientSex(String patientSex)
	{
		this.patientSex = patientSex;
	}

	@Override
	@Transient
	public Attributes getQueryAttributes()
	{
		if (numberOfStudies == 0)
		{
			return new Attributes(0);
		}

		Attributes attrs = new Attributes(1);
		attrs.setInt(Tag.NumberOfPatientRelatedStudies, VR.IS, numberOfStudies);

		return attrs;
	}

	@Basic(optional = false)
	@Column(name = "subject_id")
	public String getSubjectId()
	{
		return subjectId;
	}

	public void setSubjectId(String subjectID)
	{
		this.subjectId = subjectID;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(subjectId, patientId);
	}

	public void incrementNumberOfStudies()
	{
		numberOfStudies++;
	}

	@Override
	public void setData(Attributes attrs) throws IOException
	{
		patientId = attrs.getString(Tag.PatientID, "*");
		patientName = attrs.getString(Tag.PatientName, "*");
		patientBirthDate = attrs.getString(Tag.PatientBirthDate, "*");
		patientSex = attrs.getString(Tag.PatientSex, "*").toUpperCase();

		AttributeFilter filter = DicomwebDeviceConfiguration.getAttributeFilter(
			org.nrg.xnatx.dicomweb.conf.Entity.Patient);
		Attributes attrsBlob = new Attributes(attrs, filter.getSelection());
		setAttributes(attrsBlob);

		// Query attributes
		numberOfStudies = 0;
	}

	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(this).add("Patient[pk]=", getId())
											.add("ID=", patientId).add("Subject ID=", subjectId)
											.add("No. studies=", numberOfStudies).toString();
	}
}
