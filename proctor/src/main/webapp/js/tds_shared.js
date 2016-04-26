//required: yui-min.js
YUI.add("tds-shared", function (Y) {
    //-------------------------------------------------------------------------
    // Private variables
    //-------------------------------------------------------------------------
    var data;
    var _oClassName = Y.pClassName;
    var _oElem = Y.pElem;
    var _mobileV = null;
    var _N = Y.Node;
    var _keysSubscription = null;
    var _disableKeys = false;

    //-------------------------------------------------------------------------
    // Private functions
    //-------------------------------------------------------------------------
    function _removeBodyClass(classN) {
        var node = Y.one(document.body);
        return node.removeClass(classN);
    }
    function _addBodyClass(classN) {
        var node = Y.one(document.body);
        return node.addClass(classN);
    }
    function _hasBodyClass(classN) {
        var node = Y.one(document.body);
        return node.hasClass(classN);
    }
    function _buildNode(htmlString, content, className) {
        var node = _N.create(htmlString);
        if (content != null)
            node.setContent(content);
        if (className != null)
            node.setAttribute('class', className);
        return node;
    }
    function _cbNode(name, value, className, checked, disabled) {
        var node = _buildNode('<input type="checkbox" name="' + name + '"' + ((checked) ? ' checked ' : '') + ((disabled) ? ' disabled ' : '') + '>', null, className);

        if (value != null) node.setAttribute('value', value);
        node.setAttribute('id', name);
        return node;
    }
    function _labelNode(name, label) {
        return _buildNode('<label for="' + name + '"></label>', label, null);
    }



    //activate scrollview for mobile
    function _activateScollView(elem) {
        return null; //Hoai-Anh: disabled the scroll view per Adam/Dan requested on 01/11/2012

        if (!_mobile()) return null;
        //alert(elem);
        if (elem == null) return null;
        var elemDOM = Y.Node.getDOMNode(elem);
        var elemId = elem.get('id');
        elemId += "_scrollview";
        Y.log("tdsShared._activateScollView: " + elemDOM.offsetHeight);
        var scrollView = new Y.ScrollView({
            //id: elemId,
            srcNode: elem,
            height: elemDOM.offsetHeight - 1 //the scrollview height must be smaller than a div height for its to work
        });
        scrollView.render();
        _scrollViewDelegate(scrollView);
        return scrollView;
    }
    function _scrollViewDelegate(scrollView) {
        if (scrollView == null) return;
        var content = scrollView.get("contentBox");

        content.delegate("click", function (e) {
            // Prevent links from navigating as part of a scroll gesture
            if (Math.abs(scrollView.lastScrolledAmt) > 2) {
                e.preventDefault();
                Y.log("Link behavior suppressed.");
            }
        }, "a");

        content.delegate("mousedown", function (e) {
            // Prevent default anchor drag behavior, on browsers which let you drag anchors to the desktop
            e.preventDefault();
        }, "a");

        Y.log("tdsShared._scrollViewDelegate");
    }

    //browser UA
    function _mobile() {
        //return true;
        if (_mobileV != null)
            return _mobileV;
        //alert(Y.UA.android);
        _mobileV = (Y.UA.mobile != null || Y.UA.android);
        return _mobileV;
    }
    //check orientation
    function _orientation() {
        if (!_mobile()) return;
        Y.log("orientationchange");
        Y.on('orientationchange', function (evt) {
            setTimeout(_orientationHandler, 1000); 
            //hack to fixed the race issue where it gets the width and height too soon
        });
        setTimeout(_orientationHandler, 1000);
    }
    function _orientationHandler() {
        var oClass = 'landscape';
        //alert(Y.config.win.orientation);

        //alert('screen.width: ' + screen.width + ' screen.height: ' + screen.height + '\nwindow.innerWidth: ' + window.innerWidth + ' window.innerHeight: ' + window.innerHeight);
        /*
        //orientation does not seem to work on all android devices
        if (Y.config.win.orientation % 180) {
        _addBodyClass(oClass);
        } else {
        _removeBodyClass(oClass);
        }
        */
        //alert('innerWidth: ' + window.innerWidth + ' innerHeight: ' + window.innerHeight);
        if (screen.width > screen.height)
            _addBodyClass(oClass);
        else
            _removeBodyClass(oClass);
            
    }

    //-------------------------------------------------------------------------
    // Public interface
    //-------------------------------------------------------------------------
    Y.tdsShared = {
        removeBodyClass: function (classN) {
            _removeBodyClass(classN);
        },

        addBodyClass: function (classN) {
            _addBodyClass(classN);
        },

        hasBodyClass: function (classN) {
            return _hasBodyClass(classN);
        },
        //timeout *************************************************************************************************
        //need to call within iframe only, to make sure activities on the iframe page get registered to the parent page
        addRegisterActivity: function () {
            if (top.fireGlobalEvent != undefined) {
                //add this if you want to keep track of user inactivity from the parent window.
                if (top.fireGlobalEvent != undefined) {
                    var doc = Y.one(document);
                    doc.on('keydown', function (e) { top.fireGlobalEvent('GlobalEvent:registerActivity'); });
                    doc.on('mousedown', function (e) { top.fireGlobalEvent('GlobalEvent:registerActivity'); });
                }
            }
        },

        //please wait **********************************************************************************************
        startPleaseWait: function () {
            Y.log("tdsShared.startPleaseWait");
            _addBodyClass(_oClassName.please_wait);
        },

        stopPleaseWait: function () {
            Y.log("tdsShared.stopPleaseWait");
            _removeBodyClass(_oClassName.please_wait);
        },

        showError: function (msg) {
            var elem = _oElem.spanInfoMessage;
            _removeBodyClass(_oClassName.please_wait);

            if (msg == undefined || msg.length < 1) {
                elem.hide();
            } else {
                elem.setContent(msg);
                elem.show();
            }
        },

        renderError: function (objReturnStatus) {
            if (objReturnStatus == undefined || objReturnStatus.status == null || objReturnStatus.status.length < 1) return false;
            var dlg;
            //this is a hack for the dialog display
            if (objReturnStatus.reason.indexOf("11601") != -1 || objReturnStatus.reason.indexOf("11602") != -1
            || objReturnStatus.reason.indexOf("The session is closed.") != -1 || objReturnStatus.reason.indexOf("The session is not owned by this proctor") != -1) {
                //SESSION_CLOSED: session is closed or assign to another proctor for some reasons
                _removeBodyClass(_oClassName.please_wait);
                Y.pUI.stopAutoRefresh();
                dlg = Y.tdsShared.msgDialog(Y.tds.messages.getRaw("Important!"), Y.tds.messages.getMessage(objReturnStatus.reason));
                dlg.addButton("confirm", Y.tds.messages.getRaw("Button.OK"), Y.tdsSession.sessionClosedHandler);
                dlg.show(Y.tdsShared.NotificationType.warning);
                return true;
            }
            if (objReturnStatus.reason.indexOf("11603") != -1 || objReturnStatus.reason.indexOf("Unauthorized session access") != -1) {
                //INVALID_ACCESS_HANDOFF: hand off to another browser key
                _removeBodyClass(_oClassName.please_wait);
                Y.pUI.stopAutoRefresh();
                dlg = Y.tdsShared.msgDialog(Y.tds.messages.getRaw("Important!"), Y.tds.messages.getMessage(objReturnStatus.reason));
                dlg.addButton("confirm", Y.tds.messages.getRaw("Button.LogBackIn"), Y.pUI.logoutBrowserSessionOnly);
                dlg.show(Y.tdsShared.NotificationType.locked);
                return true;
            }
            if (objReturnStatus.reason.indexOf("11604") != -1 || objReturnStatus.reason.indexOf("There already is an active session for this user.") != -1) {
                //"ALREADY_HAS_ACTIVESESSION" browser crashed or someone else already created the session
                _removeBodyClass(_oClassName.please_wait);
                Y.pUI.stopAutoRefresh();
                dlg = Y.tdsShared.msgDialog(Y.tds.messages.getRaw("Important!"), Y.tds.messages.getMessage(objReturnStatus.reason));
                dlg.addButton("confirm", Y.tds.messages.getRaw("Button.Enter"), Y.tdsSession.takeOverSession, dlg);
                dlg.addButton("close", Y.tds.messages.getRaw("Button.Logout"), Y.pUI.logoutBrowserSessionOnly);
                dlg.addText("locked_sessionID", null, null);
                dlg.show(Y.tdsShared.NotificationType.locked);
                return true;
            }

            return false;
        },
        renderErrorDlg: function (objReturnStatus) {
            if (objReturnStatus == undefined || objReturnStatus.status == null || objReturnStatus.status.length < 1) return false;
            var dlg;
            _removeBodyClass(_oClassName.please_wait);
            Y.pUI.stopAutoRefresh();
            dlg = Y.tdsShared.msgDialog(Y.tds.messages.getRaw("Important!"), Y.tds.messages.getMessage(objReturnStatus.reason));
            dlg.addButton("confirm", Y.tds.messages.getRaw("Button.OK"), Y.tdsSession.sessionClosedHandler);
            dlg.show(Y.tdsShared.NotificationType.warning);
        },

        //browser UA
        mobile: function () {
            return _mobile();
        },
        //check orientation
        orientation: function () {
            return _orientation();
        },
        //browser less than ie 8
        belowIE8: function () {
            return (Y.UA.ie && Y.UA.ie < 8);
        },

        //disable/enable keys
        disableKeys: function (e) {
            Y.log("disableKeys");
            if (_keysSubscription != undefined) return;
            var node = Y.one(document.body);
            _keysSubscription = node.on("key", function (e) {
                if (_disableKeys) {
                    e.preventDefault();
                    e.stopPropagation();
                    e.halt();
                    return false;
                }
            }, 'down:9, 32'); //tab, space

        },
        enableKeys: function () {
            Y.log("enableKeys");
            if (_keysSubscription == undefined) return;
            _keysSubscription.detach();
            _keysSubscription = null;
        },

        activateScollView: function (elem) {
            return _activateScollView(elem);
        },

        includeCSSfile: function (href, callBackFunc) {
            var head_node = Y.Node.one(document.getElementsByTagName('head')[0]);
            var link_node = _buildNode('<link rel="stylesheet" type="text/css" href="' + href + '" />', null, null);
            Y.log('includeCSSfile');
            head_node.append(link_node);
            //var node = Y.one("#testID");           
            if (callBackFunc != undefined)
                Y.on("domready", callBackFunc, link_node);
        },

        //string ----------------------------------------------
        rightString: function (fullString, subString) {
            if (fullString.indexOf(subString) == -1) {
                return "";
            } else {
                return (fullString.substring(fullString.indexOf(subString) + subString.length, fullString.length));
            }
        },
        leftString: function (fullString, subString) {
            if (fullString.indexOf(subString) == -1) {
                return "";
            } else {
                return (fullString.substring(0, fullString.indexOf(subString)));
            }
        },
        disableStyle: function (id, callBackFunc) {
            var altStyle = Y.one(id);
            if (altStyle == null) return;
            altStyle.set("disabled", true);
            if (callBackFunc != undefined)
                Y.on("domready", callBackFunc, altStyle);
        },

        init: function (alertType) {

        },
        accTypeDivId: function (accTypeName) {
            return Y.tdsShared.buildId('accTypeDiv_', accTypeName);
        },
        buildOppId: function (partialID, testOpp) {
            return partialID + testOpp.oppKey;
        },
        buildId: function (partialID, id) {
            if (id == null || id.length < 1)
                return "";
            return partialID + id.replace(/ /g, '_');
        },
        buildSegmentLabel: function (segment) {
            return 'Segment ' + segment.position + ' (' + segment.label + ')';
        },
        h2Node: function (content, className) {
            return _buildNode('<h2></h2>', content, className);
        },
        linkNode: function (rel, type, href) {
            return _buildNode('<link rel="' + rel + '" type="' + type + '" href="' + href + '"></link>', null, null);
        },
        aNode: function (content, className) {
            return _buildNode('<a href="#"></a>', content, className);
        },
        divNode: function (content, className) {
            return _buildNode('<div></div>', content, className);
        },
        spanNode: function (content, className) {
            return _buildNode('<span></span>', content, className);
        },
        tdNode: function (content, className) {
            return _buildNode('<td></td>', content, className);
        },
        trNode: function (content, className) {
            return _buildNode('<tr></tr>', content, className);
        },
        selectNode: function (className) {
            return _buildNode('<select></select>', null, className);
        },
        optionNode: function (content, className) {
            return _buildNode('<option></option>', content, className);
        },
        iframeNode: function () {
            return _buildNode('<iframe frameborder="0"></iframe>', null, null);
        },
        liCheckBoxNode: function (name, value, label, className, checked, disabled) {
            var li = _buildNode('<li></li>', null, className);
            li.append(_cbNode(name, value, className, checked, disabled));
            li.append(_labelNode(name, label));
            return li;
        },
        ulNode: function (content, className) {
            return _buildNode('<ul></ul>', content, className);
        },
        buildNode: function (htmlString, content, className) {
            return _buildNode(htmlString, content, className);
        },

        //get all checked values
        getCheckedValues: function (parentElem) {
            var options = parentElem.getElementsByTagName('input');
            if (options == null) return null;
            var len = options.size();
            var selectedValues = new Array();
            for (var i = 0; i < len; i++) {
                var item = options.item(i);
                var isDisabled = item.get('disabled');
                if (isDisabled) continue;
                var isSelected = item.get('checked');
                if (!isSelected) continue;
                selectedValues.push(item.get('id'));
            }
            return selectedValues;
        },
        setCheckedValues: function (list, values) {
            if (list == undefined) return;

            var options = list.getElementsByTagName('input');
            if (options.size() < 1) return;
            var len = options.size();
            for (var i = 0; i < len; i++)
                options.item(i).set('checked', false); //clear out all selections
            if (values == undefined) return;
            var vLen = values.length;
            for (var v = 0; v < vLen; v++) {
                for (var i = 0; i < len; i++) {
                    var item = options.item(i);
                    if (values[v] == item.get('id'))
                        item.set('checked', true);
                }
            }
        },

        //checked the next item and return the id
        checkedNextItem: function (list, value) {
            if (list == undefined) return;

            var options = list.getElementsByTagName('input');
            if (options.size() < 1) return;
            var len = options.size();
            for (var i = 0; i < len; i++) {
                if (value == options.item(i).get('id')) {
                    var index = 0;
                    if (i + 1 < len)
                        index = i + 1;
                    var item = options.item(index);
                    item.set('checked', true);
                    return item.get('id');
                }
            }
            options.item(0).set('checked', true); //checked the first item if all failed
            return options.item(0).get('id');
        },
        //remove all elements in arrayElement from arraySrc
        removeByElements: function (arraySrc, arrayElement) {
            if (arraySrc == null || arrayElement == null) return;
            var elemLen = arrayElement.length;
            var srcLen = arraySrc.length;
            for (var i = 0; i < elemLen; i++) {
                for (var s = 0; s < srcLen; s++) {
                    if (arrayElement[i] == arraySrc[s])
                        arraySrc.splice(s, 1);
                }
            }
        },

        NotificationType: {
            none: 0,
            success: 1,
            warning: 2,
            error: 3,
            info: 4,
            locked: 5
        },
        ReturnStatus: function (status, reason) {
            this.status = status;
            this.reason = reason;
            return this;
        },
        isCharsORNum: function (value) {
            if (value == null || value.length < 1)
                return false;
            if (/[^A-Za-z\d]/.test(value))
                return false;
            return true;
        },
        isValidName: function (value) {
            if (value == null || value.length < 1)
                return false;
            //if (!(/^['A-Za-z]{1}[\(\)\''\`\.\,a-zA-Z0-9\s\/\-\$!~#'']{0,49}$/.test(value)))
            if (!(/^['A-Za-z]{1}[\(\)\\\''\`\.\,a-zA-Z0-9\s\/\-\@$*!~#'']{0,49}$/.test(value)))
                return false;
            return true;
        },
        //select box ------------------------------------
        //list: select box
        //values: Array() of values
        setSelectedValues: function (list, values) {
            if (list == undefined) return;

            var options = list.get('options')
            if (options.size() < 1) return;
            var len = options.size();
            for (var i = 0; i < len; i++)
                options.item(i).set('selected', false); //clear out all selections
            if (values == undefined) return;
            var vLen = values.length;
            for (var v = 0; v < vLen; v++) {
                for (var i = 0; i < len; i++) {
                    var item = options.item(i);
                    if (values[v] == item.get('value'))
                        item.set('selected', true);
                }
            }
        },

        //return an ary of list box values
        getListBoxValues: function (elem) {
            var options = elem.get('options')
            if (options.size() < 1) return null;
            var ary = new Array();
            var len = options.size();
            for (var i = 0; i < len; i++) {
                var item = options.item(i);
                var isSelected = item.get('selected');
                if (isSelected)
                    ary.push(item.get('value'));
            }
            return ary;
        },

        getSelectedValue: function(selElem) {
            var elem = Y.tdsShared.getSelectedOption(selElem);
            if (elem == null)
                return "";
            return elem.getAttribute('value');
        },
            //get selected option in a drop down list
            //return an opt elem
        getSelectedOption: function (selElem) {           
            if (selElem == null)
                return null;
            var idx = selElem.get('selectedIndex');
            return selElem.get('options').item(idx);
        },

        renderSingleSelect: function (selBoxNode, firstItemText, aryValues, selectedValue) {
            if (selBoxNode == null) return;
            selBoxNode.empty();
            if (firstItemText != null) 
                selBoxNode.insert('<option value="">' + firstItemText + '</option>');
            var len = aryValues.length;

            for (var i=0; i<len;i++) {
                selBoxNode.insert('<option value="' + aryValues[i] + '" ' + ((selectedValue == aryValues[i]) ? 'selected' : '') + '>' + aryValues[i] + '</option>')
            }
        },
        //return an arry of keys
        getObjectKeys: function(objHash){
            var keys = [];
            for (var key in objHash) {
                if (objHash.hasOwnProperty(key)) {
                    keys.push(key);
                }
            }
            return keys;
        },

        //mobile hacks ************************
        addClosing: function () {
            if (!Y.tdsShared.mobile())
                return;
            var classN = "closing";
            Y.log("addClosing");
            Y.tdsShared.addBodyClass(classN);
            setTimeout(function () {
                Y.tdsShared.removeBodyClass(classN);
            }, 1000);
        },

        msgDialog: function (title, message) {
            var yN = Y.Node;
            this._id = '#msgDialog';
            this._container = Y.one(this._id); //elem that contain the msgDialog
            this._title = yN.create('<h2>' + title + '</h2>');  //h2 elem
            this._message = yN.create('<span class="message_container"></span>'); //span elem
            this._message.set('innerHTML', message); //in case where message has html elems  
            this._error = yN.create('<span class="infoMessage" style="display:none"></span>');

            this._buttons = []; //list of button elems with listener already assigned, in order of the array
            this._actionsElem = yN.create('<div class="action"></div>');

            this.addButton = function (type, label, fnListener, fnArgs) { //type is a css class name = close|confirm|warn
                var yN = Y.Node;
                var btnElem = yN.create('<a class="' + type + '">' + label + '</a>');
                btnElem.on('click', fnListener, null, fnArgs);
                this._buttons[this._buttons.length] = btnElem;
            };

            this.addText = function (id, value, className) {
                var yN = Y.Node;
                var elem = yN.create('<input autocorrect="off" id="' + id + '">');
                if (className != null)
                    elem.set('class', className);
                if (value != null)
                    elem.set('value', value);
                this._buttons[this._buttons.length] = elem;
            };

            this.render = function () {
                if (this._container == null)
                    return;

                this._container.set('innerHTML', '');
                if (this._title != null)
                    this._container.appendChild(this._title);
                if (this._message != null)
                    this._container.appendChild(this._message);
                if (this._error != null) //error elem
                    this._container.appendChild(this._error);
                if (this._buttons != null)
                    for (var i = 0; i < this._buttons.length; i++)
                        this._actionsElem.appendChild(this._buttons[i]);
                if (this._actionsElem != null)
                    this._container.appendChild(this._actionsElem);
                window.location.hash = this._id; //hack to shilf focus to the dialog             
            };

            this.show = function (notificationType) {
                var _oShared = Y.tdsShared;
                var _oClass = Y.pClassName;
                var type = _oShared.NotificationType;

                switch (notificationType) {
                    case type.info:
                        _oShared.addBodyClass(_oClass.show_dialog);
                        _oShared.removeBodyClass(_oClass.message_error);
                        _oShared.removeBodyClass(_oClass.message_warning);
                        _oShared.addBodyClass(_oClass.message_info);
                        _oShared.removeBodyClass(_oClass.message_success);
                        break;
                    case type.success:
                        _oShared.addBodyClass(_oClass.show_dialog);
                        _oShared.removeBodyClass(_oClass.message_error);
                        _oShared.removeBodyClass(_oClass.message_warning);
                        _oShared.removeBodyClass(_oClass.message_info);
                        _oShared.addBodyClass(_oClass.message_success);
                        break;
                    case type.warning:
                        _oShared.addBodyClass(_oClass.show_dialog);
                        _oShared.removeBodyClass(_oClass.message_error);
                        _oShared.addBodyClass(_oClass.message_warning);
                        _oShared.removeBodyClass(_oClass.message_info);
                        _oShared.removeBodyClass(_oClass.message_success);
                        break;
                    case type.error:
                        _oShared.addBodyClass(_oClass.show_dialog);
                        _oShared.addBodyClass(_oClass.message_error);
                        _oShared.removeBodyClass(_oClass.message_warning);
                        _oShared.removeBodyClass(_oClass.message_info);
                        _oShared.removeBodyClass(_oClass.message_success);
                        break;
                    case type.locked:
                        _oShared.addBodyClass(_oClass.show_dialog);
                        _oShared.addBodyClass(_oClass.message_locked);
                        _oShared.removeBodyClass(_oClass.message_error);
                        _oShared.removeBodyClass(_oClass.message_warning);
                        _oShared.removeBodyClass(_oClass.message_info);
                        _oShared.removeBodyClass(_oClass.message_success);
                        break;
                }
                this.render();
                Y.tdsShared.disableKeys();
            };
            this.onError = function (strError) {
                this._error.set('innerHTML', strError);
                this._error.setStyle('display', 'block');
            };

            this.cancel = function () {
                var _oShared = Y.tdsShared;
                var _oClass = Y.pClassName;
                _oShared.removeBodyClass(_oClass.show_dialog);
                _oShared.removeBodyClass(_oClass.message_error);
                _oShared.removeBodyClass(_oClass.message_warning);
                _oShared.removeBodyClass(_oClass.message_info);
                _oShared.removeBodyClass(_oClass.message_success);
                _oShared.removeBodyClass(_oClass.message_locked);
                Y.tdsShared.enableKeys();
            };

            this.showMsg = function (notificationType) {
                this.addButton("close", Y.tds.messages.getRaw("Button.Cancel"), this.cancel);
                this.show(notificationType);
            };

            return this;
        }
    };
}, "0.1", { requires: ["node", "p-ClassName", "p-Elem"] });







