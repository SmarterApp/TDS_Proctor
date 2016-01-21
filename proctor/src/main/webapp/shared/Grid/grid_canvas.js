function GridCanvas(grid)
{
	this.grid = grid;
	this.ui = grid.ui;
	this.question = grid.question;

	// current action
	this._currentAction = null;

	// currently selected question entity
	this._currentSelected = null;

	ErrorHandler.wrapFunctions(this,
    [
	    'finalizePoint', 'finalizeLines', 'processMouseEvent', 'processKeyEvent', 'subscribeToModelEvents'
    ]);
};

GridCanvas.prototype._createAction = function()
{
	if (this._currentAction) return false;
	
	var mode = this.grid.getMode();
	
	// get action class
	var action = GridAction.actions[mode];
	
	// make sure exists
	if (!action) return false;
	
	// create instance of action
	this._currentAction = new action(this.grid);
	logger.debug('Action - {mode} started', { mode: mode });
	
	return true;
};

// cancels the current action (finalize)
GridCanvas.prototype.cancelAction = function()
{
    if (!this._currentAction) return false; // no action to cancel
    if (!this._currentAction.isCompleted()) this._currentAction.finalize(); // not completed? finish it up
    this._currentAction = null; // destroy action
    return true;
};

// get the current selected element
GridCanvas.prototype.getSelected = function() { return this._currentSelected; }

// clear selected
// @entity This is optional and if provided then only will this entity be cleared if it is currently selected
GridCanvas.prototype.clearSelected = function(entity) 
{ 
	if (entity)
	{
		var selected = this.getSelected();
		if (!selected || selected != entity) return;
	}
		
	return this.setSelected(null); 
}

// call this function to set an object as selected
GridCanvas.prototype.setSelected = function(newSelection)
{
    // check if element is already selected
    if (newSelection == this._currentSelected) return false;

    // BUG 14216: Dragging one palette object on another palette object in the grid selects the earlier object
    if (this.grid.palette.moving == true) return false;

    // deselect current entity
    if (this._currentSelected)
    {
        var id = this._currentSelected.getID();

        if (this._currentSelected instanceof GridQuestion.Point)
        {
            this.ui.stylePointDefault(id);
        }
        else if (this._currentSelected instanceof GridQuestion.Image)
        {
            this.ui.deselectImage(id);
        }
    }

    // set new entity
    this._currentSelected = newSelection || null;
    
    // select current entity
    if (this._currentSelected)
    {
        var id = this._currentSelected.getID();

        if (this._currentSelected instanceof GridQuestion.Point)
        {
            this.ui.stylePointSelected(id);
        }
        else if (this._currentSelected instanceof GridQuestion.Image)
        {
            this.ui.selectImage(id);
        }
    }

    return true;
};

// call this function when a point stops moving
GridCanvas.prototype.finalizePoint = function(point)
{
    var lines = point.getLines();

    // snap to closest snap point and if none then the try the grid
    if (!point.snapToPoint())
    {
        point.snapToGrid();
    }

    // check if this point intersects with any other points
    var intersectedPoints = point.getIntersections();

    if (intersectedPoints.length == 0)
    {
        // no point intersections
        this.finalizeLines(lines);
        return point;
    }

    // get the first intersected point to merge with
    var mergePoint = intersectedPoints[0];

    // move lines from the current point to the existing intersecting point
    point.moveLines(mergePoint);

    // if the current point was selected then select the new one
    if (point == this.getSelected()) this.setSelected(mergePoint);

    // delete current point
    this.question.deletePoint(point);

    this.finalizeLines(mergePoint.getLines());
    return mergePoint;
};

