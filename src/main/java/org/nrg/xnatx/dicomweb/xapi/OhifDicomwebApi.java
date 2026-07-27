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
package org.nrg.xnatx.dicomweb.xapi;

import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.dcm4che3.util.StringUtils;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.xapi.rest.*;
import org.nrg.xdat.model.XnatSubjectassessordataI;
import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xdat.om.XnatImagesessiondata;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.om.XnatSubjectdata;
import org.nrg.xdat.security.helpers.AccessLevel;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xft.security.UserI;
import org.nrg.xnatx.dicomweb.service.inputcreator.DicomwebInputHandler;
import org.nrg.xnatx.dicomweb.service.qido.QidoRsModel;
import org.nrg.xnatx.dicomweb.service.qido.QidoRsService;
import org.nrg.xnatx.dicomweb.service.wado.WadoRsService;
import org.nrg.xnatx.dicomweb.service.wado.WadoRsTarget;
import org.nrg.xnatx.dicomweb.toolkit.DicomwebUtils;
import org.nrg.xnatx.dicomweb.conf.QIDO;
import org.nrg.xnatx.dicomweb.xapi.annotation.*;
import org.nrg.xnatx.plugin.PluginCode;
import org.nrg.xnatx.plugin.PluginException;
import org.nrg.xnatx.plugin.PluginUtils;
import org.nrg.xnatx.plugin.Security;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author mo.alsad
 */
@Slf4j
@Api(description = "OHIF Viewer DICOMweb API")
@SwaggerDefinition
@XapiRestController
@RequestMapping(value = "/viewerDicomweb")
public class OhifDicomwebApi extends AbstractXapiRestController
{
	private final Lock genAllDwDataLock = new ReentrantLock();
	private final DicomwebInputHandler dwInputHandler;
	private final QidoRsService qidoRsService;
	private final WadoRsService wadoRsService;

	@Autowired
	public OhifDicomwebApi(final DicomwebInputHandler dwInputHandler,
		final UserManagementServiceI userManagementService,
		final RoleHolder roleHolder, final QidoRsService qidoRsService,
		final WadoRsService wadoRsService)
	{
		super(userManagementService, roleHolder);
		this.dwInputHandler = dwInputHandler;
		this.qidoRsService = qidoRsService;
		this.wadoRsService = wadoRsService;
		log.info("Viewer DICOMweb XAPI initialised");
	}

	/*
	############################################################
		DICOMweb data generation
	############################################################
	*/

	@ApiOperation(value = "Remove DICOMweb data for the specified experiment ID.")
	@ApiResponses(
		{
			@ApiResponse(code = 200, message = "OK, the session DICOMweb deleted."),
			@ApiResponse(code = 403, message = "The user does not have permission to delete the indicated experiment."),
			@ApiResponse(code = 404, message = "The specified DICOMweb does not exist."),
			@ApiResponse(code = 500, message = "An unexpected error occurred."),
			@ApiResponse(code = 501, message = "SOP Class or modality not supported."),
		})
	@XapiRequestMapping(
		value = "projects/{projectId}/experiments/{experimentId}",
		method = RequestMethod.DELETE,
		restrictTo = AccessLevel.Admin)
	public ResponseEntity<String> deleteExperimentDicomweb(
		final @ApiParam(value = "Project ID") @PathVariable("projectId") @Project String projectId,
		final @ApiParam(value = "Experiment ID") @PathVariable("experimentId") @Experiment String experimentId)
		throws PluginException
	{
		UserI user = getSessionUser();
		XnatImagesessiondata sessionData = checkPermissions(user, projectId,
			experimentId, Security.Delete);

		if (!DicomwebUtils.isSessionValidForDicomweb(sessionData))
		{
			return new ResponseEntity<>(
				"Session "+experimentId+" is not supported for DICOMweb",
				HttpStatus.NOT_IMPLEMENTED);
		}

		log.info("DicomwebApi::deleteExperimentDicomweb(projectId="+projectId+
							 ", experimentId="+experimentId+")");
		if (log.isDebugEnabled())
		{
			log.debug("DELETE /projects/"+projectId+"/experiments/"+experimentId+
									" by user "+user.getUsername());
		}

		dwInputHandler.deleteDicomwebData(sessionData);

		return new ResponseEntity<>(HttpStatus.OK);
	}

