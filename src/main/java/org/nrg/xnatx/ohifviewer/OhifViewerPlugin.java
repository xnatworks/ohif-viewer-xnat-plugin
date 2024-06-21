package org.nrg.xnatx.ohifviewer;

import org.nrg.framework.annotations.XnatDataModel;
import org.nrg.framework.annotations.XnatPlugin;
import org.springframework.context.annotation.ComponentScan;

@XnatPlugin(
	value = "ohifViewerPlugin",
	name = "XNAT OHIF Viewer Plugin",
	version = "3.7.0-RC.2",
	description = "Integrates the OHIF Cornerstone Viewer into XNAT.",
	logConfigurationFile="/ohifviewer-logback.xml",
	dataModels={
		@XnatDataModel(
			value="icr:roiCollectionData",
			singular = "ROI Collection",
			plural = "ROI Collections",
			code = "ROIC")
	},
	entityPackages={"icr.xnat.plugin.roi.entity",
		"org.nrg.xnatx.ohifviewer.entity",
		"org.nrg.xnatx.dicomweb.entity"},
	openUrls = {"/viewer.html", "/*.js", "/*.stats.json", "/*.css", "/packages/**/*", "/sockjs/**/*"})
@ComponentScan({
	"org.nrg.xnatx.ohifviewer.data",
	"org.nrg.xnatx.ohifviewer.xapi",
	"org.nrg.xnatx.ohifviewer.event.listeners",
	"org.nrg.xnatx.ohifviewer.init",
	"org.nrg.xnatx.ohifviewer.service",
	"org.nrg.xnatx.ohifviewer.inputcreator",
	"org.nrg.xnatx.roi.data",
	"org.nrg.xnatx.roi.event.listeners",
	"org.nrg.xnatx.roi.service",
	"org.nrg.xnatx.roi.xapi",
 	// ICR DICOMweb
	"org.nrg.xnatx.dicomweb.dao",
	"org.nrg.xnatx.dicomweb.service",
	"org.nrg.xnatx.dicomweb.xapi"})
public class OhifViewerPlugin
{}
