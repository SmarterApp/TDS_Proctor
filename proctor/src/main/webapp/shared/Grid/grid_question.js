// NAMING CONVENTIONS: http://www.graphviz.org/doc/info/attrs.html
// NAMING CONVENTIONS: http://www.graphviz.org/doc/info/attrs.html

/* OPTIONS CLASS */
// XML: <Question> <QuestionPart> <Options>
var GridOptions = function()
{
    this.showGridLines = false; // this is true if grid color is 'None'
    this.gridColor = 'None'; // NOTE: This will be 'None' or 'LightBlue' always
    this.gridSpacing = 0; // the spacing in between the grid lines
    this.snapToGrid = false; // snap to the grid spacing?
    this.snapRadius = 0;
    this.selectionTolerance = 0;
    this.showButtons = []; // collection of button names (e.x., 'delete', 'point')
    this.showPalette = false; // was an object or atomic object loaded as part of the answer space?

    // changing these won't affect the UI right now:
    this.gridWidth = 600;
    this.gridHeight = 500;
    this.canvasWidth = 500;
    this.canvasHeight = 410;
};

GridOptions.prototype.addButton = function(button)
{
    if (button == 'delete' || button == 'point' || button == 'connect' || button == 'arrow' || button == 'arrw2')
    {
        this.showButtons.push(button);
    }
};

/* QUESTION CLASS */

var GridQuestion = function(id, options)
{
    this.id = id || '';
    this.options = options || new GridOptions();
    this._uuid = 0;

    // the part of the its item where the question is located
    this.questionPartID = 1;

    this.description = '';

    // the default radius of a point if none is provided
    this.defaultRadius = 5;

    // cache objects for easy id based lookup
    this._cache = {};
    this._addCache = function(id, obj) { this._cache[id] = obj; }
    this._removeCache = function(id) { this._cache[id] = null; }

    // collections
    this._points = [];
    this._snappoints = [];
    this._lines = [];
    this._paletteimages = [];
    this._canvasimages = [];
    this._backgroundimages = [];

    // is the model working with the answer space (importing == true) or response?
    this.importing = false;

    this._listenForCacheEvents();

    this._instance = ++GridQuestion._instances;
};

YAHOO.lang.augmentProto(GridQuestion, EventLazyProvider);

GridQuestion._instances = 0;

GridQuestion.prototype._createUUID = function()
{
    var t = new Date().getTime();
    var r = Math.random().toString().split('.')[1] * 1;

    ++this._uuid;
    return this._uuid; // + '_' + rnd;
};

GridQuestion.prototype._listenForCacheEvents = function()
{
	var question = this;
	
	this.subscribe('addEntity', function(entity)
	{
		var id = entity.getID();
		question._cache[id] = entity;
	});

	this.subscribe('deleteEntity', function(entity)
	{
		var id = entity.getID();
		question._cache[id] = null;
	});
};

// remove an object from an array based on index
GridQuestion.prototype._remove = function(array, from, to /*optional*/)
{
	var rest = array.slice((to || from) + 1 || array.length);
	array.length = from < 0 ? array.length + from : from;
	return array.push.apply(array, rest);
};

// check if a string and has something
GridQuestion.prototype._hasString = function(id) { return (Lang.isString(id) && id.length > 0); };

/* PUBLIC API */

// generic delete function
GridQuestion.prototype._deleteEntity = function(entityToDelete)
{
    var type = entityToDelete.getType();
    var collectionName = '_' + type + 's';
    var collection = this[collectionName];

    for (var i = 0; i < collection.length; i++)
    {
        // get entity
        var entity = collection[i];

        // check if entity is the one that was passed in
        if (entity != entityToDelete) continue;

        // fire event
        this.fireLazy('deleteEntity', entityToDelete);

        // remove from array
        this._remove(this[collectionName], i);
        return true;
    }

    return false; // entity not found
};

// clear response
GridQuestion.prototype.clearResponse = function()
{
    while (this._snappoints.length > 0) this.deleteSnapPoint(this._snappoints[0]);
    while (this._points.length > 0) this.deletePoint(this._points[0]);
    while (this._canvasimages.length > 0) this.deleteImage(this._canvasimages[0]);
};

//clear question
GridQuestion.prototype.clearQuestion = function()
{
    this.clearResponse();

    while (this._snappoints.length > 0) this.deleteSnapPoint(this._snappoints[0]);
    while (this._paletteimages.length > 0) this.deletePaletteImage(this._paletteimages[0]);
    while (this._backgroundimages.length > 0) this.deleteBackgroundImage(this._backgroundimages[0]);

    this.options = new GridOptions();
};

// get any entity (e.x., point, line) by ID
GridQuestion.prototype.getEntity = function(id) { return this._cache[id]; };

// add a new point
GridQuestion.prototype.addPoint = function(x, y)
{
	var radius = (arguments[2] > 0) ? arguments[2] : this.defaultRadius; // selectionTolerance
	
	var point = new GridQuestion.Point(this, x, y, radius);
	// point.snapToGrid();
	
	this._points.push(point);
	this.fireLazy('addEntity', point);
	
	return point;
};

// get a collection of all the points
GridQuestion.prototype.getPoints = function() { return this._points; };

// delete an existing point
GridQuestion.prototype.deletePoint = function(point)
{
	var points = this.getPoints();
	
	// remove point
	for(var i = 0; i < this._points.length; i++)
	{
		if (point != this._points[i]) continue;

		// remove any lines associated to this point
		var pointLines = point.getLines();
		
		for(var j = 0; j < pointLines.length; j++)
		{
			var line = pointLines[j];
			this.deleteLine(line);
		}
		
		// remove point from array
		this._remove(this._points, i);
		this.fireLazy('deleteEntity', point);
		
		return true;
	}
	
	return false;
};

