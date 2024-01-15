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
package org.nrg.xnatx.plugin;

import icr.etherj.StringUtils;
import icr.etherj.Xml;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.nrg.xdat.base.BaseElement;
import org.nrg.xdat.model.XnatAbstractresourceI;
import org.nrg.xdat.model.XnatImagescandataI;
import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xdat.om.XnatExperimentdataShare;
import org.nrg.xdat.om.XnatImagescandata;
import org.nrg.xdat.om.XnatImagesessiondata;
import org.nrg.xdat.om.XnatResourcecatalog;
import org.nrg.xft.ItemI;
import org.nrg.xft.search.CriteriaCollection;
import org.nrg.xft.security.UserI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author jamesd
 */
public class PluginUtils
{
	private final static Logger logger = LoggerFactory.getLogger(
		PluginUtils.class);

	/**
	 *
	 * @param schemaElement
	 * @return
	 * @throws PluginException
	 */
	public static ItemI generateItem(String schemaElement) throws PluginException
	{
		Class c = BaseElement.GetGeneratedClass(schemaElement);
		ItemI o = null;
		try
		{
			o = (ItemI) c.newInstance();
		}
		catch (InstantiationException | IllegalAccessException ex)
		{
			throw new PluginException("Error creating item for "+schemaElement,
				PluginCode.HttpInternalError, ex);
		}
		return BaseElement.GetGeneratedItem(o);
	}


	/**
	 *
	 * @param expData
	 * @return
	 */
	public static String getExperimentPath(XnatExperimentdata expData)
	{
		File sessionDir = expData.getSessionDir();
		return (sessionDir != null)
			? sessionDir.getPath()+File.separator
			: null;
	}

//	/**
//	 *
//	 * @param sessionData
//	 * @return
//	 */
//	public static String getExperimentPath(XnatImagesessiondata sessionData)
//	{
//		File sessionDir = sessionData.getSessionDir();
//		return (sessionDir != null)
//			? sessionData.getSessionDir().getPath()+File.separator
//			: null;
//	}

	/**
	 *
	 * @param sessionData
	 * @param id
	 * @return
	 * @throws PluginException
	 */
	public static XnatImagescandata getImageScanData(
		XnatImagesessiondata sessionData, String id) throws PluginException
	{
		if (sessionData == null)
		{
			throw new PluginException("Null image session data",
				PluginCode.HttpUnprocessableEntity);
		}
		XnatImagescandata scanData = sessionData.getScanById(id);
		if (scanData == null)
		{
			throw new PluginException("No scan found for ID: "+id,
				PluginCode.HttpUnprocessableEntity);
		}
		return scanData;
	}

	/**
	 *
	 * @param sessionData
	 * @param uid
	 * @return
	 */
	public static XnatImagescandata getImageScanDataByUid(
		XnatImagesessiondata sessionData, String uid)
	{
		CriteriaCollection sessionCc = new CriteriaCollection("OR");
		sessionCc.addClause("xnat:imageScanData/image_session_ID",
			sessionData.getId());
		CriteriaCollection cc = new CriteriaCollection("AND");
		cc.addClause(sessionCc);
		cc.addClause("xnat:imageScanData/UID", uid);
		List<XnatImagescandata> scanList =
			XnatImagescandata.getXnatImagescandatasByField(cc, null, false);
		if ((scanList == null) || scanList.isEmpty())
		{
			return null;
		}
		return scanList.get(0);
	}

	/**
	 *
	 * @param sessionId
	 * @param user
	 * @return
	 * @throws PluginException
	 */
	public static Map<String,String> getImageScanUidIdMap(String sessionId,
		UserI user) throws PluginException
	{
		XnatImagesessiondata sessionData = PluginUtils.getImageSessionData(
			sessionId, user);
		return getImageScanUidIdMap(sessionData);
	}

