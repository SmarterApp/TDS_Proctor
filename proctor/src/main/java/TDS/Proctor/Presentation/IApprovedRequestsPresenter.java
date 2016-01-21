/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package TDS.Proctor.Presentation;

import TDS.Proctor.Sql.Data.TestOpps;
import TDS.Proctor.Sql.Data.TesteeRequests;

/**
 * IApprovedRequestsPresenter abstract class
 * 
 * 
 */
public interface IApprovedRequestsPresenter extends IPresenterBase
{
  public void setRequests (TesteeRequests requests);

  public TesteeRequests getTesteeRequests ();

  public void setOpps (TestOpps opps);

  public TestOpps getOpps ();

  public String getStrSessionKey ();

  public void setStrSessionKey (String proctorSessionKey);

}
