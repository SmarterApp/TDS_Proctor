/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package TDS.Proctor.Web.Handlers;

import AIR.Common.Configuration.AppSettingsHelper;
import AIR.Common.Web.Session.HttpContext;
import TDS.Proctor.Services.ProctorUserService;
import TDS.Proctor.Sql.Data.ProctorUser;
import TDS.Proctor.Web.VariablesCookie;
import TDS.Shared.Configuration.TDSSettings;
import TDS.Shared.Data.ReturnStatus;
import TDS.Shared.Exceptions.NoDataException;
import TDS.Shared.Exceptions.ReturnStatusException;
import TDS.Shared.Exceptions.TDSSecurityException;
import TDS.Shared.Web.UserCookie;
import org.opentestsystem.delivery.logging.LoggingExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// / <summary>
// / Base class for any TDS HTTP handler services
// / </summary>
public abstract class HttpHandlerBase implements BeanFactoryAware
{
private static final Logger _logger = LoggerFactory.getLogger(HttpHandlerBase.class);
  private UserCookie      _userInfo        = null;
  private VariablesCookie _variablesCookie = null;
  private ProctorUser     _user            = null;
  private BeanFactory     contextBeans     = null;
  private HttpContext     _currentContext  = HttpContext.getCurrentContext ();

  @Autowired
  private TDSSettings tdsSettings;
  /*
   * JavaScriptSerializer serializer = new JavaScriptSerializer(); private
   * Dictionary<string, Action> actions = new Dictionary<string,
   * Action>(StringComparer.CurrentCultureIgnoreCase);
   */
  public void setBeanFactory (BeanFactory beanFactory) throws BeansException {
    this.contextBeans = beanFactory;
    onBeanFactoryInitialized ();
    try {
      // this is just so that we can prepopulate the objects.
      // TODO Shiva should we be doing this at a different location?
      init ();
    } catch (TDSSecurityException exp) {
      _logger.error (exp.getMessage ());
    }
  }

  @ExceptionHandler ({ NoDataException.class })
  @ResponseBody
  public ReturnStatus handleNoDataException (NoDataException exp, HttpServletRequest request, HttpServletResponse response) {
    LoggingExceptionHandler.handleException(request, exp);
    _logger.error (exp.getMessage ());
    response.setStatus (HttpServletResponse.SC_OK);
    return exp.getReturnStatus ();
  }

  @ExceptionHandler ({ ReturnStatusException.class })
  @ResponseBody
  public ReturnStatus handleReturnStatusException (ReturnStatusException exp, HttpServletRequest request, HttpServletResponse response) {
    LoggingExceptionHandler.handleException(request, exp);
    _logger.error (exp.getMessage ());
    response.setStatus (HttpServletResponse.SC_OK);
    return exp.getReturnStatus ();
  }

