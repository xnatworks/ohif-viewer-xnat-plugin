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

import org.dcm4che3.data.UID;
import org.nrg.xnatx.dicomweb.toolkit.MediaTypes;
import org.nrg.xnatx.dicomweb.wado.InstanceInfo;
import org.springframework.http.MediaType;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author mo.alsad
 * <p>
 * Some parts of code are refactored from dcm4chee. Refer to DCM4CHEE_LICENSE.
 */
enum ObjectType
{
	UncompressedSingleFrameImage(MediaTypes.IMAGE_JPEG, true, false)
		{
			@Override
			public Optional<MediaType> getCompatibleMimeType(MediaType other)
			{
				return findCompatibleSingleFrameMimeType(other);
			}

			@Override
			public MediaType[] getRenderedContentTypes()
			{
				return ObjectType.renderedSingleFrameMediaTypes();
			}

			@Override
			public MediaType[] getBulkdataContentTypes(InstanceInfo inst)
			{
				return octetStreamMediaType();
			}
		},
	CompressedSingleFrameImage(MediaTypes.IMAGE_JPEG, true, false)
		{
			@Override
			public Optional<MediaType> getCompatibleMimeType(MediaType other)
			{
				return findCompatibleSingleFrameMimeType(other);
			}

			@Override
			public MediaType[] getRenderedContentTypes()
			{
				return renderedSingleFrameMediaTypes();
			}

			@Override
			public MediaType[] getBulkdataContentTypes(InstanceInfo inst)
			{
				return calcPixelDataContentTypes(inst);
			}
		},
	UncompressedMultiFrameImage(MediaTypes.APPLICATION_DICOM, true, false)
		{
			@Override
			public Optional<MediaType> getCompatibleMimeType(MediaType other)
			{
				return findCompatibleMultiFrameMimeType(other);
			}

			@Override
			public MediaType[] getRenderedContentTypes()
			{
				return renderedMultiFrameMediaTypes();
			}

			@Override
			public MediaType[] getBulkdataContentTypes(InstanceInfo inst)
			{
				return octetStreamMediaType();
			}
		},
	CompressedMultiFrameImage(MediaTypes.APPLICATION_DICOM, true, false)
		{
			@Override
			public Optional<MediaType> getCompatibleMimeType(MediaType other)
			{
				return findCompatibleMultiFrameMimeType(other);
			}

			@Override
			public MediaType[] getRenderedContentTypes()
			{
				return renderedMultiFrameMediaTypes();
			}

			@Override
			public MediaType[] getBulkdataContentTypes(InstanceInfo inst)
			{
				return calcPixelDataContentTypes(inst);
			}
		},
	MPEG2Video(MediaTypes.VIDEO_MPEG, false, true),
	MPEG4Video(MediaTypes.VIDEO_MP4, false, true),
	SRDocument(MediaType.TEXT_HTML, false, false)
		{
			@Override
			public Optional<MediaType> getCompatibleMimeType(MediaType other)
			{
				return findCompatibleSRMimeType(other);
			}

			@Override
			public MediaType[] getRenderedContentTypes()
			{
				return renderedSRMediaTypes();
			}

			@Override
			public MediaType[] getBulkdataContentTypes(InstanceInfo inst)
			{
				return null;
			}
		},
	EncapsulatedPDF(MediaTypes.APPLICATION_PDF, false, false),
	EncapsulatedCDA(MediaType.TEXT_XML, false, false)
		{
			@Override
			public MediaType[] getRenderedContentTypes()
			{
				return null;
			}
		},
	EncapsulatedSTL(MediaTypes.MODEL_STL, false, false)
		{
			@Override
			public MediaType[] getRenderedContentTypes()
			{
				return null;
			}
		},
	EncapsulatedOBJ(MediaTypes.MODEL_OBJ, false, false)
		{
			@Override
			public MediaType[] getRenderedContentTypes()
			{
				return null;
			}
		},
	EncapsulatedMTL(MediaTypes.MODEL_MTL, false, false)
		{
			@Override
			public MediaType[] getRenderedContentTypes()
			{
				return null;
			}
		},
	EncapsulatedGenozip(MediaTypes.APPLICATION_VND_GENOZIP, false, false)
		{
			@Override
			public MediaType[] getRenderedContentTypes()
			{
				return null;
			}
		},
	Other(MediaTypes.APPLICATION_DICOM, false, false)
		{
			@Override
			public MediaType[] getRenderedContentTypes()
			{
				return null;
			}

			@Override
			public MediaType[] getBulkdataContentTypes(InstanceInfo inst)
			{
				return null;
			}
		};

	private final MediaType defaultMimeType;
	private final boolean image;
	private final boolean video;

