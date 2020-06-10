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
package org.nrg.xnatx.roi.process;

import icr.etherj.StringUtils;
import org.nrg.xnatx.plugin.PluginCode;
import org.nrg.xnatx.plugin.PluginException;
import org.nrg.xnatx.roi.Constants;
import org.nrg.xnatx.plugin.PluginUtils;
import icr.xnat.plugin.roi.entity.Roi;
import org.nrg.xnatx.roi.data.RoiCollection;
import org.nrg.xnatx.roi.service.RoiService;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.nrg.xdat.om.IcrRoicollectiondata;
import org.nrg.xdat.om.IcrRoicollectiondataSeriesuid;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.om.XnatResource;
import org.nrg.xdat.om.XnatResourcecatalog;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.restlet.util.FileWriterWrapperI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

/**
 *
 * @author jamesd
 */
public class DefaultCollectionStorage extends BaseCollectionProcessor
	implements CollectionStorage
{
	private final static Logger logger = LoggerFactory.getLogger(
		DefaultCollectionStorage.class);

	private boolean catalogSaved;
	private boolean collectDataSaved;
	private final Lock lock = new ReentrantLock();
	private boolean roisCreated;
	private RoiService roiService;

	@Override
	public Result store(UserI user, RoiCollection roiCollection,
		RoiService roiService) throws PluginException
	{
		// Lock the single public method to allow use of instance variables
		try
		{
			lock.lock();
			return storeImpl(user, roiCollection, roiService);
		}
		finally
		{
			lock.unlock();
		}
	}

	private void checkArgs(UserI user, RoiCollection roiCollection,
		RoiService roiService) throws PluginException
	{
		if (user == null)
		{
			throw new PluginException("User must not be null",
				PluginCode.HttpUnprocessableEntity);
		}
		this.user = user;
		if (roiCollection == null)
		{
			throw new PluginException("ROI collection must not be null",
				PluginCode.HttpUnprocessableEntity);
		}
		this.roiCollection = roiCollection;
		if (roiService == null)
		{
			throw new PluginException("ROI service must not be null",
				PluginCode.HttpUnprocessableEntity);
		}
		this.roiService = roiService;
	}

	private void checkDependencies() throws PluginException
	{
		logger.debug("Checking dependencies present");
		Set<String> studyUids = roiCollection.getStudyUids();
		String studyUid = sessionData.getUid();
		logger.debug("Checking study UID: "+studyUid);
		if ((studyUids.size() != 1) || (!studyUids.contains(studyUid)))
		{
			throw new PluginException("Missing dependencies: study UID",
				PluginCode.HttpUnprocessableEntity);
		}
		String sessionId = sessionData.getId();
		Map<String,String> uidIdMap = PluginUtils.getImageScanUidIdMap(sessionId, user);
		Set<String> seriesUids = roiCollection.getSeriesUids();
		Set<String> sopInstUids = new HashSet<>();
		for (String seriesUid : seriesUids)
		{
			logger.debug("Checking series UID: "+seriesUid);
			String scanId = uidIdMap.get(seriesUid);
			if (StringUtils.isNullOrEmpty(scanId))
			{
				throw new PluginException("Missing dependencies: series UID",
				PluginCode.HttpUnprocessableEntity);
			}
			Set<String> scanSopInstUids = PluginUtils.getSopInstanceUids(user,
				sessionId, scanId);
			sopInstUids.addAll(scanSopInstUids);
			logger.debug(String.valueOf(scanSopInstUids.size())+
				" SOP instance UIDs added from scan "+scanId);
		}
		Set<String> collectionSopInstUids = roiCollection.getSopInstanceUids();
		logger.debug("Checking "+collectionSopInstUids.size()+" SOP instance UIDs");
		if (!sopInstUids.containsAll(collectionSopInstUids))
		{
			throw new PluginException("Missing dependencies: SOP instance UID",
				PluginCode.HttpUnprocessableEntity);
		}
	}

	private void clearCollectData() throws PluginException
	{
		logger.debug("Clearing ROI collection data");
		XnatProjectdata projectData = collectData.getProjectData();
		String rootPath = projectData.getArchiveRootPath();
		String projectId = projectData.getId();
		String collectId = collectData.getId();
		String xsiType = collectData.getXSIType();
		// Remove referenced UIDs
		List<IcrRoicollectiondataSeriesuid> seriesUidList =
			collectData.getReferences_seriesuid();
		for (int i=seriesUidList.size()-1; i>=0; i--)
		{
			IcrRoicollectiondataSeriesuid seriesUid = seriesUidList.get(i);
			collectData.removeReferences_seriesuid(i);
			logger.debug("Removed seriesUid: {}", seriesUid.getSeriesuid());
		}
		// Remove in files from the file system and the collection data
		List<XnatResource> inFileList = collectData.getIn_file();
		for (int i=inFileList.size()-1; i>=0; i--)
		{
			String uri = inFileList.get(i).getUri();
			removeFile(uri, "InFile");
			collectData.removeIn_file(i);
		}
		// Remove out files from the file system and the collection data
		List<XnatResource> outFileList = collectData.getOut_file();
		for (int i=outFileList.size()-1; i>=0; i--)
		{
			PersistentWorkflowI workflow = null;
			EventMetaI eventMeta = null;
			try
			{
				XnatResource resource = outFileList.get(i);
				if (resource.getItem().instanceOf("xnat:resourceCatalog"))
				{
					workflow = ProcessUtils.getOrCreateWorkflowData(user, xsiType,
						collectId, projectId, EventUtils.REMOVE_CATALOG);
					eventMeta = workflow.buildEvent();
					removeResource(resource, rootPath, projectId, collectData.getItem(),
								   eventMeta, "OutFile");
					collectData.removeOut_file(i);
					ProcessUtils.complete(workflow, eventMeta);
				}
				else
				{
					removeFile(resource.getUri(), "OutFile");
					collectData.removeOut_file(i);
				}
			}
			catch (Exception ex)
			{
				ProcessUtils.safeFail(workflow, eventMeta);
				throw new PluginException("Error removing out file",
					PluginCode.HttpInternalError, ex);
			}
		}
		saveCollectionData();
		refreshCollectionData();
	}

	private IcrRoicollectiondata createCollectionData()
		throws PluginException
	{
		collectData = IcrRoicollectiondata.getIcrRoicollectiondatasById(
			roiCollection.getId(), user, false);
		if (collectData != null)
		{
			clearCollectData();
		}
		else
		{
			logger.debug("Creating ROI collection data");
			collectData = (IcrRoicollectiondata) PluginUtils.generateItem(
				IcrRoicollectiondata.SCHEMA_ELEMENT_NAME);
			// Set the session ID so that saving updates the session
			collectData.setImagesessionId(sessionData.getId());
		}
		collectData.setId(roiCollection.getId());
		collectData.setProject(roiCollection.getProjectId());
		collectData.setSubjectid(roiCollection.getSubjectId());
		collectData.setLabel(roiCollection.getLabel());
		collectData.setName(roiCollection.getName());
		collectData.setCollectiontype(roiCollection.getType());
		collectData.setDate(dateOrNull(roiCollection.getDate()));
		collectData.setTime(timeOrNull(roiCollection.getTime()));
		collectData.setUid(roiCollection.getUid());
		for (String uid : roiCollection.getSeriesUids())
		{
			logger.debug("Referenced series UID: {}", uid);
			IcrRoicollectiondataSeriesuid uidItem = 
				(IcrRoicollectiondataSeriesuid)PluginUtils.generateItem(
					IcrRoicollectiondataSeriesuid.SCHEMA_ELEMENT_NAME);
			uidItem.setSeriesuid(uid);
			try
			{
				EventMetaI eventMeta = EventUtils.DEFAULT_EVENT(user,
					"Referenced series UID "+uid+" created");
				uidItem.save(user, false, true, eventMeta);
				collectData.addReferences_seriesuid(uidItem);
			}
			catch (Exception ex)
			{
				throw new PluginException(
					"Error adding referenced series UID: "+ex.getMessage(),
					PluginCode.HttpInternalError, ex);
			}
		}
		saveCollectionData();
		collectDataSaved = true;
		return collectData;
	}

	private List<FileWriterWrapperI> createFileWriters()
	{
		List<FileWriterWrapperI> list = new ArrayList<>();
		FileWriterWrapperI fww = new RoiCollectionWriterWrapper(roiCollection);
		list.add(fww);

		return list;
	}

	private void createRois()
	{
		List<Roi> rois = roiCollection.getRoiList();
		logger.debug("Creating {} ROIs", rois.size());
		for (Roi roi : rois)
		{
			roiService.create(roi);
		}
		roisCreated = true;
	}

	private Object dateOrNull(String date)
	{
		return !StringUtils.isNullOrEmpty(date) ? date : null;
	}

	private String getTargetFileName()
	{
		String fileName = roiCollection.getLabel();
		switch (roiCollection.getType())
		{
			case Constants.AIM:
				fileName += ".xml";
				break;
			case Constants.RtStruct:
			case Constants.Segmentation:
				fileName += ".dcm";
				break;
			case Constants.Nifti:
				fileName += ".nii.gz";
				break;
			default:
				logger.error("File name generate failed for target type: {}",
					roiCollection.getType());
		}
		return fileName;
	}

	private void removeFile(String uri, String label)
	{
		try
		{
			Files.delete(Paths.get(uri));
			logger.debug(label+" deleted: "+uri);
		}
		catch (FileNotFoundException ex)
		{
			logger.warn(label+" not found: "+uri, ex);
		}
		catch (IOException ex)
		{
			logger.error(label+" not deleted from filesystem: "+uri, ex);
		}
	}

	private void resetState()
	{
		user = null;
		roiCollection = null;
		collectData = null;
		sessionData = null;
		collectDataSaved = false;
		catalogSaved = false;
		roiService = null;
		roisCreated = false;
	}

	private void rollback()
	{
		logger.info("Rolling back ROI collection creation");
		if (roisCreated)
		{
			logger.debug("Deleting ROIs");
			roiService.deleteCollectionRois(collectData.getId());
		}
		if (catalogSaved)
		{
			logger.debug("Deleting catalog and child resources");
			XnatProjectdata projectData = collectData.getProjectData();
			String rootPath = projectData.getArchiveRootPath();
			String catLabel = collectData.getCollectiontype();
			// Remove out files from the file system and the collection data
			List<XnatResource> outFileList = collectData.getOut_file();
			for (int i=outFileList.size()-1; i>=0; i--)
			{
				try
				{
					XnatResource resource = outFileList.get(i);
					if (resource.getItem().instanceOf("xnat:resourceCatalog") &&
						 catLabel.equals(resource.getLabel()))
					{
						EventMetaI eventMeta = EventUtils.DEFAULT_EVENT(user,
							"Remove Catalog");
						removeResource(resource, rootPath, projectData.getId(), collectData.getItem(),
									   eventMeta, "OutFile");
						collectData.removeOut_file(i);
						saveCollectionData();
						refreshCollectionData();
						break;
					}
				}
				catch (Exception ex)
				{
					logger.error("Error removing catalog - "+ex.getMessage());
				}
			}
		}
		if (collectDataSaved)
		{
			logger.debug("Deleting collection data");
			try
			{
				XnatProjectdata projectData = collectData.getProjectData();
				EventMetaI eventMeta = EventUtils.DEFAULT_EVENT(user,
					"ROI Collection "+collectData.getName()+" deleted");
				String result = collectData.delete(projectData, user, true,
					eventMeta);
				if (result != null)
				{
					logger.warn(result);
				}
			}
			catch (Exception ex)
			{
				logger.error("Error removing collection data - "+ex.getMessage());
			}
		}
	}

	private Result storeImpl(UserI user, RoiCollection roiCollection,
		RoiService roiService) throws PluginException
	{
		logger.info("Storing ROI collection "+roiCollection.getLabel());
		resetState();
		Result result;
		PersistentWorkflowI workflow = null;
		EventMetaI eventMeta = null;
		try
		{
			checkArgs(user, roiCollection, roiService);
			sessionData = PluginUtils.getImageSessionData(
				roiCollection.getSessionId(), user);
			checkDependencies();
			createCollectionData();

			workflow = ProcessUtils.buildOpenWorkflow(user, sessionData.getItem(),
				"Upload ROI Collection");
			eventMeta = workflow.buildEvent();

			XnatResourcecatalog resCatalog = createAndInsertCatalog(
				roiCollection.getType());
			catalogSaved = true;
			createAndInsertResource(resCatalog, getTargetFileName(),
				createFileWriters());
			
			saveCollectionData(eventMeta);
			createRois();
			ProcessUtils.complete(workflow, eventMeta);
			logger.info("ROI collection "+collectData.getLabel()+" stored");
			result = new Result(collectData.getId(), HttpStatus.OK);
			refreshCollectionData();
		}
		catch (PluginException ex)
		{
			rollback();
			ProcessUtils.safeFail(workflow, eventMeta);
			throw ex;
		}
		catch (Exception ex)
		{
			rollback();
			ProcessUtils.safeFail(workflow, eventMeta);
			String message = "Error storing ROI collection"+
				((collectData != null) ? " "+collectData.getLabel() : "");
			throw new PluginException(message, PluginCode.HttpInternalError, ex);
		}
		finally
		{
			resetState();
		}

		return result;
	}

	private Object timeOrNull(String time)
	{
		return !StringUtils.isNullOrEmpty(time) ? time : null;
	}

}
