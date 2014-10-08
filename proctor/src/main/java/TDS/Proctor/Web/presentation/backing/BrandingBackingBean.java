/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package TDS.Proctor.Web.presentation.backing;

import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.opentestsystem.shared.progman.client.domain.Tenant;
import org.opentestsystem.shared.security.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import AIR.Common.Utilities.SpringApplicationContext;

/**
 * @author mpatel
 *
 */
public class BrandingBackingBean
{
  
  private static final Logger _logger = LoggerFactory.getLogger (BrandingBackingBean.class);
  private UserService _userService;
  private String logoImageURL;
  private String logoGIFURL;
  private String logoImageTitle;
  
  public String getLogoImageTitle () {
    if(logoImageTitle==null) {
      populateLogoImageDetails();
    }
    return logoImageTitle;
  }

  public String getLogoImageURL () {
    if(logoImageURL==null) {
      populateLogoImageDetails();
    }
    return logoImageURL;
  }
  
  

  public String getLogoGIFURL () {
    if(logoGIFURL==null) {
      populateLogoImageDetails();
    }
    return logoGIFURL;
  }

  @PostConstruct
  public void init() {
    _userService = SpringApplicationContext.getBean (UserService.class);
  }
  
  public Map<String, Object> getAssets(String tenantId) {
    return _userService.getAssetsForTenant(tenantId);
  }
  
  
  @SuppressWarnings ("unchecked")
  public void populateLogoImageDetails() {
    HttpServletRequest request = (HttpServletRequest)FacesContext.getCurrentInstance ().getExternalContext ().getRequest ();
    //Path for the default SBAC Image
    logoImageURL = request.getContextPath () + "/shared/themes/pillars/images/logo_sbac.png";
    logoGIFURL = request.getContextPath () + "/shared/themes/pillars/images/logo_sbac.gif";
    logoImageTitle = "Smarter Balanced Assessment Consortium";
    try {
      String tenantId = "";
      List<Tenant> tenantsList = _userService.getUniqueTenantsForUser ().getTenants ();
      //If Only one tenant associated than get the image from progman and display it else display the default sbac image.
      if(tenantsList!=null && tenantsList.size ()==1){
         Tenant tenant  = tenantsList.get (0);
         tenantId = tenant.getId ();
 
         Map<String, Object> assets = _userService.getAssetsForTenant(tenantId);
         if(assets!=null && !assets.isEmpty ()) {
            List<Map<String,String>> assetsList = (List<Map<String,String>>)assets.get("assets");
            for(Map<String,String> asset:assetsList) {
              if(asset.get ("name").equals ("logo")) {
                logoImageURL = asset.get ("url");
              } else if(asset.get ("name").equalsIgnoreCase ("title" )) {
                logoImageTitle = asset.get ("url");
              } else if(asset.get ("name").equalsIgnoreCase ("logo-gif" )) {
                logoGIFURL = asset.get ("logo-gif");
              }
            }
          }
      }
    } catch (Exception e) {
      _logger.error (e.toString (),e);
    }
  }
}
