
//required: yui-min.js
YUI.add("p-Elem", function(Y) {
    //-------------------------------------------------------------------------
    // Private variables
    //-------------------------------------------------------------------------   

    //-------------------------------------------------------------------------
    // Private functions
    //-------------------------------------------------------------------------   

    //-------------------------------------------------------------------------
    // Public interface
    //-------------------------------------------------------------------------
    Y.pElem = {
        btnHelp: null,
        btnLogout: null,
        btnAlerts: null,
        btnStartSession: null,
        btnStopSession: null,
        btnSettings: null,
        btnSettingsMobile: null,
        btnLookup: null,
        btnPrint: null,
        btnRefresh: null,
        lblSessionID: null,
        btnApprovals: null,
        radioSortTests: null,
        btnSelectAllTests: null,
        lblInstruction: null, //instruction

        lblTotalOppsCount: null,
        lblApprovedRequests: null,
        testOppsHideShowColumns: null,
        tblTestOpps: null, //active opps table node
        msgDialog: null,
        accsDetails: null,
        allDetails: null,
        spanInfoMessage: null,

        init: function() {
            var oElem = Y.pElem;
            oElem.btnHelp = Y.one("#btnHelp");
            oElem.btnLogout = Y.one('#btnLogout');
            oElem.btnAlerts = Y.one('#btnAlerts');

            oElem.btnStartSession = Y.one('#btnStartSession');
            oElem.btnStopSession = Y.one('#btnStopSession');
            oElem.btnSettings = Y.one('#btnSettings');
            oElem.btnSettingsMobile = Y.one('#btnSettingsMobile');

            oElem.btnLookup = Y.one('#btnLookup');
            oElem.btnPrint = Y.one('#btnPrint');
            oElem.btnRefresh = Y.one('#btnRefresh');
            oElem.lblSessionID = Y.one('#lblSessionID');

            oElem.radioSortTests = Y.one('#radioSortTests');
            oElem.btnSelectAllTests = Y.one('#btnSelectAllTests');

            oElem.lblInstruction = Y.one('#lblInstruction'); //instruction
            oElem.lblTotalOppsCount = Y.one('#lblTotalOppsCount');
            oElem.lblApprovedRequests = Y.one('#lblApprovedRequests');
            oElem.testOppsHideShowColumns = Y.one('#testOppsHideShowColumns');

            oElem.tblTestOpps = Y.one('#tblTestOpps');

            oElem.msgDialog = Y.one('#msgDialog');
            oElem.accsDetails = Y.one('#accsDetails');
            oElem.allDetails = Y.one('#allDetails');
            oElem.spanInfoMessage = Y.one('#spanInfoMessage');
        },
        addListeners: function() {
            var oElem = Y.pElem;
            if (oElem.btnHelp != null)
                oElem.btnHelp.on('click', Y.pUI.help);
            oElem.btnLogout.on('click', Y.pUI.logout);
            oElem.btnAlerts.on('click', Y.pUI.alerts);
            oElem.btnStartSession.on('click', Y.pUI.startSession);
            oElem.btnStopSession.on('click', Y.pUI.stopSession)
            oElem.btnSettings.on('click', Y.pUI.toggleSettings);

            oElem.btnLookup.on('click', Y.pUI.lookup);
            oElem.btnPrint.on('click', Y.pUI.print);
            oElem.btnRefresh.on('click', Y.pUI.refreshManually);
            var node = Y.one('#accsDetails_Close');
            node.on('click', Y.tdsTestOpps.hideAccDetails);

            if (oElem.radioSortTests != null)
                oElem.radioSortTests.on('change', Y.pUI.radioSortTests);
            oElem.btnSelectAllTests.on('click', Y.pUI.selectAllTests);

            oElem.testOppsHideShowColumns.on('change', Y.tdsHideShow.setHideShow);
            node = Y.one('#btnInstruction');
            node.on('click', Y.pUI.showHideInst);
            node = Y.one('#btnCollapse');
            node.on('click', Y.pUI.collapse);

            oElem.lblApprovedRequests.on('click', Y.pUI.approvedRequests);

            var ddl = Y.one('#ddlUserSites');
            if(ddl != null)
                ddl.on('change', Y.pUI.userSitesChange);
        },

        test: function() {
            Y.pElem.init();
            Y.log(Y.pElem.btnHelp);
        }
    };
}, "0.1", { requires: ["node", "dom", "event", "tds-hideshow"] });