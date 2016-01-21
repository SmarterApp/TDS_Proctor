/*
    PrintRequest class which retrieves item/stim information (including accommodation info) and passes it to the Blackbox handler.
    This is the new preferred way of rendering items/stim in the Proctor application
*/
function PrintRequest(gTesteeRequest) {
    this._gTesteeRequest = gTesteeRequest;
};

//init method - this fills in the various labels on the page with the student/item/stim information
PrintRequest.prototype.init = function () {
    //collection of the configs that have been loaded
    PrintRequest.configs = {};

    var titleElem = P.TDS.y.one('title');
    if (this._gTesteeRequest === undefined) {
        titleElem.set('innerHTML', P.TDS.messages.getMessage('Invalid request or request key')); return;
    }
    var requestType = P.TDS.messages.getRaw(this._gTesteeRequest.RequestType);
    var title = requestType + ': ' + this._gTesteeRequest.TesteeName + ' | ' + P.TDS.messages.getRaw('SSID') + ': ' + this._gTesteeRequest.TesteeID;
    document.title = title;

    var lbldate = P.TDS.y.one('#lbldate');
    lbldate.set('innerHTML', this._gTesteeRequest.StrDatePrinted);

    var lblname1 = P.TDS.y.one('#lblname1');
    lblname1.set('innerHTML', requestType + ': ' + this._gTesteeRequest.TesteeName);
};

/*  Called from blackbox API when state becomes available - current state is 'Available' */
PrintRequest.prototype.blackboxAvailable = function () {
    console.log('PrintRequest.blackboxAvailable()');

    if (typeof Blackbox === 'undefined') {
        BlackboxWin = BlackboxLoader.getWin();
        BlackboxDoc = BlackBoxLoader.getDoc();
        Blackbox = BlackboxWin.Blackbox;
        Accommodations = BlackboxWin.Accommodations;
        Util = BlackboxWin.Util;
        ContentManager = BlackboxWin.ContentManager;
        YAHOO = BlackboxWin.YAHOO;
        YUI = BlackboxWin.YUI;
        zXPath = BlackboxWin.zXPath;
    } else {
        BlackboxWin = window;
        BlackboxDoc = document;
    }
};

/* Function is called when blackbox is completely ready and everything is initialized */
PrintRequest.prototype.blackboxReady = function () {
    console.log('PrintRequest.blackboxReady()');

    //clear the top nav bar
    var configPage = processTesteeRequest(this._gTesteeRequest);

    //display item/passage (configPage object)
    Blackbox.loadContent(configPage);

    //clear out the top navbar - not needed for print requests.
    YUI().use('node', 'tds-shared', 'tds', function (Y) {
        Y.tds.initBase(gTDS);

        //display the correct title
        var titleElem = P.TDS.y.one('title');
        if (gTesteeRequest === undefined) {
            titleElem.set('innerHTML', P.TDS.messages.getMessage('Invalid request or request key')); return;
        }
        var requestType = P.TDS.messages.getRaw(gTesteeRequest.RequestType);
        var title = requestType + ': ' + gTesteeRequest.TesteeName + ' | ' + P.TDS.messages.getRaw('SSID') + ': ' + gTesteeRequest.TesteeID;
        document.title = title;
        //set date
        var lbldate = P.TDS.y.one('#lbldate');
        lbldate.set('innerHTML', gTesteeRequest.StrDatePrinted);
        //set name1 label
        var lblname1 = P.TDS.y.one('#lblname1');
        lblname1.set('innerHTML', requestType + ': ' + gTesteeRequest.TesteeName);
    });

    //create a configPage object from the gTesteeRequest object to pass into blackbox
    function processTesteeRequest(testeeRequest) {
        var configPage = null;

        //confirm _gTesteeRequest is not null
        if ((testeeRequest !== null) && (testeeRequest !== 'undefined')) {

            configPage = createConfigPage(testeeRequest);
        }

        return configPage;
    }

    // Create a configPage object using values from testeeRequest.
    function createConfigPage(testeeRequest) {

        //create configPage object
        var configPage = {};
        configPage.encrypted = false;   //keep unencrypted for now.
        configPage.id = 'printRequest';
        configPage.client = this.gTDS.appConfig.ClientName;
        configPage.language = testeeRequest.Language;
        configPage.layoutFolder = '';   //not required
        configPage.layoutFile = '';     //not required
        configPage.passage = null;  //Object{file=''}
        configPage.items = new Array(); //[Object{file='', position=0, label=null}]
        switch (testeeRequest.RequestType) {
            case 'PRINTITEM':
                configPage.layoutName = '6'; //changed to layout 6
                configPage.items.push({ 'file': testeeRequest.RequestValue, 'position': testeeRequest.ItemPosition, 'response':testeeRequest.ItemResponse });
                break;
            case 'PRINTPASSAGE':
                configPage.layoutName = 'PassagePrint';
                configPage.passage = { 'file': testeeRequest.RequestValue };
                break;
        }
        //retrieve accommodations from testeeRequest.RequestParams
        var accommodationsJson = getAccommodations(testeeRequest.RequestParameters);
        //retrieve current accommodations manager
        var accommodations = Accommodations.Manager.getCurrent();
        //load the accommodations to the manager
        accommodations.importJson(accommodationsJson);
        //set the accommodations to configPage property
        configPage.accommodations = accommodations;

        configPage.settings = new Array();   //[Object{name='',type='',value=''}]

        return configPage;
    }

    //retrieve accommodations
    function getAccommodations(testeeRequestParams) {
        var accsJson = { types: [] };

        if (testeeRequestParams == null || testeeRequestParams.length < 1) return accommodations;

        var accGroups = testeeRequestParams.split(';');

        for (var i = 0; i < accGroups.length; i++) {
            var accGroup = accGroups[i];
            var accPair = accGroup.split(':');
            accsJson.types.push({ name: accPair[0], values: [{ code: accPair[1], selected: true}] });   //must set selected to true
        }
        return accsJson;
    }
};

//Functions and Vars for print countdown and close
var gWaitForPrinter = 60; //wait for printer countdown

function removePleaseWait() {
    var node = P.TDS.y.one(document.body);
    return node.addClass('printloaded');
}

function printClicked() {
    var doc = document;
    var lblTimerText = doc.getElementById('lblTimerText');
    lblTimerText.style.display = 'block';

    //start count down
    countDown();
}

function closeClicked() {
    top.close();
}

function countDown() {
    gWaitForPrinter--;
    if (gWaitForPrinter < 0) { //we are done, close the window
        closeClicked();
        return;
    }
    var doc = document;
    doc.getElementById('lblTime').innerHTML = gWaitForPrinter;
    var id = setTimeout("countDown()", 1000); //1 sec
}
