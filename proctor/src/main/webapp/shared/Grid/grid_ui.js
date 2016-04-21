var SVG_NS = "http://www.w3.org/2000/svg";
var XLINK_NS = "http://www.w3.org/1999/xlink";
var GRID_NS = 'http://www.air.org/2010/grid/';

// The grid UI class (uses SVG)
var GridUI = function(svgFile)
{
    this.svgFile = svgFile;

    this.width = 600;
    this.height = 500;

    // reference to svg <object> element
    this._svgObject = null;

    // reference to svg document
    this._svgDoc = null;

    // reference to svg window
    this._svgWin = null;

    // reference to svg root
    this._svgRoot = null;

    this._svgElement = null;

    // current zoom level
    this._zoomLevel = 1;

    // cache for quick dom lookups
    this._domCache = {};

    // timer used for drawing attribute updates
    this._drawTimer = null;

    // a queue of attributes to be drawn when the timer expires
    this._attributesQueue = {};

    // is svg ready to be used (svgweb initialized)
    this.svgReady = false;

    // has the svg file been successfully loaded
    this.svgLoaded = false;

    this._suspendRedrawEnabled = false; // use SVG suspendRedraw()
    this._attributefBatchEnabled = false; // batch attribute updates
    this._attributeBatchDefault = 30; // default wait before processing attribute updates

    // write out extra debug logs
    this._debug = false;

    this._fixedOffset = false;

    // add error logging to public functions (NOTE: firebug doesn't report <object> exceptions..)
    ErrorHandler.wrapFunctions(this,
    [
        '_queueAttributes', '_processAttributes', '_suspend', '_assignAttributes', '_createElementFromJSON',
        'createPoint', 'createSnapPoint', 'movePoint', 'createLine', 'moveLine'
    ]);
};

// does this browser support native SVG
GridUI.prototype._hasNative = function()
{
	return document.implementation.hasFeature('http://www.w3.org/TR/SVG11/feature#BasicStructure', '1.1');
};

// was the svgweb library loaded
GridUI.prototype._hasSVGWeb = function() { return (typeof(svgweb) == 'object'); };

GridUI.prototype._loadedObject = function(svgObject)
{
    this._svgObject = svgObject;
    this._svgDoc = this._svgObject.contentDocument;
    this._svgWin = this._svgDoc.defaultView;
    this._svgElement = this._svgDoc.documentElement;
    this._svgRoot = this._svgDoc.rootElement;

    /*
    // set viewbox
    var offset = -0.5;
    var viewBox = offset + ' ' + offset + ' ' + this.width + ' ' + this.height;
    this._svgRoot.setAttribute('viewBox', viewBox);

	// set svg object height/width
    this._svgObject.setAttribute('width', this.width);
    this._svgObject.setAttribute('height', this.height);

	// get viewbox
    viewbox = this._svgElement.getAttribute('viewBox')
    */

    this.svgLoaded = true;
    this.fireLazy('loaded');
};

// create a new svg object within a DOM element
GridUI.prototype._createObject = function(element)
{
    var svgID = element.id + 'Container';

    // get elements document and window
    var elementDoc = element.ownerDocument;
    var elementWin = elementDoc.parentWindow || elementDoc.defaultView;
    var isFrame = (window != elementWin);

    // FRAME FIX: if elements window does not have svgweb then copy it from parent parent window
    if (this._hasSVGWeb() && elementWin && !elementWin.svgweb)
    {
        elementWin.svgweb = window.svgweb;
    }

    // create <object>
    // NOTE: We use the top level document to create element since that is where svgweb is loaded.
    // WARNING: DO NOT CHANGE THIS
    var svgObject = document.createElement('object', true);
    svgObject.setAttribute('id', svgID);
    svgObject.setAttribute('name', svgID);
    svgObject.setAttribute('type', 'image/svg+xml');
    svgObject.setAttribute('data', this.svgFile);

    var ui = this;

    // when svg file has loaded this gets called
    var loaded = function() { ui._loadedObject(this); };
    
    // assign dom load event
    if (this._hasSVGWeb())
    {
        svgObject.addEventListener('SVGLoad', loaded, false);
        svgweb.appendChild(svgObject, element);

        // FRAME FIX: fix for Flash in iframes
        if (isFrame) this._patchSvgwebObject(svgID);
    }
    else
    {
        svgObject.addEventListener('load', loaded, false);
        element.appendChild(svgObject);
    }
};

