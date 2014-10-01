//required: yui-min.js
YUI.add("tds-ackalerts", function(Y) {
    //-------------------------------------------------------------------------
    // Private variables
    //-------------------------------------------------------------------------
    var _data = null,
        _elem = {
            btnDone: null,
            divAlertMessages: null
        };
    var _oShared = Y.tdsShared;
    var _oClassName = Y.pClassName;

    //-------------------------------------------------------------------------
    // Private functions
    //-------------------------------------------------------------------------
    //hit the server and bring down a list of message
    function _load() {
        //A function handler to use for successful requests:
        var handleSuccess = function(ioId, o) {
            if (o.responseText != undefined) {
                try {
                    var alertMessages = Y.JSON.parse(o.responseText);
                    if (alertMessages.status != undefined && alertMessages.status == 'failed') {//failed
                        _oShared.showError(alertMessages.reason);
                    }
                    else {
                        _data = alertMessages;
                        _renderCurMsgs();
                    }
                }
                catch (e) {
                    Y.log("ERROR: AlertMessages.load: " + e);
                    return false;
                }
            }
        };
        var handleFailure = function(ioId, o) {
            Y.log("ERROR: AlertMessages.load: " + x);
        };
        /* Configuration object for POST transaction */
        var cfg = {
            method: "POST",
            data: "i=i", //for FF3 and below and IIS7 issue with 0 content length
        	timeout: gXHRTimeout, // Abort the transaction, if it is still pending, after X ms.
            headers: { "cache-control": "no-cache", 'X-Transaction': 'Get Alert Messages data' },
            on: {
                success: handleSuccess,
                failure: handleFailure
            }
        };

        Y.io("Services/XHR.axd/GetCurrentAlertMessages", cfg);
    }

    //call from parent page
    function _doneCurMsgs(e) {
        //e.halt(true);
        //fire a global event   
        window.parent.fireGlobalEvent('GlobalEvent:closeDialog', _oClassName.alerts_window);
    }

    //render current alert message(s)
    function _renderCurMsgs() {
        Y.log("AlertMessages.renderCurMsgs");
        //clean out the div contents
        var contElem = _elem.divAlertMessages;
        contElem.setContent(''); //clean out
        var alertMessages = _data;
        var len = alertMessages.length;
        for (var i = 0; i < len; i++) {
            var msg = alertMessages[i];
            var divMsg = Y.Node.create("<div class='listmessage'></div>");
            var h3 = Y.Node.create("<h3></h3")
            var str = "<span class='date'>" + msg.DateStarted + "</span> at <span class='time'>" +
                msg.TimeStarted + "</span> (" + Y.Messages.getRaw("TimeZone") + ")";
            h3.setContent(str)
            divMsg.append(h3);
            var h4 = Y.Node.create("<h4></h4>"); h4.setContent(msg.Title); divMsg.append(h4);
            var p = Y.Node.create("<p></p>"); p.setContent(msg.Message); divMsg.append(p);
            contElem.append(divMsg);
        }
        
        _oShared.activateScollView(Y.one("#divBottomHalf"));        
    }

    function _hasAlerts() {
        if (_data == undefined || _data.length < 1)
            return false;
        return true;
    }

    function _initCurMsgs() {
        //alert("Init");
        _elem.btnDone = Y.one("#btnDone");
        Y.on('click', _doneCurMsgs, _elem.btnDone);
        _elem.divAlertMessages = Y.one("#divAlertMessages");
        _load();
        //_registerEvents();
        _oShared.removeBodyClass("loading");
        _oShared.addRegisterActivity();
        
    }


    //-------------------------------------------------------------------------
    // Public interface
    //-------------------------------------------------------------------------
    //Acknowledge alert messages
    Y.AckAlertMessages = {
        init: function() {
            _initCurMsgs();
        },
        doneCurMsgs: function(e) {
            _doneCurMsgs(e);
        }
    };

}, "0.1", { requires: ["tds-shared", "p-ClassName", "event", "event-custom", "node", "io", "json-parse"] });