// determines if a line merge occurs and merges
// EdgeDB.java - line 341 combineOverlappingLineSegments(jLine line, int tolerance)
GridCanvas.prototype.finalizeLines = function(pointLines)
{
    // HACK: If we are just starting a line the first point will get finalized and it 
    // might merge with an existing point. If that existing point has a line attached 
    // to it and that line can be merged with a nearby line then the source point could 
    // possibly get deleted (rightfully so). So visually you would see yourself moving
    // a line without a source point. So in this case we need to not do line merging. 
    if (this._currentAction instanceof GridAction.Line && this._currentAction.targetPoint == null) return;

    var toleranceParallel = 10; // java was 10 (using slope)
    var toleranceDistance = 4; // the max distance between two lines before merging

    // loop through all lines that were being moved
    for (var i = 0; i < pointLines.length; i++)
    {
        var pointLine = pointLines[i];

        var lines = this.question.getLinesByDir(pointLine.dirType);

        // loop through all the lines on the grid
        for (var j = 0; j < lines.length; j++)
        {
            var line = lines[j];
            if (pointLine == line) continue;

            // check if the lines are parallel with a certain tolerance
            var isParallel = pointLine.isParallelTo(line, toleranceParallel);
            if (!isParallel) continue;

            // check if the point line is within a certain distance from this line
            var distance = pointLine.distanceFrom(line);
            if (distance > toleranceDistance) continue;

            // begin line merge process
            var newLine = pointLine.getLongestLine(line);
            this.question.deleteLine(line);
            this.question.deleteLine(pointLine);
            this.question.addLine(newLine.source, newLine.target, pointLine.dirType);

            // delete empty points that got removed
            if (line.source.getLines().length == 0) this.question.deletePoint(line.source);
            if (line.target.getLines().length == 0) this.question.deletePoint(line.target);
            if (pointLine.source.getLines().length == 0) this.question.deletePoint(pointLine.source);
            if (pointLine.target.getLines().length == 0) this.question.deletePoint(pointLine.target);

            // stop processing lines 
            // TODO: we should process the rest of the lines as well, but this was how current grid worked
            return;
        }
    }
};

// call this function when a point stops moving
GridCanvas.prototype.finalizeImage = function(image)
{
    // snap to closest snap point and if none then the try the grid
    if (!image.snapToPoint())
    {
        image.snapToGrid();
    }
};

/**
 * returns the next object in the selection order, or null if this is the only point in the vector.
 * If this is at the end of the selection order it returns the first point in the selection order.
 * The selection order begins at the top, left and moves towards the bottom, right  
 * @param p - the current point
 * @return next point in the selection order or null 
 */
GridCanvas.prototype._nextObjectInSelectionOrder = function(currentPosition, positions)
{
	if (currentPosition == null) return this._firstObjectInSelectionOrder(positions);

	var nextPosition = null;
	
	for(var i = 0; i < positions.length; i++)
	{
		var existingPosition = positions[i];
		
		if (existingPosition.y < currentPosition.y || (existingPosition.y == currentPosition.y && existingPosition.x <= currentPosition.x))
		{
			continue; // this was a previous point 
		}
		
		if (nextPosition == null) 
		{
			nextPosition = existingPosition;
		}
		else if (existingPosition.y < nextPosition.y || (existingPosition.y == nextPosition.y && existingPosition.x < nextPosition.x))
		{
			nextPosition = existingPosition;
		}
	}
	
	if (nextPosition == null) nextPosition = this._firstObjectInSelectionOrder(positions);		
	return nextPosition;
};

/**
 * Finds the top left most point in the point vector
 * @return the top left most point
 */
GridCanvas.prototype._firstObjectInSelectionOrder = function(positions) 
{
	var firstPosition = null;
	
	for (var i = 0; i < positions.length; i++) 
	{
		var existingPosition = positions[i];

		if (firstPosition == null) 
		{
			firstPosition = existingPosition;
		}
		else if (existingPosition.y < firstPosition.y || (existingPosition.y == firstPosition.y && existingPosition.x < firstPosition.x))
		{
			firstPosition = existingPosition;
		}
	}

	return firstPosition;
};

GridCanvas.prototype._processMouseAction = function(evt)
{
    var mode = this.grid.getMode();

    // if there is no current action then create one based on the mode
    if (!this._currentAction)
    {
        if (evt.name != 'mousedown') return;
        this._createAction();
    }

    // fire mouse event
    try
    {
        this._currentAction.onMouseEvent(evt);
    }
    catch (ex)
    {
        this._currentAction = null;
        throw ex;
    }

    // if the action is completed then remove it
    if (this._currentAction.isCompleted())
    {
        this.grid.setModeHint();
        this._currentAction = null;
        logger.debug('Action - {mode} completed', { mode: mode });
    }
};

// action keyboard mapping
GridCanvas.prototype._processKeyAction = function(name, evt)
{
	// console.log('keypress: ' + evt.keyCode);
	
	var mode = this.grid.getMode();
	
	// if there is no current action then create one based on the mode
	if (!this._currentAction) this._createAction();
	
	// fire mouse event
	try
	{
		this._currentAction.onKeyEvent(evt);
	}
	catch(ex)
	{
		this._currentAction.finalize();
		this._currentAction = null;
		throw ex;
	}
	
	// if the action is completed then remove it
	if (this._currentAction.isCompleted())
	{
		this._currentAction = null;
		logger.debug('Action - {mode} completed', { mode: mode });
	}
	
};