	/**
	 *
	 * @param sessionData
	 * @return
	 */
	public static Map<String,String> getImageScanUidIdMap(
		XnatImagesessiondata sessionData)
	{
		if (sessionData == null)
		{
			throw new IllegalArgumentException("SessionData must not be null");
		}
		Map<String,String> uidIdMap = new LinkedHashMap<>();
		List<XnatImagescandataI> scans = sessionData.getScans_scan();
		if (scans.isEmpty())
		{
			logger.info("Session "+sessionData.getId()+" contains zero scans");
		}
		for (XnatImagescandataI scanData : scans)
		{
			String uid = scanData.getUid();
			if (!StringUtils.isNullOrEmpty(uid))
			{
				uidIdMap.put(uid, scanData.getId());
			}
			else
			{
				logger.warn("UID is null or empty for scan "+scanData.getId()+
					" in session "+sessionData.getId());
			}
		}
		return uidIdMap;
	}

	/**
	 *
	 * @param id
	 * @param user
	 * @return
	 * @throws PluginException
	 */
	public static XnatImagesessiondata getImageSessionData(String id, UserI user)
		throws PluginException
	{
		XnatImagesessiondata sessionData = 
			XnatImagesessiondata.getXnatImagesessiondatasById(id, user, false);
		if (sessionData == null)
		{
			throw new PluginException("No session found for ID: "+id,
				PluginCode.HttpUnprocessableEntity);
		}
		return sessionData;
	}

	/**
	 *
	 * @param projectId
	 * @return
	 */
	public static List<XnatImagesessiondata> getImageSessionDataByProject(
		String projectId)
	{
		CriteriaCollection cc = new CriteriaCollection("OR");
		cc.addClause("xnat:imageSessionData/project", projectId);
		cc.addClause("xnat:imageSessionData/sharing/share/project", projectId);
		List<XnatImagesessiondata> sessionList =
			XnatImagesessiondata.getXnatImagesessiondatasByField(cc, null, false);
		return (sessionList != null) ? sessionList : new ArrayList<>();
	}

	/**
	 *
	 * @param sessionData
	 * @return
	 */
	public static String getImageSessionModality(XnatImagesessiondata sessionData)
	{
		String modality = sessionData.getModality();
		if (!StringUtils.isNullOrEmpty(modality))
		{
			return modality;
		}
		// Hack to work around the session's modality not being filled in during
		// session creation
		String xsiType = sessionData.getXSIType();
		logger.warn(
			"Modality for session "+sessionData.getId()+" is null or empty."+
				" Deriving from XSI type: "+xsiType);
		switch (xsiType)
		{
			case "xnat:mrSessionData":
				return "MR";
			case "xnat:ctSessionData":
				return "CT";
			case "xnat:petSessionData":
				return "PT";
			case "xnat:crSessionData":
				return "CR";
			default:
		}
		modality = xsiType.substring(5, xsiType.indexOf("SessionData"));
		return modality.toUpperCase();
	}

	/**
	 *
	 * @param user
	 * @param sessionId
	 * @param scanId
	 * @return
	 * @throws PluginException
	 */
	public static Set<String> getSopInstanceUids(UserI user, String sessionId,
		String scanId) throws PluginException
	{
		Set<String> uids = new LinkedHashSet<>();
		XnatImagesessiondata sessionData = PluginUtils.getImageSessionData(
			sessionId, user);
		XnatImagescandata scanData = sessionData.getScanById(scanId);
		if (scanData == null)
		{
			throw new PluginException(
				"Bad scan ID or not visible to user "+user.getUsername()+": "+scanId,
				PluginCode.HttpUnprocessableEntity);
		}
		List<String> catPaths = getScanCatalogs(sessionData, scanData);
		for (String catPath : catPaths)
		{
			addSopInstanceUids(catPath, uids);
		}
		logger.debug("UIDs found: "+uids.size());
		return uids;
	}

	/**
	 * Get path to scan catalog xml
	 * @param sessionData 	the session object
	 * @param scanData		the scan object
	 * @return the path to catalog.xml
	 */
	public static String getScanCatalog(XnatImagesessiondata sessionData,
		XnatImagescandata scanData)
	{
		String dcmPath = null;
		String secPath = null;
		for (XnatAbstractresourceI res : scanData.getFile()) {
			if (res instanceof XnatResourcecatalog)
			{
				switch (res.getLabel())
				{
					case "DICOM":
						dcmPath = ((XnatResourcecatalog) res).getUri();
						break;
					case "secondary":
						secPath = ((XnatResourcecatalog) res).getUri();
						break;
					default:
				}
			}
		}
		if (dcmPath != null)
		{
			return dcmPath;
		}
		if (secPath != null)
		{
			return secPath;
		}
		String scanId = scanData.getId();
		String path = PluginUtils.getExperimentPath(sessionData)+
				"SCANS"+File.separator+scanId+File.separator+"DICOM"+File.separator+
				"scan_"+scanId+"_catalog.xml";
		logger.warn("No DICOM or secondary catalogs found in XnatImagescandata. Using fallback path: "+path);
		return path;
	}