// get a collection of all the lines
GridQuestion.prototype.getLines = function() { return this._lines; };

// Add a new line using an existing source and target point
// @source The starting point
// @target The ending point
// @dirType The direction of the line (none, forward, back, both) MORE INFO: http://www.graphviz.org/doc/info/attrs.html#k:dirType
GridQuestion.prototype.addLine = function(source, target, dirType)
{
	if (!source || !target) throw new Error('Invalid source or target point');

	// check if source and target are the same
	if (source == target) return null;
	
	// check if these points already have a line connected
	for(var i = 0; i < this._lines.length; i++)
	{
		var existingLine = this._lines[i];
		if (source == existingLine.source && target == existingLine.target) return existingLine; // exact duplicate
		if (target == existingLine.source && source == existingLine.target) return existingLine; // reversed duplicate
	}
	
	// DIRECTION
	if (!dirType) dirType = 'none'; // no direction provided default to none
	else if (Lang.isBoolean(dirType)) dirType = dirType ? 'forward' : 'none'; // boolean provided, if true then set as forward
	else if (Lang.isString(dirType)) // string provided, verify
	{
		dirType = dirType.toLowerCase();
		
		if (dirType != 'none' && dirType != 'forward' && dirType != 'back' && dirType != 'both')
		{
			throw new Error('Invalid line direction');
		}
	}
	
	var line = new GridQuestion.Line(this, source, target, dirType);
	this._lines.push(line);
	this.fireLazy('addEntity', line);

	return line;
};

// get all lines for a direction (none, forward, back, both)
GridQuestion.prototype.getLinesByDir = function(dirType)
{
	var lineDirections = [];
	
	for (var i = 0; i < this._lines.length; i++)
	{
		var line = this._lines[i];
		if (line.dirType == dirType) lineDirections.push(line);
	}
	
	return lineDirections;
};

GridQuestion.prototype.deleteLine = function(line)
{
    return this._deleteEntity(line);
};

GridQuestion.prototype.getSnapPoints = function() { return this._snappoints; };

// add a new snap point
GridQuestion.prototype.addSnapPoint = function(x, y, snapRadius)
{
	var snapPoint = new GridQuestion.SnapPoint(this, x, y, this.defaultRadius, snapRadius);
	
	this._snappoints.push(snapPoint);
	this.fireLazy('addEntity', snapPoint);
	return snapPoint;
};

GridQuestion.prototype.deleteSnapPoint = function(snapPoint)
{
    return this._deleteEntity(snapPoint);
};

GridQuestion.prototype.getPaletteImages = function() { return this._paletteimages; };

// Add an image to the palette
// XML: <ObjectMenuIcons> <IconSpec>
GridQuestion.prototype.addPaletteImage = function(name /* <Label> */, url /* <FileSpec> */, width, height)
{
	var paletteImage = new GridQuestion.PaletteImage(this, name, url, width, height);
	
	this._paletteimages.push(paletteImage);
	this.fireLazy('addEntity', paletteImage);
	
	return paletteImage;
};

GridQuestion.prototype.deletePaletteImage = function(paletteImage)
{
    // get canvas images
    var canvasImages = paletteImage.getImages();

    // delete palette image
    this._deleteEntity(paletteImage);

    // delete canvas images
    for (var i = 0; i < canvasImages.length; i++)
    {
        this._deleteEntity(canvasImages[0]);
    }
};

GridQuestion.prototype.getImages = function() { return this._canvasimages; };

// Add an image to the canvas
// XML: <AtomicObject> {CarA(64,268)} </AtomicObject>
GridQuestion.prototype.addImage = function(name, x, y)
{
    if (!Lang.isString(name) || name.length == 0) return null;

    var paletteImage = null;

    for (var i = 0; i < this._paletteimages.length; i++)
    {
        if (name == this._paletteimages[i].name)
        {
            paletteImage = this._paletteimages[i];
            break;
        }
    }

    if (paletteImage == null) throw new Error('Cannot add the image ' + name + ' because the palette image does not exist.');

    var image = new GridQuestion.Image(this, name, x, y, paletteImage.width, paletteImage.height);
    this._canvasimages.push(image);
    this.fireLazy('addEntity', image);

    return image;
};

GridQuestion.prototype.deleteImage = function(image)
{
    return this._deleteEntity(image);
};

GridQuestion.prototype.getBackgroundImages = function() { return this._backgroundimages; };

// Add an image to the background of the canvas which cannot be moved
// XML: <ImageSpec>
GridQuestion.prototype.addBackgroundImage = function(url /* <FileSpec> */, x, y, width, height)
{
    var backgroundImage = new GridQuestion.BackgroundImage(this, url, x, y, width, height);

    this._backgroundimages.push(backgroundImage);
    this.fireLazy('addEntity', backgroundImage);
    return backgroundImage;
};

GridQuestion.prototype.deleteBackgroundImage = function(backgroundImage)
{
    return this._deleteEntity(backgroundImage);
};

/* DATA STRUCTURES */

// CLASS: BaseModel
GridQuestion.Base = function(question)
{
	this.question = question;
	// this._id = question._createUUID();
	this._id = question._instance + '_' + question._createUUID(); // question.instanceID + '_' + 
};

