//required: yui-min.js
YUI.add("tds-testopps", function(Y) {
    //-------------------------------------------------------------------------
    // Private variables
    //-------------------------------------------------------------------------
    var _data = null;
    var curTestOpp = null, //temp variable
        _hasRequests = false; //do we have any student requests

    var _oElem = Y.pElem;
    var _oSort = Y.tdsSort;
    var _oHideShow = Y.tdsHideShow;
    var _oShared = Y.tdsShared;
    var _oUI = Y.pUI;
    var _oMessages = Y.Messages;
    var _oAccTypes = Y.tdsAccTypes;
    var _oClassName = Y.pClassName;
   
    //-------------------------------------------------------------------------
    // Private functions
    //-------------------------------------------------------------------------  
    function _render() {
        Y.log("tdsTestOpps._render");
        _oElem.lblTotalOppsCount.setContent(0); //reset testOpps number
        _hasRequests = false;
        if (_oElem.tblTestOpps == null) return;

        //remove all children tables
        var tblElem = _oElem.tblTestOpps;
        var tbodies = tblElem.getElementsByTagName('tbody');
        var tbody = null;
        if (tbodies == null || tbodies.size() < 1)
            tbody = tblElem.append('<tbody></tbody>');
        else
            var tbody = tbodies.item(0);
        tbody.setContent(''); //clear out exist table first 

        var testOpps = _data;
        if (testOpps == null || testOpps.length < 1) {//no data
            tblElem.hide();
            return;
        }
        tblElem.show();

        _oElem.lblTotalOppsCount.setContent(testOpps.length);
        //Sort the data if neccessary???
        //do we need to render the sort test opp list?
        var sortedIndex = _oSort.getSortedArray();
        var bUseThisIdx = true;
        if (sortedIndex == null)
            bUseThisIdx = false;
        var len = testOpps.length;
        for (var i = 0; i < len; i++) {
            var idx = i;
            if (bUseThisIdx)
                idx = sortedIndex[i].index;
            tbody.append(_buildRow(testOpps[idx]));
        }

        setTimeout(_requests, 1); //special render if has student requests

        _oHideShow.init(_oElem.tblTestOpps, _oElem.testOppsHideShowColumns);
    }

    function _requests() {
        var className = 'printingRequested';
        if (_hasRequests) {
            _oShared.addBodyClass(className);
            //_oElem.lblApprovedRequests.show();
        }
        else {
            _oShared.removeBodyClass(className);
            //_oElem.lblApprovedRequests.hide();
        }
    }

    function _pauseConfirm(e, testOpp) {
        Y.log("_pauseConfirm");
        //_curTestOpp = testOpp;
        var msgDialog = _oShared.msgDialog(Y.tds.messages.getRaw("Important!"), Y.tds.messages.getRaw("PAUSE_A_TEST"));
        msgDialog.addButton("confirm", Y.tds.messages.getRaw("Button.Yes"), _pause, testOpp);
        msgDialog.addButton("close", Y.tds.messages.getRaw("Button.No"), msgDialog.cancel);
        msgDialog.show(_oShared.NotificationType.warning);
        return;
    }

    //pause a test opp
    function _pause(e, testOpp) {
        Y.log("_pause");
        var msgDialog = _oShared.msgDialog(null, null);
        msgDialog.cancel(); //close the dialog

        var that = this;
        //A function handler to use for successful requests:
        var handleSuccess = function(ioId, o) {
            if (o.responseText != undefined) {
                try {
                    var returnedStatus = Y.JSON.parse(o.responseText);
                    if (returnedStatus.status != undefined && returnedStatus.status == 'failed') {//failed               
                        _oShared.showError(returnedStatus.reason);
                        that.setStyle('display', '');
                    }
                    else { //updated the testOpp status
                        testOpp.status = "paused";
                        var id = _oShared.buildOppId("studentStatus_", testOpp);
                        var domElem = document.getElementById(id);
                        var elem = Y.one(domElem); //change the status text
                        elem.setContent(_getStatusText(testOpp));

                        id = _oShared.buildOppId("pauseTest_", testOpp);
                        domElem = document.getElementById(id);
                        elem = Y.one(domElem);
                        elem.setContent(''); // remove pause button              
                    }
                }
                catch (e) {
                    Y.log("ERROR: TestOpps._pause: " + e);
                    _oShared.showError(_oMessages.get('UnableToProcessRequest'));
                    return;
                }
            }
        };
        var handleFailure = function(ioId, o) {
            Y.log("ERROR: TestOpps._pause: handleFailure");
            _oShared.showError(_oMessages.get('UnableToProcessRequest'));
        };
        if (testOpp == undefined || testOpp == null) {
            _oShared.showError(_oMessages.get("InvalidTestOpp"));
            return;
        }
        that.setStyle('display', 'none'); //hide the button right away
        var postData = "sessionKey=" + Y.tdsSession.key() + "&oppKey=" + testOpp.oppKey;
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
        Y.io("Services/XHR.axd/PauseOpportunity", cfg);
    }

    function _getStatusText(testOpp) {
        if (testOpp == null) return "";
        return testOpp.status + ": " + testOpp.responseCount + "/" + testOpp.itemcount;
    }

    var _btnPauseLabel = null;
    function _getBtnPauseLabel() {
        if (_btnPauseLabel == null) {
            _btnPauseLabel = _oMessages.getRaw('Button.Pause')
        }
        return _btnPauseLabel;
    }
    var _customSettingsLabel = null;
    function _getCustomSettingsLabel() {
        if (_customSettingsLabel == null) {
            _customSettingsLabel = _oMessages.getRaw('Label.CustomSettings')
        }
        return _customSettingsLabel;
    }
    var _standardSettingsLabel = null;
    function _getStandardSettingsLabel() {
        if (_standardSettingsLabel == null) {
            _standardSettingsLabel = _oMessages.getRaw('Label.StandardSettings')
        }
        return _standardSettingsLabel;
    }
    //StudentName|SSID|Opp#|Test|Accom|Requests|StudentStatus|Pause Test
    function _buildRow(testOpp) {
        var className = (testOpp.status == "started" || testOpp.status == "approved" || testOpp.status == "review") ? null : "inactive";
        //var N = Y.Node;
        var tr = _oShared.trNode(null, className);
        tr.setAttribute('id', _oShared.buildOppId("tr_", testOpp));

        var td = _oShared.tdNode(testOpp.name, "table_name");
        tr.append(td);

        td = _oShared.tdNode(testOpp.ssid, "table_ssid");
        tr.append(td);

        td = _oShared.tdNode(testOpp.opp, "table_opp opp" + testOpp.opp);
        tr.append(td);

        td = _oShared.tdNode(testOpp.testName, "table_test");
        tr.append(td);

        //accommodations
        td = _oShared.tdNode(null, "table_accomm");
        var span = _oShared.spanNode(null, "positioner");
        var a = _oShared.aNode("Details", null);
        a.on('click', _showAccDetails, null, testOpp);
        span.append(a);
        span.append((testOpp.custAccs) ? _getCustomSettingsLabel() : _getStandardSettingsLabel());
        td.append(span);
        tr.append(td);

        var rowHasRequest = false;
        //we need to render a button testOpp.requestCount
        td = _oShared.tdNode(null, "table_request");
        if (testOpp.requestCount != undefined && testOpp.requestCount > 0) {
            var a = _oShared.aNode("Print", null);
            a.on('click', _oUI.request, null, testOpp);
            td.append(a);
            _hasRequests = true;
            rowHasRequest = true;
        }
        tr.append(td);

        //status
        td = _oShared.tdNode(null, "table_status");
        var span = _oShared.spanNode();
        span.setAttribute('id', _oShared.buildOppId("studentStatus_", testOpp));
        span.setContent(testOpp.displayStatus);
        td.append(span);
        tr.append(td);

        //we need to render a pause button
        td = _oShared.tdNode(null, "table_pause");
        td.setAttribute('id', _oShared.buildOppId("pauseTest_", testOpp));
        if (testOpp.status == "started" || testOpp.status == "approved") {
            var a = _oShared.aNode(_getBtnPauseLabel(), null);
            a.on('click', _pauseConfirm, null, testOpp);
            td.append(a);
        }
        tr.append(td);

        //Mobile browser:
        _buildRowMobile(tr, testOpp, rowHasRequest);

        return tr;
    }
    //for mobile only**********************************************************************************************   
    function _buildRowMobile(tr, testOpp, hasRequest) {
        if (!_oShared.mobile()) return; //only for mobile
        var N = Y.Node;
        var td = _oShared.tdNode(null, "table_details");
        var a = _oShared.aNode("See all details", null);
        a.on('click', _showAllDetails, null, testOpp);
        if (hasRequest)
            a.setAttribute("class", "requested");
        td.append(a)
        tr.append(td);
    }
    //all details  
    function _showAllDetails(e, testOpp) {
        Y.log("tdsTestOpps._showAllDetails");
        var node = Y.one('#allDetails_Close');
        node.detach();
        node.on('click', _hideAllDetails);

        var segments = null;
        if (Y.tdsTests.isSegmented(testOpp.testKey))
            segments = Y.tdsSegments.getSegments(testOpp.testKey);

        //load the accTypes if need and then call the renderer
        _oAccTypes.loadAccs(testOpp, segments, _renderAllDetails);

        _oShared.addBodyClass(_oClassName.all_details);
        _oElem.allDetails.setXY([e.pageX, e.pageY]);
    }

    function _renderAllDetails(testOpp, segments, masterAccTypesList) {
        Y.log("tdsTestOpps._renderAllDetails");
        var allDetailsElem = _oElem.allDetails;
        var contElem = allDetailsElem.one("#allDetails_content");
        if (contElem == undefined)
            return;
        //render test opp info:
        _renderTestOppDetails(contElem, testOpp);

        //TEST level
        //set acc values and render these accs
        var masterAccTypes = masterAccTypesList[0];
        var ary = _oAccTypes.parseAccsString(testOpp.accs);

        _oAccTypes.setIsVisibleAndLabel(masterAccTypes, ary);
        var contElemSettings = allDetailsElem.one("#allDetails_contentSettings");
        if (contElemSettings == undefined)
            return;
        var allDivs = contElemSettings.getElementsByTagName('div'); //only remove div elems and leave the header
        allDivs.remove();
        contElemSettings.append(_oAccTypes.getAccDetailsViewString(ary));

        //set student name:
        var nameElem = allDetailsElem.one('#allDetails_name');
        nameElem.setContent(testOpp.name);
        
        _oShared.activateScollView(allDetailsElem.one("#pop_container_bottomHalf"));
    }
    //render the opp details page for mobile browser
    function _renderTestOppDetails(contElem, testOpp) {
        var N = Y.Node;
        contElem.setContent('');
        var divClass = 'accommodation', spanNClass = 'accomm_name', spanVClass = 'accomm_value';

        var div = _oShared.divNode(null, divClass);
        var spanName = _oShared.spanNode('Test:', spanNClass);
        var spanValue = _oShared.spanNode(testOpp.testName, spanVClass);
        div.append(spanName); div.append(spanValue); contElem.append(div);

        div = _oShared.divNode(null, divClass);
        spanName = _oShared.spanNode(_oMessages.getRaw('SSID') + ':', spanNClass);
        spanValue = _oShared.spanNode(testOpp.ssid, spanVClass);
        div.append(spanName); div.append(spanValue); contElem.append(div);

        div = _oShared.divNode(null, divClass);
        spanName = _oShared.spanNode('Opportunity Number:', spanNClass);
        spanValue = _oShared.spanNode(testOpp.opp, spanVClass);
        div.append(spanName); div.append(spanValue); contElem.append(div)

        if (testOpp.requestCount != undefined && testOpp.requestCount > 0) {
            div = _oShared.divNode(null, divClass);
            spanName = _oShared.spanNode('Requests:', spanNClass);
            var a = _oShared.aNode('Print Request', 'printRequest');
            a.on('click', _oUI.request, null, testOpp);
            spanValue = _oShared.spanNode(null, spanVClass);
            spanValue.append(a);
            div.append(spanName); div.append(spanValue); contElem.append(div);
        }  
        

        div = _oShared.divNode(null, divClass);
        spanName = _oShared.spanNode('Student Status:', spanNClass);
        spanValue = _oShared.spanNode(testOpp.displayStatus, spanVClass);
        div.append(spanName); div.append(spanValue); contElem.append(div);

        div = _oShared.divNode(null, divClass);
        spanName = _oShared.spanNode('Pause Test:', spanNClass);
        spanValue = _oShared.spanNode(null, spanVClass);
        if (testOpp.status == "started" || testOpp.status == "approved") {
            a = _oShared.aNode('Click to Pause', 'pauseTest');
            a.on('click', _pauseConfirm, null, testOpp);
            spanValue.append(a);
        } 
        div.append(spanName); div.append(spanValue); contElem.append(div);
    }
    //close the dialog
    function _hideAllDetails() {
        Y.log("tdsTestOpps._hideAllDetails");
        _oShared.removeBodyClass(_oClassName.all_details);
    }
    //accommodation details  **********************************************************************************************   
    function _showAccDetails(e, testOpp) {
        Y.log("tdsTestOpps._showAccDetails");

        var segments = null;
        if (Y.tdsTests.isSegmented(testOpp.testKey))
            segments = Y.tdsSegments.getSegments(testOpp.testKey);

        //load the accTypes if need and then call the renderer
        _oAccTypes.loadAccs(testOpp, segments, _renderAccDetails);

        _oShared.addBodyClass(_oClassName.approvals_detail);
        (e.pageY > 270) ? _oElem.accsDetails.setXY([e.pageX, e.pageY]) : _oElem.accsDetails.setXY([e.pageX, 270]);  //if pageY value must be at least the height of the acc details messagebox.  This is so buttons/info does not get cut off.
    }
    //accTypes: this test's accommodation Types
    function _renderAccDetails(testOpp, segments, masterAccTypesList) {
        Y.log("tdsTestOpps._renderAccDetails");

        var contElem = Y.one("#accsDetails_content");
        if (contElem == undefined)
            return;

        //set TEST level acc values
        var masterAccTypes = masterAccTypesList[0];
        var ary = _oAccTypes.parseAccsString(testOpp.accs);
        _oAccTypes.setIsVisibleAndLabel(masterAccTypes, ary);
        var strDetails = _oAccTypes.getAccDetailsViewString(ary);
        
        contElem.setContent(strDetails);

        //set student name:
        var nameElem = Y.one('#accsDetails_name');
        nameElem.setContent(testOpp.name);
    }

    function _hideAccDetails() {
        Y.log("tdsTestOpps.hideAccDetails");
        _oShared.removeBodyClass(_oClassName.approvals_detail);
    }

    //-------------------------------------------------------------------------
    // Public interface
    //-------------------------------------------------------------------------
    Y.tdsTestOpps = {
        //not use?
        //        init: function(sessionKey) {
        //            _load(sessionKey);
        //        },
        render: function() {
            _render();
        },
        data: function() {
            return _data;
        },
        setData: function(data) {
            _data = data;
        },
        //accommodation details  ********************************************************************************************** 
        hideAccDetails: function() {
            _hideAccDetails();
        },
        hideAllDetails: function() {
            _hideAllDetails();
        }
    };
}, "0.1", { requires: ["node", "io", "json-parse", "tds-accTypes", "p-UI", "p-Elem", "p-ClassName", "tds-hideshow", "tds-shared"] });

