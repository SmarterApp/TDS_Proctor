var ConfirmExit = {
    that: null,
    returnToOriginalApp: false,
    hasValidated: false,
    daDiv: null,
    dvWait: null,
    localDomains: "",
    bRequiredConfirmation: true, //is the page require confirmation or not
    returnPage: "",
    logoutPage: "",

    init: function(bRequiredConfirmation, localDomains, returnPage, logoutPage) {
        this.that = this;
        this.localDomains = localDomains;
        this.returnPage = returnPage;
        this.logoutPage = logoutPage;
        this.daDiv = document.getElementById("dvWarn");
        this.dvWait = document.getElementById("dvWait");
        if (bRequiredConfirmation != null)
            this.bRequiredConfirmation = bRequiredConfirmation;
        if (!window.opener || window.opener.closed || !this.isLocalPage()) {
            if (this.bRequiredConfirmation) {
                if (this.daDiv != null)
                    this.daDiv.style.display = 'block';
                setInterval('ConfirmExit.setFocus()', 100);
            }
            else
                this.OnExit();
        }
        else {
            if (this.daDiv != null)
                this.daDiv.style.display = 'none';
            window.opener.focus();
            this.hasValidated = true;
            window.close();
        }
        window.onunload = ConfirmExit.checkForValidatedExit;
    },

    isLocalPage: function() {
        var url;
        var that = ConfirmExit.that;
        try {
            if (window.opener && !window.opener.closed)
                url = window.opener.location.toString();
            else
                return false;
        }
        catch (e) {
            return false;
        }
        return (that.isInDomain(url));
    },
    setFocus: function() {
        if (self || (window && !window.closed))
            self.focus();
    },
    isInDomain: function(src) {
        var that = ConfirmExit.that;
        var splitString = that.localDomains.toLowerCase();
        var splitArray = splitString.split("|");
        var inDomain = false;
        var lSrc = src.toLowerCase();
        for (var i = 0; i < splitArray.length; i++) {
            if (lSrc.indexOf(splitArray[i]) != -1) {
                inDomain = true;
                break;
            }
        }
        return inDomain;
    },
    ValidateExit: function() {
        var that = ConfirmExit.that;
        that.OnWait();
        if (that.hasValidated)
            return;
        that.hasValidated = true;
        if (!that.returnToOriginalApp) {
            that.OnExit();
            return;
        }
        if (window.opener && !window.opener.closed) {
            var popupWin = window;            
            window.open(that.returnPage, '_blank', 'menubar=yes,toolbar=yes,location=yes,directories=yes,resizable=yes,scrollbars=yes');
            popupWin.close();
            //window.opener.close();	//did not work on all browsers except for IE			
        }
        else {
            var popupWin = window;
            window.open(that.returnPage, '_blank', 'menubar=yes,toolbar=yes,location=yes,directories=yes,resizable=yes,scrollbars=yes');
            popupWin.close();
        }
    },
    checkForValidatedExit: function() {
        var that = ConfirmExit.that;
        if (!that.hasValidated) {
            window.open(that.logoutPage);
        }
    },
    OnExit: function() {
        var that = ConfirmExit.that;
        window.location = that.logoutPage;
    },
    OnWait: function() {
        var that = ConfirmExit.that;
        if (that.dvWait != null)
            that.dvWait.style.display = 'block';
        if (this.daDiv != null)
            this.daDiv.style.display = 'none';
    },
    ToString: function() {
        var that = ConfirmExit.that;
        var str = 'returnToOriginalApp=' + that.returnToOriginalApp + '\n' +
            'hasValidated=' + that.hasValidated + '\n' +
            'localDomains=' + that.localDomains + '\n' +
            'bRequiredConfirmation=' + that.bRequiredConfirmation + '\n' +
            'returnPage=' + that.returnPage + '\n' +
            'logoutPage=' + that.logoutPage + '\n';

        return str;
    }
}
