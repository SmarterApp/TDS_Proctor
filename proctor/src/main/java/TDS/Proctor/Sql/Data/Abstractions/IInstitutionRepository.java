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

import TDS.Proctor.Sql.Data.Districts;
import TDS.Proctor.Sql.Data.Grades;
import TDS.Proctor.Sql.Data.InstitutionList;
import TDS.Proctor.Sql.Data.Schools;
import TDS.Shared.Exceptions.ReturnStatusException;

public interface IInstitutionRepository
{

  InstitutionList getUserInstitutions (String clientname, long userKey, int sessionType, List<String> roles) throws ReturnStatusException;

  InstitutionList getInstitutions (String clientname) throws ReturnStatusException;

  Districts getDistricts (String clientname) throws ReturnStatusException;

  Schools getSchools (String clientname, String districtKey) throws ReturnStatusException;

  Grades getGrades (String clientname, String schoolKey) throws ReturnStatusException;

}
