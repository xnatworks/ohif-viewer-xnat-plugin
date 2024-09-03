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
import org.nrg.xnatx.dicomweb.conf.AttributeFilter;
import org.nrg.xnatx.dicomweb.conf.DicomwebDeviceConfiguration;
import org.nrg.xnatx.dicomweb.entity.util.QueryAttribute;
import org.nrg.xnatx.dicomweb.toolkit.DicomwebUtils;

import javax.persistence.*;
import java.io.IOException;
import java.util.Date;
import java.util.Objects;

/**
 * @author mo.alsad
 */
@Entity
@Table(
	name = "icr_dicomweb_series_data",
	uniqueConstraints = {
		@UniqueConstraint(columnNames = {"study_fk", "series_iuid"})
	},
	indexes = {
		@Index(columnList = "series_iuid"),
		@Index(columnList = "series_no"),
		@Index(columnList = "series_desc"),
		@Index(columnList = "modality"),
		@Index(columnList = "sop_cuid")
	}
)
public class DwSeries extends DwEntity
{
	private String scanId;
	private String seriesInstanceUid;
	private Integer seriesNumber;
	private String seriesDescription;
	private String modality;
	private String sopClassUid;
	private String institutionName;
	private String institutionalDepartmentName;
	private String stationName;
	private String bodyPartExamined;
	private String laterality;
	private String performedProcedureStepInstanceUid;
	private String performedProcedureStepClassUid;
	private String performedProcedureStepStartDate;
	private String performedProcedureStepStartTime;
	private DwStudy study;

	@QueryAttribute
	private int numberOfSeriesRelatedInstances;
	@QueryAttribute
	private String availableTransferSyntaxUid;
	@QueryAttribute
	private String sopClassesInSeries;

	@Column(name = "tsuid")
	public String getAvailableTransferSyntaxUid()
	{
		return availableTransferSyntaxUid;
	}

	public void setAvailableTransferSyntaxUid(
		String availableTransferSyntaxUID)
	{
		this.availableTransferSyntaxUid = availableTransferSyntaxUID;
	}

	@Column(name = "body_part")
	public String getBodyPartExamined()
	{
		return bodyPartExamined;
	}

	private void setBodyPartExamined(String bodyPartExamined)
	{
		this.bodyPartExamined = bodyPartExamined;
	}

	@Column(name = "institution")
	public String getInstitutionName()
	{
		return institutionName;
	}

	private void setInstitutionName(String institutionName)
	{
		this.institutionName = institutionName;
	}

	@Column(name = "department")
	public String getInstitutionalDepartmentName()
	{
		return institutionalDepartmentName;
	}

	private void setInstitutionalDepartmentName(
		String institutionalDepartmentName)
	{
		this.institutionalDepartmentName = institutionalDepartmentName;
	}

	@Column(name = "laterality")
	public String getLaterality()
	{
		return laterality;
	}

	private void setLaterality(String laterality)
	{
		this.laterality = laterality;
	}

	@Basic(optional = false)
	@Column(name = "modality")
	public String getModality()
	{
		return modality;
	}

	private void setModality(String modality)
	{
		this.modality = modality;
	}

	@Column(name = "num_instances")
	public int getNumberOfSeriesRelatedInstances()
	{
		return numberOfSeriesRelatedInstances;
	}

	public void setNumberOfSeriesRelatedInstances(
		int numberOfSeriesRelatedInstances)
	{
		this.numberOfSeriesRelatedInstances = numberOfSeriesRelatedInstances;
	}

	@Column(name = "pps_cuid")
	public String getPerformedProcedureStepClassUid()
	{
		return performedProcedureStepClassUid;
	}

	private void setPerformedProcedureStepClassUid(
		String performedProcedureStepClassUID)
	{
		this.performedProcedureStepClassUid = performedProcedureStepClassUID;
	}

	@Column(name = "pps_iuid")
	public String getPerformedProcedureStepInstanceUid()
	{
		return performedProcedureStepInstanceUid;
	}

	private void setPerformedProcedureStepInstanceUid(
		String performedProcedureStepInstanceUID)
	{
		this.performedProcedureStepInstanceUid = performedProcedureStepInstanceUID;
	}

	@Column(name = "pps_start_date")
	public String getPerformedProcedureStepStartDate()
	{
		return performedProcedureStepStartDate;
	}

	private void setPerformedProcedureStepStartDate(
		String performedProcedureStepStartDate)
	{
		this.performedProcedureStepStartDate = performedProcedureStepStartDate;
	}

	@Column(name = "pps_start_time")
	public String getPerformedProcedureStepStartTime()
	{
		return performedProcedureStepStartTime;
	}

	private void setPerformedProcedureStepStartTime(
		String performedProcedureStepStartTime)
	{
		this.performedProcedureStepStartTime = performedProcedureStepStartTime;
	}

