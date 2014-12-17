/*******************************************************************************
 * Educational Online Test Delivery System Copyright (c) 2014 American
 * Institutes for Research
 * 
 * Distributed under the AIR Open Source License, Version 1.0 See accompanying
 * file AIR-License-1_0.txt or at http://www.smarterapp.org/documents/
 * American_Institutes_for_Research_Open_Source_Software_License.pdf
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

public class BasePage extends TDS.Shared.Web.BasePage
{
  public BasePage () {
  }

  public ClientScript getClientScriptBlock () {
    return getBean ("clientScript", ClientScriptContainerBean.class).getClientScript ();
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