// Get the type string name
// TODO: try and remove the need for this function
GridQuestion.Base.prototype.getType = function()
{
	if (this instanceof GridQuestion.BackgroundImage) return 'backgroundimage';
	if (this instanceof GridQuestion.PaletteImage) return 'paletteimage';
	if (this instanceof GridQuestion.Image) return 'canvasimage';
	if (this instanceof GridQuestion.Line) return 'line';
	if (this instanceof GridQuestion.SnapPoint) return 'snappoint';
	if (this instanceof GridQuestion.Point) return 'point';
	return '';
}

// get unique id for this entity
GridQuestion.Base.prototype.getID = function()
{
	return this.getType() + '_' + this._id;
};

/* POSITION CLASS */

GridQuestion.Position = function(question, x, y)
{
	GridQuestion.Position.superclass.constructor.call(this, question);
	this.x = x;
	this.y = y;
};

Lang.extend(GridQuestion.Position, GridQuestion.Base);

// get a Point2D object for use with 2D.js library
GridQuestion.Position.prototype.get2D = function() { return new Point2D(this.x, this.y); };

GridQuestion.Position.prototype.setXY = function(x, y)
{
    // set changes
    this.x = x;
    this.y = y;

    this.question.fireLazy('moveEntity', this);
};

// Set the XY and throw an event.
// Pass true for preventSnap to stop grid snapping.
GridQuestion.Position.prototype.moveTo = function(moveX, moveY, preventSnap /*optional*/)
{
    // check if anything has changed
    if (moveX == this.x && moveY == this.y) return false;

    this.x = moveX;
    this.y = moveY;
    
    // check if we should perform snap to grid for this move
    if (!preventSnap) this.snapToGrid();

    this.question.fireLazy('moveEntity', this);

    return true;
};

GridQuestion.Position.prototype._moveBy = function(moveSize)
{
    var options = this.question.options;

    // if grid snapping is enabled then move by the grid spacing
    if (options.snapToGrid && options.gridSpacing > 0)
    {
        return options.gridSpacing;
    }
    
    return moveSize;
};
	
GridQuestion.Position.prototype.moveLeft = function(moveSize)
{
    return this.moveTo(this.x - this._moveBy(moveSize), this.y);
};

GridQuestion.Position.prototype.moveUp = function(moveSize)
{
    return this.moveTo(this.x, this.y - this._moveBy(moveSize));
};

GridQuestion.Position.prototype.moveRight = function(moveSize)
{
    return this.moveTo(this.x + this._moveBy(moveSize), this.y);
};

GridQuestion.Position.prototype.moveDown = function(moveSize)
{
    return this.moveTo(this.x, this.y + this._moveBy(moveSize));
};

// get the distance from this position to another
GridQuestion.Position.prototype.distanceFrom = function(that)
{
    var p1 = this.get2D(),
        p2 = that.get2D();
    
    return p1.distanceFrom(p2);
};

// get the closest snap point that this position intersects with
GridQuestion.Position.prototype._getNearestSnapPoint = function()
{
	var snapPoints = this.question.getSnapPoints();
    
    var closestSnapPoint = null;
    var closestDistance = +Infinity;
    
	for(var i = 0; i < snapPoints.length; i++)
	{
		var snapPoint = snapPoints[i];
		
		// get the distance from this point to the snap point
		var distance = this.distanceFrom(snapPoint);
		
		// see if this point is within the snap radius and if it is then see if 
		// it is closer than any previous snap point we have found
		if (distance <= snapPoint.snapRadius && distance < closestDistance)
		{
		    closestDistance = distance;
		    closestSnapPoint = snapPoint;		    
		}
	}
	
	return closestSnapPoint;
};

// Snap to snap point.
GridQuestion.Position.prototype.snapToPoint = function()
{
    var snapPoint = this._getNearestSnapPoint();

    if (snapPoint != null)
    {
        // move to the nearest snap point
        return this.moveTo(snapPoint.x, snapPoint.y, true);
    }

    return false;
};

// Snap to grid.
// SCORING NOTE: When snapping to the grid with an image you need to use the top/left corner. However
// the image position is stored using the bottom/middle. So to get the left most side we divide the width 
// by two and then subract that from the current x. Dividing the width though could leave you with a fractional
// x coordinate and this will get rounded since drawing an SVG image on a fractional coordinate can render an 
// image blurry. This means during rounding the actual left of the image might be a pixel off. I had some concerns
// that this would cause issues in the scoring engine but I spoke to Xiaohui and he had similar issues. He said that 
// in the scoring engine if a point is near a snap point within a certain tollerance then the scoring engine fixes 
// this to be in the snap point. I explained this scenerio and he said I would be ok.
GridQuestion.Position.prototype.snapToGrid = function()
{
    var spacing = 1; // default spacing even if snapping is off
    var options = this.question.options;

    if (options.snapToGrid && options.gridSpacing > 0)
    {
        spacing = options.gridSpacing;
    }

    // start with X/Y as provided
    var fixedX = this.x;
    var fixedY = this.y;

    // HACK: set X/Y to top/left for images
    if (this instanceof GridQuestion.Image)
    {
        fixedX = (fixedX - Math.round(this.width / 2));
        fixedY = (fixedY - this.height);
    }

    // perform grid snapping
    var snappedX = Math.round(fixedX / spacing) * spacing;
    var snappedY = Math.round(fixedY / spacing) * spacing;

    // HACK: set X/Y back to bottom/middle
    if (this instanceof GridQuestion.Image)
    {
        snappedX = (snappedX + Math.round(this.width / 2));
        snappedY = (snappedY + this.height);
    }

    // move to the snapped grid spacing
    return this.moveTo(snappedX, snappedY, true);
};

