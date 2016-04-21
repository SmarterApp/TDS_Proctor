//included this file below all form elements
//TODO Shiva/Ravi : Use of gTDS.appConfig.contextPath here is unreliable as that script may not have been executed.
var gConfirmExitPage = gTDS.appConfig.contextPath + '/confirmExit.xhtml';
var gConfirmExitWinName = 'confirmExit_Proctor';
var gConfirmExitQueryStr = '?rc=1'; //require confirmation by default, to turn off, set to rc=0;
var gReturnPage = gTDS.appConfig.contextPath + '/default.xhtml';
var gHashValue = '#hppwd';
/////////////////////////////////////////////////////
/////Added variable to hold the logoutpage name./////
//////////////DON'T CHANGE VALUE/////////////////////
var gLogOutPage = 'shared/logout.xhtml?exl=false' //logout for only application and if this is the only app in CLS, logout
var gLogOutPageExl = 'shared/logout.xhtml?exl=true' //logout all