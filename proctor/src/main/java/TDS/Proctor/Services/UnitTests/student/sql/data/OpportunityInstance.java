/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package TDS.Proctor.Services.UnitTests.student.sql.data;

import java.util.UUID;

/**
 * @author temp_rreddy
 * 
 */
public class OpportunityInstance
{
  private final UUID _oppKey;
  private final UUID _sessionKey;
  private final UUID _browserKey;

  // / <summary>
  // / Opportunity Key
  // / </summary>
  public UUID getKey () {
    return _oppKey;
  }

  // / <summary>
  // / Session Key
  // / </summary>
  public UUID getSessionKey () {
    return _sessionKey;
  }

  // / <summary>
  // / Browser Key
  // / </summary>
  public UUID getBrowserKey () {
    return _browserKey;
  }

  public OpportunityInstance (UUID oppKey, UUID sessionKey, UUID browserKey) {
    _oppKey = oppKey;
    _sessionKey = sessionKey;
    _browserKey = browserKey;
  }
}
