YUI.add("util", function (Y) {
    /* The Proctor Util object is now contained in the P class to prevent confusion between the Proctor and Blackbox's global Util objects */
    P = {};

    //-------------------------------------------------------------------------
    // Public interface
    //-------------------------------------------------------------------------
    P.Util = {};

    //wrapper for Dom
    P.Util.Dom = {};

    P.Util.Structs = {};

    P.Util.Array = {};

    //select elements based on selector
    P.Util.Dom.all = function (selector) {
        return Y.all(selector);
    };

    P.Util.Dom.getTextContent = function (node) {
        return node.get('text');
    };

    P.Util.Dom.setTextContent = function (node, text) {
        return node.set('text', text);
    };

    P.Util.Dom.setContent = function (node, text) {
        return node.setContent(text);
    };

    P.Util.Dom.setAttribute = function (node, attribute, value) {
        return node.set(attribute, value);
    };



    // http://stackoverflow.com/questions/6053108/javascript-4d-arrays/6053332#6053332
    P.Util.Array.createMultiDimArray = function () {
        var args = Array.prototype.slice.call(arguments);

        function helper(arr) {
            if (arr.length <= 0) {
                return;
            }
            else if (arr.length == 1) {
                return new Array(arr[0]);
            }

            var currArray = new Array(arr[0]);
            var newArgs = arr.slice(1, arr.length);
            for (var i = 0; i < currArray.length; i++) {
                currArray[i] = helper(newArgs);
            }
            return currArray;
        }

        return helper(args);
    };

    P.Util.xhrCall = function (url, callBackFunction, data) {
        //A function handler to use for successful requests:
        var handleSuccess = function (ioId, o) {
            if (o.responseText != undefined) {
                var returnedStatus = Y.JSON.parse(o.responseText);
                if (callBackFunction != undefined)
                    callBackFunction(returnedStatus);
            }
            else {
                var returnedStatus = new P.Util.ReturnStatus("failed", "responseText is undefined");
                if (callBackFunction != undefined)
                    callBackFunction(returnedStatus);
            }
        };
        var handleFailure = function (ioId, o) {
            var returnedStatus = new P.Util.ReturnStatus("failed", "transaction failed");
            if (callBackFunction != undefined)
                callBackFunction(returnedStatus);
        };

        /* Configuration object for POST transaction */
        var cfg = {
            method: "POST",
            data: data,
            timeout: gXHRTimeout,
            headers: { "cache-control": "no-cache", 'X-Transaction': '' },
            on: {
                success: handleSuccess,
                failure: handleFailure
            }
        };
        Y.io(url, cfg);
    };

    P.Util.ReturnStatus = function (status, reason) {
        this.status = status;
        this.reason = reason;
        return this;
    };
}, "0.1", { requires: ["node", "io", "json-parse"] });