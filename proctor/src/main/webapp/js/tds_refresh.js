//NOTE: refresh the active session page every x seconds
//required: yui-min.js
YUI.add("tds-refresh", function(Y) {
    //-------------------------------------------------------------------------
    // Private variables
    //-------------------------------------------------------------------------
    var _refreshCallback = "", //function name to be called on refresh
        _refreshInterval = 30000, //refresh interval in milliseconds: 30secs: 30x1000=3000milliseconds
        _intervalId = 0, //Id for setTimeout function
        _refreshCount = 0, //number of times refresh functions called
        _refreshCookie = "refreshTDS";

    //-------------------------------------------------------------------------
    // Private functions
    //-------------------------------------------------------------------------    
    function _refresh() {
        _refreshTimeOut();
        _refreshCount++;
        _refreshCallback();
        //_testDisplay();
    }

    function _refreshTimeOut() {
        clearTimeout(_intervalId);
        _intervalId = setTimeout(_refresh, _refreshInterval);
    }

    function setInterval(newInterval) {//set new refresh interval        
        _refreshInterval = newInterval;
    }

    function _saveRefreshCookie(e, ddl) {
        var value = ddl.options[ddl.selectedIndex].value;
        if (value == null || value.length < 1) {//set to default
            Y.tdsCookie.setCookie(_refreshCookie, _refreshInterval, 1);
            return;
        }
        Y.tdsCookie.setCookie(_refreshCookie, value, 1);
        setInterval(parseInt(value));
        _refreshTimeOut();
    }

    function _readRefreshCookie() {
        var value = Y.tdsCookie.getCookie(_refreshCookie);
        if (value == null || value.length < 1)
            return null;
        setInterval(parseInt(value));
        return value;
    }

    function _testDisplay() { // for testing        
        var str = "<br/>refreshFuncCall: <b>" + _refreshCallback + "</b>" +
                        "<br/>refreshInterval: <b>" + _refreshInterval + "</b>" +
                        "<br/>intervalId: <b>" + _intervalId + "</b>" +
                        "<br/>refreshCount: <b>" + _refreshCount + "</b>";
        Y.log(str);
    }

    //-------------------------------------------------------------------------
    // Public interface
    //-------------------------------------------------------------------------
    Y.tdsRefresh = {
        stop: function() {
            clearTimeout(_intervalId);
        },
        start: function() {
            Y.log("Y.tdsRefresh.start")
            _refreshTimeOut();
        },
        init: function(refreshInterval, refreshCallback) {
            Y.log("Y.tdsRefresh.init");
            _refreshInterval = refreshInterval;
            _refreshCallback = refreshCallback;
        }

    };
}, "0.1", { requires: ["tds-cookie"] });