GridCanvas.prototype.processMouseEvent = function(evt)
{
	if (evt.name == 'mousedown' && evt.target != null)
	{
		var entity = this.question.getEntity(evt.target.id);
		
		if (entity && (entity.getType() == 'point' || entity.getType() == 'canvasimage'))
		{
			// select element
			this.setSelected(entity);
		}
		else
		{
			this.clearSelected();
		}
	}
	
	this._processMouseAction(evt);
};

GridCanvas.prototype.processKeyEvent = function(evt)
{
    var selected = this.getSelected();

    if (evt.key == 'esc')
    {
        this.clearSelected();
        this.cancelAction();
    }

    // SELECT
    if (evt.key == 'enter' && !this._currentAction)
    {
        var objects = [];

        // copy points
        var points = this.question.getPoints();
        for (var i = 0; i < points.length; i++) objects.push(points[i]);

        // if we are in move mode then include images as well
        if (this.grid.getMode() == 'move' || this.grid.getMode() == 'delete')
        {
            var images = this.question.getImages();
            for (var i = 0; i < images.length; i++) objects.push(images[i]);
        }

        var nextObject = this._nextObjectInSelectionOrder(selected, objects);

        if (selected != nextObject)
        {
            this.cancelAction();
        }

        this.setSelected(nextObject);
        return;
    };

    // ACTION
    if (evt.key == 'space')
    {
        if (selected)
        {
            // set style on point to look like its being moved
            // this.ui.movePoint(selected.getID(), selected.x, selected.y);
        }

        this._processKeyAction('keydown', evt);
    };

    // only allow moving something if an action is currently taking place
    if (this._currentAction == null) return;

    // MOVE
    var moveKey = (!evt.dom.ctrlKey && (evt.key == 'left' || evt.key == 'right' || evt.key == 'up' || evt.key == 'down'));

    // move object
    if (selected && moveKey)
    {
        var x = selected.x,
			y = selected.y;

        var moveSize = evt.dom.shiftKey ? 1 : 10;
        if (evt.key == 'left') selected.moveLeft(moveSize);
        if (evt.key == 'right') selected.moveRight(moveSize);
        if (evt.key == 'up') selected.moveUp(moveSize);
        if (evt.key == 'down') selected.moveDown(moveSize);
    }

};

// get line info used for drawing (x1, y1, x2, y2, angle)
GridCanvas.prototype._getLineInfo = function(line)
{
	// get the x, y, radius and thickness of the source and target
	var x1 = line.source.x, 
		y1 = line.source.y, 
		r1 = line.source.radius, 
		t1 = 1;
	var x2 = line.target.x, 
		y2 = line.target.y,
		r2 = line.target.radius, 
		t2 = 1;
	
	// skip below if this line doesn't have an arrow (uncomment this out for maybe slight performance gain?)
	// if (!line.directed) return {x1: x1, y1: y1, x2: x2, y2: y2, angle: null };
	
	// determine intersection of connecting line with circles
	// angle from circle two origin to circle one origin
	var radians = Math.atan2(y2 - y1, x2 - x1);

	// locate intersection by displacing circle origin to perimeter
	// by half the diameter +/- the thickness [1] of the perimeter line circle one
	// EXAMPLE RESULT: ---->*
	var sourceOffset = -2;
	var targetOffset = (line.dirType == 'none') ? -2 : -1;

	x1 = x1 + (Math.cos(radians) * ((r1 + t1) + sourceOffset));
	y1 = y1 + (Math.sin(radians) * ((r1 + t1) + sourceOffset));
	x2 = x2 - (Math.cos(radians) * ((r2 + t2) + targetOffset));
	y2 = y2 - (Math.sin(radians) * ((r2 + t2) + targetOffset));
	
	var angle = (radians / (2 * Math.PI)) * 360; // radians to degrees
	
	return {x1: x1, y1: y1, x2: x2, y2: y2, angle: angle };		
};

