//required: yui-min.js
YUI.add("tds-alerts", function(Y) {
    //-------------------------------------------------------------------------
    // Private variables
    //-------------------------------------------------------------------------
    var _data = null,
        _elem = {
            btnDone: null,
            divAlertMessages: null,
            divAlerts: null,
            btnAlertsOK: null
        };
    var _oShared = Y.tdsShared;
    var _oClassName = Y.pClassName;
    var _oUI;

    //-------------------------------------------------------------------------
    // Private functions
    //-------------------------------------------------------------------------    
    function _hasAlerts() {
        if (_data == undefined || _data.length < 1)
            return false;
        return true;
    }
    //Unacknowledged alert messages **********************************************************************
    //call from parent page
    function _start() {
        Y.log("AlertMessages.start");
        if (!_hasAlerts()) return;

        //set a css class to the body        
        _oShared.addBodyClass(_oClassName.show_alert);
        _oUI.stopAutoRefresh();
        _render();
    }

    function _render() {
        Y.log("AlertMessages.render");
        //clean out the div contents
        if (_elem.divAlerts == undefined)
            _elem.divAlerts = Y.one("#divAlerts");

        var contElem = _elem.divAlerts;
        contElem.setContent(''); //clean out

        if (_elem.btnAlertsOK == undefined)
            _elem.btnAlertsOK = Y.one("#btnAlertsOK");
        _elem.btnAlertsOK.on('click', _done);

        var alertMessages = _data;
        var len = alertMessages.length;
        var yNode = Y.Node;
        for (var i = 0; i < len; i++) {
            var alertMsg = alertMessages[i];
            var divMsg = yNode.create('<div class="listmessage"></div>');
            var h3 = yNode.create('<h3></h3>');
            var str = "<span class='date'>" + alertMsg.DateStarted + "</span> at <span class='time'>" +
                alertMsg.TimeStarted + "</span> (" + Y.Messages.getRaw("TimeZone") + ")";
            h3.setContent(str);
            divMsg.append(h3);
            var h4 = yNode.create("<h4></h4>"); h4.setContent(alertMsg.Title);
            divMsg.append(h4);
            var p = yNode.create("<p></p>"); p.setContent(alertMsg.Message);
            divMsg.append(p);
            contElem.append(divMsg);
        }
    }
    function _done(e) {
        Y.log("AlertMessages.done");
        //e.halt(true);
        //remove the css class and then start auto refresh
        _oShared.removeBodyClass(_oClassName.show_alert);
        _oUI.refresh(); //do a refresh right away
        _oUI.startAutoRefresh();
        e.halt(true);
    }
    function _initUnackMsgs() {
        _elem.divAlerts = Y.one("#divAlerts");
        _elem.btnAlertsOK = Y.one("#btnAlertsOK");
    }


    //-------------------------------------------------------------------------
    // Public interface
    //-------------------------------------------------------------------------
    Y.AlertMessages = {
        init: function(alertType) {
            _initUnackMsgs();
        },
        setData: function(data) {
            _data = data;
        },
        start: function(oUI) {
            _oUI = oUI;
            _start();
        }
    };

}, "0.1", { requires: ["p-ClassName", "tds-shared", "event", "event-custom", "node"] });