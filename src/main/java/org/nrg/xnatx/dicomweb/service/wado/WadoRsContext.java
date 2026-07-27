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
package org.nrg.xnatx.dicomweb.service.wado;

import lombok.extern.slf4j.Slf4j;
import org.dcm4che3.data.UID;
import org.dcm4che3.util.ByteUtils;
import org.dcm4che3.util.SafeClose;
import org.nrg.xnatx.dicomweb.conf.DicomwebDeviceConfiguration;
import org.nrg.xnatx.dicomweb.toolkit.MediaTypeUtils;
import org.nrg.xnatx.dicomweb.toolkit.MediaTypes;
import org.nrg.xnatx.dicomweb.conf.QueryRetrieveLevel2;
import org.nrg.xnatx.dicomweb.toolkit.WebApplicationException;
import org.nrg.xnatx.dicomweb.wado.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author mo.alsad
 * <p>
 * Some parts of code are refactored from dcm4chee. Refer to DCM4CHEE_LICENSE.
 */
@Slf4j
public class WadoRsContext
{
	private final WadoRsTarget target;
	private final Map<String,String> xnatIds;
	private final String studyInstanceUid;
	private final String seriesInstanceUid;
	private final String sopInstanceUid;
	private final int[] attributePath;
	private final QueryRetrieveLevel2 queryRetrieveLevel;
	private final boolean isMetadataQuery;
	private final List<InstanceInfo> matches = new ArrayList<>();
	private final Map<String,MediaType> selectedMediaTypes = new HashMap<>();
	// Output streams
	CompressedMFPixelDataOutput compressedMFPixelDataOutput;
	UncompressedFramesOutput uncompressedFramesOutput;
	CompressedFramesOutput compressedFramesOutput;
	DecompressFramesOutput decompressFramesOutput;
	private boolean includePrivateAttributes;
	private StringBuffer requestUrl;
	private boolean isPartialContent = false;
	//
	private List<MediaType> acceptableMediaTypes;
	private List<MediaType> acceptableMultipartRelatedMediaTypes;
	private Collection<String> acceptableTransferSyntaxes;
	private Collection<String> acceptableZipTransferSyntaxes;
	private String dicomRootPartTransferSyntax;
	// Spool directory
	private Path spoolDirectory;
	//
	private int[] frameList;

	public WadoRsContext(WadoRsTarget target, Map<String,String> xnatIds,
		String studyInstanceUid, String seriesInstanceUid,
		String sopInstanceUid, int[] frameList, int[] attributePath)
	{
		this.target = target;
		this.xnatIds = xnatIds;
		this.studyInstanceUid = studyInstanceUid;
		this.seriesInstanceUid = seriesInstanceUid;
		this.sopInstanceUid = sopInstanceUid;
		this.frameList = frameList;
		this.attributePath = attributePath;

		queryRetrieveLevel = target.qrLevel;
		isMetadataQuery = target.isMetadata;
	}

	public static MediaType selectMediaType(List<MediaType> accepted,
		MediaType... provided)
	{
		// We are returning the image buffer as is,
		// so there is no decoding or encoding being processed.
		if (provided.length > 0) {
			return provided[0];
		}

		for (MediaType acceptedMediaType : accepted)
		{
			for (MediaType mediaType : provided)
			{
				if (mediaType.isCompatibleWith(acceptedMediaType))
				{
					return mediaType;
				}
			}
		}
		return null;
	}

	public void adjustFrameList(int numFrames)
	{
		int len = 0;
		for (int frame : frameList)
		{
			if (frame <= numFrames)
			{
				frameList[len++] = frame;
			}
		}

		if (len == frameList.length)
		{
			return;
		}

		if (len == 0)
		{
			frameList = ByteUtils.EMPTY_INTS;
			return;
		}

		isPartialContent = true;
		frameList = Arrays.copyOf(frameList, len);
	}

