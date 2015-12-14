//required: yui-min.js
YUI.add("p-UI", function (Y) {
    //-------------------------------------------------------------------------
    // Private variables
    //-------------------------------------------------------------------------
    var _idleTimeout = null,
        _xhrTimeout = 30000,
        _accsDetails = null,
        _appConfig = null; //view student accommodation details

    var _oClassName = Y.pClassName;
    var _oElem = Y.pElem;
    var _oRefresh = Y.tdsRefresh;
    var _oTDSSort = Y.tdsSort;
    var _oTestOpps = Y.tdsTestOpps;
    var _oSession = Y.tdsSession;
    var _oShared = Y.tdsShared;
    var _oTests = Y.tdsTests;
    var _oMessages = Y.Messages;
    var _oCookie = Y.tdsCookie;
    var _oTimeout = Y.tdsTimeout;
    var _oAccTypes = Y.tdsAccTypes;
    var _oTestOpp = null;
    var _oCurIframe = null;


    //-------------------------------------------------------------------------
    // Private functions
    //-------------------------------------------------------------------------  
    //sort
    function _sort(e, curSortAtt) {
        Y.log('pUI._sort');
        _oTDSSort.sortClick(curSortAtt);
        _oTestOpps.render();
    }



    //-------------------------------------------------------------------------
    // Public interface
    //-------------------------------------------------------------------------
    Y.pUI = {
        //initialize the proctor UI
        ///1. Session init(0) -->
        init: function (appConfig) {
            Y.log("pUI.init");
            _appConfig = appConfig;

            if (_appConfig != null && !_appConfig.IsOp)
                _oShared.addBodyClass('practiceTest');

            //display user name
            if (_appConfig != null && _appConfig.UserFullName != null) {
                var userNameElem = Y.one('#spanUserName'); userNameElem.setContent(_appConfig.UserFullName);
            }
            _oElem.init();
            _oElem.addListeners();
            if (_oTests == undefined)
                _oTests = Y.tdsTests;
            _oTests.init();
            if (_oSession == undefined)
                _oSession = Y.tdsSession;
            _oSession.init(0);
            if (_oTestOpps == undefined)
                _oTestOpps = Y.tdsTestOpps;

            if (_appConfig != null && _appConfig.Environment == 'FunctionalTest')//or just call this in the console P.TDS.pUI.selectAllTests();
                _oElem.btnSelectAllTests.setStyle('display', 'block');

            _oShared.activateScollView(Y.one("#bottomHalf"));
            _oShared.orientation();//check orientation and add class to body for mobile

            _oTDSSort.init(_oTestOpps.data(), _oElem.tblTestOpps, _sort);
            _oRefresh.init((_appConfig != null) ? _appConfig.RefreshValue : 30000, Y.pUI.refresh);

            _oTimeout.init((_appConfig != null) ? _appConfig.Timeout : null, null, true, null); //init timeout  

            //global event handler
            Y.Global.on('GlobalEvent:closeDialog', function (e, className) {
                Y.pUI.closeDialog(e, className);
            });

            //global event handler
            Y.Global.on('GlobalEvent:addRemoveClass', function (e) {
                var node = Y.one(document.body);
                if (e.bAdd == true) {
                    node.addClass(e.className);
                }
                else {
                    node.removeClass(e.className);
                }
            });

            Y.Global.on('GlobalEvent:registerActivity', function (e) {
                _oTimeout.registerActivity();
            });
        },



        //start and stop session **********************************************************************************************
        sessionStarted: function () {
            Y.log("pUI.sessionStarted");
            var pUI = Y.pUI;
            _oShared.removeBodyClass(_oClassName.notStarted);
            var bApprovals = Y.tdsApprovalOpps.hasApprovals();
            _oShared.removeBodyClass(_oClassName.settingsShow);
            if (bApprovals)
                _oShared.addBodyClass(_oClassName.approvalsNeeded);
            else
                _oShared.removeBodyClass(_oClassName.approvalsNeeded);

            //turn off waiting dialog
            _oShared.stopPleaseWait();
            pUI.startAutoRefresh();  //start auto refresh                  
            Y.tdsPing.init(_appConfig.PingInterval, _oSession.key()); //start ping the server
        },
        sessionStopped: function () {
            Y.log("pUI.sessionStopped");
            var pUI = Y.pUI;
            _oShared.addBodyClass(_oClassName.notStarted);

            if (_oTests.hasSessionTests()) {
                _oShared.removeBodyClass(_oClassName.noneSelected);
                _oShared.addBodyClass(_oClassName.someSelected);
                _oShared.addBodyClass(_oClassName.settingsShow);
            }
            else {
                _oShared.addBodyClass(_oClassName.noneSelected);
                _oShared.removeBodyClass(_oClassName.someSelected);
            }
            //turn off waiting dialog
            _oShared.stopPleaseWait();
            pUI.stopAutoRefresh();  //stop auto refresh
            Y.tdsPing.stop(); //stop ping the server
        },


        //start session onclick
        startSession: function () {
            Y.log("pUI.startSession");
            var pUI = Y.pUI;
            _oShared.startPleaseWait();
            //if no session data then call startNewSession()        
            if (!_oSession.hasSession())//start a new session
            {
                if (!pUI.startNewSession())
                    return false;
            }
            else
                pUI.startExistSession();
        },

        startNewSession: function () {
            Y.log("pUI.startNewSession");

            var arySelTests = _oTests.getSelectedTests();

            if (arySelTests == null || arySelTests.length < 1) {
                _oShared.showError(_oMessages.get('Select at least one test to start'));
                return false;
            }
            else {
                //insert new session & session tests
                _oSession.insertTests(arySelTests); // _oSession.insertTestsAndStartSession(arySelTests.join('|'));
            }
        },

        startExistSession: function () {
            Y.log("pUI.startExistSession");
            var arySelTests = _oTests.getSelectedTests();
            //insert more session tests if need
            if (arySelTests != null && arySelTests.length > 0) {
                _oSession.insertTests(arySelTests); // _oSession.insertTestsAndStartSession(arySelTests.join('|'));
            }
        },

        stopSession: function () {
            Y.log("pUI.stopSession");
            var msgDialog = _oShared.msgDialog(Y.tds.messages.getRaw("Important!"), _oMessages.get("PAUSE_TESTING_SESSION"));
            msgDialog.addButton("confirm", Y.tds.messages.getRaw("Button.OK"), Y.pUI.stopSessionOK);
            msgDialog.addButton("close", Y.tds.messages.getRaw("Button.Cancel"), msgDialog.cancel);
            msgDialog.show(_oShared.NotificationType.warning);
        },

        stopSessionOK: function (e, msgDialog) {
            _oShared.startPleaseWait();
            _oSession.stop();
            var msgDialog = _oShared.msgDialog(null, null);
            msgDialog.cancel();
        },

        //auto refresh **********************************************************************************************
        startAutoRefresh: function () {
            //Only auto start with a fresh refresh if a session exists...            
            if (!_oSession.hasSession())
                return;
            Y.log("pUI.startAutoRefresh");
            _oRefresh.start();
        },
        stopAutoRefresh: function () {
            Y.log("pUI.stopAutoRefresh");
            _oRefresh.stop();
        },
        //Refresh now  **********************************************************************************************

        refreshManually: function () {
            Y.log("pUI.refreshManually");
            _oSession.refreshCurTestees();
            Y.pUI.refresh();
        },
        //auto refresh
        refresh: function () {
            Y.log("pUI.refresh");
            _oShared.startPleaseWait();
            //_oSession.refreshCurTestees();
            _oSession.init(2);
        },
        //refresh all data
        refreshAll: function () {
            Y.log("pUI.refreshAll");
            _oShared.startPleaseWait();
            var msgDialog = _oShared.msgDialog(null, null);
            msgDialog.cancel();
            _oSession.init(0);
        },


        //instruction **********************************************************************************************
        showHideInst: function () {
            Y.log("pUI.showHideInst");
            var elem = _oElem.lblInstruction;
            if (elem.hasClass("closed"))
                elem.removeClass("closed"); //open
            else
                elem.addClass("closed"); //close
        },

        //cls selectbox ddlUserSites **********************************************************************************************
        userSitesChange: function () {
            Y.log('userSitesChange');
            var msgDialog = _oShared.msgDialog(Y.tds.messages.getRaw("Important!"), _oMessages.get("ConfirmExitWithCLS"));
            msgDialog.addButton("confirm", Y.tds.messages.getRaw("Button.Exit"), Y.pUI.userSitesChangeOK);
            msgDialog.addButton("close", Y.tds.messages.getRaw("Button.Return"), Y.pUI.userSitesChangeCancel);
            msgDialog.show(_oShared.NotificationType.locked);
        },

        userSitesChangeOK: function () {
            var listBox = Y.one('#ddlUserSites');
            var aryValues = _oShared.getListBoxValues(listBox);
            if (aryValues != null && aryValues.length > 0) {
                setAClick();
                window.location = aryValues[0];
                var url = gLogOutPage; //pause the session and logout
                setTimeout(window.open(url), 30000);
                //window.open(url);
            }
            var msgDialog = _oShared.msgDialog(null, null);
            msgDialog.cancel();
        },

        userSitesChangeCancel: function () {
            var listBox = Y.one('#ddlUserSites');
            var options = listBox.get('options');
            if (options != null && options.size() > 0)
                options.item(0).set('selected', true);
            var msgDialog = _oShared.msgDialog(null, null);
            msgDialog.cancel();
        },

        //help/logout/alerts **********************************************************************************************
        removeActiveDialogs: function () {
            _oShared.removeBodyClass(_oClassName.alerts_window);
            _oShared.removeBodyClass(_oClassName.help_window);
            _oShared.removeBodyClass(_oClassName.lookup_window);
            _oShared.removeBodyClass(_oClassName.print_window);
        },
        help: function () {
            Y.log("pUI.help");
            if (_appConfig == null) return;
            var url = "Projects/" + _appConfig.ClientPath + "/help/help.html";
            Y.pUI.removeActiveDialogs();
            _oShared.addBodyClass(_oClassName.help_window);
            _oCurIframe = Y.one('#iframe_help');
            _oCurIframe.set('src', url);
            Y.pUI.stopAutoRefresh();
        },

        logout: function () {
            Y.log("pUI.logout");
            if (_appConfig.IsCLSLogin) {
                //add css class from body
                var msgDialog = _oShared.msgDialog(Y.tds.messages.getRaw("Important!"), _oMessages.get('LogoutAlertText'));
                msgDialog.addButton("close", Y.tds.messages.getRaw("Button.Logout"), Y.pUI.alertLogoutOK);
                msgDialog.addButton("close", Y.tds.messages.getRaw("Button.Cancel"), msgDialog.cancel);
                msgDialog.show(_oShared.NotificationType.error);
            }
            else {
                Y.pUI.alertLogoutOK();
            }
        },
        logoutBrowserSessionOnly: function () {
            Y.log("pUI.logoutBrowserSessionOnly");
            setAClick();
            window.location = gLogOutPage + "&logoutBrowserSessionOnly=true"; //logout only this browser session, do nothing to the test session
        },
        alertLogoutOK: function () {
            Y.log("pUI.alertLogoutOK");
            setAClick();
            window.location = gLogOutPageExl; //this variable is in Exit_Common js file
        },

        alerts: function () {
            Y.log("pUI.alerts");
            //add a class to a body
            //stop auto refresh
            Y.pUI.removeActiveDialogs();
            _oShared.addBodyClass(_oClassName.alerts_window);
            _oCurIframe = Y.Node.one("#iframe_alerts");
            _oCurIframe.set('src', "alerts.xhtml");

            Y.pUI.stopAutoRefresh();
        },

        //close modal dialog
        closeDialog: function (e, className) {
            Y.log("pUI.closeDialog");
            //remove the css class and then start auto refresh        
            var pUI = Y.pUI;
            _oShared.removeBodyClass(className);
            _oSession.refreshCurTestees();
            pUI.refresh(); //do a refresh right away
            pUI.startAutoRefresh();

            if (_oCurIframe != null)
                _oCurIframe.set('src', 'shared/blank.html');
            e.halt(true);
        },
        //call on user perform an action client/server site
        onUserAction: function () {

        },

        //settings and widgets **********************************************************************************************
        toggleSettings: function () {
            Y.log("pUI.toggleSettings");

            var pUI = Y.pUI;
            pUI.collapse();
            /*
            var bHasSettingsShow = _oShared.hasBodyClass(_oClassName.settingsShow);

            if (bHasSettingsShow) {
            _oShared.removeBodyClass(_oClassName.settingsShow);
            }
            else
            _oShared.addBodyClass(_oClassName.settingsShow);
            */
        },

        collapse: function () {
            var bnocollapse = _oShared.hasBodyClass(_oClassName.nocollapse);

            if (bnocollapse) {
                _oShared.removeBodyClass(_oClassName.nocollapse);
            }
            else
                _oShared.addBodyClass(_oClassName.nocollapse);
        },
        selectAllTests: function () {
            Y.log("pUI.selectAllTests");
            if (_oSession.key() != null) { //existing session
                //select all active tests and then call insert session tests
                var arySelTests = _oTests.getAllActiveTests();
                if (arySelTests != null && arySelTests.length > 0)
                    _oSession.insertTests(arySelTests); //_oSession.insertTests(arySelTests.join('|'));
            } else {
                //select all tests.
                _oTests.selectAllActiveTests();
                _oTests.onTestsSelChange();
            }
        },

        //for load test only **************************** **********************************************
        //select all active tests that contains certain string
        //str is null: this is for all active tests
        selectAllTestsPartial: function (str) {
            Y.log("P.UI.selectAllTestsPartial");
            if (_oSession.key() != null) { //existing session
                //select all active tests and then call insert session tests
                var arySelTests = _oTests.getAllActiveTestsPartial(str);
                if (arySelTests != null && arySelTests.length > 0)
                    _oSession.insertTests(arySelTests); // _oSession.insertTests(arySelTests.join('|'));
            } else {
                //select all tests.
                _oTests.selectAllActiveTestsPartial(str);
                _oTests.onTestsSelChange();
            }
        },



        radioSortTests: function (thisObj) {
            Y.log("P.UI.radioSortTests");
            if (thisObj == null || thisObj.value == null)
                return;
            //sort the P.Tests object and then re-render the menu.
            if (_oTests.sortBy != null && _oTests.sortBy == thisObj.value) {//already sorted
                return;
            }
            _oTests.sort(thisObj.value);
            _oTests.render(true);
        },

        //Lookup students **********************************************************************************************
        lookup: function () {
            Y.log("P.UI.lookup");
            //add a class to a body
            //stop auto refresh
            var pUI = Y.pUI;
            pUI.removeActiveDialogs();
            _oShared.addBodyClass(_oClassName.lookup_window);
            _oCurIframe = Y.Node.one("#iframe_lookup");
            _oCurIframe.set('src', "lookup.xhtml");
            pUI.stopAutoRefresh();
        },

        //print request**********************************************************************************************
        //--add css class to body
        //--set the iframe src
        request: function (e, testOpp) {
            Y.log("pUI.request");
            //add a class to a body
            //stop auto refresh
            var pUI = Y.pUI;
            _oShared.addBodyClass(_oClassName.print_window);

            var divPrintWrapper = Y.Node.one("#divPrintWrapper");
            divPrintWrapper.setContent('');
            _oCurIframe = _oShared.iframeNode();
            _oCurIframe.set('id', "iframe_requests");
            divPrintWrapper.append(_oCurIframe);
            var qStr = "oppKey=" + testOpp.oppKey + "&sessionKey=" + testOpp.sessionKey +
                    "&ssid=" + testOpp.ssid + "&name=" + testOpp.name;
            var url = "requests.xhtml?" + escape(qStr);
            Y.log(url);
            _oCurIframe.set('src', url);
            _oTestOpp = testOpp;
            pUI.stopAutoRefresh();
        },

        approvedRequests: function () {
            Y.log("pUI.approvedRequests");
            //add a class to a body
            //stop auto refresh
            _oShared.addBodyClass(_oClassName.print_window);
            var divPrintWrapper = Y.Node.one("#divPrintWrapper");
            divPrintWrapper.setContent('');
            _oCurIframe = _oShared.iframeNode();
            _oCurIframe.set('id', "iframe_requests");
            divPrintWrapper.append(_oCurIframe);

            var url = "approvedrequests.xhtml";

            _oCurIframe.set('src', url);

            Y.pUI.stopAutoRefresh();
        },

        //Print  **********************************************************************************************
        print: function () {
            Y.log("P.UI.print");
            window.print();
        },

        //Popup check  **********************************************************************************************
        popupCheck: function () {
            Y.log("P.UI.popupCheck");
            var cookieName = "popUpCheck";
            var value = _oCookie.getCookie(cookieName);
            if (value == null || value.length < 1) {
                var popUnWin = window.open('../shared/popup.html', '', 'width=1,height=1');
                if (popUnWin) {
                    popUnWin.close();
                    _oCookie.setValue(cookieName, 1);
                    return;
                }
                //pop up alert
                var msgDialog = _oShared.msgDialog(Y.tds.messages.getRaw("Important!"), _oMessages.get("PopupInstructions"));
                msgDialog.show(_oShared.NotificationType.warning);
            }
        },


        //Testing  **********************************************************************************************
        test1: function () {
            alert('this is a test');
        },
        testEvent: function (e, testparam) {
            alert(testparam);

        },
        test: function () {
            var appConfig = eval({ "IsCLSLogin": false, "Local_Domains": "testadmin|localhost:3300|localhost\/proctor", "PingInterval": 300000, "RefreshValue": 30000, "Timeout": 1200000, "UserName": "special char%$!", "WaitForPrinter": 60, "ClientPath": "Hawaii" });
            this.init(appConfig);
            _oElem.test();
            _oClassName.test();

        }

    };
}, "0.1", { requires: ["overlay", "node", "event", "p-ClassName", "p-Elem", "tds-tests", "tds-refresh",
                        "tds-timeout", "tds-hideshow", "tds-sort", "tds-ping", "tds-testopps",
                        "tds-accTypes", "tds-session", "json-stringify", "querystring-parse"]
});
