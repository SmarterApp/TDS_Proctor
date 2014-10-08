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

import org.opentestsystem.shared.security.domain.SbacUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tds.dll.api.TestType;
import AIR.Common.Web.FacesContextHelper;
import TDS.Proctor.Services.ProctorUserService;
import TDS.Proctor.Sql.Data.ProctorUser;
import TDS.Proctor.Sql.Data.Abstractions.IProctorUserService;
import TDS.Shared.Exceptions.ReturnStatusException;

/**
 * @author mpatel
 *
 */
public class SelectRolePresenter  extends PresenterBase
  {
    
  private static Logger _logger = LoggerFactory.getLogger (SelectRolePresenter.class);
  private IProctorUserService _proctorUserService = FacesContextHelper.getBean ("iProctorUserService", IProctorUserService.class);
  
    public SelectRolePresenter (IPresenterBase view) {
      super (view);
      // init
      initView ();
    }

    public void initView () {

    }
    
    public ProctorUser validateLogin (String clientName, UUID browserKey, String userName, String password, boolean ignorePW, SbacUser sbacUser) throws Exception{
      try {
        
        ProctorUser user = _proctorUserService.validate (browserKey, userName, password, true);
        user.setFullname (sbacUser.getFullName ());
        ProctorUserService.save (user, getUserInfoCookie ()); // save info to a
                                                              // cookie
        this.saveVariablesCookie (); // save some config to the cookie for use
                                     // later
        return user;
      } catch (ReturnStatusException rex) {
        _logger.error (rex.getMessage ()==null?rex.toString ():rex.getMessage (),rex);
        throw rex;
      } catch (Exception ex) {
        _logger.error (ex.getMessage ()==null?ex.toString ():ex.getMessage (),ex);
        throw ex;
      }
    }
    
    public  void createAndUpdateProctorIsCurrent(String roleName, long userKey,String clientName,String entityId,String entityLevel) throws Exception{
      
      TestType testType = null;
       if(roleName.equalsIgnoreCase ("Test Administrator")) {
         testType = TestType.SUMMATIVE;
       } else if(roleName.equalsIgnoreCase ("Interim Test Administrator")){
         testType = TestType.NON_SUMMATIVE;
       }
      _proctorUserService.createAndUpdateProctorIsCurrent (entityLevel, entityId, clientName, userKey,testType);
    }

}
