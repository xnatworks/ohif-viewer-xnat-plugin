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

import org.nrg.xnatx.plugin.DisplayUtils;
import org.nrg.xnatx.plugin.PluginCode;
import org.nrg.xnatx.plugin.PluginException;
import org.nrg.xnatx.roi.Constants;
import org.nrg.xnatx.roi.data.RoiCollection;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.base.BaseElement;
import org.nrg.xdat.om.IcrRoicollectiondata;
import org.nrg.xdat.om.XnatImagesessiondata;
import org.nrg.xdat.om.XnatResource;
import org.nrg.xdat.om.XnatResourcecatalog;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTItem;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.InvalidValueException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.SaveItemHelper;
import org.nrg.xnat.helpers.resource.XnatResourceInfo;
import org.nrg.xnat.helpers.resource.direct.DirectResourceModifierBuilder;
import org.nrg.xnat.helpers.resource.direct.ResourceModifierA;
import org.nrg.xnat.helpers.resource.direct.ResourceModifierBuilderI;
import org.nrg.xnat.helpers.uri.UriParserUtils;
import org.nrg.xnat.restlet.util.FileWriterWrapperI;
import org.nrg.xnat.services.archive.CatalogService;
import org.nrg.xnatx.roi.RoiUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jamesd
 */
public abstract class BaseCollectionProcessor
{
	private final static Logger logger = LoggerFactory.getLogger(
		BaseCollectionProcessor.class);

	protected final CatalogService catalogService =
		XDAT.getContextService().getBean(CatalogService.class);
	protected IcrRoicollectiondata collectData;
	protected RoiCollection roiCollection;
	protected XnatImagesessiondata sessionData;
	protected UserI user;

	protected XnatResourceInfo buildResourceInfo(EventMetaI eventMeta,
		String targetType)
	{
		String content = null;
		String description = null;
		String format = null;
		String[] tags = null;
		switch (targetType)
		{
			case Constants.AIM:
				content = "EXTERNAL";
				description = "AIM instance file";
				format = "XML";
				break;
			case Constants.RtStruct:
				content = "EXTERNAL";
				description = "RT Structure Set";
				format = "DICOM";
				break;
			case Constants.Segmentation:
				content = "EXTERNAL";
				description = "DICOM Segmentation";
				format = "DICOM";
				break;
			case Constants.Nifti:
				content = "EXTERNAL";
				description = "NIfTI File";
				format = "NIFTI";
				break;
			default:
		}
		Date date = EventUtils.getEventDate(eventMeta, false);

		XnatResourceInfo info = XnatResourceInfo.buildResourceInfo(description,
			format, content, tags, user, date, date,
			EventUtils.getEventId(eventMeta));

		return info;
	}

	protected ResourceModifierA buildResourceModifier(boolean overwrite,
		EventMetaI eventMeta) throws PluginException
	{
		ResourceModifierBuilderI builder = new DirectResourceModifierBuilder();
		
		XnatImagesessiondata assessed = collectData.getImageSessionData();
		builder.setAssess(assessed, collectData, "out");
		ResourceModifierA modifier;
		try
		{
			modifier = builder.buildResourceModifier(overwrite, user, eventMeta);
		}
		catch (Exception ex)
		{
			throw new PluginException("ResourceModifier creation error",
				PluginCode.HttpInternalError, ex);
		}
		return modifier;
	}

	protected XnatResourcecatalog createAndInsertCatalog(String targetType)
		throws PluginException
	{
		// Adapted from xnat-web. createCatalogItem() and setCatalogAttributes
		// seem to do nearly duplicate things. Not clear why
		XFTItem item = createCatalogItem(targetType);
		XnatResourcecatalog resCatalog =
			(XnatResourcecatalog) BaseElement.GetGeneratedItem(item);
		setCatalogAttributes(resCatalog, targetType);

		PersistentWorkflowI workflow = null;
		EventMetaI eventMeta = null;
		try
		{
			workflow = ProcessUtils.buildOpenWorkflow(user, collectData.getItem(),
				"Catalog Creation");
			eventMeta = workflow.buildEvent();
			catalogService.insertResourceCatalog(user,
				UriParserUtils.getArchiveUri(collectData), resCatalog);
			ProcessUtils.complete(workflow, eventMeta);
		}
		catch (Exception ex)
		{
			ProcessUtils.safeFail(workflow, eventMeta);
			throw new PluginException("Error creating catalog",
				PluginCode.HttpInternalError, ex);
		}
		refreshCollectionData();
		return resCatalog;
	}

	protected void createAndInsertResource(XnatResourcecatalog resCatalog,
		String fileName, List<FileWriterWrapperI> writers)
		throws PluginException
	{
		PersistentWorkflowI workflow = null;
		EventMetaI eventMeta = null;
		try
		{
			workflow = ProcessUtils.buildOpenWorkflow(user, collectData.getItem(),
				"Resource Creation");
			eventMeta = workflow.buildEvent();

			ResourceModifierA.UpdateMeta updateMeta =
				new ResourceModifierA.UpdateMeta(eventMeta, true);
			ResourceModifierA modifier = buildResourceModifier(true, updateMeta);
			Integer resourceId = resCatalog.getXnatAbstractresourceId();
			XnatResourceInfo resourceInfo = buildResourceInfo(updateMeta,
				resCatalog.getLabel());
			modifier.addFile(writers, resourceId, "out", fileName,
				resourceInfo, false);
			ProcessUtils.complete(workflow, eventMeta);
		}
		catch (Exception ex)
		{
			ProcessUtils.safeFail(workflow, eventMeta);
			throw new PluginException("Error creating resource",
				PluginCode.HttpInternalError, ex);
		}
	}

