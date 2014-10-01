var Lang = YAHOO.util.Lang;

var ErrorHandler =
{
    report: function(name, ex)
    {
        if (typeof console != 'object') return;
        console.error(name + ' ' + ex.name + ': ' + ex.message + ' - ' + ex.fileName + ' (line ' + ex.lineNumber + ')');
    },

    wrapClass: function(fn)
    {
        var cache = fn;

        fn = function()
        {
            try
            {
                return new cache.apply(context, arguments);
            }
            catch (ex)
            {
                ErrorHandler.log(name, ex);
                throw ex;
            }
        }
    },

    wrapFunction: function(context, name)
    {
        var fn = context[name];

        context[name] = function()
        {
            try
            {
                return fn.apply(context, arguments);
            }
            catch (ex)
            {
                ErrorHandler.report(name, ex);
                throw ex;
            }
        }
    },

    wrapFunctions: function(context, names)
    {
        names.forEach(function(name)
        {
            ErrorHandler.wrapFunction(context, name);
        });
    }
}

// lazy events helper
var EventLazyProvider = function() {};

EventLazyProvider.prototype =
{
	_scope: null,
	
	setScope: function(obj) { this._scope = obj; },
	
	fireLazy: function(name, obj)
	{
		if (!this.hasEvent(name))
		{
			if (this._scope) this.createEvent(name, { scope: this._scope });
			else this.createEvent(name);
		}
		
		return this.fireEvent(name, obj);		
	}
};

// NOTE: I am not sure why but a class has to be created like the one above for augmentProto to work here
YAHOO.lang.augmentProto(EventLazyProvider, YAHOO.util.EventProvider);

// logging
var Logger = function(prefix)
{
	var enabled = true;
	
	this.enable = function() { enabled = true; }
	this.disable = function() { enabled = false; }
	
	var log = function(level, message, params)
	{
		if (level == 'error') level = 'warn';
		if (!enabled) return;
		if (!top.console || !top.console[level]) return;
		if (prefix) message = prefix + message;
		if (params) message = YAHOO.lang.substitute(message, params);
		top.console[level](message);		
	}
	
	this.debug = function(message, params) { log('debug', message, params); };
	this.info = function(message, params) { log('info', message, params); };
	this.warn = function(message, params) { log('warn', message, params); };
	this.error = function(message, params) { log('error', message, params); };
};

var logger = new Logger('GRID: ');

/* ARRAY FUNCTIONS */

// FOREACH: Executes a provided function once per array element.
if (!Array.prototype.forEach) {
	Array.prototype.forEach = function(fun /*, thisp*/) { var len = this.length >>> 0; if (typeof fun != "function") throw new TypeError(); var thisp = arguments[1]; for (var i = 0; i < len; i++) { if (i in this) fun.call(thisp, this[i], i, this); } };
}

// FILTER: Creates a new array with all elements that pass the test implemented by the provided function.
if (!Array.prototype.filter) {
	Array.prototype.filter = function(fun /*, thisp*/) { var len = this.length >>> 0; if (typeof fun != "function") throw new TypeError(); var res = []; var thisp = arguments[1]; for (var i = 0; i < len; i++) { if (i in this) { var val = this[i]; if (fun.call(thisp, val, i, this)) res.push(val); } } return res; };
}

// EVERY: Tests whether all elements in the array pass the test implemented by the provided function.
if (!Array.prototype.every) {
	Array.prototype.every = function(fun /*, thisp*/) { var len = this.length >>> 0; if (typeof fun != "function") throw new TypeError(); var thisp = arguments[1]; for (var i = 0; i < len; i++) { if (i in this && !fun.call(thisp, this[i], i, this)) return false; } return true; };
}

// SOME: Tests whether some element in the array passes the test implemented by the provided function.
if (!Array.prototype.some) {
	Array.prototype.some = function(fun /*, thisp*/) { var i = 0, len = this.length >>> 0; if (typeof fun != "function") throw new TypeError(); var thisp = arguments[1]; for (; i < len; i++) { if (i in this && fun.call(thisp, this[i], i, this)) return true; } return false; };
}

// MAP: Creates a new array with the results of calling a provided function on every element in this array.
if (!Array.prototype.map) { 
	Array.prototype.map = function(fun /*, thisp*/) { var len = this.length >>> 0; if (typeof fun != "function") throw new TypeError(); var res = new Array(len); var thisp = arguments[1]; for (var i = 0; i < len; i++) { if (i in this) res[i] = fun.call(thisp, this[i], i, this); } return res; };
}
