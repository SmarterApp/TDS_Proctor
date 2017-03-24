/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package TDS.Proctor.Web.presentation.backing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.opentestsystem.delivery.logging.EventInfo;
import org.opentestsystem.delivery.logging.EventParser;
import org.opentestsystem.delivery.logging.ProctorEventLogger;
import org.opentestsystem.shared.security.domain.SbacPermission;
import org.opentestsystem.shared.security.domain.SbacRole;
import org.opentestsystem.shared.security.domain.SbacUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import AIR.Common.Utilities.SpringApplicationContext;
import AIR.Common.Web.CookieHelper;
import TDS.Proctor.Presentation.IPresenterBase;
import TDS.Proctor.Presentation.PresenterBase;
import TDS.Proctor.Presentation.SelectRolePresenter;
import TDS.Proctor.Sql.Data.ProctorUser;
import TDS.Proctor.Sql.Data.Abstractions.IProctorUserService;
import TDS.Proctor.Web.presentation.taglib.CSSLink;
import TDS.Proctor.Web.presentation.taglib.GlobalJavascript;
import TDS.Shared.Exceptions.ReturnStatusException;

import static org.opentestsystem.delivery.logging.EventLogger.Checkpoint.ENTER;
import static org.opentestsystem.delivery.logging.EventLogger.Checkpoint.EXIT;
import static org.opentestsystem.delivery.logging.ProctorEventLogger.ProctorEventData.BROWSER_ID;
import static org.opentestsystem.delivery.logging.ProctorEventLogger.ProctorEventData.PROCTOR_ID;
import static org.opentestsystem.delivery.logging.ProctorEventLogger.ProctorEventData.SESSION_ID;
import static org.opentestsystem.delivery.logging.ProctorEventLogger.ProctorLogEvent.LOGIN;

/**
 * @author mpatel
 *
 */
public class UserDetailsBacking extends BasePage implements IPresenterBase
{
  private static Logger _logger = LoggerFactory.getLogger (UserDetailsBacking.class);
  private boolean userWithMultipleRoles = false;
  private List<RoleSelection> sbacRoles = null;
  private String userFullName = null;
  private SelectRolePresenter _selectRolePresenter = null;
  // Start page controls
  private GlobalJavascript _globalJs        = null;
  private CSSLink          _clientCSSLink   = null;
  private String _clientName = null;
  private String selectedRole = null;
  private SbacUser sbacUser = null;
  private ProctorUser proctorUser = null;
  
  public UserDetailsBacking () throws Exception{
    super ();
    _selectRolePresenter = new SelectRolePresenter (this);
    init();
  }

  public void setClientCSSLink (CSSLink link) {
    this._clientCSSLink = link;
    this._clientCSSLink.setPresenter (_selectRolePresenter);
  }

  public CSSLink getClientCSSLink () {
    return this._clientCSSLink;
  }

  public void setGlobalJs (GlobalJavascript gjs) {
    this._globalJs = gjs;
    this._globalJs.setPresenter (_selectRolePresenter);
  }

  public GlobalJavascript getGlobalJs () {
    return this._globalJs;
  }
  
  
  public String getSelectedRole () {
    return selectedRole;
  }

  public void setSelectedRole (String selectedRole) {
    this.selectedRole = selectedRole;
  }

  public boolean isUserWithMultipleRoles () {
    return userWithMultipleRoles;
  }
  
  
  public void setUserWithMultipleRoles (boolean userWithMultipleRoles) {
    this.userWithMultipleRoles = userWithMultipleRoles;
  }

  
  public List<RoleSelection> getSbacRoles () {
    return sbacRoles;
  }

  public void setSbacRoles (List<RoleSelection> sbacRoles) {
    this.sbacRoles = sbacRoles;
  }

  public String getUserFullName () {
    return userFullName;
  }

  public void setUserFullName (String userFullName) {
    this.userFullName = userFullName;
  }
  
  

  public String getClientName () {
    return _clientName;
  }

  public void setClientName (String clientName) {
    this._clientName = clientName;
  }

