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

import org.nrg.xnatx.plugin.PluginCode;
import org.nrg.xnatx.plugin.PluginException;
import org.nrg.xnatx.roi.Constants;
import org.nrg.xnatx.plugin.PluginUtils;
import org.nrg.xnatx.roi.data.RoiCollection;
import org.nrg.xnatx.roi.service.DicomSpatialDataService;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.nrg.xdat.om.IcrRoicollectiondata;
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
public class DefaultCollectionConverter extends BaseCollectionProcessor
	implements CollectionConverter
{
	private final static Logger logger = LoggerFactory.getLogger(
		DefaultCollectionConverter.class);

	private boolean catalogSaved;
	private Helper helper;
	private final Lock lock = new ReentrantLock();
	private DicomSpatialDataService spatialDataService;
	private String targetType;

	@Override
	public Result convert(UserI user, RoiCollection roiCollection,
		String targetType, DicomSpatialDataService spatialDataService)
		throws PluginException
	{
		// Lock the single public method to allow use of instance variables
		try
		{
			lock.lock();
			return convertImpl(user, roiCollection, targetType, spatialDataService);
		}
		finally
		{
			lock.unlock();
		}
	}

	private void checkArgs(UserI user, RoiCollection roiCollection,
		String targetType, DicomSpatialDataService spatialDataService)
		throws PluginException
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
		if ((targetType == null) || targetType.isEmpty())
		{
			throw new PluginException("Target type must not be null or empty",
				PluginCode.HttpUnprocessableEntity);
		}
		this.targetType = targetType;
		if (spatialDataService == null)
		{
			throw new PluginException("Spatial data service must not be null",
				PluginCode.HttpUnprocessableEntity);
		}
		this.spatialDataService = spatialDataService;
	}

	private Result convertImpl(UserI user, RoiCollection roiCollection,
		String targetType, DicomSpatialDataService spatialDataService)
		throws PluginException
	{
		logger.info("Converting ROI collection "+roiCollection.getLabel()+
			" to "+targetType);
		resetState();
		Result result;
		PersistentWorkflowI workflow = null;
		EventMetaI eventMeta = null;
		try
		{
			checkArgs(user, roiCollection, targetType, spatialDataService);
			createHelper();
			sessionData = PluginUtils.getImageSessionData(
				roiCollection.getSessionId(), user);
			getCollectionData();

			workflow = ProcessUtils.buildOpenWorkflow(user, sessionData.getItem(),
				"Convert ROI Collection");
			eventMeta = workflow.buildEvent();

			removeExistingTargetTypeCatalog();
			XnatResourcecatalog resCatalog = createAndInsertCatalog(targetType);
			catalogSaved = true;
			createAndInsertResource(resCatalog, getTargetFileName(),
				createFileWriters(helper.convert()));

			saveCollectionData(eventMeta);
			ProcessUtils.complete(workflow, eventMeta);
			String message = "ROI collection "+collectData.getLabel()+
				" converted to "+targetType;
			logger.info(message);
			result =  new Result(message, HttpStatus.CREATED);
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
			String message = "Error converting ROI collection"+
				((collectData != null) ? " "+collectData.getLabel() : "");
			throw new PluginException(message, PluginCode.HttpInternalError, ex);
		}
		finally
		{
			resetState();
		}
		return result;
	}

	private List<FileWriterWrapperI> createFileWriters(byte[] bytes)
	{
		List<FileWriterWrapperI> writers = new ArrayList<>();
		FileWriterWrapperI fww = new StreamWriterWrapper(
			new ByteArrayInputStream(bytes), roiCollection.getName());
		writers.add(fww);

		return writers;
	}

	private void createHelper()
		throws PluginException
	{
		switch (roiCollection.getType())
		{
			case Constants.AIM:
				helper = new AimConversionHelper(roiCollection, targetType,
					spatialDataService);
				break;

			case Constants.RtStruct:
				helper = new RtStructConversionHelper(roiCollection, targetType,
					spatialDataService);
				break;

			default:
				throw new PluginException(
					"Unknown or unsupported ROI collection type: "+
						roiCollection.getType(),
					PluginCode.HttpUnprocessableEntity);
		}
		if (!helper.outputTypes().contains(targetType))
		{
			throw new PluginException(
				"Target format "+targetType+" not supported for collection type "+
					roiCollection.getType(),
				PluginCode.HttpUnprocessableEntity);
		}
}

	private void getCollectionData() throws PluginException
	{
		collectData = IcrRoicollectiondata.getIcrRoicollectiondatasById(
			roiCollection.getId(), user, false);
		if (collectData == null)
		{
			throw new PluginException(
				"Unknown ROI collection: "+roiCollection.getId(),
				PluginCode.HttpUnprocessableEntity);
		}
	}

	private String getTargetFileName()
	{
		String fileName = roiCollection.getLabel();
		switch (targetType)
		{
			case Constants.AIM:
				fileName += ".xml";
				break;
			case Constants.RtStruct:
				fileName += ".dcm";
				break;
			default:
				logger.error("File name generate failed for target type: {}",
					targetType);
		}
		return fileName;
	}

	private void resetState()
	{
		user = null;
		roiCollection = null;
		collectData = null;
		sessionData = null;
		catalogSaved =  false;
		spatialDataService = null;
	}

	private void removeExistingTargetTypeCatalog() throws PluginException
	{
		List<XnatResource> list = collectData.getOut_file();
		int idx = 0;
		for (XnatResource resource : list)
		{
			PersistentWorkflowI workflow = null;
			EventMetaI eventMeta = null;
			try
			{
				if (!(resource.getItem().instanceOf("xnat:resourceCatalog") &&
					 targetType.equals(resource.getLabel())))
				{
					idx++;
					continue;
				}
				XnatProjectdata projectData = collectData.getProjectData();
				workflow = ProcessUtils.getOrCreateWorkflowData(user,
					collectData.getXSIType(), roiCollection.getId(),
					projectData.getId(), EventUtils.REMOVE_CATALOG);
				eventMeta = workflow.buildEvent();
				removeResource(resource, projectData.getArchiveRootPath(),
							   projectData.getId(), collectData.getItem(), eventMeta, "OutFile");
				collectData.removeOut_file(idx);
				ProcessUtils.complete(workflow, eventMeta);
				// Save and refresh after modification
				saveCollectionData();
				refreshCollectionData();
				break;
			}
			catch (Exception ex)
			{
				ProcessUtils.safeFail(workflow, eventMeta);
				throw new PluginException("Error removing catalog",
					PluginCode.HttpInternalError, ex);
			}
		}
	}

	private void rollback()
	{
		if (catalogSaved)
		{
			logger.debug("Deleting catalog and child resources");
			XnatProjectdata projectData = collectData.getProjectData();
			String rootPath = projectData.getArchiveRootPath();
			// Remove out files from the file system and the collection data
			List<XnatResource> outFileList = collectData.getOut_file();
			for (int i=outFileList.size()-1; i>=0; i--)
			{
				try
				{
					XnatResource resource = outFileList.get(i);
					if (resource.getItem().instanceOf("xnat:resourceCatalog") &&
						 targetType.equals(resource.getLabel()))
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
	}

}
