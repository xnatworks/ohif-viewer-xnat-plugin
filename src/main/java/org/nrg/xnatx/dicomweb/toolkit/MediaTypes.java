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
package org.nrg.xnatx.dicomweb.toolkit;

import org.dcm4che3.data.UID;

import org.springframework.http.MediaType;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author mo.alsad
 *
 * Some parts of code are refactored from dcm4chee. Refer to DCM4CHEE_LICENSE.
 */
@SuppressWarnings("unused")
public class MediaTypes
{
	/**
	 * "application/dicom"
	 */
	public final static String APPLICATION_DICOM_VALUE = "application/dicom";

	/**
	 * "application/dicom"
	 */
	public final static MediaType APPLICATION_DICOM = MediaType.valueOf(
		APPLICATION_DICOM_VALUE);

	/**
	 * "application/dicom+xml"
	 */
	public final static String APPLICATION_DICOM_XML_VALUE = "application/dicom+xml";

	/**
	 * "application/dicom+xml"
	 */
	public final static MediaType APPLICATION_DICOM_XML = MediaType.valueOf(
		APPLICATION_DICOM_XML_VALUE);

	/**
	 * "application/dicom+json"
	 */
	public final static String APPLICATION_DICOM_JSON_VALUE = "application/dicom+json";

	/**
	 * "application/dicom+json"
	 */
	public final static MediaType APPLICATION_DICOM_JSON = MediaType.valueOf(
		APPLICATION_DICOM_JSON_VALUE);

	/**
	 * "image/*"
	 */
	public final static String IMAGE_WILDCARD_VALUE = "image/*";

	/**
	 * "image/*"
	 */
	public final static MediaType IMAGE_WILDCARD = MediaType.valueOf(
		IMAGE_WILDCARD_VALUE);

	/**
	 * "image/gif"
	 */
	public final static String IMAGE_GIF_VALUE = "image/gif";

	/**
	 * "image/gif"
	 */
	public final static MediaType IMAGE_GIF = MediaType.valueOf(IMAGE_GIF_VALUE);


	/**
	 * "image/png"
	 */
	public final static String IMAGE_PNG_VALUE = "image/png";

	/**
	 * "image/png"
	 */
	public final static MediaType IMAGE_PNG = MediaType.valueOf(IMAGE_PNG_VALUE);

	/**
	 * "image/jpeg"
	 */
	public final static String IMAGE_JPEG_VALUE = "image/jpeg";

	/**
	 * "image/jpeg"
	 */
	public final static MediaType IMAGE_JPEG = MediaType.valueOf(
		IMAGE_JPEG_VALUE);

	/**
	 * "image/jls"
	 */
	public final static String IMAGE_JLS_VALUE = "image/jls";

	/**
	 * "image/jls"
	 */
	public final static MediaType IMAGE_JLS = MediaType.valueOf(IMAGE_JLS_VALUE);

	/**
	 * "image/jp2"
	 */
	public final static String IMAGE_JP2_VALUE = "image/jp2";

	/**
	 * "image/jp2"
	 */
	public final static MediaType IMAGE_JP2 = MediaType.valueOf(IMAGE_JP2_VALUE);

	/**
	 * "image/jpx"
	 */
	public final static String IMAGE_JPX_VALUE = "image/jpx";

	/**
	 * "image/dicom+jpeg-jpx"
	 */
	public final static MediaType IMAGE_JPX = MediaType.valueOf(IMAGE_JPX_VALUE);

	/**
	 * "image/dicom-rle"
	 */
	public final static String IMAGE_DICOM_RLE_VALUE = "image/dicom-rle";

	/**
	 * "image/dicom-rle"
	 */
	public final static MediaType IMAGE_DICOM_RLE = MediaType.valueOf(
		IMAGE_DICOM_RLE_VALUE);

	/**
	 * "video/*"
	 */
	public final static String VIDEO_WILDCARD_VALUE = "video/*";

	/**
	 * "video/*"
	 */
	public final static MediaType VIDEO_WILDCARD = MediaType.valueOf(
		VIDEO_WILDCARD_VALUE);

	/**
	 * "video/mpeg"
	 */
	public final static String VIDEO_MPEG_VALUE = "video/mpeg";

	/**
	 * "video/mpeg"
	 */
	public final static MediaType VIDEO_MPEG = MediaType.valueOf(
		VIDEO_MPEG_VALUE);
	/**
	 * "video/mp4"
	 */
	public final static String VIDEO_MP4_VALUE = "video/mp4";

	/**
	 * "video/mp4"
	 */
	public final static MediaType VIDEO_MP4 = MediaType.valueOf(VIDEO_MP4_VALUE);

	/**
	 * "video/quicktime"
	 */
	public final static String VIDEO_QUICKTIME_VALUE = "video/quicktime";

	/**
	 * "video/quicktime"
	 */
	public final static MediaType VIDEO_QUICKTIME = MediaType.valueOf(
		VIDEO_QUICKTIME_VALUE);

	/**
	 * "application/pdf"
	 */
	public final static String APPLICATION_PDF_VALUE = "application/pdf";

