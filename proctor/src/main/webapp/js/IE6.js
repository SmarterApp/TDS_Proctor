function getWindowHeight() {
    var windowHeight = 0;
    if (typeof (window.innerHeight) == 'number') {
        windowHeight = window.innerHeight;
    }
    else {
        if (document.documentElement && document.documentElement.clientHeight) {
            windowHeight = document.documentElement.clientHeight;
        }
        else {
            if (document.body && document.body.clientHeight) {
                windowHeight = document.body.clientHeight;
            }
        }
    }
    return windowHeight;
}
function setBottonHalfHeight() {
    var height = getWindowHeight() - 84;
   
    var elem = document.getElementById("bottomHalf");
    if (elem == null || elem == null || height<0)
        return;
    
    elem.style.height = height + "px";
}

window.onload = function() {
    setBottonHalfHeight();
}
window.onresize = function() {
    setBottonHalfHeight();
}
