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
public class OpportunityInfo
{
  public OpportunityStatusType _status;
  public UUID                  _browserKey;
  public UUID                  _oppKey;

  // / <summary>
  // / Is this opportunity open.
  // / </summary>
  public OpportunityStatusType getStatus () {
    return _status;
  }

  public void setStatus (OpportunityStatusType status) {
    _status = status;
  }

  public UUID getBrowserKey () {
    return _browserKey;
  }

  public void setBrowserKey (UUID browserKey) {
    _browserKey = browserKey;
  }

  public UUID getOppKey () {
    return _oppKey;
  }

  public void setOppKey (UUID oppKey) {
    _oppKey = oppKey;
  }

  public boolean isOpen () {
    return (_status == OpportunityStatusType.Pending || _status == OpportunityStatusType.Suspended || _status == OpportunityStatusType.Approved);

  }

  public OpportunityInfo () {
  }

  public OpportunityInstance createOpportunityInstance (UUID sessionKey) {
    return new OpportunityInstance (_oppKey, sessionKey, _browserKey);
  }
}
