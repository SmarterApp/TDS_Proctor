YUI.add("tds", function (Y) {

    //Object to wrap Proctor's TDS object
    if (typeof P == 'undefined') { P = {}; }
    //entry point for the application
    P.TDS = function () {

    };
    P.TDS.messages = null;
    P.TDS.messageloader = null;
    P.TDS.gTDS = null;
    P.TDS.pUI = null;
    P.TDS.lookup = null;
    P.TDS.alerts = null;
    P.TDS.accommodations = null;
    P.TDS.y = Y;

    //gTDS JSON that comes from the server
    P.TDS.initBase = function (gTDS) {
        //application initializing
        P.TDS.gTDS = gTDS;

        P.TDS.Config.load();

        if (P.TDS.messages) {
            MessageTemplate.processLanguage();
        }
        if(gTDS.globalAccs != undefined)
            P.TDS.initGlobalAccs(gTDS.globalAccs);
    }
    //login.xhtml page init
    P.TDS.initLogin = function (gTDS) {
        P.TDS.initBase(gTDS);
        var node = Y.one(document.body);
        node.removeClass("please_wait");
    }


    P.TDS.initDefault = function (gTDS) {
        P.TDS.initBase(gTDS);
        P.TDS.pUI = Y.pUI;
        P.TDS.pUI.init(gTDS.appConfig);
    }

    //lookup.xhtml page init
    P.TDS.initLookup = function (gTDS) {
        P.TDS.initBase(gTDS);
        P.TDS.lookup = Y.tdsLookup;
        P.TDS.lookup.init(gTDS.appConfig);
    }
    //alerts.xhtml page init
    P.TDS.initAlerts = function (gTDS) {
        P.TDS.initBase(gTDS);
        P.TDS.alerts = Y.AckAlertMessages;
        P.TDS.alerts.init();
    }

    //requests.xhtml page init
    P.TDS.initRequests = function (gTDS) {
        P.TDS.initBase(gTDS);
        P.TDS.requests = Y.tdsRequest;
        P.TDS.requests.init();
    }

    //global accommodations
    P.TDS.initGlobalAccs = function () {
        if (gTDS.globalAccs == undefined) return;

        P.TDS.accommodations = new Accommodations();

        P.TDS.accommodations.load(gTDS.globalAccs);

        var messagesSwitcher = new MessageSwitcher(P.TDS.accommodations.getGlobalLanguageAccs(), P.TDS.getCurrentLanguage());
        messagesSwitcher.init();
    };

    P.TDS.getCurrentLanguage = function () {
        return gTDS.appConfig.Language;
    };
    P.TDS.setCurrentLanguage = function (curLang) {
        gTDS.appConfig.Language = curLang;
        //make an xhr call to save the language to cookie
        P.Util.xhrCall("Services/XHR.axd/SetCurrentLanguage", null, "selectedLanguage=" + curLang);
    };


    P.TDS.processLanguage = MessageTemplate.processLanguage;

    if (typeof (P.TDS.Config) == 'undefined') P.TDS.Config = {};

    P.TDS.Config.load = function () {
        //load messages
        P.TDS.Config._loadMessages();
    }

    //load new messages system
    P.TDS.Config._loadMessages = function () {
        if (typeof (P.TDS.gTDS) != 'object') return;

        P.TDS.messageloader = new MessageLoader();
        if (P.TDS.gTDS.messages) P.TDS.messageloader.load(P.TDS.gTDS.messages);
        // save message system 
        P.TDS.messageloader.buildIndex();
        P.TDS.messages = P.TDS.messageloader.getMessageSystem();
    }

    //error messages
    Y.Messages = {
        //args is an array of substitute values = ['value for 0', 'value for 1']
        get: function (defaultMessage, args) {
            return P.TDS.messages.getMessage(defaultMessage, args);
        },
        getRaw: function (defaultMessage, args) {
            return P.TDS.messages.getRaw(defaultMessage, args);
        }


    };



    Y.tds = P.TDS;

}, "0.1", { requires: ["messages_loader", "messages_template", "tds-shared", "io", "json-parse"] });
