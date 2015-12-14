//for table
//required: yui-min.js
YUI.add("tds-hideshow", function (Y) {
    //-------------------------------------------------------------------------
    // Private variables
    //-------------------------------------------------------------------------
    var _table = null,
        _ddl = null,
        _hideshowCols = null,
        _cookieName = "hideshowTDS",
        _cookieExpiredays = 1, //default 1 day
        _cellStyle = null;// "block";

    //-------------------------------------------------------------------------
    // Private functions
    //-------------------------------------------------------------------------
    function _loadCols() {
        var theads = _table.getElementsByTagName('thead');
        if (theads == null || theads.size() < 1) return;
        var thead = theads.item(0); //get 1st thead elem
        var rows = thead.get('rows');
        if (rows == null) return;
        var row = rows.item(0).get('cells');
        var cellsLen = row.size();

        var aryCols = _readCookie();
        if (aryCols != null && aryCols.length == cellsLen) { //get value from cookie
            _hideshowCols = aryCols;
            _hideshowColumns();
            return;
        }
    }
    function _readCookie() {
        var value = Y.tdsCookie.getCookie(_cookieName);
        if (value == null || value.length < 1)
            return null;
        var aryCols = value.split(",");
        for (var i = 0; i < aryCols.length; i++)
            aryCols[i] = eval(aryCols[i]);
        return aryCols;
    }

    function _saveCookie() {
        if (_hideshowCols == null)
            return;
        Y.tdsCookie.setCookie(_cookieName, _hideshowCols.join(","), _cookieExpiredays);
    }

    function _hideshowColumn(col, hide) {
        if (col >= 0 && col < _hideshowCols.length) {
            _hideshowCols[col] = hide;
            //table hide/show here...
            var theads = _table.getElementsByTagName('thead');
            if (theads == null || theads.size() < 1) return;
            var thead = theads.item(0); //get 1st thead elem
            var rows = thead.get('rows');
            if (rows == null) return;
            var row = rows.item(0).get('cells');
            var cellsLen = row.size();
            var cell = row.item(col);

            var tBodies = _table.getElementsByTagName('tbody');
            if (tBodies == null || tBodies.size() < 1) return;

            var tBody = tBodies.item(0);
            var bodyRows = tBody.get('rows');
            var bodyRow = bodyRows.item(0).get('cells');
            var bodyCell = bodyRow.item(col);
            if (bodyCell == null) return;
            var style = _hideshowCols[col] ? 'none' : _cellStyle;

            if (cell.getStyle('display') == style && bodyRows.size() > 0
                && bodyCell.getStyle('display') == style) {//dont need to reset                
                return;
            }
            cell.setStyle('display', style); //show/hide header
            var len = bodyRows.size();
            for (var i = 0; i < len; i++) {
                bodyRow = bodyRows.item(i).get('cells');
                bodyCell = bodyRow.item(col);
                if (bodyCell != null)
                    bodyCell.setStyle('display', style);
            }
        }
    }

    function hideshowHeader(table, hide) {
        var theads = _table.getElementsByTagName('thead');
        if (theads == null || theads.size() < 1) return;
        var thead = theads.item(0); //get 1st thead elem
        thead.setStyle('display', hide ? 'none' : '');
    }

    function _hideshowColumns() {
        if (_hideshowCols == null)
            return;
        var len = _hideshowCols.length;
        for (var i = 0; i < len; i++) {
            _hideshowColumn(i, _hideshowCols[i]);
        }
        //_saveCookie();
    }
    function _initCols() {
        var theads = _table.getElementsByTagName('thead');
        if (theads == null || theads.size() < 1) return;
        var thead = theads.item(0); //get 1st thead elem
        var rows = thead.get('rows');
        if (rows == null) return;
        var row = rows.item(0).get('cells');
        var cellsLen = row.size();

        _hideshowCols = new Array(cellsLen);
        var len = cellsLen;
        for (var i = 0; i < len; i++)
            _hideshowCols[i] = false; //load all cols default as
    }

    //-------------------------------------------------------------------------
    // Public interface
    //-------------------------------------------------------------------------
    Y.tdsHideShow = {
        init: function (table, ddl, cookieName) {
            Y.log('tdsHideShow.init()');
            _table = table;
            _ddl = ddl;
            if (cookieName != null && cookieName.length > 0)
                _cookieName = cookieName;
            //2/2013: removed for IE 10 fix
            //if (navigator.appName != "Microsoft Internet Explorer")//?
            //    _cellStyle = null;
            _loadCols();
        },

        setHideShow: function () {
            if (_ddl == undefined) return;

            var thisObj = _ddl;
            var options = thisObj.get('options');
            var selectedOpt = options.item(thisObj.get('selectedIndex'));
            var col = selectedOpt.get('value');

            if (_hideshowCols == null)
                _initCols();
            _hideshowCols[col] = !_hideshowCols[col];
            _hideshowColumn(col, _hideshowCols[col]);
            _saveCookie();
            thisObj.set('selectedIndex', 0);
        }

    };
}, "0.1", { requires: ["tds-cookie"] });
