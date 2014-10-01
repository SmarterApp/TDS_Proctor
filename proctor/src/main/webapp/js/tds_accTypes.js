//required: yui-min.js
YUI.add("tds-accTypes", function (Y) {
    //-------------------------------------------------------------------------
    // Private variables
    //-------------------------------------------------------------------------
    var _data; //hash of acctypes one key per test.
    var _depData; //dependencies data
    var _oShared = Y.tdsShared;
    var _oMessages = Y.Messages;

    //-------------------------------------------------------------------------
    // Private functions
    //-------------------------------------------------------------------------   
    //NOTE: we should also get the acc dependencies if neccessary
    function _loadAccs(testOpp, segments, renderFunc) {
        Y.log("AccTypes._loadAccs");
        if (testOpp == undefined || testOpp == null) {
            _oShared.showError(Y.Messages.get('UnableToProcessRequest'));
            return;
        }

        var handleSuccess = function (ioId, o) {
            try {
                var accs = Y.JSON.parse(o.responseText);
                if (accs.status != undefined && accs.status == 'failed') {//failed
                    _oShared.showError(accs.reason);
                }
                else { //remove the row from the table if this is the last row remove the table
                    if (_data == undefined)
                        _data = new Array();
                    var len = accs.length;
                    for (var i = 0; i < len; i++) {
                        //do the translation for the master set of the accs
                        _doTranslation(accs[i].Value);
                        _data[accs[i].Key] = accs[i].Value;
                    }
                    //Y.log(accs);

                    debugData['Y.tdsAccTypes'] = _data;

                    if (renderFunc != undefined)
                        renderFunc(testOpp, segments, _getAccs(segments, testOpp.testKey));
                    return;
                }
            }
            catch (x) {
                _oShared.showError(Y.Messages.get('UnableToProcessRequest'));
                Y.log("ERROR: AccTypes._loadAccs: " + x);
                return;
            }
        };
        var handleFailure = function (ioId, o) {
            _oShared.showError(Y.Messages.get('UnableToProcessRequest'));
            Y.log("ERROR: _loadAccs: handleFailure");
        };

        if (_data != undefined) { //check to see if data already exists, no need to go back to the server
            var masterAccTypesList = _getAccs(segments, testOpp.testKey);
            if (masterAccTypesList != null && masterAccTypesList.length > 0) {
                if (renderFunc != undefined) {
                    renderFunc(testOpp, segments, masterAccTypesList);
                }
                return;
            }
        }
        var postData = "testKey=" + testOpp.testKey;
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
        Y.io("Services/XHR.axd/GetAccs", cfg); //get test and associated segments accs
    }

    //do translation for all accType label and acc Values label
    function _doTranslation(accTypes) {
        if (accTypes == undefined) return;
        var len = accTypes.length;
        for (var i = 0; i < len; i++) {
            var accType = accTypes[i];
            var translationMsg = _oMessages.getRaw(accType.Key);
            if (translationMsg != accType.Key)
                accType.Value.Label = translationMsg;
            var values = accType.Value.Values;
            var lenVal = values.length;
            for (var j = 0; j < lenVal; j++) {
                var accValue = values[j];
                translationMsg = _oMessages.getRaw(accValue.Code);
                if (translationMsg != accValue.Code)
                    accValue.Value = translationMsg;
            }
        }
    }

    //get test accs and all its segments accs
    function _getAccs(segments, testKey) {
        var aryAccTypes = new Array();
        var accTypes = _data[testKey]; //data already here ? ...
        if (accTypes == null || accTypes == undefined)
            return null;
        aryAccTypes.push(accTypes);
        if (segments != null) {
            var len = segments.length;
            for (var i = 0; i < len; i++) {
                aryAccTypes.push(_data[segments[i].testKey + '_' + segments[i].position]);
            }
        }
        return aryAccTypes;
    }

    function _init() {
        Y.log("AccTypes.init");
        if (_data != undefined) //no need to init
            return;
        //load acc types here ...
    }

    //static Method *************************************************************************************    
    function _toString(accTypes) {
        var str = '';
        if (accTypes == undefined)
            return str;
        var len = accTypes.length;
        for (var i = 0; i < len; i++) {
            if (str.length < 1) str = _formatAccValues(accTypes[i].Value);
            else str += ' | ' + _formatAccValues(accTypes[i].Value);
        }
        return str;
    }

    //build a accValue
    function _buildAccValue(allowCombine, code, isSelected, value) {
        var accValue = new Object();
        accValue.AllowCombine = allowCombine;
        accValue.Code = code;
        accValue.IsSelected = isSelected;
        accValue.Value = value;
        return accValue;
    }

    function _buildCheckBoxGroup(testOpp, oppAccTypes, masterAccType, masterAccTypes) {
        //ignore not visible acc Type IsVisible= false
        //check the master accs list
        if (masterAccType == undefined || !masterAccType.IsVisible || testOpp == undefined)
            return null;

        //get the selected accs list
        var selectedAccType = _getAccType(masterAccType.Type, oppAccTypes);

        if (selectedAccType == undefined) { //not exists yet, patch in from the master list
            selectedAccType = _copyAccTypeSelectedValues(masterAccType.Type, masterAccType);
            oppAccTypes[oppAccTypes.length] = selectedAccType
        }

        if (selectedAccType.prevSelectedValues != undefined) selectedAccType.prevSelectedValues = null; //reset pre-selected values

        var bUseDefault = false;
        if (selectedAccType == undefined)
            bUseDefault = true;

        var accValues = masterAccType.Values;
        var selDiv = _oShared.divNode(null, "checkboxHolder");
        //selDiv.setAttribute('name', masterAccType.Type);
        selDiv.setAttribute('id', _oShared.buildId('accEdit_', masterAccType.Type));
        var ulNode = _oShared.ulNode(null, "checkbox-list");

        var len = accValues.length;
        for (var j = 0; j < len; j++) { //each acc value
            var accValue = accValues[j];

            var bSelected = false;
            //if testee does not have the acc type, then use default
            if (bUseDefault)
                bSelected = accValue.IsSelected;
            else
                if (_isSelected(selectedAccType.Value, accValue.Code))
                    bSelected = true;
            var opt = _oShared.liCheckBoxNode(accValue.Code, accValue.Value, accValue.Value, null, bSelected, false);

            ulNode.append(opt);
        }
        var checkBoxes = ulNode.getElementsByTagName('input');
        var len = checkBoxes.size();
        for (var i = 0; i < len; i++) {
            checkBoxes.item(i).on('click', _accSelOnChange, null, selDiv, testOpp, oppAccTypes, masterAccType, masterAccTypes);
        }
        //selDiv.on('change', _accSelOnChange, null, testOpp, oppAccTypes, masterAccType, masterAccTypes);
        selDiv.append(ulNode);
        return selDiv;
    }

    //for accs Edit
    function _buildSelBox(testOpp, oppAccTypes, masterAccType, masterAccTypes) {
        //ignore not visible acc Type IsVisible= false
        if (masterAccType == undefined || !masterAccType.IsVisible || testOpp == undefined)
            return null;
        var selectedAccType = _getAccType(masterAccType.Type, oppAccTypes);

        if (selectedAccType == undefined) { //not exists yet, patch from the master list
            selectedAccType = _copyAccTypeSelectedValues(masterAccType.Type, masterAccType);
            oppAccTypes[oppAccTypes.length] = selectedAccType
        }

        if (selectedAccType.prevSelectedValues != undefined) selectedAccType.prevSelectedValues = null; //reset pre-selected values

        var bUseDefault = false;
        if (selectedAccType == undefined)
            bUseDefault = true;

        var arySelectedIdx = []; //selected indexes for IE 6
        var accValues = masterAccType.Values;
        var N = Y.Node;
        var selElem = _oShared.selectNode("accomm_value");
        //selElem.setAttribute('name', masterAccType.Type);
        selElem.setAttribute('id', _oShared.buildId('accEdit_', masterAccType.Type));

        //flags to process: isselected, multiselection and allowcombine, allowchange, isselectable(readonly), visible
        var nAllowCombine = 0; // if this number is >1 then the list box is a multi selected
        selElem.setAttribute('multiple', 'multiple'); //allow multi-selection
        if (Y.UA.ie && Y.UA.ie < 7) {//IE6 Hack
            selElem.length = 10; selElem.length = 0;
        }
        var len = accValues.length;
        for (var j = 0; j < len; j++) { //each acc value
            var accValue = accValues[j];
            var opt = _oShared.optionNode(accValue.Value, null);
            opt.setAttribute('value', accValue.Code);
            var bSelected = false;
            //if testee does not have the acc type, then use default
            if (bUseDefault)
                bSelected = accValue.IsSelected;
            else
                if (_isSelected(selectedAccType.Value, accValue.Code))
                    bSelected = true;

            if (bSelected)
                arySelectedIdx.push(j); // selected index for IE6 hack
            //opt.setAttribute('selected', bSelected);
            opt.set('selected', bSelected);
            selElem.appendChild(opt);

            if (accValue.AllowCombine) nAllowCombine++;
        }
        if (nAllowCombine < 2) //single selected
            selElem.removeAttribute('multiple');
        else {
            selElem.addClass('multiline'); //IE Hack
            selElem.setAttribute('size', '5'); //IE Hack

            if (Y.UA.ie && Y.UA.ie < 7)//IE6 Hack
                _setSelectedValue(selElem, arySelectedIdx);
        }
        /*
        change all read-only selectbox to static text
        //NOTE: from Larry:
        //If the status is ‘pending’ then the test has not officially started and any accommodation available to the proctor can be changed.
        //If the status is ‘suspended’, then the test has officially started and any accommodations whose AllowChange flag = 0 cannot be altered.

        //NOTE: from H-A
        //the test has officially started when student clicked on "Begin Test Now"
        var bEnable = (testOpp.status == 'pending') ? true : accType.AllowChange;

        if (!bEnable || !accType.IsSelectable)
        selElem.setAttribute('disabled', 'disabled');
        */
        //add a event listener to handle allowcombine validation
        selElem.on('change', _accSelOnChange, null, selElem, testOpp, oppAccTypes, masterAccType, masterAccTypes);
        selElem.on('focus', _selBoxOnFocus);
        return selElem;
    }

    //mobile hack
    function _selBoxOnFocus(e) {
        Y.log("_selBoxOnFocus");
        //hack for mobile scrolling
        if (!Y.tdsShared.mobile())
            return;
        var shellElem = Y.one('#divApprovalsShell');
        if (shellElem != null) {
            shellElem.addClass('dropdown');
            setTimeout(function () {
                shellElem.removeClass('dropdown');
            }, 1000);
        }
    }

    //selElem: select box object
    //arySelectedIdx: list of selected indexes 
    //IE6 hack
    function _setSelectedValue(selElem, arySelectedIdx) {
        if (selElem == null || arySelectedIdx == null || arySelectedIdx.length < 1)
            return;
        // IE 6 BUG: initial option selection has to be on a 1 millisecond delay
        setTimeout(function () {
            var idx = arySelectedIdx.length;
            for (var i = 0; i < idx; i++)
                selElem.options[arySelectedIdx[i]].selected = true;
        }, 1);
    }

    //for a test opp, all acc types
    //mark if this selected values got changed from the default value for CUSTOM vs Standard settings
    function _isCustomAccs(oppAccTypes, masterAccTypes) {
        Y.log("_isCustomAccs");
        var bIsCustom = false;
        if (oppAccTypes == null || masterAccTypes == null)
            return bIsCustom;
        //--Find the source of all possible acc values
        var sourceAccTypes = masterAccTypes;
        var selectedAccTypes = oppAccTypes;
        var len = selectedAccTypes.length;
        for (var i = 0; i < len; i++) {
            var selectedAccType = selectedAccTypes[i];
            var sourceAccType = _getAccType(selectedAccType.Key, sourceAccTypes); //all possible acc values for a type
            if (sourceAccType == null) //somehow the test does not have this acc type.
                continue;
            var selectedValues = _getSelectedAccCode(selectedAccType);

            if (_isCustomAcc(sourceAccType.Value.Values, selectedValues)) {
                bIsCustom = true;
                break;
            }
        }
        return bIsCustom;
    }

    //for only one acc type
    //mark if this selected values got changed from the default value
    //sourceValues: an array of default values
    //selectedValues: any array of selected values string
    function _isCustomAcc(sourceValues, selectedValues) {
        if (sourceValues == null || selectedValues == null)
            return false;

        var sourceHash = _getSelectedAccCodeHash(sourceValues);

        //if not equal length, then it is custom
        if (sourceHash.length != selectedValues.length)
            return true;

        for (var i = 0; i < selectedValues.length; i++) {
            if (typeof (sourceHash[selectedValues[i]]) == 'undefined') {
                return true;
            }
        }
        return false;
    }
    //build new accType
    function _newAccType(key) {
        return { Key: key, Value: { Values: [] }, prevSelectedValues: null };
    }
    //copy accType with selected values ONLY
    function _copyAccTypeSelectedValues(name, masterAccType) {
        var accType = _newAccType(name);
        if (masterAccType == undefined || masterAccType.Values == undefined)
            return accType;

        var values = masterAccType.Values;
        var len = values.length;
        var accTypeValue = accType.Value;
        accTypeValue.AllowChange = masterAccType.AllowChange;
        accTypeValue.IsSelectable = masterAccType.IsSelectable;
        accTypeValue.IsVisible = masterAccType.IsVisible;
        accTypeValue.Label = masterAccType.Label;
        accTypeValue.Type = masterAccType.Type;
        accTypeValue.accDepParentType = masterAccType.accDepParentType;
        accTypeValue.dependOnType = masterAccType.dependOnType;
        accTypeValue.sOrder = masterAccType.sOrder;
        for (var i = 0; i < len; i++) { //for each option
            var value = values[i];
            if (value.IsSelected)
                accTypeValue.Values[accTypeValue.Values.length] = _buildAccValue(value.AllowCombine, value.Code, value.IsSelected, value.Value);
        }
        return accType;
    }

    //do validation on acc selection
    function _accSelOnChange(e, selElem, testOpp, oppAccTypes, masterAccTypeVal, masterAccTypes) {
        Y.log("AccTypes._accSelOnChange");
        //NOTE: Do not change value in testOpp.accTypes until setAcc button is clicked.
        //mark if this selected values got changed from the default value for CUSTOM vs Standard settings
        //validate: if allow combine flag is off, do allow multi-select with any other values
        //we need to be able to undo the selection. If the cache values is null, use testOpp.accTypes as the cache

        //On change
        testOpp.accChange = true;
        //--Find selected accType using name(selectbox name)
        var name = masterAccTypeVal.Type;
        var selectedAccType = _getAccType(name, oppAccTypes);

        if (selectedAccType == undefined) { //not exists yet
            selectedAccType = _copyAccTypeSelectedValues(name, masterAccTypeVal);
            oppAccTypes[oppAccTypes.length] = selectedAccType;
        }
        //if no cache, build the cache values
        if (selectedAccType.prevSelectedValues == undefined) {
            selectedAccType.prevSelectedValues = _getSelectedAccCode(selectedAccType);
        }

        var bUseCheckBoxes = (masterAccTypeVal.allowCombineCount > 1);
        //--Get selected values in an array from a select box or checkboxes group
        var selectedValues = (!bUseCheckBoxes) ? _oShared.getListBoxValues(selElem) : _oShared.getCheckedValues(selElem); //array of acc codes

        //validate if selected values are allow to be selected together (multi-selection)
        if (selectedValues == undefined || selectedValues.length < 1) { //if none selected, undo selections
            var str = Y.Messages.get('At lease one accommodation must be selected.');
            var msgDialog = _oShared.msgDialog(Y.tds.messages.getRaw("Important!"), str);
            msgDialog.addButton("close", Y.tds.messages.getRaw("Button.OK"), msgDialog.cancel);
            msgDialog.show(_oShared.NotificationType.warning);
            //undo the selections,
            (!bUseCheckBoxes) ? _oShared.setSelectedValues(selElem, selectedAccType.prevSelectedValues) : _oShared.setCheckedValues(selElem, selectedAccType.prevSelectedValues);
            return;
        }

        //set acc value readonly
        var selElemParent = selElem.get('parentNode');
        var accValElem = selElemParent.one('.accomm_value');

        //--Find the source of all possible acc values
        var sourceAccType = masterAccTypeVal; //all possible acc values
        if (sourceAccType == undefined || sourceAccType.Values.length < 1 || selectedValues == undefined || selectedValues.length < 1) //no source: this shouldn't happen
            return;

        var len = selectedValues.length;
        if (len > 1) { //only do this validation if more than one selected values
            for (var i = 0; i < len; i++) {
                var accValue = _allowCombine(sourceAccType, selectedValues[i]);
                if (accValue != null) {
                    var str = Y.Messages.get('Non-combinable accommodations can not have other accommodations selected. Do you want to continue with your selection?');
                    var msgDialog = _oShared.msgDialog(Y.tds.messages.getRaw("Important!"), str);
                    msgDialog.addButton("close", Y.tds.messages.getRaw("Button.Cancel"), function () {
                        //undo the selections,
                        (!bUseCheckBoxes) ? _oShared.setSelectedValues(selElem, selectedAccType.prevSelectedValues) : _oShared.setCheckedValues(selElem, selectedAccType.prevSelectedValues);
                        msgDialog.cancel();
                    });
                    msgDialog.addButton("confirm", Y.tds.messages.getRaw("Button.Select"), function () {
                        _oShared.removeByElements(selectedValues, selectedAccType.prevSelectedValues); //remove all pre-selected values and keep only newly selected values
                        (!bUseCheckBoxes) ? _oShared.setSelectedValues(selElem, selectedValues) : _oShared.setCheckedValues(selElem, selectedValues);
                        selectedAccType.prevSelectedValues = selectedValues;
                        if (accValElem != null) accValElem.setContent(_selectedValuesToText(selectedValues, masterAccTypeVal));
                        msgDialog.cancel();
                    });
                    msgDialog.show(_oShared.NotificationType.warning);

                    return;
                }
            }
        }
        Y.log(selectedAccType.prevSelectedValues);

        //set the cache values
        selectedAccType.prevSelectedValues = selectedValues;
        if (accValElem != null && bUseCheckBoxes) accValElem.setContent(_selectedValuesToText(selectedValues, masterAccTypeVal));


        //process acc dependencies here...
        //get a list of children that belong to this parent: accDepParentTypes
        //remove ALL children that are relate to this parent: the current selectbox(this); accDepParentTypes[i].accDepChildTypes
        //render children that are relate to these selected parent values

        var parentDiv = selElemParent.get('parentNode');

        _removeAllaccDepChildren(parentDiv, masterAccTypeVal.accDepParentTypes);

        Y.tdsApprovalOpps.renderAccsDependencies(testOpp, oppAccTypes, masterAccTypes, parentDiv, name, masterAccTypeVal.accDepParentTypes)

        Y.log("AccTypes._accSelOnChange --end");
    }



    //remove ALL children that are relate to this parent: the current selectbox(this); accDepParentTypes[i].accDepChildTypes
    function _removeAllaccDepChildren(parentDiv, accDepParentTypes) {
        if (parentDiv == undefined || accDepParentTypes == undefined)
            return; //do nothing        

        var len = accDepParentTypes.length;
        for (var i = 0; i < len; i++) {
            var accDepParentType = accDepParentTypes[i];
            var accDepChildTypes = accDepParentType.accDepChildTypes;
            var cnt = accDepChildTypes.length;
            for (var j = 0; j < cnt; j++) {
                var accDepChildType = accDepChildTypes[j];
                var accTypeDivId = '#' + _oShared.accTypeDivId(accDepChildType.thenType);
                var accTypeDiv = parentDiv.one(accTypeDivId);
                if (accTypeDiv == null) continue;
                accTypeDiv.remove(true);
            }
        }
    }

    //return the acc value object if not allow else return null
    function _allowCombine(sourceAccType, selectedAccCode) {
        var sourceAccTypeVals = sourceAccType.Values;
        var len = sourceAccTypeVals.length;
        for (var i = 0; i < len; i++) {
            if (sourceAccTypeVals[i].Code == selectedAccCode) {
                if (sourceAccTypeVals[i].AllowCombine)
                    return null;
                else
                    return sourceAccTypeVals[i];
            }
        }
        return null;
    }
    //return an associative array of selected acc codes
    //accTypeValues: is an array of acc Value object
    function _getSelectedAccCodeHash(accTypeValues) {
        var aryObj = {};
        aryObj.length = 0;
        if (accTypeValues == undefined)
            return aryObj;
        var len = accTypeValues.length;
        for (var i = 0; i < len; i++) {
            if (accTypeValues[i].IsSelected != undefined && accTypeValues[i].IsSelected) {
                aryObj[accTypeValues[i].Code] = accTypeValues[i].Code;
                aryObj.length += 1;
            }
        }
        return aryObj;
    }
    //return an array of selected acc codes
    function _getSelectedAccCode(accType) {
        var ary = new Array();
        if (accType == undefined || accType.Value == undefined || accType.Value.Values == undefined)
            return ary;
        var accTypeValues = accType.Value.Values;
        var len = accTypeValues.length;
        for (var i = 0; i < len; i++) {
            if (accTypeValues[i].IsSelected != undefined && accTypeValues[i].IsSelected)
                ary.push(accTypeValues[i].Code);
        }
        return ary;
    }
    //make a deep copy and return the copy
    //accType: the original type
    //copy only values in thenValues
    function _copyAccType(accType, thenValues) {
        if (accType == null || thenValues == null || thenValues.length < 1) return null;
        var newAccType = {};
        newAccType.Key = accType.Key;
        newAccType.Value = {};
        newAccType.Value.AllowChange = accType.Value.AllowChange;
        newAccType.Value.IsSelectable = accType.Value.IsSelectable;
        newAccType.Value.IsVisible = accType.Value.IsVisible;
        newAccType.Value.Label = accType.Value.Label;
        newAccType.Value.Type = accType.Value.Type;
        newAccType.Value.dependOnType = null; //accType.Value.dependOnType;
        newAccType.Value.sOrder = accType.Value.sOrder;
        newAccType.Value.accDepParentTypes = accType.Value.accDepParentTypes;
        newAccType.Value.allowCombineCount = accType.Value.allowCombineCount;
        newAccType.Value.Values = new Array();

        var newValues = newAccType.Value.Values;
        var sourceValues = accType.Value.Values;
        var sourceLen = sourceValues.length;
        for (var i = 0; i < sourceLen; i++) {
            var sourceValue = sourceValues[i];

            var len = thenValues.length;
            for (var j = 0; j < len; j++) {
                var value = thenValues[j];
                if (value.thenValue != sourceValue.Code) continue;

                var newValue = {};
                newValue.AllowCombine = sourceValue.AllowCombine;
                newValue.Code = sourceValue.Code;
                newValue.IsSelected = value.isDefault;
                newValue.Value = sourceValue.Value;

                newValues[newValues.length] = newValue;
            }
        }
        return newAccType;
    }

    //return an accType object
    function _getAccType(type, accTypes) {
        if (type == undefined || accTypes == undefined || accTypes.length < 1)
            return null;
        var len = accTypes.length;
        for (var i = 0; i < len; i++) {
            if (type == accTypes[i].Key)
                return accTypes[i];
        }
        return null;
    }
    //get an accType index location, return an interger index if found else return -1
    function _getAccTypeIdx(type, accTypes) {
        if (type == undefined || accTypes == undefined || accTypes.length < 1)
            return -1;
        var len = accTypes.length;
        for (var i = 0; i < len; i++) {
            if (type == accTypes[i].Key)
                return i;
        }
        return -1;
    }
    function _isSelected(selectedAccType, accCode) {
        if (selectedAccType == undefined || accCode == undefined)
            return false;
        var accValues = selectedAccType.Values;
        var len = accValues.length;
        for (var i = 0; i < len; i++) {
            //Y.log("isSelected.accValues[i].Code = " + accValues[i].Code);
            if (accCode == accValues[i].Code)
                return true;
        }
        return false;
    }
    function _getAccCodes_OLD(accTypes) {
        var str = '';
        if (accTypes == undefined)
            return str;
        var len = accTypes.length;
        for (var i = 0; i < len; i++) {
            var aryValues = accTypes[i].Value.Values;
            for (var j = 0; j < aryValues.length; j++)
                str += aryValues[j].Code + ';';
        }
        Y.log("getAccCodes=" + str);
        return str;
    }

    //get pipe delimiter of acc codes
    function _getAccCodes(accTypesList) {
        var str = '';
        if (accTypesList == undefined)
            return str;

        var len = accTypesList.length; //for each test or segment acc Types
        for (var k = 0; k < len; k++) {
            var accTypes = accTypesList[k];
            var accTypesLen = accTypes.length;
            for (var i = 0; i < accTypesLen; i++) {
                var aryValues = accTypes[i].Value.Values;
                for (var j = 0; j < aryValues.length; j++)
                    str += aryValues[j].Code + '|';
            }
            str += ';'//test or segment accs delimiter            
        }
        return str;
    }

    function _formatAccValues(accType) {
        var str = '';
        if (accType == undefined || accType.Values == undefined)
            return str;
        var values = accType.Values;
        var len = values.length;
        for (var i = 0; i < len; i++) {
            if (str.length < 1) str = accType.Type + ': ' + values[i].Value;
            else
                str += ' | ' + accType.Type + ': ' + values[i].Value;
        }
        return str;
    }

    function _getAccTypeLabel(accTypeVal) {
        return (accTypeVal.Label == null || accTypeVal.Label.length < 1) ? accTypeVal.Type : accTypeVal.Label;
    }

    //deal with accsString 
    //return an associative array of <atype>: <object Key, Values(arrays) >
    function _parseAccsString(accsString) {
        if (accsString == undefined || accsString.length < 1)
            return null;
        var accTypes = new Array();
        var aryTypes = accsString.split('|');
        var len = aryTypes.length;
        for (var i = 0; i < len; i++) {
            var aryValue = aryTypes[i].split(':');
            if (aryValue.length != 2) //skip the invalid acc string
                continue;
            var atype = Y.Lang.trim(aryValue[0]);

            var avalue = Y.Lang.trim(aryValue[1]);
            if (accTypes[atype] == undefined || accTypes[atype].Values == undefined)
                accTypes[atype] = { IsVisible: true, Values: new Array(avalue) };
            else {
                accTypes[atype].Values[accTypes[atype].Values.length] = avalue;
            }
        }
        return accTypes;
    }
    //Deal with accsString array aryTypes = <atype>: <object Key, Values(arrays) >
    function _getAccDetailsViewString(aryTypes) {
        if (aryTypes == undefined)
            return '';
        var output = '';
        var sortAccTypes = _sortAccType(aryTypes);
        var len = sortAccTypes.length;
        for (var n = 0; n < len; n++ ) {
            var aType = sortAccTypes[n][1];
            if (aType.IsVisible != undefined && !aType.IsVisible) //skip non visible types
                continue;
            output += '<div class="accommodation"><span class="accomm_name">' + aType.Label + ':</span><span class="accomm_value">';
            var values = aType.Values;
            for (var i = 0; i < values.length; i++) {
                if (i > 0)
                    output += ', ';
                output += values[i];
            }
            output += '</span></div>';
        }
        return output;
    }

    function _sortAccType(aryTypes) {
        var sortAryTypes = [];
        for (var type in aryTypes) sortAryTypes.push([type, aryTypes[type]]);
        sortAryTypes.sort(function (a, b) {
            a = a[1].sOrder;
            b = b[1].sOrder;
            return a < b ? -1 : (a > b ? 1 : 0);
        });
        return sortAryTypes;
    }

    //check to make sure selected values that is a subset of the master list of values
    //Description: Get selected values, return an array object of values and return null if no match
    function _getSelectedAccTypeValue(selectedAccTypeV, masterAccTypeV) {
        if (selectedAccTypeV == null || selectedAccTypeV.Values == null)
            return masterAccTypeV;
        if (masterAccTypeV == null || masterAccTypeV.Values == null)
            return selectedAccTypeV;
        var selectedValues = selectedAccTypeV.Values;
        var masterValues = masterAccTypeV.Values;
        var sLen = selectedValues.length;
        var mLen = masterValues.length;
        var newSelectedValues = new Array();
        for (var i = 0; i < sLen; i++) {
            var selectedValue = selectedValues[i];
            for (var j = 0; j < mLen; j++) {
                if (selectedValue.Code == masterValues[j].Code) {
                    selectedValue.Value = masterValues[j].Value;
                    newSelectedValues.push(selectedValue);
                }
            }
        }
        if (newSelectedValues.length < 1) //selected values are not the subset of the master list
            return masterAccTypeV;
        selectedAccTypeV.Values = newSelectedValues;
        return selectedAccTypeV;
    }

    //build a accomm_value span elem
    function _buildAccValueSpan(aType) {
        var elem = _oShared.spanNode(null, 'accomm_value');
        if (aType == null)
            return elem;

        var values = aType.Values;
        var output = '';
        var len = values.length;
        var ids = '';
        var vals = '';
        for (var i = 0; i < len; i++) {
            var value = values[i]
            if (!value.IsSelected) continue;
            if (output.length > 0) {
                output += ', ';
                ids += '|';
                vals += '|';
            }
            output += value.Value;
            ids += value.Code;
            vals += value.Value;
        }
        elem.setContent(output);
        elem.setAttribute('name', ids);
        elem.setAttribute('value', vals);
        return elem;
    }

    //selectedValuesArray: contain acc codes and we need to get the value for display
    //only do this for mobile    
    function _selectedValuesToText(selectedValuesArray, accTypeVal) {
        if (!_oShared.mobile()) return '';
        if (selectedValuesArray == null || accTypeVal == null) return '';
        var masterValues = accTypeVal.Values;
        var masterLen = masterValues.length;
        var selLen = selectedValuesArray.length;
        var output = '';
        for (var s = 0; s < selLen; s++) {
            var selCode = selectedValuesArray[s];
            for (var m = 0; m < masterLen; m++) {
                var masterVal = masterValues[m];
                if (selCode == masterVal.Code) {
                    if (output.length > 0)
                        output += ', ';
                    output += masterVal.Value;
                    break;
                }
            }
        }
        return output;
    }

    //Deal with accsString array aryTypes = <atype>: <object Key, Values(arrays) >
    function _setIsVisibleAndLabel(masterAccTypes, aryTypes) {
        if (masterAccTypes == null || aryTypes == null)
            return;
        var len = masterAccTypes.length;
        for (var i = 0; i < len; i++) {
            var masterAccType = masterAccTypes[i];
            var atype = aryTypes[masterAccType.Key];
            if (atype != null) {
                atype.IsVisible = masterAccType.Value.IsVisible;
                atype.sOrder = masterAccType.Value.sOrder;
                //do translation
                atype.Label = _getAccTypeLabel(masterAccType.Value);
                _setAccValueLabel(atype.Values);
            }
        }
    }
    //do translation on the acc value
    function _setAccValueLabel(values) {
        if (values == undefined) return;

        var destLen = values.length;
        for (var i = 0; i < destLen; i++) {
            values[i] = _oMessages.getRaw(values[i]);
        }
    }



    //-------------------------------------------------------------------------
    // Public interface
    //-------------------------------------------------------------------------
    Y.tdsAccTypes = {
        loadATestAccs: function (testOpp, renderFunc) {
            _loadATestAccs(testOpp, renderFunc);
        },

        loadAccs: function (testOpp, segments, renderFunc) {
            _loadAccs(testOpp, segments, renderFunc);
        },

        buildSelBox: function (testOpp, oppAccTypes, masterAccType, masterAccTypes) {
            return _buildSelBox(testOpp, oppAccTypes, masterAccType, masterAccTypes);
        },
        buildCheckBoxGroup: function (testOpp, oppAccTypes, masterAccType, masterAccTypes) {
            return _buildCheckBoxGroup(testOpp, oppAccTypes, masterAccType, masterAccTypes);
        },

        buildID: function (name) {
            return _buildID(name);
        },
        getAccs: function (key) {
            return _data[key];
        },
        data: function () {
            return _data;
        },
        setData: function (data) {
            _data = data;
        },
        getAccType: function (type, accTypes) {
            return _getAccType(type, accTypes);
        },
        getAccTypeIdx: function (type, accTypes) {
            return _getAccTypeIdx(type, accTypes);
        },

        getAccCodes: function (accTypes) {
            return _getAccCodes(accTypes);
        },
        newAccType: function (key) {
            return _newAccType(key);
        },
        parseAccsString: function (accsString) {
            return _parseAccsString(accsString);
        },

        getAccTypeLabel: function (accTypeVal) {
            return _getAccTypeLabel(accTypeVal);
        },
        getAccDetailsViewString: function (aryTypes) {
            return _getAccDetailsViewString(aryTypes);
        },

        buildAccValue: function (allowCombine, code, isSelected, value) {
            return _buildAccValue(allowCombine, code, isSelected, value);
        },
        getSelectedAccTypeValue: function (selectedAccTypeV, masterAccTypeV) {
            return _getSelectedAccTypeValue(selectedAccTypeV, masterAccTypeV);
        },

        buildAccValueSpan: function (aType) {
            return _buildAccValueSpan(aType);
        },
        isCustomAccs: function (oppAccTypes, masterAccTypes) {
            return _isCustomAccs(oppAccTypes, masterAccTypes);
        },
        setIsVisibleAndLabel: function (masterAccTypes, aryTypes) {
            _setIsVisibleAndLabel(masterAccTypes, aryTypes);
        },
        copyAccType: function (accType, thenValues) {
            return _copyAccType(accType, thenValues);
        },
        removeAllaccDepChildren: function (parentDiv, accDepParentTypes) {
            return _removeAllaccDepChildren(parentDiv, accDepParentTypes);
        },
        getSelectedAccCode: function (accType) {
            return _getSelectedAccCode(accType);
        },
        copyAccTypeSelectedValues: function (name, masterAccType) {
            return _copyAccTypeSelectedValues(name, masterAccType);
        }

    };
}, "0.1", { requires: ["node", "io", "json-parse"] });
