(function($) {
  var methods = {
    init: function(settings) {
      return this.each(function() {
        var self = this;
        if (typeof self.pleaseWait != "undefined") return;
        self.pleaseWait = {
          opt: $.extend(true, {}, $.fn.pleaseWait.defaults, settings),
          cog: document.createElement("img"),
          timer: false,
          overlay: document.createElement("div"),
          degrees: 1
        };

        var targetWidth = $(self).outerWidth();
        var targetHeight = $(self).outerHeight();
        var verticalMidpoint =
          targetHeight / 2 - self.pleaseWait.opt.height / 2;
        var horizontalMidpoint =
          targetWidth / 2 - self.pleaseWait.opt.width / 2;
        var offset = $(self).offset();

        $(self.pleaseWait.cog).css({
          position: "absolute",
          "z-index": "999999",
          left: offset.left + horizontalMidpoint,
          top: offset.top + verticalMidpoint
        });
        $(document.body).append(self.pleaseWait.cog);

        self.pleaseWait.overlay = document.createElement("div");
        $(self.pleaseWait.overlay).css({
          position: "absolute",
          "z-index": "999998",
          left: offset.left,
          top: offset.top,
          width: targetWidth,
          height: targetHeight,
          "background-color": "rgba(255,255,255,0.8)",
          display: "none"
        });
        $(document.body).append(self.pleaseWait.overlay);

        if (
          self.pleaseWait.opt.imageType != "encoded" &&
          self.pleaseWait.opt.image.length > 0
        ) {
          self.pleaseWait.cog.src = image;
        } else {
          self.pleaseWait.cog.src =
            "data:image/png;base64," + self.pleaseWait.opt.image;
        }

        $(self).attr("data-pleaseWait", "1");

        methods.start.call(self);
      });
    },
    start: function() {
      var self = $(this);
      if (typeof self.length != "undefined" && self.length > 0) self = self[0];
      if (typeof self.pleaseWait == "undefined") return;

      var targetWidth = $(self).outerWidth();
      var targetHeight = $(self).outerHeight();
      var verticalMidpoint = targetHeight / 2 - self.pleaseWait.opt.height / 2;
      var horizontalMidpoint = targetWidth / 2 - self.pleaseWait.opt.width / 2;
      var offset = $(self).offset();
      $(self.pleaseWait.cog).css({
        left: offset.left + horizontalMidpoint,
        top: offset.top + verticalMidpoint
      });
      $(self.pleaseWait.overlay).css({
        left: offset.left,
        top: offset.top,
        width: targetWidth,
        height: targetHeight
      });

      $(self.pleaseWait.cog).css("display", "");
      $(self.pleaseWait.overlay).css("display", "");
      if (self.pleaseWait.timer != false) clearTimeout(self.pleaseWait.timer);
      var drawMethod = methods.draw;
      var removeMethod = methods.remove;
      self.pleaseWait.timer = setInterval(function() {
        if (document.body.contains(self)) {
          drawMethod.call(self);
        } else {
          removeMethod.call(self);
        }
      }, self.pleaseWait.opt.speed);
      return self;
    },
    stop: function() {
      var self = $(this);
      if (typeof self.length != "undefined" && self.length > 0) self = self[0];
      if (typeof self.pleaseWait == "undefined") return;
      if (self.pleaseWait.timer != false) clearTimeout(self.pleaseWait.timer);
      self.pleaseWait.timer = false;
      $(self.pleaseWait.cog).css("display", "none");
      $(self.pleaseWait.overlay).css("display", "none");
      return self;
    },
    draw: function() {
      var self = this;
      if (typeof self.length != "undefined" && self.length > 0) self = self[0];
      if (typeof self.pleaseWait == "undefined") return;

      var rotateTarget = self.pleaseWait.cog;
      if (self.pleaseWait.opt.crazy) rotateTarget = self;

      if (navigator.userAgent.match("MSIE")) {
        rotateTarget.style.msTransform =
          "rotate(" + self.pleaseWait.degrees + "deg)";
      } else if (navigator.userAgent.match("Opera")) {
        rotateTarget.style.OTransform =
          "rotate(" + self.pleaseWait.degrees + "deg)";
      } else {
        rotateTarget.style.transform =
          "rotate(" + self.pleaseWait.degrees + "deg)";
      }
      self.pleaseWait.degrees =
        parseInt(self.pleaseWait.degrees) +
        parseInt(self.pleaseWait.opt.increment);
      if (self.pleaseWait.degrees > 359) {
        self.pleaseWait.degrees = 1;
      }
    },
    remove: function() {
      var self = this;
      if (typeof self.length != "undefined" && self.length > 0) self = self[0];
      if (typeof self.pleaseWait != "undefined") {
        if (self.pleaseWait.timer != false) clearTimeout(self.pleaseWait.timer);

        $(self.pleaseWait.cog).remove();
        $(self.pleaseWait.overlay).remove();

        $(self).removeAttr("data-pleaseWait");
        delete self.pleaseWait.overlay;
        delete self.pleaseWait.opt;
        delete self.pleaseWait.cog;
        delete self.pleaseWait.timer;
        delete self.pleaseWait;
      }
    }
  };
  $.fn.pleaseWait = function(method) {
    if (!methods[method]) {
      var attr = $(this).attr("data-pleaseWait");
      if (typeof attr == "undefined" || attr == false)
        return methods.init.apply(this, arguments);
      method = "start";
    }
    if (methods[method]) {
      return methods[method].apply(
        this,
        Array.prototype.slice.call(arguments, 1)
      );
    } else if (typeof method === "object" || !method) {
      return methods.init.apply(this, arguments);
    } else {
      $.error('pleaseWait plugin :: method "' + method + '" does not exist!');
    }
  };
  $.fn.pleaseWait.defaults = {
    crazy: false,
    speed: 8,
    increment: 2,
    image: "iVBORw0KGgoAAAANSUhEUgAAAEEAAABBCAYAAACO98lFAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAABK1JREFUeNrsXFtIFFEYPqsbZl7S1G5oqETgS2SghUUXIughCiuCHnrrAhFUD0EUUj1E9BBFVBC9RAWBD92IiF4iykCLlCCKkBLU0i5meU/b7f/b79DxtLo7t53dnf3hw5lxZ+bMN//9nF1fMBgUsRSfzxf2eKzHoUqaS/c9xM8NnBQuS5pIifDbqdKRxE2Vd1oT8ghbPKcJkHTCDsIBQhbhDeF1IpJgRRN+E9YQckHIiUTVBKvmcBxksFQTar1IAqv/dWX/IGGK10hgOUXoxXYpYbcXSegFEVL2EWZ7jQQBk5CRgSNFnRdJYOd4RNmvhaP0FAksTYSHyv7RhCOBU+FoMYkcI4xiuzJRQuZEmlBi8npthEvKfh18RHwLFzVaYbOc8AIPUGDikvzQLwkfgcNhCqhFhF1AtToON+CTA4Oac6JTr2hCP+Eyjo0aIILN4AK2PxOq+Py/N4zDpopOQj7e3Grtc+2Ec4RHBq5dj7B5hvAznktpnQQpVUiBy7XPPyOcJbxPpn7CRCRIp7mJsAeVopQA4SbhPMwlqUmQkk3YC0LUaMIqfhGEBOL9Qa2SIIVNYz+hRjvOpnGa0OgFEqSsQDdJzyW2c3fJKyTIULqVsBPm8hRaIrxEgpQCOM4rCKOeJCFhIkBUJHhZUiSkSAAJXvUDOgk8hbbM5PlcMl9LdBJ4Gq6IMN/k+Z3JYA6pqXmYA2vCdJPn9xG6k8EnpBxjKkSmSEjM2sGOcepjtIOEWYQvAt2lGJKQjnsG3SQhk7CesEqEOsuPY0xCERw7t/mG3SBhJWGj+De7NCBCkzUDMSIhSwvrI4QfhDEzJBhduLVAhDpKJWEyx0yQEQsJADLZyyDMxP37hNL4tdMxFqLGqNSO8+wSd5ubXXCMTECO+H+uMwAiBuwyB2Z4HWGtGL8WidXvgQhNxY+5HB38MI0M7fgYTGTECglLCZvF+IkXlgbCbTgkR8KnyUg1FWPVTXwYYx0zQkIpYRv+qtJGuIG/0Ug5zKXfIgl5UO1oJoR9MI9srTgMKv4iGImEGSK0KFO9QA/hlgitRIlWsuBA+aItQMAgCZlwwDkYwweD1TFrxTTt+BDhuz4OvZTmmz3BNjN/X4RWnzQZVM0l8CGsmmVqQmNg9Us6CJAvJ9tg9OhFEvdLOd4fbXTIgi+4B1KMCi/f26Ds3yV0SXPAfbiJU4H/twpt5koZSxkIYBkkvDWTIUKr/DAHEUkTZOJz1SQBPHq1VdcqCTApHUrMZ9XON3mdIUlAOPGbCW2TeG9+uwWKOTVajIijcK5ycWgx1DxgZ+FmZ3uNfcBiZb/ZpgzykxIZ+B5z7M497CShUvHG/PCvbEyRO7SqNSMeSeBwtFDZbzSav0eQHsWz+4T5JYaOklCjXKsLDtFuaVciw3QlfMYFCfMAmZk1OFQ2DGoRq1jY1Ci2SoIPNYYU/h7UNwfrp04tZBbGAwkVyO0FMrPnDvcRRhEtpMwV1r7MZhsJUlrCla0OSLdyH7/yElwj4Y4IrYPusTEkRqzI4STZR7wjfLXcozCTaVn5hqzB2sH2XoXTyVLCit/sG3VU32P9cwYu/X5CrlIRch+w183pwD8CDAC/RBaBiIoXIgAAAABJRU5ErkJggg==",
    imageType: "encoded",
    height: window.innerHeight / 2,
    width: 65
  };
})(jQuery);

