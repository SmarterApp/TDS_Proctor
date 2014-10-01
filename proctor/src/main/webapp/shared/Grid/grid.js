var GridState =
{
    Error: -1, // error in any of the grid states
    Uninitialized: 0, // initial grid state
    Initialized: 1, // grid initialization process started, getting SVG
    Created: 2, // grid svg has loaded but events are hooked up yet
    Ready: 3, // all events are hooked up and the grid is ready to have data loaded into it
    Loading: 4, // in process of loading the answer space and response xml
    Loaded: 5 // all xml has loaded and the student can use the grid now
};

/*
 * This is the main grid class and the entry point for everything.
 */

// An instance of the grid
// @element The grid will load into this div
// @svgFile The grid svg file to load
var Grid = function(element, svgFile)
{
    this._element = YAHOO.util.Dom.get(element);
    this._svgFile = svgFile;

    // reference to the grid ui (svg)
    this.ui = null;

    // reference to the question (model)
    this.question = null;

    // reference to the import/export module
    this.importexport = null;

    // reference to the palette panel
    this.palette = null;

    // reference to the toolbar panel
    this.toolbar = null;

    // reference to the feedback panel
    this.feedback = null;

    // reference to the canvas panel
    this.canvas = null;

    // the current panel
    this._currentPanel = null;

    this._currentMode = null;

    this._state = GridState.Uninitialized;
    
    // allow the tab key to toggle between panels
    this.allowTab = true;

    this._debug = false;
    
    // create objects
    this.ui = new GridUI(this._svgFile);
    this.question = new GridQuestion();
    this.importexport = new GridImportExport(this.question);
    this.palette = new GridPalette(this);
    this.toolbar = new GridToolbar(this);
    this.canvas = new GridCanvas(this);

    // add error logging to public functions (NOTE: firebug doesn't report <object> exceptions..)
    ErrorHandler.wrapFunctions(this, ['_svgRendered', 'init', 'update', 'loadItemXml', 'loadResponseXml', 'getResponseXml']);
};

YAHOO.lang.augmentProto(Grid, EventLazyProvider);

Grid.prototype._setState = function(state)
{
    this._state = state;

    var stateName = 'Unknown';

    switch (this._state)
    {
        case GridState.Error: stateName = 'Error'; break;
        case GridState.Uninitialized: stateName = 'Uninitialized'; break;
        case GridState.Initialized: stateName = 'Initialized'; break;
        case GridState.Created: stateName = 'Created'; break;
        case GridState.Ready: stateName = 'Ready'; break;
        case GridState.Loading: stateName = 'Loading'; break;
        case GridState.Loaded: stateName = 'Loaded'; break;
    }

    this.fireLazy('onStateChange', { grid: this, state: state, name: stateName });
};

Grid.prototype.getState = function() { return this._state; };

Grid.prototype._processKeyEvent = function(evt)
{
    evt.preventDefault();
    // evt.stopPropagation();

    // skip processing any keys with a modifier held down
    // if (evt.ctrlKey || evt.altKey || evt.metaKey) return;

    var key = '';

    switch (evt.keyCode)
    {
        case 9: key = 'tab'; break;
        case 13: key = 'enter'; break;
        case 27: key = 'esc'; break;
        case 32: key = 'space'; break;
        case 37: key = 'left'; break;
        case 38: key = 'up'; break;
        case 39: key = 'right'; break;
        case 40: key = 'down'; break;
    }

    var keyEvent =
	{
	    dom: evt,
	    key: key
	};

    var area = this.getArea();

    // ctrl-tab moves to different panel
    if (this.allowTab && evt.ctrlKey && key == 'tab')
    {
        this.ui._svgWin.focus();

        if (area == 'canvas')
        {
            if (this.question.options.showPalette) this.setArea('palette');
            else if (this.question.options.showButtons.length > 0) this.setArea('toolbar');
        }
        else if (area == 'palette') this.setArea('toolbar');
        else if (area == 'toolbar') this.setArea('canvas');

        return;
    }

    if (area == 'canvas' && typeof this.canvas.processKeyEvent == 'function') this.canvas.processKeyEvent(keyEvent);
    else if (area == 'palette' && typeof this.palette.processKeyEvent == 'function') this.palette.processKeyEvent(keyEvent);
    else if (area == 'toolbar' && typeof this.toolbar.processKeyEvent == 'function') this.toolbar.processKeyEvent(keyEvent);

};

