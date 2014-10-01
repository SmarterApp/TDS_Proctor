/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package TDS.Proctor.Web.presentation.taglib;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.faces.component.FacesComponent;
import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.apache.commons.lang3.StringUtils;

import AIR.Common.Web.FacesContextHelper;
import TDS.Proctor.Presentation.PresenterBase;
import TDS.Proctor.Sql.Data.Abstractions.IMessageService;
import TDS.Shared.Configuration.TDSSettings;
import TDS.Shared.Exceptions.ReturnStatusException;
import TDS.Shared.Exceptions.RuntimeReturnStatusException;

@FacesComponent (value = "GlobalJavascript")
public class GlobalJavascript extends UIComponentBase
{
  private String          _contextName    = null;
  private String          _clientMessages = null;
  private PresenterBase   _presenter      = null;
  // generate member attributes;
  private List<String>    _contexts       = null;
  private TDSSettings     _settings       = null;
  private IMessageService _messageService = null;

  public PresenterBase getPresenter () {
    return _presenter;
  }

  public void setPresenter (PresenterBase presenter) {
    this._presenter = presenter;
  }

  public String getContextName () {
    return _contextName;
  }

  public void setContextName (String name) {
    this._contextName = name;
  }

  public String getClientMessages () {
    return _clientMessages;
  }

  public void setClientMessages (String m) {
    this._clientMessages = m;
  }

  @Override
  public String getFamily () {
    return "GlobalJavascript";
  }

  @Override
  public void encodeAll (FacesContext context) throws IOException {
    _settings = FacesContextHelper.getBean (context, "tdsSettings", TDSSettings.class);
    _messageService = FacesContextHelper.getBean (context, "iMessageService", IMessageService.class);
    ResponseWriter output = context.getResponseWriter ();
    output.write ("<script text=\"text/javascript\">");
    output.write ("\n\r");
    writeJavascript (output);
    output.write ("\n\r");
    output.write ("</script>");
  }

  public void writeJavascript (ResponseWriter output) {
    try {
      GlobalJavascriptWriter writer = new GlobalJavascriptWriter (_messageService, _settings, output, getPresenter ());
      _contexts = new ArrayList<String> (Arrays.<String> asList (StringUtils.split (getContextName ().toLowerCase (), ',')));
      writer.writeProperties (); // required
      writeGlobalAccs (writer); // required for default.aspx and login.aspx
                                // only.
                                // this will be use for language switcher
      writeMessages (writer);
    } catch (ReturnStatusException exp) {
      throw new RuntimeReturnStatusException (exp);
    } catch (IOException exp) {
      throw new RuntimeException (exp);
    }
  }

  private void writeGlobalAccs (GlobalJavascriptWriter writer) throws IOException {
    if (_contexts == null)
      return;
    if (_contexts.contains ("login.aspx") || _contexts.contains ("default.aspx"))
      writer.writeGlobalAccs ();// get global accs list
  }

  private void writeMessages (GlobalJavascriptWriter writer) throws ReturnStatusException, IOException {
    if (_contexts == null)
      _contexts = new ArrayList<String> ();
    // GLOBAL
    _contexts.add ("CommonPage");

    writer.writeMessages (_contexts);
  }

}
