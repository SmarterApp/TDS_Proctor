/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package TDS.Proctor.Sql.Data.Abstractions;

import TDS.Proctor.Sql.Data.Testee;
import TDS.Proctor.Sql.Data.Testees;
import TDS.Shared.Exceptions.ReturnStatusException;

public interface ITesteeRepository
{
  Testee getTestee (String clientname, String testeeID) throws ReturnStatusException;

  Testees getSchoolTestees (String clientname, String schoolKey, String grade, String firstName, String lastName) throws ReturnStatusException;
}
