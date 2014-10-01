/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package TDS.Proctor.Sql.RepositorySP;

import java.sql.CallableStatement;
import java.sql.SQLException;
import java.util.Date;

import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import AIR.Common.DB.AbstractDAO;
import AIR.Common.DB.SQLConnection;
import AIR.Common.time.DateTime;
import TDS.Proctor.Sql.Data.Testee;
import TDS.Proctor.Sql.Data.TesteeAttribute;
import TDS.Proctor.Sql.Data.Testees;
import TDS.Proctor.Sql.Data.Abstractions.ITesteeRepository;
import TDS.Shared.Data.ColumnResultSet;
import TDS.Shared.Data.ReturnStatus;
import TDS.Shared.Exceptions.ReturnStatusException;

public class TesteeRepository extends AbstractDAO implements ITesteeRepository
{

private static final Logger _logger = LoggerFactory.getLogger(TesteeRepository.class);

  public Testee getTestee (String clientname, String testeeID) throws ReturnStatusException {
    final String sp = "{call P_GetRTSTestee(?, ?) }";
    ColumnResultSet reader = null;

    Testee testee = new Testee ();
    try (SQLConnection connection = getSQLConnection ()) {
      try (CallableStatement callstatement = connection.prepareCall (sp)) {
        callstatement.setString (1, clientname);
        callstatement.setString (2, testeeID);

        boolean exeStoredProc = callstatement.execute ();
        if (exeStoredProc) {
        	reader = ColumnResultSet.getColumnResultSet (callstatement.getResultSet ());
        	ReturnStatusException.getInstanceIfAvailable (reader, "Failed getting testee");
        	reader.setFixNulls (true);
        }
        
        loadTesteeAttributes(reader, testee);
        
        boolean rtsKeyExists = false;
        for (TesteeAttribute testeeAttribute : testee.getTesteeAttributes()) {
      	  if ("--rts key--".equalsIgnoreCase(testeeAttribute.getTdsId())) {
      		  rtsKeyExists = true;
      	  	  break;
      	  }
        }
        
        if (testee.getTesteeAttributes().size() <= 0 || !rtsKeyExists) {
      	  throw new ReturnStatusException (new ReturnStatus("failed", "No match for student ID"));
        }
      }
    } catch (SQLException e) {

      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    return testee;
  }

  // advance search
  // Get all students for a school If grade level is null, get all grade
  // levels
  public Testees getSchoolTestees (String clientname, String schoolKey, String grade, String firstName, String lastName) throws ReturnStatusException {
    final String sp = "{call GetSchoolStudents(?, ?, ?, ?, ?) }";

    Testees testees = new Testees ();
    ColumnResultSet reader = null;
    try (SQLConnection connection = getSQLConnection ()) {
      try (CallableStatement callstatement = connection.prepareCall (sp)) {
        callstatement.setString (1, clientname);
        callstatement.setString (2, schoolKey);
        // TODO: verify if DBHelper needs to pass in null.
        if (StringUtils.isEmpty (grade))
          callstatement.setNull (3, java.sql.Types.VARCHAR);
        else
          callstatement.setString (3, grade);
        callstatement.setString (4, firstName);
        callstatement.setString (5, lastName);

        if (callstatement.execute ()) {
          reader = ColumnResultSet.getColumnResultSet (callstatement.getResultSet ());

          ReturnStatusException.getInstanceIfAvailable (reader, "Failed getting school testees");

          loadSchoolTestee (reader, testees);
        }
      }
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    return testees;
  }

  private boolean loadSchoolTestee (ColumnResultSet reader, Testees testees) throws SQLException {

	    if (reader == null)
	      return true;// nothing to load

	    while (reader.next ()) {
	      // load stuff here ...
	      Testee testee = new Testee ();
	      TesteeAttribute taRtsKey, taSSID, taLastName, taFirstName, taGrade;
	      Long rtsKey = reader.getLong("rtsKey");
	      if (rtsKey > 0) {
	          taRtsKey = new TesteeAttribute();
	          taRtsKey.setTdsId("--RTS KEY--");
	          taRtsKey.setType("ENTITYKEY");
	          taRtsKey.setValue(rtsKey.toString());
	          taRtsKey.setShowOnProctor(true);
	          testee.getTesteeAttributes().add(taRtsKey);
	      }
	      String SSID = reader.getString("SSID");
	      if (!StringUtils.isEmpty(SSID))
	      {
	          taSSID = new TesteeAttribute();
	          taSSID.setTdsId("ID");
	          taSSID.setType("attribute");
	          taSSID.setRtsName("ExternalID");
	          taSSID.setLabel("SSID");
	          taSSID.setValue(SSID);
	          taSSID.setShowOnProctor(true);
	          testee.getTesteeAttributes().add(taSSID);
	      }
	      String lastName = reader.getString("LastName");
	      if (!StringUtils.isEmpty(lastName))
	      {
	          taLastName = new TesteeAttribute();
	          taLastName.setTdsId("LastName");
	          taLastName.setType("attribute");
	          taLastName.setRtsName("LglLNm");
	          taLastName.setLabel("Last Name");
	          taLastName.setValue(lastName);
	          taLastName.setShowOnProctor(true);
	          testee.getTesteeAttributes().add(taLastName);
	      }
	      String firstName = reader.getString("FirstName");
	      if (!StringUtils.isEmpty(firstName))
	      {
	          taFirstName = new TesteeAttribute();
	          taFirstName.setTdsId("FirstName");
	          taFirstName.setType("attribute");
	          taFirstName.setRtsName("LglFNm");
	          taFirstName.setLabel("First Name");
	          taFirstName.setValue(firstName);
	          taFirstName.setShowOnProctor(true);
	          testee.getTesteeAttributes().add(taFirstName);
	      }
	      String grade = reader.getString("Grade");
	      if (!StringUtils.isEmpty(grade))
	      {
	          taGrade = new TesteeAttribute();
	          taGrade.setTdsId("Grade");
	          taGrade.setType("attribute");
	          taGrade.setRtsName("EnrlGrdCd");
	          taGrade.setLabel("Grade");
	          taGrade.setValue(grade);
	          taGrade.setShowOnProctor(true);
	          testee.getTesteeAttributes().add(taGrade);
	      }
	      testees.add (testee);
	    }

	    return true;
	  }

	  private boolean loadTesteeAttributes(ColumnResultSet reader, Testee testee) throws SQLException {
	      if (reader == null)
	          return true;    //nothing to load

	      while (reader.next())
	      {
	          TesteeAttribute testeeAttribute = new TesteeAttribute();
	          testeeAttribute.setTdsId(reader.getString("TDS_ID"));
	          testeeAttribute.setType(reader.getString("type"));
	          testeeAttribute.setAtLogin(reader.getString("atLogin"));
	          testeeAttribute.setRtsName(reader.getString("rtsName"));
	          testeeAttribute.setLabel(reader.getString("label"));
	          testeeAttribute.setValue(reader.getString("value"));
	          testeeAttribute.setSortOrder(reader.getInt("sortOrder"));
	          testeeAttribute.setEntityKey(reader.getLong("entityKey"));
	          testeeAttribute.setEntityID(reader.getString("entityID"));
	          testeeAttribute.setShowOnProctor(reader.getBoolean("showOnProctor"));

	          testee.getTesteeAttributes().add(testeeAttribute);
	      }
	      return true;
	  }
  
  private Date parseBirthday (String value) throws SQLException {

    if (StringUtils.isEmpty (value)) {
      return DateTime.getMinValue ();
    }

    Date birthday;

    try {
    	// parse other date formats 1. 2008-05-01 2. 01 Nov 2008 3. 03/01/2009
      birthday = DateUtils.parseDateStrictly (value, new String[] { "MMddyyyy", "yyyy-MM-dd", "dd MMM yyyy ", "MM/dd/yyyy" });
    } catch (Exception e) {
      birthday = DateTime.getMinValue (); // keep the birthday as is from RTS
    }

    return birthday;
  }

}