//############################################################//

function checkSessionJSON(newTab, projectId, subjectId, experimentId, parentProjectId) {
  $('body').pleaseWait();

  const jsonRootUrl = XNAT.url.rootUrl("/xapi/viewer/projects/");
  const jsonUrl = jsonRootUrl + projectId + "/experiments/" + experimentId;
  const jsonExistsUrl = jsonUrl + "/exists";

  checkJsonExists(
    jsonExistsUrl
  ).then(existsResult => {
    return processJsonExistsResult(existsResult, jsonUrl, experimentId);
  }).then(viewerSession => {
    if (viewerSession.type === 'JSON') {
      return getSessionViewerParams(projectId, subjectId, experimentId);
    } else if (viewerSession.type === 'DICOMWEB') {
      return processDicomwebSession(projectId, subjectId, experimentId);
    }
    // Neither type resolved: throw instead of returning undefined (which becomes /VIEWERundefined).
    throw new Error('Unsupported viewer session type for experiment ' + experimentId +
      ': "' + (viewerSession && viewerSession.type) + '".');
  }).then(viewerParams => {
    return openViewer(viewerParams, newTab, parentProjectId);
  }).catch(error => {
    const errorMessage = error.message || 'Unknown error!';
    console.error(error);
    XNAT.dialog.message('Error', errorMessage);
  }).finally(() => {
    $('body').pleaseWait('stop');
  });
}

