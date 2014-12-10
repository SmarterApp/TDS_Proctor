//NOTE: TDS testee requests
//required: yui-min.js
YUI.add("tds-request", function(Y) {
    //-------------------------------------------------------------------------
    // Private variables
    //-------------------------------------------------------------------------
    var _data;
    var _browserAction; //browserAction enum value.  0 - Allow, 1 - Deny, 2 - Warn
    var _browserWhitelistContext = "PRINTING";
    var _dto = null; //shared data between parent and iframe
    var _oShared = Y.tdsShared;
    var _oClassName = Y.pClassName;


    //-------------------------------------------------------------------------
    // Private functions
    //-------------------------------------------------------------------------
    function _init() {        
        Y.tdsShared.addRegisterActivity();
        var elem = Y.one('#requestClose');
        elem.detach();
        elem.on('click', _close);
        var str = document.location.href.split('?')[1];  
        str = unescape(str); 
        var qStr = Y.QueryString.parse(str);        
        //qStr.name = Y.QueryString.unescape(qStr.name);
        if (qStr.oppKey != null)
            _load(qStr);
    }

    function _load(qStr) {
        //XHR ping to server
        //A function handler to use for successful requests:
        var handleSuccess = function(ioId, o) {
            if (o.responseText != undefined) {
                var testeeRequestsDTO = Y.JSON.parse(o.responseText);
                if (testeeRequestsDTO != undefined) {
                    _parse(testeeRequestsDTO);
                    if (_data == null || _data.length < 1)
                        _close(); //no more requests
                    _render(null);
                }
            }
        };
        var handleFailure = function(ioId, o) {
            _showMsg(_errorMsg());
        };
        if (qStr.oppKey == undefined || qStr.oppKey == null) {
            _showMsg(Y.Messages.get('UnableToProcessRequest'));
            return;
        }

        _loadInit(qStr);
        var postData = "oppKey=" + qStr.oppKey + "&sessionKey=" + qStr.sessionKey + "&environment=" + gTDS.appConfig.Environment + "&context=" + _browserWhitelistContext;
        /* Configuration object for POST transaction */
        var cfg = {
            method: "POST",
            data: postData,
        	timeout: gXHRTimeout, // Abort the transaction, if it is still pending, after X ms.
            headers: { "cache-control": "no-cache", 'X-Transaction': 'Get testeee requests init data' },
            on: {
                success: handleSuccess,
                failure: handleFailure
            }
        };
        Y.io("Services/XHR.axd/GetCurrentRequests", cfg);
    }
    function _loadInit(qStr) {
        Y.one('#lblname').set('innerHTML', qStr.name);
        Y.one('#lblssid').set('innerHTML', qStr.ssid);
    }
    function _errorMsg() {
        _oShared.showError(Y.Messages.get('UnableToProcessRequest'));
    }

    function _showMsg(msg) {
        var elem = Y.one('#spanInfoMessage');
        if (elem == null)
            return;
        if (msg == undefined || msg.length < 1) {
            elem.hide();
        } else {
            elem.setContent(msg);
            elem.show();
        }
    }

    function _parse(testeeRequestDTO) {
        if (testeeRequestDTO == undefined)
            return;
        if (testeeRequestDTO.bReplaceRequests) {
            _data = testeeRequestDTO.requests;
        }
        if (testeeRequestDTO.browserAction) {
            _browserAction = testeeRequestDTO.browserAction;
        }
    }

    function _close() {        
        window.parent.fireGlobalEvent('GlobalEvent:closeDialog', Y.pClassName.print_window);
    }

    function _render(e) {
        Y.log("_render");
        if (!_hasActive()) {
            _close(e); // close the iframe if no active requests.
            return;
        }

        //display print browser whitelist warn/deny messages if appropriate
        if (_browserAction == 1) {
            Y.one('.print_whitelist_deny').show();
        } else if (_browserAction == 2) {
            Y.one('.print_whitelist_warn').show();
        }

        //remove all children tables
        var tblElem = Y.one('#tblRequests');
        var tbodies = tblElem.getElementsByTagName('tbody');
        var tbody = null;
        if (tbodies == null || tbodies.size() < 1)
            tbody = tblElem.append('<tbody></tbody>');
        else
            var tbody = tbodies.item(0);
        tbody.setContent(''); //clear out exist table first
        var len = _data.length;
        for (var i = 0; i < len; i++) {
            if (!_data[i].disabled)
                tbody.append(_buildRow(_data[i]));
        }
    }
    //Approve btn|Deny btn|New Requests|Date & Time of Request
    function _buildRow(testeeRequest) {
        var tr = _oShared.trNode(null, null);

        var td = _oShared.tdNode(null, null);
        var a;
        
        // Display if browserAction is not 1 (BrowserAction.DENY)
        a = _oShared.aNode('Approve', 'approve');
        if (_browserAction !== 1) {
            a.on('click', _approve, null, testeeRequest);
        } else {
            a.on('click', _printDeny, null, testeeRequest);
        }
        td.append(a);
        tr.append(td);
        tr.appendChild(td);

        td = _oShared.tdNode(null, null);
        a = _oShared.aNode('Deny', 'deny');
        a.on('click', _deny, null, testeeRequest);
        td.append(a);
        tr.append(td);
        tr.appendChild(td);

        td = _oShared.tdNode(_getLabel(testeeRequest), null);
        tr.append(td);

        td = _oShared.tdNode(testeeRequest.StrDateSubmitted, null);
        tr.append(td);
        return tr;
    }

    function _getLabel(testeeRequest) {
        if (testeeRequest.RequestDesc != null && testeeRequest.RequestDesc.length > 0)
            return testeeRequest.RequestDesc;
        var requestType = (testeeRequest.RequestType == 'PRINTITEM') ? "Print Item - " : "Print Passage - ";
        var firstIdx = testeeRequest.RequestValue.lastIndexOf('\\');
        var secondIdx = testeeRequest.RequestValue.lastIndexOf('.');
        if (firstIdx >= secondIdx)
            return requestType;

        return requestType + testeeRequest.RequestValue.substring(firstIdx + 1, secondIdx);
    }

    function _hasActive() {
        if (_data == undefined)
            return false;
        for (var i = 0; i < _data.length; i++) {
            if (!_data[i].disabled)
                return true;
        }
        return false;
    }
    function _approve(e, testeeRequest) {
        testeeRequest.disabled = true;
        var page = "PrintRequest.xhtml";
        var requestType = testeeRequest.RequestType;
        if (requestType.indexOf("EMBOSS") != -1)
            page = "EmbossRequest.xhtml";
        window.open(page + "?requestKey=" + testeeRequest.Key, '_blank');
        _render(e);
    }

    function _deny(e, testeeRequest) {
        //add a class to the body, remove old listeners if any, add new listener
        _oShared.addBodyClass('show_denial');
        var elem = Y.one('#btnDenialOK');
        elem.detach();
        elem.on('click', _denialOK, null, testeeRequest);
        elem = Y.one('#denialText');
        elem.set('value', '');
        if (elem != undefined && !Y.tdsShared.mobile()) elem.focus();
    }

    function _denialOK(e, testeeRequest) {
        //XHR ping to server
        //A function handler to use for successful requests:
        var handleSuccess = function(ioId, o) {
            _oShared.removeBodyClass('show_denial');
            _render(e);
        };
        var handleFailure = function(ioId, o) {
            _oShared.removeBodyClass('show_denial');
            _render(e);
        };

        var postData = "requestKey=" + testeeRequest.Key + "&reason=" + escape(_getDenialReason());
        /* Configuration object for POST transaction */
        var cfg = {
            method: "POST",
            data: postData,
        	timeout: gXHRTimeout, // Abort the transaction, if it is still pending, after X ms.
            headers: { "cache-control": "no-cache", 'X-Transaction': 'Get testeee requests init data' },
            on: {
                success: handleSuccess,
                failure: handleFailure
            }
        };
        testeeRequest.disabled = true;
        Y.io("Services/XHR.axd/DenyTesteeRequest", cfg);
    }

    function _printDeny(e, testeeRequest) {
        //add a class to the body, remove old listeners if any, add new listener
        _oShared.addBodyClass('show_printDenial');
        var elem = Y.one('#btnPrintDenialOK');
        elem.detach();
        elem.on('click', function () {
            _oShared.removeBodyClass('show_printDenial');
        });
        if (elem != undefined && !Y.tdsShared.mobile()) elem.focus();
    }

    function _getDenialReason() {
        var elem = Y.one('#denialText');
        if (elem == undefined)
            return '';
        return elem.get('value');
    }

    function _testDisplay() {

    }


    //-------------------------------------------------------------------------
    // Public interface
    //-------------------------------------------------------------------------
    Y.tdsRequest = {

        init: function() {
            _init();
        }

    };
}, "0.1", { requires: ["io", "json-parse", "querystring-parse", "tds-shared"] });

