//included this file below all form elements
var clicked = false;
window.onunload = confirmExit;

function confirmExit(e) {   
    if (document.all)
        e = event;

    if (!e)
        e = window.event;

    if (e) {
        if (clicked == false && (e.target == document || e.target == null || e.clientX < 0 || e.clientY < 0)) {
            window.open(gConfirmExitPage + gConfirmExitQueryStr, gConfirmExitWinName, 'height=600,width=800, toolbar=no, menubar=no, scrollbars=no, resizable=no,location=no, directories=no, status=no');
        }
    }
}

function setAClick() {
    clicked = true;
}

function cancelAClick() {
    clicked = false;
}
/*
YAHOO.util.Event.onDOMReady(function() {
    var ATagList = document.getElementsByTagName("a");
    if (ATagList && ATagList.length > 0) {
        for (var i = 0; i < ATagList.length; i++) {
            var a = ATagList[i];
            if (!a.onclick) {
                if (isLocal(a.href)) {
                    YAHOO.util.Event.addListener(a, 'click', setAClick);
                }
                else {
                    YAHOO.util.Event.addListener(a, 'click', cancelAClick);
                }
            }
            else
                YAHOO.util.Event.addListener(a, 'click', setAClick);
        }
    }
});
*/
function isLocal1() {
    var url;

    try {
        url = window.location.toString();
    }
    catch (e) {
        return false;
    }

    return (isInDomain(url));
}

function isLocal(src) {
    return (isInDomain(src) || src.indexOf('javascript') != -1 || src.indexOf('mailto') != -1 || src.indexOf('#') == 0);
}

function isInDomain(src) {
    var splitString = gTDS.appConfig.Local_Domains;
    var splitArray = splitString.split("|");
    var inDomain = false;
    var lSrc = src.toLowerCase();
    for (var j = 0; j < splitArray.length; j++) {
        if (lSrc.indexOf(splitArray[j]) != -1) {
            inDomain = true;
            break;
        }
    }
    return inDomain;
}