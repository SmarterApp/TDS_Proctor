/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package TDS.Proctor.Services.remote;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opentestsystem.delivery.tds.loadtest.api.Assessment;
import org.opentestsystem.delivery.tds.loadtest.api.TDSUser;
import org.opentestsystem.delivery.tds.loadtest.api.TDSUserCredentialSet;
import org.opentestsystem.shared.test.LifecycleManagingTestRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import tds.dll.common.rtspackage.proctor.data.Proctor;
import tds.dll.common.rtspackage.proctor.data.ProctorPackage;
import TDS.Proctor.Sql.Data.Abstractions.IProctorPackageService;

@RunWith (LifecycleManagingTestRunner.class)
@ContextConfiguration ("classpath:remote-proctor-package-service-test-context.xml")
public class RemoteProctorPackageServiceTest
{

  @Autowired
  private TDSUser                alice;

  @Autowired
  private IProctorPackageService proctorPackageService;

  @Autowired
  private Assessment             assessment;

  @Test
  public void roundTripTest () {
    TDSUserCredentialSet credentials = (TDSUserCredentialSet) alice.getCredentialSet (TDSUserCredentialSet.KEY);
    String pkg = proctorPackageService.getProctorPackageString ("STATE", credentials.getStateCode ());
    //TODO:
   // Proctor proctor = pkg.getProctor ();
   // assertEquals ("Wrong number of tests", 1, proctor.getTests ().getTest ().size () );
  }
  
  @Test
  public void noProctorTest() {
    String pkg = proctorPackageService.getProctorPackageString( "notauser", "nostate" );
    //TODO:
    //assertNull ( "Proctor object should be null", pkg );
  }

}