// returns the closest position (Point2D) along a line
GridQuestion.Position.prototype.nearestPointAlongLine = function(line)
{
    var px = this.x,
		py = this.y,
		x1 = line.source.x,
		y1 = line.source.y,
		x2 = line.target.x,
		y2 = line.target.y;

    var dx = x2 - x1;
    var dy = y2 - y1;

    // check if the segment is just a point
    if (dx == 0 && dy == 0) return { x: x1, y: y1 };

    // calculate the t that minimizes the distance.
    var t = ((px - x1) * dx + (py - y1) * dy) / (dx * dx + dy * dy);

    // See if this represents one of the segment's end points or a point in the middle.
    if (t < 0)
    {
        dx = x1;
        dy = y1;
    }
    else if (t > 1)
    {
        dx = x2;
        dy = y2;
    }
    else
    {
        dx = x1 + t * dx;
        dy = y1 + t * dy;
    }

    return new Point2D(dx, dy);
};

// what is the distance from this position to the line
GridQuestion.Position.prototype.distanceFromLine = function(line)
{
    var nearestPoint = this.nearestPointAlongLine(line);
    return this.get2D().distanceFrom(nearestPoint);
};

// GridQuestion.Position.prototype.toString = function() { this.x + ',' + this.y; };

/* POINT CLASS */

GridQuestion.Point = function(question, x, y, radius)
{
	GridQuestion.Point.superclass.constructor.call(this, question, x, y);
	this.radius = radius;
};

Lang.extend(GridQuestion.Point, GridQuestion.Position);

// move point
GridQuestion.Point.prototype.moveTo = function(moveX, moveY, preventSnap)
{
    var width = this.radius * 2;
    var height = this.radius * 2;

    // get canvas info
    var canvasWidth = this.question.options.canvasWidth;
    var canvasHeight = this.question.options.canvasHeight;

    // get bounding rect
    var topLeftX = moveX - Math.round(width / 2);
    var topLeftY = moveY - height;
    var bottomRightX = topLeftX + width;
    var bottomRightY = topLeftY + height;

    // make any out of bounds coordinates within bounds
    if (topLeftX < 0) moveX = 0;
    if (topLeftY < 0) moveY = 0;
    if (bottomRightX > canvasWidth) moveX = canvasWidth;
    if (bottomRightY > canvasHeight) moveY = canvasHeight;

    // call superclass method
    var moved = GridQuestion.Point.superclass.moveTo.call(this, moveX, moveY, preventSnap);

    if (moved)
    {
        // check if has any lines that need updating
        var lines = this.getLines();
        for (var i = 0; i < lines.length; i++) this.question.fireLazy('moveEntity', lines[i]);
    }

    return moved;
};

GridQuestion.Point.prototype.getLines = function()
{
	var pointLines = [];
	var lines = this.question.getLines();
	
	for(var i = 0; i < lines.length; i++)
	{
		var line = lines[i];
		
		if (this == line.source || this == line.target) 
		{
			pointLines.push(line);
		}
	}
	
	return pointLines;
};
	
// Move all the lines from this point to another point
GridQuestion.Point.prototype.moveLines = function(toPoint)
{
	if (!toPoint) return false;
	
	// get all this points lines
	var fromLines = this.getLines();

	// check if there are any lines on our current point we need to move to the final point
	for(var i = 0; i < fromLines.length; i++)
	{
		var fromLine = fromLines[i];
		
		// is the from point the source or the target (we need to know to create the new line properly)
		var isSource = (fromLine.source == this);
		
		// remove line on from point
		this.question.deleteLine(fromLine);
		
		// add line to new point
		if (isSource) this.question.addLine(toPoint, fromLine.target, fromLine.dirType);
		else this.question.addLine(fromLine.source, toPoint, fromLine.dirType);
	}
	
	return true;
};

// check if this point intersects another point
GridQuestion.Point.prototype.intersect = function(that)
{
	var c1 = this.get2D(),
		r1 = this.radius,
		c2 = that.get2D(),
		r2 = that.radius;
	
	return (Intersection.intersectCircleCircle(c1, r1, c2, r2).status != 'Outside');
};

// check if this point intersects with a line
GridQuestion.Point.prototype.intersectLine = function(line)
{
	var c = this.get2D(),
		r = this.radius, 
		a1 = line.source.get2D(), 
		a2 = line.target.get2D()
	
	return (Intersection.intersectCircleLine(c, r, a1, a2).source != 'Outside');
};

// get all the points that intersect with this point
GridQuestion.Point.prototype.getIntersections = function()
{
	var intersectingPoints = [];
	var points = this.question.getPoints();
	
	for(var i = 0; i < points.length; i++)
	{
		var point = points[i];
		
		if (this != point && this.intersect(point))
		{
			intersectingPoints.push(point);
		}
	}
	
	return intersectingPoints;
};

/* SNAPPOINT CLASS */

GridQuestion.SnapPoint = function(question, x, y, radius, snapRadius)
{
	GridQuestion.SnapPoint.superclass.constructor.call(this, question, x, y, radius);
	this.snapRadius = snapRadius;
};

Lang.extend(GridQuestion.SnapPoint, GridQuestion.Point);

/* LINE CLASS */

GridQuestion.Line = function(question, source, target, dirType)
{
	GridQuestion.Line.superclass.constructor.call(this, question);
	this.source = source;
	this.target = target;
	this.dirType = dirType;
};

Lang.extend(GridQuestion.Line, GridQuestion.Base);

