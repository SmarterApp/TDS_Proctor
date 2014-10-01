/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package TDS.Proctor.Sql.Data;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Testee
{
  private List<TesteeAttribute> _testeeAttributes = new ArrayList<TesteeAttribute> ();

  @JsonProperty("TesteeAttributes")
  public List<TesteeAttribute> getTesteeAttributes () {
    return _testeeAttributes;
  }

  public Testee () {

  }
}