	/**
	 * Get paths to scan catalog xml files
	 * @param sessionData 	the session object
	 * @param scanData		the scan object
	 * @return the path to catalog.xml
	 */
	public static List<String> getScanCatalogs(XnatImagesessiondata sessionData,
		XnatImagescandataI scanData)
	{
		List<String> catPaths = new ArrayList<>();
		for (XnatAbstractresourceI res : scanData.getFile()) {
			if (res instanceof XnatResourcecatalog &&
					(res.getLabel().equals("DICOM") || res.getLabel().equals("secondary")))
			{
				catPaths.add(((XnatResourcecatalog) res).getUri());
			}
		}
		return catPaths;
	}

	/**
	 * Get path to scan data
	 * @param sessionData 	the session object
	 * @param scanData		the scan object
	 * @return the path to the scan data
	 */
	public static String getScanPath(XnatImagesessiondata sessionData,
		XnatImagescandata scanData)
	{
		return Paths.get(getScanCatalog(sessionData, scanData)).getParent().toString();
	}

	/**
	 *
	 * @param id
	 * @param user
	 * @return
	 */
	public static boolean isImageSessionData(String id, UserI user)
	{
		XnatImagesessiondata sessionData = 
			XnatImagesessiondata.getXnatImagesessiondatasById(id, user, false);
		return (sessionData != null);
	}

	/**
	 *
	 * @param expData
	 * @param projectId
	 * @return
	 */
	public static boolean isSharedIntoProject(XnatExperimentdata expData,
		String projectId)
	{
		if (expData.getProject().equals(projectId))
		{
			logger.info("Experiment "+expData.getId()+" belongs to project "+
				projectId);
			return true;
		}

		List<XnatExperimentdataShare> xnatExperimentdataShareList =
			expData.getSharing_share();
		for (XnatExperimentdataShare share : xnatExperimentdataShareList)
		{
			logger.info("Share project ID: "+share.getProject());
			if (share.getProject().equals(projectId))
			{
				return true;
			}
		}
		return false;		
	}

	/**
	 *
	 * @param is
	 * @return
	 * @throws PluginException
	 */
	public static Document streamToDoc(InputStream is) throws PluginException
	{
		Document doc = null;
		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			doc = builder.parse(is);
		}
		catch (ParserConfigurationException ex)
		{
			throw new PluginException(PluginCode.ParserConfiguration, ex);
		}
		catch (SAXException ex)
		{
			throw new PluginException(PluginCode.SAX, ex);
		}
		catch (IllegalArgumentException ex)
		{
			throw new PluginException(PluginCode.IllegalArgument, ex);
		}
		catch (IOException ex)
		{
			throw new PluginException(PluginCode.IO, ex);
		}
		return doc;
	}

	private static void addSopInstanceUids(String path, Set<String> uids) throws PluginException
	{
		Document doc;
		try
		{
			logger.debug("Catalog for UIDs: "+path);
			doc = streamToDoc(new FileInputStream(path));
		}
		catch (IOException ex)
		{
			throw new PluginException("DICOM catalog error",
				PluginCode.HttpUnprocessableEntity, ex);
		}
		Node node = Xml.getFirstMatch(doc.getDocumentElement(), "cat:entries");
		NodeList children = node.getChildNodes();
		for (int i=0; i<children.getLength(); i++)
		{
			Node child = children.item(i);
			if (!child.getNodeName().equals("cat:entry"))
			{
				continue;
			}
			NamedNodeMap attrs = child.getAttributes();
			Node uidNode = attrs.getNamedItem("UID");
			if (uidNode != null)
			{
				String uid = uidNode.getTextContent();
				if (!StringUtils.isNullOrEmpty(uid))
				{
					uids.add(uid);
				}
			}
		}		
	}

	private PluginUtils()
	{}
}
