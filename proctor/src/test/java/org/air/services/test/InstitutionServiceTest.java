/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package org.air.services.test;

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
import TDS.Proctor.Sql.Data.Abstractions.IInstitutionService;

@RunWith (LifecycleManagingTestRunner.class)
@ContextConfiguration ("classpath:test-context-staged-data.xml")
@ActiveProfiles("rts")
public class InstitutionServiceTest
{

  private static final Logger _logger = LoggerFactory.getLogger (InstitutionServiceTest.class);
  @Autowired
  @Qualifier ("iInstitutionService")
  IInstitutionService         institutionService;

  @Test
  @IfProfileValue(name="TestProfile", value="ToBeFixed")
  public void testGetUserInstitutions () throws Exception {
    List<String> userCurrentRoles = new ArrayList<String> ();
    userCurrentRoles.add ("proctor");
    InstitutionList institutionList = institutionService.getUserInstitutions (9795, userCurrentRoles);
    _logger.info ("SIZE.." + institutionList.size ());
    assertTrue (institutionList.size () > 0);
  }

  @Test
  @IfProfileValue(name="TestProfile", value="ToBeFixed")
  public void testGetDistricts () throws Exception {
    Districts DistrictsList = institutionService.getDistricts ();
    _logger.info ("District list..." + DistrictsList.size ());
    assertTrue (DistrictsList.size () > 0);
  }

  @Test
  @IfProfileValue(name="TestProfile", value="ToBeFixed")
  public void testGetInstitutions () throws Exception {
    InstitutionList institutions = institutionService.getInstitutions ();
    assertTrue (institutions.size () > 0);
  }

  @Test
  @IfProfileValue(name="TestProfile", value="ToBeFixed")
  public void testGetSchools () throws Exception {
    // Schools SchoolsList = institutionService.getSchools (370330); // valid
    // for Oregon
    Schools SchoolsList = institutionService.getSchools ("107249");
    _logger.info ("School size..." + SchoolsList.size ());
    assertTrue (SchoolsList.size () > 0);
  }

  @Test
  @IfProfileValue(name="TestProfile", value="ToBeFixed")
  public void testGetGrades () throws Exception {
    Grades GradesList = institutionService.getGrades ("130");
    _logger.info ("Grades size..." + GradesList.size ());
    assertTrue (GradesList.size () > 0);
  }

}
