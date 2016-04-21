//required: yui-min.js
YUI.add("p-ClassName", function(Y) {
    //-------------------------------------------------------------------------
    // Private variables
    //-------------------------------------------------------------------------

    //-------------------------------------------------------------------------
    // Private functions
    //-------------------------------------------------------------------------    

    //-------------------------------------------------------------------------
    // Public interface
    //-------------------------------------------------------------------------
    Y.pClassName = {
        notStarted: "not_started",
        noneSelected: "none_selected",
        someSelected: "some_selected",
        approvalsNeeded: "approvals_needed",
        approvals_detail: "approvals_detail",
        all_details: "all_details",
        settingsShow: "settings_show",
        nocollapse: "nocollapse",

        please_wait: "please_wait",
        approvals_window: "approvals_window",
        show_details: "show_details",
        show_denial: "show_denial",
        alerts_window: "alerts_window",
        lookup_window: "lookup_window",
        help_window: "help_window",
        print_window: "print_window", //print request
        show_alert: "show_alert", //show unacknowledged alert message

        show_dialog: "show_dialog",
        message_error: "message_error",
        message_warning: "message_warning",
        message_info: "message_info",
        message_success: "message_success",
        message_locked: "message_locked",

        test: function() {
            Y.log(Y.pClassName.message_success);
        }
    };
}, "0.1");