var GridPalette = function(grid)
{
	this.grid = grid;
	this.ui = grid.ui;
	this.question = grid.question;
	this.canvas = grid.canvas;

	// is there something selected in the palette
	this._selectedImage = null;

	// ID of the image we are moving (NULL if not moving anything)
	this.moving = false;

	ErrorHandler.wrapFunctions(this, ['processPaletteEvents', 'subscribeToModelEvents']);
}

// clear the moving palette image
GridPalette.prototype.clearMoving = function()
{
    this.moving = false;

    var imageID = this._selectedImage.getID();
    var cloneID = imageID + '_clone';
    this.ui.removeElement(cloneID);
    this.grid.setModeHint();
};

GridPalette.prototype.getSelected = function() { return this._selectedImage; };

GridPalette.prototype.hasSelected = function () { return this._selectedImage != null; };

GridPalette.prototype.setSelected = function(paletteImage)
{
    // BUG #14224: If you use keyboard for adding palette image while moving an image you will have unset palette image
    if (this.moving) this.clearMoving();

    //check if image is already selected
    if (this._selectedImage && this._selectedImage == paletteImage) return false;

    // clear existing selection
    if (this._selectedImage)
    {
        var id = this._selectedImage.getID() + '_border';
        this.ui.deselectPaletteImage();
        logger.debug('palette unselected: ' + id);
    }

    // set new selection
    this._selectedImage = paletteImage || null;

    // make new selection
    if (paletteImage)
    {
        var id = paletteImage.getID();
        this.ui.selectPaletteImage(id);
        logger.debug('palette selected: ' + id);

        // reset mode and deselect any canvas items
        this.grid.setMode('move');
        this.grid.canvas.clearSelected();
    }

    return true;
};

GridPalette.prototype.clearSelected = function()
{
	return this.setSelected(null);
};

GridPalette.prototype.processPaletteEvents = function(evt)
{
    if (!this.moving && evt.name == 'mousedown')
    {
        // check if the click occured on palette image
        if (evt.target.nodeName == 'image' && evt.target.id.indexOf('paletteimage_') != -1)
        {
            this.setSelected(this.question.getEntity(evt.target.id));
        }
        else
        {
            this.clearSelected();
        }
    }

    var paletteImage = this.getSelected();
    if (paletteImage == null) return;

    // get the cloned palette image id
    var imageID = paletteImage.getID();
    var cloneID = imageID + '_clone';

    var gridX = evt.currentPosition.x - Math.round(paletteImage.width / 2);
    var gridY = evt.currentPosition.y - paletteImage.height;

    // BEGIN MOVE
    if (!this.moving && (evt.name == 'mousedown' || evt.name == 'dragbegin'))
    {
        this.moving = true;

        // clone palette image
        this.ui.clonePaletteImage(imageID, cloneID);
        this.grid.setHint('DraggingObject');
    }
    // MOVE
    else if (this.moving && (evt.name == 'mousemove' || evt.name == 'drag'))
    {
        // this.ui.movePaletteImage(cloneID, gridX, gridY);
        this.ui.movePaletteImage(cloneID, gridX, gridY);
    }
    // END MOVE
    else if (this.moving && (evt.name == 'mousedown' || evt.name == 'dragend'))
    {
        // remove move element
        this.clearMoving();

        // clear palette selection
        this.clearSelected();

        // get the dragged image coordinates based on where it was dropped on the canvas
        var canvasPosition = this.ui.translateElement('groupCanvas', evt.raw.clientX, evt.raw.clientY);

        // add palette image to canvas if the coordinates were valid
        if (canvasPosition && canvasPosition.x > 0 && canvasPosition.y > 0)
        {
            var image = this.question.addImage(paletteImage.name, canvasPosition.x, canvasPosition.y);
            this.grid.canvas.finalizeImage(image);
            // this.grid.canvas.setSelected(image);

            // BUG #15471: Focus remains in Palette when clicking in that area to drag an object to Grid answer space
            this.grid.setArea('canvas');
        }
    }
};

GridPalette.prototype.subscribeToModelEvents = function()
{
	var palette = this;
	var ui = this.ui;
	var question = this.question;
	
	// ADD
	this.question.subscribe('addEntity', function(entity)
	{
		var id = entity.getID();

		if (entity instanceof GridQuestion.PaletteImage)
		{
			ui.createPaletteImage(id, entity.width, entity.height, entity.url);
		}
	});
	
	// DELETE
	this.question.subscribe('deleteEntity', function(entity)
	{
		var id = entity.getID();
		
		if (entity instanceof GridQuestion.PaletteImage)
		{
			ui.removePaletteImage(id);
		}
	});
};

GridPalette.prototype.processKeyEvent = function(evt)
{
    var selected = this.getSelected();

    var paletteImages = this.question.getPaletteImages();

    if (evt.key == 'space')
    {
        var image = this.question.addImage(selected.name, 0, 0);
        this.grid.canvas.finalizeImage(image);
        this.grid.canvas.setSelected(image);
        this.grid.setArea('canvas');
        this.grid.setMode('move');
        this.clearSelected();
        this.grid.canvas.processKeyEvent(evt); // transfer the palette key event to the canvas (should trigger move action)
    }

    // if nothing is selected and up or down is pressed then just select the first palette image
    if (selected == null && (evt.key == 'up' || evt.key == 'down'))
    {
        this.setSelected(paletteImages[0]);
        return;
    }

    if (evt.key == 'up' || evt.key == 'down')
    {
        if (paletteImages.length == 0) return; // nothing to select

        var paletteIndex = -1;

        for (var i = 0; i < paletteImages.length; i++)
        {
            if (paletteImages[i] == selected)
            {
                paletteIndex = i;
                break;
            }
        }

        if (evt.key == 'down')
        {
            if (paletteIndex < paletteImages.length - 1)
            {
                this.setSelected(paletteImages[paletteIndex + 1]);
            }
            else
            {
                this.setSelected(paletteImages[0]);
            }
        }

        if (evt.key == 'up')
        {
            if (paletteIndex == 0)
            {
                this.setSelected(paletteImages[paletteImages.length - 1]);
            }
            else
            {
                this.setSelected(paletteImages[paletteIndex - 1]);
            }
        }

    }

};