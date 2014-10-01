//required: yui-min.js
YUI.add("tds-timeout", function(Y) {
    //-------------------------------------------------------------------------
    // Private variables
    //-------------------------------------------------------------------------
    var _configTimeout = 1200000, //config timeout in the web config file, 20mins = 20x60x1000 milliseconds
        _serverCheckURL = "Services/XHR.axd/ServerActivity",  //XHR url to call to check the server for activity
        _hasServerActivity = true,
        _starttime = 0, //last activity time, in milliseconds
        _activityCheckInterval = 1200000, //20mins = 20x60x1000 milliseconds , check for user's activity
        _intervalId = 0,
        _requiredServerCheck = true, //true if required to ping the server
        _logoutURL = 'saml/logout'; //page to call when inactivity period=configTimeout

    //-------------------------------------------------------------------------
    // Private functions
    //-------------------------------------------------------------------------    
    function _reset() {
        _checkServerActivity();
    }

    function resetInterval() {
        clearTimeout(_intervalId);
        _intervalId = setTimeout(_reset, _activityCheckInterval);
    }

    function _now() {
        return (new Date()).getTime();
    }

    function _registerActivity() {
        _starttime = _now();
        resetInterval();
    }

    function _checkServerActivity() {
        if (!_requiredServerCheck) //if server check is not needed ...
        {//logout user            
            _logout(); return;
        }
        //XHR ping to server to check for activity 
    	//A function handler to use for successful requests:
        var handleSuccess = function(ioId, o) {
            if (o.responseText != undefined) {
                var returnedStatus = Y.JSON.parse(o.responseText);
                if (returnedStatus.status != undefined && returnedStatus.status == "True") {
                    Y.log("hasServerActivity=true");
                    _hasServerActivity = true;
                    //reset the timeout allowance
                    _registerActivity();
                }
                else {
                    Y.log("hasServerActivity=false --> logout()");
                    _hasServerActivity = false;
                    //logout user
                    _logout();
                }
            }
        };
        var handleFailure = function(ioId, o) {
             Y.log("failure()");
            _hasServerActivity = true; //This would never happened but just in case
            //reset the timeout allowance
            _registerActivity();
        };
       
        // Configuration object for POST transaction 
        var cfg = {
            method: "POST",
            data: "i=i", //for FF3 and below and IIS7 issue with 0 content length
        	timeout: gXHRTimeout, // Abort the transaction, if it is still pending, after X ms.
            headers: { "cache-control": "no-cache", 'X-Transaction': 'checkServerActivity' },
            on: {
                success: handleSuccess,
                failure: handleFailure
            }
        };

        Y.io(_serverCheckURL, cfg);
    }

    function _logout() {
        //clicked = true;  //Need to fix
        //window.open(that.logoutURL);
        window.location = _logoutURL;
    }

    function _testDisplay() {
        setTimeout(_testDisplay, 2000);
        Y.log("Server config - configTimeout: " + _configTimeout / (1000) + " seconds | " +
                        "Your Idle time - idleTime: " + (_now() - _starttime) / (1000) + " seconds");
    }

    //-------------------------------------------------------------------------
    // Public interface
    //-------------------------------------------------------------------------
    Y.tdsTimeout = {
        init: function(configTimeout, logoutURL, requiredServerCheck, serverCheckURL, activityCheckInterval) {
            if(configTimeout != null)
                _configTimeout = configTimeout;
            if (logoutURL != null)//if null used default
                _logoutURL = logoutURL;
            _requiredServerCheck = requiredServerCheck;
            if (serverCheckURL != null)
                _serverCheckURL = serverCheckURL;
            //if not required server check then the activity check time will be equal to the config timeout          
            if (!requiredServerCheck || activityCheckInterval == null)
                _activityCheckInterval = _configTimeout;
            else
                _activityCheckInterval = activityCheckInterval;

            Y.on('keydown', _registerActivity, document);
            Y.on('mousedown', _registerActivity, document);
            _registerActivity();
            //_testDisplay();
        },
        registerActivity: function() {
            _registerActivity();
        }


    };
}, "0.1", { requires: ["node", "io", "json-parse"] });