// call this function after appending a element to the DOM for svgweb if you are using iframes
GridUI.prototype._patchSvgwebObject = function(svgID)
{
    // get this elements svgweb handler and internal object
    var handler = svgweb.handlers[svgID];
    var svgObject = handler._svgObject;

    // this function is from svgweb code directly except modified to use the elements doc/win 
    svgObject._onFlashLoaded = function(msg)
    {
        var svgNode = this._handler._svgObject._svgNode
        var svgDoc = svgNode.ownerDocument;
        var svgWin = svgDoc.defaultView;

        this._handler.flash = svgDoc.getElementById(this._handler.flashID);

        if (this._savedParams.length) 
        {
            for (var i = 0; i < this._savedParams.length; i++) 
            {
                var param = this._savedParams[i];
                this._handler.flash.appendChild(param);
                param = null;
            }

            this._savedParams = null;
        }

        this._handler.flash.top = this._handler.flash.parent = svgWin;
        this._swfLoaded = true;

        if (!YAHOO.env.ua.ie || this._htcLoaded) 
        {
            this._onEverythingLoaded();
        }
    };

};

// load the svg file into child of a div
GridUI.prototype.render = function(id)
{
	var parent = YAHOO.util.Dom.get(id);
	var ui = this;
	
	var ready = function()
	{
		ui.svgReady = true;
		ui._createObject(parent);
	};
	
	// check if the svg library is loaded
	if (this._hasSVGWeb())
	{
		if (svgweb.pageLoaded)
		{
			ready();
		}
		else
		{
			window.onsvgload = function() 
			{
				ready();
			};
		}
	}
	// if there is no svg library then use native svg if available
	else if (this._hasNative())
	{
		ready();
	}
	else
	{
		throw new Error('This browser does not have support for SVG');
	}
};

// get offset for a single element
GridUI.prototype._getOffset = function(element)
{
    if (!element || typeof element.getClientRects != 'function')
    {
        return { top: 0, left: 0 };
    };

    var top = element.getClientRects()[0].top;
    var left = element.getClientRects()[0].left;
    return { top: top, left: left };
}

// Get offset for a single element (NOTE: not used right now, but maybe in the future)
// Helpful links:
// jquery: http://github.com/jquery/jquery/tree/master/src/
// jquery-ui: http://code.google.com/p/jquery-ui/
GridUI.prototype._getOffsetAdv = function(elem)
{
    if (!("getBoundingClientRect" in document.documentElement))
    {
        return { top: 0, left: 0 };
    }

    // original code: http://github.com/jquery/jquery/blob/master/src/offset.js
    var boxModel = true;
    var box = elem.getBoundingClientRect();
    var doc = elem.ownerDocument;
    var body = doc.body;
    var docElem = doc.documentElement;
    var clientTop = docElem.clientTop || body.clientTop || 0;
    var clientLeft = docElem.clientLeft || body.clientLeft || 0;
    var top = box.top + (self.pageYOffset || boxModel && docElem.scrollTop || body.scrollTop) - clientTop;
    var left = box.left + (self.pageXOffset || boxModel && docElem.scrollLeft || body.scrollLeft) - clientLeft;
    return { top: top, left: left };
}