// GridQuestion.Line.prototype.toString = function() { return '(' + this.source + '),(' + this.target + ')'; };

// get line length
GridQuestion.Line.prototype.getLength = function()
{
	var p1 = this.source.get2D();
	var p2 = this.target.get2D();
	return p1.distanceFrom(p2);
};

// check if this line intersects with another line
GridQuestion.Line.prototype.intersect = function(line)
{
	var a1 = this.source.get2D(),
		a2 = this.target.get2D(),
		b1 = line.source.get2D(),
		b2 = line.target.get2D();
		
	return (Intersection.intersectLineLine(a1, a2, b1, b2).status == 'Intersection');
};

// get the distance from this line to another line
GridQuestion.Line.prototype.distanceFrom = function(line)
{
	// if the lines intersect then the distance is 0
	if (this.intersect(line)) return 0;
	
	// try each of the four points vertices with the other segment
	var distances = [];
	distances.push(this.source.distanceFromLine(line));
	distances.push(this.target.distanceFromLine(line));
	distances.push(line.source.distanceFromLine(this));
	distances.push(line.target.distanceFromLine(this));
	
	return Math.min.apply(Math, distances); // return shortest distance
};

// Get the slope of the line.
// NOTE: Undefined Slope and Zero Slope - http://mathforum.org/library/drmath/view/57310.html
GridQuestion.Line.prototype.getSlope = function()
{
    var denom = this.target.x - this.source.x;
    var num = this.target.y - this.source.y;

    if (denom == 0) return Infinity; // A vertical line does not have a slope.
    else if (num == 0) return 0.0; // A horizontal line has a slope which is the number zero.
    else return num / denom;
};

// Get the angle of the line.
GridQuestion.Line.prototype.getAngle = function()
{
    var x1 = this.source.x,
        y1 = this.source.y,
        x2 = this.target.x,
        y2 = this.target.y;

    return Math.atan2(y2 - y1, x2 - x1) * 180 / Math.PI;
};

// Get the angle of the line always from left to right.
GridQuestion.Line.prototype.getAngle2 = function()
{
    var x1 = this.source.x,
        y1 = this.source.y,
        x2 = this.target.x,
        y2 = this.target.y;

    // make sure x is increasing to the right, and y increasing up the page
    var radians = (x1 <= x2) ? Math.atan2(y2 - y1, x2 - x1) : Math.atan2(y1 - y2, x1 - x2);
    var degrees = (radians * 180 / Math.PI);
    return degrees;
};

/**
 * returns true if this line is parallel to the other line, within tolerance. Tolerance allows a difference of <tolerance>
 * over the length of the second line.
 * @param line - line to compare
 * @param tolerance - allowable total discrepancy from parallel across length of line
 * @return true if parallel within tolerance, false otherwise
 */
GridQuestion.Line.prototype.isParallelTo_Old = function(line, tolerance)
{
    // TODO: Review this http://code.google.com/p/boundary-generator/source/browse/trunk/src/geom/Vector.cs#80

    var tol = tolerance / line.getLength();
    var s1 = this.getSlope();
    var s2 = line.getSlope();

    if (s1 == Infinity)
    {
        if (s2 == Infinity) return true;
        if (Math.abs(line.target.x - line.source.x) < tolerance) return true;
        return false;
    }

    if (Math.abs(s1 - s2) <= tol) return true;

    return false;
};

GridQuestion.Line.prototype.isParallelTo = function(line, tolerance)
{
    var AngleTolerance = 0.05;
	var line1Vertical = false;
    var line1Horizontal = false;
	var line2Vertical = false;
	var line2Horizontal = false;
	
	var line1 = this;
	var line2 = line;
	
    //checking if vertical
	//checking line1
	if (line1.source.x >= line1.target.x){
	    if(Math.abs(line1.source.x - line1.target.x) < .25)
			line1Vertical = true;
	} else 	if(Math.abs(line1.target.x - line1.source.x) < .25)
			line1Vertical = true;
		
	//checking line2
	if (line2.source.x >= line2.target.x){
		if(Math.abs(line2.source.x - line2.target.x) < .25)
			line2Vertical = true;
	} else 	if(Math.abs(line2.target.x - line2.source.x) < .25)
			line2Vertical = true;

    //checking if horizontal
	//checking line1
	if (line1.source.y >= line1.target.y){
		if(Math.abs(line1.source.y - line1.target.y) < .25)
			line1Horizontal = true;
	} else 	if(Math.abs(line1.target.y - line1.source.y) < .25)
			line1Horizontal = true;

	//checking line2
	if (line2.source.y >= line2.target.y){
		if(Math.abs(line2.source.y - line2.target.y) < .25)
			line2Horizontal = true;
		} else 	if(Math.abs(line2.target.y - line2.source.y) < .25)
			line2Horizontal = true;


	if ((line1Vertical && line2Vertical) || (line1Horizontal && line2Horizontal))
                return true;
	//making sure no 0's or infinity in denominator
	if (!line1Vertical && !line2Vertical){
        var slope1 = (line1.target.y - line1.source.y) / (line1.target.x - line1.source.x);
        var slope2 = (line2.target.y - line2.source.y) / (line2.target.x - line2.source.x);
        if (slope1 == slope2) return true;
	}
	// turning line segments into vectors
    var newX1 = line1.target.x - line1.source.x;
    var newY1 = line1.target.y - line1.source.y;
    var newX2 = line2.target.x - line2.source.x;
    var newY2 = line2.target.y - line2.source.y;
    var dotProd = newX1 * newX2 + newY1 * newY2;
    var distance1 = distance(newX1, newY1);
    var distance2 = distance(newX2, newY2);
    var LengthProd = distance1 * distance2;
    if (Math.abs(dotProd) == Math.abs(LengthProd)) return true;
    
    var theta = AngleTolerance+1;
	if (LengthProd != 0) theta = Math.acos(Math.abs(dotProd/LengthProd));

    if (theta <= AngleTolerance) return true;
    return false;

    function distance(x, y){
        return (Math.sqrt(Math.pow((x),2) + Math.pow((y),2)));
    }
}

