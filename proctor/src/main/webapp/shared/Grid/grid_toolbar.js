/* BUTTON CLASS */

// up, over, selected, selOver
var GridButtonState =
{
    Up: 0,
    Selected: 1,
    Over: 2,
    SelectedOver: 3
};

function GridButton(toolbar, name)
{
    this.name = name;
    this.width = 100;
    
    this.toolbar = toolbar;
    this.grid = toolbar.grid;
    this.ui = toolbar.ui;

    this._visible = true;
    this._state = GridButtonState.Up;

    this._iconFillElements = [];
    this._iconStrokeElements = [];

    this._init();
};

GridButton.prototype._init = function()
{
    var buttonGroup = this.getGroup();

    for (var i = 0; i < buttonGroup.childNodes.length; i++)
    {
        var element = buttonGroup.childNodes[i];

        if (element.nodeType == 1)
        {
            if (element.getAttribute('fill') == '#ffffff') this._iconFillElements.push(element);
            if (element.getAttribute('stroke') == '#ffffff') this._iconStrokeElements.push(element);
        }
        
        if (element.nodeName == 'text') //<text> node
        {
            if(typeof(Messages) == 'object')
            {
                // We may have internationalized messages     
                // The prefix here is match up with the keys used for internationalization
                var key = "GridSVG.Label.button_"+element.textContent.replace(" ","_");
                var alternateLabel = Messages.get(key);  
                // check to make sure an alternate label was found. If not, leave the text alone                          
                if(alternateLabel != key) {
                    element.textContent = alternateLabel;
                }                     
            }
        }
    }
};

// get the button group <g>
GridButton.prototype.getGroup = function()
{
    return this.ui._getElement('button_' + this.name);
};

// get the button container <path>
GridButton.prototype.getContainer = function()
{
    var buttonGroup = this.getGroup();

    var path = null;
    for (var i = 0; i < buttonGroup.childNodes.length; i++)
    {
        var node = buttonGroup.childNodes[i];

        if (node.nodeName == 'path')
        {
            path = node;
            break;
        }
    }

    return path;
};

GridButton.prototype.show = function()
{
    var buttonGroup = this.getGroup();
    buttonGroup.style.display = '';
    this._visible = true;
};

GridButton.prototype.hide = function()
{
    var buttonGroup = this.getGroup();
    buttonGroup.style.display = 'none';
    this._visible = false;
};

GridButton.prototype.isVisible = function() { return this._visible; };

GridButton.prototype._setIconColor = function(color)
{
    for (var i = 0; i < this._iconFillElements.length; i++)
    {
        var element = this._iconFillElements[i];
        element.setAttribute('fill', color);
    }

    for (var i = 0; i < this._iconStrokeElements.length; i++)
    {
        var element = this._iconStrokeElements[i];
        element.setAttribute('stroke', color);
    }
};

GridButton.prototype.deselect = function()
{
    this._state = GridButtonState.Up;
    this._setIconColor('#ffffff');
    
    var container = this.getContainer();
    container.setAttribute('fill', 'url(#buttons_background_up)');
};

GridButton.prototype.select = function()
{
    this._state = GridButtonState.Selected;
    this._setIconColor('#1f5181');

    var container = this.getContainer();
    container.setAttribute('fill', 'url(#buttons_background_selected)');
};

function GridButtonCircle(toolbar, name)
{
    GridButtonCircle.superclass.constructor.call(this, toolbar, name);
    this.width = 18;
};

Lang.extend(GridButtonCircle, GridButton);

// get the button container <path>
GridButtonCircle.prototype.getContainer = function()
{
    var buttonGroup = this.getGroup();

    var circle = null;
    for (var i = 0; i < buttonGroup.childNodes.length; i++)
    {
        var node = buttonGroup.childNodes[i];

        if (node.nodeName == 'circle')
        {
            circle = node;
            break;
        }
    }

    return circle;
};

/* TOOLBAR CLASS */

function GridToolbar(grid)
{
    this.grid = grid;
    this.ui = grid.ui;

    this._buttonHash = {};
    this._buttons = [];
};

GridToolbar.prototype.init = function()
{
    var buttonNames = ['move', 'delete', 'point', 'connect', 'arrow', 'arrw2']; // , 'label', 'value'

    var grid = this.grid;

    for (var i = 0; i < buttonNames.length; i++)
    {
        var buttonName = buttonNames[i];

        var button = null;

        switch (buttonName)
        {
            case 'move': button = new GridButtonCircle(this, buttonName); break;
            default: button = new GridButton(this, buttonName); break;
        }

        button.hide();

        this._buttons.push(button);
        this._buttonHash[buttonName] = button;

        (function(button)
        {
            var buttonGroup = button.getGroup();

            buttonGroup.addEventListener('click', function()
            {
                grid.setMode(button.name);
            }, false);

        })(button);
    }
};

