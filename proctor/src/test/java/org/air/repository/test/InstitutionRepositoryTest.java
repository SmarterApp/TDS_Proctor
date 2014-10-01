/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package org.air.repository.test;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opentestsystem.shared.test.LifecycleManagingTestRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import TDS.Proctor.Sql.Data.Districts;
import TDS.Proctor.Sql.Data.Grades;
import TDS.Proctor.Sql.Data.InstitutionList;
import TDS.Proctor.Sql.Data.Schools;
import TDS.Proctor.Sql.Data.Abstractions.IInstitutionRepository;
import TDS.Proctor.Sql.Data.Abstractions.IProctorRepository;
import TDS.Proctor.Sql.Data.Abstractions.ITestRepository;

@RunWith (LifecycleManagingTestRunner.class)
@ContextConfiguration ("classpath:test-context-staged-data.xml")
@ActiveProfiles("rts")
public class InstitutionRepositoryTest
{
  @Autowired
  @Qualifier ("iInstitutionRepository")
  private IInstitutionRepository institutionRepository = null;

  @Autowired
  @Qualifier ("iProctorRepository")
  private IProctorRepository     proctorRepository     = null;

  @Autowired
  @Qualifier ("iTestRepository")
  private ITestRepository        testRepository        = null;

  private static final Logger    _logger               = LoggerFactory.getLogger (InstitutionRepositoryTest.class);

  @Test
  @IfProfileValue(name="TestProfile", value="ToBeFixed")
  public void testGetUserInstitutions () throws Exception {
    // Institution institution = new Institution (1, "TEST", "ID", "STATE");
    List<String> roles = new ArrayList<String> ();
    roles.add ("proctor");
    int sessionType = 0;
    InstitutionList tdsInstitutionList = institutionRepository.getUserInstitutions ("Oregon", 10000, sessionType, roles);
    _logger.info ("SIZE.." + tdsInstitutionList.size ());
    assertTrue (tdsInstitutionList.size () > 0);
  }

  @Test
  @IfProfileValue(name="TestProfile", value="ToBeFixed")
  public void testGetInstitutions () throws Exception {
    InstitutionList institutionList = institutionRepository.getInstitutions ("Oregon");
    assertTrue (institutionList.size () > 0);
  }

  @Test
  @IfProfileValue(name="TestProfile", value="ToBeFixed")
  public void testGetDistricts () throws Exception {
    Districts districtsList = institutionRepository.getDistricts ("Oregon");
    _logger.info ("District List Size.." + districtsList.size ());
    assertTrue (districtsList.size () > 0);
  }

  @Test
  @IfProfileValue(name="TestProfile", value="ToBeFixed")
  public void testGetSchools () throws Exception {
    Schools schoolsList = institutionRepository.getSchools ("Oregon", "370330");
    assertTrue (schoolsList.size () > 0);
  }

  @Test
  @IfProfileValue(name="TestProfile", value="ToBeFixed")
  public void testGetGrades () throws Exception {
    Grades GradesList = institutionRepository.getGrades ("Oregon_PT", "130");
    _logger.info ("Grade List Size.." + GradesList.size ());
    assertTrue (GradesList.size () > 0);
  }

  //
  // @Test(expected=Exception)
  // public void testGetGlobalAccs() {
  // TDSSqlResult<Accs> TDSSqlResult = testRepository.GetGlobalAccs
  // ("Oregon","StudentGlobal");
  // _logger.info("status....."+TDSSqlResult.ReturnStatus.getStatus ());
  // _logger.info("reason....."+TDSSqlResult.ReturnStatus.getReason ());
  // }

}
