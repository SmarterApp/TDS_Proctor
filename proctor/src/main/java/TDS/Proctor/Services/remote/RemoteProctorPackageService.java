/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package TDS.Proctor.Services.remote;

import javax.annotation.PostConstruct;

import org.opentestsystem.shared.trapi.ITrClient;
import org.opentestsystem.shared.trapi.exception.TrApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import AIR.Common.Utilities.UrlEncoderDecoderUtils;
import AIR.Common.Web.HttpWebHelper;
import AIR.Common.time.DateTime;
import TDS.Proctor.Sql.Data.Abstractions.IProctorPackageService;

public class RemoteProctorPackageService implements IProctorPackageService
{

  private static final Logger _logger = LoggerFactory.getLogger (RemoteProctorPackageService.class);

  @Autowired
  private HttpWebHelper       _httpWebHelper;
  
  @Autowired
  private ITrClient       _trClient;
  
  @Value("${StateCode}")
  private String _stateCode;
  
  @PostConstruct
  private void init() {
    _stateCode = _stateCode.toUpperCase ();
  }
  
  @Override
  public String getProctorPackageString (String entityLevel, String entityId) {
    entityLevel = UrlEncoderDecoderUtils.encode (entityLevel);
    entityId = UrlEncoderDecoderUtils.encode (entityId);
    String urlPath = null;
    try {
      urlPath = "proctorassessments?stateAbbreviation=" + _stateCode + "&entityLevel=" + entityLevel + "&entityName=" + entityId + "&date=" + DateTime.getTodaysDate("yyyy-MM-dd");
      return _trClient.getPackage (urlPath);
    } catch (Exception e) {
      _logger.error(e.getMessage(), e);
      return null;
    } 
    
  }

 
}