function checkJsonExists(jsonExistsUrl) {
  return xov_xhrPromise(
    jsonExistsUrl,
    { method: 'GET', accept: 'application/json' }
  );
}

function processJsonExistsResult(existsResult, jsonUrl, experimentId) {
  const { status, responseText } = existsResult;
  switch (status) {
    case 200:
      console.log('JSON found for session ' + experimentId);
      return getViewerSessionJson(jsonUrl)
        .then(studyList => checkValidJsonOrDicomweb(studyList));
    case 404:
      console.log('Generating JSON for session ' + experimentId);
      return generateViewerSessionJson(jsonUrl)
        .then(() => getViewerSessionJson(jsonUrl))
        .then(studyList => checkValidJsonOrDicomweb(studyList));
    default:
      throw new Error('Unsuccessful, status: ' + status);
  }
}

function getSessionViewerParams(projectId, subjectId, experimentId) {
  const sessionUrl = XNAT.url.rootUrl("/data/archive/projects/" + projectId + "/subjects/" + subjectId + "/experiments/" + experimentId + "?format=json");

  console.log("Get session viewer Params, URL: " + sessionUrl);

  return xov_xhrPromise(
    sessionUrl,
    { method: 'GET' }
  ).then(result => {
    if (result.status === 200) {
      const jsonString = result.responseText;
      const sessionJSON = JSON.parse(jsonString);

      const params = '?subjectId=' + subjectId +
        '&projectId=' + projectId +
        '&experimentId=' + experimentId +
        '&experimentLabel=' + sessionJSON.items[0].data_fields.label;

      return params;
    }
    // Do not fall through returning undefined: that becomes '/VIEWER' + undefined -> /VIEWERundefined.
    // A non-200 here typically means a missing subjectId produced a malformed archive URL. Surface it.
    throw new Error('Unable to resolve viewer params for experiment ' + experimentId +
      ' (subjectId "' + subjectId + '"): archive session request returned status ' + result.status + '.');
  });
}

