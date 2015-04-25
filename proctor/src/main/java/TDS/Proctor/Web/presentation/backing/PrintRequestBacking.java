/*******************************************************************************
 * Educational Online Test Delivery System Copyright (c) 2014 American
 * Institutes for Research
 * 
 * Distributed under the AIR Open Source License, Version 1.0 See accompanying
 * file AIR-License-1_0.txt or at http://www.smarterapp.org/documents/
 * American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package TDS.Proctor.Web.presentation.backing;

import java.util.UUID;

import org.apache.commons.lang.StringUtils;

import tds.blackbox.BlackboxSettings;
import AIR.Common.Utilities.TDSStringUtils;
import AIR.Common.Web.FacesContextHelper;
import AIR.Common.Web.WebHelper;
import AIR.Common.Web.taglib.ClientScriptContainerBean;
import TDS.Proctor.Presentation.IPrintRequestPresenterView;
import TDS.Proctor.Presentation.PresenterBase;
import TDS.Proctor.Presentation.PrintRequestPresenter;
import TDS.Proctor.Web.presentation.taglib.GlobalJavascript;

public/* partial */class PrintRequestBacking extends BasePage implements IPrintRequestPresenterView
{
  private PrintRequestPresenter _presenter;

  private String                _pageTitle;

  private String                _lblName;

  private UUID                  _requestKey;

  private PresenterBase         _presenterBase;

  // TODO Shajib: used in displayMessage, don't know who sets this
  private String                _msg;

  public PrintRequestBacking () {
    Page_Load ();
  }

  protected void Page_Load ()
  {
    try
    {
      // retrieve print request from the database store as _testeeRequest JSON
      // object
      setRequestKey (UUID.fromString (WebHelper.getQueryString ("requestKey")));

      _presenter = new PrintRequestPresenter (this);
      String response = _presenter.GetTesteeRequest ().getItemResponse ();
      if (!StringUtils.equals (response, null))
      {
        response = response.replace ("\"", "\\\"");
        response = response.replace ("\n", "\\n");
        response = response.replace ("\\frac", "\\\\frac");
        _presenter.GetTesteeRequest ().setItemResponse (response);
      }
      this.getClientScriptBlock ().registerClientScriptBlock ("_testeeRequest", _presenter.GetTesteeRequestJSON (), true);
      /*
       * this.ClientScript.RegisterClientScriptBlock(typeof (Page),
       * "_testeeRequest", _presenter.GetTesteeRequestJSON(), true);
       */
      /*
       * this.getClientScript ().addToJsCode (_presenter.GetTesteeRequestJSON
       * ());
       */
    } catch (Exception ex)
    {
      // log exception
      // TDSLogger.Application.Error(ex);
      return;
    }
  }

  public void setLblName (String value) {
    this._lblName = value;
  }

  public String getLblName ()
  {
    return this._lblName;
  }

  public void setMsg (String value) {
    _msg = value;
  }

  public String getMsg () {
    return _msg;
  }

  public String getShowMsg ()
  {
    if (StringUtils.equals (_msg, null))
      return "none";
    else
      return "block";
  }

  public void setPageTitle (String value)
  {
    _pageTitle = value;
  }

  public String getPageTitle ()
  {
    return this._pageTitle;
  }

  public void displayMessage (String msg)
  {
    /*
     * lblMessage.Visible = true; lblMessage.Text = msg;
     */
  }

  public String getBlackboxHandler () {
    return getBlackboxHandler (null, null);
  }

  public String getBlackboxHandler (String clientName, String shellName)
  {
    StringBuilder urlBuilder = new StringBuilder (getBlackboxUrl ("Blackbox.axd/loadSeed"));
    urlBuilder.append (TDSStringUtils.format ("?client={0}", !StringUtils.equals (clientName, null) ? clientName : BlackboxSettings.getClientName ()));
    urlBuilder.append (TDSStringUtils.format ("&shell={0}", !StringUtils.equals (shellName, null) ? shellName : BlackboxSettings.getShellName ()));

    // check for custom scripts file
    String scriptsFile = WebHelper.getQueryString ("scriptsFile");
    if (scriptsFile != null)
      urlBuilder.append (TDSStringUtils.format ("&scriptsFile={0}", scriptsFile));

    // check for custom scripts ID
    String scriptsID = WebHelper.getQueryString ("scriptsID");
    if (scriptsID != null)
      urlBuilder.append (TDSStringUtils.format ("&scriptsID={0}", scriptsID));

    // check for custom styles file
    String stylesFile = WebHelper.getQueryString ("stylesFile");
    if (stylesFile != null)
      urlBuilder.append (TDSStringUtils.format ("&stylesFile={0}", stylesFile));

    // check for custom styles
    String stylesID = WebHelper.getQueryString ("stylesID");
    if (stylesID != null)
      urlBuilder.append (TDSStringUtils.format ("&stylesID={0}", stylesID));

    return urlBuilder.toString ();
  }

  @Override
  public void setPresenterBase (PresenterBase presenter) {
    this._presenterBase = presenter;
  }

  @Override
  public void setRequestKey (UUID value) {
    this._requestKey = value;
  }

  @Override
  public UUID getRequestKey () {
    return _requestKey;
  }
}
