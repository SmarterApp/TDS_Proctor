//NOTE: ping the server every x mins
//required: yui-min.js
YUI.add("tds-ping", function(Y) {
    //-------------------------------------------------------------------------
    // Private variables
    //-------------------------------------------------------------------------
    var _sessionKey = null,
         _xhrTimeout = 30000,
        _interval = 180000, //x mins
        _url = "Services/XHR.axd/ProctorPing",  //XHR url to call the server for the ping   
        _intervalId = 0,
        _starttime = 0;
    var _failedCount = 0;
    var _maxFailedCount = 50;
    //-------------------------------------------------------------------------
    // Private functions
    //-------------------------------------------------------------------------    
    function _now() {
        return (new Date()).getTime();
    }
    function _getInterval() {
        return _interval;
    }

    function _ping() {
        //XHR ping to server
        //A function handler to use for successful requests:
        var handleSuccess = function(ioId, o) {
            if (o.responseText != undefined) {
                var returnedStatus = Y.JSON.parse(o.responseText);
                if (returnedStatus.status != undefined && returnedStatus.status == "True") {
                    Y.log("Ping successfully - restart the ping");
                    //restart the ping
                    _start();
                    _failedCount = 0;
                }
                else {
                    Y.log("Ping failed - restart the ping");
                    Y.tdsShared.renderError(returnedStatus);    //render the error if any               
                    _failedCount++;
                    if(_failedCount<_maxFailedCount) //dont do anymore if failed many time.
                        _start();
                }
            }
            else {
            	_start(); //start again if failed for some reason
            }
        };
        var handleFailure = function(ioId, o) {
            Y.log("Ping failed - restart the ping -- failure()");
            _start();
        };

        /* Configuration object for POST transaction */
        var cfg = {
            method: "POST",
            data: "sessionKey=" + _sessionKey,
        	timeout: gXHRTimeout, // Abort the transaction, if it is still pending, after X ms.
            headers: { "cache-control": "no-cache", 'X-Transaction': 'Get Session Init data' },
            on: {
                success: handleSuccess,
                failure: handleFailure
            }
        };

        Y.log("ServerActivity sessionKey=" + _sessionKey);
        Y.io(_url, cfg);
    }

    function _start() {
        if (_sessionKey == null)
            return;
        clearTimeout(_intervalId);
        _starttime = _now();
        _intervalId = setTimeout(_ping, _interval);
    }

    function _init(interval, sessionKey) {
        _interval = interval;
        _sessionKey = sessionKey;
        _start();
        // _testDisplay();
    }

    function _testDisplay() {
        setTimeout(_testDisplay, 1000);

        Y.log("Config interval: " + _getInterval() / (1000) + " seconds | " +
                        "Your Idle time - idleTime: " + (_now() - _starttime) / (1000) + " seconds");
    }


    //-------------------------------------------------------------------------
    // Public interface
    //-------------------------------------------------------------------------
    Y.tdsPing = {
        start: function() {
            _start();
        },

        stop: function() {
            clearTimeout(_intervalId);
        },

        init: function(interval, sessionKey) {
            _init(interval, sessionKey);
        }

    };
}, "0.1", { requires: ["io", "json-parse"] });

