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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;

import AIR.Common.DB.AbstractDAO;
import AIR.Common.DB.SQLConnection;
import TDS.Proctor.Sql.Data.District;
import TDS.Proctor.Sql.Data.Districts;
import TDS.Proctor.Sql.Data.Grade;
import TDS.Proctor.Sql.Data.Grades;
import TDS.Proctor.Sql.Data.Institution;
import TDS.Proctor.Sql.Data.InstitutionList;
import TDS.Proctor.Sql.Data.School;
import TDS.Proctor.Sql.Data.Schools;
import TDS.Proctor.Sql.Data.Abstractions.IInstitutionRepository;
import TDS.Shared.Data.ColumnResultSet;
import TDS.Shared.Exceptions.ReturnStatusException;
import org.slf4j.LoggerFactory;

public class InstitutionRepository extends AbstractDAO implements IInstitutionRepository
{

private static final Logger _logger = LoggerFactory.getLogger(InstitutionRepository.class);

  public InstitutionList getUserInstitutions (String clientname, long userKey, int sessionType, List<String> roles) throws ReturnStatusException {
    ColumnResultSet reader = null;
    InstitutionList institutions = null;
    String CMD_GET_DISTRICTS = "{call GetRTSUserRoles(?, ?, ?) }";
    try (SQLConnection connection = getSQLConnection ()) {
      try (CallableStatement callstatement = connection.prepareCall (CMD_GET_DISTRICTS)) {
        callstatement.setString (1, clientname);
        callstatement.setLong (2, userKey);
        callstatement.setInt (3, sessionType);
        boolean exeStoredProc = callstatement.execute ();
        if (exeStoredProc)
          reader = ColumnResultSet.getColumnResultSet (callstatement.getResultSet ());

        // handle sql exceptions, null result sets and result sets with columns
        // reason and status inside repository layer
        ReturnStatusException.getInstanceIfAvailable (reader, "Failed reading user institutions");

        // this code will execute only if database status is success
        institutions = new InstitutionList ();
        while (reader.next ()) {
          String tdsRole = reader.getString ("tds_role");
          if (!roles.contains (tdsRole)) // ignore role
            continue;

          String instType = reader.getString ("institutionType"); // INSTITUTION|DISTRICT|STATE
          if (Institution.isState (instType)) { // state user
            institutions = getInstitutions (clientname); // get all districts in
                                                         // a state
          } else {
            institutions.add (new Institution (reader.getString ("InstitutionKey"), reader.getString ("InstitutionName"), reader.getString ("InstitutionID"), instType));
          }
        }

        if (institutions.size () == 1)
          institutions.get (0).setSelected (true); // select if only one in the
                                                   // list
      }
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    return institutions;
  }

  public InstitutionList getInstitutions (String clientname) throws ReturnStatusException {

    ColumnResultSet reader = null;
    InstitutionList institutions = new InstitutionList ();
    String CMD_GET_DISTRICTS = "{call R_GetDistricts(?) }";
    try (SQLConnection connection = getSQLConnection ()) {
      try (CallableStatement callstatement = connection.prepareCall (CMD_GET_DISTRICTS)) {
        callstatement.setString (1, clientname);
        boolean exeStoredProc = callstatement.execute ();
        if (exeStoredProc)
          reader = ColumnResultSet.getColumnResultSet (callstatement.getResultSet ());

        // handle sql exceptions, null result sets and result sets with columns
        // reason and status inside repository layer
        ReturnStatusException.getInstanceIfAvailable (reader, "Failed getting institutions");

        loadDistrictInstitutions (callstatement, institutions);
      }
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    return institutions;
  }

  public Districts getDistricts (String clientname) throws ReturnStatusException {
    Districts districts = new Districts ();
    ColumnResultSet reader = null;
    try {
      String CMD_GET_DISTRICTS = "{call R_GetDistricts(?) }";
      try (SQLConnection connection = getSQLConnection ()) {
        try (CallableStatement callstatement = connection.prepareCall (CMD_GET_DISTRICTS)) {
          callstatement.setString (1, clientname);
          boolean exeStoredProc = callstatement.execute ();
          if (exeStoredProc)
            reader = ColumnResultSet.getColumnResultSet (callstatement.getResultSet ());

          // handle sql exceptions, null result sets and result sets with
          // columns reason and status inside repository layer
          ReturnStatusException.getInstanceIfAvailable (reader, "Failed Getting Districs");
          loadDistricts (callstatement, districts);
        }
      }
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }

    return districts;
  }

  public Schools getSchools (String clientname, String districtKey) throws ReturnStatusException {

    ColumnResultSet reader = null;
    Schools schools = new Schools ();
    String CMD_GET_SCHOOLS = "{call R_GetDistrictSchools(?, ?) }";
    try (SQLConnection connection = getSQLConnection ()) {
      try (CallableStatement callstatement = connection.prepareCall (CMD_GET_SCHOOLS)) {
        callstatement.setString (1, clientname);
        callstatement.setString (2, districtKey);
        boolean exeStoredProc = callstatement.execute ();
        if (exeStoredProc)
          reader = ColumnResultSet.getColumnResultSet (callstatement.getResultSet ());

        // handle sql exceptions, null result sets and result sets with
        // columns reason and status inside repository layer
        ReturnStatusException.getInstanceIfAvailable (reader, "Failed Getting Schools");
        loadSchools (callstatement, schools);
      }
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }

    return schools;
  }

  public Grades getGrades (String clientname, String schoolKey) throws ReturnStatusException {
    ColumnResultSet reader = null;
    Grades grades = new Grades ();
    String CMD_GET_GRADES = "{call R_GetSchoolGrades(?,?) }";
    try (SQLConnection connection = getSQLConnection ()) {
      try (CallableStatement callstatement = connection.prepareCall (CMD_GET_GRADES)) {
        callstatement.setString (1, clientname);
        callstatement.setString (2, schoolKey);
        boolean exeStoredProc = callstatement.execute ();
        if (exeStoredProc)
          reader = ColumnResultSet.getColumnResultSet (callstatement.getResultSet ());

        // handle sql exceptions, null result sets and result sets with
        // columns reason and status inside repository layer
        ReturnStatusException.getInstanceIfAvailable (reader, "Failed Getting Grades");
        loadGrades (callstatement, grades);
      }
    } catch (SQLException e) {
      // TODO
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    return grades;
  }

  private boolean loadSchools (CallableStatement callStatement, Schools schools) throws SQLException {

    if (callStatement == null)
      return true;// nothing to load

    ResultSet reader = callStatement.getResultSet ();
    while (reader.next ()) {
      School school = new School (reader.getString ("SchoolName"), reader.getString ("SchoolID"), reader.getString ("RTSKey"));
      schools.add (school);
    }
    return true;
  }

  private boolean loadGrades (CallableStatement callstatement, Grades grades) throws SQLException {

    if (callstatement == null)
      return true;// nothing to load

    ResultSet reader = callstatement.getResultSet ();

    while (reader.next ()) {
      Grade grade = new Grade (reader.getString ("grade"));
      grades.add (grade);
    }

    return true;
  }

  private boolean loadDistricts (CallableStatement callStatement, Districts districts) throws SQLException {

    boolean properResultSet = false;

    ResultSet reader = callStatement.getResultSet ();

    // this is the result set we are interested in.
    while (reader.next ()) {
      String districtName = reader.getString ("DistrictName");
      String districtId = reader.getString ("DistrictID");
      String rtsKey = reader.getString ("RTSKey");
      District district = new District (districtName, districtId, rtsKey);
      districts.add (district);
    }
    properResultSet = true;
    return properResultSet;
  }

  private boolean loadDistrictInstitutions (CallableStatement callStatement, InstitutionList institutions) throws SQLException {

    if (callStatement == null)
      return false;

    ResultSet reader = callStatement.getResultSet ();
    while (reader.next ()) // DistrictName, DistrictID, RTSKey
    {
      try {
        Institution inst = new Institution (reader.getString ("RTSKey"), reader.getString ("DistrictName"), reader.getString ("DistrictID"), "DISTRICT");
        institutions.add (inst);
      } catch (SQLException e) {
        _logger.error (e.getMessage ());
        throw e;
      }
    }
    return true;
  }
}
