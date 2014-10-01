
    function MessageSwitcher (langAccType, currentLanguage) {
        this._languageAccType = langAccType;
        this._currentLanguage = currentLanguage;
        this._selectBoxElem = P.TDS.y.one("#selectLang");
        this._languagesLoaded = new Array();
    };

    MessageSwitcher.prototype.init = function () {
        if (this._selectBoxElem == undefined) return;
        if (this._languageAccType == undefined || this._languageAccType.Values.length < 1) {
            this._selectBoxElem.hide(); return;
        }

        this._selectBoxElem.on("change", this.onChange, null, this);
        this._selectBoxElem.empty(); // clear the select box
        var len = this._languageAccType.Values.length;
        for (var j = 0; j < len; j++) {
            var accValue = this._languageAccType.Values[j];
            var opt = P.TDS.y.tdsShared.optionNode(accValue.Value, null);
            opt.setAttribute('value', accValue.Code);
            var isSelected = (accValue.Code == this._currentLanguage) ? true : false;
            opt.set('selected', isSelected);
            this._selectBoxElem.appendChild(opt);
        }

        this._languagesLoaded.push(this._currentLanguage);
    };

    //are all messages for this language have been load
    MessageSwitcher.prototype.messagesLoaded = function (selectedLang) {
        if (this._selectBoxElem == undefined || this._languageAccType == undefined) return;
        this._selectBoxElem.empty(); // clear the select box
        var len = this._languageAccType.Values.length;
        for (var j = 0; j < len; j++) {
            var accValue = this._languageAccType.Values[j];
            var opt = P.TDS.y.tdsShared.optionNode(accValue.Value, null);
            opt.setAttribute('value', accValue.Code);
            var isSelected = (accValue.Code == this._currentLanguage) ? true : false;
            opt.set('selected', isSelected);
            this._selectBoxElem.appendChild(opt);
        }

        this._languagesLoaded.push(this._currentLanguage);
    };

    MessageSwitcher.prototype.onChange = function (e, that) {
        var options = this.get('options');
        var selectedOpt = options.item(this.get('selectedIndex'));
        var selectedLang = selectedOpt.get('value');
        var len = that._languagesLoaded.length;
        var bLoaded = false;
        for (var i = 0; i < len; i++) {
            if (that._languagesLoaded[i] == selectedLang) {
                bLoaded = true;
                break;
            }
        }
        if (bLoaded) {
            P.TDS.setCurrentLanguage(selectedLang);
            //load new set of messages with selected language
            MessageTemplate.processLanguage();
            return;
        }
       
        //make xhr call to get messages for selected language
        var contexts = P.TDS.messages.getContextKeys().join(',');
        var data = "selectedLanguage=" + selectedLang + "&contexts=" + contexts;

        P.Util.xhrCall("Services/XHR.axd/GetMessagesLanguage", function (returnObject) {
            //set language and then load
            if (returnObject == undefined || (returnObject.status != undefined && returnObject.status == "failed"))
                return;
            P.TDS.messageloader.load(returnObject);
            P.TDS.setCurrentLanguage(selectedLang);
            that._languagesLoaded.push(selectedLang);
            //load new set of messages with selected language
            MessageTemplate.processLanguage();
        }, data);        
    };