// this function gets called when the svg is loaded and rendered
Grid.prototype._svgRendered = function()
{
    var grid = this;

    // focus on grid when hovering over it
    try
    {
        /*        
        this.ui._svgDoc.addEventListener('mousemove', function(evt)
        {
        grid.ui._svgWin.focus();
        }, false);
        
        YAHOO.util.Event.on(this.ui._svgObject, 'mouseenter', function(evt)
        {
        grid.ui._svgWin.focus();
        });

        YAHOO.util.Event.on(this.ui._svgObject, 'mouseleave', function(evt)
        {
            
        });
        */
    }
    catch (ex)
    {
        logger.warn(ex);
    }

    // listen to group clicks for setting the panel current panel
    var groupPalette = this.ui._getElement('groupPalette');
    var groupToolbar = this.ui._getElement('groupToolbar');
    var groupCanvas = this.ui._getElement('groupCanvas');

    groupPalette.addEventListener('mousedown', function(evt)
    {
        grid.setArea('palette');
    }, false);

    groupToolbar.addEventListener('mousedown', function(evt)
    {
        grid.setArea('toolbar');
    }, false);

    groupCanvas.addEventListener('mousedown', function(evt)
    {
        grid.setArea('canvas');
    }, false);

    // attach key handler
    this.ui._svgRoot.addEventListener("keyup", function(evt) { evt.preventDefault(); }, false);
    this.ui._svgRoot.addEventListener("keypress", function(evt) { evt.preventDefault(); }, false);

    this.ui._svgRoot.addEventListener("keydown", function(evt)
    {
        try { grid._processKeyEvent(evt); }
        catch (ex) { logger.error('error processing key event - ' + ex.message); }

    }, false);

    // attach canvas mouse handler
    this.addMouseListener('groupCanvas', function(evt)
    {
        try { grid.canvas.processMouseEvent(evt); }
        catch (ex) { logger.error('error processing mouse action - ' + ex.message); }
    });

    // attach palette mouse handler
    this.addMouseListener('groupWrapper', function(evt)
    {
        try { grid.palette.processPaletteEvents(evt); }
        catch (ex) { logger.error('error processing palette mouse event - ' + ex.message); }
    });

    // process model notifications
    grid.palette.subscribeToModelEvents();
    grid.canvas.subscribeToModelEvents();

    // initialize toolbar
    this.toolbar.init();

    // set the default area and mode
    // this.setArea('canvas');
    this.setMode('move');

    // turn on rendering enhancements only if browser is older than Firefox 3.5
    if (YAHOO.env.ua.gecko < 1.91)
    {
        this.ui._suspendRedrawEnabled = true;
        this.ui._attributeBatchEnabled = true;
    }

    // fire ready event
    setTimeout(function()
    {
        grid._setState(GridState.Ready);
    }, 0);
};

Grid.prototype.getArea = function() { return this._currentPanel; };

// set a panel as being active (pass in NULL to turn them all off)
Grid.prototype.setArea = function(panelName)
{
    if (this._currentPanel == panelName) return false;

    // cancel any actions (moving lines) and deselect points/images
    this.canvas.cancelAction();
    this.canvas.clearSelected();

    // remove focus style
    if (this._currentPanel != null)
    {
        switch (this._currentPanel)
        {
            case 'canvas': this.ui._assignAttributes('backgroundCanvas', { 'stroke': 'black' }); break;
            case 'palette': this.ui._assignAttributes('backgroundPalette', { 'stroke': 'black' }); break;
            case 'toolbar': this.ui._assignAttributes('backgroundToolbar', { 'stroke': 'white' }); break;
        }
    }
    
    // set current panel name
    this._currentPanel = panelName;

    // add focus style
    if (this._currentPanel != null)
    {
        switch (this._currentPanel)
        {
            case 'canvas': this.ui._assignAttributes('backgroundCanvas', { 'stroke': 'blue' }); break;
            case 'palette': this.ui._assignAttributes('backgroundPalette', { 'stroke': 'blue' }); break;
            case 'toolbar': this.ui._assignAttributes('backgroundToolbar', { 'stroke': 'blue' }); break;
        }
    }

    this.fireLazy('onAreaChange', { grid: this, name: panelName });
    return true;
};

Grid.prototype.getMode = function() { return this._currentMode; };