	@Override
	@Transient
	public Attributes getQueryAttributes()
	{
		if (numberOfSeriesRelatedInstances == 0)
		{
			return new Attributes(0);
		}

		Attributes attrs = new Attributes(2);
		attrs.setInt(Tag.NumberOfSeriesRelatedInstances, VR.IS,
			numberOfSeriesRelatedInstances);
		attrs.setString(Tag.AvailableTransferSyntaxUID, VR.UI,
			availableTransferSyntaxUid);

		return attrs;
	}

	@Basic(optional = false)
	@Column(name = "scan_id")
	public String getScanId()
	{
		return scanId;
	}

	public void setScanId(String scanId)
	{
		this.scanId = scanId;
	}

	@Basic(optional = false)
	@Column(name = "series_desc")
	public String getSeriesDescription()
	{
		return seriesDescription;
	}

	private void setSeriesDescription(String seriesDescription)
	{
		this.seriesDescription = seriesDescription;
	}

	@Basic(optional = false)
	@Column(name = "series_iuid")
	public String getSeriesInstanceUid()
	{
		return seriesInstanceUid;
	}

	private void setSeriesInstanceUid(String seriesInstanceUID)
	{
		this.seriesInstanceUid = seriesInstanceUID;
	}

	@Column(name = "series_no")
	public Integer getSeriesNumber()
	{
		return seriesNumber;
	}

	private void setSeriesNumber(Integer seriesNumber)
	{
		this.seriesNumber = seriesNumber;
	}

	@Basic(optional = false)
	@Column(name = "sop_cuid")
	public String getSopClassUid()
	{
		return sopClassUid;
	}

	private void setSopClassUid(String sopClassUID)
	{
		this.sopClassUid = sopClassUID;
	}

	@Column(name = "cuids_in_series")
	public String getSopClassesInSeries()
	{
		return sopClassesInSeries;
	}

	public void setSopClassesInSeries(String sopClassesInSeries)
	{
		this.sopClassesInSeries = sopClassesInSeries;
	}

	public String getStationName()
	{
		return stationName;
	}

	@Column(name = "station_name")
	private void setStationName(String stationName)
	{
		this.stationName = stationName;
	}

	@ManyToOne(optional = false)
	@JoinColumn(name = "study_fk")
	public DwStudy getStudy()
	{
		return study;
	}

	public void setStudy(DwStudy study)
	{
		this.study = study;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(study, seriesInstanceUid);
	}

	@Override
	public void setData(Attributes attrs) throws IOException
	{
		seriesInstanceUid = attrs.getString(Tag.SeriesInstanceUID);
		seriesNumber = DicomwebUtils.getInt(attrs,
			Tag.SeriesNumber,
			null);
		seriesDescription = attrs.getString(Tag.SeriesDescription,
			"*");
		modality = attrs.getString(Tag.Modality, "*").toUpperCase();
		sopClassUid = attrs.getString(Tag.SOPClassUID, "*");

		institutionName = attrs.getString(Tag.InstitutionName, "*");
		institutionalDepartmentName = attrs.getString(
			Tag.InstitutionalDepartmentName, "*");
		stationName = attrs.getString(Tag.StationName, "*");

		bodyPartExamined = attrs.getString(Tag.BodyPartExamined,
			"*").toUpperCase();
		laterality = attrs.getString(Tag.Laterality,
			"*").toUpperCase();

		Attributes refPPS = attrs.getNestedDataset(
			Tag.ReferencedPerformedProcedureStepSequence);
		if (refPPS != null)
		{
			performedProcedureStepInstanceUid = refPPS.getString(
				Tag.ReferencedSOPInstanceUID, "*");
			performedProcedureStepClassUid = refPPS.getString(
				Tag.ReferencedSOPClassUID, "*");
		}
		else
		{
			performedProcedureStepInstanceUid = "*";
			performedProcedureStepClassUid = "*";
		}

		Date dt = attrs.getDate(
			Tag.PerformedProcedureStepStartDateAndTime);
		if (dt != null)
		{
			performedProcedureStepStartDate = DateUtils.formatDA(null,
				dt);
			performedProcedureStepStartTime =
				attrs.containsValue(Tag.PerformedProcedureStepStartDate)
					? DateUtils.formatTM(null, dt)
					: "*";
		}
		else
		{
			performedProcedureStepStartDate = "*";
			performedProcedureStepStartTime = "*";
		}

		AttributeFilter filter = DicomwebDeviceConfiguration.getAttributeFilter(
			org.nrg.xnatx.dicomweb.conf.Entity.Series);
		Attributes attrsBlob = new Attributes(attrs,
			filter.getSelection());
		setAttributes(attrsBlob);
	}

	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(this)
											.add("Series[pk]=", getId())
											.add("UID=", seriesInstanceUid)
											.add("Modality=", modality)
											.add("Class UID=", sopClassUid)
											.add("No. instances=", numberOfSeriesRelatedInstances)
											.add("TSUID=", availableTransferSyntaxUid)
											.toString();
	}
}
