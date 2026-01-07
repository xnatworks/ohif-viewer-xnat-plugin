**3.7.2**

- Added keyboard shortcuts to browse stacks in a multi-stack scan.
- Prioritise the image plane information if provided in the root metadata.
- Fixed invalid order and dimension indices of multi-stack (4D) caused by non-compliant metadata.
- Fixed nested value of the PatientName tag in measurements.

**3.7.1**

- Dynamic retrieval of viewer settings from XNAT.
- Improved the processing of the Enhanced MR images so that it becomes possible to:
  - detect irregular spacing and missing frames,
  - calculate the volume of contour and mask ROIs, and
  - jump to frames relevant to an ROI.
- Refactor the annotations API and UI components to accommodate the multi-stack configuration.
- Fixed the import and export of masks with Enhanced MR as a reference dataset.
- Fixed metadata configuration for multi-frame images, allowing for mask import/export.
- Fixed a bug causing the sculptor and contour drawing tools to freeze.
- Fixed the rotation of the pathology slide preview in the thumbnail images.
- Fixed scan brightness and contrast in 3D MPR.

**3.7.0**

- Supporting DICOM Microscopy images as per the prerelease notes listed below.
- Multiple bug fixes and improvements.

**3.7.0-RC-3**

- Fixed typo (missing application) in the octet-stream media type.
- Fixed the freehand3DTool becoming unresponsive when deleting a contour while drawing.
- Fixed dependencies related to Node.js modules with Webpack 4.
  
**3.7.0-RC-2**

- Upload snapshot images to XNAT session resources.

**3.7.0-RC-1**

- Partial support for images with multiple focal planes.
    - If provided, the extended-depth-of-field (focus stacking) images are displayed.
    - Otherwise, the viewer will try to display the first focal plane.
- Slide information panel providing the following features:
    - View the slide label image (when available).
    - Switching the visibility between provided channels (optical paths).

**3.7.0-BETA-1**

- Added DICOMweb data handlers to the XNAT session events. DICOMweb data is automatically created for new sessions and also removed upon deleting a session.
- DICOMweb data generation at subject, project, and all site levels.
- Refactored the XNAT UI scripts with the JavaScript Promise API and included support for the DICOMweb-based sessions.
- Provide detailed error messages from the microscopy viewer.

**3.7.0-ALPHA-1**

- Implemented a customised support for the DICOMweb QIDO and WADO services.
- Integrated dicom-microscopy-viewer v0.45.1.

**3.6.3**

- Fixed the image position synchroniser for scans with different slice thickness.

**3.6.2**

- Search both DICOM and secondary catalogs for SOP instance UIDs for ROI collection dependency checks.
- Support 4-D datasets with duplicate InstanceNumber values.
- Additional dimension tags for the grouping of stacks in 4-D datasets.
- Dynamic window width calculation when the value is zero.
- Fixed issues preventing from parsing and displaying NM images.
- Fixed the detection of orientation upon importing segmentations.
- Fixed the middle frame calculation used for displaying the thumbnail images.
- Fixed irregular spacing wrongly detected as missing frames.

**3.6.1**

- Automated the detection of 4-D stack dimensions when creating multi-stack displays.
- Enabled the grouping of DICOM Enhanced images belonging to the same scan into a single Cluster.
- Display the number of study scans in the Scans panel.
- Simplified the data validation for enabling the 3D MPR mode.
- Fixed invalid VOI Attributes provided by the cornerstone-wado-image-loader.

**3.6.0**

- Minor improvements and bug fixes.

**3.6.0-RC-2**

- Support organising and displaying of 4-D datasets as multi-stack.

**3.6.0-RC-1**