// set the current grid mode ('move', 'delete', 'point', 'connect', 'arrow', 'arrw2')
Grid.prototype.setMode = function(mode)
{
    // BUG 14229: Mouse selects both object and delete action at the same time
    if (this.palette.moving) return;

    // check if anything to change
    if (this._currentMode != null && mode == this._currentMode && mode == 'move') return;

    // check if we are toggling on current mode
    if (mode == this._currentMode)
    {
        // reset to move
        mode = 'move';
    }

    if (this._currentMode) this.toolbar.getButton(this._currentMode).deselect(); // set button 'up'
    this._currentMode = mode;
    this.toolbar.getButton(this._currentMode).select(); // set button 'selected'

    this.canvas.cancelAction();
    this.setModeCursor();
    this.setModeHint();

    this.fireLazy('onModeChange', { grid: this, name: mode });
};

// called externally to initialize the grid
Grid.prototype.init = function()
{
    var grid = this;

    // subscribe to the UI event for when SVG file is loaded
    this.ui.subscribe('loaded', function()
    {
        grid._setState(GridState.Created);
        grid._svgRendered();
    });

    this._setState(GridState.Initialized);
    this.ui.render(this._element);
};

// call this when you want to sync the model options to grid UI
Grid.prototype.update = function()
{
    var options = this.question.options;

    // check if showing grid lines
    if (options.showGridLines) this.ui.createGridLines(options.gridSpacing);
    else this.ui.clearGridLines();

    // check if showing palette
    if (options.showPalette) this.ui.showPalette();
    else this.ui.hidePalette();

    // check if we need to add toolbar buttons
    if (options.showButtons.length > 0)
    {
        this.ui.showToolbar();
    
        var buttonNames = ['move']; // add move manually since it doesn't come in xml
        
        // add buttons in xml
        for (var i = 0; i < options.showButtons.length; i++)
        {
            var buttonName = options.showButtons[i];
            buttonNames.push(buttonName);
        }

        this.toolbar.enableButtons(buttonNames);
    }
    else
    {
        this.ui.hideToolbar();
        this.toolbar.resetButtons();
    }

    // set mode
    this.setMode('move');
};

// load the grid answer space and response
Grid.prototype.loadXml = function(itemXml, responseXml)
{
    if (this._debug) logger.info('LOAD ITEM XML = ' + itemXml);

    this._setState(GridState.Loading);

    var grid = this;

    // load item xml and wait for callback
    this.importexport.loadItem(itemXml, function()
    {
        // update grid options
        grid.update();

        // load response if any
        if (responseXml)
        {
            if (grid._debug) logger.info('LOAD RESPONSE XML = ' + responseXml);
            grid.importexport.loadAnswer(responseXml);
        }

        grid._setState(GridState.Loaded);
        grid.ui.zoom(1);
    });
};

// get the response xml
Grid.prototype.getResponseXml = function()
{
    if (!this.isLoaded()) return null;
    return this.importexport.getAnswerXml();
};

// has the grid finished loading?
Grid.prototype.isLoaded = function()
{
    return (this.getState() == GridState.Loaded);
}

//  Check if xml passed in is different than what is the current response in the grid
Grid.prototype.hasChanged = function(xml)
{
    if (!this.isLoaded()) return false;

    // parse out just the response without the date for comparison purposes (DO NOT SAVE THIS TO DB)
    var cleanXml = function(xml)
    {
        if (xml == null) return '';
        var values = xml.split('DOCTYPE');
        if (values.length > 1) return values[1].split(' ').join('');
        else return xml;
    };
    
    var currentAnswerXml = this.getResponseXml();
    return (cleanXml(currentAnswerXml) != cleanXml(xml));
};

// TODO: This function should validate if the current grid response is valid
Grid.prototype.isValid = function()
{
    // isStudentResponseValid?
    if (this.isLoaded())
    {
        if (this.importexport && this.importexport.isStudentResponseValid()) return true;
    }

    return false;
};

Grid.Hints =
{
    'SetPoint': 'Select locations of points',
    'Connect': 'Select 2 points to connect or press & drag to create & connect points.',
    'Arrow': 'Select 2 points to connect with arrow.',
    'DoubleArrow': 'Select 2 points to connect with double arrow.',
    'Delete': 'Select object to delete.',
    'AddValue': 'Select point or edge to add value',
    'AddLabel': 'Select location of label',
    'AddComponent': '',
    'MotionPending': 'Move object to new location and click where you want it.',
    'DraggingObject': 'Release the mouse button to drop it where you want it.',
    'WaitForDropDragging': 'Release the mouse button to drop it where you want it.',
    'WaitForDrop': 'Click to drop the object where you want it.',
    'None': ' '
};

