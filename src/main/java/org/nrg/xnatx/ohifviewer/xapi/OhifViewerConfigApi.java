/*********************************************************************
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
package org.nrg.xnatx.ohifviewer.xapi;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import icr.etherj.StringUtils;
import io.swagger.annotations.*;
import org.nrg.config.entities.Configuration;
import org.nrg.config.services.ConfigService;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.xapi.rest.AbstractXapiRestController;
import org.nrg.xapi.rest.Project;
import org.nrg.xapi.rest.XapiRequestMapping;
import org.nrg.xdat.security.helpers.AccessLevel;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xft.security.UserI;
import org.nrg.xnatx.plugin.PluginCode;
import org.nrg.xnatx.plugin.PluginException;
import org.nrg.xnatx.plugin.Security;
import org.nrg.xnatx.roi.Constants;
import org.nrg.xnatx.ohifviewer.JsonRoiPresetstHandler;
import org.nrg.xnatx.ohifviewer.data.RoiPreset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.naming.InvalidNameException;
import java.io.IOException;
import java.util.*;

/**
 *
 * @author mo.alsad
 */
@Api(description="OHIF Viewer Config API")
@XapiRestController
@RequestMapping(value = "/viewerConfig")
public class OhifViewerConfigApi extends AbstractXapiRestController
{
    private static final Logger logger = LoggerFactory.getLogger(
            OhifViewerConfigApi.class);

    private final String[] allowableRoiTypes = {Constants.AIM,
        Constants.Segmentation, Constants.Measurement};
    private final JsonRoiPresetstHandler roiPresetsJsonHandler;

    protected OhifViewerConfigApi(final ConfigService configService,
        final UserManagementServiceI userManagementService,
        final RoleHolder roleHolder)
    {
        super(userManagementService, roleHolder);
        roiPresetsJsonHandler = new JsonRoiPresetstHandler(configService);
        logger.info("OHIF Viewer Config XAPI initialised");
    }

    @ApiOperation(value = "Returns the ROI Presets for the specified project and ROI type.")
    @ApiResponses(
    {
        @ApiResponse(code = 200, message = "The project was located and JSON ROI Presets returned."),
        @ApiResponse(code = 403, message = "The user does not have permission to view the indicated project."),
        @ApiResponse(code = 422, message = "Unprocessable request"),
        @ApiResponse(code = 500, message = "An unexpected error occurred.")
    })
    @XapiRequestMapping(
        value = "projects/{projectId}/roipreset",
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.GET,
        restrictTo = AccessLevel.Read
    )
    @ResponseBody
    public ResponseEntity<Map<String, List<RoiPreset>>>  getRoiPresets(
        final @ApiParam(value="Project ID") @PathVariable("projectId") @Project String projectId,
        final @ApiParam(value="Type", allowableValues="AIM,SEG,MEAS") @RequestParam(value="type", required=false, defaultValue="") String type)
        throws PluginException
    {
        // Set default type to "All" unless it is specified in the request
        String roiType = "ALL";
        if (!StringUtils.isNullOrEmpty(type)) {
            roiType = type.toUpperCase();
        }
        UserI user = getSessionUser();
        if (logger.isDebugEnabled())
        {
            logger.debug("GET /viewerConfig/projects/"+projectId+"/roipreset"+
                " roiType="+roiType+
                " by user "+user.getUsername());
        }
        Security.checkProject(user, projectId);
        Map<String, Configuration> projectConfigMap = getProjectConfigMap(projectId, roiType);
        return buildResponseFromConfigMap(projectConfigMap);
    }

