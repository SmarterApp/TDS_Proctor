//required: yui-min.js
//get data and add more method into testSession object (JSON c# object)
YUI.add("tds-session", function (Y) {
    //-------------------------------------------------------------------------
    // Private variables
    //-------------------------------------------------------------------------
    var _data = null,
        _proctorKey = null;
    var _loadCurTesteesMulitplier = 2; //30 seconds * 2 = 60 seconds
    var _loadWaitingForApprovalCount = 1;
    var _isReloading = false;

    var _oShared = Y.tdsShared;
    var _oMessages = Y.Messages;
    var _oTests = Y.tdsTests;
    var _oSegments = Y.tdsSegments;
    var _oApprovalOpps = Y.tdsApprovalOpps;
    var _oTestOpps = Y.tdsTestOpps;


    //-------------------------------------------------------------------------
    // Private functions
    //------------------------------------------------------------------------
    function _hasSession() {
        return (_data == undefined || _data._key == undefined) ? false : true;
    }
    function _id() {
        if (_data == undefined || _data.id == undefined)
            return '';
        return _data.id;
    }
    function _key() {
        if (_data == null) return null;
        return _data._key;
    }
    function _status() {
        if (_data == undefined || _data.status == undefined)
            return '';
        return _data.status;
    }
    function _isOpen() {
        return (_status() == 'open') ? true : false;
    }



    //aryTestKeys: array of test keys
    function _insertTests(aryTestKeys) {
        //A function handler to use for successful requests:
        var handleSuccess = function (ioId, o) {
            if (o.responseText != undefined) {
                try {
                    var sessionDTO = Y.JSON.parse(o.responseText);
                    _setAndRender(sessionDTO);
                }
                catch (e) {
                    _oShared.showError(_oMessages.get('UnableToProcessRequest'));
                    Y.log("ERROR: Session._insertTests: " + e);
                    return false;
                }
            }
        };
        var handleFailure = function (ioId, o) {
            Y.log("ERROR: Session._insertTests: handleFailure");
            var msg = _oMessages.get('UnableToProcessRequest');
            _oShared.showError(msg);
        };

        var testKeys = aryTestKeys.join('|');
        var aryTestIDs = _oTests.getTestIDs(aryTestKeys);
        var testIDs = aryTestIDs.join('|');
        var postData = "testKeys=" + testKeys + "&testIDs=" + testIDs;

        if (_key() != null)
            postData += "&sessionKey=" + _key();
        Y.log(postData);
        /* Configuration object for POST transaction */
        var cfg = {
            method: "POST",
            data: postData,
            timeout: gXHRTimeout, // Abort the transaction, if it is still pending, after X ms.
            headers: { "cache-control": "no-cache", 'X-Transaction': 'Get Session Init data' },
            on: {
                success: handleSuccess,
                failure: handleFailure
            }
        };

        Y.io("Services/XHR.axd/InsertSessionTests", cfg);
    }

    function _stop() {
        if (_data == null) {
            _oShared.showError(_oMessages.get('UnableToProcessRequest'));
            return;
        }
        //A function handler to use for successful requests:
        var handleSuccess = function (ioId, o) {
            if (o.responseText != undefined) {
                try {
                    var sessionDTO = Y.JSON.parse(o.responseText);
                    if (sessionDTO.status != undefined) { //if error, alert the user and then refresh the page.
                        _oShared.renderErrorDlg(sessionDTO);
                        return false;
                    }
                    _setAndRender(sessionDTO);
                    Y.tdsTests.setIsSelected();
                    Y.pUI.sessionStopped();
                    return true;
                }
                catch (e) {
                    Y.log("ERROR: tdsSession.stop: " + e);
                    _oShared.showError(_oMessages.get('UnableToProcessRequest'));
                    return false;
                }
            }
        };
        var handleFailure = function (ioId, o) {
            Y.log("ERROR: tdsSession.stop: " + x);
            _oShared.showError(_oMessages.get('UnableToProcessRequest'));
        };

        /* Configuration object for POST transaction */
        var cfg = {
            method: "POST",
            data: "sessionKey=" + _key(),
            timeout: gXHRTimeout, // Abort the transaction, if it is still pending, after X ms.
            headers: { "cache-control": "no-cache", 'X-Transaction': 'Get Session Init data' },
            on: {
                success: handleSuccess,
                failure: handleFailure
            }
        };

        Y.log("tdsSession.stop");
        Y.io("Services/XHR.axd/PauseSession", cfg);
    }

    function _sessionClosedHandler() {
        var msgDialog = _oShared.msgDialog(null, null);
        msgDialog.cancel();
        _load(0); //init all
    }

    function _render() {
        Y.log("_oSession._render");
        var elem = Y.pElem.lblSessionID;
        if (elem != null)
            elem.setContent('---- - ----');
        if (!_hasSession()) {
            Y.pUI.sessionStopped();
            return;
        }
        if (elem != null)
            elem.setContent(_id());
        if (_isOpen())
            Y.pUI.sessionStarted();
        else
            Y.pUI.sessionStopped();
    }


    function _load(nLoadType) {
        //A function handler to use for successful requests:
        Y.log('_load(nLoadType)--' + nLoadType);
        if (_isReloading) {//dont try to hit the server again until the loading is completed, prevent multiple hits on the server
            Y.log('is loading ...');
            //_oShared.stopPleaseWait();
            return;
        }
        var handleSuccess = function (ioId, o) {
            if (o.responseText != undefined) {
                try {
                    var sessionDTO = Y.JSON.parse(o.responseText);
                    _setAndRender(sessionDTO);
                    _isReloading = false;
                    return true;
                }
                catch (e) {
                    _isReloading = false;
                    Y.log("ERROR: tdsSession.load: " + e);
                    _oShared.showError(_oMessages.get('UnableToProcessRequest'));
                    return false;
                }
            }
            _isReloading = false;
        };
        var handleFailure = function (ioId, o) {
            _isReloading = false;
            Y.log("ERROR: tdsSession.load: ");
            _oShared.showError(_oMessages.get('UnableToProcessRequest'));
        };

        var funcName;
        var postData = '';
        switch (nLoadType) {
            case 0: //load all init data                
                funcName = "GetInitData";
                break;
            case 1: //GetCurrentSession
                funcName = "GetCurrentSession";
                break;
            case 2: //load auto refresh data
                funcName = "AutoRefreshData";
                Y.log("_loadWaitingForApprovalCount - " + _loadWaitingForApprovalCount);
                Y.log("_loadCurTesteesMulitplier - " + _loadCurTesteesMulitplier);
                if (_loadWaitingForApprovalCount >= _loadCurTesteesMulitplier) { //if the number of load to get waiting for approvals >= load current testees multiplier
                    _loadWaitingForApprovalCount = 1;
                    postData = "bGetCurTestees=true&";
                }
                else {
                    _loadWaitingForApprovalCount++;
                    postData = "bGetCurTestees=false&";
                }
                var key = Y.tdsSession.key();
                if (key == undefined) {
                    _oShared.stopPleaseWait();
                    return;
                }
                postData += "sessionKey=" + key;
                break;
        }
        /* Configuration object for POST transaction */
        var cfg = {
            method: "POST",
            data: postData,
            timeout: gXHRTimeout, // Abort the transaction, if it is still pending, after X ms.
            headers: { "cache-control": "no-cache", 'X-Transaction': 'Get data' },
            on: {
                success: handleSuccess,
                failure: handleFailure
            }
        };
        //testing
        var url = "Services/XHR.axd/" + funcName;
        Y.log("Y.tdsSession._load - " + url);
        Y.io(url, cfg);
        _isReloading = true;
    }


    function _setAndRender(sessionDTO) {
        Y.log("Session._setAndRender");

        try {
            var pUI = Y.pUI;
            _oShared.stopPleaseWait(); //stop please wait dialog
            if (sessionDTO == null)
                return;
            var msgDialog;
            if (sessionDTO.status != undefined) {
                var bHasSpecialError = _oShared.renderError(sessionDTO);
                if (!bHasSpecialError)
                    _oShared.showError(_oMessages.get(sessionDTO.reason));
                return; //errors
            }
            _oShared.showError(null); //close the error message box

            if (sessionDTO.bReplaceTests) {
                _oTests.setData(sessionDTO.tests);
                debugData['tests'] = sessionDTO.tests;
            }

            if (sessionDTO.bReplaceSegments) {
                _oSegments.setData(sessionDTO.segments);
                debugData['segments'] = sessionDTO.segments;
            }

            if (sessionDTO.bReplaceSessionTests) {
                _oTests.setSelectedTests(sessionDTO.sessionTests);
                _oTests.setIsSelected();
            }

            if (sessionDTO.bReplaceTests || sessionDTO.bReplaceSessionTests) {
                _oTests.render(true); //re-render               
            }
            if (sessionDTO.bReplaceApprovalOpps) {
                _oTests.setTestName(sessionDTO.approvalOpps); //set test display name for all opps
                _oApprovalOpps.setData(sessionDTO.approvalOpps);
                _oApprovalOpps.renderView(); //re-renderc
                debugData['approvalOpps'] = sessionDTO.approvalOpps;
            }

            if (sessionDTO.bReplaceTestOpps) {
                _oTests.setTestName(sessionDTO.testOpps); //set test display name for all opps
                _oTestOpps.setData(sessionDTO.testOpps);
                Y.tdsSort.reset(_oTestOpps.data()); //re-sort if neccessary
                _oTestOpps.render(); //re-render
                debugData['testOpps'] = sessionDTO.testOpps;
            }
            if (sessionDTO.bReplaceSession) {
                _data = sessionDTO.session;
                _render(); //re-render
            }
            if (sessionDTO.bReplaceAlertMsgs) {
                Y.AlertMessages.setData(sessionDTO.alertMessages);
                Y.AlertMessages.start(Y.pUI); //start alert if any
            }
            if (sessionDTO.msbAlert) {
                Y.AlertMessages.setData([{
                    Key: 'key1',
                    Title: 'MSB Alert',
                    Message: 'asdfasdf asdfasdfasdf'

                }]);
                Y.AlertMessages.start(Y.pUI); //start alert if any
            }
        }
        catch (e) {
            Y.log("ERROR: tdsSession.setAndRender: " + e);
            _oShared.showError(_oMessages.get('UnableToProcessRequest'));
        }

    }
    //take over the session with another browserkey
    function _takeOverSession(e, msgDialog) {
        Y.log("_takeOverSession");
        var elem = Y.one("#locked_sessionID");
        if (elem == null)
            return;
        var sessionID = elem.get('value');
        _handoffSession(sessionID, msgDialog);

    }
    function _handoffSession(sessionID, msgDialog) {
        //A function handler to use for successful requests:
        var handleSuccess = function (ioId, o) {
            if (o.responseText != undefined) {
                var returnedStatus = Y.JSON.parse(o.responseText);
                if (returnedStatus.status != undefined && returnedStatus.status == "True") {
                    //success
                    _load(0); //get all data
                    msgDialog.cancel();
                }
                else {
                    //failed
                    msgDialog.onError(_oMessages.get("Invalid session ID"));
                    return;
                }
            }
        };
        var handleFailure = function (ioId, o) {
            msgDialog.onError(_oMessages.get("Unable to process request, please try again."));
        };
        if (sessionID == null || sessionID.length < 1) {
            msgDialog.onError(_oMessages.get("Invalid session ID"));
            return;
        }

        /* Configuration object for POST transaction */
        var cfg = {
            method: "POST",
            data: "sessionID=" + sessionID,
            timeout: gXHRTimeout, // Abort the transaction, if it is still pending, after X ms.
            headers: { "cache-control": "no-cache", 'X-Transaction': 'Hand off session' },
            on: {
                success: handleSuccess,
                failure: handleFailure
            }
        };
        var url = "Services/XHR.axd/HandoffSession";
        Y.io(url, cfg);
    }

    function _init(nLoadType) {
        Y.log("Y.tdsSession._init - " + nLoadType);
        if (_oTests == undefined)
            _oTests = Y.tdsTests;
        if (_oApprovalOpps == undefined)
            _oApprovalOpps = Y.tdsApprovalOpps;
        if (_oTestOpps)
            _oTestOpps = Y.tdsTestOpps;
        if (Y.tds.gTDS.appConfig != null && Y.tds.gTDS.appConfig.RefreshVM > 0)
            _loadCurTesteesMulitplier = Y.tds.gTDS.appConfig.RefreshVM;
        _load(nLoadType);
    }
    //-------------------------------------------------------------------------
    // Public interface
    //-------------------------------------------------------------------------
    Y.tdsSession = {
        init: function (nLoadType) {
            _init(nLoadType);
        },
        initAll: function () {
            Y.log("Y.tdsSession.initAll");
            _load(0);
        },
        //session key
        key: function () {
            return _key();
        },
        //aryTestKeys: array of test keys
        //        insertTestsAndStartSession: function (aryTestKeys) {
        //            _insertTests(aryTestKeys);
        //        },
        insertTests: function (aryTestKeys) {
            _insertTests(aryTestKeys);
        },
        hasSession: function () {
            return _hasSession();
        },
        isOpen: function () {
            return _isOpen();
        },
        stop: function () {
            _stop();
        },

        //make sure to get the current testees as well.
        refreshCurTestees: function () {
            _loadWaitingForApprovalCount = _loadCurTesteesMulitplier + 1;
        },

        sessionClosedHandler: function () {
            _sessionClosedHandler();
        },

        takeOverSession: function (e, msgDialog) {
            _takeOverSession(e, msgDialog);
        },

        activateScollView: function () {
            var bottomHalfs = Y.all('.bottomHalf');
            if (bottomHalfs == undefined) return;
            var len = bottomHalfs.size();
            //alert(len);
            for (var i = 0; i < len; i++) {
                //alert(bottomHalfs.item(i).get('id'));
                _oShared.activateScollView(bottomHalfs.item(i));
            }
        }

    };
}, "0.1", { requires: ["node", "io", "json-parse", "tds", "tds-tests", "tds-segments", "tds-testopps", "tds-approvalopps", "tds-alerts"] });