- Feature: organise and display multi-stack frames into subgroups. Only Enhanced MR storage is currently supported.
- Feature: viewport options menu to declutter the display and reduce visual search and selection time.
- Feature: support 2D image fusion using cornerstone layers.
- Feature: toggle visibility of annotations (masks, contours & measurements) for individual viewports.
- Notify the user of metadata inconsistencies found in individual images.
- Added the capability to use false color images instead of colormaps in 2D image fusion.
- Make the dialog display optional for new measurements.
- Display fusion description and colormap name on the viewport.
- Security fix: remove the polyfill script tag from index.html.
- Fixed line intersection checks for the reference lines tool.

**3.5.3**

- Fixed rendering overlay data over thumbnail images.

**3.5.2**

- Ensure ROICollections return a non-null, non-empty string to use as a filename. Fixes issue 44.

**3.5.1**

- Added Viewer Config API (/viewerConfig). Currently, serves endpoints only to GET and PUT ROI Presets.
- Select ROI labels using a list populated from presets stored on XNAT at project-level.
- Display overlays in the scan thumbnail view for scans with overlay data.
- Deactivated the export of ROI Collections in cases of unsupported modalities or insufficient permission.
- Improved alert messages to clarify that viewer will automatically open, surface errors to user if metadata cannot be generated.
- Added scrollbar to the scan browser.
- Fixed a bug in obtaining XNAT user details for guest access.
- Fixed a bug preventing guests from loading the viewer.
- Fixed bug blocking json generation from subject page "View Images" link.

**3.5.0**

- ROI contour rendering in the 3D MPR mode.
- Volume calculation and display for contour and mask ROIs.
- Calculate and display the 2D statistics of mask ROIs.
- Refactored the ROI Collections export for shared projects configuration.
- Populate DICOM Equipment module values from the reference scan for storing ROI Collections.
- Display scan modality in the scan browser.
- Upgraded underlying JavaScript packages and tools.
- Fixed measurement import/export issue for multiframe images.
- Fixed issues caused when using Node.js v16.14.0.
- Restrict NM image fusion only for image type 'RECON TOMO'.

**3.5.0 ALPHA-1**

- Improved session JSON creation, serialisation and retrieval processes.
  - JSON metadata minification with up to 50% size reduction.
  - Optimised CPU and memory utilisation.
- Populate instance-level fields that were dropped from the optimised JSON metadata.

**3.4.1**

- Fixed import and export issues of measurement data for DICOM multiframe images.
- Set values of the DICOM General Equipment tags based on the viewer attributes: manufacturer, model-name and software-versions.

**3.4.0**

- Enabled NVIDIA AIAA tools. Switch between AIAA or MONAILabel tools based on the backend configuration.
- Fixed label value to label name mapping in the MONAILabel client.

**3.4.0 RC-2**

- New settings option to load a scan from the middle rather than from the top slice.
- Added a progress indicator to monitor the loading progress of images. Currently, does not support multiframe images or images that were removed from cache.
- Show a dialog upon creating measurements to set name and description.
- Added support to set and restore presentation state parameters from measurements.
- Fixed issues caused when switching between the 3D MPR and the standard viewer modes.

**3.4.0 RC-1**

- Added server-side storage for measurement collection data.
- New feature: Measurement Service to manage and interact with measurement annotations.
  - Measurement panel: groups measurements into working and imported collections.
  - Export and import measurement collections to and from the XNAT platform.
  - Individual measurement interaction buttons: edit metadata, toggle visibility, jump to slice, and remove.
  - Measurement API to interface with measurements from other Viewer extensions.
- New feature: Client for MONAI Label server to facilitate interactive medical image annotation.
  - Interactively segment parts of an image using clicks (DeepGrow & DeepEdit models).
  - Fully automated annotation without user interaction (Segmentation models).
- Minor improvements and bug fixes.

**3.3.0**