GridToolbar.prototype.getButton = function(buttonName) { return this._buttonHash[buttonName]; };

GridToolbar.prototype.getButtons = function() { return this._buttons; };

GridToolbar.prototype.getVisibleButtons = function()
{
    var visibleButtons = [];

    for (var i = 0; i < this._buttons.length; i++)
    {
        var button = this._buttons[i];
        
        if (button.isVisible()) visibleButtons.push(button);
    }

    return visibleButtons;
};

// hides all the buttons
GridToolbar.prototype.resetButtons = function()
{
    for (var i = 0; i < this._buttons.length; i++)
    {
        var button = this._buttons[i];
        button.hide();
    }
};

// shows all the buttons passed in the string array
GridToolbar.prototype.enableButtons = function(buttonNames)
{
    this.resetButtons();

    for (var i = 0; i < buttonNames.length; i++)
    {
        var button = this._buttonHash[buttonNames[i]];
        button.show();
    }

    var spacing = 5;
    var x = spacing;
    var y = 2;

    for (var i = 0; i < this._buttons.length; i++)
    {
        var button = this._buttons[i];
        if (!button.isVisible()) continue;

        var buttonGroup = button.getGroup();

        this.ui._assignAttributes(buttonGroup,
        {
            'transform': 'translate(' + x + ', ' + y + ')'
        }, 0);

        x = x + button.width + spacing;
    }
};

GridToolbar.prototype.processKeyEvent = function(evt)
{
    // enter moves you to canvas
    if (evt.key == 'enter')
    {
        this.grid.setArea('canvas');
        return;
    }

    // left/right moves selected button
    if (evt.key != 'left' && evt.key != 'right') return;

    var selectedButton = this.getButton(this.grid.getMode());
    var visibleButtons = this.getVisibleButtons();
    
    // make sure there are some buttons
    if (visibleButtons.length == 0) return;

    for (var i = 0; i < visibleButtons.length; i++)
    {
        var button = visibleButtons[i];

        if (button == selectedButton)
        {
            if (evt.key == 'left')
            {
                if (i == 0) selectedButton = visibleButtons[visibleButtons.length - 1]; // first
                else selectedButton = visibleButtons[i - 1]; // left
            }
            else if (evt.key == 'right')
            {
                if (i == (visibleButtons.length - 1)) selectedButton = visibleButtons[0]; // last
                else selectedButton = visibleButtons[i + 1]; // right
            }

            break;
        }
    }

    this.grid.setMode(selectedButton.name);

};

/* TOOLBAR ACTIONS */

var GridAction = {};

// collection of actions
GridAction.actions = {};

// register an action for a mode name
GridAction.registerAction = function(modeName, action) { GridAction.actions[modeName] = action; };

GridAction.Base = function(grid)
{
	this.grid = grid;
	this.canvas = grid.canvas;
	this.question = grid.question;
	this._completed = false;
};

// is the action completed
GridAction.Base.prototype.isCompleted = function() { return this._completed; };

// override these events in inherited classes to create behavior
GridAction.Base.prototype.onMouseEvent = function(evt) {};
GridAction.Base.prototype.onKeyEvent = function(evt) {};

// override this to cleanup action when it is being finalized
GridAction.Base.prototype.dispose = function() {};

// call this to finalize the action when it is finished
GridAction.Base.prototype.finalize = function() 
{ 
	this.dispose();
	this._completed = true; 
};

// MODE: MOVE POINT AND IMAGE
GridAction.Move = function(grid)
{
    GridAction.Move.superclass.constructor.call(this, grid);

    // point we are moving
    this.moveObject = null;
}

YAHOO.lang.extend(GridAction.Move, GridAction.Base);

GridAction.Move.prototype.dispose = function()
{
	if (this.moveObject instanceof GridQuestion.Point) this.canvas.finalizePoint(this.moveObject);
	else if (this.moveObject instanceof GridQuestion.Image) this.canvas.finalizeImage(this.moveObject);
};

GridAction.Move.prototype.onMouseEvent = function(evt)
{
    var selected = this.canvas.getSelected();

    // clicked while moving
    if (evt.name == 'mousedown')
    {
        if (this.moveObject || selected == null)
        {
            this.finalize();
        }
        else
        {
            this.moveObject = selected;
            this.grid.setHint('DraggingObject');
        }
    }

    // check if we are moving anything
    if (this.moveObject == null) return;

    // check if we are dragging mouse
    if (evt.name == 'mousemove' || evt.name == 'drag')
    {
        var x = evt.currentPosition.x,
			y = evt.currentPosition.y;

        this.moveObject.moveTo(x, y);
    }

    // on dragend end action
    if (evt.name == 'dragend')
    {
        this.finalize();
    }

    // for points finalize action when release mouse
    if (evt.name == 'mouseup' && selected instanceof GridQuestion.Point)
    {
        this.finalize();
    }
};

