/*******************************************************************************
 * Educational Online Test Delivery System Copyright (c) 2014 American
 * Institutes for Research
 * 
 * Distributed under the AIR Open Source License, Version 1.0 See accompanying
 * file AIR-License-1_0.txt or at http://www.smarterapp.org/documents/
 * American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package TDS.Proctor.Web.presentation.taglib;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import javax.faces.context.ResponseWriter;

import org.apache.commons.lang3.StringUtils;

import AIR.Common.Configuration.AppSettingsHelper;
import AIR.Common.Utilities.TDSStringUtils;
import TDS.Proctor.Presentation.PresenterBase;
import TDS.Proctor.Sql.Data.AppConfig;
import TDS.Proctor.Sql.Data.Abstractions.IMessageService;
import TDS.Proctor.Sql.Data.Accommodations.AccsDTO;
import TDS.Shared.Configuration.TDSSettings;
import TDS.Shared.Exceptions.ReturnStatusException;
import TDS.Shared.Messages.MessageJson;
import TDS.Shared.Messages.MessageSystem;

import com.fasterxml.jackson.databind.ObjectMapper;

public class GlobalJavascriptWriter
{
  private ResponseWriter  _writer         = null;
  private PresenterBase   _presenter      = null;
  private TDSSettings     _settings       = null;
  private IMessageService _messageService = null;

  public GlobalJavascriptWriter (IMessageService service, TDSSettings settings, ResponseWriter textWriter, PresenterBase presenter) {
    _writer = textWriter;
    _presenter = presenter;
    _settings = settings;
    _messageService = service;
  }

  private <T> String getBooleanJS (T value) {
    if (value == null)
      return "false";
    return value.toString ().toLowerCase ();
  }

  public void writeProperties () throws IOException {
    _writer.write ("if (typeof(gTDS) == 'undefined') var gTDS = {}; if (typeof(gTDS.appConfig) == 'undefined') gTDS.appConfig = {};");
    _writer.write ("\n\r");
    String clientName = _settings.getClientName ();
    // client from cookie/web.config
    _writer.write (TDSStringUtils.format ("gTDS.appConfig.ClientName = \"{0}\"; ", clientName));
    _writer.write ("\n\r");

    // from web.config
    _writer.write (TDSStringUtils.format ("gTDS.appConfig.AppName = \"{0}\"; ", _settings.getAppName ()));
    _writer.write ("\n\r");

    // from P_GetConfigs
    if (_presenter != null) {
      AppConfig pAppConfig = _presenter.getAppConfigDB ();
      if (pAppConfig != null) {
        _writer.write (TDSStringUtils.format ("gTDS.appConfig.ClientPath = \"{0}\"; \r\n", pAppConfig.getClientPath ()));
        _writer.write (TDSStringUtils.format ("gTDS.appConfig.Environment = \"{0}\"; \r\n", pAppConfig.getEnvironment ()));
        _writer.write (TDSStringUtils.format ("gTDS.appConfig.IsOp = {0}; \r\n", getBooleanJS (new Boolean (pAppConfig.isOperational ()))));
        _writer.write (TDSStringUtils.format ("gTDS.appConfig.RefreshValue = {0}; \r\n", pAppConfig.getRefreshValue () * 1000));
        _writer.write (TDSStringUtils.format ("gTDS.appConfig.RefreshVM = {0}; \r\n", pAppConfig.getRefreshValueMultiplier ()));
        _writer.write (TDSStringUtils.format ("gTDS.appConfig.Timeout = {0}; \r\n", pAppConfig.getTimeout () * 60000)); // 120000);//pAppConfig.Timeout
                                                                                                                        // *
                                                                                                                        // 60000);
        _writer.write (TDSStringUtils.format ("gTDS.appConfig.PingInterval = 180000; \r\n"));
        _writer.write (TDSStringUtils.format ("gTDS.appConfig.TimeZoneOffset = {0}; \r\n", pAppConfig.getTimeZoneOffset ()));
        _writer.write (TDSStringUtils.format ("gTDS.appConfig.Local_Domains = \"{0}\"; \r\n", AppSettingsHelper.get ("Local_Domains", "proctor|testadmin|checkin|192.168.")));
        _writer.write (TDSStringUtils.format ("gTDS.appConfig.Language = \"{0}\"; \r\n", getLanguage ()));

        // from AIR.CLS lib
        // TODO Shiva we are ignoring CLS
        // SB-286
        _writer.write (TDSStringUtils.format ("gTDS.appConfig.IsCLSLogin = {0}; ", pAppConfig.getCLSLogin ()));
        _writer.write ("\n\r");

        if (_presenter.getThisUser () != null)
          _writer.write (TDSStringUtils.format ("gTDS.appConfig.UserFullName = \"{0}\"; \r\n", _presenter.getThisUser ().getFullname ()));

        _writer.write (TDSStringUtils.format ("gTDS.appConfig.contextPath = \"{0}\"; \r\n", _presenter.getHttpCurrentContext ().getRequest ().getContextPath ()));
      }
    }
  }

  public void writeGlobalAccs () throws IOException {

    AccsDTO accsDTO = _presenter.getGlobalAccs ();
    _writer.write ("gTDS.globalAccs = ");
    StringWriter sw = new StringWriter ();
    ObjectMapper mapper = new ObjectMapper (); // can reuse, share globally
    mapper.writeValue (sw, accsDTO);
    sw.close ();
    _writer.write (sw.toString ());
    _writer.write (";");
    _writer.write ("\r\n");
  }

  String _language = null;

  public String getLanguage () // get language from cookie
  {
    if (!StringUtils.isEmpty (_language))
      return _language;
    if (_presenter != null) {
      if (StringUtils.isEmpty (_presenter.getVariables ().getCurrentLanguage ())) {
        _presenter.getVariables ().setCurrentLanguage ("ENU");
      }
      _language = _presenter.getVariables ().getCurrentLanguage ();
      return _language;
    }

    _language = "ENU";
    return _language;
  }

  public void writeMessages (List<String> contexts) throws ReturnStatusException, IOException {
    MessageSystem messageSystem = _messageService.load (getLanguage (), contexts);

    MessageJson messageJson = new MessageJson (messageSystem);
    _writer.write ("gTDS.messages = ");
    _writer.write (messageJson.create ());
    _writer.write (";");
    _writer.write (TDSStringUtils.format ("gTDS.LanguagesLoaded = ['{0}'];\r\n", getLanguage ()));

    _writer.write ("\r\n");
  }

}
