/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package TDS.Proctor.Web.presentation.backing;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.collections.CollectionUtils;

import TDS.Proctor.Presentation.IApprovedRequestsPresenter;
import TDS.Proctor.Sql.Data.TestOpps;
import TDS.Proctor.Sql.Data.TesteeRequest;
import TDS.Proctor.Sql.Data.TesteeRequests;

abstract class AbstractApprovedRequestsPresenter extends BasePage implements IApprovedRequestsPresenter
{
  protected TesteeRequests _testeeRequests = null;
  protected TestOpps       _testOpps       = null;
  protected String         _strSessionKey  = null;

  public void setRequests (TesteeRequests requests) {
    this._testeeRequests = requests;
  }

  public TesteeRequests getTesteeRequests () {
    return _testeeRequests;
  }

  public void setOpps (TestOpps opps) {
    this._testOpps = opps;
  }

  public TestOpps getOpps () {
    return _testOpps;
  }

  public String getStrSessionKey () {
    return _strSessionKey;
  }

  public void setStrSessionKey (String proctorSessionKey) {
    this._strSessionKey = proctorSessionKey;
  }

  // TODO Shiva
  /*
   * This was because of a problem with inheritance: see document
   * "docs/known issues/inheritance vs. encapsulation.mht"
   */
  public int getSizeTesteeRequests () {
    if (_testeeRequests == null)
      return 0;
    return _testeeRequests.size ();
  }

  public int getSizeTestOpps () {
    if (_testOpps == null)
      return 0;
    return _testOpps.size ();
  }

  /**
   * @return List<TesteeRequest>
   */
  public List<TesteeRequest> findAll (UUID oppKey) {
    if (getTesteeRequests () == null)
      return null;

    return new ArrayList<TesteeRequest> (CollectionUtils.select (getTesteeRequests (), new OppKeyPredicate (oppKey)));
  }
}