// fix offset for the element passed in and all of that elements parent iframes
GridUI.prototype._fixOffset = function()
{
    // check if we already fixed the offset for this
    if (this._fixedOffset) return;

    // function for getting an elements <iframe>
    var getElementFrame = function()
    {
        var doc = element.ownerDocument;
        var win = doc.parentWindow || doc.defaultView;
        return win.frameElement;
    }

    // current element
    var element = this._svgObject;

    // add all the elements to fix offset on into a collection
    var elements = [];

    do
    {
        elements.push(element);
    }
    while (element = getElementFrame())

    // reverse the array so we start with the top level element first
    elements.reverse();

    // figure out the offset required to place the <object> tag in such a way where there is no fractional offset in relation to the parent iframes
    var top = 0, left = 0;

    for (var i = 0; i < elements.length; i++)
    {
        // get elements document offset
        var offset = this._getOffset(elements[i]);

        // round to get fractional difference and add that
        top += (Math.ceil(offset.top) - offset.top);
        left += (Math.ceil(offset.left) - offset.left);
    }

    // check if any offsets to apply
    if (top > 0) YAHOO.util.Dom.setStyle(this._svgObject, 'margin-top', top + 'px');
    if (left > 0) YAHOO.util.Dom.setStyle(this._svgObject, 'margin-left', left + 'px');

    // mark as being completed
    this._fixedOffset = true;
};

/*******************************************************************************************/

// get elements by tag name
GridUI.prototype._getElements = function(tagName)
{
    if (this._svgDoc == null) return null;
    return this._svgDoc.getElementsByTagNameNS(SVG_NS, tagName);
}
	
// get element by id
GridUI.prototype._getElement = function(id)
{
	if (!id) return null;
	if (id.nodeType) return id; // already a dom element
	
	if (typeof id == 'string')
	{
		var element = this._domCache[id];
		
		if (element) return element;
		else if (this._svgDoc) return this._svgDoc.getElementById(id)
	}
	
	return null;
};

// add attributes that you want to draw to the screen
GridUI.prototype._queueAttributes = function(node, attrs)
{
	var current = this._attributesQueue[node.id];
	
	if (!current)
	{
		current = { node: node, attrs: {}};
		this._attributesQueue[node.id] = current;	
	}

	for (var name in attrs) 
	{
		if (node.id && name == 'id') continue; // skip id attribute if it is already on node
		current.attrs[name] = attrs[name];
	}
};
	
// apply any attributes in the queue to svg dom
GridUI.prototype._processAttributes = function()
{
    var handle = null;

    // suspend native svg
    if (this._suspendRedrawEnabled)
    {
        try
        {
            handle = this._svgRoot.suspendRedraw(10000);
        }
        catch (ex)
        {
            // ignore errors but stop processing attributes
            return;
        }
    }

    // apply attributes to dom nodes
    for (var id in this._attributesQueue)
    {
        var cache = this._attributesQueue[id];
        var node = cache.node;
        var attrs = cache.attrs;

        for (var name in attrs)
        {
            var value = attrs[name];
            node.setAttribute(name, value);
        }
    }

    // resume native svg
    if (handle != null)
    {
        this._svgRoot.unsuspendRedraw(handle);
    }

    // clear queue
    this._attributesQueue = {};
};
	
// suspend svg drawing
GridUI.prototype._suspend = function(wait)
{
	if (this._drawTimer != null) return;
	
	var ui = this;
	
	this._drawTimer = setTimeout(function()
	{
		ui._processAttributes();
		ui._drawTimer = null;
	}, wait);
};
	
// assign attributes to an alement using json
GridUI.prototype._assignAttributes = function(node, attrs, wait)
{
    var node = this._getElement(node);

    // check if element exists
    if (!node) return false;

    this._queueAttributes(node, attrs);

    // set the wait time
    wait = Lang.isNumber(wait) ? wait : this._attributeBatchDefault;

    // if this node doesn't have an ID yet then do not batch it
    if (this._attributeBatchEnabled && wait > 0 && node.id)
    {
        // batch for later..
        this._suspend(wait);
    }
    else
    {
        // update right away..
        this._processAttributes();
    }

    return true;
};

// add element to the dom cache
GridUI.prototype._addCache = function(element) { if (element.id) this._domCache[element.id] = element; };

// remove an element from the dom cache
GridUI.prototype._removeCache = function(element) { if (element.id) delete(this._domCache[element.id]); };
	
