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

import TDS.Proctor.Sql.Data.Districts;
import TDS.Proctor.Sql.Data.Grades;
import TDS.Proctor.Sql.Data.InstitutionList;
import TDS.Proctor.Sql.Data.Schools;
import TDS.Proctor.Sql.Data.Abstractions.IInstitutionRepository;
import TDS.Proctor.Sql.Data.Abstractions.IInstitutionService;
import TDS.Shared.Configuration.TDSSettings;
import TDS.Shared.Exceptions.ReturnStatusException;

public class InstitutionService implements IInstitutionService
{
  private final IInstitutionRepository _instRepository;
  String                               _clientName;
  int                                  _sessionType = 0;

  public InstitutionService (TDSSettings settings, IInstitutionRepository instRepository) {
    _instRepository = instRepository;
    _clientName = settings.getClientName ();
    _sessionType = settings.getSessionType ();
  }

  public InstitutionList getUserInstitutions (long userKey, List<String> userCurrentRoles) throws ReturnStatusException {
    InstitutionList result = null;
    try {
      result = _instRepository.getUserInstitutions (_clientName, userKey, _sessionType, userCurrentRoles);
    } catch (ReturnStatusException re) {
      throw re;
    }
    return result;
  }

  public InstitutionList getInstitutions () throws ReturnStatusException {
    InstitutionList institutionList = null;
    try {
      institutionList = _instRepository.getInstitutions (_clientName);
    } catch (ReturnStatusException re) {
      throw re;
    }
    return institutionList;
  }

  public Districts getDistricts () throws ReturnStatusException {
    Districts districts = null;
    try {
      districts = _instRepository.getDistricts (_clientName);
    } catch (ReturnStatusException re) {
      throw re;
    }
    return districts;
  }

  public Schools getSchools (String districtKey) throws ReturnStatusException {
    Schools schools = null;
    try {
      schools = _instRepository.getSchools (_clientName, districtKey);
    } catch (ReturnStatusException re) {
      throw re;
    }
    return schools;
  }

  public Grades getGrades (String schoolKey) throws ReturnStatusException {
    Grades grades = null;
    try {
      grades = _instRepository.getGrades (_clientName, schoolKey);
    } catch (ReturnStatusException re) {
      throw re;
    }
    return grades;
  }
}
