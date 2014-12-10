/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package TDS.Proctor.Web.presentation.backing;

import java.net.URISyntaxException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import AIR.Common.Configuration.AppSettingsHelper;
import AIR.Common.Utilities.TDSStringUtils;
import AIR.Common.Web.FacesContextHelper;
import AIR.Common.Web.UrlHelper;
import AIR.Common.Web.Session.HttpContext;
import AIR.Common.Web.taglib.ClientScript;
import AIR.Common.Web.taglib.ClientScriptContainerBean;
import TDS.Shared.Configuration.TDSSettings;

public class BasePage
{
  private HttpContext  httpContext  = HttpContext.getCurrentContext ();
  private TDSSettings  tdsSettings;
  private ClientScript clientScript = null;

  public BasePage () {
    tdsSettings = FacesContextHelper.getBean ("tdsSettings", TDSSettings.class);

    clientScript = getBean ("clientScriptBackingBean", ClientScriptContainerBean.class).getClientScript ();
  }

  public TDSSettings getTdsSettings () {
    return tdsSettings;
  }

  public HttpContext getCurrentContext () {
    return httpContext;
  }

  public ClientScript getClientScript () {
    return clientScript;
  }

  public void setClientScript (ClientScript clientScript) {
    this.clientScript = clientScript;
  }

  public String getLoginClientName () {
    String clientName = tdsSettings.getClientNameFromQueryString ();
    if (StringUtils.isEmpty (clientName))
      clientName = tdsSettings.getClientNameFromConfig ();
    return clientName;
  }

  //TODO Shiva: Do we need to use FacesContextHelper here or can we do a
  // direct injection.
  protected <T> T getBean (String beanName, final Class<T> clazz) {
    return FacesContextHelper.getBean (beanName, clazz);
  }
  
  public String buildClientMessage (String message) {
    return TDSStringUtils.format ("<span i18n-content=\"{0}\" class=\"messageBox\"></span>", message);
  }

  public String getClientStylePath (String filePath) {
    return filePath;// string.Format("Projects/{0}/{1}", ClientPath, filePath);
  }

  public ClientScript getClientScriptBlock () {
    return (FacesContextHelper.getBean ("clientScript", ClientScriptContainerBean.class)).getClientScript ();
  }

  protected HttpServletRequest getRequest () {
    return httpContext.getRequest ();
  }

  protected HttpServletResponse getResponse () {
    return httpContext.getResponse ();
  }

  public String getBlackboxUrl (String path)
  {
    String fullBlackboxUrl;
    String blackboxUrl = AppSettingsHelper.get ("BlackboxUrl");
    StringBuilder urlBuilder = null;

    // not set in appsettings, default to ~/Blackbox/
    if (StringUtils.isEmpty (blackboxUrl))
    {
      blackboxUrl = "~/Blackbox/";
    }

    try {
      urlBuilder = new StringBuilder (UrlHelper.resolveFullUrl (blackboxUrl));
    } catch (URISyntaxException e) {

    }

    // add path
    if (path != null)
      urlBuilder.append (path);

    return urlBuilder.toString ();
  }
  
}