// create an element using json
GridUI.prototype._createElementFromJSON = function(data) 
{
	var shape = this._getElement(data.attr.id);
	
	if (!shape)
	{
		shape = this._svgDoc.createElementNS(SVG_NS, data.element);
	}
	
	this._assignAttributes(shape, data.attr, 0);

	if (this._debug && shape.id)
	{
		logger.debug('Created \'{nodeName}\' with id \'{id}\'', shape);
	}
	
	this._addCache(shape);
	
	return shape;
};

// append an element to a parent id
GridUI.prototype._appendElement = function(parentID, element)
{
	var parent = this._getElement(parentID);
	
	if (!parent)
	{
		logger.warn('Failed to append the element \'' + element.id + '\' because the parent \'' + parentID + '\' was not found.')
		return false;
	}

	parent.appendChild(element);
	
	// Hack for IE -- SVG in Flash does not appear to propage parent group styles to children
	// We read any parent styles and attempt to apply to children when flash renderer is being used
	if(typeof(svgweb) != 'undefined' && svgweb.getHandlerType() == 'flash')
	{
    	if(parent.getAttribute('style') != '')
    	{
    	   this._assignAttributes(element,
            {
                'style': parent.getAttribute('style')
            }, 0);
        }
    }
	
	return true;
};

GridUI.prototype._removeChildren = function(parentID)
{
	var parent = this._getElement(parentID);

	while (parent.firstChild)
	{
		parent.removeChild(parent.firstChild);
	}
};

// remove an element
GridUI.prototype.removeElement = function(id)
{
    var element = this._getElement(id);

    if (!element)
    {
        logger.warn('Cannot remove the element \'' + id + '\' because it does not exist');
        return false;
    }

    // NOTE: if there is no parentNode then this element is not attached to the dom and is already removed
    if (element.parentNode != null)
    {
        try
        {
            element.parentNode.removeChild(element);
        }
        catch (ex)
        {
            logger.error(ex.message);
        }
    }

    this._removeCache(element);

    if (this._debug) logger.debug('Removed \'{nodeName}\' with id \'{id}\'', element);
    
    return true;
};
	
// a helper function for getting a svg elements main details
// TODO: try and remove this function
GridUI.prototype._parseElementXY = function(el)
{
    var el = this._getElement(el);

    var getFloat = function(attrib)
    {
        var value = el.getAttribute(attrib);
        return (value == null) ? 0 : parseFloat(value);
    };

    var data = {};

    if (el.nodeName == 'circle')
    {
        data.x = getFloat('cx');
        data.y = getFloat('cy');
        data.radius = getFloat('r');
    }
    else if (el.nodeName == 'line')
    {
        data.x1 = getFloat('x1');
        data.y1 = getFloat('y1');
        data.x2 = getFloat('x2');
        data.y2 = getFloat('y2');
    }
    else if (el.nodeName == 'image' || el.nodeName == 'rect')
    {
        data.x = getFloat('x');
        data.y = getFloat('y');
        data.width = getFloat('width');
        data.height = getFloat('height');
    }
    else
    {
        data.x = getFloat('x');
        data.y = getFloat('y');
    }

    data.thickness = getFloat('stroke-width');

    return data;
};

// get the height and width of the entire grid container
GridUI.prototype.getResolution = function()
{
	if (!this._svgRoot) return null;
	
	return {
	    width: this._svgElement.getAttribute("width") * 1,
	    height: this._svgElement.getAttribute("height") * 1,
	    zoom: this._zoomLevel
	};
};

// zoom the grid
GridUI.prototype.zoom = function(scale)
{
    // save scale
    this._zoomLevel = scale;

    // zoom original width/height
    var zoomedWidth = Math.round(this.width * scale);
    var zoomedHeight = Math.round(this.height * scale);

    // set width/height on svg element
    if (this._svgElement && this._svgElement.width.baseVal)
    {
        this._svgElement.width.baseVal.value = zoomedWidth;
        this._svgElement.height.baseVal.value = zoomedHeight;
    }

    // set width/height on svg container
    if (this._svgObject)
    {
        this._svgObject.width = zoomedWidth;
        this._svgObject.height = zoomedHeight;
    }

    // set zoom scale
    var groupWrapper = this._getElement('groupWrapper');

    if (groupWrapper)
    {
        this._assignAttributes(groupWrapper,
        {
            'transform': 'translate(0.5, 0.5) scale(' + scale + ')'
        }, 0);
    }
};

