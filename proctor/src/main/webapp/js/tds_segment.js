//required: yui-min.js
//segments
YUI.add("tds-segments", function(Y) {
    //-------------------------------------------------------------------------
    // Private variables
    //-------------------------------------------------------------------------
    var _data = null;
    var _hashTestKeyPos = new Array();
    //-------------------------------------------------------------------------
    // Private functions
    //-------------------------------------------------------------------------

    //return an array of segment objects
    function _getSegments(testKey) {
        if (_data == null)
            return null;
        var segments = new Array();
        var len = _data.length;
        for (var i = 0; i < len; i++) {
            var seg = _data[i];
            if (seg.testKey == testKey)
                segments.push(seg); //add to the end
        }

        return (segments.length < 1) ? null : segments;
    }
    //lookup by testkey and position
    function _getSegmentByPosition(testKey, position) {
        if (_data == null)
            return null;
        var idx = null;
        var hashKey = testKey + "_" + position;

        if (_hashTestKeyPos != null)
            idx = _hashTestKeyPos[hashKey];
        if (idx != null)
            return _data[idx]; //get from hash index

        var len = _data.length;
        for (var i = 0; i < len; i++) {
            var seg = _data[i];
            if (seg.testKey == testKey && seg.position == position) { //should be unique per testkey and seg position
                _hashTestKeyPos[hashKey] = i; //store in a hash for easy lookup
                return seg; //add to the end
            }
        }
        return null;
    }
    //-------------------------------------------------------------------------
    // Public interface
    //-------------------------------------------------------------------------
    Y.tdsSegments = {
        setData: function(data) {
            _data = data;
        },
        getSegments: function(testKey) {
            return _getSegments(testKey);
        },
        getSegmentByPosition: function(testKey, position) {
            return _getSegmentByPosition(testKey, position);
        }
    };
}, "0.1", { requires: ["node"] });