	@ApiOperation(value = "Checks if Session level DICOMweb exists")
	@ApiResponses(
		{
			@ApiResponse(code = 200, message = "OK, the session DICOMweb exists."),
			@ApiResponse(code = 403, message = "The user does not have permission to view the indicated experiment."),
			@ApiResponse(code = 404, message = "The specified DICOMweb does not exist."),
			@ApiResponse(code = 500, message = "An unexpected error occurred."),
			@ApiResponse(code = 501, message = "SOP Class or modality not supported."),
		})
	@XapiRequestMapping(
		value = "projects/{projectId}/experiments/{experimentId}/exists",
		method = RequestMethod.GET,
		restrictTo = AccessLevel.Read)
	public ResponseEntity<String> doesExperimentDicomwebExist(
		final @ApiParam(value = "Project ID") @PathVariable("projectId") @Project String projectId,
		final @ApiParam(value = "Experiment ID") @PathVariable("experimentId") @Experiment String experimentId)
		throws PluginException
	{
		UserI user = getSessionUser();
		XnatImagesessiondata sessionData = checkPermissions(user, projectId,
			experimentId, Security.Read);

		if (!DicomwebUtils.isSessionValidForDicomweb(sessionData))
		{
			return new ResponseEntity<>(
				"Session "+experimentId+" is not supported for DICOMweb",
				HttpStatus.NOT_IMPLEMENTED);
		}

		if (!dwInputHandler.hasValidDicomwebData(sessionData))
		{
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@ApiOperation(value = "Generates DICOMweb data for the specified experiment ID.")
	@ApiResponses(
		{
			@ApiResponse(code = 201, message = "The DICOMweb data has been created."),
			@ApiResponse(code = 403, message = "The user does not have permission to perform this action."),
			@ApiResponse(code = 423, message = "This process is already underway and is locked."),
			@ApiResponse(code = 500, message = "An unexpected error occurred."),
			@ApiResponse(code = 501, message = "SOP Class or modality not supported."),
		})
	@XapiRequestMapping(
		value = "generate-all-dicomweb",
		method = RequestMethod.POST,
		restrictTo = AccessLevel.Admin
	)
	public ResponseEntity<String> postAllExperimentsDicomweb(
		final @ApiParam(value = "Overwrite existing data")
		@RequestParam(value = "overwrite", required = false, defaultValue = "false") boolean overwrite)
		throws PluginException
	{
		// Prevent starting the generation process if another one is already running
		if (!genAllDwDataLock.tryLock())
		{
			return new ResponseEntity<>(HttpStatus.LOCKED);
		}
		HttpStatus status;
		try
		{
			log.info("All projects DICOMweb data creation requested");
			status = generateAllDwData(overwrite);
			log.info("All projects DICOMweb data creation complete");
		}
		finally
		{
			genAllDwDataLock.unlock();
		}
		return new ResponseEntity<>(status);
	}

	@ApiOperation(value = "Generates DICOMweb data for the specified experiment ID.")
	@ApiResponses(
		{
			@ApiResponse(code = 201, message = "The DICOMweb data has been created."),
			@ApiResponse(code = 403, message = "The user does not have permission to perform this action."),
			@ApiResponse(code = 500, message = "An unexpected error occurred."),
			@ApiResponse(code = 501, message = "SOP Class or modality not supported."),
		})
	@XapiRequestMapping(
		value = "projects/{projectId}/experiments/{experimentId}",
		method = RequestMethod.POST,
		restrictTo = AccessLevel.Edit
	)
	public ResponseEntity<String> postExperimentDicomweb(
		final @ApiParam(value = "Project ID") @PathVariable("projectId") @Project String projectId,
		final @ApiParam(value = "Experiment ID") @PathVariable("experimentId") @Experiment String experimentId,
		final @ApiParam(value = "Overwrite existing data")
		@RequestParam(value = "overwrite", required = false, defaultValue = "false") boolean overwrite)
		throws PluginException
	{
		UserI user = getSessionUser();
		XnatImagesessiondata sessionData = checkPermissions(user, projectId,
			experimentId, Security.Edit, Security.Read);

		log.info("Session " + experimentId + " DICOMweb data creation requested");
		dwInputHandler.createDicomwebData(sessionData, overwrite);
		log.info("Session " + experimentId + " DICOMweb data creation complete");

		return new ResponseEntity<>(HttpStatus.OK);
	}

	@ApiOperation(value = "Generates DICOMweb data for every session in the project.")
	@ApiResponses(
		{
			@ApiResponse(code = 201, message = "The DICOMweb data has been created for every session in the project."),
			@ApiResponse(code = 403, message = "The user does not have permission to perform this action."),
			@ApiResponse(code = 423, message = "This process is already underway and is locked."),
			@ApiResponse(code = 500, message = "An unexpected error occurred."),
			@ApiResponse(code = 501, message = "SOP Class or modality not supported."),
		})
	@XapiRequestMapping(
		value = "projects/{projectId}",
		method = RequestMethod.POST,
		restrictTo = AccessLevel.Admin
	)
	public ResponseEntity<String> postProjectDicomweb(
		final @ApiParam(value = "Project ID") @PathVariable("projectId") @Project String projectId,
		final @ApiParam(value = "Overwrite existing data")
		@RequestParam(value = "overwrite", required = false, defaultValue = "false") boolean overwrite)
		throws PluginException
	{
		UserI user = getSessionUser();
		Security.checkProject(user, projectId);

		// Prevent starting the generation process if another one is already running
		if (!genAllDwDataLock.tryLock())
		{
			return new ResponseEntity<>(HttpStatus.LOCKED);
		}
		HttpStatus status;
		try
		{
			log.info("Project "+projectId+" DICOMweb data creation requested");
			status = generateProjectDwData(user, projectId, overwrite);
			log.info("Project "+projectId+" DICOMweb data creation complete");
		}
		finally
		{
			genAllDwDataLock.unlock();
		}
		return new ResponseEntity<>(status);
	}

	@ApiOperation(value = "Generates DICOMweb data for every session in the subject.")
	@ApiResponses(
		{
			@ApiResponse(code = 201, message = "The DICOMweb data has been created for every session in the subject."),
			@ApiResponse(code = 403, message = "The user does not have permission to perform this action."),
			@ApiResponse(code = 423, message = "This process is already underway and is locked."),
			@ApiResponse(code = 500, message = "An unexpected error occurred."),
			@ApiResponse(code = 501, message = "SOP Class or modality not supported."),
		})
	@XapiRequestMapping(
		value = "projects/{projectId}/subjects/{subjectId}",
		method = RequestMethod.POST,
		restrictTo = AccessLevel.Edit
	)
	public ResponseEntity<String> postSubjectDicomweb(
		final @ApiParam(value = "Project ID") @PathVariable("projectId") @Project String projectId,
		final @ApiParam(value = "Subject ID") @PathVariable("subjectId") @Subject String subjectId,
		final @ApiParam(value = "Overwrite existing data")
		@RequestParam(value = "overwrite", required = false, defaultValue = "false") boolean overwrite)
		throws PluginException
	{
		UserI user = getSessionUser();
		Security.checkProject(user, projectId);

		// Prevent starting the generation process if another one is already running
		if (!genAllDwDataLock.tryLock())
		{
			return new ResponseEntity<>(HttpStatus.LOCKED);
		}
		HttpStatus status;
		try
		{
			log.info("Subject "+subjectId+" DICOMweb data creation requested");
			status = generateSubjectDwData(user, projectId, subjectId, overwrite);
			log.info("Subject "+subjectId+" DICOMweb data creation complete");
		}
		finally
		{
			genAllDwDataLock.unlock();
		}
		return new ResponseEntity<>(status);
	}

	/*
	############################################################
		QIDO-RS
	############################################################
	*/

	@ApiOperation(value = "Search for studies.")
	@QidoApiResponses
	@QidoRequestMapping(value = "aets/{projectId}/{experimentId}/rs/studies")
	public ResponseEntity<StreamingResponseBody> searchForStudies(
		final HttpServletRequest request,
		final @ApiParam(value = "Project ID") @PathVariable("projectId") @Project String projectId,
		final @ApiParam(value = "Experiment ID") @PathVariable("experimentId") @Experiment String experimentId,
		final @ApiParam(value = "Query Parameters") @RequestParam MultiValueMap<String,String> queryParams)
		throws PluginException
	{
		Map<String,String> xnatIds =
			validateAndCheckPermissions(projectId, experimentId);

		return qidoRsService.search(xnatIds, request, queryParams,
			QidoRsModel.STUDY, null, null, QIDO.STUDY);
	}

	@ApiOperation(value = "Search for series.")
	@QidoApiResponses
	@QidoRequestMapping(value = "aets/{projectId}/{experimentId}/rs/series")
	public ResponseEntity<StreamingResponseBody> searchForSeries(
		final HttpServletRequest request,
		final @ApiParam(value = "Project ID") @PathVariable("projectId") @Project String projectId,
		final @ApiParam(value = "Experiment ID") @PathVariable("experimentId") @Experiment String experimentId,
		final @ApiParam(value = "Query Parameters") @RequestParam MultiValueMap<String,String> queryParams)
		throws PluginException
	{
		Map<String,String> xnatIds =
			validateAndCheckPermissions(projectId, experimentId);

		return qidoRsService.search(xnatIds, request, queryParams,
			QidoRsModel.SERIES, null, null, QIDO.STUDY_SERIES);
	}

	@ApiOperation(value = "Search for series of a study.")
	@QidoApiResponses
	@QidoRequestMapping(value = "aets/{projectId}/{experimentId}/rs/studies/{studyUID}/series")
	public ResponseEntity<StreamingResponseBody> searchForSeriesOfStudy(
		final HttpServletRequest request,
		final @ApiParam(value = "Project ID") @PathVariable("projectId") @Project String projectId,
		final @ApiParam(value = "Experiment ID") @PathVariable("experimentId") @Experiment String experimentId,
		final @ApiParam(value = "Study Instance UID") @PathVariable("studyUID") String studyUID,
		final @ApiParam(value = "Query Parameters") @RequestParam MultiValueMap<String,String> queryParams)
		throws PluginException
	{
		Map<String,String> xnatIds =
			validateAndCheckPermissions(projectId, experimentId);

		return qidoRsService.search(xnatIds, request, queryParams,
			QidoRsModel.SERIES, studyUID, null, QIDO.SERIES);
	}

	@ApiOperation(value = "Search for instances.")
	@QidoApiResponses
	@QidoRequestMapping(value = "aets/{projectId}/{experimentId}/rs/instances")
	public ResponseEntity<StreamingResponseBody> searchForInstances(
		final HttpServletRequest request,
		final @ApiParam(value = "Project ID") @PathVariable("projectId") @Project String projectId,
		final @ApiParam(value = "Experiment ID") @PathVariable("experimentId") @Experiment String experimentId,
		final @ApiParam(value = "Query Parameters") @RequestParam MultiValueMap<String,String> queryParams)
		throws PluginException
	{
		Map<String,String> xnatIds =
			validateAndCheckPermissions(projectId, experimentId);

		return qidoRsService.search(xnatIds, request, queryParams,
			QidoRsModel.INSTANCE, null, null, QIDO.STUDY_SERIES_INSTANCE);
	}

	@ApiOperation(value = "Search for instances of a study.")
	@QidoApiResponses
	@QidoRequestMapping(value = "aets/{projectId}/{experimentId}/rs/studies/{studyUID}/instances")
	public ResponseEntity<StreamingResponseBody> searchForInstancesOfStudy(
		final HttpServletRequest request,
		final @ApiParam(value = "Project ID") @PathVariable("projectId") @Project String projectId,
		final @ApiParam(value = "Experiment ID") @PathVariable("experimentId") @Experiment String experimentId,
		final @ApiParam(value = "Study Instance UID") @PathVariable("studyUID") String studyUID,
		final @ApiParam(value = "Query Parameters") @RequestParam MultiValueMap<String,String> queryParams)
		throws PluginException
	{
		Map<String,String> xnatIds =
			validateAndCheckPermissions(projectId, experimentId);

		return qidoRsService.search(xnatIds, request, queryParams,
			QidoRsModel.INSTANCE, studyUID, null, QIDO.SERIES_INSTANCE);
	}

	@ApiOperation(value = "Search for instances of a series.")
	@QidoApiResponses
	@QidoRequestMapping(value = "aets/{projectId}/{experimentId}/rs/studies/{studyUID}/series/{seriesUID}/instances")
	public ResponseEntity<StreamingResponseBody> searchForInstancesOfSeries(
		final HttpServletRequest request,
		final @ApiParam(value = "Project ID") @PathVariable("projectId") @Project String projectId,
		final @ApiParam(value = "Experiment ID") @PathVariable("experimentId") @Experiment String experimentId,
		final @ApiParam(value = "Study Instance UID") @PathVariable("studyUID") String studyUID,
		final @ApiParam(value = "Series Instance UID") @PathVariable("seriesUID") String seriesUID,
		final @ApiParam(value = "Query Parameters") @RequestParam MultiValueMap<String,String> queryParams)
		throws PluginException
	{
		Map<String,String> xnatIds =
			validateAndCheckPermissions(projectId, experimentId);

		return qidoRsService.search(xnatIds, request, queryParams,
			QidoRsModel.INSTANCE, studyUID, seriesUID, QIDO.INSTANCE);
	}

	/*
	############################################################
		WADO-RS
	############################################################
	*/

	@ApiOperation(value = "Retrieve study.")
	@WadoApiResponses
	@WadoInstancesRequestMapping(
		value = "aets/{projectId}/{experimentId}/rs/studies/{studyUID}")
	public DeferredResult<ResponseEntity<StreamingResponseBody>> retrieveStudy(
		final HttpServletRequest request,
		final @ApiParam(value = "Project ID") @PathVariable("projectId") @Project String projectId,
		final @ApiParam(value = "Experiment ID") @PathVariable("experimentId") @Experiment String experimentId,
		final @ApiParam(value = "Study Instance UID") @PathVariable("studyUID") String studyUID,
		final @ApiParam(value = "Query Parameters") @RequestParam MultiValueMap<String,String> queryParams)
		throws PluginException
	{
		Map<String,String> xnatIds =
			validateAndCheckPermissions(projectId, experimentId);

		return wadoRsService.retrieve(xnatIds, request, queryParams,
			WadoRsTarget.Study, studyUID, null, null,
			null, null);
	}

	@ApiOperation(value = "Retrieve study metadata.")
	@WadoApiResponses
	@WadoMetadataRequestMapping(
		value = "aets/{projectId}/{experimentId}/rs/studies/{studyUID}/metadata")
	public DeferredResult<ResponseEntity<StreamingResponseBody>> retrieveStudyMetadata(
		final HttpServletRequest request,
		final @ApiParam(value = "Project ID") @PathVariable("projectId") @Project String projectId,
		final @ApiParam(value = "Experiment ID") @PathVariable("experimentId") @Experiment String experimentId,
		final @ApiParam(value = "Study Instance UID") @PathVariable("studyUID") String studyUID,
		final @ApiParam(value = "Query Parameters") @RequestParam MultiValueMap<String,String> queryParams)
		throws PluginException
	{
		Map<String,String> xnatIds =
			validateAndCheckPermissions(projectId, experimentId);

		return wadoRsService.retrieve(xnatIds, request, queryParams,
			WadoRsTarget.StudyMetadata, studyUID, null, null,
			null, null);
	}

	@ApiOperation(value = "Retrieve series.")
	@WadoApiResponses
	@WadoInstancesRequestMapping(
		value = "aets/{projectId}/{experimentId}/rs/studies/{studyUID}/series/{seriesUID}")
	public DeferredResult<ResponseEntity<StreamingResponseBody>> retrieveSeries(
		final HttpServletRequest request,
		final @ApiParam(value = "Project ID") @PathVariable("projectId") @Project String projectId,
		final @ApiParam(value = "Experiment ID") @PathVariable("experimentId") @Experiment String experimentId,
		final @ApiParam(value = "Study Instance UID") @PathVariable("studyUID") String studyUID,
		final @ApiParam(value = "Series Instance UID") @PathVariable("seriesUID") String seriesUID,
		final @ApiParam(value = "Query Parameters") @RequestParam MultiValueMap<String,String> queryParams)
		throws PluginException
	{
		Map<String,String> xnatIds =
			validateAndCheckPermissions(projectId, experimentId);

		return wadoRsService.retrieve(xnatIds, request, queryParams,
			WadoRsTarget.Series, studyUID, seriesUID, null,
			null, null);
	}

	@ApiOperation(value = "Retrieve series metadata.")
	@WadoApiResponses
	@WadoMetadataRequestMapping(
		value = "aets/{projectId}/{experimentId}/rs/studies/{studyUID}/series/{seriesUID}/metadata")
	public DeferredResult<ResponseEntity<StreamingResponseBody>> retrieveSeriesMetadata(
		final HttpServletRequest request,
		final @ApiParam(value = "Project ID") @PathVariable("projectId") @Project String projectId,
		final @ApiParam(value = "Experiment ID") @PathVariable("experimentId") @Experiment String experimentId,
		final @ApiParam(value = "Study Instance UID") @PathVariable("studyUID") String studyUID,
		final @ApiParam(value = "Series Instance UID") @PathVariable("seriesUID") String seriesUID,
		final @ApiParam(value = "Query Parameters") @RequestParam MultiValueMap<String,String> queryParams)
		throws PluginException
	{
		Map<String,String> xnatIds =
			validateAndCheckPermissions(projectId, experimentId);

		return wadoRsService.retrieve(xnatIds, request, queryParams,
			WadoRsTarget.SeriesMetadata, studyUID, seriesUID, null,
			null, null);
	}

	@ApiOperation(value = "Retrieve instance.")
	@WadoApiResponses
	@WadoInstancesRequestMapping(
		value = "aets/{projectId}/{experimentId}/rs/studies/{studyUID}/series/{seriesUID}/instances/{sopUID}")
	public DeferredResult<ResponseEntity<StreamingResponseBody>> retrieveInstance(
		final HttpServletRequest request,
		final @ApiParam(value = "Project ID") @PathVariable("projectId") @Project String projectId,
		final @ApiParam(value = "Experiment ID") @PathVariable("experimentId") @Experiment String experimentId,
		final @ApiParam(value = "Study Instance UID") @PathVariable("studyUID") String studyUID,
		final @ApiParam(value = "Series Instance UID") @PathVariable("seriesUID") String seriesUID,
		final @ApiParam(value = "SOP Instance UID") @PathVariable("sopUID") String sopUID,
		final @ApiParam(value = "Query Parameters") @RequestParam MultiValueMap<String,String> queryParams)
		throws PluginException
	{
		Map<String,String> xnatIds =
			validateAndCheckPermissions(projectId, experimentId);

		return wadoRsService.retrieve(xnatIds, request, queryParams,
			WadoRsTarget.Instance, studyUID, seriesUID, sopUID,
			null, null);
	}

	@ApiOperation(value = "Retrieve instance metadata.")
	@WadoApiResponses
	@WadoMetadataRequestMapping(
		value = "aets/{projectId}/{experimentId}/rs/studies/{studyUID}/series/{seriesUID}/instances/{sopUID}/metadata")
	public DeferredResult<ResponseEntity<StreamingResponseBody>> retrieveInstanceMetadata(
		final HttpServletRequest request,
		final @ApiParam(value = "Project ID") @PathVariable("projectId") @Project String projectId,
		final @ApiParam(value = "Experiment ID") @PathVariable("experimentId") @Experiment String experimentId,
		final @ApiParam(value = "Study Instance UID") @PathVariable("studyUID") String studyUID,
		final @ApiParam(value = "Series Instance UID") @PathVariable("seriesUID") String seriesUID,
		final @ApiParam(value = "SOP Instance UID") @PathVariable("sopUID") String sopUID,
		final @ApiParam(value = "Query Parameters") @RequestParam MultiValueMap<String,String> queryParams)
		throws PluginException
	{
		Map<String,String> xnatIds =
			validateAndCheckPermissions(projectId, experimentId);

		return wadoRsService.retrieve(xnatIds, request, queryParams,
			WadoRsTarget.InstanceMetadata, studyUID, seriesUID, sopUID,
			null, null);
	}

	@ApiOperation(value = "Retrieve bulkdata.")
	@WadoApiResponses
	@WadoBulkdataRequestMapping(
		value = "aets/{projectId}/{experimentId}/rs/studies/{studyUID}/series/{seriesUID}/instances/{sopUID}/bulkdata/**")
	public DeferredResult<ResponseEntity<StreamingResponseBody>> retrieveBulkdata(
		final HttpServletRequest request,
		final @ApiParam(value = "Project ID") @PathVariable("projectId") @Project String projectId,
		final @ApiParam(value = "Experiment ID") @PathVariable("experimentId") @Experiment String experimentId,
		final @ApiParam(value = "Study Instance UID") @PathVariable("studyUID") String studyUID,
		final @ApiParam(value = "Series Instance UID") @PathVariable("seriesUID") String seriesUID,
		final @ApiParam(value = "SOP Instance UID") @PathVariable("sopUID") String sopUID,
		final @ApiParam(value = "Query Parameters") @RequestParam MultiValueMap<String,String> queryParams)
		throws PluginException
	{
		Map<String,String> xnatIds =
			validateAndCheckPermissions(projectId, experimentId);

		// Fallback for the attributePath part
		String[] fallback = request.getRequestURI().split("/bulkdata/");
		if (fallback.length != 2)
		{
			throw new PluginException("Invalid attribute path",
				PluginCode.HttpBadRequest);
		}

		// Format: "{dicomTag}/{itemIndex}/{AttributePath}"
		String attributePath = fallback[1];

		return wadoRsService.retrieve(xnatIds, request, queryParams,
			WadoRsTarget.Bulkdata, studyUID, seriesUID, sopUID,
			null, new AttributePath(attributePath).path);
	}

	@ApiOperation(value = "Retrieve frames.")
	@WadoApiResponses
	@WadoFrameRequestMapping(
		value = "aets/{projectId}/{experimentId}/rs/studies/{studyUID}/series/{seriesUID}/instances/{sopUID}/frames/{frameList}")
	public DeferredResult<ResponseEntity<StreamingResponseBody>> retrieveFrames(
		final HttpServletRequest request,
		final @ApiParam(value = "Project ID") @PathVariable("projectId") @Project String projectId,
		final @ApiParam(value = "Experiment ID") @PathVariable("experimentId") @Experiment String experimentId,
		final @ApiParam(value = "Study Instance UID") @PathVariable("studyUID") String studyUID,
		final @ApiParam(value = "Series Instance UID") @PathVariable("seriesUID") String seriesUID,
		final @ApiParam(value = "SOP Instance UID") @PathVariable("sopUID") String sopUID,
		final @ApiParam(
			value = "List of one or more frame numbers (comma-separated and non-duplicate)"
		) @PathVariable("frameList") String frameList,
		final @ApiParam(value = "Query Parameters") @RequestParam MultiValueMap<String,String> queryParams)
		throws PluginException
	{
		Map<String,String> xnatIds =
			validateAndCheckPermissions(projectId, experimentId);

		return wadoRsService.retrieve(xnatIds, request, queryParams,
			WadoRsTarget.Frame, studyUID, seriesUID, sopUID,
			new FrameList(frameList).frames, null);
	}

	/*
	############################################################
		Helper methods
	############################################################
	*/

	private XnatImagesessiondata checkPermissions(UserI user, String projectId,
		String experimentId, String... permissions) throws PluginException
	{
		Security.checkProject(user, projectId);
		Security.checkSession(user, experimentId);
		XnatImagesessiondata sessionData = PluginUtils.getImageSessionData(
			experimentId, user);
		Security.checkPermissions(user, sessionData.getXSIType() + "/project",
			projectId, permissions);

		if (!PluginUtils.isSharedIntoProject(sessionData, projectId))
		{
			throw new PluginException(
				"Experiment " + experimentId + " is not part of Project " + projectId,
				PluginCode.HttpNotFound);
		}

		return sessionData;
	}

	private HttpStatus generateAllDwData(boolean overwriteExisting)
		throws PluginException
	{
		UserI user = getSessionUser();
		List<XnatExperimentdata> experiments =
			XnatExperimentdata.getAllXnatExperimentdatas(user, true);
		List<String> exptIds = getImageSessionIds(experiments);
		return generateDwData(user, exptIds, overwriteExisting);
	}

	private HttpStatus generateDwData(UserI user, List<String> exptIds,
		boolean overwriteExisting) throws PluginException
	{
		// Use multithreading, if available
		int numThreads = Runtime.getRuntime().availableProcessors();
		numThreads = Math.min(numThreads, 4);
		log.info("Thread count for parallel DICOMweb data creation: " + numThreads);
		ExecutorService service = Executors.newFixedThreadPool(numThreads);

		List<Callable<Void>> tasks = new ArrayList<>();
		for (String id : exptIds)
		{
			log.info("ImageSession ID: "+id);
			tasks.add((Callable<Void>) () ->
			{
				dwInputHandler.createDicomwebData(id, user, overwriteExisting);
				return null;
			});
		}
		try
		{
			service.invokeAll(tasks);
		}
		catch (InterruptedException ex)
		{
			throw new PluginException(
				"DICOMweb data creation interrupted: "+ex.getMessage(),
				PluginCode.HttpInternalError, ex);
		}
		finally
		{
			service.shutdown();
		}
		return HttpStatus.CREATED;
	}

	private HttpStatus generateProjectDwData(UserI user, String projectId,
		boolean overwriteExisting) throws PluginException
	{
		XnatProjectdata projectData = XnatProjectdata.getProjectByIDorAlias(
			projectId, user, false);
		List<String> exptIds = getImageSessionIds(projectData.getExperiments());
		return generateDwData(user, exptIds, overwriteExisting);
	}

	private HttpStatus generateSubjectDwData(UserI user, String projectId,
		String subjectId, boolean overwriteExisting) throws PluginException
	{
		XnatSubjectdata subjectData = XnatSubjectdata.getXnatSubjectdatasById(
			subjectId, user, true);
		if (!subjectData.getProject().equals(projectId))
		{
			throw new PluginException(
				"Subject "+subjectId+" not found in project "+projectId,
				PluginCode.HttpUnprocessableEntity);
		}
		List<String> exptIds = new ArrayList<>();
		for (XnatSubjectassessordataI assessorData :
			subjectData.getExperiments_experiment())
		{
			if (assessorData instanceof XnatImagesessiondata)
			{
				exptIds.add(assessorData.getId());
			}
		}
		return generateDwData(user, exptIds, overwriteExisting);
	}

	private List<String> getImageSessionIds(List<XnatExperimentdata> experiments)
	{
		List<String> exptIds = new ArrayList<>();
		for (XnatExperimentdata experimentData : experiments)
		{
			if (experimentData instanceof XnatImagesessiondata)
			{
				exptIds.add(experimentData.getId());
			}
		}
		return exptIds;
	}

	private Map<String,String> validateAndCheckPermissions(String projectId,
		String experimentId) throws PluginException
	{
		UserI user = getSessionUser();

		XnatImagesessiondata sessionData = checkPermissions(user, projectId,
			experimentId, Security.Read);

		boolean isSharedProject = !sessionData.getProject().equals(projectId);

		return DicomwebUtils.getXnatIds(sessionData,
			isSharedProject ? projectId : null);
	}

	public static final class AttributePath {
		final int[] path;

		public AttributePath(String s) {
			String[] split = StringUtils.split(s, '/');
			if ((split.length & 1) == 0)
			{
				throw new IllegalArgumentException(s);
			}

			int[] path = new int[split.length];
			for (int i = 0; i < split.length; i++)
			{
				path[i] = Integer.parseInt(split[i], (i & 1) == 0 ? 16 : 10);
			}
			this.path = path;
		}
	}

	public static class FrameList {
		final int[] frames;

		public FrameList(String s) {
			String[] split = StringUtils.split(s, ',');
			int[] frames = new int[split.length];
			for (int i = 0; i < split.length; i++) {
				if ((frames[i] = Integer.parseInt(split[i])) <= 0)
					throw new IllegalArgumentException(s);
			}
			this.frames = frames;
		}
	}
}