	WadoRsOutput bulkdataFrame()
	{
		checkMultipartRelatedAcceptable();
		return WadoRsOutput.BULKDATA_FRAME;
	}

	WadoRsOutput bulkdataPath()
	{
		checkMultipartRelatedAcceptable();
		return WadoRsOutput.BULKDATA_PATH;
	}

	public void closeOutputStreams()
	{
		SafeClose.close(compressedMFPixelDataOutput);
		SafeClose.close(uncompressedFramesOutput);
		SafeClose.close(compressedFramesOutput);
		SafeClose.close(decompressFramesOutput);
	}

	WadoRsOutput dicomOrBulkdataOrZip()
	{
		if (acceptableMultipartRelatedMediaTypes.isEmpty()
					&& acceptableZipTransferSyntaxes.isEmpty())
		{
			throw new WebApplicationException(
				"Request is missing acceptable Media type(s)",
				HttpStatus.NOT_ACCEPTABLE);
		}
		if (selectMediaType(acceptableMultipartRelatedMediaTypes,
			MediaTypes.APPLICATION_DICOM) != null)
		{
			return WadoRsOutput.DICOM;
		}
		if (!acceptableZipTransferSyntaxes.isEmpty())
		{
			return WadoRsOutput.ZIP;
		}
		return WadoRsOutput.BULKDATA;
	}

	public List<MediaType> getAcceptableMediaTypes()
	{
		return acceptableMediaTypes;
	}

	public List<MediaType> getAcceptableMultipartRelatedMediaTypes()
	{
		return acceptableMultipartRelatedMediaTypes;
	}

	public Collection<String> getAcceptableTransferSyntaxes()
	{
		return acceptableTransferSyntaxes;
	}

	public Collection<String> getAcceptableZipTransferSyntaxes()
	{
		return acceptableZipTransferSyntaxes;
	}

	public int[] getAttributePath()
	{
		return attributePath;
	}

	public String getDicomRootPartTransferSyntax()
	{
		return dicomRootPartTransferSyntax;
	}

	public void setDicomRootPartTransferSyntax(
		String dicomRootPartTransferSyntax)
	{
		this.dicomRootPartTransferSyntax = dicomRootPartTransferSyntax;
	}

	public int[] getFrameList()
	{
		return frameList;
	}

	public List<InstanceInfo> getMatches()
	{
		return matches;
	}

	public QueryRetrieveLevel2 getQueryRetrieveLevel()
	{
		return queryRetrieveLevel;
	}

	public StringBuffer getRequestUrl()
	{
		return requestUrl;
	}

	public void setRequestUrl(StringBuffer requestUrl)
	{
		this.requestUrl = requestUrl;
	}

	public Map<String,MediaType> getSelectedMediaTypes()
	{
		return selectedMediaTypes;
	}

	public String getSeriesInstanceUid()
	{
		return seriesInstanceUid;
	}

	public String getSopInstanceUid()
	{
		return sopInstanceUid;
	}

	public Path getSpoolDirectory(int[] frameList) throws IOException
	{
		if (spoolDirectory != null)
		{
			return spoolDirectory;
		}
		for (int i = 1; i < frameList.length; i++)
		{
			if (frameList[i - 1] > frameList[i])
			{
				return (spoolDirectory = Files.createTempDirectory(
					DicomwebDeviceConfiguration.getSpoolDirectoryRoot(), null));
			}
		}
		return null;
	}

	public Path getSpoolDirectory() throws IOException
	{
		if (spoolDirectory != null)
		{
			return spoolDirectory;
		}
		return (spoolDirectory = Files.createTempDirectory(
			DicomwebDeviceConfiguration.getSpoolDirectoryRoot(), null));
	}

	public String getStudyInstanceUid()
	{
		return studyInstanceUid;
	}

	public WadoRsTarget getTarget()
	{
		return target;
	}

	public Map<String,String> getXnatIds()
	{
		return xnatIds;
	}