// get feedback label
Grid.getHint = function(key) 
{ 
    if(typeof(Messages) == 'object')
    {
        // We have internationalized messages
        // The prefix here is match up with the keys used for internationalization
        return Messages.get("GridJS.Label.Hint"+key); 
    }
    return Grid.Hints[key] || ''; 
};

// set feedback label
Grid.prototype.setHint = function(key) { this.ui.setFeedbackText(Grid.getHint(key)); };

Grid.prototype.setModeHint = function()
{
    var mode = this.getMode();

    switch(mode)
    {
        case 'move': this.setHint('None'); break;
        case 'delete': this.setHint('Delete'); break;
        case 'point': this.setHint('SetPoint'); break;
        case 'connect': this.setHint('Connect'); break;
        case 'arrow': this.setHint('Arrow'); break;
        case 'arrw2': this.setHint('DoubleArrow'); break;
        default: this.setHint('');
    }
};

Grid.prototype.setModeCursor = function()
{
    var mode = this.getMode();

    switch(mode)
    {
        case 'move': this.ui.setCanvasCursor('default'); break;                
        case 'delete': this.ui.setCanvasCursor('crosshair'); break;
        case 'arrow': this.ui.setCanvasCursor('pointer'); break; // alias
        case 'arrw2': this.ui.setCanvasCursor('pointer'); break; // alias
        case 'point': this.ui.setCanvasCursor('pointer'); break; // pointer
        case 'connect': this.ui.setCanvasCursor('pointer'); break;
        default: this.ui.setCanvasCursor('default');
    }
};

// Use this to assign normalized mouse handlers to an element
// events: mousedown, mouseup, mousemove, dragstart, drag, dragend
Grid.prototype.addMouseListener = function(id, handler)
{
    // get group and check if it exists
    var group = this.ui._getElement(id);
    if (!group) return false;

    // state variables
    var isClicked = false;
    var isDragging = false;
    var clickedPosition = null;

    var mouseDOMEvent = function(name, evt)
    {
        var clientX = evt.clientX - 0.5;
        var clientY = evt.clientY - 0.5;

        // get position
        var currentPosition = this.ui.translateElement(group, clientX, clientY);
        // console.log('mouseDOMEvent: ' + id + ' (' + currentPosition.x + ',' + currentPosition.y + ')');

        // if mousing down then save this as the clicked point
        if (name == 'mousedown') clickedPosition = currentPosition;

        var callHandler = function(eventName)
        {
            // create new event
            var svgEvent =
			{
			    name: eventName,
			    target: evt.target,
			    raw: evt,
			    currentPosition: currentPosition,
			    clickedPosition: clickedPosition
			};

            handler(svgEvent);
        };

        if (name == 'mousedown')
        {
            if (isDragging)
            {
                // NOTE: this is for if you start dragging, move your mouse off the screen, release mouse button, then move mouse back to canvas and click
                callHandler('dragend');
                isClicked = false;
                isDragging = false;
            }
            else
            {
                isClicked = true;
                callHandler('mousedown');
            }
        }

        if (name == 'mousemove')
        {
            if (isClicked)
            {
                if (!isDragging)
                {
                    callHandler('dragbegin');
                    isDragging = true;
                }

                callHandler('drag');
            }
            else
            {
                callHandler('mousemove');
            }
        }

        if (name == 'mouseup')
        {
            if (isDragging)
            {
                callHandler('dragend');
                isDragging = false;
            }
            else if (isClicked)
            {
                callHandler('mouseup');
            }

            isClicked = false;
            clickedPosition = null;
        }

        evt.preventDefault();

        if (name == 'mousedown')
        {
            this.ui._svgWin.focus();
        }
    };

    var grid = this;

    group.addEventListener('mousedown', function(evt) { mouseDOMEvent.call(grid, 'mousedown', evt); }, false);
    group.addEventListener('mousemove', function(evt) { mouseDOMEvent.call(grid, 'mousemove', evt); }, false);
    group.addEventListener('mouseup', function(evt) { mouseDOMEvent.call(grid, 'mouseup', evt); }, false);
};
