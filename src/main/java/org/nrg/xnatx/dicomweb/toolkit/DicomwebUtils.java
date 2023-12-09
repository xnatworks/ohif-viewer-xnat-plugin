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

import org.dcm4che3.data.*;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomOutputStream;
import org.dcm4che3.json.JSONWriter;
import org.nrg.xdat.om.XnatImagesessiondata;
import org.nrg.xnatx.dicomweb.conf.DicomwebDeviceConfiguration;
import org.nrg.xnatx.plugin.PluginCode;
import org.nrg.xnatx.plugin.PluginException;
import org.nrg.xnatx.plugin.PluginUtils;

import javax.json.*;
import java.io.*;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

/**
 * @author m.alsad
 */
public class DicomwebUtils
{
	static final EnumSet<VR> encodeAsJSONNumber = EnumSet.noneOf(VR.class);

	public static boolean isSessionValidForDicomweb(
		XnatImagesessiondata sessionData)
	{
		String modality = PluginUtils.getImageSessionModality(sessionData);
		return DicomwebDeviceConfiguration.isDicomwebModality(modality);
	}

	public static Attributes decodeAttributes(byte[] b) throws IOException
	{
		if (b == null || b.length == 0)
		{
			return new Attributes(0);
		}

		Attributes result = new Attributes();
		ByteArrayInputStream is = new ByteArrayInputStream(b);
		try (DicomInputStream dis = new DicomInputStream(is))
		{
			dis.readFileMetaInformation();
			dis.readAttributes(result, -1, -1);
		}
		catch (IOException e)
		{
			throw new IOException("Could not decode attributes", e);
		}
		return result;
	}

	public static Attributes decodeMetadata(byte[] b) throws IOException
	{
		if (b == null || b.length == 0)
		{
			return new Attributes(0);
		}

		ByteArrayInputStream bin = new ByteArrayInputStream(b);
		InflaterInputStream cin = new InflaterInputStream(bin);
		try (ObjectInputStream in = new ObjectInputStream(cin))
		{
			return (Attributes) in.readObject();
		}
		catch (IOException | ClassNotFoundException e)
		{
			throw new IOException("Could not decode attributes", e);
		}
	}

	public static JSONWriter encodeAsJSONNumber(JSONWriter writer)
	{
		encodeAsJSONNumber.forEach(
			vr -> writer.setJsonType(vr, JsonValue.ValueType.NUMBER));
		return writer;
	}

	public static byte[] encodeAttributes(Attributes attrs)
		throws IOException
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream(512);
		try (DicomOutputStream dos =
					 new DicomOutputStream(out, UID.ExplicitVRLittleEndian))
		{
			dos.writeDataset(null, attrs);
		}
		catch (IOException e)
		{
			throw new IOException("Could not encode attributes", e);
		}
		return out.toByteArray();
	}

	public static byte[] encodeMetadata(Attributes attrs)
		throws IOException
	{
		ByteArrayOutputStream bout = new ByteArrayOutputStream(2048);
		DeflaterOutputStream cout = new DeflaterOutputStream(bout);
		try (ObjectOutputStream out = new ObjectOutputStream(cout))
		{
			out.writeObject(attrs);
		}
		catch (IOException e)
		{
			throw new IOException("Could not encode attributes", e);
		}
		return bout.toByteArray();
	}

	public static String generateEmptySessionJson(String sessionId,
		String studyUid) throws PluginException
	{
		JsonObjectBuilder study = Json.createObjectBuilder();
		study.add("StudyInstanceUID", studyUid);
		JsonArrayBuilder studies = Json.createArrayBuilder();
		studies.add(study);

		JsonObjectBuilder root = Json.createObjectBuilder();
		root.add("transactionId", sessionId)
				.add("isDicomWeb", true)
				.add("studies", studies);
		JsonObject jsonObject = root.build();

		String jsonString;
		try (Writer writer = new StringWriter())
		{
			Json.createWriter(writer).write(jsonObject);
			jsonString = writer.toString();
		}
		catch (IOException e)
		{
			throw new PluginException(
				"DICOMweb: Could not generate a placeholder session JSON",
				PluginCode.HttpUnprocessableEntity);
		}

		return jsonString;
	}

	public static Integer getInt(Attributes attrs, int tag, String defVal)
	{
		String val = attrs.getString(tag, defVal);
		if (val != null)
		{
			try
			{
				return Integer.valueOf(val);
			}
			catch (NumberFormatException ignored)
			{
			}
		}
		return null;
	}

	public static Map<String,String> getXnatIds(XnatImagesessiondata sessionData)
	{
		return getXnatIds(sessionData, null);
	}

	public static Map<String,String> getXnatIds(XnatImagesessiondata sessionData,
		String sharedProjectId)
	{
		Map<String,String> xnatIds = new HashMap<>();
		xnatIds.put(DicomwebConstants.XNAT_PROJECT_ID, sessionData.getProject());
		xnatIds.put(DicomwebConstants.XNAT_SUBJECT_ID, sessionData.getSubjectId());
		xnatIds.put(DicomwebConstants.XNAT_SESSION_ID, sessionData.getId());
		xnatIds.put(DicomwebConstants.XNAT_SHARED_PROJECT_ID, sharedProjectId);

		return xnatIds;
	}

	public static boolean isImage(Attributes attrs)
	{
		String sopClassUID = attrs.getString(Tag.SOPClassUID);
		return attrs.contains(Tag.BitsAllocated) &&
						 !sopClassUID.equals(UID.RTDoseStorage);
	}

	public static int parseInt(String s, String pattern)
	{
		if (s == null || !s.matches(pattern))
		{
			return 0;
		}

		return Integer.parseInt(s);
	}
}