- Removed events that should not trigger a JSON metadata rebuild. Only rebuild on certain events if project anonymization enabled and add support for a subject-level event.
- New feature: lazy loading of contour ROIs. The feature can be activated in preferences.
- New feature: concurrent loading of contour ROIs (optionally enabled in preferences).
- New feature: store and retrieve a project template for contour ROI colors. Added server-side storage of color template.
- New feature: switch between contour ROI color schemes - project template, from metadata, and custom values.
- Supported drawing/import/export of mask ROIs for the Ultrasound Image Storage 1.2.840.10008.5.1.4.1.1.6.1.
- Added UI controls to sort the contour ROI list by name. Applies only to the locked collections.
- Used automatically generated notation to identify scans with duplicate series number.
- Fixed contour ROI import/export for multiframe images.
- Fixed segmentation and mask tools for touch-based interaction.
- Fixed the rendering position of the contour sculpt tool.
- Switched to SVGR loader to enable custom title for icons.
- Removed unnecessary ITK modules to reduce the bundle size.
- Other minor improvements and bug fixes.

**3.2.0**

- Prevent regenerating session JSON on project sharing events.
- Set subject's ID/name as title in the browser tab.
- Use rescaled colormap for image fusion in the 3D MPR mode.
- Cache the ROI collection lists used in the study browser to reduce repeated API calls.
- Fixed manifest and service worker issues when served from a subfolder.
- Quick ROI imports via clicking on relevant icons in the study browser.
- Fixed thumbnail re-rendering issue.
- Fixed intensity scaling for images with variable window/level.
- Display 4D image thumbnail based on the subset image index.
- Added windowing info to the viewport overlay.
- Refactored smooth and sync components for improved interaction.
- Handle missing modality scaling parameters in the 3D MPR mode.

**3.2.0 RC-1**

- Added link to open viewer from XNAT search listings with admin configuration for default project listings.
- Multi-volume image fusion in 3D MPR mode.
- Improved support for DICOM Nuclear Medicine (NM) IOD.
- Two new tools: reference lines and crosshairs.
- Added orientation marker in MPR mode.
- Improved mask import for co-planar and perpendicular segmentation data.
- Added keyboard shortcuts for mask undo/redo.
- Fixed duplicate StudyInstanceUID issue.
- Refactored the mask settings menu for improved interaction.
- Upgraded the image loader to use WebAssembly for data decoding.
- Upgraded dependencies to recent applicable versions. 

**3.1.0**

- Fixed buffer size for reading NIfTI segmentation data.

**3.1.0 RC-3**

- Replaced frame number with instance number in the tag browser.
- Added DICOM File Meta Information to the Tag Browser.
- Fixed overlays not displaying issue.

**3.1.0 RC-2**

- Refactored the Tag Browser extension: bug fixes and showing all elements (for loaded images).
- Refactored a new class to read and align NIfTI segmentation data. Added support for cornerstonejs/nifti-image-loader.
- Handle errors when loading images and show notification with error message.
- Notify about ROI export export issues.
- Added a progress indicator for importing contour collections.
- Fixed a bug that prevented changing Window/Level preferences.
- Fixed slice order in image sets to match imageIndex with instanceNumber.  

**3.1.0 RC-1**

