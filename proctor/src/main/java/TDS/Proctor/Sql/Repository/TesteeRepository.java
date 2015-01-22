/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package TDS.Proctor.Sql.Repository;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import tds.dll.api.IProctorDLL;
import tds.dll.api.IRtsDLL;
import AIR.Common.DB.AbstractDAO;
import AIR.Common.DB.SQLConnection;
import AIR.Common.DB.results.DbResultRecord;
import AIR.Common.DB.results.SingleDataResultSet;
import AIR.Common.time.DateTime;
import TDS.Proctor.Sql.Data.Testee;
import TDS.Proctor.Sql.Data.TesteeAttribute;
import TDS.Proctor.Sql.Data.Testees;
import TDS.Proctor.Sql.Data.Abstractions.ITesteeRepository;
import TDS.Shared.Data.ReturnStatus;
import TDS.Shared.Exceptions.ReturnStatusException;

/**
 * @author efurman
 * 
 */
public class TesteeRepository extends AbstractDAO  implements ITesteeRepository
{
	private static final Logger _logger = LoggerFactory.getLogger(TesteeRepository.class);
    @Autowired
	IProctorDLL _pdll     = null;
    @Autowired
	IRtsDLL _rdll       = null;
	
  public Testee getTestee (String clientName, String testeeId, long proctorKey) throws ReturnStatusException {

    Testee testee = new Testee ();
    try (SQLConnection connection = getSQLConnection ()) {
      SingleDataResultSet result = _pdll.P_GetRTSTestee_SP (connection, clientName, testeeId, proctorKey);
      ReturnStatusException.getInstanceIfAvailable (result, "The SP P_GetRTSTestee did not return any records.");

      result.setFixNulls (true);
      Iterator<DbResultRecord> records = result.getRecords ();
      
      loadTesteeAttributes(records, testee);
      
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
        
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    return testee;
  }

  // advance search
  // Get all students for a school If grade level is null, get all grade
  // levels
  public Testees getSchoolTestees (String clientName, String schoolKey, String grade, String firstName, String lastName) throws ReturnStatusException {

    Testees testees = new Testees ();
    try (SQLConnection connection = getSQLConnection ()) {
      SingleDataResultSet result = _rdll.getSchoolStudents (connection, clientName, schoolKey, grade, firstName, lastName);
      ReturnStatusException.getInstanceIfAvailable (result, "The SP GetSchoolStudents did not return any records.");
      //ReturnStatusException.getInstanceIfAvailable (result);
      
      Iterator<DbResultRecord> records = result.getRecords ();
      
      // get here only if we get valid rows
      loadSchoolTestee (records, testees);
            
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    return testees;
  }

  private boolean loadSchoolTestee (Iterator<DbResultRecord> records, Testees testees) throws SQLException {

    if (records == null)
      return true;// nothing to load

    while (records.hasNext ()) {
      DbResultRecord record = records.next ();
      // load stuff here ...
      Testee testee = new Testee ();
      TesteeAttribute taRtsKey, taSSID, taLastName, taFirstName, taGrade;
      String rtsKey = record.<String> get("rtsKey");
      if (rtsKey != null) {
          taRtsKey = new TesteeAttribute();
          taRtsKey.setTdsId("--RTS KEY--");
          taRtsKey.setType("ENTITYKEY");
          taRtsKey.setValue(rtsKey.toString());
          taRtsKey.setShowOnProctor(true);
          testee.getTesteeAttributes().add(taRtsKey);
      }
      String SSID = record.<String> get("SSID");
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
      String lastName = record.<String> get("LastName");
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
      String firstName = record.<String> get("FirstName");
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
      String grade = record.<String> get("Grade");
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

  private boolean loadTesteeAttributes(Iterator<DbResultRecord> records, Testee testee) {
      if (records == null) {
          return true;    //nothing to load
      }
      
      while (records.hasNext()) {
    	    DbResultRecord record = records.next ();
          
          TesteeAttribute testeeAttribute = new TesteeAttribute();
          testeeAttribute.setTdsId(record.<String> get ("TDS_ID"));
          testeeAttribute.setType(record.<String> get ("type"));
          testeeAttribute.setAtLogin(record.<String> get ("atLogin"));
          testeeAttribute.setRtsName(record.<String> get ("rtsName"));
          testeeAttribute.setLabel(record.<String> get ("label"));
          testeeAttribute.setValue(record.<String> get ("value"));
          testeeAttribute.setSortOrder(record.<Integer> get ("sortOrder"));
          testeeAttribute.setEntityKey(record.<String> get ("entityKey"));
          testeeAttribute.setEntityID(record.<String> get ("entityID"));
          testeeAttribute.setShowOnProctor(record.<Boolean> get ("showOnProctor"));


          testee.getTesteeAttributes().add(testeeAttribute);
      }
      return true;
  }
  
  // TODO
  private Date parseBirthday (String value) throws SQLException {

    Calendar cal = Calendar.getInstance ();
    cal.setTimeInMillis (0);

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
