/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package TDS.Proctor.Services;

import java.util.List;
import java.util.Map;

import TDS.Proctor.Sql.Data.Segments;
import TDS.Proctor.Sql.Data.Test;
import TDS.Proctor.Sql.Data.Abstractions.ITestRepository;
import TDS.Proctor.Sql.Data.Abstractions.ITestService;
import TDS.Proctor.Sql.Data.Accommodations.AccTypes;
import TDS.Proctor.Sql.Data.Accommodations.Accs;
import TDS.Proctor.Sql.Data.Accommodations.AccsDTO;
import TDS.Shared.Configuration.TDSSettings;
import TDS.Shared.Exceptions.ReturnStatusException;

public class TestService implements ITestService
{
  private final ITestRepository _repository;
  private String                _clientName;
  private int                   _sessionType;

  public TestService (TDSSettings settings, ITestRepository repository) {
    _repository = repository;
    _clientName = settings.getClientName ();
    _sessionType = settings.getSessionType ();
  }

  public List<Test> getSelectableTests (Long proctorId) throws ReturnStatusException {
    List<Test> result = null;
    try {
      result = _repository.getSelectableTests (_clientName, _sessionType, proctorId);
    } catch (ReturnStatusException re) {
      throw re;
    }
    return result;
  }

  public Segments getSegments () throws ReturnStatusException {
    Segments result = null;
    try {
      result = _repository.getSegments (_clientName, _sessionType);
    } catch (ReturnStatusException re) {
      throw re;
    }
    return result;
  }

  public AccsDTO getTestAccs (String testkey) throws ReturnStatusException {
    Accs result = null;

    try {
      result = _repository.getTestAccs (testkey);
    } catch (ReturnStatusException re) {
      throw re;
    }

    Accs accs = result;
    AccsDTO accsDTO = new AccsDTO ();

    for (Map.Entry<String, AccTypes> pair : accs.getData ().entrySet ()) {
      accsDTO.add (pair.getKey (), pair.getValue ());
    }
    return accsDTO;
  }

  public AccsDTO getGlobalAccs () throws ReturnStatusException {
    Accs result = null;

    try {
      result = _repository.getGlobalAccs (_clientName, "TAGlobal");
    } catch (ReturnStatusException re) {
      throw re;
    }
    Accs accs = result;
    AccsDTO accsDTO = new AccsDTO ();

    for (Map.Entry<String, AccTypes> pair : accs.getData ().entrySet ()) {
      accsDTO.add (pair.getKey (), pair.getValue ());
    }
    return accsDTO;
  }

}
