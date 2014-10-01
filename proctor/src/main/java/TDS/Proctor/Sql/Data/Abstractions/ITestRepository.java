/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package TDS.Proctor.Sql.Data.Abstractions;

import java.util.List;

import TDS.Proctor.Sql.Data.Segments;
import TDS.Proctor.Sql.Data.Test;
import TDS.Proctor.Sql.Data.Accommodations.Accs;
import TDS.Shared.Exceptions.ReturnStatusException;

public interface ITestRepository
{

  List<Test> getSelectableTests (String clientname, int sessionType, Long proctorId) throws ReturnStatusException;

  Segments getSegments (String clientname, int sessionType) throws ReturnStatusException;

  Accs getTestAccs (String testkey) throws ReturnStatusException;

  Accs getGlobalAccs (String clientname, String context) throws ReturnStatusException;

}