	private XFTItem createCatalogItem(String targetType) throws PluginException
	{
		String dataType = "xnat:resourceCatalog";
		XFTItem item;
		Map<String,String> params = new HashMap<>();
		switch (targetType)
		{
			case Constants.AIM:
				params.put("content", "EXTERNAL");
				params.put("description", "AIM instance file");
				params.put("format", "XML");
				break;
			case Constants.RtStruct:
				params.put("content", "EXTERNAL");
				params.put("description", "RT Structure Set");
				params.put("format", "DICOM");
				break;
			case Constants.Segmentation:
				params.put("content", "EXTERNAL");
				params.put("description", "DICOM Segmentation");
				params.put("format", "DICOM");
				break;
			case Constants.Nifti:
				params.put("content", "EXTERNAL");
				params.put("description", "NIfTI File");
				params.put("format", "NIFTI");
				break;
			default:
				throw new PluginException(
					"Unknown ROI collection type: "+targetType,
					PluginCode.HttpUnprocessableEntity);
		}
		item = populateItem(params, dataType);
		return item;
	}

	protected void logDisplay(IcrRoicollectiondata ircData, String description)
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos);
		ps.println(description);
		DisplayUtils.display(ircData, ps);
		logger.debug(baos.toString());
	}

	protected void logDisplay(XnatResourcecatalog catalog, String description)
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos);
		ps.println(description);
		DisplayUtils.display(catalog, ps);
		logger.debug(baos.toString());
	}

	protected void refreshCollectionData()
	{
		collectData = RoiUtils.getCollectionDataById(roiCollection.getId());
	}

	protected void removeResource(XnatResource resource, String rootPath, final String projectId, ItemI item, EventMetaI eventMeta, String description) {
		try
		{
			resource.deleteWithBackup(rootPath, projectId, user, eventMeta);
			SaveItemHelper.authorizedRemoveChild(item,
				"xnat:imageAssessorData/out/file", resource.getItem(), user,
				eventMeta);
			logger.debug(description+" "+resource.getXSIType()+" deleted: "+
				resource.getUri());
		}
		catch (Exception ex)
		{
			logger.error("Error removing "+description, ex);
		}
	}

	protected boolean saveCollectionData() throws PluginException
	{
		return saveCollectionData(null);
	}

	protected boolean saveCollectionData(EventMetaI eventMeta) throws PluginException
	{
		boolean collectDataSaved = false;
		if (eventMeta == null)
		{
			eventMeta = EventUtils.DEFAULT_EVENT(user, "Save ROI Collection");
		}
		logger.debug("Saving ROI collection data");
		try
		{
			collectDataSaved = SaveItemHelper.authorizedSave(collectData, user,
				false, true, eventMeta);
			if (collectDataSaved)
			{
				logger.info("ROI collection "+collectData.getName()+" saved");
			}
			else
			{
				logger.error("ROI collection "+collectData.getName()+" not saved");
			}
		}
		catch (Exception ex)
		{
			String message = "Error saving ROI collection"+
				((collectData != null) ? " "+collectData.getLabel() : "");
			throw new PluginException(message, PluginCode.HttpInternalError, ex);
		}
		return collectDataSaved;
	}

	private XFTItem populateItem(Map<String, String> params, String dataType)
		throws PluginException
	{
		XFTItem item;
		try
		{
			item = XFTItem.NewItem(dataType, user);
			item.setProperties(params, true);
			item.removeEmptyItems();
		}
		catch (ElementNotFoundException | XFTInitException |
			FieldNotFoundException | InvalidValueException ex)
		{
			throw new PluginException("Error creating XFTItem for "+dataType,
				PluginCode.HttpInternalError, ex);
		}
		return item;
	}

	private void setCatalogAttributes(XnatResourcecatalog resCatalog,
		String targetType) throws PluginException
	{
		switch (targetType)
		{
			case Constants.AIM:
				resCatalog.setContent("EXTERNAL");
				resCatalog.setDescription("AIM instance file");
				resCatalog.setFormat("XML");
				resCatalog.setLabel(Constants.AIM);
				break;
			case Constants.RtStruct:
				resCatalog.setContent("EXTERNAL");
				resCatalog.setDescription("RT Structure Set");
				resCatalog.setFormat("DICOM");
				resCatalog.setLabel(Constants.RtStruct);
				break;
			case Constants.Segmentation:
				resCatalog.setContent("EXTERNAL");
				resCatalog.setDescription("DICOM Segmentation");
				resCatalog.setFormat("DICOM");
				resCatalog.setLabel(Constants.Segmentation);
				break;
			case Constants.Nifti:
				resCatalog.setContent("EXTERNAL");
				resCatalog.setDescription("NIfTI File");
				resCatalog.setFormat("NIFTI");
				resCatalog.setLabel(Constants.Nifti);
				break;
			default:
				throw new PluginException(
					"Unknown ROI collection type: "+targetType,
					PluginCode.HttpUnprocessableEntity);
		}
	}

}