// move an element id to top layer
GridUI.prototype.bringToFront = function(id)
{
	var element = this._getElement(id);
	if (!element || element.nodeName == 'image') return false;
	
	var parent = element.parentNode;
	if (!parent) return false;

	parent.removeChild(element);
	parent.appendChild(element);
	return true;
};

// get the translated document XY coodinates based on an element
GridUI.prototype.translateElement = function(id, x, y)
{
	var element = this._getElement(id);

	if (!element)
	{
		logger.warn('Could not translate the element \'' + id + '\' because it was not found.')
		return null;
	}

	// svg matrix
	var matrix = element.getCTM().inverse();

	// convert window points through the matrix to get the document coordinates
	x = matrix.a * x + matrix.c * y + matrix.e;
	y = matrix.b * x + matrix.d * y + matrix.f;
	
	return {x: x, y: y};
};

/* FEEDBACK */

GridUI.prototype.setFeedbackText = function(text)
{
    var feedbackText = this._getElement('feedback');
    
    // clear current text
    this._removeChildren(feedbackText);
    
    // add new text
    var text = this._svgDoc.createTextNode(text);
    feedbackText.appendChild(text);
};
    
/* PALETTE */

// this function will organize the current palette images based on height
GridUI.prototype._updatePaletteLayout = function()
{
	var paletteContainer = this._getElement('backgroundPalette');
	var paletteContainerXY = this._parseElementXY(paletteContainer);
	var paletteImages = this._getElement('paletteImages').childNodes;

	var spacing = 3;
	var x = 2;
	var y = spacing;
		
	for(var i = 0; i < paletteImages.length; i++)
	{
		var paletteImage = paletteImages[i];
		
		this._assignAttributes(paletteImage, {
			'x': x,
			'y': y
		}, 0);
		
		// set the starting position for the next image
		var imageXY = this._parseElementXY(paletteImage);
		y = y + imageXY.height + spacing;				
	}
};

GridUI.prototype.showPalette = function()
{
    this._assignAttributes(this._getElement('groupPalette'),
    {
        'transform': ''
    }, 0);
};

GridUI.prototype.hidePalette = function()
{
    this._assignAttributes(this._getElement('groupPalette'),
    {
        'transform': 'translate(-1000, 0)'
    }, 0);
};

GridUI.prototype.showToolbar = function()
{
    this._assignAttributes(this._getElement('groupToolbar'),
    {
        'transform': 'translate(80, 0)'
    }, 0);
};

GridUI.prototype.hideToolbar = function()
{
    this._assignAttributes(this._getElement('groupToolbar'),
    {
        'transform': 'translate(-1000, 0)'
    }, 0);
};

GridUI.prototype.createPaletteImage = function(id, width, height, url)
{
	var paletteElement = this._createElementFromJSON({
		'element': 'image',
		'attr': {
			'id': id,
			'width': width,
			'height': height,
			'transform': 'translate(-0.5, -0.5)'
		}
	});
	
	paletteElement.setAttributeNS(XLINK_NS, 'xlink:href', url);
		
	this._appendElement('paletteImages', paletteElement);
	this._updatePaletteLayout();
	
	return paletteElement;
};

// clone a palette image for dragging
GridUI.prototype.clonePaletteImage = function(id, cloneID)
{
	var paletteElement = this._getElement(id);
	var clonedElement = paletteElement.cloneNode(true);
	clonedElement.id = cloneID;

	this._appendElement('paletteDragging', clonedElement);
	return clonedElement;
};

// call this when dragging the palette image
GridUI.prototype.movePaletteImage = function(id, x, y)
{
	var clonedElement = this._getElement(id);

	this._assignAttributes(clonedElement,
    {
        'x': x,
        'y': y
    });
};

GridUI.prototype.removePaletteImage = function(id)
{
	this.removeElement(id);
	this._updatePaletteLayout();
};

