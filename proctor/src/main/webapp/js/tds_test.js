//required: yui-min.js
//selectable tests
YUI.add("tds-tests", function (Y) {
    //-------------------------------------------------------------------------
    // Private variables
    //-------------------------------------------------------------------------
    var _data = null,
        _selectedTests = null, // list of selected tests    
        _oMenu = null,
        _oMenuItemCfg = null,
        _sortBy = null;
    var _oMessages = Y.Messages;
    var _oShared = Y.tdsShared;
    var _oSession = Y.tdsSession;
    var _oClassName = Y.pClassName;
    var _selectBox = null;
    var _testIDs = null; // <string, int> hash tables for lookup
    var _testKeys = null; // <string, int> hash tables for lookup
    //-------------------------------------------------------------------------
    // Private functions
    //-------------------------------------------------------------------------
    function _load() {
        //A function handler to use for successful requests:
        var handleSuccess = function (ioId, o) {
            if (o.responseText != undefined) {
                try {
                    var testsData = Y.JSON.parse(o.responseText);
                    if (testsData == undefined || testsData.status == undefined || testsData.status == 'NODATA') { //no data return
                        _setData(null); return false;
                    }
                    _setData(testsData);
                    _buildHashTables(); //build hash tables for lookup
                    if (!loadSelectedTests())
                        _render(true);
                    return true;
                }
                catch (e) {
                    _oShared.showError(_oMessages.get('UnableToProcessRequest'));
                    Y.log("ERROR: Tests.load: " + e);
                    return false;
                }
            }
        };
        var handleFailure = function (ioId, o) {
            _oShared.showError(_oMessages.get('UnableToProcessRequest'));
            Y.log("ERROR: Tests.load: handleFailure");
        };

        var postData = "testKeys=" + testIDs;
        var key = Y.tdsSession.key();
        if (key != null)
            postData += "&sessionKey=" + key;

        // Configuration object for POST transaction 
        var cfg = {
            method: "POST",
            data: postData,
            timeout: gXHRTimeout, // Abort the transaction, if it is still pending, after X ms.
            headers: { "cache-control": "no-cache", 'X-Transaction': 'get tests' },
            on: {
                success: handleSuccess,
                failure: handleFailure
            }
        };

        Y.io("Services/XHR.axd/GetTests", cfg);
    }
    function _loadSelectedTests() {
        var key = Y.tdsSession.key();
        if (key == null)
            return false;
        //A function handler to use for successful requests:
        var handleSuccess = function (ioId, o) {
            if (o.responseText != undefined) {
                try {
                    var selectedTests = Y.JSON.parse(o.responseText);
                    if (selectedTests == undefined || selectedTests.status == undefined || selectedTests.status == 'NODATA') { //no data return
                        _selectedTests = null; return false;
                    }
                    _selectedTests = selectedTests;
                    _setIsSelected();
                    _render(true);
                }
                catch (e) {
                    _oShared.showError(_oMessages.get('UnableToProcessRequest'));
                    Y.log("ERROR: Tests.loadSelectedTests: " + e);
                    return false;
                }
            }
        };
        var handleFailure = function (ioId, o) {
            _oShared.showError(_oMessages.get('UnableToProcessRequest'));
            Y.log("ERROR: Tests.loadSelectedTests: handleFailure");
        };

        var postData = "sessionKey=" + key;
        // Configuration object for POST transaction 
        var cfg = {
            method: "POST",
            data: postData,
            timeout: gXHRTimeout, // Abort the transaction, if it is still pending, after X ms.
            headers: { "cache-control": "no-cache", 'X-Transaction': 'get tests' },
            on: {
                success: handleSuccess,
                failure: handleFailure
            }
        };

        Y.io("Services/XHR.axd/GetSessionTests", cfg);
    }

    function _render(bRemoveAll) {
        Y.log("tdsTest._render");
        _selectBoxInit(); // init tests list if not already is

        if (bRemoveAll)
            _clear(); //clear tests list       

        var sortByGrade = Y.one('#radioSortTests_grade');
        var sortBy = (sortByGrade.get('checked')) ? "grade" : "subject";
        _sort(sortBy);
        _applyFilters();
        var tests = _getData();
        var len = tests.length;
        for (var i = 0; i < len; i++) {          
            var test = tests[i];
            if (this._bApplyFilters && test.isFiltered) continue; //do not display filtered tests

            var checked = false, disabled = false, classN = null;
            if (test.isSelected != undefined && test.isSelected) {
                checked = true;
                disabled = true;
                classN = 'selected';
            }
            else if (test.isSelectedYetInsert != undefined && test.isSelectedYetInsert) {
                checked = true;
            }
            var node = _oShared.liCheckBoxNode(test.Key, null, test.DisplayName, classN, checked, disabled);

            _selectBox.append(node);
        }
        var checkBoxes = _selectBox.getElementsByTagName('input');
        var len = checkBoxes.size();
        for (var i = 0; i < len; i++) {
            checkBoxes.item(i).on('click', _testsSelectionChange);
        }
        //_buildHashTables(); //build hash tables for lookup
    }

    function _applyFilters() {
        this._bApplyFilters = false;
        var tests = _getData();
        var len = tests.length;
        //get filter inputs
        var objFilters = _getFilters(); 
        //if(objFilters == null) return; //no filters
        for (var i = 0; i < len; i++) {
            var test = tests[i];
            if (test.Category == null) return; //for this case, we dont apply any filters
            if (objFilters== null ||((objFilters.Category == '' || objFilters.Category == test.Category) &&
                (objFilters.GradeText == '' || objFilters.GradeText == test.GradeText) &&
                (objFilters.Subject == '' || objFilters.Subject == test.Subject)))
                test.isFiltered = false;
            else
                test.isFiltered = true;
        }
        this._bApplyFilters = true;
        _renderFilters();
    }

    //call this after _applyFilters
    function _renderFilters() {
        if (!this._bApplyFilters) {
            return;
        }
        var filterOptions = _getFilterOptions();
        var objFilters = _getFilters();
        var elemCat = _getElemSelCategory();
        var elemGrade = _getElemSelGradeText();
        var elemSubject = _getElemSelSubject();
        _oShared.renderSingleSelect(elemCat, 'Category', _sortKeys(filterOptions.Categories),
            (objFilters==null)? '' : objFilters.Category);
        _oShared.renderSingleSelect(elemGrade, 'Grade',  _oShared.getObjectKeys(filterOptions.GradeTexts),
            (objFilters == null) ? '' : objFilters.GradeText);
        _oShared.renderSingleSelect(elemSubject, 'Subject', _sortKeys(filterOptions.Subjects),
            (objFilters == null) ? '' : objFilters.Subject);
        if (elemCat == null) return;
        elemCat.detach(); elemCat.on('change', _render, this);
        elemGrade.detach(); elemGrade.on('change', _render, this);
        elemSubject.detach(); elemSubject.on('change', _render, this);
        _oShared.addBodyClass('show_filter');
    }

    //obj: is a hash obj
    //return: array of sorted keys
    function _sortKeys(obj) {
        if (obj == null) return null;
        var keys = _oShared.getObjectKeys(obj);
        keys.sort();
        return keys;
    }

    //return: obj with 3 arrays (Categories, GradeTexts, Subjects)
    function _getFilterOptions() {
        var tests = _getData();
        var len = tests.length;
        var categories = {};
        var gradeTexts = {};
        var subjects = {};
        for (var i = 0; i < len; i++) {
            var test = tests[i];
            if (categories[test.Category] == undefined)
                categories[test.Category] = test.Category;
            if (gradeTexts[test.GradeText] == undefined)
                gradeTexts[test.GradeText] = test.GradeText;
            if (subjects[test.Subject] == undefined)
                subjects[test.Subject] = test.Subject;
        }        
        return { Categories: categories, GradeTexts: gradeTexts, Subjects: subjects };
    }

    //return: obj with 3 properties, gradeText, subject, and category
    function _getFilters() {        
        var category = _oShared.getSelectedValue(_getElemSelCategory());
        var gradeText = _oShared.getSelectedValue(_getElemSelGradeText());
        var subject = _oShared.getSelectedValue(_getElemSelSubject());
        if (category == '' && gradeText == '' && subject == '') return null;  
        return { Category: category, GradeText: gradeText, Subject: subject };
        
        //return { Category: '', GradeText: '', Subject: 'Reading' };
    }
    function _getElemSelCategory(){
        return Y.one('#selCategoryListBox');
    }
    function _getElemSelGradeText(){
        return Y.one('#selGradeTextListBox');
    }
    function _getElemSelSubject(){
        return Y.one('#selSubjectListBox');
    }
    function _resetIsSelected() {
        var selectBox = _selectBoxInit(); // init select box if not already is
        var options = selectBox.getElementsByTagName('input');
        //var tests = _getData();
        var len = options.size();//tests.length;

        for (var i = 0; i < len; i++) {
            var item = options.item(i);
            var test = _getTest(item.get('id'));//tests[i];            
            if (test.isSelected) {
                item.set('checked', true);
                item.addClass('selected');
            }
            else {
                item.set('checked', false);
                item.removeClass('selected');
            }
        }
    }

    function _clear() {
        if (_selectBox == null) return;

        _selectBox.setContent(''); // clear all children
    }

    function _selectBoxInit() {
        if (_selectBox == null) {
            if (_oShared.mobile())
                _selectBox = Y.one("#selTestsListBox2");
            else
                _selectBox = Y.one("#selTestsListBox");
        }
        return _selectBox;
    }

    function _setIsSelected() {
        var tests = _getData();
        if (tests == null)
            return;
        var selectedTests = _selectedTests;
        var len = tests.length;
        for (var i = 0; i < len; i++)
            tests[i].isSelected = false; //unselected by default
        if (selectedTests == null) //no selected tests
            return;
        for (var i = 0; i < len; i++) {
            var sLen = selectedTests.length;
            for (var j = 0; j < sLen; j++)
                if (selectedTests[j] == tests[i].Key) {
                    tests[i].isSelected = true;
                    break;
                }
        }
    }

    //build test IDs and Keys hash table for lookup
    function _buildHashTables() {
        Y.log("tdsTest._buildHashTables");
        //if (_testIDs != null && _testKeys != null && !rebuild) return; //already built 
        var tests = _getData();
        if (tests == null)
            return;
        _testIDs = new P.Util.Structs.Map(); // <string, int>
        _testKeys = new P.Util.Structs.Map(); // <string, int>
        var len = tests.length;
        for (var i = 0; i < len; i++) {
            _testIDs.set(tests[i].Id, i)
            _testKeys.set(tests[i].Key, i)
        }
    }

    //get test by Key
    function _getTest(key) {
        var tests = _getData();
        if (tests == null)
            return null;
        if (_testKeys == null)
            _buildHashTables();
        if (_testKeys == null)
            return null;
        var idx = _testKeys.get(key);
        return tests[idx];
    }
    //get test by Id
    function _getTestById(id) {
        var tests = _data;
        if (tests == null)
            return null;
        if (_testIDs == null)
            _buildHashTables();
        if (_testIDs == null)
            return null;
        var idx = _testIDs.get(id);
        return tests[idx];
    }

    //return array of testids
    function _getTestIDs(aryTestKeys) {
        if (aryTestKeys == null || aryTestKeys.length < 1)
            return null;
        var testIDs = new Array();
        var len = aryTestKeys.length;
        for (var i = 0; i < len; i++) {
            var test = _getTest(aryTestKeys[i]);
            testIDs.push(test.Id);
        }
        return testIDs;
    }

    //get testname by key
    function _getTestName(key) {
        var test = _getTest(key);
        if (test == null)
            return '';
        else
            return test.DisplayName;
    }

    //get testname by Id
    function _getTestNameById(id) {
        var test = _getTestById(id);
        if (test == null)
            return '';
        else
            return test.DisplayName;
    }

    //set testName properties for all test Opps
    function _setTestName(testOpps) {
        if (testOpps == null) return;
        var len = testOpps.length;
        var currentTest = null;
        for (var i = 0; i < len; i++) {
            var testOpp = testOpps[i];
            var testKey = testOpp.testKey;
            if (currentTest == null || currentTest.Key != testKey) {//look for the test object
                currentTest = _getTest(testKey);
            }
            testOpp.testName = currentTest.DisplayName;
        }
    }

    function _sortTests(e, sortBy) {
        Y.log("Tests.sortTests");
        _sort(sortBy);
        _render(true);
    }

    function _getData() {
        return _data;
    }

    function _setData(testsdata) {
        _data = testsdata;
        _testIDs = null;
        _testKeys = null;
    }

    function _sort(sortBy) {
        Y.log("_sort");
        if (sortBy == null || sortBy.length < 1 || _sortBy == sortBy)
            return;
        var tests = _getData();
        if (tests == null)
            return;
        //save selected tests but not yet insert into the database
        _saveSelectedTestsYetInsert();

        _sortBy = sortBy;
        if (sortBy == "subject")
            tests.sort(_sortBySubject);
        else
            tests.sort(_sortByGrade);

        _buildHashTables(); //rebuild the hash table
    }
    function _sortBySubject(a, b) {
        if (a.Subject == null || b.Subject == null)
            return -1
        if (a.Subject < b.Subject)
            return -1;
        if (a.Subject > b.Subject)
            return 1;
        else {
            return _sortByGrade(a, b); // sort by subject and then grade
        }
    }
    //By default SortOrder column from the database is sort by grade
    //For grade sort, we just use this column
    function _sortByGrade(a, b) {
        if (a.SortGrade == null || b.SortGrade == null)
            return -1
        if (a.SortGrade < b.SortGrade)
            return -1;
        if (a.SortGrade > b.SortGrade)
            return 1;
        else {
            return 0;
        }
    }

    //tests selection handler 
    //when user clicked on the tests list  
    function _testsSelectionChange(e) {
        Y.log("_testsSelectionChange");
        //NOTE: Not mobile browser, we will NOT use default selected attribute to determine test selection. 
        //We will use class="selected" for this.
        //var hasSelected = _selectedSwitch();
        //if (!hasSelected) return;

        //confirm if the user wants to add, if one or more test selected, then enable the Start Session button.
        if (_oSession.hasSession()) {
            //e.halt(true);
            var msgDialog = _oShared.msgDialog(Y.tds.messages.getRaw("Important!"), _oMessages.get("You have added test(s) to this session. Would you like to proceed?"));
            msgDialog.addButton("confirm", Y.tds.messages.getRaw("Button.Yes"), _onTestsSelChangeConfirm);
            msgDialog.addButton("close", Y.tds.messages.getRaw("Button.No"), _onTestsSelChangeNotConfirm);
            msgDialog.show(_oShared.NotificationType.info);
        }
        else
            _onTestsSelChangeConfirm();
    }

    //tests selection handler after confirmation
    function _onTestsSelChangeConfirm() {
        Y.log("_onTestsSelChangeConfirm");
        _onTestsSelChange();
        var msgDialog = _oShared.msgDialog(null, null);
        msgDialog.cancel();
    }

    //tests selection change handler
    function _onTestsSelChange() {
        Y.log("_onTestsSelChange");
        var arySelTests = _getSelectedTests();

        if (arySelTests != null && arySelTests.length > 0) { //some selected
            _oShared.removeBodyClass(_oClassName.noneSelected);
            _oShared.addBodyClass(_oClassName.someSelected);
            //insert selected tests if session already started
            if (_oSession.hasSession())
                _oSession.insertTests(arySelTests); // _oSession.insertTests(arySelTests.join('|'));
            return;
        }

        if (_oSession.hasSession())
            return;

        _oShared.addBodyClass(_oClassName.noneSelected);
        _oShared.removeBodyClass(_oClassName.someSelected);
        _oShared.addBodyClass(_oClassName.notStarted);
    }
    function _onTestsSelChangeNotConfirm() {
        Y.log("_onTestsSelChangeNotConfirm");
        _resetIsSelected();
        var msgDialog = _oShared.msgDialog(null, null);
        msgDialog.cancel();
    }

    //get all tests that currently not in the session and return an array of tests id
    function _getAllActiveTests() {
        Y.log("_getAllActiveTests");
        var selectBox = _selectBoxInit();
        var options = selectBox.getElementsByTagName('input');
        if (options == null) return null;
        var len = options.size();
        var selectedTests = new Array();
        for (var i = 0; i < len; i++) {
            var item = options.item(i);
            var isDisabled = item.get('disabled');
            if (isDisabled) continue;

            selectedTests.push(item.get('id'));
        }
        return selectedTests;
    }

    //select all active tests and return nothing
    function _selectAllActiveTests() {
        Y.log("_selectAllActiveTests");
        var selectBox = _selectBoxInit();
        var options = selectBox.getElementsByTagName('input');
        if (options == null) return null;
        var len = options.size();

        for (var i = 0; i < len; i++) {
            var item = options.item(i);
            var isDisabled = item.get('disabled');
            if (isDisabled) continue;
            item.set('checked', true);
        }
    }

    //    //for load test only **************************** **********************************************
    //    //get all active tests with a certain name and return an array of tests id
    //    //str is null: this is for all active tests
    //    function _getAllActiveTestsPartial(str) {
    //        Y.log("_getAllActiveTestsPartial");
    //        var selectBox = _selectBoxInit();
    //        var options = selectBox.get('options');
    //        if (options == null) return null;
    //        var len = options.size();
    //        var selectedTests = new Array();
    //        for (var i = 0; i < len; i++) {
    //            var item = options.item(i);
    //            var isDisabled = item.get('disabled');
    //            if (isDisabled) continue;
    //            var value = item.get('value');
    //            if (str != null && value.indexOf(str) == -1)
    //                continue;
    //            selectedTests.push(value);
    //        }

    //        return selectedTests;
    //    }

    //get all selected tests but not yet inserted
    function _getSelectedTests() {
        var selectBox = _selectBoxInit();
        var options = selectBox.getElementsByTagName('input');
        if (options == null) return null;
        var len = options.size();
        var selectedTests = new Array();
        for (var i = 0; i < len; i++) {
            var item = options.item(i);
            var isDisabled = item.get('disabled');
            if (isDisabled) continue;
            var isSelected = item.get('checked');
            if (!isSelected) continue;
            selectedTests.push(item.get('id'));
        }
        return selectedTests;
    }

    function _saveSelectedTestsYetInsert() {
        var tests = _getData();
        if (tests == null)
            return;
        var selectedTests = _selectedTests;
        for (var i = 0; i < tests.length; i++)
            tests[i].isSelectedYetInsert = false; //unselected by default

        var selectBox = _selectBoxInit();
        var options = selectBox.getElementsByTagName('input');
        if (options == null) return null;
        var len = options.size();

        for (var i = 0; i < len; i++) {
            var item = options.item(i);
            var isDisabled = item.get('disabled');
            if (isDisabled) continue;
            var isSelected = item.get('checked'); //if has a class, then selected

            if (!isSelected) continue;
            var testKey = item.get('id');
            var test = _getTest(testKey);
            if (test != null)
                test.isSelectedYetInsert = true;
        }
    }
    //init without loading the data from the server
    function _init() {
        Y.log("Y.tdsTests._init");
        if (_oMessages == undefined)
            _oMessages = Y.Messages;
        if (_oShared == undefined)
            _oShared = Y.tdsShared;
        if (_oSession == undefined)
            _oSession = Y.tdsSession;
        Y.one("#radioSortTests_grade").on('click', _sortTests, null, 'grade');
        Y.one("#radioSortTests_subject").on('click', _sortTests, null, 'subject');
        _selectBoxInit();
        Y.pElem.btnSettingsMobile.on('click', _selectBoxFocus);
        Y.one("#btnSettingsMobileClose").on('click', _selectBoxClose);
    }

    function _selectBoxFocus(e) {
        //add a "selectTests" class to the body and remove the class when click "close"
        _oShared.addBodyClass('selectTests');
        _oShared.activateScollView(Y.one("#testsListBox_mobile"));
        e.halt(true);
    }
    function _selectBoxClose(e) {
        //add a "selectTests" class to the body and remove the class when click "close"
        _oShared.removeBodyClass('selectTests');
        e.halt(true);
    }

    function _isSegmented(testKey) {
        var test = _getTest(testKey)
        if (test == null) return false;
        return test.IsSegmented;
    }


    //-------------------------------------------------------------------------
    // Public interface
    //-------------------------------------------------------------------------
    Y.tdsTests = {
        init: function () {
            _init();
        },
        render: function (bRemoveAll) {
            _render(bRemoveAll);
        },
        setData: function (data) {
            _setData(data);
        },
        setSelectedTests: function (selectedTests) {
            _selectedTests = selectedTests;
        },
        setIsSelected: function () {
            _setIsSelected();
        },
        hasSessionTests: function () {
            return (_selectedTests == null || _selectedTests.length < 1) ? false : true;
        },
        getTest: function (testKey) {
            return _getTest(testKey);
        },
        getTestNameById: function (id) {
            return _getTestNameById(id);
        },
        getTestIDs: function (aryTestKeys) {
            return _getTestIDs(aryTestKeys);
        },
        setTestName: function (testOpps) {
            return _setTestName(testOpps);
        },
        //do we have any tests?
        hasTests: function () {
            return (_data == null) ? false : true;
        },

        isSegmented: function (testKey) {
            return _isSegmented(testKey);
        },
        getSelectedTests: function () {
            return _getSelectedTests();
        },
        getAllActiveTests: function () {
            return _getAllActiveTests();
        },
        selectAllActiveTests: function () {
            _selectAllActiveTests();
        },
        onTestsSelChange: function () {
            _onTestsSelChange();
        }
    };
}, "0.1", { requires: ["node", "io", "tds", "tds-shared", "tds-session", "json-parse"] });