function processDicomwebSession(projectId, subjectId, experimentId) {
  const dwRootUrl = XNAT.url.rootUrl("/xapi/viewerDicomweb/projects/");
  const dwUrl = dwRootUrl + projectId + "/experiments/" + experimentId;
  const dwExistsUrl = dwUrl + "/exists";

  return xov_xhrPromise(
    dwExistsUrl,
    { method: 'GET' }
  ).then(existsResult => {
    const { status } = existsResult;
    if (subjectId) {
      // Session-level viewer
      switch (status) {
        case 200:
          console.log('DICOMweb data found for session ' + experimentId);
          return getSessionViewerParams(projectId, subjectId, experimentId);
        case 404:
          return generateViewerSessionDicomweb(dwUrl)
            .then(() => (
              getSessionViewerParams(projectId, subjectId, experimentId)
            ));
        default:
          throw new Error('Unsuccessful, status: ' + status);
      }
    } else {
      // Subject-level viewer
      switch (status) {
        case 200:
          console.log('DICOMweb data found for session ' + experimentId);
          return Promise.resolve(true);
        case 404:
          return generateViewerSessionDicomweb(dwUrl)
            .then(() => (
              Promise.resolve(true)
            ));
        default:
          throw new Error('Unsuccessful, status: ' + status);
      }
    }
  });
}

function getViewerSessionJson(jsonUrl) {
  return xov_xhrPromise(
    jsonUrl,
    { method: 'GET', accept: 'application/json' }
  ).then(result => {
    if (result.status === 200) {
      const studyList = JSON.parse(result.responseText);
      console.log(studyList);
      return studyList;
    } else {
      throw new Error(result.responseText)
    }
  });
}

function checkValidJsonOrDicomweb(studyList) {
  return new Promise((resolve, reject) => {
    if (!studyList.studies) {
      reject(new Error('Invalid studyList object.'));
      return;
    }

    if (studyList.isDicomWeb) {
      resolve({ type: 'DICOMWEB', isValid: true });
      return;
    }

    let isEmpty = true;

    for (let i = 0; i < studyList.studies.length; i++) {

      if (!studyList.studies[i] || !studyList.studies[i].series) {
        continue;
      }

      const series = studyList.studies[i].series;

      for (let j = 0; j < series.length; j++) {
        if (series[j].instances.length) {
          isEmpty = false;
          break;
        }
      }
    }

    if (isEmpty) {
      reject(new Error(
        'No viewable scans: ' +
        'There are no scans in this session compatible with the OHIF Viewer.'
      ));
      return;
    }

    resolve({ type: 'JSON', isValid: true });
  });
}

function generateViewerSessionJson(jsonUrl) {
  return xov_xhrPromise(jsonUrl, { method: 'POST' });
}

function generateViewerSessionDicomweb(dwUrl) {
  return xov_xhrPromise(dwUrl, { method: 'POST' });
}

function openViewer(params, newTab, parentProjectId) {
  if (!params) {
    // Guard: never navigate to '/VIEWER' + undefined (-> /VIEWERundefined, a 404). A falsy params
    // means an upstream step (session-JSON resolution or subject/session context) did not resolve.
    throw new Error('OHIF viewer launch aborted: viewer parameters are undefined ' +
      '(missing subject/session context).');
  }
  if (parentProjectId) {
    params = params + '&parentProjectId=' + parentProjectId;
  }

  const openViewerUrl = XNAT.url.rootUrl('/VIEWER' + params);

  if (newTab) {
    window.open(openViewerUrl);
  } else {
    window.location.href = openViewerUrl;
  }
}

