/*********************************************************************
 * Copyright (c) 2018, Institute of Cancer Research
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
package icr.xnat.plugin.roi.entity;

import icr.etherj.StringUtils;
import jakarta.persistence.Entity;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jamesd
 */
@Entity
public class DicomSpatialData extends AbstractHibernateEntity
{
	private final static Logger logger = LoggerFactory.getLogger(
		DicomSpatialData.class);

	private int frameNumber = 1;
	private String frameOfReferenceUid = "";
	private String imageOrientationPatient = "";
	private String imagePositionPatient = "";
	private String pixelSpacing = "";
	private String seriesUid = "";
	private String sopClassUid = "";
	private String sopInstanceUid = "";
	private String studyUid = "";

	/**
	 * @return the frameNumber
	 */
	public int getFrameNumber()
	{
		return frameNumber;
	}

	/**
	 * @return the frameOfReferenceUid
	 */
	public String getFrameOfReferenceUid()
	{
		return frameOfReferenceUid;
	}

	/**
	 * @return the imageOrientationPatient
	 */
	public String getImageOrientationPatient()
	{
		return imageOrientationPatient;
	}

	/**
	 * @return the imageOrientationPatient
	 */
	public double[] fetchImageOrientationPatientAsDoubles()
	{
		return parse(imageOrientationPatient, 6);
	}

	/**
	 * @return the imagePositionPatient
	 */
	public String getImagePositionPatient()
	{
		return imagePositionPatient;
	}

	/**
	 * @return the imagePositionPatient
	 */
	public double[] fetchImagePositionPatientAsDoubles()
	{
		return parse(imagePositionPatient, 3);
	}

	/**
	 * @return the pixelSpacing
	 */
	public String getPixelSpacing()
	{
		return pixelSpacing;
	}

	/**
	 * @return the pixelSpacing
	 */
	public double[] fetchPixelSpacingAsDoubles()
	{
		return parse(pixelSpacing, 2);
	}

	/**
	 * @return the seriesUid
	 */
	public String getSeriesUid()
	{
		return seriesUid;
	}

	/**
	 * @return the sopClassUid
	 */
	public String getSopClassUid()
	{
		return sopClassUid;
	}

	/**
	 * @return the sopInstanceUid
	 */
	public String getSopInstanceUid()
	{
		return sopInstanceUid;
	}

	/**
	 * @return the studyUid
	 */
	public String getStudyUid()
	{
		return studyUid;
	}

	/**
	 * @param frameNumber the frameNumber to set
	 */
	public void setFrameNumber(int frameNumber)
	{
		this.frameNumber = frameNumber;
	}

	/**
	 * @param frameOfReferenceUid the frameOfReferenceUid to set
	 */
	public void setFrameOfReferenceUid(String frameOfReferenceUid)
	{
		this.frameOfReferenceUid = (frameOfReferenceUid != null)
			? frameOfReferenceUid : "";
	}

	/**
	 * @param imageOrientationPatient the imageOrientationPatient to set
	 */
	public void setImageOrientationPatient(String imageOrientationPatient)
	{
		double[] result = parse(imageOrientationPatient, 6);
		this.imageOrientationPatient = (result != null)
			? imageOrientationPatient : "";
	}

	/**
	 * @param imageOrientationPatient the imageOrientationPatient to set
	 */
	public void setImageOrientationPatient(double[] imageOrientationPatient)
	{
		this.imageOrientationPatient = format(imageOrientationPatient, 6);
	}

	/**
	 * @param imagePositionPatient the imagePositionPatient to set
	 */
	public void setImagePositionPatient(String imagePositionPatient)
	{
		double[] result = parse(imagePositionPatient, 3);
		this.imagePositionPatient = (result != null)
			? imagePositionPatient : "";
	}

	/**
	 * @param imagePositionPatient the imagePositionPatient to set
	 */
	public void setImagePositionPatient(double[] imagePositionPatient)
	{
		this.imagePositionPatient = format(imagePositionPatient, 3);
	}

	/**
	 * @param pixelSpacing the pixelSpacing to set
	 */
	public void setPixelSpacing(String pixelSpacing)
	{
		double[] result = parse(pixelSpacing, 2);
		this.pixelSpacing = (result != null)
			? pixelSpacing : "";
	}

	/**
	 * @param pixelSpacing the pixelSpacing to set
	 */
	public void setPixelSpacing(double[] pixelSpacing)
	{
		this.pixelSpacing = format(pixelSpacing, 2);
	}

	/**
	 * @param seriesUid the seriesUid to set
	 */
	public void setSeriesUid(String seriesUid)
	{
		this.seriesUid = (seriesUid != null) ? seriesUid : "";
	}

	/**
	 * @param sopClassUid the sopClassUid to set
	 */
	public void setSopClassUid(String sopClassUid)
	{
		this.sopClassUid = (sopClassUid != null) ? sopClassUid : "";
	}

	/**
	 * @param sopInstanceUid the sopInstanceUid to set
	 */
	public void setSopInstanceUid(String sopInstanceUid)
	{
		this.sopInstanceUid = (sopInstanceUid != null) ? sopInstanceUid : "";
	}

	/**
	 * @param studyUid the studyUid to set
	 */
	public void setStudyUid(String studyUid)
	{
		this.studyUid = (studyUid != null) ? studyUid : "";
	}

	private String format(double[] array, int nRequired)
	{
		if ((array == null) || (array.length != nRequired))
		{
			logger.warn("Array null or not {} elements", nRequired);
			return "";
		}
		String[] result = new String[nRequired];
		for (int i=0; i<nRequired; i++)
		{
			result[i] = String.valueOf(array[i]);
		}
		return StringUtils.join("\\", result);
	}

	private double[] parse(String input, int nRequired)
	{
		if ((input == null) || input.isEmpty())
		{
			return null;
		}
		String[] array = input.split("\\\\");
		if (array.length != nRequired)
		{
			return null;
		}
		double[] result = new double[nRequired];
		for (int i=0; i<nRequired; i++)
		{
			try
			{
				result[i] = Double.parseDouble(array[i]);
			}
			catch (NumberFormatException ex)
			{
				logger.warn("Not a double: {}", array[i]);
				return null;
			}
		}
		return result;
	}
}