GridAction.Move.prototype.onKeyEvent = function(evt)
{
    if (this.moveObject)
    {
        this.canvas.clearSelected();
        this.finalize();
    }
    else
    {
        var selected = this.canvas.getSelected();

        if (selected)
        {
            this.moveObject = selected;
        }
        else
        {
            this.finalize();
        }
    }
};

// MODE: CREATE POINT
GridAction.Point = function(grid)
{
    GridAction.Point.superclass.constructor.call(this, grid);
    this.createdPoint = null;
};

YAHOO.lang.extend(GridAction.Point, GridAction.Base);
	
GridAction.Point.prototype.dispose = function()
{
	if (this.createdPoint)
	{
		this.createdPoint = this.canvas.finalizePoint(this.createdPoint);
	}
};

GridAction.Point.prototype.onMouseEvent = function(evt) 
{
	var x = evt.currentPosition.x,
		y = evt.currentPosition.y;
	
	if (evt.name == 'mousedown')
	{
		var entity = this.question.getEntity(evt.target.id);
		
		if (entity && entity.getType() == 'point')
		{
			this.createdPoint = entity;
		}
		else
		{
			this.createdPoint = this.question.addPoint(x, y);
		}
		
		// check if point was created before continuing
		if (this.createdPoint == null) this.finalize();
	}
	
	if (evt.name == 'drag')
	{
		this.createdPoint.moveTo(x, y);
	}
	
	if (evt.name == 'mouseup' || evt.name == 'dragend')
	{
		this.finalize();
	}
};

GridAction.Point.prototype.onKeyEvent = function(evt)
{
    // check if a point is created
    if (!this.createdPoint)
    {
        this.createdPoint = this.question.addPoint(30, 30);
        this.createdPoint.snapToGrid();
    }
    else
    {
        this.finalize();
    }
}

// MODE: CREATE LINE
GridAction.Line = function(grid)
{
    GridAction.Line.superclass.constructor.call(this, grid);

    this.pointType = 'none';
    this.sourcePoint = null;
    this.targetPoint = null;
    this.line = null;
    this.moved = false;
};

YAHOO.lang.extend(GridAction.Line, GridAction.Base);

GridAction.Line.prototype.dispose = function()
{
	if (this.pointType == 'source')
	{
		this.canvas.finalizePoint(this.sourcePoint);
	}
	else if (this.pointType == 'target')
	{
		this.targetPoint = this.canvas.finalizePoint(this.targetPoint);
		
		// if the source and target are the same then delete the line
		if (this.sourcePoint == this.targetPoint)
		{
			this.question.deleteLine(this.line);
		}
	}
};

// get the current point type element we are working with (source or target)
GridAction.Line.prototype.getPoint = function() { return this[this.pointType + 'Point']; };

// set the current point types element
GridAction.Line.prototype.setPoint = function(point) { this[this.pointType + 'Point'] = point; };

GridAction.Line.prototype.movePoint = function(x, y)
{
	var currentPoint = this.getPoint();
	currentPoint.moveTo(x, y);
};

GridAction.Line.prototype.createLine = function()
{
	this.line = this.question.addLine(this.sourcePoint, this.targetPoint);
};

// check if the line can be finalized
GridAction.Line.prototype.canLineBeFinalized = function()
{
	// make sure there are source and target points
	if (!this.sourcePoint || !this.targetPoint) return false;
	
	if (!this.moved) return false; // has the user moved mouse at all?
	
	// check if the target point intersects with the source, if it does then don't let them do this
	var intersectedPoints = this.targetPoint.getIntersections();
	
	for(var i = 0; i < intersectedPoints.length; i++) {
		if (this.sourcePoint == intersectedPoints[i]) return false;
	}
	
	return true;
};

