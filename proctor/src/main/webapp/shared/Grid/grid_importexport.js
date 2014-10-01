/*
 * Grid Import/Outport
 */

var GridImportExport = function(gquestion)
{
    var gridquestion = gquestion;
    var answerSpaceXml;
    //public APIs

    //getAnswerXml(): get student's response from SVG grid
    //loadItem (answerSpaceXml): load item question to SVG grid
    //loadAnswer(response): load a response into SVG grid
    //isStudentResponseValid(answerSpaceXml): check if student makes any response on the item

    this.isStudentResponseValid = function()
    {
        if (answerSpaceXml == null) return false;

        var gridq = new GridQuestion(); //validate response only
        gridq._paletteimages = gridquestion._paletteimages;
        var xmlobject = getDomXmlParser(answerSpaceXml);

        var presetAnswerList = xmlobject.getElementsByTagName("PreSetAnswerPart");
        if ((presetAnswerList) && (presetAnswerList[0]) && (presetAnswerList[0].childNodes[0]))
        {
            this.loadAnswer(serializeToString(presetAnswerList[0]), gridq);
        }

        if (!pointsEqual(gridq.getPoints(), gridquestion.getPoints())) return true;
        if (!edgesEqual(gridq.getLines(), gridquestion.getLines())) return true;
        if (!imagesEqual(gridq.getImages(), gridquestion.getImages())) return true;
        if (!arrowEqual(gridq.getLinesByDir("forward"), gridquestion.getLinesByDir("forward"))) return true;
        if (!doubleArrowEqual(gridq.getLinesByDir("both"), gridquestion.getLinesByDir("both"))) return true;

        return false;

        function pointsEqual(points1, points2)
        {
            if (points1.length != points2.length) return false;
            if (points1.length == 0) return true;
            for (var i = 0; i < points1.length; i++)
            {
                var found = false;
                for (var j = 0; j < points2.length; j++)
                {
                    if ((points1[i].x == points2[j].x) && (points1[i].y == points2[j].y)) found = true;
                }
                if (!found) return false;
            }
            return true;
        }

        function edgesEqual(edges1, edges2)
        {
            //alert (edges1.length + ':' + edges2.length);
            if (edges1.length != edges2.length) return false;
            if (edges1.length == 0) return true;
            for (var i = 0; i < edges1.length; i++)
            {
                var found = false;
                for (var j = 0; j < edges2.length; j++)
                {
                    if (((edges1[i].source.x == edges2[j].source.x) && (edges1[i].source.y == edges2[j].source.y)
                     && (edges1[i].target.x == edges2[j].target.x) && (edges1[i].target.y == edges2[j].target.y)) ||
                    ((edges1[i].source.x == edges2[j].target.x) && (edges1[i].source.y == edges2[j].target.y)
                     && (edges1[i].target.x == edges2[j].source.x) && (edges1[i].target.y == edges2[j].source.y))
                    ) found = true;
                }
                if (!found) return false;
            }
            return true;
        }

        function imagesEqual(images1, images2)
        {
            if (images1.length != images2.length) return false;
            if (images1.length == 0) return true;
            for (var i = 0; i < images1.length; i++)
            {
                var found = false;
                for (var j = 0; j < images2.length; j++)
                {
                    if ((images1[i].name == images2[j].name) && (images1[i].x == images2[j].x)
                        && (images1[i].y == images2[j].y)) found = true;
                }
                if (!found) return false;
            }
            return true;
        }

        function arrowEqual(arrows1, arrows2)
        {
            if (arrows1.length != arrows2.length) return false;
            if (arrows1.length == 0) return true;
            for (var i = 0; i < arrows1.length; i++)
            {
                var found = false;
                for (var j = 0; j < arrows2.length; j++)
                {
                    if (((arrows1[i].source.x == arrows2[j].source.x) && (arrows1[i].source.y == arrows2[j].source.y)
                     && (arrows1[i].target.x == arrows2[j].target.x) && (arrows1[i].target.y == arrows2[j].target.y)))
                        found = true;
                }
                if (!found) return false;
            }
            return true;

        }

        function doubleArrowEqual(da1, da2)
        {
            return edgesEqual(da1, da2);
        }

    }

    //export response from model
    this.getAnswerXml = function()
    {
        function getSnapPoints(xmlDoc, QuestionPartNode)
        {
            var SnapPointNode = xmlDoc.createElement('SnapPoint');
            var snapPoints = gridquestion.getSnapPoints();
            var str = "";
            for (var i = 0; i < snapPoints.length; i++)
            {
                if (i == 0) str = snapPoints[0].snapRadius + "@";
                str += snapPoints[i].x + "," + snapPoints[i].y;
                if (i != snapPoints.length - 1) str += ";";
            }
            var textNode = xmlDoc.createTextNode(str);
            SnapPointNode.appendChild(textNode);
            QuestionPartNode.appendChild(SnapPointNode);
        }

        function getTerminatedEdgeObjects(xmlDoc, ObjectSetNode)
        {

            var arrows = gridquestion.getLinesByDir("forward");
            for (var i = 0; i < arrows.length; i++)
            {
                //var tNode = document.createElement('TerminatedEdgeObject');
                var tNode = xmlDoc.createElement('TerminatedEdgeObject');

                var sourceXY = arrows[i].source.x + ',' + translateCoordinate(arrows[i].source.y);
                var targetXY = arrows[i].target.x + ',' + translateCoordinate(arrows[i].target.y);
                var str = "(" + sourceXY + "),(" + targetXY + "),Type-1";

                var textNode = xmlDoc.createTextNode(str);
                tNode.appendChild(textNode);
                ObjectSetNode.appendChild(tNode);
            }

            var doubleArrows = gridquestion.getLinesByDir("both");
            for (var i = 0; i < doubleArrows.length; i++)
            {
                var tNode = xmlDoc.createElement('TerminatedEdgeObject');

                var sourceXY = doubleArrows[i].source.x + ',' + translateCoordinate(doubleArrows[i].source.y);
                var targetXY = doubleArrows[i].target.x + ',' + translateCoordinate(doubleArrows[i].target.y);
                var str = "(" + sourceXY + "),(" + targetXY + "),Type-2";

                var textNode = xmlDoc.createTextNode(str);
                tNode.appendChild(textNode);
                ObjectSetNode.appendChild(tNode);
            }
        }

        function createAtomicObject(xmlDoc, ObjectSetNode)
        {
            var images = gridquestion.getImages();
            for (var i = 0; i < images.length; i++)
            {
                var AtomicObjectNode = xmlDoc.createElement('AtomicObject');

                // NOTE: Images need to be saved from the bottom/middle xy
                var pointXY = images[i].x + "," + translateCoordinate(images[i].y);
                var str = "{" + images[i].name + "(" + pointXY + ")}";

                var TextNode = xmlDoc.createTextNode(str);
                AtomicObjectNode.appendChild(TextNode);
                ObjectSetNode.appendChild(AtomicObjectNode);
            }
        }

        function createPointObject(xmlDoc, ObjectSetNode, points)
        {
            if (!(points)) return;

            for (var i = 0; i < points.length; i++)
            {

                var ObjectNode = xmlDoc.createElement('Object');
                var PointVectorNode = xmlDoc.createElement('PointVector');
                var EdgeVectorNode = xmlDoc.createElement('EdgeVector');
                var LabelListNode = xmlDoc.createElement('LabelList');
                var ValueListNode = xmlDoc.createElement('ValueList');

                var pointStr = "{(" + points[i].x + ',' + translateCoordinate(points[i].y) + ")}";
                var PointTextNode = xmlDoc.createTextNode(pointStr);
                PointVectorNode.appendChild(PointTextNode);

                var edgeStr = " {} ";
                var EdgeTextNode = xmlDoc.createTextNode(edgeStr);
                EdgeVectorNode.appendChild(EdgeTextNode);

                var labelStr = " {} ";
                var LabelTextNode = xmlDoc.createTextNode(labelStr);
                LabelListNode.appendChild(LabelTextNode);

                var valueStr = " {} ";
                var ValueTextNode = xmlDoc.createTextNode(valueStr);
                ValueListNode.appendChild(ValueTextNode);

                ObjectNode.appendChild(PointVectorNode);
                ObjectNode.appendChild(EdgeVectorNode);
                ObjectNode.appendChild(LabelListNode);
                ObjectNode.appendChild(ValueListNode);

                ObjectSetNode.appendChild(ObjectNode);

            }
        }

        function createConnectedLinesObj(xmlDoc, ObjectSetNode, thisWorkingLines)
        {
            var ObjectNode = xmlDoc.createElement('Object');
            var PointVectorNode = xmlDoc.createElement('PointVector');
            var EdgeVectorNode = xmlDoc.createElement('EdgeVector');
            var LabelListNode = xmlDoc.createElement('LabelList');
            var ValueListNode = xmlDoc.createElement('ValueList');

            var pointsStr = "{";
            var linesStr = "{";
            var previousLeftPoint = "";
            var linePoints = [];
            for (var i = 0; i < thisWorkingLines.length; i++)
            {
                var sourceXY = thisWorkingLines[i].source.x + ',' + translateCoordinate(thisWorkingLines[i].source.y);
                var targetXY = thisWorkingLines[i].target.x + ',' + translateCoordinate(thisWorkingLines[i].target.y);

                linesStr += " {(" + sourceXY + '),(' + targetXY + ")}";

                if (i == 0)
                {
                    linePoints.push(sourceXY);
                    linePoints.push(targetXY);
                } else
                {
                    if (!findExistingItem(linePoints, sourceXY)) linePoints.push(sourceXY);
                    if (!findExistingItem(linePoints, targetXY)) linePoints.push(targetXY);
                }

            }

            //if (previousLeftPoint!="") pointsStr += "(" + previousLeftPoint + ")";
            //PointVectorNode.innerHTML = pointsStr + "}";
            for (var i = 0; i < linePoints.length; i++)
            {
                pointsStr += "(" + linePoints[i] + ")";
            }
            pointsStr += "}";

            var PointVectorTextNode = xmlDoc.createTextNode(pointsStr);
            PointVectorNode.appendChild(PointVectorTextNode);

            //EdgeVectorNode.innerHTML = linesStr + "}";
            linesStr += "}";
            var EdgeVectorTextNode = xmlDoc.createTextNode(linesStr);
            EdgeVectorNode.appendChild(EdgeVectorTextNode);


            var labelStr = " {} ";
            var LabelTextNode = xmlDoc.createTextNode(labelStr);
            LabelListNode.appendChild(LabelTextNode);

            var valueStr = " {} ";
            var ValueTextNode = xmlDoc.createTextNode(valueStr);
            ValueListNode.appendChild(ValueTextNode);


            ObjectNode.appendChild(PointVectorNode);
            ObjectNode.appendChild(EdgeVectorNode);
            ObjectNode.appendChild(LabelListNode);
            ObjectNode.appendChild(ValueListNode);

            ObjectSetNode.appendChild(ObjectNode);

        }

        function findExistingItem(array, item)
        {
            for (var i = 0; i < array.length; i++)
            {
                //alert (item + " vs " + array[i]);
                if (item == array[i]) return true;
            }
            return false;
        }

        function addLinePointsToQueue(pointQueue, line)
        {
            var rtnPointQueue = pointQueue;
            if (!pointExistsInQueue(pointQueue, line.source)) rtnPointQueue.push(line.source);
            if (!pointExistsInQueue(pointQueue, line.target)) rtnPointQueue.push(line.target);
            return rtnPointQueue;
        }

        function pointExistsInQueue(pointQueue, point)
        {
            for (var i = 0; i < pointQueue.length; i++)
            {
                if (point == pointQueue[i]) return true;
            }
            return false;
        }

        function getNewLinesConnectedToPoint(remainingLines, workingLines, pointQueue, point)
        {
            var newLines = [];
            for (var i = 0; i < remainingLines.length; i++)
            {
                if ((remainingLines[i].source == point) || (remainingLines[i].target == point))
                {
                    //if line not in workingLines
                    var found = false;
                    for (var j = 0; j < workingLines.length; j++)
                    {
                        if (workingLines[j] == remainingLines[i])
                        {
                            //alert ("found=true " + workingLines[j] + "==" + remainingLines[i]);
                            found = true;
                            break;
                        }
                    }
                    //alert ("found = " + found);
                    if (!found) newLines.push(remainingLines[i]);
                }
            }

            return newLines;
        }

        function getRemainingLines(remainingLines, thisWorkingLines)
        {
            var rLines = [];
            for (var i = 0; i < remainingLines.length; i++)
            {
                var found = false;
                for (var j = 0; j < thisWorkingLines.length; j++)
                {
                    if (remainingLines[i] == thisWorkingLines[j])
                    {
                        found = true;
                        break;
                    }
                }
                if (!found) rLines.push(remainingLines[i]);
            }
            //alert ("----------- remaining Lines: " + rLines);
            return rLines;

        }

        function identifyObjects(xmlDoc, ObjectSetNode)
        {
            var lines = gridquestion.getLinesByDir("none");
            var points = gridquestion.getPoints();

            //identify connected lines
            var remainingLines = gridquestion.getLinesByDir("none");

            while (remainingLines.length > 0)
            {

                var thisWorkingLines = [];
                var thisPointQueue = [];

                thisWorkingLines.push(remainingLines[0]);
                var newQueue = addLinePointsToQueue(thisPointQueue, remainingLines[0]);
                thisPointQueue = newQueue;

                while ((thisPointQueue) && (thisPointQueue.length > 0))
                {
                    var newPoint = thisPointQueue.shift();
                    var newLines = getNewLinesConnectedToPoint(remainingLines, thisWorkingLines, thisPointQueue, newPoint);
                    if (newLines)
                    {
                        for (var i = 0; i < newLines.length; i++)
                        {
                            thisWorkingLines.push(newLines[i]);
                            addLinePointsToQueue(thisPointQueue, newLines[i]);
                        }
                    }
                }
                var rLines = getRemainingLines(remainingLines, thisWorkingLines);
                remainingLines = rLines;

                if (thisWorkingLines.length > 0)
                {
                    createConnectedLinesObj(xmlDoc, ObjectSetNode, thisWorkingLines);
                }
            }

            //identify isolated points: points not associated with any lines including arrows

            var iPoints = getIsolatedPoints();
            if (iPoints.length > 0) createPointObject(xmlDoc, ObjectSetNode, iPoints);

            createAtomicObject(xmlDoc, ObjectSetNode);

        }

        function getIsolatedPoints()
        {
            var lines = gridquestion.getLines();
            var points = gridquestion.getPoints();
            var iPoints = [];

            for (var i = 0; i < points.length; i++)
            {
                var found = false;
                for (var j = 0; j < lines.length; j++)
                {
                    if ((points[i] == lines[j].source) || (points[i] == lines[j].target)) //all lines including directed lines
                    {
                        found = true;
                        break;
                    }
                }
                //alert ("iPoints: found="+found);
                if (!found) iPoints.push(points[i]);
            }
            return iPoints;
        }

        function getIndent(depth)
        {
            var str = "";
            for (var i = 0; i < depth * 3; i++)
            {
                str += " ";
            }
            return str;
        }

        //        function getIndentedText(xNode, depth)
        //        {
        //            var alignedText = getIndent(depth) + '<' + xNode.nodeName + '>';
        //            //alert ("children: " + AnswerSetNode.children.length);
        //            if (xNode.children.length < 1) 
        //            {
        //                if (xNode.childNodes[0])
        //                    return (alignedText + xNode.childNodes[0].nodeValue + '</' + xNode.nodeName + '>');
        //                else 
        //                    return (alignedText + '</' + xNode.nodeName + '>');
        //            }
        //            for (var i=0; i<xNode.children.length; i++)
        //            {
        //                if (alignedText.charAt(alignedText.length-1) == '\n')
        //                    alignedText += getIndentedText(xNode.childNodes[i], depth+1);
        //                else
        //                    alignedText += '\n' + getIndentedText(xNode.childNodes[i], depth+1);
        //            }
        //            
        //            if (alignedText.charAt(alignedText.length-1) == '\n')
        //            {
        //                return (alignedText + getIndent(depth) + '</' + xNode.nodeName + '>\n');
        //            } else
        //                return (alignedText + '\n' + getIndent(depth) + '</' + xNode.nodeName + '>\n');
        //        
        //        }


        function getIndentedText(xNode, depth)
        {
            var alignedText = '';
            var length = 0;

            if (xNode.nodeType == 3)
            {
                if (xNode.nodeValue)
                    return (alignedText + xNode.nodeValue);
                else
                    return (alignedText);
            }
            alignedText = getIndent(depth) + '<' + xNode.nodeName + '>';

            var length = alignedText.length;
            for (var i = 0; i < xNode.childNodes.length; i++)
            {
                //if (xNode.nodeName == 'SnapPoint') alert ("snpaPoint nodeType: " + xNode.childNodes[i].nodeType);
                if (xNode.childNodes[i].nodeType == 3)
                    alignedText += getIndentedText(xNode.childNodes[i], depth + 1);
                else
                    alignedText += '\n' + getIndentedText(xNode.childNodes[i], depth + 1);
            }

            var trimedText = alignedText.replace(/^\s+|\s+$/g, "");
            if ((trimedText.charAt(trimedText.length - 1) == '}') || (xNode.nodeName == 'TerminatedEdgeObject') || (alignedText.length == length))
            {
                return (alignedText + '</' + xNode.nodeName + '>');
            } else
            {
                return (alignedText + '\n' + getIndent(depth) + '</' + xNode.nodeName + '>');
            }
        }

        var xmlDoc = getDomXmlParser("");

        var AnswerSetNode = xmlDoc.createElement('AnswerSet');
        var QuestionNode = xmlDoc.createElement('Question');

        QuestionNode.setAttribute('id', gridquestion.id);
        AnswerSetNode.appendChild(QuestionNode);

        var QuestionPartNode = xmlDoc.createElement('QuestionPart');
        QuestionPartNode.setAttribute('id', 1);
        QuestionNode.appendChild(QuestionPartNode);


        var ObjectSetNode = xmlDoc.createElement('ObjectSet');
        QuestionPartNode.appendChild(ObjectSetNode);
        identifyObjects(xmlDoc, ObjectSetNode);

        var TerminatedEdgeObjectNode = xmlDoc.createElement('TerminatedEdgeObject');
        getTerminatedEdgeObjects(xmlDoc, ObjectSetNode);

        getSnapPoints(xmlDoc, QuestionPartNode);

        var currentTime = new Date();
        var month = currentTime.getMonth() + 1;
        var day = currentTime.getDate();
        var year = currentTime.getFullYear() + '';

        var hours = currentTime.getHours()
        var minutes = currentTime.getMinutes()

        var APM = 'AM';
        if (hours > 11) APM = 'PM';


        var timeInfo = month + '/' + day + '/' + year.substring(2, 4) + '  ' + hours + ':' + minutes + ' ' + APM;

        var header =
            '<?xml version="1.0" encoding="UTF-8"?>\n' +
            '<!-- MACHINE GENERATED ' + timeInfo + '. DO NOT EDIT -->\n' +
            '<!DOCTYPE AnswerSet [\n' +
            '<!ELEMENT AnswerSet (Question+)>\n' +
            '<!ELEMENT AtomicObject (#PCDATA)>\n' +
            '<!ELEMENT EdgeVector (#PCDATA)>\n' +
            '<!ELEMENT GridImageTestPoints (TestPoint*)>\n' +
            '<!ELEMENT LabelList (#PCDATA)>\n' +
            '<!ELEMENT Object (PointVector,EdgeVector,LabelList,ValueList)>\n' +
            '<!ELEMENT ObjectSet (Object,AtomicObject+)>\n' +
            '<!ELEMENT PointVector (#PCDATA)>\n' +
            '<!ELEMENT Question (QuestionPart)>\n' +
            '<!ATTLIST Question id NMTOKEN #REQUIRED>\n' +
            '<!ELEMENT QuestionPart (LabelList,GridImageTestPoints,ObjectSet)>\n' +
            '<!ATTLIST QuestionPart id NMTOKEN #REQUIRED>\n' +
            '<!ELEMENT TestPoint (#PCDATA)>\n' +
            '<!ELEMENT ValueList (#PCDATA)>\n' +
            ']>\n';

        var indentedResponse = getIndentedText(AnswerSetNode, 0);
        //add item id
        indentedResponse = indentedResponse.replace("<Question>", "<Question id=\"" + gridquestion.id + "\">");
        return (header + indentedResponse);
        //return (header + (new XMLSerializer()).serializeToString(AnswerSetNode));
    }




    //load Question (answerSpaceXml)
    this.loadItem = function(questionXml, callback)
    {

        answerSpaceXml = questionXml;

        function removeQuotes(str)
        {
            var first = str.indexOf('"');
            if (first == -1) return str;
            str = str.substring(first + 1);
            var second = str.lastIndexOf('"');
            if (second == -1) return str;
            return str.substring(0, second);
        }

        function getURL()
        {
            var pathname = window.location.pathname;
            var index = pathname.lastIndexOf("/");
            pathname = pathname.substring(0, index);
            var newURL = window.location.protocol + "//" + window.location.host + pathname;
            return newURL;
        }


        function addBackgroundImage(bkgNode)
        {

            var node = bkgNode.firstChild;
            var fileSpec = "";
            var position = "";
            while (node)
            {
                if (node.nodeName == "FileSpec")
                {
                    fileSpec = removeQuotes(node.childNodes[0].nodeValue);
                } else
                    if (node.nodeName == "Position")
                {
                    position = removeQuotes(node.childNodes[0].nodeValue);
                }

                if ((fileSpec != "") && (position != ""))
                {

                    var xy = position.split(','); //100,200 relative to upper-left cornor
                    var url = getURL();
                    if (fileSpec.startsWith("/"))
                        url += fileSpec;
                    else
                        url += "/" + fileSpec;

                    // load background image async
                    var backgroundLoader = new ImageLoader();
                    backgroundLoader.addImage(url);
                    backgroundLoader.load(function(htmlImages)
                    {
                        var htmlImage = htmlImages[0];
                        gridquestion.addBackgroundImage(htmlImage.src, xy[0] * 1, xy[1] * 1, htmlImage.width, htmlImage.height); // HTML IMAGE
                    });

                    //gridquestion.loadBackgroundImage(url, xy[0] * 1, translateCoordinate(xy[1] * 1)); // HTML IMAGE
                    return;
                }
                node = node.nextSibling;
            }
        }

        var paletteImagesToLoad = [];

        function addPalleteImage(palleteNode)
        {
            var node = palleteNode.firstChild;

            var labelName = "";
            var fileName = "";

            while (node)
            {
                if (node.nodeName == "FileSpec")
                {
                    fileName = removeQuotes(node.childNodes[0].nodeValue);

                } else
                    if (node.nodeName == "Label")
                {
                    labelName = removeQuotes(node.childNodes[0].nodeValue);
                }
                if ((fileName != "") && (labelName != ""))
                {
                    var url = getURL();
                    if (fileName.startsWith("/"))
                        url += fileName;
                    else
                        url += "/" + fileName;

                    paletteImagesToLoad.push({ labelName: YAHOO.lang.trim(labelName), url: url });

                    return;
                }
                node = node.nextSibling;
            }
        }

        //note to check quotes

        gridquestion.clearQuestion();

        // LOAD OPTIONS
        var xmlobject = getDomXmlParser(questionXml);

        var buttonNode = xmlobject.getElementsByTagName("ShowButtons");
        if ((buttonNode) && (buttonNode[0]) && (buttonNode[0].childNodes[0]) && (buttonNode[0].childNodes[0].nodeValue))
        {
            var buttons = removeQuotes(buttonNode[0].childNodes[0].nodeValue).split(",");
            for (var i = 0; i < buttons.length; i++)
            {
                gridquestion.options.addButton(buttons[i]);
            }
        }
        var selectionToleranceNode = xmlobject.getElementsByTagName("SelectionTolerance");
        if ((selectionToleranceNode) && (selectionToleranceNode[0]) && (selectionToleranceNode[0].childNodes[0]))
            gridquestion.options.selectionTolerance = parseInt(removeQuotes(selectionToleranceNode[0].childNodes[0].nodeValue));

        var gridSpacingNode = xmlobject.getElementsByTagName("GridSpacing");
        if ((gridSpacingNode) && (gridSpacingNode[0]) && (gridSpacingNode[0].childNodes[0]))
        {
            var spacingStr = gridSpacingNode[0].childNodes[0].nodeValue;
            spacingStr = removeQuotes(spacingStr.replace(/^\s+|\s+$/g, ""));
            var strSplit = spacingStr.split(",");
            gridquestion.options.gridSpacing = parseInt(strSplit[0].replace(/^\s+|\s+$/g, ""));
            if (strSplit[1].replace(/^\s+|\s+$/g, "") == "Y") gridquestion.options.snapToGrid = true;
            else gridquestion.options.snapToGrid = false;
        }

        var gridColorNode = xmlobject.getElementsByTagName("GridColor");
        if ((gridColorNode) && (gridColorNode[0]) && (gridColorNode[0].childNodes[0]))
        {
            gridquestion.options.gridColor = removeQuotes(gridColorNode[0].childNodes[0].nodeValue);

            // NOTE: If the grid lines is not set to "None" then show the grid lines
            gridquestion.options.showGridLines = (gridquestion.options.gridColor != 'None');
        }

        // BACKGROUND IMAGES
        var backgroundImageList = xmlobject.getElementsByTagName("ImageSpec");
        if ((backgroundImageList) && (backgroundImageList[0]) && (backgroundImageList[0].childNodes[0]))
        {
            for (var i = 0; i < backgroundImageList.length; i++)
            {
                addBackgroundImage(backgroundImageList[i]);
            }
        }

        // PALETTE IMAGES
        var palleteImageList = xmlobject.getElementsByTagName("IconSpec");
        if ((palleteImageList) && (palleteImageList[0]) && (palleteImageList[0].childNodes[0]))
        {
            for (var i = 0; i < palleteImageList.length; i++)
            {
                addPalleteImage(palleteImageList[i]);
            }
        }

        function loadPresetAnswer()
        {
            // PRESET ANSWER
            var presetAnswerList = xmlobject.getElementsByTagName("PreSetAnswerPart");
            if ((presetAnswerList) && (presetAnswerList[0]) && (presetAnswerList[0].childNodes[0]))
            {
                this.loadAnswer(serializeToString(presetAnswerList[0]));
            }

            // NOTE: Balaji said only if the palette has images and the preset answer does not have points/images do we show the palette
            if (gridquestion.getPaletteImages().length > 0 && gridquestion.getPoints().length == 0 && gridquestion.getImages().length == 0)
            {
                gridquestion.options.showPalette = true;
            }
            // fire call back
            if (typeof callback == 'function') callback();
        }

        var scope = this;

        if (paletteImagesToLoad.length > 0)
        {
            // create image loader for palette
            var paletteLoader = new ImageLoader();

            // add palette images to loader
            for (var i = 0; i < paletteImagesToLoad.length; i++)
            {
                var paletteImage = paletteImagesToLoad[i];
                paletteLoader.addImage(paletteImage.url);
            }

            // begin async image loading
            paletteLoader.load(function(htmlImages)
            {
                for (var i = 0; i < paletteImagesToLoad.length; i++)
                {
                    var paletteImage = paletteImagesToLoad[i];
                    var htmlImage = htmlImages[i];
                    gridquestion.addPaletteImage(paletteImage.labelName, htmlImage.src, htmlImage.width, htmlImage.height);
                }

                // call into load preset answer
                loadPresetAnswer.call(scope);
            });
        }
        else
        {
            loadPresetAnswer.call(scope);
        }
    };

    //load student response
    this.loadAnswer = function(response, gridq)
    {
        function addSnapPoint(snapPoint, gridq)
        {
            // check if there are any snap points
            if (snapPoint.childNodes.length == 0) return;

            var snapStr = snapPoint.childNodes[0].nodeValue;
            //alert ("snapStr: " + snapStr);
            var first = snapStr.indexOf("@");
            var snapRadius = parseInt(snapStr.substring(0, first));
            //alert ("radius: " + snapRadius);
            snapStr = snapStr.substring(first + 1) + ";";
            //alert ("second part of snapStr: " + snapStr); 
            var index = snapStr.indexOf(";");
            while (index != -1)
            {
                var pntStr = snapStr.substring(0, index);
                var indexComma = pntStr.indexOf(",");
                var x = parseInt(pntStr.substring(0, indexComma));
                var y = parseInt(pntStr.substring(indexComma + 1));
                //alert ("snap Point: x-y: " + x + "-" + y);
                //if (gridquestion.getSnapPointByXY(x,y)==null) 
                if (gridq == null) gridquestion.addSnapPoint(x, y, snapRadius);
                else gridq.addSnapPoint(x, y, snapRadius);
                snapStr = snapStr.substring(index + 1);
                index = snapStr.indexOf(";");
            }
        }

        function addAtomicObj(obj, gridq)
        {
            var atomicStr = obj.childNodes[0].nodeValue;
            //atomicStr = atomicStr.replace(" ", "");
            var first = atomicStr.indexOf("{");
            var second = atomicStr.indexOf("(");
            var third = atomicStr.indexOf(")");
            var labelName = atomicStr.substring(first + 1, second);
            //alert ("labelName=" + labelName);
            var pntStr = atomicStr.substring(second + 1, third);
            //alert ("image Position = " + pntStr);

            var indexComma = pntStr.indexOf(",");
            var x = parseInt(pntStr.substring(0, indexComma));
            var y = parseInt(pntStr.substring(indexComma + 1));
            //alert ("image Position: x-y: " + x + "-" + y);
            if (gridq == null)
                gridquestion.addImage(YAHOO.lang.trim(labelName), x, translateCoordinate(y)); // HTML IMAGE
            else
                gridq.addImage(YAHOO.lang.trim(labelName), x, translateCoordinate(y));

        }

        function addTerminatedEdgeObj(obj, gridq)
        {
            var arrowStr = obj.childNodes[0].nodeValue;
            var index = arrowStr.indexOf(",Type-");
            var type = arrowStr.substring(index + 6);

            // need to decide type: 0,1,2
            var dirType = "both";
            if (type == "1") dirType = "forward";
            parseSingleLine(arrowStr.substring(0, index), dirType, gridq);
        }

        function addObj(obj, gridq)
        {
            var node = obj.firstChild;
            while (node)
            {
                if (node.nodeName == "PointVector")
                {
                    parsePointVectorString(node.childNodes[0].nodeValue, gridq);
                }
                else if (node.nodeName == "EdgeVector")
                {
                    //alert (" EdgeVector: " + node.childNodes[0].nodeValue);
                    parseEdgeVectorString(node.childNodes[0].nodeValue, gridq);
                }
                node = node.nextSibling;
            }
        }

        function parsePointVectorString(pointsStr, gridq)
        {

            var indexLeft = pointsStr.indexOf("(");
            var indexRight = pointsStr.indexOf(")");
            while ((indexLeft != -1) && (indexRight != -1))
            {
                var pntStr = pointsStr.substring(indexLeft + 1, indexRight);
                pointsStr = pointsStr.substring(indexRight + 1);
                //alert (pntStr);

                var indexComma = pntStr.indexOf(",");
                var x = parseInt(pntStr.substring(0, indexComma));
                var y = parseInt(pntStr.substring(indexComma + 1));
                if (gridq == null)
                    gridquestion.addPoint(x, translateCoordinate(y));
                else
                {
                    gridq.addPoint(x, translateCoordinate(y));
                }
                indexLeft = pointsStr.indexOf("(");
                indexRight = pointsStr.indexOf(")");
            }
        }

        function parseEdgeVectorString(edgesStr, gridq)
        {

            var firstLeft = edgesStr.indexOf("{");
            var lastRight = edgesStr.lastIndexOf("}");
            edgesStr = edgesStr.substring(firstLeft + 1, lastRight);

            var indexLeft = edgesStr.indexOf("{");
            var indexRight = edgesStr.indexOf("}");

            while ((indexLeft != -1) && (indexRight != -1))
            {
                var eStr = edgesStr.substring(indexLeft + 1, indexRight);
                parseSingleLine(eStr, "none", gridq);
                edgesStr = edgesStr.substring(indexRight + 1);
                indexLeft = edgesStr.indexOf("{");
                indexRight = edgesStr.indexOf("}");
            }
        }

        function parseSingleLine(eStr, dirType, gridq)
        {
            //alert ("eStr: " + eStr);
            var indexLeft = eStr.indexOf("(");
            var indexRight = eStr.indexOf(")");
            var pntStr = eStr.substring(indexLeft + 1, indexRight);
            //alert ("first Point: " + pntStr);
            var indexComma = pntStr.indexOf(",");
            var x = parseInt(pntStr.substring(0, indexComma));
            var y = parseInt(pntStr.substring(indexComma + 1));
            var point1;

            if (gridq == null)
            {

                if (dirType != "none")
                    point1 = gridquestion.addPoint(x, translateCoordinate(y));
                else
                    point1 = getExistingPoint(x, translateCoordinate(y), gridq);
            } else
            {
                if (dirType != "none")
                    point1 = gridq.addPoint(x, translateCoordinate(y));
                else
                    point1 = getExistingPoint(x, translateCoordinate(y), gridq);
            }

            pntStr = eStr.substring(indexRight + 1);
            indexLeft = pntStr.indexOf("(");
            indexRight = pntStr.indexOf(")");
            pntStr = pntStr.substring(indexLeft + 1, indexRight);
            //alert ("second Point: " + pntStr);
            indexComma = pntStr.indexOf(",");
            x = parseInt(pntStr.substring(0, indexComma));
            y = parseInt(pntStr.substring(indexComma + 1));
            //alert ("Line: x,y=" + x + " " + y);
            var point2;

            if (gridq == null)
            {
                if (dirType != "none")
                    point2 = gridquestion.addPoint(x, translateCoordinate(y));
                else
                    point2 = getExistingPoint(x, translateCoordinate(y), gridq);

                if ((point1) && (point2))
                {
                    gridquestion.addLine(point1, point2, dirType);
                }
            } else
            {
                if (dirType != "none")
                    point2 = gridq.addPoint(x, translateCoordinate(y));
                else
                    point2 = getExistingPoint(x, translateCoordinate(y), gridq);

                if ((point1) && (point2))
                {
                    gridq.addLine(point1, point2, dirType);
                }

            }
        }

        function getExistingPoint(x, y, gridq)
        {
            var points;
            if (gridq == null)
                points = gridquestion.getPoints();
            else
                points = gridq.getPoints();

            for (var i = 0; i < points.length; i++)
            {
                if ((points[i].x == x) && (points[i].y == y))
                    return points[i];

            }
            return null;
        }


        // loadAnswer main

        if (gridq == null) gridquestion.clearResponse();
        else gridq.clearResponse();

        var xmlobject = getDomXmlParser(response);

        var objectList = xmlobject.getElementsByTagName("Object");
        if ((objectList) && (objectList[0]) && (objectList[0].childNodes[0]))
        {
            for (var i = 0; i < objectList.length; i++)
            {
                addObj(objectList[i], gridq);
            }
        }

        var atomicObjList = xmlobject.getElementsByTagName("AtomicObject");
        if ((atomicObjList) && (atomicObjList[0]) && (atomicObjList[0].childNodes[0]))
        {
            for (var i = 0; i < atomicObjList.length; i++)
            {
                addAtomicObj(atomicObjList[i], gridq);
            }
        }

        var terminatedEdgeList = xmlobject.getElementsByTagName("TerminatedEdgeObject");
        if ((terminatedEdgeList) && (terminatedEdgeList[0]) && (terminatedEdgeList[0].childNodes[0]))
        {
            for (var i = 0; i < terminatedEdgeList.length; i++)
            {
                addTerminatedEdgeObj(terminatedEdgeList[i], gridq);
            }
        }

        var snapPointList = xmlobject.getElementsByTagName("SnapPoint");
        if ((snapPointList) && (snapPointList[0]) && (snapPointList[0].childNodes[0]))
        {
            for (var i = 0; i < snapPointList.length; i++)
            {
                addSnapPoint(snapPointList[i], gridq);
            }
        }

        if (gridq == null) gridquestion.importing = true;

    }

    function translateCoordinate(y)
    {
        return (gridquestion.options.canvasHeight - y);
    }

    function getDomXmlParser(text)
    {
        var xmlDoc;

        if (window.DOMParser) 
		{
			var domParser = new DOMParser();
			xmlDoc = domParser.parseFromString(text, "text/xml");
        }
		else
        {
			// HACK: For IE to load xml you need to remove DTD
			if (text.indexOf(']>') != -1)
			{
				text = text.split(']>')[1];
			}
		
            xmlDoc = new ActiveXObject("Microsoft.XMLDOM");
            xmlDoc.async = "false";
            xmlDoc.loadXML(text);
        }

        return xmlDoc;
    }

    function serializeToString(node)
    {
        return node.xml || (new XMLSerializer()).serializeToString(node);
    }

}

// class used for async loading images
var ImageLoader = function()
{
    this.images = [];
    this.loadedImages = [];
    this.imagesLoaded = 0;

    this._callback = null;

    this._checkFinished = function()
    {
        this.imagesLoaded++;

        if (this.imagesLoaded == this.images.length)
        {
            if (typeof this._callback == 'function') this._callback(this.loadedImages);
        }
    };

    this.addImage = function(url)
    {
        url = url.replace('&amp;', '&');
        this.images.push(url);
    };

    this.load = function(callback)
    {
        var loader = this;
        this._callback = callback;

        for (var i = 0; i < this.images.length; i++)
        {
            this.loadedImages[i] = new Image();
            this.loadedImages[i].tries = 0;

            this.loadedImages[i].onload = function()
            {
                loader._checkFinished();
            };

            this.loadedImages[i].src = this.images[i];
        }

    };
};

String.prototype.startsWith = function(str)
{return (this.match("^"+str)==str)}