GridUI.prototype.selectPaletteImage = function(paletteID)
{
    var paletteElement = this._getElement(paletteID);
    var imageXY = this._parseElementXY(paletteElement);

    var selectedPalette = this._getElement('selectedPalette'); // rect used for selection

    this._assignAttributes(selectedPalette,
    {
		'x': imageXY.x - 2,
		'y': imageXY.y - 2,
		'width': 75,
		'height': imageXY.height + 4
    }, 0);
    
    selectedPalette.style.display = '';
};

// remove selected style
GridUI.prototype.deselectPaletteImage = function()
{
    var selectedPalette = this._getElement('selectedPalette'); // rect used for selection
    selectedPalette.style.display = 'none';
};

/* CANVAS */

// get the width/height of the canvas
GridUI.prototype.getCanvasResolution = function() 
{
	var backgroundCanvas = this._getElement('backgroundCanvas');
	
	return {
		width: backgroundCanvas.getAttribute("width") * 1,
		height: backgroundCanvas.getAttribute("height") * 1
	};
};

// removes all the grid lines
GridUI.prototype.clearGridLines = function()
{
	this._removeChildren('gridlines');
};

GridUI.prototype.createGridLines = function(spacing)
{
	// clear any existing lines
	this.clearGridLines();
	
	var gridLines = this._getElement('gridlines');

	// create function for building lines
	var createLine = function(x1, y1, x2, y2)
	{
		var lineElement = this._createElementFromJSON({
			element: 'line',
			attr: {
				'x1': x1,
				'y1': y1,
				'x2': x2,
				'y2': y2
			}
		});
		
		gridLines.appendChild(lineElement);
	}

	var res = this.getCanvasResolution();
	
	// draw vertical lines
	for (var x = 0; x <= res.width; x += spacing)
	{
		createLine.call(this, x, 0, x, res.height);
	}
	
	// draw horizontal lines
	for (var y = 0; y <= res.height; y += spacing)
	{
		createLine.call(this, 0, y, res.width, y);
	}
};

/* CANVAS DOM API */

// set the canvas cursor (http://www.w3.org/TR/SVG11/interact.html#CursorProperty)
GridUI.prototype.setCanvasCursor = function(type)
{
    var groupCanvas = this._getElement('groupCanvas');

    this._assignAttributes(groupCanvas, {
        'cursor': type
    });
};

GridUI.prototype.setCanvasCustomCursor = function(url, x, y)
{
    var groupCanvas = this._getElement('groupCanvas');
    x = x || 0;
    y = y || 0;

    this._assignAttributes(groupCanvas, {
        'cursor': 'url(\'' + url + '\') ' + x + ' ' + y + ' , crosshair'
    });
};

// set point to its last style
GridUI.prototype.stylePointDefault = function(id)
{
	var pointElement = this._getElement(id);
	
	this._assignAttributes(pointElement, {
		'fill': '',
		'stroke': '',
		'stroke-width': '',
		'opacity': ''
	});
};

// set styling for selecting a point
GridUI.prototype.stylePointSelected = function(id)
{
	var pointElement = this._getElement(id);

	this.bringToFront(pointElement);
	
	this._assignAttributes(pointElement, {
		'opacity': 1,
		'stroke': 'blue',
		'stroke-width': '1'
	});
};

/*
// set styling for selecting a point
GridUI.prototype.stylePointMoving = function(id)
{
	var pointElement = this._getElement(id);
	
	this._assignAttributes(pointElement, {
		'opacity': 0.5
	});
};
*/

GridUI.prototype.createPoint = function(id, x, y)
{
	if (!Lang.isString(id)) throw new Error('Cannot create point invalid id');
	if (!Lang.isNumber(x) || !Lang.isNumber(y)) throw new Error('Cannot create point invalid position');

	// create element
	var pointElement = this._createElementFromJSON({
		"element": "circle",
		"attr": {
			"id": id,
			"cx": x,
			"cy": y,
			"r": 4.6
		}
	});

	this._appendElement('points', pointElement);
	
	return pointElement;
};

