﻿YUI.add("util_structs", function (Y) {

    //-------------------------------------------------------------------------
    // Public interface
    //-------------------------------------------------------------------------

    // Class for Hash Map datastructure.
    P.Util.Structs.Map = function (opt_map, var_args) {
        /**
        * The number of key value pairs in the map.
        * @private
        * @type {number}
        */
        this._count = 0;


        /**
        * Version used to detect changes while iterating.
        * @private
        * @type {number}
        */
        this._version = 0;

        /**
        * Underlying JS object used to implement the map.
        * @type {!Object}
        * @private
        */
        this._map = {};

        /**
        * An array of keys. This is necessary for two reasons:
        *   1. Iterating the keys using for (var key in this._map) allocates an
        *      object for every key in IE which is really bad for IE6 GC perf.
        *   2. Without a side data structure, we would need to escape all the keys
        *      as that would be the only way we could tell during iteration if the
        *      key was an internal key or a property of the object.
        *
        * This array can contain deleted keys so it's necessary to check the map
        * as well to see if the key is still in the map (this doesn't require a
        * memory allocation in IE).
        * @type {!Array.<string>}
        * @private
        */
        this._keys = [];

        var argLength = arguments.length;

        if (argLength > 1) {
            if (argLength % 2) {
                throw Error('Uneven number of arguments');
            }

            for (var i = 0; i < argLength; i += 2) {
                this.set(arguments[i], arguments[i + 1]);
            }
        }
        else if (opt_map) {
            this.addAll(/** @type {Object} */(opt_map));
        }
    };

    /**
    * @return {number} The number of key-value pairs in the map.
    */
    P.Util.Structs.Map.prototype.getCount = function () {
        return this._count;
    };

    /**
    * Returns the values of the map.
    * @return {!Array} The values in the map.
    */
    P.Util.Structs.Map.prototype.getValues = function () {
        this._cleanupKeysArray();

        var rv = [];
        for (var i = 0; i < this._keys.length; i++) {
            var key = this._keys[i];
            rv.push(this._map[key]);
        }
        return rv;
    };


    /**
    * Returns the keys of the map.
    * @return {!Array.<string>} Array of string values.
    */
    P.Util.Structs.Map.prototype.getKeys = function () {
        this._cleanupKeysArray();
        return /** @type {!Array.<string>} */(this._keys.concat());
    };

    /**
    * Whether the map contains the given key.
    * @param {*} key The key to check for.
    * @return {boolean} Whether the map contains the key.
    */
    P.Util.Structs.Map.prototype.containsKey = function (key) {
        return P.Util.Structs.Map._hasKey(this._map, key);
    };


    /**
    * Whether the map contains the given value. This is O(n).
    * @param {*} val The value to check for.
    * @return {boolean} Whether the map contains the value.
    */
    P.Util.Structs.Map.prototype.containsValue = function (val) {
        for (var i = 0; i < this._keys.length; i++) {
            var key = this._keys[i];
            if (P.Util.Structs.Map._hasKey(this._map, key) && this._map[key] == val) {
                return true;
            }
        }
        return false;
    };


    /**
    * Whether this map is equal to the argument map.
    * @param {P.Util.Structs.Map} otherMap The map against which to test equality.
    * @param {function(*, *) : boolean=} opt_equalityFn Optional equality function
    *     to test equality of values. If not specified, this will test whether
    *     the values contained in each map are identical objects.
    * @return {boolean} Whether the maps are equal.
    */
    P.Util.Structs.Map.prototype.equals = function (otherMap, opt_equalityFn) {
        if (this === otherMap) {
            return true;
        }

        if (this._count != otherMap.getCount()) {
            return false;
        }

        var equalityFn = opt_equalityFn || P.Util.Structs.Map.defaultEquals;

        this._cleanupKeysArray();
        for (var key, i = 0; key = this._keys[i]; i++) {
            if (!equalityFn(this.get(key), otherMap.get(key))) {
                return false;
            }
        }

        return true;
    };


    /**
    * Default equality test for values.
    * @param {*} a The first value.
    * @param {*} b The second value.
    * @return {boolean} Whether a and b reference the same object.
    */
    P.Util.Structs.Map.defaultEquals = function (a, b) {
        return a === b;
    };


    /**
    * @return {boolean} Whether the map is empty.
    */
    P.Util.Structs.Map.prototype.isEmpty = function () {
        return this._count == 0;
    };


    /**
    * Removes all key-value pairs from the map.
    */
    P.Util.Structs.Map.prototype.clear = function () {
        this._map = {};
        this._keys.length = 0;
        this._count = 0;
        this._version = 0;
    };

    /**
    * Removes a key-value pair based on the key. This is O(logN) amortized due to
    * updating the keys array whenever the count becomes half the size of the keys
    * in the keys array.
    * @param {*} key  The key to remove.
    * @return {boolean} Whether object was removed.
    */
    P.Util.Structs.Map.prototype.remove = function (key) {
        if (P.Util.Structs.Map._hasKey(this._map, key)) {
            delete this._map[key];
            this._count--;
            this._version++;

            // clean up the keys array if the threshhold is hit
            if (this._keys.length > 2 * this._count) {
                this._cleanupKeysArray();
            }

            return true;
        }
        return false;
    };


    /**
    * Cleans up the temp keys array by removing entries that are no longer in the
    * map.
    * @private
    */
    P.Util.Structs.Map.prototype._cleanupKeysArray = function () {
        if (this._count != this._keys.length) {
            // First remove keys that are no longer in the map.
            var srcIndex = 0;
            var destIndex = 0;
            while (srcIndex < this._keys.length) {
                var key = this._keys[srcIndex];
                if (P.Util.Structs.Map._hasKey(this._map, key)) {
                    this._keys[destIndex++] = key;
                }
                srcIndex++;
            }
            this._keys.length = destIndex;
        }

        if (this._count != this._keys.length) {
            // If the count still isn't correct, that means we have duplicates. This can
            // happen when the same key is added and removed multiple times. Now we have
            // to allocate one extra Object to remove the duplicates. This could have
            // been done in the first pass, but in the common case, we can avoid
            // allocating an extra object by only doing this when necessary.
            var seen = {};
            var srcIndex = 0;
            var destIndex = 0;
            while (srcIndex < this._keys.length) {
                var key = this._keys[srcIndex];
                if (!(P.Util.Structs.Map._hasKey(seen, key))) {
                    this._keys[destIndex++] = key;
                    seen[key] = 1;
                }
                srcIndex++;
            }
            this._keys.length = destIndex;
        }
    };


    /**
    * Returns the value for the given key.  If the key is not found and the default
    * value is not given this will return {@code undefined}.
    * @param {*} key The key to get the value for.
    * @param {*=} opt_val The value to return if no item is found for the given
    *     key, defaults to undefined.
    * @return {*} The value for the given key.
    */
    P.Util.Structs.Map.prototype.get = function (key, opt_val) {
        if (P.Util.Structs.Map._hasKey(this._map, key)) {
            return this._map[key];
        }
        return opt_val;
    };


    /**
    * Adds a key-value pair to the map.
    * @param {*} key The key.
    * @param {*} value The value to add.
    */
    P.Util.Structs.Map.prototype.set = function (key, value) {
        if (!(P.Util.Structs.Map._hasKey(this._map, key))) {
            this._count++;
            this._keys.push(key);
            // Only change the version if we add a new key.
            this._version++;
        }
        this._map[key] = value;
    };


    /**
    * Adds multiple key-value pairs from another P.Util.Structs.Map or Object.
    * @param {Object} map  Object containing the data to add.
    */
    P.Util.Structs.Map.prototype.addAll = function (map) {
        var keys, values;
        if (map instanceof P.Util.Structs.Map) {
            keys = map.getKeys();
            values = map.getValues();
        } else {
            keys = P.Util.Object.getKeys(map);
            values = P.Util.Object.getValues(map);
        }
        // we could use goog.array.forEach here but I don't want to introduce that
        // dependency just for this.
        for (var i = 0; i < keys.length; i++) {
            this.set(keys[i], values[i]);
        }
    };


    /**
    * Clones a map and returns a new map.
    * @return {!P.Util.Structs.Map} A new map with the same key-value pairs.
    */
    P.Util.Structs.Map.prototype.clone = function () {
        return new P.Util.Structs.Map(this);
    };


    /**
    * Safe way to test for hasOwnProperty.  It even allows testing for
    * 'hasOwnProperty'.
    * @param {Object} obj The object to test for presence of the given key.
    * @param {*} key The key to check for.
    * @return {boolean} Whether the object has the key.
    * @private
    */
    P.Util.Structs.Map._hasKey = function (obj, key) {
        return Object.prototype.hasOwnProperty.call(obj, key);
    };

}, "0.1", { requires: ["util"] });