/*
GridQuestion.Line.prototype.isParallelTo = function(line, tolerance)
{
    var s1 = this.getAngle2();
    var s2 = line.getAngle2();
    var diff = Math.abs(s1 - s2);
    var isParallel = (diff <= tolerance);

    // console.log('parallel: ' + line.getID() + ' s1=' + s1 + ' s2=' + s2 + ' diff: ' + diff);
    return isParallel;
};
*/

// between this line and another line create a new line which is equal length (used for merging)
// NOTE: code taken from the java applet.. jLine.java line 204 jLine getLongestLine(jLine line)
GridQuestion.Line.prototype.getLongestLine = function(line)
{
	var lines = [];
	
	var d1 = this.source.distanceFrom(line.source);
	var d2 = this.source.distanceFrom(line.target);
	
	var start, end; // points
	
	if (d1 < d2)
	{
		start = line.source;
		end = line.target;
	} 
	else 
	{
		start = line.target;
		end = line.source;
	}
    
    lines[0] = new GridQuestion.Line(this.question, start, end);
    lines[1] = new GridQuestion.Line(this.question, this.source, end);
    lines[2] = new GridQuestion.Line(this.question, start, this.target);
    lines[3] = new GridQuestion.Line(this.question, end, this.target);

	var length = this.getLength();
	var longest = this;
	
	for (var i = 0; i < 4; i++)
	{
		var l = lines[i].getLength(); 
		
		if (lines[i].getLength() > length)
		{
			longest = lines[i];
			length = l;
		}
    }
	
	return longest;
};

// CLASS: Palette Image
GridQuestion.PaletteImage = function(question, name, url, width, height)
{
	GridQuestion.PaletteImage.superclass.constructor.call(this, question);
	this.name = name;
	this.url = url;
	this.width = width || 0;
	this.height = height || 0;
	this.loaded = false; // is the image loaded?
};

Lang.extend(GridQuestion.PaletteImage, GridQuestion.Base);

// get all the canvas images associated to this palette image
GridQuestion.PaletteImage.prototype.getImages = function()
{
	var paletteCanvasImages = [];
	var images = this.question.getImages();
	
	for(var i = 0; i < images.length; i++)
	{
		var image = images[i];
		
		if (image.name == this.name)
		{
			paletteCanvasImages.push(image);
		}
	}
	
	return paletteCanvasImages;
};

GridQuestion.PaletteImage.prototype.toString = function() { return this.name };

/* IMAGE CLASS */

GridQuestion.Image = function(question, name, x, y, width, height)
{
	GridQuestion.Image.superclass.constructor.call(this, question, x, y);
	
	this.name = name;
	this.width = width || 0;
	this.height = height || 0;
};

Lang.extend(GridQuestion.Image, GridQuestion.Position);

// get the image rect size
GridQuestion.Image.prototype.getSize = function() { return this.width * this.height; };

// get the palette image associated to this canvas image
GridQuestion.Image.prototype.getPaletteImage = function()
{
	var paletteImages = this.question.getPaletteImages();
	
	for(var i = 0; i < paletteImages.length; i++)
	{
		var paletteImage = paletteImages[i];
		if (paletteImage.name == this.name) return paletteImage;
	}
};

/**
 * find images that might be obscured by this image. That is those that are the same or nearly the same size
 * at nearly the same location
 * @param thisRec - the record that might do the obscuring
 * @param locx - location x coordinate
 * @param locy - location y coordinate
 * @param tolerance - locations within this distance are considered nearly the same
 * @return
 */
GridQuestion.prototype.otherImageInSamePlace = function(thisRec, realX, realY, tolerance)
{
    // NOTE: This code is taking from the java applet (ImageDB.java - line 254)
    var p = new Point2D(realX, realY);
    var size = thisRec.getSize();
    
    // get all images to look through
    var images = this.getImages();
    
    for(var i = 0; i < images.length; i++)
    {
        var rec = images[i];
        if (rec == thisRec) continue;
        
        var distance = rec.get2D().distanceFrom(p);
        
        if (distance < tolerance)
        {
            var sizeDif = Math.abs(rec.getSize() - size);
            if (sizeDif < (.15 * size)) return rec;
        }        
    }
    
    return null;
};

// move image (based on bottom/middle xy)
GridQuestion.Image.prototype.moveTo = function(moveX, moveY, preventSnap)
{
    // get bounding rect
    var topLeftX = moveX - Math.round(this.width / 2);
    var topLeftY = moveY - this.height;
    var bottomRightX = topLeftX + this.width;
    var bottomRightY = topLeftY + this.height;

    // get canvas info
    var canvasWidth = this.question.options.canvasWidth;
    var canvasHeight = this.question.options.canvasHeight;

    // fix any out of bounds coordinates to be within bounds
    if (topLeftX < 0) moveX = Math.round(this.width / 2);
    if (topLeftY < 0) moveY = this.height;
    if (bottomRightX > canvasWidth) moveX = canvasWidth - Math.round(this.width / 2);
    if (bottomRightY > canvasHeight) moveY = canvasHeight;

    // check if image is stacked on another image
    var tolerance = 4;
    var rec = this.question.otherImageInSamePlace(this, moveX, moveY, tolerance);

    while (rec != null)
    {
        moveX = rec.x + tolerance;
        moveY = rec.y + tolerance;
        rec = this.question.otherImageInSamePlace(this, moveX, moveY, tolerance);
    }

    // call superclass method
    return GridQuestion.Image.superclass.moveTo.call(this, moveX, moveY, preventSnap);
};