GridUI.prototype.createSnapPoint = function(id, x, y, radius)
{
	if (!Lang.isString(id)) throw new Error('Cannot create snap point invalid id');
	if (!Lang.isNumber(x) || !Lang.isNumber(y)) throw new Error('Cannot create snap point invalid position');
	if (!Lang.isNumber(radius)) throw new Error('Cannot create snap point invalid radius');

	// create element
	var pointElement = this._createElementFromJSON({
		"element": "circle",
		"attr": {
			"id": id,
			"cx": x,
			"cy": y,
			"r": radius
		}
	});

	this._appendElement('snapPoints', pointElement);
	
	return pointElement;
};

GridUI.prototype.movePoint = function(id, x, y)
{
	var pointElement = this._getElement(id);
	if (!pointElement) throw new Error('Cannot move the point \'' + id + '\ because it does not exist');
	
	this._assignAttributes(pointElement, {
		'cx': x,
		'cy': y,
		'opacity': 1
	});
	
	return pointElement;
};

// a dotted line used for helping
GridUI.prototype.updateHelperLine = function(id, x1, y1, x2, y2)
{
    var mergeLine = this._getElement(id);

    if (!mergeLine)
    {
        // create element
        mergeLine = this._createElementFromJSON(
        {
            'element': 'line',
            'attr': 
            {
                'id': id,
                'fill': 'none',
                'stroke': 'green',
                'stroke-width': '1',
                'opacity': '0.7',
                'stroke-dasharray': '3, 3'
            }
        });

        this._appendElement('mergeLines', mergeLine);
    }

    this._assignAttributes(mergeLine, 
    {
        'x1': x1,
        'y1': y1,
        'x2': x2,
        'y2': y2
    });
};

GridUI.prototype.createLine = function(id, x1, y1, x2, y2)
{
	if (!Lang.isString(id)) throw new Error('Line id is invalid = ' + id);
	if (!Lang.isNumber(x1) || !Lang.isNumber(y1)) throw new Error('Line source position is invalid');
	if (!Lang.isNumber(x2) || !Lang.isNumber(y2)) throw new Error('Line source position is invalid');
	
	// create element
	var lineElement = this._createElementFromJSON({
		"element": "line",
		"attr": {
			'id': id,
			'x1': x1,
			'y1': y1,
			'x2': x2,
			'y2': y2
		}
	});
	
	this._appendElement('lines', lineElement);
	
	return lineElement;
};

GridUI.prototype.moveLine = function(id, x1, y1, x2, y2)
{
	var lineElement = this._getElement(id);
	if (!lineElement) throw new Error('Cannot move the line ' + id + ' because it does not exist');

	this._assignAttributes(lineElement, {
		'x1': x1,
		'y1': y1,
		'x2': x2,
		'y2': y2
	});
	
	return lineElement;		
};

// get the SVG path for the arrow (start where point is, draw top, to bottom, to where point is
GridUI.prototype._getArrowPath = function(x, y)
{
	var size = 6;
	return 'M' + x + ' ' + y + ' L' + (x - size) + ' ' + (y - size) + ' M' + (x - size) + ' ' + (y + size) + ' L' + x + ' ' + y;
};

GridUI.prototype.createArrow = function(id, x, y, angle)
{
	var path = this._getArrowPath(x, y);
	
	var element = this._createElementFromJSON({
		'element': 'path',
		"attr": {
			'id': id,
			'd': path,
			'transform': 'rotate(' + angle + ', ' + x + ', ' + y + ')'
		}
	});
		
	// add arrow to dom
	this._appendElement('arrows', element);
	
	return element;
};

GridUI.prototype.moveArrow = function(id, x, y, angle)
{
	var arrowElement = this._getElement(id);
	if (!arrowElement) throw new Error('Cannot move the arrow ' + id + ' because it does not exist');
	
	var path = this._getArrowPath(x, y);
	
	this._assignAttributes(arrowElement, {
		'd': path,
		'transform': 'rotate(' + angle + ', ' + x + ', ' + y + ')'
	});
	
	return arrowElement;
};