- Upgraded to v4.9.20 of the mainstream OHIF Viewer ([@ohif/viewer@4.9.20](https://github.com/OHIF/Viewers/releases/tag/%40ohif%2Fviewer%404.9.20)).
- Filter ROI collection import list based session label and scan number.
- DICOM tag browser added. Note: this is a new feature developed by the core OHIF team that we have added. It is still a work-in-progress and incomplete in terms of the tags that are included (notably, meta-elements, sequences and private tags are currently missing).
- Visual notifications added to warn about inconsistencies in scan data, e.g. frames have different dimensions.
- Highlight active scan in the Scan Browser panel, and show the number of available ROIs per scan.
- Added new segmentation tools: correction scissors and brush eraser.
- Toolbar buttons added to undo/redo manual segmentation.
- Support changing segmentation brush size when using touch screen devices.
- Smart & Auto segmentation brush: custom gate-separation value + retain the settings between viewer sessions.
- Show the number of slices for each segment and jump to the middle slice when value clicked.
- Toggle visibility of contour collections.
- Added support for 3D deepgrow in the NVIDIA AIAA Client (requires Clara v4.0+).
- Fixed the annotations ‘delete all’ function to remove currently displayed measurements.
- Fixed segmentation export issue when having multiple viewports.
- Fixed Ultrasound measurements to show values in physical units. Fix does not support SequenceOfUltrasoundRegions with a multiple entries.
- Fixed `imageId` issue that prevented scrolling through some multi-frame images.

**3.0**

- Fix event-triggered JSON metadata generation for session uploads.
- Relative URLs in JSON metadata allows XNAT's host name to change without requiring a metadata regeneration.
- ROICollection data type schema removed and integrated into `xnat-data-models`. Other plugins such as XSync can now use the data type.
- Stricter checking of session ID and ROICollection ID on overwrite.
- Finer grained JSON metadata regeneration XAPI calls added.
- JSON metadata moved into XNAT database. Existing JSON metadata files will be removed during metadata regeneration.
- Breaking change in XNAT 1.8.0 `deleteWithBackup()` method means 1.7.x and earlier no longer supported.
- Improved study and series UID checking for SEGs.
- JSON metadata format update to support OHIF 4.2.7 requirements. Regeneration of all metadata required, will happen automatically if admin doesn't manually intervene.
- 2D-only modalities supported. Contours can only be stored in AIM format, no RT-STRUCT will be generated.
- Add `xnat-xnat-viewer` repo as submodule. Add build script to allow single command build of whole plugin.
- Clean up build dependencies, limit what is included in fatjar. Greatly reduces jar size.
- Remove build outputs from repo.
- Merge `xnat-roi-plugin` into `ohif-xnat-viewer-plugin` to roll all functionality into a single jar file. Reduces confusion for users.

**2.0**
UX:
Update refresh to work on events such as click and touch. Can always log back into XNAT in another tab if it times out.

**2.0 RC-9**
Bug Fixes:
- Export default names for ROI Collections properly.

JSON Generation:
- Re-added endpoint for POST.

**2.0 RC-8**
UX:
- Multiframe images now fetch their snapshot from a cached XNAT resource, avoiding timeout issues with large multiframes.

**2.0 RC-7**
UX:
- Icons under a scan in the scan list to display whether there are any ROIs or segments on that scan. Really useful when you import one ROICollection onto a large Session.

**2.0 RC-6**
UI:
- Help menus have been updated.
- Correctly update the Contours side panel when a ROI Contour name is changed via the double-click-on-contour method.

**2.0 RC-5**
UX:
- The default ROICollection name on export is equal to the segment/contour name if there is only one, and blank otherwise, so some human readable label is enf$


**2.0 RC-4**
Bug Fixes:
- Fix multiple import edge-case bug with ROI Contours.
- Updated to new dcmjs version which fixes segmentation IO for images with frames of length not divisible by 8.

**2.0 RC-3**
Bug Fixes:
- Fixed scrolling of XNAT Nav bar introduced when moving from a React-in-Blaze to a React component.
- Opening a Contours or Segments side panel before ann enabled element has data does fails gracefully and displays an empty ROIContour/Segment list until changes are made/a series is loaded.
- Fixed imagePlane metadata extraction for multi-frame images where IOP/IPP is stored per frame.

UI/UX:
- Changed the 'View Images' button to 'View Legacy XImgView' to avoid confusion with OHIF. Once XNAT is running on OHIF 2.0 and has solid MPR, the old XImgViewer can be nuked.
- Middle/Right clicking 'View Session'/'View Subject' will open the viewer in a new tab/window depending on browser configuration.
- Whilst XNAT is performing the View Session checks, a spinner will display.

**2.0 RC-2**

UI/UX:
- Rebuilt all Segmentation/XNAT dialogs as React components ready for the new version. Render these as Blaze-encapsulated-react components for now.
- The top toolbar no longer has settings/IO functionality for contours and segmentations, only tools.
- The management/IO/Settings of ROIContours and Segmentations has been to the right sidebar.

**2.0-RC1**
UX
- Removed the ON/OFF tags from the ROI stats and interpolation toggles, as it wasn't clear if e.g. it is OFF, or clicking it will turn it off. It now highlights bright green when its on. (Happy for fee$

UI:
- Removed the binding for stack scroll to left click. Stack scroll can already be used whilst any tool is active by either the mouse wheel, left clicking on the scrollbar at the side of the image, the $
- The series list now can show full 64 character series descriptions and 10 digit series numbers cleanly, with line wrapping prioritising spaces, and otherwise linebreaking where needbe.
- Added a scrollbar to the series list, and styled the scrollbars for the two side windows to match the OHIF styling.

JSON Generation:
- Use the dicomweb protocol for everything, as it seems to have better support for multiframe images in cornerstoneWADOImageLoader.
- Expand the list of valid SopClassUIDs to include some imaging modalities I originally missed.

**1.16.0 Beta WIP**
XAPI: I note that all of this here is fallback incase JSON gets deleted for some reason. Automation should generate JSON on upload/transfer/deletion of scans, and the average user should never see any "Generating JSON" dialogs.
 - The OHIF viewer API now only has 3 end points, GET for JSON existance, JSON itself, and an admin-level POST to generate JSON for the whole database.
 - JSON is now generated within the GET code if it doesn't exist and cached for future usage. This means a user with only READ permissions to a session can trigger JSON generation if the session JSON doesn't exist.
 - The "exists" check is very quick, and can be used to check if the JSON generation will need to happen in the GET, so that you can display approiate "loading" UI.
 - "View Subject" from the XNAT UI now brings a dialog up tracking progress of JSON generation for each related session.

UX:
- When navigating to a Subject or Session view from the navbar that has missing JSON metadata, a request is made to generate this, and appropriate progress dialogs display until the data is generated. At which point the user is redirected.

UI:
- Make sure scrollbar for navbar sticks to the right.


**1.15.3 Beta**
UX:
- Produce a list of available ROICollections in the side nav bar.
- If you have unsaved annotations, get a confirmation from the user before switching scans.

**1.15.2 Beta**
Fixes:
- Updated DICOM SEG version to properly add ReferencedSOPClassUIDs to relevant lists. The individual segmentations now come up in the ROI list in the ROICollection view.

**1.15.1 Beta**
Features:
- Navigation from View Subject/View Session:
 - These buttons now open the viewer directly in the window, as this has much better support for mobile.

**1.15.0 Beta**
Features:
- Navigation Bar:
 - A navigation bar can now be accessed by opening the right-hand tab in the viewer.
 - The navigation bar allows you to switch to a different subject/session, allowing you to segment many scans without leaving the viewer.
 - If a subject/session is shared from another project, the projectId of its parent is also displayed.
 - Under "This Project", the current project is displayed, with the current subject/session in yellow.
 - You may navigate to other projects via the "Other Projects" menu.
 - The navigation bar fetches data on Projects/Subjects/Sessions as requested, providing a quick and responsive UI.
>>>>>>> xnat-roi-beta-1.17

**1.14.0 Beta**
Features:
- DICOM SEG improvements:
  - We now have support for DICOM Segmentation objects in a variety of possible configurations.
  - DICOM SEG export for both single frame and multiframe source data.
  - ROICollectionName baked into the generated DICOM SeriesDescription.
  - Only segmentation frames with at least one segment occupying them are now saved, reducing the filesize of small segmentations on large scans drastically.

- Freehand Improvements:
  - If you find yourself in a pickle whilst drawing, hitting delete will cleanly cancel your contour and allow you to start again.
  - Hitting escape whilst drawing will quickly close your contour.
  - Whilst drawing you can now zoom/pan with the right/middle mouse buttons respectively whilst drawing. So if you wish to zoom in part way through drawing your contour, you can.


**1.13.0 Beta**
Features:
- Subject level view:
  - On a Subject page you can now access a subject level viewer by clicking on "View Subject".
  - In the scan list there will be a collapsable list of scans for each Session. You can view scans from multiple sessions simultaneously.
  - When you annotate ROIs and export them back to XNAT, they will be stored under the appropriate session.
- ROICollection Page (Thanks @JamesDarcy616 !):
  - The default XNAT datatype page has been replaced with a customised ROI Collection page.
  - The ROI Collection resources, as well as the individual ROIs included in the collection are listed on the page.
  - The user can now use the manage files/delete functionality within the ROI Collection page.

**1.12.0 Beta**

UX:
- The csrfToken used to PUT RoiCollections is now dynamically fetched using the XNAT JSESSION cookie in the browser.
  - This means that the user can time out/disconnect, and log back into XNAT via the main XNAT interface, then push back from the viewer.
  - Users can E-Mail direct links to a viewer session. The user can log in, annotate, and push directly, without going through the regular 'View Session' route.

**1.11.4 Beta**

Bug Fix:
- Correctly extract imagePlane information for multiframe images.
- If a RoiCollection had a type of RTSTRUCT, then the RTSTRUCT resources is once again parsed and loaded instead of the AIM.

**1.11.3 Beta**
Bug Fix:
- Fixed the new routing mechanism introduced when the viewer transitioned away from an iframe, such that URLs are properly constructed for XNATs hosted on root.
- Support for RTSTRUCTs which map on to multi-frame data is still poorly supported. For now the AIM files will be read if they exist, since they are well supported for both single and multi-frame images.

**1.11.2 Beta**

Bug Fix:

- Account for the edge case where metadata has multiple studies.

**1.11.1 Beta**
UI:

- Use XNAT styled dilogs for alert prompts.

Bug Fix:

- Fix broken manual post of view metadata introduced in 1.11.0.

**1.11.0 Beta**
Features:
- RoiCollections shared into shared projects can now be loaded into the viewer.

UX:
- If a user in a shared project has write access to the parent project, the export button will show in the interface, and will push annotations back to the source project.

**1.10.0 Beta**
Features:
- Session shared into projects can now be viewed in the viewer. No annotation support for shared projects yet.
UI/UX:
- Fullscreen viewer. After some architectural changes to the viewer's router, the viewer no longer has to sit in an XNAT window to operate. The viewer now opens fullscreen in a separate tab instead of appearing in an iframe.

**1.9.2 Beta**

UX:
- Updated to latest version of cornerstoneTools.
  - Freehand spacing is based on image resolution, not canvas size/zoom.
  - Freehand sculpter is more efficient and clever. The max size is adapted and it only adds new points where they are needed to alter the geometry.
- When clicking on 'draw' or 'paint' for the first time on a new scan, the dialog for metadata input appears immediately, as opposed to requiring a click first.

**1.9.1 Beta**
UX:
- Improved select -> drag UX of the freehand sculpter.

Bug Fixes:
- Fix edge case of erroneous "export failed" message when the export actually succeeded.
- Fixed ctrl + click which broken in cornerstoneTools 3.0 (also pushed back to the cTools repo).

**1.9.0 Beta**

Features:
- NIFTI mask import. You can now import NIFTI-based RoiCollections. An uploader capable of uploading NIFTI will come in the future.
  - NOTE: There is no NIFTI export yet. If you edit a NIFTI mask you can save back as SEG for now.
  - NOTE: You can currently only view NIFTI masks that map onto DICOM images. A full NIFTI workflow is in the works.
- If you are drawing on an imported NIFTI mask, the brush mode is set to non-overlapping, i.e. drawing with one color overwrites another, and the mask data more closely resembles a NIFTI formatted file.
- You can now rotate and flip the image and the brush layer will react accordingly. You can paint the mask from any orientation.
- The client side backup feature has been disabled for now, as it needs a few optimisations to be useful for large NIFTI masks without significantly inhibiting UX. I will come back to this feature in the future and multithread it with webworkers.

**1.8.0 Beta**

Features:
- DICOM-SEG import and export. Saves as RoiCollections of type SEG. There is currently a hard cap of 20 segmentations per series, but this limit will be removed in future developments.
- Client side edge server. A big new feature is that Masks and contour based ROIs are backed up every minute in your browser. Should your browser crash, or your internet drop, etc. You will be given the option to recover this data when you return to view the session. The local backups are deleted if you choose not to restore the data, or when data is successfully pushed back to XNAT. No sensitive information is held on the client side database. Series instance UIDs are hashed using a one way hashing algorithm, and this hash is used to relate the data to a particular series.
- Reference lines back in! Feature parity with the old cornerstoneTools v2 version of the view.

UI:
- New UI to go along with DICOM-SEG support.
- Cleaned up and prettified some existing UI dialogs.

Bug-Fixes:
- Fixed passive cornerstoneTools functionality being broken in 1.7.2.
- Fixed memory leak issue in OHIF viewer.

**1.7.2 Beta**

Bug-Fixes:
- Bumped cornerstoneTools dev version to fix issues with broken ROI tools. This broke reference lines, which will be fixed soon.

**1.7.1 Beta**

Bug-Fixes:
- Fixed stack scroll with keyboard and bar.
- Fixed reference lines.

**1.7.0 Beta**

Features:
- CornerstoneTools v3:
  - Upgraded to a pre-release of cornerstoneTools v3! On the surface not a lot will have appeared to change yet, apart from tool UX bellow. But the new major version of library opens the doors to lots of possibilities going forward.
  - Improved the speed of all ROI UI/caching. This will likely only be noticeable in larger studies.
  - The image screenshot functionality has been removed whilst the OHIF foundation updates it to v3. When it returns we plan to be able to store the generated images to XNAT.
- Brush Tool: New segmentation mask brush tool from cornerstoneTools v3. One may create multiple 3D segmentations, and they may overlap. There are instructions available in the help menu. Export to DICOM-SEG and NIFTI masks to XNAT will come in the future.

UX:
- Freehand Draw: No more shift clicking is required for 'pencil mode'. To draw using the pencil simply click and drag , release the mouse to close the ROI. Click-move-click to use polygon mode. You can now freely switch between the two modes during use.
- Freehand Sculpter: No more ctrl-click needed. Double click near an ROI to select it. A live preview of the toolsize can be seen during use, making it a lot easier to do precision sculpting. Just try it!

Bug-Fixes:
- Fixes erroneous mapping of RTSTRUCT contours onto images for some orientations.

**1.6.2 Beta**
UI:
- Moved the smooth toggle to the viewport overlay, under sync. Smoothing is off by default.

**1.6.1 Beta**
Bug-Fixes:
- Fixes cases where the URLs of the package libraries would sometimes be prepended by multiple slashes, causing no problems to the user, but causing Tomcat to throw exceptions to the log on the backend.

**1.6.0 Beta**

Features:
- Added a toggle for image smoothing, as it is sometimes necessary to see each pixel clearly when segmenting small objects.
- Added server-side creation of RTSTRUCT when roiCollection is exported to AIM. Both representations will appear under the roiCollection in XNAT.

**1.5.4 Beta**

Bug-Fixes:
- Changing tool whilst midway through drawing an ROI will finish that roi instead of doing crazy things.

**1.5.3 Beta**

Bug-Fixes:
- Fix broken help menu button.

**1.5.2 Beta**

Bug-Fixes:

- Updated EtherJ-Core and EtherJ-XNAT Versions. This fixes issues that occurred whilst converting to and from AIM and RTSTRUCT using the ROI api.

**1.5.1 Beta**

Bug-Fixes:

- Fixed Issue #6.

**1.5.0 Beta**

Features:
- Clicking import ROIs now brings up a menu which allows importing of specific ROI collections, instead of importing all that the AsyncRoiFetcher can find. You can still import all if you wish.

UI:
- Only collections that reference the active series are shown in the import menu.
- If an ROI Collection has already been imported, it won't appear on the import list again.
- A message is displayed to tell the user there are no ROI Collections to import if either there are none in the Session that reference the series, or they have all already been imported.

UX:
- The import menu now pops up instantly and fills asynchronously as it receives data from the backend.

**1.4.0 Beta**

Features:

- The volume management dialog now gives the option to disable the visibility of imported RoiCollections, such that only a selection of the collections are rendered and interact-able.

**1.3.0 Beta**

Features:

- Synchronization
  - Using the tick boxes at the top right of each viewport, one can synchronise scans to scroll together.
  - The 'Sync Settings' menu has the option to set all viewports to be synced by default.
  - The 'Sync Settings' menu allows you to configure the synchronization to opperate via image position, or by frame index. Image position is the default.
- Export:
  - The user can now choose which ROIs to export as a collection, instead of exporting all ROI's by default.
  - The exported ROIs will become locked as before, and will be listed as an ROI collection in the ROI management interface.

UI:
- Consolidated Help to a single interface.
- The ROI management UI now displays ROIs in organised ROI collections when imported.
- New export UI.

**1.2.1 Beta**

UI:

- Cleaned up UI of subject and project views, so that they don't list RoiCollections. RoiCollections are still listed in the Session they correspond to (Thanks James D'Arcy!).

**1.2.0 Beta**

Features:

- Delete tools. Under the delete menu there is:
  - The Eraser tool, which can delete any annotation by clicking on its handle. Note there is no highlighting of the tools being deleted currently. This will come later once we have a more developed cornerstoneTools API.
  - Clear, which deletes all annotations on the slice.
  - Locked ROIs cannot be deleted by the Eraser tool, or by Clear.

UI:

- New Icons for import/export.
- XNAT logo overlaying OHIF logo on the top left.
- ROI plugin version number top left.
- Side bar open by default.

**1.1.0 Beta**

Features:

- Full multi-frame support. Tested on both primary and secondary multi-frame DICOMs and can successfully save/load AIMs to these.

**1.0.0**

- Initial public release with no ROI support for the backend.

Bug fixes:

- Made a list of SOP classes defining viewer should and should not attempted to load. If any non-imaging resources sit as "scans", the viewer will no longer try to load these.
- If image assessors other than the RoiCollections are present in the assessor list, the viewer will now count the correct number of RoiCollections and won't hang on import.

**0.5.0**

UX:

- Added server side event listeners to automate JSON generation on session upload/modification.
- Removed need for setting up of automation scripts.

**0.4.0**

UX:

- Removed the requirement for the "Sync Viewer" button.
- Added a Groovy script to automate server side JSON generation whenever new images are uploaded or removed. This requires setup by an admin. Please check the README for instructions.

**0.3.1**

Features:

- Series level JSONification and the ability to read only series from the viewer application, however this is not currently wired up to the XNAT UI (yet is functional).

Performance:
- Improved Multithreaded code for generating all metadata.

**0.3.0**

Features:
- Added a new REST call "/xapi/viewer/generate-all-metadata" which will generate the viewer metadata for every session in the database using as many threads as available on the server. This is intended to only be called once, when the plugin is added to an already populated XNAT. Currently this can invoked via the command line or the swagger-ui interface.

**0.2.0**

Features:
- Added support for secondary DICOM files.
- Added support for single frame images which contain the tag (0028,0008), which is supposed to be reserved for multi-frame images according to the DICOM standard.
- Added support for generating JSON with the correct protocol for both single and multi-frame DICOM images, however this is currently unused (see changes).

Changed:
- Multi-frame images will not appear in the viewer when View Study is clicked, as they currently do not load correctly.

**0.1.0**

Basic functionality, the viewer is correctly linked and interfaced with XNAT. Although not yet feature rich, the plugin is useable.