    @ApiOperation(value = "Sets the ROI Presets for the specified project and ROI type.")
    @ApiResponses(
    {
        @ApiResponse(code = 200, message = "The project was located and JSON ROI Presets were stored."),
        @ApiResponse(code = 403, message = "The user does not have permission to access the resource."),
        @ApiResponse(code = 422, message = "Unprocessable request"),
        @ApiResponse(code = 500, message = "An unexpected error occurred.")
    })
    @XapiRequestMapping(
        value = "projects/{projectId}/roipreset",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.PUT,
        restrictTo = AccessLevel.Admin
    )
    @ResponseBody
    public ResponseEntity<String> setRoiPresets(
        final @ApiParam(value="Project ID") @PathVariable("projectId") @Project String projectId,
        final @ApiParam(value="Type", allowableValues="AIM,SEG,MEAS") @RequestParam(value="type", required=true) String type,
        @RequestBody List<RoiPreset> roiPresetList)
        throws PluginException
    {
        if (StringUtils.isNullOrEmpty(type) ||
                !Arrays.asList(allowableRoiTypes).contains(type))
        {
            throw new PluginException(
                "Collection type "+type+" not supported.",
                PluginCode.HttpUnprocessableEntity);
        }
        UserI user = getSessionUser();
        Security.checkProject(user, projectId);
        if (logger.isDebugEnabled())
        {
            logger.debug("PUT /viewerConfig/projects/"+projectId+"/roipreset"+
                " roiType="+type+
                " by user "+user.getUsername());
        }
        String json = parseRoiPresetList(roiPresetList);
        roiPresetsJsonHandler.setProjectJsonConfig(user, projectId, type, json);
        return new ResponseEntity<>("Updated "+type+" ROI presets for project "+projectId,
                HttpStatus.OK);
    }

    private ResponseEntity<Map<String, List<RoiPreset>>> buildResponseFromConfigMap(
        Map<String, Configuration> configMap)
        throws PluginException
    {
        Map<String, List<RoiPreset>> roiPresetListMap = new HashMap<>();
        try
        {
            ObjectMapper mapper = new ObjectMapper();
            CollectionType javaType = mapper.getTypeFactory()
                .constructCollectionType(List.class, RoiPreset.class);
            for (String key : configMap.keySet())
            {
                Configuration config = configMap.get(key);
                if (config == null)
                {
                    roiPresetListMap.put(key, new ArrayList<>());
                    continue;
                }
                String configContents = config.getContents();
                if (StringUtils.isNullOrEmpty(configContents))
                {
                    roiPresetListMap.put(key, new ArrayList<>());
                    continue;
                }
                List<RoiPreset> roiPresetList = mapper.readValue(configContents, javaType);
                roiPresetListMap.put(key, roiPresetList);
            }
        }
        catch (IOException ex)
        {
            throw new PluginException("Error creating JSON: "+ex.getMessage(),
                    PluginCode.HttpInternalError, ex);
        }
        return ResponseEntity.ok(roiPresetListMap);
    }

    private Map<String, Configuration> getProjectConfigMap(String projectId, String roiType)
        throws PluginException
    {
        Map<String, Configuration> projectConfigMap = new HashMap<>();
        switch (roiType)
        {
            case Constants.AIM:
            case Constants.Segmentation:
            case Constants.Measurement:
                projectConfigMap.put(roiType,
                    roiPresetsJsonHandler.getProjectJsonConfig(projectId, roiType));
                break;
            case "ALL":
                for (String type: allowableRoiTypes)
                {
                    projectConfigMap.put(type,
                        roiPresetsJsonHandler.getProjectJsonConfig(projectId, type));
                }
                break;
            default:
                throw new PluginException("Allowable ROI types: [AIM,SEG,MEAS]",
                    PluginCode.HttpUnprocessableEntity);
        }

        return projectConfigMap;
    }

    private String parseRoiPresetList(List<RoiPreset> roiPresetList) throws PluginException
    {
        // Remove duplicates from the list
        Set<RoiPreset> presetSet = new HashSet<>(roiPresetList);
        roiPresetList.clear();
        roiPresetList.addAll(presetSet);

        // Sort by label
        roiPresetList.sort((o1, o2) -> o1.getLabel().compareTo(o2.getLabel()));

        ObjectMapper mapper = new ObjectMapper();
        String json;
        try
        {
            for (RoiPreset roiPreset : roiPresetList)
            {
                // Check ROI label
                String label = roiPreset.getLabel();
                if (StringUtils.isNullOrEmpty(label)) {
                    throw new InvalidNameException("ROI label cannot be empty.");
                }
                else if (label.length() > 64) {
                    throw new InvalidNameException("Label length cannot be more than 64 characters.");
                }
                // Check ROI color
                // ROI color is set to [0,0,0] if invalid input was provided
            }
            json = mapper.writeValueAsString(roiPresetList);
        }
        catch (InvalidNameException ex)
        {
            throw new PluginException("Error creating JSON: "+ex.getMessage(),
                    PluginCode.IllegalArgument, ex);
        } catch (JsonProcessingException ex)
        {
            throw new PluginException("Error creating JSON: "+ex.getMessage(),
                    PluginCode.HttpUnprocessableEntity, ex);
        }
        return json;
    }
}
