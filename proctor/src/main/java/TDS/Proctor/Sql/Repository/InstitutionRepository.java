/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package TDS.Proctor.Sql.Repository;

/**
 * @author efurman
 * 
 */

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;

import AIR.Common.DB.AbstractDAO;
import AIR.Common.DB.SQLConnection;
import AIR.Common.DB.results.DbResultRecord;
import AIR.Common.DB.results.SingleDataResultSet;
import TDS.Proctor.Sql.Data.District;
import TDS.Proctor.Sql.Data.Districts;
import TDS.Proctor.Sql.Data.Grade;
import TDS.Proctor.Sql.Data.Grades;
import TDS.Proctor.Sql.Data.Institution;
import TDS.Proctor.Sql.Data.InstitutionList;
import TDS.Proctor.Sql.Data.School;
import TDS.Proctor.Sql.Data.Schools;
import TDS.Proctor.Sql.Data.Abstractions.IInstitutionRepository;
import TDS.Shared.Exceptions.ReturnStatusException;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import tds.dll.api.IRtsDLL;

public class InstitutionRepository extends AbstractDAO implements IInstitutionRepository
{

	private static final Logger _logger = LoggerFactory.getLogger(InstitutionRepository.class);
	@Autowired
	IRtsDLL _rdll		  = null;


  public InstitutionList getUserInstitutions (String clientname, long userKey, int sessionType, List<String> roles) throws ReturnStatusException {

//    HashMap<String, String> rolesMap = new HashMap<String, String> ();
    InstitutionList institutions = null;

    try (SQLConnection connection = getSQLConnection ()) {
      SingleDataResultSet result = _rdll.GetRTSUserRoles_SP (connection, clientname, userKey, sessionType);
      ReturnStatusException.getInstanceIfAvailable (result);

      Iterator<DbResultRecord> records = result.getRecords ();
      // handle sql exceptions, null result sets and result sets with columns
      // reason and status inside repository layer

      // this code will execute only if database status is success
      institutions = new InstitutionList ();
      while (records.hasNext ()) {
        DbResultRecord record = records.next ();
        // rolesMap is map to convert rts role into tds role
        String tdsRole = record.<String> get ("tds_role");
//        String role = record.<String> get ("tds_role");
//        String tdsRole = rolesMap.get (role);
        if (!roles.contains (tdsRole)) // ignore role
          continue;

        String instType = record.<String> get ("institutionType"); // INSTITUTION|DISTRICT|STATE
        if (Institution.isState (instType)) // state user
        {
          institutions = getInstitutions (clientname); // get all districts in
                                                       // a state
        } else {
          institutions.add (new Institution (record.<String> get ("InstitutionKey"), record.<String> get ("InstitutionName"), record.<String> get ("InstitutionID"), instType));
        }
      }
      if (institutions.size () == 1)
        institutions.get (0).setSelected (true); // select if only one in the
                                                 // list
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    return institutions;
  }

  public InstitutionList getInstitutions (String clientname) throws ReturnStatusException {

    InstitutionList institutions = new InstitutionList ();

    try (SQLConnection connection = getSQLConnection ()) {
      SingleDataResultSet result = _rdll.R_GetDistricts_SP (connection, clientname);
      ReturnStatusException.getInstanceIfAvailable (result);
      
      loadDistrictInstitutions (result, institutions);

    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    return institutions;
  }

  private boolean loadDistrictInstitutions (SingleDataResultSet result, InstitutionList institutions) throws SQLException {

    if (result == null)
      return false;

    Iterator<DbResultRecord> records = result.getRecords ();

    while (records.hasNext ()) // DistrictName, DistrictID, RTSKey
    {
      DbResultRecord record = records.next ();
      Institution inst = new Institution (record.<String> get ("RTSKey"), record.<String> get ("DistrictName"), record.<String> get ("DistrictID"), "DISTRICT");
	  institutions.add (inst);
    }
    return true;
  }

  public Districts getDistricts (String clientname) throws ReturnStatusException {
    Districts districts = new Districts ();

    try {
      try (SQLConnection connection = getSQLConnection ()) {

        SingleDataResultSet result = _rdll.R_GetDistricts_SP (connection, clientname);
        ReturnStatusException.getInstanceIfAvailable (result);

        loadDistricts (result, districts);
      }
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    return districts;
  }

  private boolean loadDistricts (SingleDataResultSet result, Districts districts) throws SQLException {

    if (result == null)
      return true;

    Iterator<DbResultRecord> records = result.getRecords ();

    // this is the result set we are interested in.
    while (records.hasNext ()) {
      DbResultRecord record = records.next ();
      String districtName = record.<String> get ("DistrictName");
      String districtId = record.<String> get ("DistrictID");
      String rtsKey = record.<String> get ("RTSKey");
      District district = new District (districtName, districtId, rtsKey);
      districts.add (district);
      records.next ();
    }
    return true;
  }

  public Schools getSchools (String clientname, String districtKey) throws ReturnStatusException {

    Schools schools = new Schools ();

    try (SQLConnection connection = getSQLConnection ()) {
      SingleDataResultSet result = _rdll.R_GetDistrictSchools_SP (connection, clientname, districtKey);
      ReturnStatusException.getInstanceIfAvailable (result);

      loadSchools (result, schools);

    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }

    return schools;
  }

  private boolean loadSchools (SingleDataResultSet result, Schools schools) throws SQLException {

    if (result == null)
      return true;// nothing to load

    Iterator<DbResultRecord> records = result.getRecords ();
    while (records.hasNext ()) {
      DbResultRecord record = records.next ();
      School school = new School (record.<String> get ("SchoolName"), record.<String> get ("SchoolID"), record.<String> get ("RTSKey"));
      schools.add (school);
    }
    return true;
  }

  public Grades getGrades (String clientname, String schoolKey) throws ReturnStatusException {

    Grades grades = new Grades ();

    try (SQLConnection connection = getSQLConnection ()) {
      SingleDataResultSet result = _rdll.R_GetSchoolGrades_SP (connection, clientname, schoolKey);
      ReturnStatusException.getInstanceIfAvailable (result);
      
      loadGrades (result, grades);
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    return grades;
  }

  private boolean loadGrades (SingleDataResultSet result, Grades grades) throws SQLException {

    if (result == null)
      return true;// nothing to load

    Iterator<DbResultRecord> records = result.getRecords ();

    while (records.hasNext ()) {
      DbResultRecord record = records.next ();
      Grade grade = new Grade (record.<String> get ("grade"));
      grades.add (grade);
    }

    return true;
  }
}
