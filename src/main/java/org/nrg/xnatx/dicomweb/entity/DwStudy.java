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
import org.dcm4che3.util.DateUtils;
import org.dcm4che3.util.StringUtils;
import org.nrg.xnatx.dicomweb.conf.AttributeFilter;
import org.nrg.xnatx.dicomweb.conf.DicomwebDeviceConfiguration;
import org.nrg.xnatx.dicomweb.entity.util.QueryAttribute;

import javax.persistence.*;
import java.io.IOException;
import java.util.Date;
import java.util.Objects;

/**
 * @author mo.alsad
 */
@Entity
@Table(
	name = "icr_dicomweb_study_data",
	uniqueConstraints = {
		@UniqueConstraint(columnNames = {"session_id"})
	},
	indexes = {
		@Index(columnList = "study_iuid"),
		@Index(columnList = "study_date"),
		@Index(columnList = "study_time"),
		@Index(columnList = "accession_no"),
		@Index(columnList = "study_desc")
	}
)
public class DwStudy extends DwEntity
{
	private String sessionId;
	private String studyInstanceUid;
	private String studyId;
	private String studyDate;
	private String studyTime;
	private String accessionNumber;
	private String studyDescription;
	private DwPatient patient;
	private String storagePath;

	@QueryAttribute
	private int numberOfStudyRelatedInstances;
	@QueryAttribute
	private int numberOfStudyRelatedSeries;
	@QueryAttribute
	private String modalitiesInStudy;
	@QueryAttribute
	private String sopClassesInStudy;

	@Basic(optional = false)
	@Column(name = "accession_no")
	public String getAccessionNumber()
	{
		return accessionNumber;
	}

	private void setAccessionNumber(String accessionNumber)
	{
		this.accessionNumber = accessionNumber;
	}

	@Column(name = "mods_in_study")
	public String getModalitiesInStudy()
	{
		return modalitiesInStudy;
	}

	public void setModalitiesInStudy(String modalitiesInStudy)
	{
		this.modalitiesInStudy = modalitiesInStudy;
	}

	@Column(name = "num_instances")
	public int getNumberOfStudyRelatedInstances()
	{
		return numberOfStudyRelatedInstances;
	}

	public void setNumberOfStudyRelatedInstances(
		int numberOfStudyRelatedInstances)
	{
		this.numberOfStudyRelatedInstances = numberOfStudyRelatedInstances;
	}

	@Column(name = "num_series")
	public int getNumberOfStudyRelatedSeries()
	{
		return numberOfStudyRelatedSeries;
	}

	public void setNumberOfStudyRelatedSeries(
		int numberOfStudyRelatedSeries)
	{
		this.numberOfStudyRelatedSeries = numberOfStudyRelatedSeries;
	}

	@ManyToOne(optional = false)
	@JoinColumn(name = "patient_fk")
	public DwPatient getPatient()
	{
		return patient;
	}

	public void setPatient(DwPatient patient)
	{
		this.patient = patient;
	}

	@Column(name = "storage_path")
	public String getStoragePath()
	{
		return storagePath;
	}

	public void setStoragePath(String storagePath)
	{
		this.storagePath = storagePath;
	}

	@Override
	@Transient
	public Attributes getQueryAttributes()
	{
		if (numberOfStudyRelatedInstances == 0)
		{
			return new Attributes(0);
		}

		Attributes attrs = new Attributes(4);
		attrs.setInt(Tag.NumberOfStudyRelatedInstances, VR.IS,
			numberOfStudyRelatedInstances);
		attrs.setInt(Tag.NumberOfStudyRelatedSeries, VR.IS,
			numberOfStudyRelatedSeries);
		attrs.setString(Tag.ModalitiesInStudy, VR.CS,
			StringUtils.split(modalitiesInStudy, '\\'));
		attrs.setString(Tag.SOPClassesInStudy, VR.UI,
			StringUtils.split(sopClassesInStudy, '\\'));

		return attrs;
	}

	@Basic(optional = false)
	@Column(name = "session_id")
	public String getSessionId()
	{
		return sessionId;
	}

	public void setSessionId(String sessionID)
	{
		this.sessionId = sessionID;
	}

	@Column(name = "cuids_in_study", length = 1200)
	public String getSopClassesInStudy()
	{
		return sopClassesInStudy;
	}

	public void setSopClassesInStudy(String sopClassesInStudy)
	{
		this.sopClassesInStudy = sopClassesInStudy;
	}

	@Basic(optional = false)
	@Column(name = "study_date")
	public String getStudyDate()
	{
		return studyDate;
	}

	private void setStudyDate(String studyDate)
	{
		this.studyDate = studyDate;
	}

	@Basic(optional = false)
	@Column(name = "study_desc")
	public String getStudyDescription()
	{
		return studyDescription;
	}

	private void setStudyDescription(String studyDescription)
	{
		this.studyDescription = studyDescription;
	}

	@Column(name = "study_id")
	public String getStudyId()
	{
		return studyId;
	}

	private void setStudyId(String studyID)
	{
		this.studyId = studyID;
	}

	@Basic(optional = false)
	@Column(name = "study_iuid")
	public String getStudyInstanceUid()
	{
		return studyInstanceUid;
	}

	private void setStudyInstanceUid(String studyInstanceUID)
	{
		this.studyInstanceUid = studyInstanceUID;
	}

	@Basic(optional = false)
	@Column(name = "study_time")
	public String getStudyTime()
	{
		return studyTime;
	}

	private void setStudyTime(String studyTime)
	{
		this.studyTime = studyTime;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(sessionId, studyInstanceUid);
	}

	@Override
	@Transient
	public void setData(Attributes attrs) throws IOException
	{
		studyInstanceUid = attrs.getString(Tag.StudyInstanceUID);
		studyId = attrs.getString(Tag.StudyID, "*");
		Date dt = attrs.getDate(Tag.StudyDateAndTime);
		if (dt != null)
		{
			studyDate = DateUtils.formatDA(null, dt);
			studyTime = attrs.containsValue(Tag.StudyTime)
										? DateUtils.formatTM(null, dt)
										: "*";
		}
		else
		{
			studyDate = "*";
			studyTime = "*";
		}
		accessionNumber = attrs.getString(Tag.AccessionNumber, "*");
		studyDescription = attrs.getString(Tag.StudyDescription,
			"*");

		AttributeFilter filter = DicomwebDeviceConfiguration.getAttributeFilter(
			org.nrg.xnatx.dicomweb.conf.Entity.Study);
		Attributes attrsBlob = new Attributes(attrs,
			filter.getSelection());
		setAttributes(attrsBlob);
	}

	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(this)
											.add("Study[pk]=", getId())
											.add("UID=", studyInstanceUid)
											.add("Session ID=", sessionId)
											.add("Modalities=", modalitiesInStudy)
											.add("No. series=", numberOfStudyRelatedSeries)
											.add("No. instances=", numberOfStudyRelatedInstances)
						 					.add("Storage path=", storagePath)
											.toString();
	}
}