// resorts all the images on the canvas by size
GridCanvas.prototype.reorderImages = function()
{
    var images = this.question.getImages();

    // remove all images
    for (var i = 0; i < images.length; i++)
    {
        var image = images[i];
        this.ui.removeElement(image.getID());
    }

    // sorts images from largest to smallest
    var imageSorter = function(imageA, imageB)
    {
        var sizeA = imageA.getSize();
        var sizeB = imageB.getSize();
        return sizeA > sizeB ? -1 : sizeA < sizeB ? 1 : 0;
    };

    images.sort(imageSorter);

    // add all images back
    for (var i = 0; i < images.length; i++)
    {
        var image = images[i];
        var paletteImage = image.getPaletteImage();
        this.ui.createImage(image.getID(), image.x, image.y, image.width, image.height, paletteImage.url);
    }
};

GridCanvas.prototype.subscribeToModelEvents = function()
{
    var canvas = this;
    var ui = this.ui;

    // ADD
    this.question.subscribe('addEntity', function(entity)
    {
        var id = entity.getID();

        if (entity instanceof GridQuestion.SnapPoint)
        {
            // ui.createSnapPoint(id, entity.x, entity.y, entity.snapRadius);
        }
        else if (entity instanceof GridQuestion.Point)
        {
            ui.createPoint(id, entity.x, entity.y);
            canvas.setSelected(entity);
        }
        else if (entity instanceof GridQuestion.Line)
        {
            var lineInfo = canvas._getLineInfo(entity);

            ui.createLine(id, lineInfo.x1, lineInfo.y1, lineInfo.x2, lineInfo.y2);
            if (entity.dirType == 'forward' || entity.dirType == 'both') ui.createArrow(id + '_arrow1', lineInfo.x2, lineInfo.y2, lineInfo.angle);
            if (entity.dirType == 'both') ui.createArrow(id + '_arrow2', lineInfo.x1, lineInfo.y1, lineInfo.angle - 180);
        }
        else if (entity instanceof GridQuestion.Image)
        {
            var paletteImage = entity.getPaletteImage();
            ui.createImage(id, entity.x, entity.y, entity.width, entity.height, paletteImage.url);

            canvas.reorderImages();
        }
        else if (entity instanceof GridQuestion.BackgroundImage)
        {
            ui.createBackground(id, entity.x, entity.y, entity.width, entity.height, entity.url);
        }
    });

    // MOVE
    this.question.subscribe('moveEntity', function(entity)
    {
        var id = entity.getID();

        if (entity instanceof GridQuestion.SnapPoint)
        {
        }
        else if (entity instanceof GridQuestion.Point)
        {
            ui.movePoint(id, entity.x, entity.y);
        }
        else if (entity instanceof GridQuestion.Line)
        {
            var lineInfo = canvas._getLineInfo(entity);

            ui.moveLine(id, lineInfo.x1, lineInfo.y1, lineInfo.x2, lineInfo.y2);
            if (entity.dirType == 'forward' || entity.dirType == 'both') ui.moveArrow(id + '_arrow1', lineInfo.x2, lineInfo.y2, lineInfo.angle);
            if (entity.dirType == 'both') ui.moveArrow(id + '_arrow2', lineInfo.x1, lineInfo.y1, lineInfo.angle - 180);

            /*
            var angle = entity.getAngle();
            var angle2 = entity.getAngle2();
            var slope = entity.getSlope();
            console.log('line \'' + id + '\' - angle: ' + angle + ' angle2: ' + angle2 + ' slope: ' + slope);
            */
        }
        else if (entity instanceof GridQuestion.Image)
        {
            // NOTE: When moving an image and it is not currently selected then we need to not draw the border around it
            var isSelected = (entity == canvas.getSelected());
            ui.moveImage(id, entity.x, entity.y, entity.width, entity.height, isSelected);
        }
    });

    // DELETE
    this.question.subscribe('deleteEntity', function(entity)
    {
        var id = entity.getID();

        if (entity instanceof GridQuestion.SnapPoint)
        {
            // ui.removeElement(id);
        }
        else if (entity instanceof GridQuestion.Point)
        {
            canvas.clearSelected(entity);
            ui.removeElement(id);
        }
        else if (entity instanceof GridQuestion.Line)
        {
            ui.removeElement(id);
            if (entity.dirType == 'forward' || entity.dirType == 'both') ui.removeElement(id + '_arrow1');
            if (entity.dirType == 'both') ui.removeElement(id + '_arrow2');
        }
        else if (entity instanceof GridQuestion.Image)
        {
            canvas.clearSelected(entity);
            ui.removeElement(id);
        }
        else if (entity instanceof GridQuestion.BackgroundImage)
        {
            ui.removeElement(id);
        }

    });
};
