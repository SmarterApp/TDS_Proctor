//required: yui-min.js
//NOTE: this class is using on both proctor and reports app for student lookup
YUI.add("tds-lookup", function (Y) {
    //-------------------------------------------------------------------------
    // Private variables
    //-------------------------------------------------------------------------
    var _institutions = null,
    _districts = null,
    _schools = null,
    _grades = null,
    _staticTextField = 'Enter a',
    _testees = null,
    _lookup_window_class = "lookup_window",
    _firstNameLabel = 'Enter a First Name',
    _lastNameLabel = 'Enter a Last Name',
    _isQuickTab = true;
    var _advancedTabScrollView;
    var _quickTabScrollView;
    var _appConfig = null;

    var _elem = {
        btnClose: null,
        btnQuickSearch: null,
        divQuickResult: null,
        txtSSID: null,

        btnQuickTab: null,
        divQuickTab: null,

        btnAdvancedTab: null,
        divAdvancedTab: null,
        divAdvancedResultSingle: null,
        tblAdvancedResult: null,
        divCol2AdvancedResult: null,
        divCol3AdvancedResultSingle: null,
        firstNameTBox: null,
        lastNameTBox: null
    };

    var _oShared = Y.tdsShared;
    var _oMessages = Y.Messages;
    //-------------------------------------------------------------------------
    // Private functions
    //-------------------------------------------------------------------------
    function _init(appConfig) {
        Y.log("_init");
        _appConfig = appConfig;
        _elem.btnClose = Y.one("#btnClose");
        _elem.txtSSID = Y.one("#txtSSID");
        _elem.txtSSID.focus();
        if (_elem.btnClose != null)
            _elem.btnClose.on('click', _close);

        //quick search
        _elem.divQuickTab = Y.one("#divQuickTab");
        _elem.btnQuickTab = Y.one("#btnQuickTab");
        _elem.btnQuickTab.on('click', _quickTab);

        _elem.btnQuickSearch = Y.one("#btnQuickSearch");
        _elem.btnQuickSearch.on('click', _search);
        _elem.divQuickResult = Y.one("#divQuickResult");

        //advance search
        _elem.btnAdvancedTab = Y.one("#btnAdvancedTab");
        _elem.btnAdvancedTab.on('click', _advancedTab);

        _elem.divAdvancedTab = Y.one("#divAdvancedTab");
        _elem.divAdvancedResultSingle = Y.one("#divAdvancedResultSingle");
        _elem.tblAdvancedResult = Y.one("#tblAdvancedResult");
        _elem.divCol2AdvancedResult = Y.one("#divCol2AdvancedResult");
        _elem.divCol3AdvancedResultSingle = Y.one("#divCol3AdvancedResultSingle");

        //hide and show col3 for mobile
        var btn = Y.one("#divCol3Close");
        btn.on('click', function () {
            _elem.divCol3AdvancedResultSingle.setStyle('display', 'none');
            _oShared.removeBodyClass("show_detail");
        });

        _elem.btnAdvancedSearch = Y.one("#btnAdvancedSearch");
        _elem.btnAdvancedSearch.on('click', _advancedSearch);
        Y.one("#selBoxInstitution").on('change', _selectAnInstitution);
        //Y.one("#selBoxDistrict").on('change', _selectADistrict);
        Y.one("#selBoxSchool").on('change', _selectASchool);

        if (_oMessages == null)
            _oMessages = Y.Messages;
        _firstNameLabel = _oMessages.getRaw("Label.EnterAFirstName");
        _lastNameLabel = _oMessages.getRaw("Label.EnterALastName");

        _elem.firstNameTBox = Y.one("#firstName");
        _elem.lastNameTBox = Y.one("#lastName");
        _elem.firstNameTBox.on('focus', _clearTxt, null, _elem.firstNameTBox);
        _elem.lastNameTBox.on('focus', _clearTxt, null, _elem.lastNameTBox);

        //enter key
        var nodes = Y.all('input');
        nodes.on('keypress', _enterKey);

        //remove the wait
        _oShared.removeBodyClass('please_wait');

        _quickTabScrollView = _oShared.activateScollView(_elem.divQuickTab);
        _advancedTabScrollView = _oShared.activateScollView(_elem.divAdvancedTab);
        _oShared.removeBodyClass("loading");

        _oShared.addRegisterActivity();
    }
    //press enter key
    function _enterKey(e) {
        e = e || window.event;
        if (e.keyCode == 13) {
            Y.log("enter key press");
            if (_isQuickTab) //quick search
                _search();
            else
                _advancedSearch();
            return false;
        }
        return true;
    }
    //close the lookup iframe
    function _close(e) {
        Y.log("_close");

        //remove the css class and then start auto refresh
        //_quickTab();
        if (_appConfig == null || _appConfig.AppName == 'Proctor') { //proctor app 
            //fire a global event
            window.parent.fireGlobalEvent('GlobalEvent:closeDialog', _lookup_window_class);
        }
        else {
            _oShared.removeBodyClass(_lookup_window_class);
        }
        e.halt(true);
    }
    function _openTab(type) {
        Y.log("_openTab type: " + type);
        _isQuickTab = (type == 0); //save active tab
        if (_isQuickTab) {//quick tab           
            _clearQuickSearchInputs();
            _clearQuickSearchResult(); //clear result
            _elem.divQuickTab.setStyle('display', 'block');
            _elem.btnQuickTab.addClass('active');
            _elem.divAdvancedTab.setStyle('display', 'none');
            _elem.btnAdvancedTab.removeClass("active");
            _elem.txtSSID.focus();
            if (_advancedTabScrollView != null) {
                _advancedTabScrollView.hide();
                _quickTabScrollView.show();
            }
        }
        else { //advance tab
            _hideAdvancedResult();
            _clearAdvancedResultInputs();

            _elem.divQuickTab.setStyle('display', 'none');
            _elem.btnQuickTab.removeClass("active");
            _elem.divAdvancedTab.setStyle('display', 'block');
            _elem.btnAdvancedTab.addClass("active");

            if (_advancedTabScrollView != null) {
                _quickTabScrollView.hide();
                _advancedTabScrollView.show();
            }
        }

    }

    //****************************************advanced search ***************************************************
    //advanced search
    function _advancedSearch() {
        Y.log("_advancedSearch");
        _hideAdvancedResult(); //hide result columns
        //validate input, hit the server for result set and then render this.
        if (!_isValid()) {
            return;
        }
        //A function handler to use for successful requests:
        var handleSuccess = function (ioId, o) {
            _showPleaseWait(false);
            if (o.responseText != undefined) {
                try {
                    var testees = Y.JSON.parse(o.responseText);
                    if (testees.status != undefined && testees.status == 'failed') {//failed
                        _showMsg(testees.ReturnedStatus.reason);
                        return;
                    }
                    _testees = testees;
                    _renderAdvancedResult(testees);
                }
                catch (e) {
                    Y.log("ERROR: advancedSearch: " + e);
                    return false;
                }
            }
        };
        var handleFailure = function (ioId, o) {
            _showPleaseWait(false);
            _showMsg(_oMessages.get("UnableToSearchForATestee"));
            Y.log("ERROR: advancedSearch: " + x);
        };

        var postData = "districtKey=" + //_getSelectedInstitution() +
             "&schoolKey=" + _getSelectedInstSchool() +
             "&grade=" + _getSelectedGrade() +
             "&firstName=" + encodeURIComponent(_getFirstName()) +
             "&lastName=" + encodeURIComponent(_getLastName());
        _showPleaseWait(true);
        /* Configuration object for POST transaction */
        var cfg = {
            method: "POST",
            data: postData,
            timeout: gXHRTimeout, // Abort the transaction, if it is still pending, after X ms.
            headers: { "cache-control": "no-cache", 'X-Transaction': 'Get Alert Messages data' },
            on: {
                success: handleSuccess,
                failure: handleFailure
            }
        };

        Y.io("Services/XHR.axd/GetSchoolTestees", cfg);
    }

    //render the advance search results
    function _renderAdvancedResult(testees) {
        Y.log("_renderAdvancedResult");
        var divElem = _elem.divCol2AdvancedResult;
        divElem.setStyle('display', 'block');

        //clear content, render content from testees
        var tblElem = _elem.tblAdvancedResult;
        //remove all children tables
        var tbodies = tblElem.getElementsByTagName('tbody');
        var tbody = null;
        if (tbodies.size() < 1)
            tbody = tblElem.append('<tbody></tbody>');
        else
            var tbody = tbodies.item(0);
        tbody.setContent(''); //clear out exist table first      
        if (testees == null || testees.length < 1)
            return;
        var len = testees.length;
        for (var i = 0; i < len; i++) {
            var testee = testees[i];
            tbody.append(_buildRowView(testee));
        }
    }

    //advance search result view
    function _buildRowView(testee) {
        var tr = Y.Node.create("<tr></tr>");
        var td = Y.Node.create("<td></td>");
        var a = Y.Node.create("<a class='detail'>Details</a>");
        a.on('click', _viewDetails, null, _getTesteeID(testee));
        var i, tID, tLName, tFName, tGrade, countAttrToShow = 4, testeeAttribute;    // countAttrToShow is number of attribute cols so we can break the loop early.
        for (i = 0; i < testee.TesteeAttributes.length; i++) {
            testeeAttribute = testee.TesteeAttributes[i];
            if (countAttrToShow <= 0)
                break;
            switch (testeeAttribute.TDS_ID) {
                case 'ID':
                    tID = testeeAttribute.value;
                    countAttrToShow--;
                    break;
                case 'FirstName':
                    tFName = testeeAttribute.value;
                    countAttrToShow--;
                    break;
                case 'LastName':
                    tLName = testeeAttribute.value;
                    countAttrToShow--;
                    break;
                case 'Grade':
                    tGrade = testeeAttribute.value;
                    countAttrToShow--;
                    break;
            }
        }
        td.append(a);
        tr.append(td);
        tr.append("<td>" + tID + "</td>");
        tr.append("<td>" + tLName + "</td>");
        tr.append("<td>" + tFName + "</td>");
        tr.append("<td>" + tGrade + "</td>");
        return tr;
    }
    //view details step 3
    function _viewDetails(e, testeeID) {
        Y.log("_viewDetails");
        var testee = _findTestee(testeeID);
        Y.log("testee.Key=" + _getTesteeRTSKey(testee));
        if (testee.cache != undefined && testee.cache == true) {
            _renderDetails(testee); //reuse the data
            return;
        }
        _quickSearch(testee, _renderDetails);
    }

    function _renderDetails(testee) {
        Y.log("_renderDetails");
        var divElem = _elem.divCol3AdvancedResultSingle;
        divElem.setStyle('display', 'block');
        //divElem.show();
        _oShared.addBodyClass("show_detail");

        var elem = _elem.divAdvancedResultSingle;

        if (testee != null && testee.status != null) { //failed
            elem.setContent(testee.reason);
            return;
        }
        if (testee == null || _getTesteeRTSKey(testee) == null) {
            elem.setContent("Invalid testee");
            return;
        }
        elem.setContent(_testeeToString(testee));
    }

    //entry point for advance search
    function _advancedTab() {
        Y.log("_advancedTab");

        if (_institutions == null || _institutions.length < 1)
            _getInstitutions();
        else
            _openTab(1);
    }

    //validation
    function _validateSearchInputs() {
        var instOpt = _getSelectedOption('#selBoxInstitution');
        var validObj = { institutionKey: true, schoolKey: true, grade: true, firstORlastName: true };
        var value = null;
        if (instOpt != null) {
            value = instOpt.getAttribute('value');
        }
        if (value == null || value.length < 1) {
            validObj.institutionKey = false;
        }
        else {
            var schoolKey;
            if (_isSchool(instOpt)) { //is a school type
                schoolKey = value;
                if (schoolKey == null || schoolKey.length < 1) {
                    validObj.instSchoolKey = false;
                }
            }
            else {
                schoolKey = _getSelectedSchool(); //get from school drop down
            }
            if (schoolKey == null || schoolKey.length < 1) {
                validObj.schoolKey = false;
            }
        }

        var grade = _getSelectedGrade();
        if (grade == null || grade.length < 1) {
            validObj.grade = false;
        }

        if (grade != null && grade != "all") {
            return validObj;
        }

        //first/last name required (chars and numbers only)
        var staticText = _staticTextField;
        var firstName = _getFirstName();
        var lastName = _getLastName();
        if ((firstName == null || firstName.length < 1)
            && (lastName == null || lastName.length < 1)) { //both empty
            validObj.firstORlastName = false;
        }
        //allow all chars on Jeremy's request: 10/07/2011
        if (firstName != null && firstName.length > 0 && !_oShared.isValidName(firstName)) { //not empty and invalid char
            validObj.invalidFirstORlastName = false;
        }
        if (lastName != null && lastName.length > 0 && !_oShared.isValidName(lastName)) { //not empty and invalid char
            validObj.invalidFirstORlastName = false;
        }

        return validObj;
    }
    //hack to add and remove for IOS 6
    function _ios6HackLookupWindowClass() {
        if (window.parent == undefined) return;
        //fire a global event
        window.parent.fireGlobalEvent('GlobalEvent:addRemoveClass', { className: "lookup_window", bAdd: false });
        //fire a global event
        window.parent.fireGlobalEvent('GlobalEvent:addRemoveClass', { className: "lookup_window", bAdd: true });

    }
    //{ districkKey: true, schoolKey: true, grade: true, firstName: true, lastName: true}
    function _isValid() {
        var validObj = _validateSearchInputs();
        var strMsg = "";
        var elem;

        if (validObj.institutionKey == false) {
            strMsg += _oMessages.get("Label.SelectDistrict") + '<br/>';
            _showErrorInstitution(true);
        }
        else
            _showErrorInstitution(false);

        if (validObj.schoolKey == false) {
            strMsg += _oMessages.get("Label.SelectSchool") + '<br/>';
            _showErrorSchool(true);
        }
        else
            _showErrorSchool(false);

        if (validObj.grade == false) {
            strMsg += _oMessages.get("Select a grade") + '<br/>';
            _showErrorGrade(true);
        }
        else
            _showErrorGrade(false);

        if (validObj.firstORlastName == false) {
            strMsg += _oMessages.get("Enter a First and/or Last Name") + '<br/>';
            _showErrorFirstLastName(true);
        }
        else if (validObj.invalidFirstORlastName == false) {
            strMsg += _oMessages.get("Invalid First and/or Last Name") + '<br/>';
            _showErrorFirstLastName(true);
        }
        else {
            _showErrorFirstLastName(false);
        }
        _showValidationMsg(strMsg);

        _ios6HackLookupWindowClass();

        return (strMsg.length > 0) ? false : true;
    }
    //**************************************** end advanced search ***************************************************    


    function _testeeToString(testee) {
        if (testee == null)
            return "";
        //var str = testee.LastName + ', ' + testee.FirstName +
        //    '<br/>Birthday: ' + testee.StrBirthday +
        //    '<br/>Grade: ' + testee.Grade +
        //    '<span class="challenge"><br/>ChallengeUp: ' + ((testee.ChallengeUps != null) ? testee.ChallengeUps : '') + '</span>' +
        //    '<br/>School: ' + ((testee.SchoolName != null) ? testee.SchoolName : '') + ((testee.SchoolID != null) ? '(' + testee.SchoolID + ')' : '') +
        //    '<br/>District: ' + ((testee.DistrictName != null) ? testee.DistrictName : '') + ((testee.DistrictID != null) ? '(' + testee.DistrictID + ')' : '');
        
        // sort the attributes by sortOrder, if 0 move to the end
        testee.TesteeAttributes = testee.TesteeAttributes.sort(function (attrA, attrB) {
            if (attrA.sortOrder === 0) {
                return 1;
            } else if (attrB.sortOrder === 0) {
                return -1;
            } else {
                return attrA.sortOrder - attrB.sortOrder;
            }
        });
        // display attributes
        var strAttr = '', testeeAttribute; //strName, 
        for (var i = 0; i < testee.TesteeAttributes.length; i++) {
            testeeAttribute = testee.TesteeAttributes[i];
            // only display if showOnProctor field is true
            if (testeeAttribute.showOnProctor === false)
                continue;

            // FirstName and LastName goes up top ---Name should be first in sort order
            //if (testeeAttribute.label == 'Name') {
            //    strName = testeeAttribute.value;
            //}
            if ((testeeAttribute.label === 'School') || (testeeAttribute.label === 'District')) {    // school/district is displayed differently
                strAttr += '<br/>' + testeeAttribute.label + ': ' + ((testeeAttribute.value != null) ? testeeAttribute.value : '')
                    + ((testeeAttribute.entityID != null) ? ' (' + testeeAttribute.entityID + ')' : '');
            } else if (testeeAttribute.TDS_ID == 'DOB') {
                strAttr += '<br/>' + testeeAttribute.label + ': ' + ((testeeAttribute.value != null) ? testeeAttribute.value.substr(0,2) + '/' + testeeAttribute.value.substr(2,2) + '/' + testeeAttribute.value.substr(4,4) : '');
            }
            //else if (testeeAttribute.label === 'ChallengeUp') {  // not sure if ChallengeUp should be here?
            //    strAttr += '<span class="challenge"><br/>' + testeeAttribute.label + ': ' + ((testeeAttribute.value != null) ? testeeAttribute.value : '')
            //        + '</span>';
            //}
            //else if (testeeAttribute.TDS_ID !== '--RTS KEY--' && testeeAttribute.TDS_ID !== 'FirstName' && testeeAttribute.TDS_ID !== 'LastName') { // do not display RTS KEY
            else {  // showOnProctor flag to show/hide attributes
                strAttr += '<br/>' + testeeAttribute.label + ': ' + ((testeeAttribute.value != null) ? testeeAttribute.value : '');
            }
        }

        return strAttr;
    }


    //**************************************** quick search ***************************************************    
    //quick search 
    function _quickTab() {
        Y.log("_quickTab");
        _openTab(0); //quick search tab        
    }

    //testee or testeeID and funcHandleSuccess
    function _quickSearch(testee, funcHandleSuccess) {
        Y.log("_quickSearch testeeID: " + testee);
        var testeeID;
        if (testee != null && _getTesteeID(testee) != null) //when pass in testee obj
            testeeID = _getTesteeID(testee);
        else
            testeeID = testee; //testeeID

        if (!_isValidSSID(testeeID)) { //if not then show the message and return           
            var returnedStatus = _oShared.ReturnStatus("failed", _oMessages.get("Invalid input SSID"));
            funcHandleSuccess(returnedStatus);
            return;
        }

        var handleSuccess = function (ioId, o) {
            _showPleaseWait(false);
            try {
                if (o.responseText != undefined) {
                    var testeeObj = Y.JSON.parse(o.responseText);
                    //if failed display the message and stopPleaseWait
                    if (testeeObj.ReturnedStatus != undefined && testeeObj.ReturnedStatus.status == "failed") {
                        funcHandleSuccess(testeeObj.ReturnedStatus);
                        return;
                    }
                    testeeObj.cache = true;
                    _setTestee(testeeObj); //save the data if success if click again
                    funcHandleSuccess(testeeObj);
                }
            }
            catch (x) {
                var returnedStatus = _oShared.ReturnStatus("failed", _oMessages.get("UnableToSearchForATestee"));
                funcHandleSuccess(returnedStatus);
                Y.log("ERROR: _quickSearch: " + x);
                return;
            }
        };
        var handleFailure = function (ioId, o) {
            _showPleaseWait(false);
            var returnedStatus = new ReturnStatus("failed", _oMessages.get("UnableToSearchForATestee"));
            funcHandleSuccess(returnedStatus);
            Y.log("ERROR: _search: handleFailure");
        };

        var postData = "testeeID=" + testeeID;
        _showPleaseWait(true);
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

        Y.io("Services/XHR.axd/GetTestee", cfg);
    }

    //validation -- hit the server -- render result
    function _search(e) {
        Y.log("_search");
        _clearQuickSearchResult();
        var testeeID = _elem.txtSSID.get('value');
        _quickSearch(testeeID, _renderResult);
    }

    function _isValidSSID(testeeID) {
        if (testeeID == null || testeeID.length < 1)
            return false;

        return true;
    }
    //simple search result  
    function _renderResult(testee) {
        Y.log("_renderResult");
        var elem = _elem.divQuickResult;
        var parent = elem.get('parentNode');
        parent.setStyle('display', 'block');

        if (testee != null && testee.status != null) { //failed
            elem.setContent(_oMessages.get(testee.reason));
            return;
        }
        if (testee == null || _getTesteeRTSKey(testee) == null) {
            elem.setContent("Invalid testee");
            return;
        }
        elem.setContent(_testeeToString(testee));
    }
    //**************************************** end quick search ***************************************************    

    function _showMsg(strMsg) {
        var elem = _elem.divQuickResult;
        var parentNode = elem.get('parentNode');
        parentNode.setStyle('display', 'block');
        elem.setContent(strMsg);
    }
    function _showPleaseWait(bShow) {
        var bodyNode = Y.one(document.body);
        if (bShow)
            bodyNode.addClass('please_wait');
        else
            bodyNode.removeClass('please_wait');
    }

    //return testees[i] testee index
    function _findTesteeIndex(testeeID) {
        if (_testees == null || _testees.length < 1 || testeeID == null || testeeID.length < 1)
            return -1;
        for (var i = 0; i < _testees.length; i++) {
            var testee = _testees[i];
            if (_setTesteeID(testee, testeeID))
                return i;
        }
        return -1;
    }

    function _setTestee(testee) {
        var idx = _findTesteeIndex(_getTesteeID(testee));
        if (idx < 0)
            return testee;
        else {
            _testees[idx] = testee;
            return _testees[idx];
        }
    }
    function _findTestee(testeeID) {
        var idx = _findTesteeIndex(testeeID);
        if (idx < 0)
            return null;
        else {
            return _testees[idx];
        }
    }

    function _getTesteeRTSKey(testee) {
        if (testee === null || testee.TesteeAttributes.length <= 0)
            return null;
        var i, rtsKey;
        for (i = 0; i < testee.TesteeAttributes.length; i++) {
            if (testee.TesteeAttributes[i].TDS_ID === '--RTS KEY--') {
                rtsKey = testee.TesteeAttributes[i].value;
                break;
            }
        }
        return rtsKey;
    }
    function _getTesteeID(testee) {
        if (testee == null || testee.TesteeAttributes == null || testee.TesteeAttributes.length <= 0)
            return null;
        var i, rtsKey;
        for (i = 0; i < testee.TesteeAttributes.length; i++) {
            if (testee.TesteeAttributes[i].TDS_ID === 'ID') {
                rtsKey = testee.TesteeAttributes[i].value;
                break;
            }
        }
        return rtsKey;
    }
    // return true/false
    function _setTesteeID(testee, newTesteeID) {
        if (testee === null || testee.TesteeAttributes.length <= 0)
            return false;
        var i, rtsKey;
        for (i = 0; i < testee.TesteeAttributes.length; i++) {
            if (testee.TesteeAttributes[i].TDS_ID === 'ID') {
                testee.TesteeAttributes[i].value = newTesteeID;
                return true;
            }
        }
        return false;
    }

    //***************************************Clear functions ***************************************
    function _clearQuickSearchResult() {
        Y.log("_clearQuickSearchResult");
        var elem = _elem.divQuickResult;
        if (elem != null)
            elem.setContent('');
    }
    function _clearQuickSearchInputs() {
        Y.log("_clearQuickSearchInputs");
        var elem = _elem.txtSSID;
        if (elem != null) {
            elem.set('value', '');
        }
    }
    function _hideAdvancedResult() {
        Y.log("_hideAdvancedResult");
        _oShared.removeBodyClass("show_detail");

        var divElem = _elem.divCol2AdvancedResult;
        divElem.setStyle('display', 'none');

        divElem = _elem.divCol3AdvancedResultSingle;
        divElem.setStyle('display', 'none');
    }
    function _clearAdvancedResultInputs() {
        Y.log("_clearAdvancedResultInputs");
        //clear out error messages and styles
        _showValidationMsg(null);
        _showErrorInstitution(false);
        _showErrorSchool(false);
        _showErrorGrade(false);
        _showErrorFirstLastName(false);

        //render institution
        _renderInstitutions(_institutions);

        //reset first/last name fields
        _resetFirstNameLabel();
        _resetLastNameLabel();
    }
    //display the label again
    function _resetFirstNameLabel() {
        if (_elem.firstNameTBox != null) {
            _elem.firstNameTBox.set('value', _firstNameLabel);
        }
    }

    function _resetLastNameLabel() {
        if (_elem.lastNameTBox != null)
            _elem.lastNameTBox.set('value', _lastNameLabel);
    }
    function _showValidationMsg(msg) {
        var elem = Y.one("#validationMessage");
        if (elem == null) return;
        if (msg == null || msg.length < 1) {
            elem.setContent('');
            elem.setStyle('display', 'none');
        }
        else {
            elem.setContent(msg);
            elem.setStyle('display', 'block');
        }
    }
    function _showErrorInstitution(bShow) {
        var elem = Y.one("#valid_institutionKey");
        if (elem == null)
            return;
        if (!bShow)
            elem.setStyle('display', 'none');
        else
            elem.setStyle('display', 'block');
    }
    function _showErrorSchool(bShow) {
        var elem = Y.one("#valid_schoolKey");
        if (elem == null)
            return;
        if (!bShow)
            elem.setStyle('display', 'none');
        else
            elem.setStyle('display', 'block');
    }
    function _showErrorGrade(bShow) {
        var elem = Y.one("#valid_grade");
        if (elem == null)
            return;
        if (!bShow)
            elem.setStyle('display', 'none');
        else
            elem.setStyle('display', 'block');
    }
    function _showErrorFirstLastName(bShow) {
        var elemFName = Y.one("#valid_firstName");
        var elemLName = Y.one("#valid_lastName");

        if (elemFName != null) {
            if (!bShow)
                elemFName.setStyle('display', 'none');
            else
                elemFName.setStyle('display', 'block');
        }
        if (elemLName != null) {
            if (!bShow)
                elemLName.setStyle('display', 'none');
            else
                elemLName.setStyle('display', 'block');
        }
    }
    function _clearTxt(e, objTextBox) {
        Y.log("_clearTxt");
        if (objTextBox == null) {
            _setFocus();
            return;
        }
        var value = objTextBox.get('value');
        if (value.length < 1) {
            _setFocus();
            return;
        }
        var staticText = _staticTextField;
        if (value.indexOf(staticText) == 0)
            objTextBox.set('value', '');

        _setFocus();
    }
    function _setFocus() { //this is NOT working on FF 3.5 or lower
        //if(_elem.btnAdvancedSearch!=null)
        //    _elem.btnAdvancedSearch.focus();
    }

    //get select school key
    function _getSelectedInstSchool() {
        Y.log("_getSelectedInstSchool");
        var instOpt = _getSelectedOption("#selBoxInstitution");
        //Y.log(instOpt);
        if (instOpt == null) return "";
        var schoolKey;
        if (_isSchool(instOpt)) { //not a school type
            schoolKey = instOpt.getAttribute('value')
        }
        else {
            schoolKey = _getSelectedSchool(); //get from school drop down
        }
        return schoolKey;
    }

    //selected institution is school type
    function _isSchool(elem) {
        return elem.hasClass('SchoolType');
    }
    function _getSelectedInstitution() {
        return _getSelectedValue("#selBoxInstitution");
    }
    function _getSelectedDistrict() {
        return _getSelectedValue("#selBoxDistrict");
    }

    function _getSelectedSchool() {
        return _getSelectedValue("#selBoxSchool");
    }
    //elemCSSName = "#selBoxSchool"
    //get selected value
    function _getSelectedValue(elemCSSName) {
        var elem = _getSelectedOption(elemCSSName);
        if (elem == null)
            return "";
        return elem.getAttribute('value');
    }
    //get selected option in a drop down list
    //return an opt elem
    function _getSelectedOption(elemName) {
        var elem = Y.one(elemName);
        if (elem == null)
            return null;
        var idx = elem.get('selectedIndex');
        return elem.get('options').item(idx);
    }

    function _getSelectedGrade() {
        return _getSelectedValue("#selBoxGrade");
    }

    function _getFirstName() {
        var elem = Y.one("#firstName");
        if (elem == null)
            return "";
        var staticText = _staticTextField;
        var value = elem.get('value');
        return (value.indexOf(staticText) == 0) ? "" : value;
    }
    function _getLastName() {
        var elem = Y.one("#lastName");
        if (elem == null)
            return "";
        var staticText = _staticTextField;
        var value = elem.get('value');
        return (value.indexOf(staticText) == 0) ? "" : value;
    }
    //hideShowSchool drop down list
    function _hideShowSchool(bHide) {
        var elem = Y.one("#selBoxSchool");
        if (elem == null)
            return;

        if (bHide) {
            elem.setStyle('display', 'none');
        } else {
            elem.setStyle('display', 'block');
        }
    }

    function _selectADistrict() {
        //get district key, get all schools for this district, render schools
        var districtKey = _getSelectedDistrict();

        if (districtKey == null || districtKey.length < 1) {
            _renderSchools(null); //render nothing
            return;
        }
        _getSchools(districtKey);
    }
    function _selectASchool() {
        //get school key, get all grades for this school, render grades
        var schoolKey = _getSelectedSchool();
        if (schoolKey == null || schoolKey.length < 1)
            return;
        _getGrades(schoolKey);
    }

    //render an institutions list
    function _renderInstitutions(institutions) {
        Y.log("_renderInstitutions: " + (new Date()).getTime());
        var elem = Y.one("#selBoxInstitution");
        var options = elem.get('options')
        var firstOpt = options.item(0);
        var firstOptText = firstOpt.getAttribute('text');
        firstOpt.set('selected', true);
        options.remove();
        elem.append(firstOpt);
        //Y.log(institutions);
        if (institutions == null) {
            _renderSchools(null); //render nothing
            //_oShared.activateScollView(Y.one("#divAdvancedTab"));
            return;
        }
        //hide the school drop down
        _hideShowSchool(true);

        var len = institutions.length;
        var bHasSelected = false;
        var selected = " selected='selected' ";
        var className = " class='SchoolType' ";
        for (var i = 0; i < len; i++) {
            var institution = institutions[i];
            if (institution.isSchool) className = " class='SchoolType' ";
            elem.append("<option value='" + institution.Key + "'" + (institution.isSchool ? className : "") + (institution.Selected ? selected : "") + ">" + institution.Name + " (" + institution.ID + ")</option>");
        }
        _selectAnInstitution();
        //_oShared.activateScollView(Y.one("#divAdvancedTab"));
        return;
    }

    //select an institution
    function _selectAnInstitution() {
        //get the selected option
        //if the selected option is a school then no need to go to the server
        Y.log("_selectAnInstitution");
        var selectedElem = _getSelectedOption('#selBoxInstitution');

        if (selectedElem == null) {
            _renderSchools(null); //render nothing
            return;
        }
        var value = selectedElem.getAttribute('value');
        if (_isSchool(selectedElem)) {
            //hide the school drop down           
            _hideShowSchool(true);
            var schoolKey = value;
            _getGrades(schoolKey); //render the grades            
            return;
        }
        //it is a district
        var districtKey = value;
        if (districtKey == null || districtKey.length < 1) {
            _renderSchools(null); //render nothing
            return;
        }
        //show the school drop down
        _hideShowSchool(false);
        //show the school drop down
        _getSchools(districtKey);
    }

    //old???
    function _renderDistricts(districts) {
        Y.log("_renderDistricts");
        var elem = Y.one("#selBoxDistrict");
        if (elem == null)
            return;
        var options = elem.get('options');
        var firstOpt = options.item(0);
        var firstOptText = firstOpt.getAttribute('text');
        options.remove();
        option.append(firstOpt);
        if (districts == null) {
            _renderSchools(null); //render nothing
            return;
        }
        var len = districts.length;
        for (var i = 0; i < len; i++) {
            var district = districts[i];
            options.append("<option value='" + district.Key + "'>" + district.Name + " (" + district.ID + ")</option>");
        }
        _selectADistrict();
    }
    function _renderSchools(schools) {
        Y.log("_renderSchools");
        var elem = Y.one("#selBoxSchool");
        var options = elem.get('options');
        var firstOpt = options.item(0);
        var firstOptText = firstOpt.getAttribute('text');
        options.remove();
        elem.append(firstOpt);

        _ios6HackLookupWindowClass(); // hack


        if (schools == null) {
            _renderGrades(null); //render nothing
            return;
        }

        for (var i = 0; i < schools.length; i++) {
            var school = schools[i];
            elem.append("<option value='" + school.Key + "'>" + school.Name + " (" + school.ID + ")</option>");
        }
        _selectASchool();
    }
    function _renderGrades(grades) {
        Y.log("_renderGrades");
        var elem = Y.one("#selBoxGrade");
        var options = elem.get('options');
        options.remove();
        elem.append("<option value='all' selected='true'>All Grades</option>");

        if (grades == null)
            return;
        for (var i = 0; i < grades.length; i++) {
            var grade = grades[i];
            elem.append("<option value='" + grade.Value + "'>" + grade.Text + "</option>");
        }
    }

    ///get a list of institutions
    function _getInstitutions(e) {
        Y.log("_getInstitutions");
        //NOTE: no need to bring down the districts list again
        if (_institutions != null && _institutions.length > 0) {
            //Y.log('_institutions already exists');
            _renderInstitutions(_institutions);
            _openTab(1);
            return;
        }

        var handleSuccess = function (ioId, o) {
            _showPleaseWait(false);
            try {
                if (o.responseText != undefined) {
                    var institutions = Y.JSON.parse(o.responseText);
                    //if failed display the message and stopPleaseWait
                    if (institutions.ReturnedStatus != undefined && institutions.ReturnedStatus.status == "failed") {
                        _showMsg(institutions.ReturnedStatus.reason);
                        return;
                    }
                    _institutions = institutions;
                    //Y.log('call _renderInstitutions');
                    //Y.log(_institutions);
                    _renderInstitutions(_institutions);
                    _openTab(1);
                }
            }
            catch (x) {
                _showMsg(_oMessages.get('UnableToProcessRequest'));
                Y.log("ERROR: _getInstitution: " + x);
                return;
            }
        };
        var handleFailure = function (ioId, o) {
            _showPleaseWait(false);

            _showMsg(_oMessages.get('UnableToProcessRequest'));
            Y.log("ERROR: _getInstitution: handleFailure");

        };
        _showPleaseWait(true);
        /* Configuration object for POST transaction */
        var cfg = {
            method: "POST",
            data: "i=i", //for FF3 and below and IIS7 issue with 0 content length
            timeout: gXHRTimeout, // Abort the transaction, if it is still pending, after X ms.
            headers: { "cache-control": "no-cache", 'X-Transaction': 'Get data' },
            on: {
                success: handleSuccess,
                failure: handleFailure
            }
        };

        Y.io("Services/XHR.axd/GetInstitutions", cfg);
    }

    function _getDistricts(e) {
        Y.log("_getDistricts");
        //NOTE: no need to bring down the districts list again
        if (_districts != null && _districts.length > 0) {
            _renderDistricts(_districts);
            return;
        }
        var handleSuccess = function (ioId, o) {
            _showPleaseWait(false);
            try {
                if (o.responseText != undefined) {
                    var districts = Y.JSON.parse(o.responseText);
                    //if failed display the message and stopPleaseWait
                    if (districts.ReturnedStatus != undefined && districts.ReturnedStatus.status == "failed") {
                        _showMsg(districts.ReturnedStatus.reason);
                        return;
                    }
                    _districts = districts;
                    _renderDistricts(districts);
                }
            }
            catch (x) {
                _showMsg(Messages.get('UnableToProcessRequest'));
                Y.log("ERROR: _getDistricts: " + x);
                return;
            }
        };
        var handleFailure = function (ioId, o) {
            _showPleaseWait(false);
            _showMsg(Messages.get('UnableToProcessRequest'));
            Y.log("ERROR: _getDistricts: handleFailure");

        };
        _showPleaseWait(true);

        /* Configuration object for POST transaction */
        var cfg = {
            method: "POST",
            data: "i=i",
            timeout: gXHRTimeout, // Abort the transaction, if it is still pending, after X ms.
            headers: { "cache-control": "no-cache", 'X-Transaction': 'Get data' },
            on: {
                success: handleSuccess,
                failure: handleFailure
            }
        };

        Y.io("Services/XHR.axd/GetDistricts", cfg);
    }
    function _getSchools(districtKey) {
        Y.log("_getSchools");
        var handleSuccess = function (ioId, o) {
            _showPleaseWait(false);
            try {
                if (o.responseText != undefined) {
                    var schools = Y.JSON.parse(o.responseText);
                    //if failed display the message and stopPleaseWait
                    if (schools.ReturnedStatus != undefined && schools.ReturnedStatus.status == "failed") {
                        _showMsg(schools.ReturnedStatus.reason);
                        return;
                    }
                    _schools = schools;
                    _renderSchools(schools);
                }
            }
            catch (x) {
                _showMsg(Messages.get('UnableToProcessRequest'));
                Y.log("ERROR: _getSchools: " + x);
                return;
            }
        };
        var handleFailure = function (ioId, o) {
            _showPleaseWait(false);

            _showMsg(Messages.get('UnableToProcessRequest'));
            Y.log("ERROR: _getSchools: handleFailure");
        };

        if (districtKey == null || districtKey.length < 1) { //if not then show the message and return
            _showMsg(Messages.get('InvalidInputDistrictKey'));
            return;
        }
        var postData = "districtKey=" + districtKey;
        _showPleaseWait(true);
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

        Y.io("Services/XHR.axd/GetSchools", cfg);
    }
    function _getGrades(schoolKey) {
        Y.log("_getGrades for schoolkey: " + schoolKey);
        var handleSuccess = function (ioId, o) {
            _showPleaseWait(false);
            try {
                if (o.responseText != undefined) {
                    var grades = Y.JSON.parse(o.responseText);
                    //if failed display the message and stopPleaseWait
                    if (grades.ReturnedStatus != undefined && grades.ReturnedStatus.status == "failed") {
                        _showMsg(grades.ReturnedStatus.reason);
                        return;
                    }
                    _grades = grades;
                    //Y.log('call _renderGrades');
                    _renderGrades(_grades);
                }
            }
            catch (x) {
                _showMsg(_oMessages.get('UnableToProcessRequest'));
                Y.log("ERROR: _getGrades: " + x);
                return;
            }
        };
        var handleFailure = function (ioId, o) {
            _showPleaseWait(false);

            _showMsg(_oMessages.get('UnableToProcessRequest'));
            Y.log("ERROR: _getGrades: handleFailure");
        };

        if (schoolKey == null || schoolKey.length < 1) { //if not then show the message and return
            _showMsg(_oMessages.get('InvalidInputSchoolKey'));
            return;
        }

        var postData = "schoolKey=" + schoolKey;
        _showPleaseWait(true);
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

        Y.io("Services/XHR.axd/GetGrades", cfg);

    }
    function _registerEvents() {
        if (top.fireGlobalEvent != undefined) {
            //add this if you want to keep track of user inactivity from the parent window.
            if (top.fireGlobalEvent != undefined) {
                var body = Y.one("#doc-body");
                body.on('keydown', function (e) { top.fireGlobalEvent('GlobalEvent:registerActivity'); });
                body.on('mousedown', function (e) { top.fireGlobalEvent('GlobalEvent:registerActivity'); });
            }
        }
    }

    //-------------------------------------------------------------------------
    // Public interface
    //-------------------------------------------------------------------------
    Y.tdsLookup = {
        init: function (appConfig) {
            _init(appConfig);
        }
    };

}, "0.1", { requires: ["tds-shared", "event", "event-custom", "event-key", "node", "io", "json-parse"] });