GridUI.prototype.createBackground = function(id, x, y, width, height, url)
{
	if (!Lang.isNumber(x) || !Lang.isNumber(y)) throw new Error('Cannot create image invalid position');
	if (!Lang.isNumber(width) || !Lang.isNumber(height)) throw new Error('Cannot create image invalid heigh or width');
	if (!Lang.isString(url)) throw new Error('Invalid url');
	
	// <image xlink:href="data:image/;base64, Base64.encode(pngByteArray)" />
	var backgroundElement = this._createElementFromJSON({
		'element': 'image',
		'attr': {
			'id': id,
			'x': x,
			'y': y,
			'width': width,
			'height': height,
			'transform': 'translate(-0.5, -0.5)'
		}
	});

	// set url
	backgroundElement.setAttributeNS(XLINK_NS, 'xlink:href', url);
	
	// add element to dom
	this._appendElement('background', backgroundElement);

	return backgroundElement;
};

GridUI.prototype.createImage = function(id, x, y, width, height, url)
{
	if (!Lang.isNumber(x) || !Lang.isNumber(y)) throw new Error('Cannot create image invalid position');
	if (!Lang.isNumber(width) || !Lang.isNumber(height)) throw new Error('Cannot create image invalid heigh or width');
	if (!Lang.isString(url)) throw new Error('Invalid url');
	
	// <image xlink:href="data:image/;base64, Base64.encode(pngByteArray)" />
	var imageElement = this._createElementFromJSON(
	{
		'element': 'image',
		'attr': {
			'id': id,
			'x': x - Math.round(width / 2),
			'y': y - height,
			'width': width,
			'height': height,
			'transform': 'translate(-0.5, -0.5)'
        }
	});

	// set url
	imageElement.setAttributeNS(XLINK_NS, 'xlink:href', url);
	
	// add element to dom 
	this._appendElement('images', imageElement);
	
	return imageElement;
};

// move a canvas image
GridUI.prototype.moveImage = function(id, x, y, width, height, isSelected)
{
    var imageElement = this._getElement(id);
    if (!imageElement) throw new Error('Cannot move the image ' + id + ' because it does not exist');

    var imageDimensions = { x: x - Math.round(width / 2), y: y - height, width: width, height: height };

    this._assignAttributes(imageElement,
    {
        'x': imageDimensions.x,
        'y': imageDimensions.y
    });

    if (isSelected)
    {
        this.selectImage(imageElement, imageDimensions);
    }

    return imageElement;
};

// style a canvas image to look selected
GridUI.prototype.selectImage = function(id, imageDimensions)
{
	var paletteElement = this._getElement(id);
	
	this.bringToFront(paletteElement);
    
    // get image dimensions (if they are not passed in we look at the dom)
	var imageDimensions = imageDimensions || this._parseElementXY(paletteElement);
	
	var borderID = paletteElement.id + '_border';
	var borderElement = this._getElement(borderID);
	
	// create border if it does not exist
	if (!borderElement)
	{
	    borderElement = this._createElementFromJSON(
		{
			'element': 'rect',
			'attr': 
			{
			    'id': borderID,
				'stroke': 'blue',
				'stroke-width': '2',
				'fill': 'none',
				'opacity': 1
			}
        });

        this._appendElement('imageBorders', borderElement);
	}
	
	// update border
	this._assignAttributes(borderElement, 
	{
		'x': imageDimensions.x - 2,
		'y': imageDimensions.y - 2,
		'width': imageDimensions.width + 4,
		'height': imageDimensions.height + 4
	});
};

GridUI.prototype.deselectImage = function(id)
{
	this.removeElement(id + '_border');
};

ErrorHandler.wrapFunctions(GridUI, 
[
	'createPoint', 'movePoint', 
	'createLine', 'moveLine', 
	'createArrow', 'moveArrow',
	'createImage', 'moveImage'
]);

YAHOO.lang.augmentProto(GridUI, EventLazyProvider);

// make sure svgweb is on the top element (this is not really required but does prevent an exception)
if (window.svgweb && top != window) top.svgweb = window.svgweb;
