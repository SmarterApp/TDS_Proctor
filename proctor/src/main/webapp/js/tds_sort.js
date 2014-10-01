//required: yui-min.js
YUI.add("tds-sort", function(Y) {
    //-------------------------------------------------------------------------
    // Private variables
    //-------------------------------------------------------------------------
    var _sortedArray = null,
        _lastSortedAtt = "status", //sort  status by default
        _lastSortedOrder = 1, //0 for asc 1 for desc
        _testOppList = null,
        _table = null,
        _sortOnclickFunc = null,
        _bSave = true,
        _cookieExpiredays = 1,
        _cookieName = 'sortState';

    var _oTDSCookie = Y.tdsCookie;
    var _oTDSShared = Y.tdsShared;
    //-------------------------------------------------------------------------
    // Private functions
    //-------------------------------------------------------------------------    
    function sort(curSortAtt, curSortOrder) {
        if (_testOppList == null || _testOppList.length < 1) {
            _sortedArray = null; return;
        }
        // no need to load the source array, double click on the same column 	    
        if (_lastSortedAtt != null && curSortAtt == _lastSortedAtt && _lastSortedAtt.length > 0 && _sortedArray != null && _sortedArray.length == _testOppList.length) {
            _sortedArray.reverse();
            _lastSortedOrder = curSortOrder;
            _lastSortedAtt = curSortAtt;
            sortFinal(); //save the state          
            return;
        }
        //load the sourceArray with sort attrib data
        _sortedArray = new Array();
        var len = _testOppList.length;
        for (var i = 0; i < len; i++) {
            _sortedArray[i] = new sortObj(i, _testOppList[i], curSortAtt);
        }
        if (curSortOrder == 0)
            _sortedArray.sort(sortCompare); //Sort alphabetically and ascending
        else {
            _sortedArray.sort(sortCompare);
            _sortedArray.reverse(); //Sort alphabetically and descending
        }
        _lastSortedOrder = curSortOrder;
        _lastSortedAtt = curSortAtt;
        sortFinal(); //save the state       
    }
    function sortFinal() {
        saveCookie(); //save the state
        setCSSClass();
    }

    //save after the sort is done
    function saveCookie() {
        if (!_bSave) return; //dont save if config so               
        if (_lastSortedAtt == null) return; //do nothing
        if (_lastSortedOrder == null) return; //do nothing

        _oTDSCookie.setCookie(_cookieName, _lastSortedAtt + '|' + _lastSortedOrder, _cookieExpiredays);
    }
    function readCookie() {
        if (!_bSave) return; //dont save if config so

        var value = _oTDSCookie.getCookie(_cookieName);
        if (value == null || value.length < 1)
            return null;
        var ary = value.split("|");
        if (ary == null || ary.length < 2) return null;
        _lastSortedAtt = ary[0];
        _lastSortedOrder = ary[1];
    }
    function clearCookie() {
        _oTDSCookie.setCookie(_cookieName, '', -1);
    }


    //set css sort order class and reset the others
    function setCSSClass() {
        if (_lastSortedAtt == null || _lastSortedOrder == null) return;
        Y.log('tdsSort.setCSSClass');
        var theads = _table.getElementsByTagName('thead');
        if (theads == null || theads.size() < 1) return;
        var thead = theads.item(0); //get 1st thead elem
        var rows = thead.get('rows');
        if (rows == null) return;
        var row = rows.item(0).get('cells');
        var len = row.size();
        for (var i = 0; i < len; i++) {
            var cell = row.item(i);
            var curColID = _oTDSShared.rightString(cell.get('id'), "th_");
            if (curColID == null || curColID.length < 1)
                continue;
            cell.removeClass('sortUp');
            cell.removeClass('sortDown');

            if (curColID == _lastSortedAtt)
                cell.addClass((_lastSortedOrder == 0) ? 'sortDown' : 'sortUp');
        }
    }

    function _reset(testOppList) {
        Y.log('tdsSort._reset');
        _sortedArray = null;
        _testOppList = testOppList;
        readCookie(); //get values from cookie

        //sort this base on the last sort att
        if (_lastSortedAtt != null && _lastSortedOrder != null) {
            sort(_lastSortedAtt, _lastSortedOrder);
        }
    }

    function getSortedIndex(testOppList) { //reset everything        
        _testOppList = testOppList;
        var curSortAtt = _lastSortedAtt;
        _lastSortedAtt = null;
        _sort(curSortAtt, _lastSortedOrder);
        return _sortedArray;
    }
    function sortObj(index, testOpp, curSortAtt) {
        this.index = index;
        this.sortAtt = eval("testOpp." + curSortAtt);
    }

    function sortCompare(aa, bb) {
        var a = aa.sortAtt;
        var b = bb.sortAtt;
        if (a == b) {
            return 0;
        } else if (a < b) {
            return -1;
        } else {
            return 1;
        }
    }

    //-------------------------------------------------------------------------
    // Public interface
    //-------------------------------------------------------------------------
    Y.tdsSort = {
        //testOppList: data source
        //table: html table object
        //sortOnclickFunc: sort function to be call onclick event
        //bSave: Save the state of the sort in a cookie
        init: function(testOppList, table, sortOnclickFunc, bSave) {
            Y.log("Y.tdsSort.init");
            _table = table;
            _testOppList = testOppList;
            _sortOnclickFunc = sortOnclickFunc;
            if (bSave != null)
                _bSave = bSave;
            if (_table == null) return;
            var thead = _table.getElementsByTagName('thead').item(0);
            var sortLinks = thead.getElementsByTagName('a');
            var len = sortLinks.size();
            for (var i = 0; i < len; i++) {
                var sortLink = sortLinks.item(i);
                var id = sortLink.get('id');
                if (id != null && id.length > 0) {
                    var curSortAtt = _oTDSShared.rightString(id, "sortCol_");
                    if (curSortAtt.length > 0) {
                        sortLink.on('click', _sortOnclickFunc, null, curSortAtt);
                        var cell = sortLink.get('parentNode');
                        cell.set('id', 'th_' + curSortAtt);
                    }
                }
            }
            sort(_lastSortedAtt, _lastSortedOrder);
        },
        sortClick: function(curSortAtt) {
            Y.log("sortTDS.sortClick");
            var curSortOrder = 0;
            if (_lastSortedOrder == null || _lastSortedOrder == 0)
                curSortOrder = 1;
            else
                curSortOrder = 0;

            sort(curSortAtt, curSortOrder);
        },
        reset: function(testOppList) {
            _reset(testOppList);
        },
        getSortedArray: function() {
            return _sortedArray;
        }

    };
}, "0.1", { requires: ["node", "event", "tds-cookie", "tds-shared"] });


   
 