GridQuestion.Image.prototype.getBottomMiddle = function()
{
	return {
	    x: this.x + Math.round(this.width / 2),
	    y: this.y + this.height
	};
};

GridQuestion.Image.prototype.toString = function() { return this.name };

GridQuestion.BackgroundImage = function(question, url, x, y, width, height)
{
	GridQuestion.BackgroundImage.superclass.constructor.call(this, question, x, y);
	
	this.url = url;
	this.width = width || 0;
	this.height = height || 0;
	this.loaded = false; // is the image loaded?
};

Lang.extend(GridQuestion.BackgroundImage, GridQuestion.Position);

// Below is subset of 2D.js from Keven Lindsey http://www.kevlindev.com/ (UNMODIFIED)
// ALSO: This seems nice: http://code.google.com/p/renderengine/source/browse/trunk/engine/engine.math2d.js

// HELP: http://www.kevlindev.com/gui/math/point2d/index.htm

function Point2D(x,y){if(arguments.length>0){this.x=x;this.y=y;}}
Point2D.prototype.clone=function(){return new Point2D(this.x,this.y);};
Point2D.prototype.add=function(that){return new Point2D(this.x+that.x,this.y+that.y);};
Point2D.prototype.addEquals=function(that){this.x+=that.x;this.y+=that.y;return this;};
Point2D.prototype.offset=function(a,b){var result=0;if(!(b.x<=this.x||this.x+a.x<=0)){var t=b.x*a.y-a.x*b.y;var s;var d;if(t>0){if(this.x<0){s=this.x*a.y;d=s/a.x-this.y;}else if(this.x>0){s=this.x*b.y;d=s/b.x-this.y}else{d=-this.y;}}else{if(b.x<this.x+a.x){s=(b.x-this.x)*a.y;d=b.y-(this.y+s/a.x);}else if(b.x>this.x+a.x){s=(a.x+this.x)*b.y;d=s/b.x-(this.y+a.y);}else{d=b.y-(this.y+a.y);}}if(d>0){result=d;}}return result;};
Point2D.prototype.rmoveto=function(dx,dy){this.x+=dx;this.y+=dy;};
Point2D.prototype.scalarAdd=function(scalar){return new Point2D(this.x+scalar,this.y+scalar);};
Point2D.prototype.scalarAddEquals=function(scalar){this.x+=scalar;this.y+=scalar;return this;};
Point2D.prototype.subtract=function(that){return new Point2D(this.x-that.x,this.y-that.y);};
Point2D.prototype.subtractEquals=function(that){this.x-=that.x;this.y-=that.y;return this;};
Point2D.prototype.scalarSubtract=function(scalar){return new Point2D(this.x-scalar,this.y-scalar);};
Point2D.prototype.scalarSubtractEquals=function(scalar){this.x-=scalar;this.y-=scalar;return this;};
Point2D.prototype.multiply=function(scalar){return new Point2D(this.x*scalar,this.y*scalar);};
Point2D.prototype.multiplyEquals=function(scalar){this.x*=scalar;this.y*=scalar;return this;};
Point2D.prototype.divide=function(scalar){return new Point2D(this.x/scalar, this.y/scalar);};
Point2D.prototype.divideEquals=function(scalar){this.x/=scalar;this.y/=scalar;return this;};
Point2D.prototype.compare=function(that){return(this.x-that.x||this.y-that.y);};
Point2D.prototype.eq=function(that){return(this.x==that.x&&this.y==that.y);};
Point2D.prototype.lt=function(that){return(this.x<that.x&&this.y<that.y);};
Point2D.prototype.lte=function(that){return(this.x<=that.x&&this.y<=that.y);};
Point2D.prototype.gt=function(that){return(this.x>that.x&&this.y>that.y);};
Point2D.prototype.gte=function(that){return(this.x>=that.x&&this.y>=that.y);};
Point2D.prototype.distanceFrom=function(that){var dx=this.x-that.x;var dy=this.y-that.y;return Math.sqrt(dx*dx+dy*dy);};
Point2D.prototype.min=function(that){return new Point2D(Math.min(this.x,that.x),Math.min(this.y,that.y));};
Point2D.prototype.max=function(that){return new Point2D(Math.max(this.x,that.x),Math.max(this.y,that.y));};
Point2D.prototype.toString=function(){return this.x+","+this.y;};
Point2D.prototype.setXY=function(x,y){this.x=x;this.y=y;};
Point2D.prototype.setFromPoint=function(that){this.x=that.x;this.y=that.y;};
Point2D.prototype.swap=function(that){var x=this.x;var y=this.y;this.x=that.x;this.y=that.y;that.x=x;that.y=y;};
Point2D.prototype.lerp=function(that,t){return new Point2D(this.x+(that.x-this.x)*t,this.y+(that.y-this.y)*t);};

// HELP: http://www.kevlindev.com/gui/math/intersection/index.htm
function Intersection(status){if(arguments.length>0){this.init(status);}}
Intersection.prototype.init=function(status){this.status=status;this.points=new Array();};
Intersection.prototype.appendPoint=function(point){this.points.push(point);};
Intersection.prototype.appendPoints=function(points){this.points=this.points.concat(points);};

