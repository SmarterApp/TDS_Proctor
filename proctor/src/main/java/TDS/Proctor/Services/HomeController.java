/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package TDS.Proctor.Services;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Handles requests for the application home page.
 */
@Scope ("prototype")
@Controller
public class HomeController implements BeanFactoryAware
{
  private Logger      logger       = LoggerFactory.getLogger (HomeController.class);

  /**
   * Simply selects the home view to render by returning its name.
   */
  @RequestMapping (value = "TestController", method = RequestMethod.GET)
  public String home (Locale locale, Model model, HttpServletRequest request) {
    logger.info ("Welcome home! The client locale is {}.", locale);

    Date date = new Date ();
    DateFormat dateFormat = DateFormat.getDateTimeInstance (DateFormat.LONG, DateFormat.LONG, locale);

    String formattedDate = dateFormat.format (date);
    model.addAttribute ("serverTime", formattedDate);
    return "home";
  }

  @Override
  public void setBeanFactory (BeanFactory beanFactory) throws BeansException {
  
  }

}
