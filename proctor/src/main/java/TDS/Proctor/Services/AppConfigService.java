/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package TDS.Proctor.Services;

import TDS.Proctor.Sql.Data.AppConfig;
import TDS.Proctor.Sql.Data.ClientContextBrowserValidation;
import TDS.Proctor.Sql.Data.Abstractions.IAppConfigRepository;
import TDS.Proctor.Sql.Data.Abstractions.IAppConfigService;
import TDS.Shared.Browser.BrowserValidation;
import TDS.Shared.Configuration.TDSSettings;
import TDS.Shared.Data.ReturnStatus;
import TDS.Shared.Exceptions.ReturnStatusException;

public class AppConfigService implements IAppConfigService
{
  private final IAppConfigRepository _repository;
  private String                     _clientName;
  private ClientContextBrowserValidation _clientContextBrowserValidation;
  

  public AppConfigService (TDSSettings settings, IAppConfigRepository repository, ClientContextBrowserValidation clientContextBrowserValidation) {
    _repository = repository;
    _clientName = settings.getClientName ();
    _clientContextBrowserValidation = clientContextBrowserValidation;
  }

  @Override
  public AppConfig getConfigs () throws ReturnStatusException {
    AppConfig appConfig = null;
    try {
      appConfig = _repository.getConfigs (_clientName);
    } catch (ReturnStatusException e) { 
      throw e;
    }
    if (appConfig == null) {
      ReturnStatus rs = new ReturnStatus ("failed", "Failed reading application config");
      throw new ReturnStatusException (rs);
    }
    
    return appConfig;
  }
  
  /** Retrieve the BrowserValidation object from the PrintBrowserValidation singleton.  If it does not exist for this client,
    * add it to the PrintBrowserValidation singleton 
    */
  @Override
  public BrowserValidation getBrowserValidation(String environment, String context) throws ReturnStatusException
  {
      BrowserValidation browserValidation = _clientContextBrowserValidation.get(_clientName, context);
      if (browserValidation == null)
      {
          // get browservalidation object, add to list in singleton, set.
          browserValidation = _repository.getBrowserValidation(_clientName, environment, context);

          _clientContextBrowserValidation.add(_clientName, context, browserValidation);
          browserValidation = _clientContextBrowserValidation.get(_clientName, context);
      }

      return browserValidation;
  }
}