  public void init() throws Exception {
    ProctorEventLogger _eventLogger = new ProctorEventLogger();
    EventInfo eventInfo = EventInfo.create(LOGIN.name(), ENTER.name(), EventParser.getEventDataFields(getCurrentContext().getRequest()));
    _eventLogger.info(eventInfo);

    try {
      sbacUser = (SbacUser) SecurityContextHolder.getContext ().getAuthentication ().getPrincipal ();
      IProctorUserService _proctorUserService = SpringApplicationContext.getBean ("iProctorUserService", IProctorUserService.class);
      try {
        if(!_proctorUserService.userAlreadyExists (sbacUser.getUniqueId (), sbacUser.getEmail ())) {
          _proctorUserService.createUser (sbacUser.getUniqueId (),sbacUser.getEmail (), sbacUser.getFullName ());
        }
      } catch (ReturnStatusException e) {
        _eventLogger.error(eventInfo, e);
        _logger.error (e.getMessage ()!=null?e.getMessage ():e.toString (),e);
        throw new UsernameNotFoundException(e.toString ());
      }
      
      proctorUser = validateLogin();
      
      this.userFullName = sbacUser.getFullName ();
      if(sbacUser.getRoles ().size ()>1) {
        userWithMultipleRoles = true;
      } 
      sbacRoles = new ArrayList<RoleSelection> ();
      for(SbacRole role:sbacUser.getRoles ()) {
        StringBuilder key = new StringBuilder (role.getRoleName ()).append ("~");
        key.append (role.getEffectiveEntity ().getEntityId ()).append ("~");
        key.append (role.getRoleEntityLevel ().getTypeName ());
        StringBuilder value = new StringBuilder (role.getRoleName ());
        value.append ("[").append (role.getEffectiveEntity ().getEntityName ()).append ("]");
        RoleSelection roleSelection = new RoleSelection(key.toString (),value.toString ());
        sbacRoles.add (roleSelection);
      }
      
      if(!userWithMultipleRoles) {
        if(sbacUser.getRoles ().size ()==1) 
        {
          SbacRole role = sbacUser.getRoles ().iterator ().next ();
          _selectRolePresenter.createAndUpdateProctorIsCurrent (role,proctorUser.getKey (),getClientName (),role.getEffectiveEntity ().getEntityId (),role.getRoleEntityLevel ().name ());
        }
      }

      _eventLogger.putField(PROCTOR_ID.name(), proctorUser.getId());
      _eventLogger.putField(SESSION_ID.name(), proctorUser.getSessionKey());
      _eventLogger.putField(BROWSER_ID.name(), proctorUser.getBrowserKey());

      _eventLogger.info(eventInfo.withCheckpoint(EXIT.name()));
    } catch (Exception e) {
      _eventLogger.error(eventInfo, e);
      _logger.error (e.getMessage ()==null?e.toString ():e.getMessage (),e);
      throw e;
    }
  }
  
  
  
  
  private ProctorUser validateLogin() throws Exception{
      populateClientName ();
      UUID browserKey = UUID.randomUUID ();
      ProctorUser proctorUser = _selectRolePresenter.validateLogin (_clientName, browserKey, sbacUser.getUniqueId (), sbacUser.getPassword (), true,sbacUser);
      return proctorUser;
  }
  
  
  
  private void populateClientName() {
    _clientName = getLoginClientName ();
    getTdsSettings ().setClientName (_clientName);// save to the RESPONSE cookie
    setRequestClientName (_clientName);// save to the REQUEST cookie as well.
  }
  
  private void setRequestClientName (String clientName) {
    String cookieName = getTdsSettings ().getCookieName ("Client");
    if (clientName != null) {
      CookieHelper.setValue (cookieName, clientName);
    }
  }
  
  
  public String next() throws Exception{
    try {
    String[] selectedRoleStr= selectedRole.split ("~");
    String entityId = selectedRoleStr[1];
    String entityLevel = selectedRoleStr[2];
    
    Collection<SbacRole> roles = sbacUser.getRoles ();
    Iterator<SbacRole> rolesIter = roles.iterator ();
    SbacRole selectedRole = null;
    while(rolesIter.hasNext ()) {
      SbacRole role = rolesIter.next ();
      if(role.getRoleName ().equalsIgnoreCase (selectedRoleStr[0]) && role.getEffectiveEntity ().getEntityId ().equalsIgnoreCase (entityId)) {
        selectedRole = role;
        break;
      }
    }
    
    if(selectedRole==null) {
      throw new IllegalArgumentException ("Error Selecting Role...");
    }
    
    _selectRolePresenter.createAndUpdateProctorIsCurrent (selectedRole,proctorUser.getKey (),getClientName (),entityId,entityLevel);
    return "default";
    } catch (Exception e) {
      _logger.error (e.getMessage ()==null?e.toString ():e.getMessage (),e);
      throw e;
    }
  }
  
  

  /* (non-Javadoc)
   * @see TDS.Proctor.Presentation.IPresenterBase#displayMessage(java.lang.String)
   */
  @Override
  public void displayMessage (String msg) {
    
  }

  /* (non-Javadoc)
   * @see TDS.Proctor.Presentation.IPresenterBase#setPresenterBase(TDS.Proctor.Presentation.PresenterBase)
   */
  @Override
  public void setPresenterBase (PresenterBase presenter) {
    
  }
  
  public static class RoleSelection {
    private String key;
    private String label;
    public String getKey () {
      return key;
    }
    public String getLabel () {
      return label;
    }
    public void setKey (String key) {
      this.key = key;
    }
    public void setLabel (String label) {
      this.label = label;
    }
    /**
     * @param key
     * @param label
     * @param role
     */
    public RoleSelection (String key, String label) {
      super ();
      this.key = key;
      this.label = label;
    }
    
  }
  
  
}