	public void initAcceptableMediaTypes(final HttpServletRequest request,
		final MultiValueMap<String,String> httpQueryParams)
	{
		List<String> headers = Collections.list(request.getHeaders("accept"));
		List<String> accept = httpQueryParams.get("accept");

		acceptableMediaTypes =
			MediaTypeUtils.acceptableMediaTypesOf(headers, accept);
		acceptableMultipartRelatedMediaTypes =
			acceptableMediaTypes.stream()
													.map(MediaTypes::getMultiPartRelatedType)
													.filter(Objects::nonNull)
													.collect(Collectors.toList());
		acceptableTransferSyntaxes =
			transferSyntaxesOf(acceptableMultipartRelatedMediaTypes
													 .stream()
													 .filter(
														 m -> m.isCompatibleWith(
															 MediaTypes.APPLICATION_DICOM)));
		acceptableZipTransferSyntaxes =
			transferSyntaxesOf(acceptableMediaTypes
													 .stream()
													 .filter(m -> m.isCompatibleWith(
														 MediaTypes.APPLICATION_ZIP)));
	}

	public boolean isIncludePrivateAttributes()
	{
		return includePrivateAttributes;
	}

	public void setIncludePrivateAttributes(boolean includePrivateAttributes)
	{
		this.includePrivateAttributes = includePrivateAttributes;
	}

	public boolean isMetadataQuery()
	{
		return isMetadataQuery;
	}

	public boolean isPartialContent()
	{
		return isPartialContent;
	}

	public void setPartialContent(boolean partialContent)
	{
		isPartialContent = partialContent;
	}

	WadoRsOutput metadataJSONorXML()
	{
		// ToDo: Support XML medatada output

		MediaType mediaType = selectMediaType(acceptableMediaTypes,
			MediaTypes.APPLICATION_DICOM_JSON,
			MediaType.APPLICATION_JSON);

		if (mediaType == null)
		{
			throw new WebApplicationException(
				"Media type in request is not acceptable",
				HttpStatus.NOT_ACCEPTABLE);
		}

		return WadoRsOutput.METADATA_JSON;
	}

	public void purgeSpoolDirectory()
	{
		if (spoolDirectory == null)
		{
			return;
		}
		try
		{
			try (DirectoryStream<Path> dir = Files.newDirectoryStream(spoolDirectory))
			{
				for (java.nio.file.Path file : dir)
				{
					try
					{
						Files.delete(file);
					}
					catch (IOException e)
					{
						log.warn("Failed to delete frame spool file {}", file, e);
					}
				}
			}
			Files.delete(spoolDirectory);
		}
		catch (IOException e)
		{
			log.warn("Failed to purge spool directory {}", spoolDirectory, e);
		}
	}

	private void checkMultipartRelatedAcceptable()
	{
		if (acceptableMultipartRelatedMediaTypes.isEmpty())
		{
			throw new WebApplicationException(
				"Request is missing Multipart Related Media type",
				HttpStatus.NOT_ACCEPTABLE);
		}
	}

	private List<String> transferSyntaxesOf(Stream<MediaType> mediaTypeStream)
	{
		List<String> list = mediaTypeStream
													.map(m -> m.isWildcardType()
														? "*"
														: m.getParameters()
															 .getOrDefault("transfer-syntax", ""))
													.collect(Collectors.toList());
		if (list.remove(""))
		{
			list.add(UID.ExplicitVRLittleEndian);
			list.add(UID.MPEG2MPML);
			list.add(UID.MPEG2MPHL);
			list.add(UID.MPEG4HP41);
			list.add(UID.MPEG4HP41BD);
			list.add(UID.MPEG4HP422D);
			list.add(UID.MPEG4HP41BD);
			list.add(UID.MPEG4HP423D);
			list.add(UID.MPEG4HP42STEREO);
			list.add(UID.HEVCMP51);
			list.add(UID.HEVCM10P51);
		}
		return list;
	}
}
