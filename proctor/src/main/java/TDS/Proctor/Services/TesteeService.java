/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package TDS.Proctor.Services;

import TDS.Proctor.Sql.Data.Testee;
import TDS.Proctor.Sql.Data.Testees;
import TDS.Proctor.Sql.Data.Abstractions.ITesteeRepository;
import TDS.Proctor.Sql.Data.Abstractions.ITesteeService;
import TDS.Shared.Configuration.TDSSettings;
import TDS.Shared.Exceptions.ReturnStatusException;

public class TesteeService implements ITesteeService
{
  private final ITesteeRepository _repository;
  String                          _clientName;

  public TesteeService (TDSSettings settings, ITesteeRepository repository) {
    _repository = repository;
    _clientName = settings.getClientName ();
  }

  public Testee getTestee (String testeeID, long proctorKey) throws ReturnStatusException {
    Testee testee = null;
    try {
      testee = _repository.getTestee (_clientName, testeeID, proctorKey);
    } catch (ReturnStatusException e) {
      throw e;
    }
   return testee;
  }

  public Testees getSchoolTestees (String schoolKey, String grade, String firstName, String lastName) throws ReturnStatusException {
    Testees testees = null;
    try {
      testees = _repository.getSchoolTestees (_clientName, schoolKey, grade, firstName, lastName);
    } catch (ReturnStatusException e) {
      throw e;
    }
    return testees;
  }
}
