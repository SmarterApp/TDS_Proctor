//required: yui-min.js
YUI.add("tds-approvalopps", function (Y) {
    //-------------------------------------------------------------------------
    // Private variables
    //-------------------------------------------------------------------------
    var _data = null;
    var _isInit = false,
        _btnApprovals,
        _btnApprovalsMobile,
        _btnRefreshApprovalOpps = null,
        _btnApprovalAll = null,
        _btnDone = null,
        _lblWaitingForApprovalsCount = null, //waiting count
        _lblWaitingForApprovalsCountMobile = null,
        _tblOfApprovalsList = null, //for approval view-only
        _divOfApprovalsList2 = null, //for actual approvals screen
        _divApprovalsShell = null,
        _spanAccsForName = null,
        _divAccsContainer = null,
        _divSegsAccsContainer = null,
        _divTestApprovalDetails = null,
        _divSegmentApprovalDetails = null,
        _divMsbSegmentApprovalDetails = null,
        _spanAccsForName_Seg = null,
        _spanAccsForName_MsbSeg = null,
        _btnSet = null,
        _btnCancel = null,
        _btnSetApprove = null;

    var _oActivateScollView = null;

    var _oSession = Y.tdsSession;
    var _sessionKey = null,
        _bPleaseWait = false,
        _seeEditDetailOpened = false;
    var _oMessages = Y.Messages;
    var _oClassName = Y.pClassName;
    var _oAccTypes = Y.tdsAccTypes;
    var _oShared = Y.tdsShared;
    var _accInputType = {
        invalid: -1,
        selectbox: 0,
        checkbox: 1,
        readonly: 2,
        invisible: 3
    };


    //-------------------------------------------------------------------------
    // Private functions
    //-------------------------------------------------------------------------
    function _elemsInit() {
        if (_isInit)
            return;
        _btnApprovals = Y.one('#btnApprovals');
        _btnApprovals.on('click', _approvals);
        _btnApprovalsMobile = Y.one('#btnApprovalsMobile');
        _btnApprovalsMobile.on('click', _approvals);

        _btnRefreshApprovalOpps = Y.one('#btnRefreshApprovalOpps');
        _btnApprovalAll = Y.one('#btnApprovalAll');
        _btnDone = Y.one('#btnDone');
        _addORremoveTopButtons(false); //remove event listeners
        _addORremoveTopButtons(true); //add event listeners

        _lblWaitingForApprovalsCount = Y.one('#lblWaitingForApprovalsCount');
        _lblWaitingForApprovalsCountMobile = Y.one('#lblWaitingForApprovalsCountMobile');

        _tblOfApprovalsList = Y.one('#tblOfApprovalsList');
        _divOfApprovalsList2 = Y.one('#divOfApprovalsList2');
        _divApprovalsShell = Y.one('#divApprovalsShell');
        _spanAccsForName = Y.one('#spanAccsForName');
        _divAccsContainer = Y.one('#divAccsContainer');
        _divSegsAccsContainer = Y.one('#divSegsAccsContainer');
        _divTestApprovalDetails = Y.one('#divTestApprovalDetails');
        _divSegmentApprovalDetails = Y.one('#divSegmentApprovalDetails');
        _divMsbSegmentApprovalDetails = Y.one('#divMsbSegmentApprovalDetails');
        _spanAccsForName_Seg = Y.one('#spanAccsForName_Seg');
        _spanAccsForName_MsbSeg = Y.one("#_spanAccsForName_MsbSeg");
        _btnSetApprove = Y.one('#btnSetApprove');
        _btnSet = Y.one('#btnSet');
        _btnCancel = Y.one('#btnCancel');

        _isInit = true;
    }
    //WaitForSegmentEntry and WaitForSegmentExit
    function _getSegmentAppText(status) {
        var key = "WaitFor" + status;
        return _oMessages.getRaw(key);
    }

    //add or remove top dialog buttons: Refresh, Approve All Students, Done
    //bAdd=true then add else remove
    function _addORremoveTopButtons(bAdd) {
        if (bAdd) {
            _btnRefreshApprovalOpps.on('click', _refresh);
            _btnApprovalAll.on('click', _approvalAll);
            _btnDone.on('click', _done);
        } else {
            _btnRefreshApprovalOpps.detach();
            _btnApprovalAll.detach();
            _btnDone.detach();
        }
    }

    function _load(bApprovalsScreen) {
        //A function handler to use for successful requests:
        Y.log('tdsApprovalOpps._load(bApprovalsScreen)--' + bApprovalsScreen);
        var oSession = Y.tdsSession;
        if (!oSession.hasSession())
            return;
        var handleSuccess = function (ioId, o) {
            if (o.responseText != undefined) {
                try {
                    var sessionDTO = Y.JSON.parse(o.responseText);
                    if (sessionDTO != undefined && sessionDTO.bReplaceApprovalOpps) {
                        _data = sessionDTO.approvalOpps;
                        Y.tdsTests.setTestName(_data);  //set test display name for all opps
                        //set testName for each testOpp here

                    }
                    if (_oShared.renderError(sessionDTO)) return;

                    if (bApprovalsScreen)
                        _render(); //render the approvals screen
                    else
                        _renderView();
                }
                catch (e) {
                    Y.log("ERROR: tdsApprovalOpps._load: " + e);
                    _oShared.showError(_oMessages.get('UnableToProcessRequest'));
                    return false;
                }
            }
        };
        var handleFailure = function (ioId, o) {
            Y.log("ERROR: tdsApprovalOpps._load: " + x);
            _oShared.showError(_oMessages.get('UnableToProcessRequest'));
        };
        var postData = "sessionKey=" + oSession.key();
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

        var url = "Services/XHR.axd/GetApprovalOpps";
        Y.io(url, cfg);
    }
    function _toString() {
        if (_data == null) {
            return;
        }
        var str = '';
        var len = _data.length;
        for (var i = 0; i < len; i++) {
            str += _data[i].rtsKey + '|';
        }
        return str;
    }
    function _hasApprovals() {
        return (_data == null || _data.length < 1) ? false : true;
    }
    function _hasSetAcc() {
        if (!_hasApprovals())
            return false;
        var len = _data.length;
        for (var i = 0; i < len; i++) {
            var testOpp = _data[i];
            if (testOpp.disabled != undefined && testOpp.disabled == true)
                continue;
            if (testOpp.setAcc == undefined || testOpp.setAcc == false)
                continue;
            return true;
        }
        return false;
    }

    function _init(bApprovalsScreen) {
        Y.log("_init");
        var oSession = Y.tdsSession;
        if (!oSession.hasSession())
            return;
        _elemsInit(); //init all elems for this

        _sessionKey = oSession.key();

        var bViewOnly = (bApprovalsScreen == undefined) ? true : bApprovalsScreen;

        _load(bViewOnly);
    }

    //Approval View ONLY ******************************************************************************************
    function _renderView() {
        Y.log("ApprovalOpps._renderView");

        _elemsInit(); ; //init all elems for this

        var tblElem = _tblOfApprovalsList;

        //remove all children tables
        var tbodies = tblElem.getElementsByTagName('tbody');
        var tbody = null;
        if (tbodies.size() < 1)
            tbody = tblElem.append('<tbody></tbody>');
        else
            var tbody = tbodies.item(0);
        tbody.setContent(''); //clear out exist table first

        _lblWaitingForApprovalsCount.setContent('0');
        _lblWaitingForApprovalsCountMobile.setContent('0');

        //recreate approvals list tables
        var testOpps = _data;
        if (testOpps == null || testOpps.length < 1) {//no data
            _oShared.removeBodyClass(_oClassName.approvalsNeeded);
            return;
        }
        _oShared.addBodyClass(_oClassName.approvalsNeeded);
        var len = testOpps.length;
        _lblWaitingForApprovalsCount.setContent(len);
        _lblWaitingForApprovalsCountMobile.setContent(len);
        for (var i = 0; i < len; i++) {
            tbody.append(_buildRowView(testOpps[i]));
        }
    }

    //Student Name, Test
    function _buildRowView(testOpp) {
        var N = Y.Node;
        var tr = N.create("<tr></tr>");
        var className = (testOpp.status == "pending") ? "test_new" : "test_continue";

        var td = N.create("<td></td>"); td.setAttribute('class', className);
        td.setContent(testOpp.name);
        tr.append(td);
        td = N.create("<td></td>");
        td.setContent(testOpp.testName);
        tr.append(td);
        return tr;
    }

    var _approvalsViewTable = null;
    function _tblApprovalsView(caption) {
        if (_approvalsViewTable != null) { //existing translated table
            var newTable = _approvalsViewTable.cloneNode(true);
            if (caption != null) {
                var capNode = newTable.getElementsByTagName('caption').item(0);
                if (capNode != null) capNode.setContent(caption);
            }
            return newTable;
        }
        var N = Y.Node;
        var table = N.create('<table><caption>' + (caption == null) ? "" : caption + '</caption></table>');
        table.append('<thead><tr><th scope="col" i18n-content="Label.StudentName">Student Name</th><th scope="col" i18n-content="Label.Test">Test</th></tr></thead><tbody></tbody>');
        Y.tds.processLanguage(table); //do the translation
        _approvalsViewTable = table.cloneNode(true);

        return table;
    }

    var _approvalsTable = null;
    function _tblApprovals(caption) {
        if (_approvalsTable != null) { //existing translated table
            var newTable = _approvalsTable.cloneNode(true);
            if (caption != null) {
                var capNode = newTable.getElementsByTagName('caption').item(0);
                if (capNode != null) capNode.setContent(caption);
            }
            return newTable;
        }
        //new table
        var N = Y.Node;
        var table = N.create('<table></table>');
        var cap = N.create('<caption></caption>');
        if (caption != null) cap.setContent(caption);
        table.append(cap);

        var thead = N.create('<thead></thead>');
        var tr = N.create('<tr></tr>');
        tr.append('<th scope="col" i18n-content="Label.StudentName">Student Name</th>');
        tr.append('<th scope="col" i18n-content="Label.SSID">SSID</th>');
        tr.append('<th scope="col" i18n-content="Label.OppNum">Opp #</th>');
        tr.append('<th scope="col" class="table_accomm" i18n-content="Label.Accommodations">Accommodations</th>');
        tr.append('<th scope="col" class="action" colspan="2" i18n-content="Label.Action"></th>');
        tr.append('<th scope="col" class="error"></th>');
        if (_oShared.mobile())
            tr.append('<th scope="col" class="table_details" i18n-content="Label.SeeDetails">See Details</th>');

        thead.append(tr);
        table.append(thead);
        table.append('<tbody></tbody>');
        Y.tds.processLanguage(table); //do the translation
        _approvalsTable = table.cloneNode(true);
        return table;
    }

    //Approvals ********************************************************************************************
    function _setPleaseWait(bOn) {
        if (bOn) {
            if (!_bPleaseWait) {
                _bPleaseWait = true;
                _oShared.addBodyClass(_oClassName.please_wait);
            }
        }
        else {
            _bPleaseWait = false;
            _oShared.removeBodyClass(_oClassName.please_wait);
        }
    }

    function _approvals() {
        Y.log("_approvals");
        if (!_hasApprovals())
            return;
        Y.pUI.stopAutoRefresh(); //stop the auto refresh
        //call render to render the approvals screen from the current P.ApprovalOpp.data
        _render();
    }
    function _refresh() {
        Y.log("_refresh");
        //confirm if there are waiting opps with setAcc=true
        if (_hasSetAcc()) {
            var msgDialog = _oShared.msgDialog("Refresh Approvals Screen", _oMessages.get("All set accommodations for waiting students will be lost. Would you like to continue?"));
            msgDialog.addButton("confirm", Y.tds.messages.getRaw("Button.Yes"), _refreshConfirm);
            msgDialog.addButton("close", Y.tds.messages.getRaw("Button.No"), msgDialog.cancel);
            msgDialog.show(_oShared.NotificationType.warning);
            return;
        }
        else
            _refreshConfirm();
    }

    function _refreshConfirm() {
        Y.log("_refreshConfirm");
        _setPleaseWait(true);
        _init(true); // hit the server and re-render the approvals screen
        var msgDialog = _oShared.msgDialog(null, null);
        msgDialog.cancel(); // close the dialog if any
    }

    function _approvalAll() {
        Y.log("_approvalAll");
        var testOpps = _data;
        if (testOpps == null || testOpps.length < 1) {//no data
            _done();
            return;
        }
        var str = _oMessages.get("Please confirm that you would approve all {0} of these students to begin their tests.", [_getNumActiveTestOpps()]);
        str += "<br/><br/>";
        str += _getTestsAndCountsStr(testOpps);
        str += "<br/>";
        str += _oMessages.getRaw("Note: Some test settings cannot be changed once the student has been approved.");

        var msgDialog = _oShared.msgDialog(Y.tds.messages.getRaw("Important!"), str);
        msgDialog.addButton("confirm", Y.tds.messages.getRaw("Button.Yes"), _approvalAllConfirm);
        msgDialog.addButton("close", Y.tds.messages.getRaw("Button.No"), msgDialog.cancel);
        msgDialog.show(_oShared.NotificationType.info);
        return;
    }

    //return a string of one line per <test>:<count>
    function _getTestsAndCountsStr(testOpps) {
        var aryTestLabel = _getTestsAndCounts(testOpps);
        if (aryTestLabel == null) return "";
        var str = "";
        for (var key in aryTestLabel) {
            var value = aryTestLabel[key];
            str += value.testlabel + ": " + value.count + "<br/>";
        }
        return str;
    }

    //return an hash array of {testlabel, count}
    function _getTestsAndCounts(testOpps) {
        if (testOpps == null || testOpps.length < 1) {//no data
            return null;
        }
        var len = testOpps.length;
        var ary = new Array();
        var tdsSegs = Y.tdsSegments;
        for (var i = 0; i < len; i++) {
            if (testOpps[i].disabled == undefined || testOpps[i].disabled == false) {
                var testOpp = testOpps[i];
                var key = testOpp.testKey + '_' + testOpp.waitSegment;
                var test = ary[key];
                var label = testOpp.testName;
                if (testOpp.IsSegmentApproval) {//get the segment infor and set the tblLabel
                    var seg = tdsSegs.getSegmentByPosition(testOpp.testKey, testOpp.waitSegment);
                    var l = (seg == null) ? "" : seg.label
                    label += " - " + l;
                }
                if (test == undefined) {
                    test = { testlabel: label, count: 1 };
                    ary[key] = test;
                }
                else
                    test.count++;
            }
        }
        return ary;
    }

    function _approvalAllConfirm() {
        Y.log("_approvalAllConfirm");
        var msgDialog = _oShared.msgDialog(null, null);
        msgDialog.cancel(); //close the dialog
        var testOpps = _data;
        if (testOpps == null || testOpps.length < 1) {//no data
            _done();
            return;
        }
        var len = testOpps.length;
        for (var i = 0; i < len; i++) {
            if (testOpps[i].disabled == undefined || testOpps[i].disabled == false)
                _approve(null, testOpps[i]);
        }
    }

    function _done() {
        Y.log("_done");
        //confirm if there are waiting opps with setAcc=true
        if (_hasSetAcc()) {
            var msgDialog = _oShared.msgDialog(Y.tds.messages.getRaw("Important!"), _oMessages.get("All set accommodations for waiting students will be lost. Would you like to continue?"));
            msgDialog.addButton("confirm", Y.tds.messages.getRaw("Button.Yes"), _doneConfirm);
            msgDialog.addButton("close", Y.tds.messages.getRaw("Button.No"), msgDialog.cancel);
            msgDialog.show(_oShared.NotificationType.warning);
            return;
        }
        else
            _doneConfirm();
    }

    function _doneConfirm() {
        Y.log("_doneConfirm");
        //make sure refresh the current active testees table
        _oSession.refreshCurTestees();
        //remove the show_details css class
        _removeShowDetails();
        //remove the class and then call Y.pUI.refresh
        _oShared.removeBodyClass(_oClassName.approvals_window);

        _oShared.addClosing(); //for mobile

        Y.pUI.refresh(); //this will pulling the data from the server again and then restart the auto refresh
        Y.pUI.startAutoRefresh();
        var msgDialog = _oShared.msgDialog(null, null);
        msgDialog.cancel(); // close the dialog if any
    }

    function _removeRow(testOpp) {
        testOpp.disabled = true;
        var elemTR = document.getElementById(_oShared.buildOppId("approvalTR_", testOpp));
        if (elemTR == null) return;
        var tbody = elemTR.parentNode;
        tbody.removeChild(elemTR);
        if (tbody.hasChildNodes()) {
            _removeShowDetails();
            return;
        }
        var table = tbody.parentNode; //remove table
        var divElem = _divOfApprovalsList2;
        divElem.removeChild(table);
        if (divElem.hasChildNodes()) {
            _removeShowDetails();
            return;
        }
        _done(); //no more students, close the pop up
    }

    function _removeShowDetails() {
        if (_divApprovalsShell != null)
            _divApprovalsShell.removeClass(_oClassName.show_details);
    }

    //show the error message from a test opp row on an approval screen
    //testOpp: current testOpp row
    //errorMessage: if null then hide/clear the error instead else show the error
    function _showErrorRow(testOpp, errorMsg) {
        if (testOpp == null)
            return;

        var id = _oShared.buildOppId("approvalTR_", testOpp);
        var domElem = document.getElementById(id);
        var elemTR = Y.one(domElem)

        if (elemTR == null)
            return;

        if (errorMsg == null)
            elemTR.removeClass("problem");
        else
            elemTR.addClass("problem");

        var elemTDError = elemTR.one('.error');

        if (elemTDError == null) return;
        elemTDError.setContent('<span>' + errorMsg + '</span>');
    }

    function _approve(e, testOpp) {
        Y.log("_approve");
        if (testOpp == undefined || testOpp == null) {
            _oShared.showError(_oMessages.get('UnableToProcessRequest'));
            return;
        }
        //that.style.display = 'none'; //hide the button right away
        //remove the row error if any
        _showErrorRow(testOpp, null);

        //A function handler to use for successful requests:
        var handleSuccess = function (ioId, o) {
            if (o.responseText != undefined) {
                try {
                    var returnedStatus = Y.JSON.parse(o.responseText);
                    if (returnedStatus.status != undefined && returnedStatus.status.length > 0) {//failed
                        if (!_oShared.renderError(returnedStatus)) //if not handle by renderError function
                            _showErrorRow(testOpp, returnedStatus.reason);
                    }
                    else { //remove the row from the table if this is the last row remove the table
                        _removeRow(testOpp);
                    }
                }
                catch (e) {
                    _oShared.showError(_oMessages.get('UnableToProcessRequest'));
                    Y.log("ERROR: _approve: " + e);
                }
            }
        };
        var handleFailure = function (ioId, o) {
            _oShared.showError(_oMessages.get('UnableToProcessRequest'));
            Y.log("ERROR: _approve: handleFailure");
        };

        var postData = "sessionKey=" + _oSession.key() + "&oppKey=" + testOpp.oppKey + "&accs=" +
            encodeURIComponent(_oAccTypes.getAccCodes(testOpp.accTypesList));
        //Y.log("postData=" + _oAccTypes.getAccCodes(testOpp.accTypesList));
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
        Y.io("Services/XHR.axd/ApproveOpportunity", cfg);
    }

    function _denyCancel() {
        //remove the css class
        _divApprovalsShell.removeClass(_oClassName.show_denial);
    }

    //deny and close the details dialog
    function _denyAndClose(e, testOpp) {
        _showEditDetailDialog(false, testOpp);
        _deny(e, testOpp);
    }

    function _deny(e, testOpp) {
        Y.log("_deny");
        if (testOpp == undefined || testOpp == null) {
            _oShared.showError(_oMessages.get('UnableToProcessRequest'));
            return;
        }
        //remove the row error if any
        _showErrorRow(testOpp, null);

        _divApprovalsShell.addClass(_oClassName.show_denial);

        var denialOk = Y.one('#btnDenialOK');
        var denialCancel = Y.one('#btnDenialCancel');
        denialOk.detach();
        denialCancel.detach();
        denialOk.on('click', _denialOk, null, testOpp);
        denialCancel.on('click', _denyCancel);

        //add a css class and get the reason for denial
        testOpp.reason = '';
        var txtElem = Y.one('#txtReasonForDenial');
        if (txtElem != undefined) {
            txtElem.set('value', '');
            txtElem.focus(); //set focus, container MUST be visible first.
        }
    }

    function _denialOk(e, testOpp) {
        Y.log("_denialOk");
        if (testOpp == undefined || testOpp == null) {
            _oShared.showError(_oMessages.get('UnableToProcessRequest'));
            return;
        }
        var handleSuccess = function (ioId, o) {
            try {
                var returnedStatus = Y.JSON.parse(o.responseText);
                if (returnedStatus.status != undefined && returnedStatus.status == 'failed') {//failed
                    _showErrorRow(testOpp, returnedStatus.reason);
                }
                else { //remove the row from the table if this is the last row remove the table
                    _removeRow(testOpp);
                }
            }
            catch (x) {
                _oShared.showError(_oMessages.get('UnableToProcessRequest'));
                Y.log("ERROR: _denialOK: " + x);
                return;
            }
        };
        var handleFailure = function (ioId, o) {
            _oShared.showError(_oMessages.get('UnableToProcessRequest'));
            Y.log("ERROR: _denialOK: handleFailure");
        };

        //get reason for denial
        var txtElem = Y.one('#txtReasonForDenial');
        if (txtElem == undefined)
            testOpp.reason = '';
        else testOpp.reason = txtElem.get('value');
        //remove the css class
        _divApprovalsShell.removeClass(_oClassName.show_denial);

        var postData = "sessionKey=" + _oSession.key() + "&oppKey=" + testOpp.oppKey + "&reason=" + escape(testOpp.reason);
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
        Y.io("Services/XHR.axd/DenyOpportunity", cfg);
        if (_oActivateScollView != null)
            _oActivateScollView.reset();
    }

    //display details for segment approval
    function _seeEditDetails_SegApproval(e, testOpp) {
        Y.log("_seeEditDetails_SegApproval");
        _showEditDetailDialog(true, testOpp);
        _addORremoveTopButtons(false); //remove event listeners

        //add show_details css class to the approvals_shell elem
        _divApprovalsShell.addClass(_oClassName.show_details);
        _divTestApprovalDetails.hide();
        _divSegmentApprovalDetails.show();
        _spanAccsForName_Seg.setContent(testOpp.name)

        //approve
        var btn = Y.one("#btnApprove_Seg");
        btn.detach();
        btn.on('click', _approveAndClose, null, testOpp);
        //denied
        btn = Y.one("#btnDeny_Seg");
        btn.detach();
        btn.on('click', _denyAndClose, null, testOpp);
        //cancel
        btn = Y.one("#btnCancel_Seg");
        btn.detach();
        btn.on('click', _cancelAcc, null, testOpp);
    }

    //display details for MSB segment approval
    function _seeEditDetails_MsbSegApproval(e, testOpp) {
        Y.log("_seeEditDetails_MsbSegApproval");
        _showEditDetailDialog(true, testOpp);
        _addORremoveTopButtons(false); //remove event listeners

        //add show_details css class to the approvals_shell elem
        _divApprovalsShell.addClass(_oClassName.show_details);
        _divTestApprovalDetails.hide();
        _divMsbSegmentApprovalDetails.show();
        _spanAccsForName_MsbSeg.setContent(testOpp.name)

        //approve
        var btn = Y.one("#btnApprove_MsbSeg");
        btn.detach();
        btn.on('click', _approveAndClose, null, testOpp);
        //denied
        btn = Y.one("#btnDeny_MsbSeg");
        btn.detach();
        btn.on('click', _denyAndClose, null, testOpp);
        //cancel
        btn = Y.one("#btnCancel_MsbSeg");
        btn.detach();
        btn.on('click', _cancelAcc, null, testOpp);
    }

    function _seeEditDetails(e, testOpp) {
        Y.log("_seeEditDetails");
        var segments = null;
        if (Y.tdsTests.isSegmented(testOpp.testKey))
            segments = Y.tdsSegments.getSegments(testOpp.testKey);
        _divTestApprovalDetails.show();
        _divSegmentApprovalDetails.hide();
        _divMsbSegmentApprovalDetails.hide()
        _showEditDetailDialog(true, testOpp);
        //Get all possible accom if not exists
        _oAccTypes.loadAccs(testOpp, segments, _renderAccsEdit);
    }

    //render Accs Edit screen
    //testOpp: current test opp
    //accTypes: List of accTypes for this testee's test
    function _renderAccsEdit(testOpp, segments, masterAccTypesList) {
        Y.log("_renderAccsEdit");
        if (testOpp == undefined) {
            _oShared.showError(_oMessages.get('UnableToProcessRequest'));
            return;
        }
        if (masterAccTypesList == undefined) {
            _oShared.showError(_oMessages.get('InvalidAccTypes'));
            return;
        }
        //set and approve
        _btnSetApprove.detach();
        _btnSetApprove.on('click', _setApproveAcc, null, testOpp);
        //set
        _btnSet.detach();
        _btnSet.on('click', _setAcc, null, testOpp);
        //cancel
        _btnCancel.detach();
        _btnCancel.on('click', _cancelAcc, null, testOpp);

        if (_oShared.mobile()) {
            var btnDeny = _divApprovalsShell.one('#btnDeny');
            btnDeny.on('click', _deny, null, testOpp);
        }

        _addORremoveTopButtons(false); //remove event listeners
        //add show_details css class to the approvals_shell elem
        _divApprovalsShell.addClass(_oClassName.show_details);

        //TEST level accommodations
        var masterAccTypes = masterAccTypesList[0]; //first elem always will be test level accs
        var oppAccTypes = testOpp.accTypesList[0]; //test level accs
        //set student name
        _spanAccsForName.setContent(testOpp.name)
        _renderOnAccTypesEdit(testOpp, oppAccTypes, masterAccTypes, _divAccsContainer);

        var oppAccTypesLen = testOpp.accTypesList.length;
        //SEGMENT level accs
        _divSegsAccsContainer.setContent(''); //clean out
        if (segments == null || oppAccTypesLen <= 1) return; //no need to reapprove segment accommodations
        var len = segments.length;
        for (var i = 0; i < len; i++) {
            masterAccTypes = masterAccTypesList[i + 1];  //_oAccTypes.getAccs(segment.key);
            oppAccTypes = testOpp.accTypesList[i + 1];
            if (masterAccTypes == null || masterAccTypes.length < 1 || oppAccTypes == null || oppAccTypes.length < 1) continue; //dont render anything if no accs
            var segment = segments[i];
            _divSegsAccsContainer.append(_oShared.h2Node(_oShared.buildSegmentLabel(segment), null)); //h2 label elem
            var detailElem = _oShared.divNode(null, "accommodation_container");
            detailElem.set('id', 'divSegsAccsContainer_' + i);
            _renderOnAccTypesEdit(testOpp, oppAccTypes, masterAccTypes, detailElem);
            _divSegsAccsContainer.append(detailElem);
        }
    }
    //call by _renderAccsEdit
    //render one accTypes
    function _renderOnAccTypesEdit(testOpp, oppAccTypes, masterAccTypes, detailElem) {
        detailElem.setContent(''); //clean out
        if (masterAccTypes == null || masterAccTypes == undefined) return;

        var len = masterAccTypes.length
        for (var i = 0; i < len; i++) { //each acc type, build a select box
            var masterAccType = masterAccTypes[i].Value;
            _renderOneAccTypeEdit(testOpp, oppAccTypes, masterAccTypes, masterAccType, detailElem);

            //tool dependencies
            var accDepParentTypes = masterAccType.accDepParentTypes;
            if (accDepParentTypes != null && accDepParentTypes.length > 0) { //call _renderOneAccTypeEdit for each dependencies
                _renderAccsDependencies(testOpp, oppAccTypes, masterAccTypes, detailElem, masterAccType.Type, accDepParentTypes);
            }
        }
    }

    //call by _renderOnAccTypesEdit
    //render one accType
    function _renderOneAccTypeEdit(testOpp, oppAccTypes, masterAccTypes, masterAccTypeV, detailElem) {
        if (masterAccTypeV == null || masterAccTypeV == undefined) return;

        //tool dependencies
        var dependOnType = masterAccTypeV.dependOnType;
        if (dependOnType != null && dependOnType.length > 0)
            return;

        var contElem = null;
        //NOTE: from Larry:
        //If the status is ‘pending’ then the test has not officially started and any accommodation available to the proctor can be changed.
        //If the status is ‘suspended’, then the test has officially started and any accommodations whose AllowChange flag = 0 cannot be altered.

        //NOTE: from H-A
        //the test has officially started when student clicked on "Begin Test Now"
        //If the status is ‘SegmentEntry’ / SegmentExit, then we dont have to approve accommodations
        var divAcc = _oShared.divNode(null, "accommodation");
        var accTypeLabel = _oAccTypes.getAccTypeLabel(masterAccTypeV);
        divAcc.setAttribute('name', masterAccTypeV.Type);
        divAcc.setAttribute('id', _oShared.accTypeDivId(masterAccTypeV.Type));
        divAcc.setAttribute('sortOrder', masterAccTypeV.sOrder);

        divAcc.append(_oShared.spanNode(accTypeLabel + ':', "accomm_name"));

        var bEnable = (testOpp.status == 'pending') ? true : masterAccTypeV.AllowChange;

        if (!bEnable || !masterAccTypeV.IsSelectable || !masterAccTypeV.IsVisible) {
            if (!masterAccTypeV.IsVisible) { //invisible acctype
                divAcc.addClass('withInvisible');
                divAcc.hide();
            }
            else
                divAcc.addClass('withReadonly');
            var selectedAccType = _oAccTypes.getAccType(masterAccTypeV.Type, oppAccTypes); //get selected values
            var selectedAccTypeV = null;
            if (selectedAccType == null)
                selectedAccTypeV = masterAccTypeV;
            else {
                //get only selected values that is a subset of the master list,
                //use the default from the test master list if selected values is null
                selectedAccTypeV = _oAccTypes.getSelectedAccTypeValue(selectedAccType.Value, masterAccTypeV);
                if (selectedAccTypeV == null)
                    selectedAccTypeV = masterAccTypeV;
            }
            divAcc.append(_oAccTypes.buildAccValueSpan(selectedAccTypeV));
        }
        else {
            if (masterAccTypeV.allowCombineCount < 2) {
                divAcc.append(_oAccTypes.buildSelBox(testOpp, oppAccTypes, masterAccTypeV, masterAccTypes));
                divAcc.addClass('withSelect');
            }
            else { //render checkbox group
                divAcc.addClass('withChecks');
                var selectedAccType = _oAccTypes.getAccType(masterAccTypeV.Type, oppAccTypes); //get selected values
                var selectedAccTypeV = null;
                if (selectedAccType == null)
                    selectedAccTypeV = masterAccTypeV;
                else {
                    //get only selected values that is a subset of the master list,
                    //use the default from the test master list if selected values is null
                    selectedAccTypeV = _oAccTypes.getSelectedAccTypeValue(selectedAccType.Value, masterAccTypeV);
                    if (selectedAccTypeV == null)
                        selectedAccTypeV = masterAccTypeV;
                }
                divAcc.append(_oAccTypes.buildAccValueSpan(selectedAccTypeV));

                //add edit/done buttons for mobile
                var aBtn = _oShared.aNode("Edit", "accomm_edit");
                aBtn.on('click', _accValueEditOnclick);
                divAcc.append(aBtn);
                aBtn = _oShared.aNode("Done", "accomm_save");
                aBtn.on('click', _accValueEditOnclick);
                divAcc.append(aBtn);
                //add check box group div and ul here...
                divAcc.append(_oAccTypes.buildCheckBoxGroup(testOpp, oppAccTypes, masterAccTypeV, masterAccTypes));
                divAcc.append(_oShared.spanNode(null, "clear"));
            }
        }
        _appendAccElem(detailElem, divAcc);
    }

    function _appendAccElem(parentElem, childElem) {
        if (parentElem == null || childElem == null) return;

        var pElems = parentElem.all('.accommodation');
        var len = pElems.size();
        if (pElems == null || len < 1) {
            parentElem.append(childElem);
            return;
        }

        var iSortOrder = childElem.getAttribute('sortOrder');

        for (var i = 0; i < len; i++) { //for each div elem
            var accDiv = pElems.item(i); //acc div

            var sortOrder = accDiv.getAttribute('sortOrder');
            if (iSortOrder <= sortOrder) {
                parentElem.insertBefore(childElem, accDiv);
                return;
            }
        }
        parentElem.append(childElem); //add anyway...

    }

    function _accValueEditOnclick(e) {
        var parentDiv = this.get('parentNode');
        if (parentDiv == null) return;
        var className = "editing";
        if (parentDiv.hasClass(className))
            parentDiv.removeClass(className);
        else
            parentDiv.addClass(className);
    }

    //parentAccTypeName: name of the acc type with children in accDepParentTypes
    //accDepParentTypes: dependencies accTypes for each parent selected values
    //render all dependencies base on selected parent values
    function _renderAccsDependencies(testOpp, oppAccTypes, masterAccTypes, detailElem, parentAccTypeName, accDepParentTypes) {
        if (accDepParentTypes == null) return;
        //get selected parent values
        var selectedAccType = _oAccTypes.getAccType(parentAccTypeName, oppAccTypes); //get selected values
        if (selectedAccType == null || selectedAccType == undefined) return;
        var selectedAccTypeV;
        if (selectedAccType.prevSelectedValues == undefined)
            selectedAccTypeV = _oAccTypes.getSelectedAccCode(selectedAccType); //get selected values
        else
            selectedAccTypeV = selectedAccType.prevSelectedValues; //prev selected values

        _oAccTypes.removeAllaccDepChildren(detailElem, accDepParentTypes); //remove all depended elems first

        var accDepParentLen = accDepParentTypes.length;
        var selectedLen = selectedAccTypeV.length;
        for (var i = 0; i < selectedLen; i++) { //for each selected parent values
            var selectedCode = selectedAccTypeV[i];
            for (var j = 0; j < accDepParentLen; j++) { //for each parent values
                var accDepParentType = accDepParentTypes[j];
                var ifType = accDepParentType.ifType;
                var ifValue = accDepParentType.ifValue;

                if (ifType != parentAccTypeName || ifValue != selectedCode) continue; //check to see if selected values even have dependencies
                var accDepChildTypes = accDepParentType.accDepChildTypes;
                if (accDepChildTypes == null) continue;

                var depTypeLen = accDepChildTypes.length;
                for (var k = 0; k < depTypeLen; k++) {
                    var accDepChildType = accDepChildTypes[k];
                    var thenType = accDepChildType.thenType;
                    var thenValues = accDepChildType.thenValues;
                    if (thenType == null || thenType.length < 1 || thenValues == null || thenValues.length < 1) continue;

                    var masterAccType = _oAccTypes.getAccType(thenType, masterAccTypes);
                    if (masterAccType == null) {
                        Y.log('ERROR: TestID: "' + testOpp.testID + '" and Accommodation type dependencies: "' + thenType + '" does not exists in the Client_TestToolType.');
                        continue;
                    }
                    //make a copy
                    var newMasterAccType = _oAccTypes.copyAccType(masterAccType, thenValues);

                    _renderOneAccTypeEdit(testOpp, oppAccTypes, masterAccTypes, newMasterAccType.Value, detailElem);
                }
            }
        }
    }

    //approval and close the details dialog
    function _approveAndClose(e, testOpp) {
        _showEditDetailDialog(false, testOpp);
        _approve(e, testOpp);
    }

    //set and approve accoms
    function _setApproveAcc(e, testOpp) {
        Y.log("_setApproveAcc");
        //set
        _setAcc(e, testOpp);
        //approve
        _approve(e, testOpp);
    }

    //set proctor's selected accoms
    function _setAcc(e, testOpp) {
        Y.log("_setAcc");
        //remove the show_details css class
        //alter selected accTypes
        //update the accs string
        _showEditDetailDialog(false, testOpp);

        //TEST level
        var detailElem = _divAccsContainer;
        //get all select box elems on the page
        var divElems = detailElem.all('.accommodation');
        var masterAccTypes = Y.tdsAccTypes.getAccs(testOpp.testKey);
        var oppAccTypesList = testOpp.accTypesList;
        _setAcc1(testOpp, oppAccTypesList[0], masterAccTypes, divElems);
        //check to see if this current set accs are changed from the default set for "Custom vs Standard settings"
        testOpp.custAccs = _oAccTypes.isCustomAccs(oppAccTypesList[0], masterAccTypes);

        var oppAccTypesLen = oppAccTypesList.length;
        if (oppAccTypesLen > 1) { //no need to approve segment's accs again, only approve the very first time
            var segments = null;
            if (Y.tdsTests.isSegmented(testOpp.testKey))
                segments = Y.tdsSegments.getSegments(testOpp.testKey);

            if (segments != null) {
                //SEGMENT level accs
                var len = segments.length;
                for (var i = 0; i < len; i++) {
                    //get all select box elems on the page
                    detailElem = Y.one("#divSegsAccsContainer_" + i);
                    if (detailElem == null) //no accs for this segment
                        continue;
                    divElems = detailElem.all('.accommodation');
                    if (divElems == null) //no accs for this segment
                        continue;

                    masterAccTypes = Y.tdsAccTypes.getAccs(segments[i].key);
                    oppAccTypes = oppAccTypesList[i + 1];
                    _setAcc1(testOpp, oppAccTypes, masterAccTypes, divElems);

                    //check to see if this current set accs are changed from the default set for "Custom vs Standard settings"
                    if (!testOpp.custAccs)
                        testOpp.custAccs = _oAccTypes.isCustomAccs(oppAccTypes, masterAccTypes);
                }
            }

        }
        //update accs string
        _alterAccsString(testOpp);
        _oShared.addClosing();
        if (_oActivateScollView != null)
            _oActivateScollView.reset();
    }
    //call by _setAcc
    //set list of select boxes / check boxes / span elems
    function _setAcc1(testOpp, oppAccTypes, masterAccTypes, divElems) {
        if (divElems == undefined || divElems.size() < 1)
            return;
        var len = divElems.size();
        for (var i = 0; i < len; i++) { //for each div elem
            var accDiv = divElems.item(i); //acc div

            //get selected elem (single selectbox or div container for checkbox group
            var selectedElem, name, options;

            name = accDiv.getAttribute('name');
            var selectedAccType = _oAccTypes.getAccType(name, oppAccTypes);

            if (selectedAccType == undefined) { //build new accType object if not exists
                oppAccTypes[oppAccTypes.length] = _oAccTypes.newAccType(name);
                selectedAccType = _oAccTypes.getAccType(name, oppAccTypes);
            }
            if (selectedAccType.Value == undefined)
                selectedAccType.Value = {}; //add the new type if not exists
            selectedAccType.Value.Values = new Array();   //alter selected accTypes in ApprovalOpps
            //get values
            selectedAccType.Value.Values = _getInputValues(accDiv, selectedAccType);
            selectedAccType.setAcc = true;
        }
        //remove extra tool from tool dependencies
        //NOTE (09/22/2011): This will not remove from the db, Larry does this in the sp
        len = oppAccTypes.length;
        i = 0;
        while (i < len) {
            if (!oppAccTypes[i].setAcc) {
                oppAccTypes.splice(i, 1);
                len = oppAccTypes.length; //shorter len
            }
            else {
                i++;
            }
        }
        testOpp.setAcc = true;
    }

    function _getInputType(accDiv) {
        if (accDiv == null)
            return _accInputType.invalid;
        if (accDiv.hasClass('withChecks'))
            return _accInputType.checkbox;
        if (accDiv.hasClass('withSelect'))
            return _accInputType.selectbox;
        if (accDiv.hasClass('withReadonly'))
            return _accInputType.readonly;
        if (accDiv.hasClass('withInvisible'))
            return _accInputType.invisible;
        return _accInputType.invalid;
    }

    function _getInputValues(accDiv, selectedAccType) {
        if (accDiv == null || selectedAccType == null) return null;

        var inputType = _getInputType(accDiv);
        var elem, options, allowCombine = false, size, selectedVals;
        var selectedVals = selectedAccType.Value.Values;

        switch (inputType) {
            case _accInputType.checkbox:
                elem = accDiv.getElementsByTagName('div').item(0);
                options = elem.getElementsByTagName('input');
                size = options.size();
                for (var opt = 0; opt < size; opt++) { //for each option
                    var item = options.item(opt);
                    if (item.get('checked'))
                        selectedVals[selectedVals.length] = _oAccTypes.buildAccValue(allowCombine, item.get('id'), true, item.get('value')); //for checkbox
                }
                break;
            case _accInputType.selectbox:
                elem = accDiv.getElementsByTagName('select').item(0);
                options = elem.get('options');
                size = options.size();
                for (var opt = 0; opt < size; opt++) { //for each option
                    var item = options.item(opt);
                    if (item.get('selected'))
                        selectedVals[selectedVals.length] = _oAccTypes.buildAccValue(allowCombine, item.get('value'), true, item.get('innerHTML')); //for selectbox
                }
                break;
            case _accInputType.readonly:
            case _accInputType.invisible:
                elem = accDiv.one('.accomm_value');

                var names = elem.getAttribute('name'); //| delimiter of acc codes
                var values = elem.getAttribute('value'); //| delimiter of acc codes
                var aryNames = names.split("|");
                var aryValues = values.split("|");
                size = aryNames.length;
                for (var opt = 0; opt < size; opt++) {
                    selectedVals[selectedVals.length] = _oAccTypes.buildAccValue(allowCombine, aryNames[opt], true, aryValues[opt]); //for readonly/invisible
                }
                break;
        }
        return selectedVals;
    }


    //update the accs string display on the approval screen after alter by the proctor
    function _alterAccsString(testOpp) {
        //get the current accs string row and then replace with new one
        Y.log("_alterAccsString");
        var id = _oShared.buildOppId("approvalTR_", testOpp);
        var domElem = document.getElementById(id);
        var tr = Y.one(domElem)

        var newTR = _buildRow(testOpp);
        tr.replace(newTR);
    }

    function _cancelAcc(e, testOpp) {
        Y.log("_cancelAcc");
        _showEditDetailDialog(false, testOpp);
        _oShared.addClosing(); //for mobile
    }

    //remove row selected class
    function _showEditDetailDialog(bShow, testOpp) {
        _seeEditDetailOpened = bShow;
        //set a class to the approval shell; add the accs content to detail div
        var id = _oShared.buildOppId("approvalTR_", testOpp);
        var domElem = document.getElementById(id);
        var elemTR = Y.one(domElem)
        if (bShow) {
            if (elemTR != null) elemTR.addClass("test selected");
        }
        else {
            if (elemTR != null) elemTR.removeClass("selected"); //remove selected row
            //just remove the show_details css class
            _divApprovalsShell.removeClass(_oClassName.show_details);
            _addORremoveTopButtons(false); //remove event listeners
            _addORremoveTopButtons(true); //add event listeners
        }
    }

    function _render() {
        Y.log("_render");
        _setPleaseWait(false);

        var divElem = _divOfApprovalsList2;

        //remove all children tables
        divElem.setContent('');

        //recreate approvals list tables
        var testOpps = _data;
        if (testOpps == null || testOpps.length < 1) {//no data
            _done();
            return;
        }
        _oShared.addBodyClass(_oClassName.approvals_window);
        var tdsSegs = Y.tdsSegments;
        var isSegmentApproval = null;
        var waitSegment = null;
        var testKey = null;
        var table = null;
        var tbody = null;
        var len = testOpps.length;
        for (var i = 0; i < len; i++) {
            var testOpp = testOpps[i];

            _loadDefaultAccs(testOpp); //patch all acc dependencies default values

            var curTestKey = testOpp.testKey;
            var curWaitSegment = testOpp.waitSegment;
            var status = testOpp.status;
            testOpp.IsSegmentApproval = (status == "segmentEntry" || status == "segmentExit") ? true : false;
            if (testKey == curTestKey && waitSegment == curWaitSegment && isSegmentApproval == testOpp.IsSegmentApproval) // same test and segment
            {
                tbody.append(_buildRow(testOpp));
            }
            else { //diff test
                testKey = curTestKey;
                waitSegment = curWaitSegment;
                isSegmentApproval = testOpp.IsSegmentApproval;
                var tblLabel = testOpp.testName;

                //check if this is a segment entry or exit
                if (testOpp.IsSegmentApproval) {//get the segment infor and set the tblLabel
                    var seg = tdsSegs.getSegmentByPosition(testKey, waitSegment);
                    var label = (seg == null) ? "" : seg.label
                    tblLabel += " - " + label;
                }

                table = _tblApprovals(tblLabel);
                divElem.append(table);

                var tbodies = table.getElementsByTagName('tbody');
                tbody = null;
                if (tbodies.size() < 1)
                    tbody = table.append('<tbody></tbody>');
                else
                    tbody = tbodies.item(0);
                tbody.append(_buildRow(testOpp));
            }
        }
        _oActivateScollView = _oShared.activateScollView(Y.one("#approvals_shell_bottomHalf"));
    }

    //Name, SSID, Opp#, Accommodations, Action
    function _buildRow(testOpp) {
        var N = Y.Node;
        var tr = _oShared.trNode();
        //assign tr an ID for hide/show
        tr.setAttribute('id', _oShared.buildOppId("approvalTR_", testOpp));
        var status = testOpp.status;
        var className = (status == "pending") ? "test_new" : "test_continue";
        var td = _oShared.tdNode(testOpp.name, className);
        tr.append(td);
        td = _oShared.tdNode(testOpp.ssid, null);
        tr.append(td);
        td = _oShared.tdNode(testOpp.opp, "table_opp opp" + testOpp.opp);
        tr.append(td);

        td = _oShared.tdNode(null, "table_accomm");
        var span = _oShared.spanNode(null, "positioner");
        // This property initiates the multi-stage braille flow
        var isMsbApproval = testOpp.msb;
        var btnDetailsLabel;
        //if segment approval
        if (testOpp.IsSegmentApproval) {
            span.setContent(_getSegmentAppText(status))
            btnDetailsLabel = _getBtnDetailsLabel();
        }
        else {
            span.setContent((testOpp.custAccs) ? _getCustomSettingsLabel() : _getStandardSettingsLabel());
            btnDetailsLabel = _getBtnSeeDetailsLabel();
        }
        span.setAttribute('id', _oShared.buildOppId("accsStr_", testOpp));

        var a = _oShared.aNode(btnDetailsLabel, "positioner");
        //if segment approval
        if (testOpp.IsSegmentApproval && !isMsbApproval) {
            a.on('click', _seeEditDetails_SegApproval, null, testOpp);
            td.append(span);
        } else if (testOpp.status == "segmentExit"  && isMsbApproval) {
            td.append("Waiting for MSB Package for " + testOpp.testName);
        } else {
            a.on('click', _seeEditDetails, null, testOpp);
            td.append(span);
            td.append(a);
        }

        tr.append(td);
        if ((testOpp.IsSegmentApproval && !isMsbApproval) || !testOpp.IsSegmentApproval) {
            td = _oShared.tdNode(null, "approve");
            a = _oShared.aNode(_getBtnApproveLabel(), null);
            a.on('click', _approve, null, testOpp);
            td.append(a);
            tr.append(td);

            td = _oShared.tdNode(null, "deny");
            a = _oShared.aNode(_getBtnDenyLabel());
            a.on('click', _deny, null, testOpp);
            td.append(a);
            tr.append(td);

            td = _oShared.tdNode(null, "error");
            span = _oShared.spanNode();
            td.append(span);
            tr.append(td);

        } else if (isMsbApproval) {
            td = _oShared.tdNode(null, "approve");
            var a = _oShared.aNode("Confirm...", "positioner");
            a.on('click', _seeEditDetails_MsbSegApproval, null, testOpp);
            td.append(a);
            tr.append(td);
        }



        //Mobile browser:
        _buildRowMobile(tr, testOpp);
        return tr;
    }
    var _btnApproveLabel = null;
    function _getBtnApproveLabel() {
        if (_btnApproveLabel == null) {
            _btnApproveLabel = _oMessages.getRaw('Button.Approve')
        }
        return _btnApproveLabel;
    }
    var _btnDenyLabel = null;
    function _getBtnDenyLabel() {
        if (_btnDenyLabel == null) {
            _btnDenyLabel = _oMessages.getRaw('Button.Deny')
        }
        return _btnDenyLabel;
    }

    var _btnDetailsLabel = null;
    function _getBtnDetailsLabel() {
        if (_btnDetailsLabel == null) {
            _btnDetailsLabel = _oMessages.getRaw('Label.Details')
        }
        return _btnDetailsLabel;
    }
    var _btnSeeDetailsLabel = null;
    function _getBtnSeeDetailsLabel() {
        if (_btnSeeDetailsLabel == null) {
            _btnSeeDetailsLabel = _oMessages.getRaw('Button.SeeEditDetails')
        }
        return _btnSeeDetailsLabel;
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

    //for mobile only
    function _buildRowMobile(tr, testOpp) {
        if (!_oShared.mobile()) return; //only for mobile

        var td = _oShared.tdNode(null, 'table_details');
        var a = _oShared.aNode('See all details', null);
        if (testOpp.IsSegmentApproval)
            a.on('click', _seeEditDetails_SegApproval, null, testOpp);
        else
            a.on('click', _seeEditDetails, null, testOpp);
        td.append(a);
        tr.append(td);
    }

    //load testee default test and segments accommodations if need
    //this js only responsible to load missing default dependencies
    function _loadDefaultAccs(testOpp) {
        Y.log("_loadDefaultAccs");
        var segments = null;
        if (Y.tdsTests.isSegmented(testOpp.testKey))
            segments = Y.tdsSegments.getSegments(testOpp.testKey);

        //Get all possible accom if not exists
        _oAccTypes.loadAccs(testOpp, segments, _populateDefaultAccs);
    }

    function _populateDefaultAccs(testOpp, segments, masterAccTypesList) {
        Y.log("_populateDefaultAccs");
        if (testOpp == undefined) {
            _oShared.showError(_oMessages.get('UnableToProcessRequest'));
            return;
        }
        if (masterAccTypesList == undefined) {
            _oShared.showError(_oMessages.get('InvalidAccTypes'));
            return;
        }

        //TEST level accommodations
        var masterAccTypes = masterAccTypesList[0]; //first elem always will be test level accs
        var oppAccTypes = testOpp.accTypesList[0]; //test level accs
        _populateOneDefaultAccs(testOpp, oppAccTypes, masterAccTypes);

        var oppAccTypesLen = testOpp.accTypesList.length;
        //SEGMENT level accs 
        if (segments == null || oppAccTypesLen <= 1) return; //no need to reapprove segment accommodations
        var len = segments.length;
        for (var i = 0; i < len; i++) {
            masterAccTypes = masterAccTypesList[i + 1];
            oppAccTypes = testOpp.accTypesList[i + 1];

            _populateOneDefaultAccs(testOpp, oppAccTypes, masterAccTypes);
        }
    }

    function _populateOneDefaultAccs(testOpp, oppAccTypes, masterAccTypes) {
        Y.log("_populateOneDefaultAccs");
        if (masterAccTypes == null || masterAccTypes == undefined) return;
        if (oppAccTypes == null || oppAccTypes == undefined) return;

        var len = masterAccTypes.length;
        for (var i = 0; i < len; i++) { //each acc type
            var masterAccType = masterAccTypes[i];
            var masterAccTypeV = masterAccType.Value;
            if (masterAccTypeV.dependOnType != null && masterAccTypeV.dependOnType.length > 0) //depend on another type
                continue;
            var type = masterAccTypeV.Type;
            var oppAccType = _oAccTypes.getAccType(type, oppAccTypes);

            if (oppAccType == null) { //no default for this testee, then populate
                oppAccType = _oAccTypes.copyAccTypeSelectedValues(type, masterAccTypeV);
                oppAccTypes[oppAccTypes.length] = oppAccType;
            }

            //tool dependencies 
            _populateDepDefaultAccs(testOpp, oppAccTypes, masterAccTypes, oppAccType, masterAccType);
        }
    }

    function _populateDepDefaultAccs(testOpp, oppAccTypes, masterAccTypes, selectedAccType, masterAccType) {
        var accDepParentTypes = masterAccType.Value.accDepParentTypes;

        if (accDepParentTypes == null || accDepParentTypes.length < 1) return;
        if (selectedAccType == null || selectedAccType == undefined) return;

        var selectedAccTypeV = selectedAccType.Value.Values;
        var accDepParentLen = accDepParentTypes.length;
        var parentAccTypeName = selectedAccType.Key;
        var selectedLen = selectedAccTypeV.length;
        for (var i = 0; i < selectedLen; i++) { //for each selected parent values
            var selectedCode = selectedAccTypeV[i].Code;
            for (var j = 0; j < accDepParentLen; j++) { //for each parent values
                var accDepParentType = accDepParentTypes[j];
                var ifType = accDepParentType.ifType;
                var ifValue = accDepParentType.ifValue;

                if (ifType != parentAccTypeName || ifValue != selectedCode) continue; //check to see if selected values even have dependencies

                var accDepChildTypes = accDepParentType.accDepChildTypes;
                if (accDepChildTypes == null) continue;

                var depTypeLen = accDepChildTypes.length;
                for (var k = 0; k < depTypeLen; k++) { //for each child acctype
                    var accDepChildType = accDepChildTypes[k];
                    var thenType = accDepChildType.thenType;
                    var thenValues = accDepChildType.thenValues;
                    if (thenType == null || thenType.length < 1 || thenValues == null || thenValues.length < 1) continue;

                    //set the thenValues
                    //first get the current selected values index
                    var index = _oAccTypes.getAccTypeIdx(thenType, oppAccTypes); // check if the testee already has this accType
                    //get the master type from the test, NOTE: this may not be accurate since the tool dependencies are not represent here
                    var newMasterAccType = _oAccTypes.getAccType(thenType, masterAccTypes);
                    if (newMasterAccType == null) {
                        Y.log('ERROR: TestID: "' + testOpp.testID + '" and Accommodation type dependencies: "' + thenType + '" does not exists in the Client_TestToolType.');
                        continue;
                    }
                    //create a new master set based ONLY on the thenValues
                    var newThenMasterAccType = _oAccTypes.copyAccType(newMasterAccType, thenValues); //copy all then values
                    var oppAccTypeNew = null;
                    if (index < 0) { //not current selected values not found, then use the default from the master list
                        oppAccTypeNew = _oAccTypes.copyAccTypeSelectedValues(thenType, newThenMasterAccType.Value); //get selected values
                        oppAccTypes[oppAccTypes.length] = oppAccTypeNew; //add
                    }
                    else { //if current pre-selected values exists, then use these as default values else use the default values from master list
                        //we have to do this since the data from db may not be accurate because of tool dependencies
                        //get ONLY selected values that is the subset of the master list
                        var newSelectedValues = _oAccTypes.getSelectedAccTypeValue(oppAccTypes[index].Value, newThenMasterAccType.Value);
                        if (newSelectedValues == null)//selected values not in the master list, then use the master
                        {
                            oppAccTypeNew = _oAccTypes.copyAccTypeSelectedValues(thenType, newThenMasterAccType.Value); //get selected values
                            oppAccTypes[index] = oppAccTypeNew; //update
                        } //otherwise, use the pre-selected as default.
                    }
                }
            }
        }
    }

    // get number of active test opps (_data)
    function _getNumActiveTestOpps() {
        var numActiveTests = 0, testOppsLen = _data.length;
        for (var i = 0; i < testOppsLen; i++) {
            if (_data[i].disabled == undefined || _data[i].disabled === 'false')
                numActiveTests++;
        }

        return numActiveTests;
    }



    //-------------------------------------------------------------------------
    // Public interface
    //-------------------------------------------------------------------------
    Y.tdsApprovalOpps = {
        init: function (bApprovalsScreen) {
            Y.log("Y.tdsApprovalOpps.init");
            _init(bApprovalsScreen);
        },
        data: function () {
            return _data;
        },
        setData: function (data) {
            _data = data;
            Y.log('Y.tdsApprovalOpps.setData');
            Y.log(_data);
        },
        hasApprovals: function () {
            return (_data == null || _data.length < 1) ? false : true;
        },
        renderView: function () {
            _renderView();
        },
        renderAccsDependencies: function (testOpp, oppAccTypes, masterAccTypes, detailElem, parentAccTypeName, accDepParentTypes) {
            return _renderAccsDependencies(testOpp, oppAccTypes, masterAccTypes, detailElem, parentAccTypeName, accDepParentTypes);
        }
    };
}, "0.1", { requires: ["node", "io", "json-parse", "tds-session", "tds-shared", "tds-accTypes"] });