	/**
	 * "application/pdf"
	 */
	public final static MediaType APPLICATION_PDF = MediaType.valueOf(
		APPLICATION_PDF_VALUE);

	/**
	 * "text/rtf"
	 */
	public final static String TEXT_RTF_VALUE = "text/rtf";

	/**
	 * "text/rtf"
	 */
	public final static MediaType TEXT_RTF = MediaType.valueOf(TEXT_RTF_VALUE);

	/**
	 * "text/csv"
	 */
	public final static String TEXT_CSV_VALUE = "text/csv";

	/**
	 * "text/csv"
	 */
	public final static MediaType TEXT_CSV = MediaType.valueOf(TEXT_CSV_VALUE);

	/**
	 * "text/csv;charset=utf-8"
	 */
	public final static String TEXT_CSV_UTF8_VALUE = "text/csv;charset=utf-8";

	/**
	 * "text/csv;charset=utf-8"
	 */
	public final static MediaType TEXT_CSV_UTF8 = MediaType.valueOf(
		TEXT_CSV_UTF8_VALUE);

	/**
	 * "application/zip"
	 */
	public final static String APPLICATION_ZIP_VALUE = "application/zip";

	/**
	 * "application/zip"
	 */
	public final static MediaType APPLICATION_ZIP = MediaType.valueOf(
		APPLICATION_ZIP_VALUE);
	/**
	 * "multipart/related"
	 */
	public final static String MULTIPART_RELATED_VALUE = "multipart/related";

	/**
	 * "multipart/related"
	 */
	public final static MediaType MULTIPART_RELATED = MediaType.valueOf(
		MULTIPART_RELATED_VALUE);

	/**
	 * "multipart/related;type=\"application/dicom\""
	 */
	public final static String MULTIPART_RELATED_APPLICATION_DICOM_VALUE =
		"multipart/related;type=\"application/dicom\"";

	/**
	 * "multipart/related;type=\"application/dicom\""
	 */
	public final static MediaType MULTIPART_RELATED_APPLICATION_DICOM =
		MediaType.valueOf(MULTIPART_RELATED_APPLICATION_DICOM_VALUE);

	/**
	 * "multipart/related;type=\"application/dicom+xml\""
	 */
	public final static String MULTIPART_RELATED_APPLICATION_DICOM_XML_VALUE =
		"multipart/related;type=\"application/dicom+xml\"";

	/**
	 * "multipart/related;type=\"application/dicom+xml\""
	 */
	public final static MediaType MULTIPART_RELATED_APPLICATION_DICOM_XML =
		MediaType.valueOf(MULTIPART_RELATED_APPLICATION_DICOM_XML_VALUE);

	/**
	 * "multipart/related;type=\"application/dicom\""
	 */
	public final static String MULTIPART_RELATED_APPLICATION_OCTET_STREAM_VALUE =
		"multipart/related;type=\"application/octet-stream\"";

	/**
	 * "model/stl"
	 */
	public final static String MODEL_STL_VALUE = "model/stl";

	/**
	 * "model/stl"
	 */
	public final static MediaType MODEL_STL = MediaType.valueOf(MODEL_STL_VALUE);
	/**
	 * "model/x.stl-binary"
	 */
	public final static String MODEL_X_STL_BINARY_VALUE = "model/x.stl-binary";

	/**
	 * "model/x.stl-binary"
	 */
	public final static MediaType MODEL_X_STL_BINARY = MediaType.valueOf(
		MODEL_X_STL_BINARY_VALUE);
	/**
	 * "application/sla"
	 */
	public final static String APPLICATION_SLA_VALUE = "application/sla";

	/**
	 * "application/sla"
	 */
	public final static MediaType APPLICATION_SLA = MediaType.valueOf(
		APPLICATION_SLA_VALUE);

	/**
	 * "model/obj"
	 */
	public final static String MODEL_OBJ_VALUE = "model/obj";

	/**
	 * "model/obj"
	 */
	public final static MediaType MODEL_OBJ = MediaType.valueOf(MODEL_OBJ_VALUE);

	/**
	 * "model/mtl"
	 */
	public final static String MODEL_MTL_VALUE = "model/mtl";

	/**
	 * "model/mtl"
	 */
	public final static MediaType MODEL_MTL = MediaType.valueOf(MODEL_MTL_VALUE);

	/**
	 * "application/vnd.genozip"
	 */
	public final static String APPLICATION_VND_GENOZIP_VALUE = "application/vnd.genozip";

	/**
	 * "application/vnd.genozip"
	 */
	public final static MediaType APPLICATION_VND_GENOZIP = MediaType.valueOf(
		APPLICATION_VND_GENOZIP_VALUE);

	public static MediaType applicationDicomWithTransferSyntax(String tsuid)
	{
		return new MediaType("application", "dicom",
			Collections.singletonMap("transfer-syntax", tsuid));
	}

	public static boolean equalsIgnoreParameters(MediaType type1, MediaType type2)
	{
		return type1.getType().equalsIgnoreCase(type2.getType())
						 && type1.getSubtype().equalsIgnoreCase(type2.getSubtype());
	}

