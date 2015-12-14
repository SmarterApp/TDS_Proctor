if (typeof(YAHOO) != 'undefined' && typeof(YUD) == 'undefined') YUD = YAHOO.util.Dom;

// zoom class, pass in a document and optional zoom css class name (to be the default)
function ContentZoom(defaultDoc, defaultZoomCSS, page)
{
    this.page = page;
    
    // the zoom CSS and factor for each level
    this.levels =
	[
		{ css: 'TDS_PS_Normal', factor: 1 },
		{ css: 'TDS_PS_Larger', factor: 1.25 },
		{ css: 'TDS_PS_Largest', factor: 1.5 }
	];

    // get level for a css name
    this.getLevel = function(zoomCSS)
    {
        if (zoomCSS != null)
        {
            for (var i = 0; i < this.levels.length; i++)
            {
                if (this.levels[i].css == zoomCSS) return i;
            }
        }

        return 0; // return 0 as default
    };

    // set default level
    this.defaultLevel = this.getLevel(defaultZoomCSS);

    this.previousLevel = -1;

    // set current level
    this.currentLevel = this.defaultLevel;

    // get the current css
    this.getCSS = function()
    {
        return this.levels[this.currentLevel].css;
    };

    // documents to perform zooming on
    this._documents = [];

    this.addDocument = function(doc)
    {
        this._documents.push(doc);
    };
    
    // add default document passed in
    this.addDocument(defaultDoc);
    
    // when this true only content images get changed
    this.contentImages = true;

    this._isContentImage = function(image) { return YUD.hasClass(image, 'Image'); };
    
    // set zoom level on a specific document
    this._setDocumentLevel = function(doc, newLevel)
    {
        // if this doc already has the zoom CSS set, then skip it
        if (this.currentLevel == newLevel && YUD.hasClass(doc.body, this.levels[newLevel].css)) return false;

        // makes sure the height is saved before performing any CSS
        this.saveHeight(doc);

        // remove current zoom css
        YUD.removeClass(doc.body, this.levels[this.currentLevel].css);

        // add new zoom css
        YUD.addClass(doc.body, this.levels[newLevel].css);

        // zoom images to new factor
        this.zoomImages(doc, this.levels[newLevel].factor)

        return true;
    };

    /**
    * Used to set a specific zoom level.
    *
    * @method zoom
    * @param  {int}  newLevel  One of the zoom levels in the levels array
    */
    this.setLevel = function(newLevel)
    {
        var levelChange = false;

        if (newLevel == null) return levelChange;

        if (typeof (newLevel) == 'string')
        {
            newLevel = this.getLevel(newLevel);
        }

        for (var i = 0; i < this._documents.length; i++)
        {
            if (this._setDocumentLevel(this._documents[i], newLevel))
            {
                levelChange = true;
            }
        }

        this.previousLevel = this.currentLevel;
        this.currentLevel = newLevel;

        // fire content manager zoom only if zoom changed on one of the documents
        if (levelChange && this.page)
        {
            ContentManager.firePageEvent('zoom', this.page, this);
        }

        return levelChange;
    };

    // Used to zoom in a level.
    this.zoomIn = function()
    {
        if (this.currentLevel < this.levels.length - 1)
        {
            return this.setLevel(this.currentLevel + 1);
        }
        else
        {
            return false;
        }
    };

    // Used to zoom out a level. 
    this.zoomOut = function()
    {
        if (this.currentLevel > 0)
        {
            return this.setLevel(this.currentLevel - 1);
        }
        else
        {
            return false;
        }
    };

    this.reset = function()
    {
        this.setLevel(this.defaultLevel);
    };

    /**
    * Used to make sure all the documents in the collection are the right css class. This can
    * be helpful for when you add a new iframe to the collection. 
    *
    * @method refresh
    */
    this.refresh = function()
    {
        this.setLevel(this.currentLevel);
    };

    /**
    * Used to zoom the images.
    *
    * @method	zoomImages
    * @param	{Object}	doc		The current HTML document.
    * @param	{int}		factor	The numeric option represents a magnifying factor. 
    * 			A zoom factor of 1 means a normal size. A zoom factor of 2, for example, means 
    * 			an element sized by 2x. A zoom factor of 0.5 will make an element half its original size.
    */
    this.zoomImages = function(doc, factor)
    {
        if (doc == null || doc.images == null) return;

        for (var i = 0; i < doc.images.length; i++)
        {
            var image = doc.images[i];

            // check if this is an image in the content
            if (this.contentImages && !this._isContentImage(image)) continue;

            // if we don't know the original height don't perform zooming
            if (!this._hasAttribute(image, "originalHeight")) continue;

            // perform zoom
            image.height = image.getAttribute('originalHeight') * factor;
            image.width = image.getAttribute('originalWidth') * factor;
        };
    };

    /**
    * Used to prepare zooming. This will get run on all documents everytime zooming is 
    * but will not do anything if height is already set.
    *
    * @method saveHeight
    * @param  {Object}  doc  The current HTML document.
    */
    this.saveHeight = function(doc)
    {
        // check if this doc is null or doesn't not have image array 
        if (doc == null || doc.images == null) return;

        // Loops through each image and remember its current dimensions.
        for (var i = 0; i < doc.images.length; i++)
        {
            var image = doc.images[i];

            // check if this is an image in the content
            if (this.contentImages && !this._isContentImage(image)) continue;

            // if we already know height skip this image
            if (this._hasAttribute(image, "originalHeight")) continue;

            // save original image dimensions only if they are greater than 0
            if (image.height > 0 && image.width > 0)
            {
                image.setAttribute('originalHeight', image.height);
                image.setAttribute('originalWidth', image.width);
            }
        };
    };

    // used to tell if an element has a specific attribute 
    // (since this DOM2 function and IE does not support, use this method instead)
    this._hasAttribute = function(ele, attr)
    {
        if (ele.hasAttribute) return ele.hasAttribute(attr);
        else return ele.getAttribute(attr);
    };
}