GridAction.Line.prototype.onMouseEvent = function(evt) 
{
	var x = evt.currentPosition.x,
		y = evt.currentPosition.y;
	
	if (evt.name == 'mousedown')
	{
		var clickedEntity = this.question.getEntity(evt.target.id);

		// make sure the clicked on target point is not already the source point
		if (this.sourcePoint && this.sourcePoint == clickedEntity) return;
		
		// create source point
		if (this.sourcePoint == null)
		{
			this.pointType = 'source';
			
			// check if clicked on an existing point
			if (clickedEntity && clickedEntity.getType == 'point')
			{
				this.setPoint(clickedEntity); // use existing point
			}
			else
			{
				this.setPoint(this.question.addPoint(x, y)); // create new point
				this.sourcePoint = this.canvas.finalizePoint(this.sourcePoint);
			}
		}
		
		if (this.targetPoint == null)
		{
			this.pointType = 'target';
			this.setPoint(this.question.addPoint(x, y));
			this.createLine();
		}
		
		if (this.canLineBeFinalized()) this.finalize();
	}
	
	if (evt.name == 'mousemove' || evt.name == 'drag')
	{
		this.moved = true;
		this.movePoint(x, y);
		// this.getPoint().snapToGrid();
	}
	
	if (evt.name == 'dragend')
	{
		if (this.canLineBeFinalized()) this.finalize();
	}

};

GridAction.Line.prototype.onKeyEvent = function(evt)
{
	var selected = this.canvas.getSelected();
	
	// if there is already a point selected then lets use that as our source
	if (selected && this.pointType == 'none') 
	{
		this.pointType = 'source';
		this.setPoint(selected);
	}

	// create a new point as the source
	if (!selected && this.pointType == 'none') 
	{
		this.pointType = 'source';
		this.setPoint(this.question.addPoint(30, 30));
	}
	// finalize source, create a new point as the target and add line
	else if (this.pointType == 'source')
	{
		this.sourcePoint = this.canvas.finalizePoint(this.sourcePoint);
		this.pointType = 'target';
		this.setPoint(this.question.addPoint(this.sourcePoint.x, this.sourcePoint.y));
		this.createLine();
	}
	// finalize target
	else
	{
		this.targetPoint = this.canvas.finalizePoint(this.targetPoint);
		
		// if the source and target are the same then delete the line
		if (this.sourcePoint == this.targetPoint)
		{
			this.question.deleteLine(this.line);
		}
		
		this.finalize();
	}
}
	
// MODE: CREATE SINGLE ARROW
GridAction.ArrowSingle = function(grid)
{
    GridAction.ArrowSingle.superclass.constructor.call(this, grid);
};

YAHOO.lang.extend(GridAction.ArrowSingle, GridAction.Line);

GridAction.ArrowSingle.prototype.createLine = function()
{
	this.line = this.question.addLine(this.sourcePoint, this.targetPoint, 'forward');
};	

// MODE: CREATE ARROWS
GridAction.ArrowDouble = function(grid)
{
    GridAction.ArrowDouble.superclass.constructor.call(this, grid);
};

YAHOO.lang.extend(GridAction.ArrowDouble, GridAction.Line);

GridAction.ArrowDouble.prototype.createLine = function()
{
	this.line = this.question.addLine(this.sourcePoint, this.targetPoint, 'both');
};	

// MODE: DELETE POINT AND IMAGE
GridAction.Delete = function(grid)
{
	GridAction.Delete.superclass.constructor.call(this, grid);
	this.deletedPoint = null;
}

YAHOO.lang.extend(GridAction.Delete, GridAction.Base);

GridAction.Delete.prototype.onMouseEvent = function(evt) 
{
	// delete when you click on something and then release mouse
	if (evt.name == 'mouseup')
	{
		var selected = this.canvas.getSelected();
		
		if (selected)
		{
			var type = selected.getType();

			switch(type)
			{
				case 'point': this.question.deletePoint(selected); break;
				case 'canvasimage': this.question.deleteImage(selected); break;
			}

			this.finalize();
		}
		else
		{
			// check if someone clicked on a line directly
			var targetEntity = this.question.getEntity(evt.target.id);
			
			if (targetEntity && targetEntity.getType() == 'line')
			{
				this.question.deleteLine(targetEntity);
			}
		}
	}

	if (evt.name == 'mouseup' || evt.name == 'dragend') this.finalize();
};

GridAction.Delete.prototype.onKeyEvent = function(evt)
{
    var selected = this.canvas.getSelected();

    if (selected)
    {
        var type = selected.getType();

        switch (type)
        {
            case 'point': this.question.deletePoint(selected); break;
            case 'canvasimage': this.question.deleteImage(selected); break;
        }

        this.finalize();
    }
};

// register actions for a mode
GridAction.registerAction('move', GridAction.Move);
GridAction.registerAction('delete', GridAction.Delete);
GridAction.registerAction('point', GridAction.Point);
GridAction.registerAction('connect', GridAction.Line);
GridAction.registerAction('arrow', GridAction.ArrowSingle);
GridAction.registerAction('arrw2', GridAction.ArrowDouble);