//############################################################//

function checkSubjectForSessionJSON(newTab, projectId, subjectId, parentProjectId) {
  const subjectExperimentListUrl = XNAT.url.rootUrl('/data/archive/projects/'+ projectId + "/subjects/" + subjectId + "/experiments?format=json");

  console.log(subjectExperimentListUrl);

  const jsonRootUrl = XNAT.url.rootUrl("/xapi/viewer/projects/" + projectId + "/experiments/");

  let experimentList = [];
  let waitDialog;

  getSubjectExperimentList(
    subjectExperimentListUrl
  ).then(exptListRsp => {
    if (exptListRsp.status !== 200) {
      throw new Error(
        'Unable to retrieve experiments for Subject ' +
        subjectId
      );
    }
    const jsonResponse = JSON.parse(exptListRsp.response);
    experimentList = jsonResponse.ResultSet.Result;
    console.log(experimentList);
    if (experimentList.length === 0) {
      throw new Error(
        'Could not find experiments for Subject ' +
        subjectId
      );
    }
    // Append JSON session URLs to the experiment list
    experimentList.forEach(expt => {
      expt.jsonUrl = jsonRootUrl + expt.ID;
      expt.jsonExistsUrl = expt.jsonUrl + "/exists";
    });
    return Promise.all(experimentList.map(expt => (
      checkJsonExists(expt.jsonExistsUrl)
    )));
  }).then(existsResults => {
    const someHasNoJson = existsResults.some(
      exists => exists.status === 404
    );
    if (someHasNoJson) {
      waitDialog = XNAT.dialog.static.wait(
        "Please wait, generating viewer metadata. " +
        "This is a one-time operation that may take a few minutes if the sessions are large. " +
        "When it is complete, the viewer will open."
      );
    }
    return Promise.all(existsResults.map((existsResult, index) => (
      processJsonExistsResult(
        existsResult,
        experimentList[index].jsonUrl,
        experimentList[index].ID
      )
    )));
  }).then(viewerSessions => {
    return Promise.all(viewerSessions.map((viewerSession, index) => {
      if (viewerSession.type === 'JSON') {
        return Promise.resolve(true);
      } else if (viewerSession.type === 'DICOMWEB') {
        return processDicomwebSession(projectId, undefined, experimentList[index].ID);
      }
    }));
  }).then(() => {
    const viewerParams = '?subjectId=' + subjectId + '&projectId=' + projectId;
    return openViewer(viewerParams, newTab, parentProjectId);
  }).catch(error => {
    const errorMessage = error.message || 'Unknown error!';
    console.error(error);
    XNAT.dialog.message('Error', errorMessage);
  }).finally(() => {
    if (waitDialog) {
      waitDialog.close();
    }
  });
}

function getSubjectExperimentList(url) {
  return xov_xhrPromise(url, { method: 'GET' });
}

//############################################################//

function xov_xhrPromise(url, options) {
  return new Promise((resolve, reject) => {
    const xhr = new XMLHttpRequest();
    const { method, accept } = options;

    const requestMethod = method || 'GET';

    xhr.onload = () => {
      if (xhr.status === 200 || xhr.status === 201) {
        resolve({
          status: xhr.status,
          responseText: xhr.responseText,
          response: xhr.response,
        });
      } else if (xhr.status === 404) {
        resolve({
          status: xhr.status,
          responseText: 'Session viewer data does not exist',
        });
      } else if (xhr.status === 501) {
        // DICOMweb not supported
        resolve({
          status: xhr.status,
          responseText: xhr.responseText,
        });
      } else if (xhr.status === 403) {
        reject(new Error('Incorrect permissions'));
      } else {
        reject(new Error('Unsuccessful, status: ' + xhr.status));
      }
    };

    xhr.onabort = () => {
      reject({ status: 0 });
    };

    xhr.onerror = () => {
      reject({ status: xhr.status });
    };

    xhr.ontimeout = () => {
      reject({ status: 504 });
    };

    xhr.open(requestMethod, url);
    if (accept) {
      xhr.setRequestHeader('Accept', accept);
    }
    xhr.send();
  });
}
