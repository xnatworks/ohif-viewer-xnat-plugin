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
package org.nrg.xnatx.dicomweb.wado;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;

import java.nio.file.Path;

/**
 * @author m.alsad
 */
public class InstanceInfo
{
	private final Long studyPk;
	private final Long seriesPk;
	private final Long instancePk;
	private final String filename;
	private final Attributes metadata;
	private final String sopClassUID;
	private final String sopInstanceUID;
	private final String transferSyntaxUID;
	private final boolean isMultiframe;
	private final boolean isImage;
	private final boolean isVideo;
	private Path storagePath;

	public InstanceInfo(Long studyPk, Long seriesPk, Long instancePk,
		String filename, Attributes metadata)
	{
		this.studyPk = studyPk;
		this.seriesPk = seriesPk;
		this.instancePk = instancePk;
		this.filename = filename;
		this.metadata = metadata;

		this.sopClassUID = metadata.getString(Tag.SOPClassUID);
		this.sopInstanceUID = metadata.getString(Tag.SOPInstanceUID);
		String transferSyntaxUID = metadata.getString(Tag.TransferSyntaxUID);
		this.transferSyntaxUID = transferSyntaxUID;

		this.isMultiframe = metadata.getInt(Tag.NumberOfFrames, 1) > 1;

		this.isImage = metadata.contains(Tag.BitsAllocated) && !sopClassUID.equals(
			UID.RTDoseStorage);
		switch (transferSyntaxUID)
		{
			case UID.MPEG2MPML:
			case UID.MPEG2MPHL:
			case UID.MPEG4HP41:
			case UID.MPEG4HP41BD:
			case UID.MPEG4HP422D:
			case UID.MPEG4HP423D:
			case UID.MPEG4HP42STEREO:
			case UID.HEVCMP51:
			case UID.HEVCM10P51:
				isVideo = true;
				break;
			default:
				isVideo = false;
		}
	}

	public String getFilename()
	{
		return filename;
	}

	public Long getInstancePk()
	{
		return instancePk;
	}

	public Attributes getMetadata()
	{
		return metadata;
	}

	public Long getSeriesPk()
	{
		return seriesPk;
	}

	public String getSopClassUID()
	{
		return sopClassUID;
	}

	public String getSopInstanceUID()
	{
		return sopInstanceUID;
	}

	public Path getStoragePath()
	{
		return storagePath;
	}

	public void setStoragePath(Path storagePath)
	{
		this.storagePath = storagePath;
	}

	public Long getStudyPk()
	{
		return studyPk;
	}

	public String getTransferSyntaxUID()
	{
		return transferSyntaxUID;
	}

	public boolean isImage()
	{
		return isImage;
	}

	public boolean isMultiframe()
	{
		return isMultiframe;
	}

	public boolean isVideo()
	{
		return isVideo;
	}

	@Override
	public String toString()
	{
		return "Instance[iuid=" + sopInstanceUID + ",cuid=" + sopClassUID + "]";
	}
}
