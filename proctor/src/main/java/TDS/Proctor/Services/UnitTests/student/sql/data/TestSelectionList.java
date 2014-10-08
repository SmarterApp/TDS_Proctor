/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
/**
 * 
 */
package TDS.Proctor.Services.UnitTests.student.sql.data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author temp_rreddy
 * 
 */
// TODO Do we need this class?
public class TestSelectionList extends ArrayList<TestSelection>
{
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public TestSelectionList () {
    super ();
  }

  public List<TestSelection> getListOfGetEligibleTestsData () {
    return this;
  }

  public void setListOfGetEligibleTestsData (List<TestSelection> listOfGetEligibleTestsData) {
    this.addAll (listOfGetEligibleTestsData);
  }

}
