/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package TDS.Proctor.Presentation;

import java.util.UUID;

public interface IPrintRequestPresenterView extends IPresenterBase
{
  UUID   _requestKey = null;
  // ProctorUser ThisUser { get; set; }
  String _lblName    = null;

  // String lblSSID { set; }
  String _pageTitle  = null;

  void setRequestKey (UUID value);

  UUID getRequestKey ();

  void setLblName (String value);

  void setPageTitle (String value);
  // String lblDate { set; }
  // TDS.ItemRenderer.WebControls.PageLayout PageLayout { get; }
}