Intersection.intersectCircleCircle=function(c1,r1,c2,r2){var result;var r_max=r1+r2;var r_min=Math.abs(r1-r2);var c_dist=c1.distanceFrom(c2);if(c_dist>r_max){result=new Intersection("Outside");}else if(c_dist<r_min){result=new Intersection("Inside");}else{result=new Intersection("Intersection");var a=(r1*r1-r2*r2+c_dist*c_dist)/(2*c_dist);var h=Math.sqrt(r1*r1-a*a);var p=c1.lerp(c2,a/c_dist);var b=h/c_dist;result.points.push(new Point2D(p.x-b*(c2.y-c1.y),p.y+b*(c2.x-c1.x)));result.points.push(new Point2D(p.x+b*(c2.y-c1.y),p.y-b*(c2.x-c1.x)));}return result;};
Intersection.intersectCircleLine=function(c,r,a1,a2){var result;var a=(a2.x-a1.x)*(a2.x-a1.x)+(a2.y-a1.y)*(a2.y-a1.y);var b=2*((a2.x-a1.x)*(a1.x-c.x)+(a2.y-a1.y)*(a1.y-c.y));var cc=c.x*c.x+c.y*c.y+a1.x*a1.x+a1.y*a1.y-2*(c.x*a1.x+c.y*a1.y)-r*r;var deter=b*b-4*a*cc;if(deter<0){result=new Intersection("Outside");}else if(deter==0){result=new Intersection("Tangent");}else{var e=Math.sqrt(deter);var u1=(-b+e)/(2*a);var u2=(-b-e)/(2*a);if((u1<0||u1>1)&&(u2<0||u2>1)){if((u1<0&&u2<0)||(u1>1&&u2>1)){result=new Intersection("Outside");}else{result=new Intersection("Inside");}}else{result=new Intersection("Intersection");if(0<=u1&&u1<=1)result.points.push(a1.lerp(a2,u1));if(0<=u2&&u2<=1)result.points.push(a1.lerp(a2,u2));}}return result;};
Intersection.intersectLineLine=function(a1,a2,b1,b2){var result;var ua_t=(b2.x-b1.x)*(a1.y-b1.y)-(b2.y-b1.y)*(a1.x-b1.x);var ub_t=(a2.x-a1.x)*(a1.y-b1.y)-(a2.y-a1.y)*(a1.x-b1.x);var u_b=(b2.y-b1.y)*(a2.x-a1.x)-(b2.x-b1.x)*(a2.y-a1.y);if(u_b!=0){var ua=ua_t/u_b;var ub=ub_t/u_b;if(0<=ua&&ua<=1&&0<=ub&&ub<=1){result=new Intersection("Intersection");result.points.push(new Point2D(a1.x+ua*(a2.x-a1.x),a1.y+ua*(a2.y-a1.y)));}else{result=new Intersection("No Intersection");}}else{if(ua_t==0||ub_t==0){result=new Intersection("Coincident");}else{result=new Intersection("Parallel");}}return result;};
Intersection.intersectRectangleRectangle=function(a1,a2,b1,b2){var min=a1.min(a2);var max=a1.max(a2);var topRight=new Point2D(max.x,min.y);var bottomLeft=new Point2D(min.x,max.y);var inter1=Intersection.intersectLineRectangle(min,topRight,b1,b2);var inter2=Intersection.intersectLineRectangle(topRight,max,b1,b2);var inter3=Intersection.intersectLineRectangle(max,bottomLeft,b1,b2);var inter4=Intersection.intersectLineRectangle(bottomLeft,min,b1,b2);var result=new Intersection("No Intersection");result.appendPoints(inter1.points);result.appendPoints(inter2.points);result.appendPoints(inter3.points);result.appendPoints(inter4.points);if(result.points.length>0)result.status="Intersection";return result;};
Intersection.intersectCircleRectangle = function(c, r, r1, r2) { var min = r1.min(r2); var max = r1.max(r2); var topRight = new Point2D(max.x, min.y); var bottomLeft = new Point2D(min.x, max.y); var inter1 = Intersection.intersectCircleLine(c, r, min, topRight); var inter2 = Intersection.intersectCircleLine(c, r, topRight, max); var inter3 = Intersection.intersectCircleLine(c, r, max, bottomLeft); var inter4 = Intersection.intersectCircleLine(c, r, bottomLeft, min); var result = new Intersection("No Intersection"); result.appendPoints(inter1.points); result.appendPoints(inter2.points); result.appendPoints(inter3.points); result.appendPoints(inter4.points); if (result.points.length > 0) result.status = "Intersection"; else result.status = inter1.status; return result; };
Intersection.intersectLineRectangle = function(a1, a2, r1, r2) { var min = r1.min(r2); var max = r1.max(r2); var topRight = new Point2D(max.x, min.y); var bottomLeft = new Point2D(min.x, max.y); var inter1 = Intersection.intersectLineLine(min, topRight, a1, a2); var inter2 = Intersection.intersectLineLine(topRight, max, a1, a2); var inter3 = Intersection.intersectLineLine(max, bottomLeft, a1, a2); var inter4 = Intersection.intersectLineLine(bottomLeft, min, a1, a2); var result = new Intersection("No Intersection"); result.appendPoints(inter1.points); result.appendPoints(inter2.points); result.appendPoints(inter3.points); result.appendPoints(inter4.points); if (result.points.length > 0) result.status = "Intersection"; return result; };