	public static MediaType forTransferSyntax(String ts)
	{
		MediaType type;
		switch (ts)
		{
			case UID.ExplicitVRLittleEndian:
			case UID.ImplicitVRLittleEndian:
				return MediaType.APPLICATION_OCTET_STREAM;
			case UID.JPEGLosslessSV1:
				return IMAGE_JPEG;
			case UID.JPEGLSLossless:
				return IMAGE_JLS;
			case UID.JPEG2000Lossless:
				return IMAGE_JP2;
			case UID.JPEG2000MCLossless:
				return IMAGE_JPX;
			case UID.RLELossless:
				return IMAGE_DICOM_RLE;
			case UID.JPEGBaseline8Bit:
			case UID.JPEGExtended12Bit:
			case UID.JPEGLossless:
				type = IMAGE_JPEG;
				break;
			case UID.JPEGLSNearLossless:
				type = IMAGE_JLS;
				break;
			case UID.JPEG2000:
				type = IMAGE_JP2;
				break;
			case UID.JPEG2000MC:
				type = IMAGE_JPX;
				break;
			case UID.MPEG2MPML:
			case UID.MPEG2MPHL:
				type = VIDEO_MPEG;
				break;
			case UID.MPEG4HP41:
			case UID.MPEG4HP41BD:
				type = VIDEO_MP4;
				break;
			default:
				throw new IllegalArgumentException("ts: " + ts);
		}
		return new MediaType(type.getType(), type.getSubtype(),
			Collections.singletonMap("transfer-syntax", ts));
	}

	public static MediaType getMultiPartRelatedType(MediaType mediaType)
	{
		if (!MediaTypes.MULTIPART_RELATED.isCompatibleWith(mediaType))
		{
			return null;
		}

		String type = mediaType.getParameters().get("type");
		if (type == null)
		{
			return MediaType.ALL;
		}

		// Remove leading and training double quotes from type
		type = type.replaceAll("^\"|\"$", "");
		MediaType partType = MediaType.valueOf(type);
		if (mediaType.getParameters().size() > 1)
		{
			Map<String,String> params = new HashMap<>(mediaType.getParameters());
			params.remove("type");
			partType = new MediaType(partType.getType(), partType.getSubtype(),
				params);
		}
		return partType;
	}

	public static String getTransferSyntax(MediaType type)
	{
		return type != null && equalsIgnoreParameters(APPLICATION_DICOM, type)
						 ? type.getParameters().get("transfer-syntax")
						 : null;
	}

	public static boolean isSTLType(MediaType mediaType)
	{
		return equalsIgnoreParameters(mediaType, MODEL_STL)
						 || equalsIgnoreParameters(mediaType, MODEL_X_STL_BINARY)
						 || equalsIgnoreParameters(mediaType, APPLICATION_SLA);
	}

	public static boolean isSTLType(String type)
	{
		return MODEL_STL_VALUE.equalsIgnoreCase(type)
						 || MODEL_X_STL_BINARY_VALUE.equalsIgnoreCase(type)
						 || APPLICATION_SLA_VALUE.equalsIgnoreCase(type);
	}

	public static String sopClassOf(MediaType bulkdataMediaType)
	{
		String type = bulkdataMediaType.getType().toLowerCase();
		return type.equals("image") ? UID.SecondaryCaptureImageStorage
						 : type.equals("video") ? UID.VideoPhotographicImageStorage
								 : equalsIgnoreParameters(bulkdataMediaType,
			APPLICATION_PDF) ? UID.EncapsulatedPDFStorage
										 : equalsIgnoreParameters(bulkdataMediaType,
			MediaType.APPLICATION_XML) ? UID.EncapsulatedCDAStorage
												 : isSTLType(
			bulkdataMediaType) ? UID.EncapsulatedSTLStorage
														 : equalsIgnoreParameters(bulkdataMediaType,
			MODEL_OBJ) ? UID.EncapsulatedOBJStorage
																 : equalsIgnoreParameters(bulkdataMediaType,
			MODEL_MTL) ? UID.EncapsulatedMTLStorage
																		 : null;
	}

	public static String transferSyntaxOf(MediaType bulkdataMediaType)
	{
		String tsuid = bulkdataMediaType.getParameters().get("transfer-syntax");
		if (tsuid != null)
		{
			return tsuid;
		}

		String type = bulkdataMediaType.getType().toLowerCase();
		String subtype = bulkdataMediaType.getSubtype().toLowerCase();
		switch (type)
		{
			case "image":
				switch (subtype)
				{
					case "jpeg":
						return UID.JPEGLosslessSV1;
					case "jls":
					case "x-jls":
						return UID.JPEGLSLossless;
					case "jp2":
						return UID.JPEG2000Lossless;
					case "jpx":
						return UID.JPEG2000MCLossless;
					case "x-dicom-rle":
					case "dicom-rle":
						return UID.RLELossless;
				}
			case "video":
				switch (subtype)
				{
					case "mpeg":
						return UID.MPEG2MPML;
					case "mp4":
					case "quicktime":
						return UID.MPEG4HP41;
				}
		}
		return UID.ExplicitVRLittleEndian;
	}
}