  /*
   * public void ProcessRequest(HttpContext context) { this._currentContext =
   * context;
   * 
   * if (_currentContext.Request.PathInfo.Length == 0) {
   * ThrowError(HttpStatusCode.NotFound, "No method name was provided."); }
   * 
   * string methodName = _currentContext.Request.PathInfo.Remove(0, 1);
   * 
   * if (string.IsNullOrEmpty(methodName)) { ThrowError(HttpStatusCode.NotFound,
   * "Empty method name was provided."); }
   * 
   * Action methodAction;
   * 
   * // check if action matches a mapping if (actions.TryGetValue(methodName,
   * out methodAction)) { // run method try { methodAction(); } catch
   * (TDSHttpException te) { SetStatus((HttpStatusCode)te.GetHttpCode(),
   * te.Message); // intentional exception thrown with HTTP error codes provided
   * } catch (ReturnStatusException rse) // explicit error condition returned
   * from SP { SetStatus(HttpStatusCode.Forbidden, rse.ReturnStatus.Reason); //
   * 403 } catch (ThreadAbortException tae) { } catch (Exception e) {
   * SetStatus(HttpStatusCode.InternalServerError); //TDSLogger.Domain.Fatal(e);
   * 
   * //if (StudentPage.DebugMode || StudentPage.DevMode) //{ //
   * SetStatus(HttpStatusCode.InternalServerError, e.Message); //} //else //{ //
   * SetStatus(HttpStatusCode.InternalServerError); //} } } else { // method not
   * found!
   * //TDSLogger.Domain.Error(string.Format("The method '{0}' was not found.",
   * methodName)); SetStatus(HttpStatusCode.NotFound); // 500 } }
   * 
   * /// <summary> /// Maps path to a method /// </summary> /// <param
   * name="name">PathInfo of the url /// (e.x., you would register "methodName"
   * if the url looked like:
   * http://example.com/handler.ashx/methodName?param=test)</param> /// <param
   * name="action">the function to call</param> protected void MapMethod(string
   * name, Action action) { actions.Add(name, action); }
   * 
   * //protected HttpVerb HttpMethod //{ // get // { // switch
   * (_currentContext.Request.HttpMethod) // { // case "GET": return
   * HttpVerb.GET; // case "POST": return HttpVerb.POST; // case "PUT": return
   * HttpVerb.PUT; // case "HEAD": return HttpVerb.HEAD; // case "DELETE":
   * return HttpVerb.DELETE; // }
   * 
   * // return HttpVerb.Unknown; // } //}
   * 
   * protected void SetContentType(ContentType contentType) { switch
   * (contentType) { case ContentType.Text: _currentContext.Response.ContentType
   * = "text/plain"; break; case ContentType.Xml:
   * _currentContext.Response.ContentType = "text/xml"; break; case
   * ContentType.Html: _currentContext.Response.ContentType = "text/html";
   * break; case ContentType.Json: _currentContext.Response.ContentType =
   * "application/json"; break; } }
   * 
   * protected string ToJson(object target) { return
   * serializer.Serialize(target); }
   * 
   * protected T FromJson<T>(string json) { return
   * serializer.Deserialize<T>(json); }
   * 
   * protected T FromJson<T>(Stream stream) { StreamReader rdr = new
   * StreamReader(stream); return FromJson<T>(rdr.ReadToEnd()); }
   * 
   * /// <summary> /// Sends a json object to the response stream /// </summary>
   * /// <param name="value"></param> protected void SendJsonString(string
   * value) { SetContentType(ContentType.Json);
   * _currentContext.Response.Write(value); }
   * 
   * /// <summary> /// Sends a json object to the response stream /// </summary>
   * /// <param name="value"></param> protected void SendJsonObject(object
   * value) { string json = ToJson(value);
   * 
   * // check if the user requests a JS callback to be returned string callback
   * = _currentContext.Request.QueryString["callback"]; if
   * (!string.IsNullOrEmpty(callback)) { json = string.Format("{0}({1});",
   * callback, json); }
   * 
   * SendJsonString(json); }
   * 
   * /// <summary> /// Gets a json object from the request stream /// </summary>
   * /// <typeparam name="T"></typeparam> /// <returns></returns> protected T
   * GetJsonObject<T>() { T value = default(T);
   * 
   * try { value = FromJson<T>(_currentContext.Request.InputStream); } catch {
   * // ignore errors }
   * 
   * return value; }
   * 
   * protected string GetQueryString(string name) { string queryValue =
   * _currentContext.Request.QueryString[name];
   * 
   * if (string.IsNullOrEmpty(queryValue)) { return string.Empty; } else {
   * return queryValue; } }
   * 
   * protected T GetQueryObject<T>(string name) { T value = default(T); string
   * queryValue = _currentContext.Request.QueryString[name];
   * 
   * if (!string.IsNullOrEmpty(queryValue)) { value = ChangeType<T>(queryValue);
   * }
   * 
   * return value; }
   * 
   * protected List<T> GetQueryObjects<T>(string name) { List<T> values = new
   * List<T>();
   * 
   * string[] queryValues = _currentContext.Request.QueryString.GetValues(name);
   * 
   * foreach (string queryValue in queryValues) { T value =
   * ChangeType<T>(queryValue); values.Add(value); }
   * 
   * return values; }
   * 
   * protected string GetFormString(string name) { string queryValue =
   * _currentContext.Request.Form[name];
   * 
   * if (string.IsNullOrEmpty(queryValue)) { return string.Empty; } else {
   * return queryValue; } }
   * 
   * protected T GetFormObject<T>(string name) { T value = default(T); string
   * queryValue = _currentContext.Request.Form[name];
   * 
   * if (!string.IsNullOrEmpty(queryValue)) { value = ChangeType<T>(queryValue);
   * }
   * 
   * return value; }
   * 
   * protected List<T> GetFormObjects<T>(string name) { List<T> values = new
   * List<T>();
   * 
   * string[] queryValues = _currentContext.Request.Form.GetValues(name);
   * 
   * foreach (string queryValue in queryValues) { T value =
   * ChangeType<T>(queryValue); values.Add(value); }
   * 
   * return values; }
   * 
   * private static T ChangeType<T>(object value) { Type conversionType =
   * typeof(T);
   * 
   * if (conversionType.IsGenericType &&
   * conversionType.GetGenericTypeDefinition().Equals(typeof(Nullable<>))) { if
   * (value == null) return default(T);
   * 
   * NullableConverter nullableConverter = new
   * NullableConverter(conversionType); conversionType =
   * nullableConverter.UnderlyingType; }
   * 
   * return (T)Convert.ChangeType(value, conversionType); }
   * 
   * protected void SetStatus(HttpStatusCode statusCode, string
   * statusDescription, params object[] values) {
   * _currentContext.Response.StatusCode = (int)statusCode;
   * 
   * if (!string.IsNullOrEmpty(statusDescription)) {
   * _currentContext.Response.StatusDescription =
   * string.Format(statusDescription, values); } }
   * 
   * protected void SetStatus(HttpStatusCode statusCode) { SetStatus(statusCode,
   * null); }
   * 
   * protected static void ThrowError(HttpStatusCode statusCode, string
   * statusDescription, params object[] values) { throw new
   * TDSHttpException(statusCode, string.Format(statusDescription, values)); }
   */

