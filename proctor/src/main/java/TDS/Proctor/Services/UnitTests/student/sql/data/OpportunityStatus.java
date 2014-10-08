/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package TDS.Proctor.Services.UnitTests.student.sql.data;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author temp_rreddy
 * 
 */
public class OpportunityStatus
{

  public OpportunityStatusType _status;
  public Date                  _dateStarted;
  public String                _comment;

  @JsonProperty ("status")
  public OpportunityStatusType getStatus () {
    return _status;
  }

  public void setStatus (OpportunityStatusType status) {
    _status = status;
  }

  @JsonProperty ("dateStarted")
  public Date getDateStarted () {
    return _dateStarted;
  }

  public void setDateStarted (Date dateStarted) {
    _dateStarted = dateStarted;
  }

  @JsonProperty ("comment")
  public String getComment () {
    return _comment;
  }

  public void setComment (String comment) {
    _comment = comment;
  }

}