	ObjectType(MediaType defaultMimeType, boolean image, boolean video)
	{
		this.defaultMimeType = defaultMimeType;
		this.image = image;
		this.video = video;
	}

	public static ObjectType objectTypeOf(WadoRsContext ctx, InstanceInfo inst,
		int frame)
	{
		if (inst.isImage())
		{
			switch (inst.getTransferSyntaxUID())
			{
				case UID.MPEG2MPML:
				case UID.MPEG2MPHL:
					return MPEG2Video;
				case UID.MPEG4HP41:
				case UID.MPEG4HP41BD:
				case UID.MPEG4HP422D:
				case UID.MPEG4HP423D:
				case UID.MPEG4HP42STEREO:
				case UID.HEVCMP51:
				case UID.HEVCM10P51:
					return MPEG4Video;
				case UID.ImplicitVRLittleEndian:
				case UID.ExplicitVRLittleEndian:
					return frame <= 0 && inst.isMultiframe()
									 ? UncompressedMultiFrameImage
									 : UncompressedSingleFrameImage;
				default:
					return frame <= 0 && inst.isMultiframe()
									 ? CompressedMultiFrameImage
									 : CompressedSingleFrameImage;
			}
		}
		switch (inst.getSopClassUID())
		{
			case UID.EncapsulatedPDFStorage:
				return EncapsulatedPDF;
			case UID.EncapsulatedCDAStorage:
				return EncapsulatedCDA;
			case UID.EncapsulatedSTLStorage:
				return EncapsulatedSTL;
			case UID.EncapsulatedMTLStorage:
				return EncapsulatedMTL;
			case UID.EncapsulatedOBJStorage:
				return EncapsulatedOBJ;
			case UID.PrivateDcm4cheEncapsulatedGenozipStorage:
				return EncapsulatedGenozip;
		}

		return Other; // ToDo: support SRDocument
	}

	private static MediaType[] calcPixelDataContentTypes(InstanceInfo inst)
	{
		String tsuid = inst.getTransferSyntaxUID();
		MediaType mediaType = MediaTypes.forTransferSyntax(tsuid);
		return new MediaType[]{mediaType, MediaType.APPLICATION_OCTET_STREAM};
	}

	private static Optional<MediaType> findCompatibleMimeType(MediaType other,
		MediaType... mimeTypes)
	{
		return Stream.of(mimeTypes).filter(other::isCompatibleWith).findFirst();
	}

	private static Optional<MediaType> findCompatibleMultiFrameMimeType(
		MediaType other)
	{
		return findCompatibleMimeType(other,
			MediaTypes.APPLICATION_DICOM,
			MediaTypes.IMAGE_GIF);
	}

	private static Optional<MediaType> findCompatibleSRMimeType(MediaType other)
	{
		return findCompatibleMimeType(other,
			MediaType.TEXT_HTML,
			MediaType.TEXT_PLAIN,
			MediaTypes.APPLICATION_DICOM);
	}

	private static Optional<MediaType> findCompatibleSingleFrameMimeType(
		MediaType other)
	{
		return findCompatibleMimeType(other,
			MediaTypes.IMAGE_JPEG,
			MediaTypes.APPLICATION_DICOM,
			MediaTypes.IMAGE_GIF,
			MediaTypes.IMAGE_PNG);
	}

	private static MediaType[] octetStreamMediaType()
	{
		return new MediaType[]{MediaType.APPLICATION_OCTET_STREAM};
	}

	private static MediaType[] renderedMultiFrameMediaTypes()
	{
		return new MediaType[]{MediaTypes.IMAGE_GIF};
	}

	private static MediaType[] renderedSRMediaTypes()
	{
		return new MediaType[]{MediaType.TEXT_HTML, MediaType.TEXT_PLAIN};
	}

	private static MediaType[] renderedSingleFrameMediaTypes()
	{
		return new MediaType[]{MediaTypes.IMAGE_JPEG, MediaTypes.IMAGE_GIF, MediaTypes.IMAGE_PNG};
	}

	public MediaType[] getBulkdataContentTypes(InstanceInfo inst)
	{
		return new MediaType[]{defaultMimeType};
	}

	public Optional<MediaType> getCompatibleMimeType(MediaType other)
	{
		return findCompatibleMimeType(other, defaultMimeType,
			MediaTypes.APPLICATION_DICOM);
	}

	public MediaType getDefaultMimeType()
	{
		return defaultMimeType;
	}

	public MediaType[] getRenderedContentTypes()
	{
		return new MediaType[]{defaultMimeType};
	}

	public boolean isImage()
	{
		return image;
	}

	public boolean isVideo()
	{
		return video;
	}
}
