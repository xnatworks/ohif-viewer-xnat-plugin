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
import org.dcm4che3.util.DateUtils;
import org.dcm4che3.util.StringUtils;
import org.nrg.xnatx.dicomweb.conf.AttributeFilter;
import org.nrg.xnatx.dicomweb.conf.DicomwebDeviceConfiguration;
import org.nrg.xnatx.dicomweb.conf.privateelements.PrivateTag;
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
	name = "icr_dicomweb_instance_data",
	uniqueConstraints = {
		@UniqueConstraint(columnNames = {"series_fk", "sop_iuid"})
	},
	indexes = {
		@Index(columnList = "sop_iuid"),
		@Index(columnList = "sop_cuid"),
		@Index(columnList = "inst_no")
	}
)
public class DwInstance extends DwEntity
{
	private String sopInstanceUid;
	private String sopClassUid;
	private Integer instanceNumber;
	private String contentDate;
	private String contentTime;
	private Integer numberOfFrames;
	private String filename;
	private String transferSyntaxUid;
	private byte[] encodedMetadata;
	private DwSeries series;

	@Transient
	private Attributes cachedMetadata;
	@Transient
	private long[] dataOffsetsAndLengths;

	@Basic(optional = false)
	@Column(name = "content_date")
	public String getContentDate()
	{
		return contentDate;
	}

	private void setContentDate(String contentDate)
	{
		this.contentDate = contentDate;
	}

	@Basic(optional = false)
	@Column(name = "content_time")
	public String getContentTime()
	{
		return contentTime;
	}

	private void setContentTime(String contentTime)
	{
		this.contentTime = contentTime;
	}

	@Basic(optional = false)
	@Column(name = "inst_no")
	public Integer getInstanceNumber()
	{
		return instanceNumber;
	}

	private void setInstanceNumber(Integer instanceNumber)
	{
		this.instanceNumber = instanceNumber;
	}

	@Transient
	public Attributes getMetadata() throws IOException
	{
		if (cachedMetadata == null)
		{
			cachedMetadata = DicomwebUtils.decodeMetadata(encodedMetadata);
		}

		return cachedMetadata;
	}

	@Transient
	public void setMetadata(Attributes attrs) throws IOException
	{
		cachedMetadata = new Attributes(attrs);
		encodedMetadata = DicomwebUtils.encodeMetadata(cachedMetadata);
	}

	@Column(name = "num_frames")
	public Integer getNumberOfFrames()
	{
		return numberOfFrames;
	}

	private void setNumberOfFrames(Integer numberOfFrames)
	{
		this.numberOfFrames = numberOfFrames;
	}

	@Override
	@Transient
	public Attributes getQueryAttributes()
	{
		return new Attributes(0);
	}

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "series_fk")
	public DwSeries getSeries()
	{
		return series;
	}

	public void setSeries(DwSeries series)
	{
		this.series = series;
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

	@Basic(optional = false)
	@Column(name = "sop_iuid")
	public String getSopInstanceUid()
	{
		return sopInstanceUid;
	}

	private void setSopInstanceUid(String sopInstanceUID)
	{
		this.sopInstanceUid = sopInstanceUID;
	}

	@Column(name = "filename")
	public String getFilename()
	{
		return filename;
	}

	public void setFilename(String filename)
	{
		this.filename = filename;
	}

	@Basic(optional = false)
	@Column(name = "tsuid")
	public String getTransferSyntaxUid()
	{
		return transferSyntaxUid;
	}

	public void setTransferSyntaxUid(String transferSyntaxUID)
	{
		this.transferSyntaxUid = transferSyntaxUID;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(series, sopInstanceUid);
	}

	@Override
	public void setData(Attributes attrs) throws IOException
	{
		sopInstanceUid = attrs.getString(Tag.SOPInstanceUID);
		sopClassUid = attrs.getString(Tag.SOPClassUID);
		instanceNumber = DicomwebUtils.getInt(attrs,
			Tag.InstanceNumber, null);

		Date dt = attrs.getDate(Tag.ContentDateAndTime);
		if (dt != null)
		{
			contentDate = DateUtils.formatDA(null, dt);
			contentTime =
				attrs.containsValue(Tag.ContentTime)
					? DateUtils.formatTM(null, dt)
					: "*";
		}
		else
		{
			contentDate = "*";
			contentTime = "*";
		}

		numberOfFrames =
			attrs.contains(Tag.Rows)
				? DicomwebUtils.getInt(attrs, Tag.NumberOfFrames, "1")
				: Integer.valueOf(0);

		AttributeFilter filter = DicomwebDeviceConfiguration.getAttributeFilter(
			org.nrg.xnatx.dicomweb.conf.Entity.Instance);
		Attributes attrsBlob = new Attributes(attrs,
			filter.getSelection());
		setAttributes(attrsBlob);
	}

	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(this)
											.add("Instance[pk]=", getId())
											.add("UID=", sopInstanceUid)
											.add("Class UID=", sopClassUid)
											.add("No. frames=", numberOfFrames)
											.add("filename=", filename)
											.toString();
	}

	@Column(name = "metadata")
	private byte[] getEncodedMetadata()
	{
		return encodedMetadata;
	}

	private void setEncodedMetadata(byte[] encodedMetadata)
	{
		this.encodedMetadata = encodedMetadata;
	}
}