  protected HttpContext getCurrentContext () {
    return _currentContext;
  }

  protected UserCookie getUserInfo () {
    if (_userInfo == null) {
      _userInfo = new UserCookie (getCurrentContext (), getUserInfoCookieName ());
    }
    return _userInfo;
  }

  public ProctorUser getUser () {
    if (_user == null) {
      _user = ProctorUserService.loadUserFromCookie (getUserInfo ());
    }
    return _user;
  }

  protected VariablesCookie getVariablesCookie () {
    if (_variablesCookie == null) {
      _variablesCookie = new VariablesCookie (_currentContext,getVariablesCookieName ());
    }
    return _variablesCookie;
  }
  
  
  // returning the user so it can be used after the authentication check instead of the code calling getUser() again
  protected ProctorUser checkAuthenticated () throws TDSSecurityException {
    /*
     * This is what was in the .NET code.
     */
    // if (HttpContext.Current == null ||
    // !HttpContext.Current.User.Identity.IsAuthenticated)
    // {
    // throw new TDSSecurityException();
    // }
    ProctorUser user = getUser();

    if (!user.isAuth ())
      throw new TDSSecurityException ();

    return user;
  }

  /*
   * /// <summary> /// Determines if this httphandler will be kept around for
   * use by all requests (meaning you shouldn't keep state in internal fields)
   * /// </summary> public bool IsReusable { get { return false; } }
   * 
   * // write out to http stream public void Write(string s, params object[]
   * values) { this.CurrentContext.Response.Write(string.Format(s, values)); }
   * 
   * // below is WCF serializers public T JsonDeserialize<T>() {
   * DataContractJsonSerializer jsonSerializer = new
   * DataContractJsonSerializer(typeof(T)); return
   * (T)jsonSerializer.ReadObject(CurrentContext.Request.InputStream); }
   * 
   * public void JsonSeserializeToStream<T>(T o) { DataContractJsonSerializer
   * jsonSerializer = new DataContractJsonSerializer(typeof(T));
   * jsonSerializer.WriteObject(CurrentContext.Response.OutputStream, o); }
   * 
   * public string JsonSeserializeToString<T>(T o) { DataContractJsonSerializer
   * jsonSerializer = new DataContractJsonSerializer(typeof(T));
   * 
   * string json; using (MemoryStream stream = new MemoryStream()) {
   * jsonSerializer.WriteObject(stream, o); json =
   * Encoding.Default.GetString(stream.ToArray()); }
   * 
   * return json; }
   */

  protected <T> T getBean (String name, Class<T> c) {
    return this.contextBeans.getBean (name, c);
  }

  protected abstract void onBeanFactoryInitialized ();

  protected HttpHandlerBase () {
    // WebRequestMethods.Http.Post;
    // HttpVerb.PUT;
  }

  private void init () throws TDSSecurityException {
    // set all the appropriate cookies;
    getVariablesCookie ();
    getUserInfo ();
    getUser ();
  }
  
  private String getSysCookieName (String type) {
    String appName = tdsSettings.getAppName ();
    String checkInSys = (isCheckinSite ()) ? "CheckIn-" : "";
    return AIR.Common.Utilities.TDSStringUtils.format ("TDS-{0}-{1}{2}", appName, checkInSys, type);
  }

  private String getUserInfoCookieName () {
    return getSysCookieName ("UserInfo");
  }

  private String getVariablesCookieName () {
    return getSysCookieName ("Variables");
  }
  
  private boolean isCheckinSite () {
    return AppSettingsHelper.getBoolean ("IsCheckinSite", false);
  }
}
