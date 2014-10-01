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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;

import AIR.Common.DB.AbstractDAO;
import AIR.Common.DB.SQLConnection;
import AIR.Common.Helpers.Constants;
import AIR.Common.Utilities.TDSStringUtils;
import TDS.Proctor.Sql.Data.TestOpportunity;
import TDS.Proctor.Sql.Data.TestOpps;
import TDS.Proctor.Sql.Data.Abstractions.ITestOpportunityRepository;
import TDS.Proctor.Sql.Data.Accommodations.AccType;
import TDS.Proctor.Sql.Data.Accommodations.AccTypes;
import TDS.Proctor.Sql.Data.Accommodations.AccValue;
import TDS.Shared.Data.ColumnResultSet;
import TDS.Shared.Data.ReturnStatus;
import TDS.Shared.Exceptions.ReturnStatusException;
import org.slf4j.LoggerFactory;

public class TestOpportunityRepository extends AbstractDAO implements ITestOpportunityRepository
{

private static final Logger _logger = LoggerFactory.getLogger(TestOpportunityRepository.class);

  public TestOpps getCurrentSessionTestees (UUID sessionKey, long proctorKey, UUID browserKey) throws ReturnStatusException {
    final String sp = "{call P_GetCurrentSessionTestees(?, ?, ?) }";
    TestOpps testOpps = null;
    try (SQLConnection connection = getSQLConnection ()) {
      try (CallableStatement callstatement = connection.prepareCall (sp)) {
        callstatement.setNString (1, sessionKey.toString ());
        callstatement.setLong (2, proctorKey);
        callstatement.setNString (3, browserKey.toString ());
        if (callstatement.execute ()) {
          ColumnResultSet reader = ColumnResultSet.getColumnResultSet (callstatement.getResultSet ());
          ReturnStatusException.getInstanceIfAvailable (reader, "Failed getting session Testees ");

          testOpps = new TestOpps ();
          loadCurrentSessionTestees (reader, testOpps);
        }
      }
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }

    return testOpps;
  }

  private void loadCurrentSessionTestees (ColumnResultSet reader, TestOpps testOpps) throws SQLException {

    if (reader == null)
      return;// nothing to load

    while (reader.next ()) {
      // load stuff here ...
      TestOpportunity tOp = new TestOpportunity ();
      tOp.setOppKey (UUID.fromString (reader.getString ("opportunityKey")));
      tOp.setName (reader.getString ("TesteeName"));
      tOp.setOpp (reader.getInt ("Opportunity"));
      tOp.setTestKey (reader.getString ("_efk_AdminSubject"));

      tOp.setSsid (reader.getString ("TesteeID"));
      tOp.setStatus (reader.getString ("Status"));
      tOp.setTestID (reader.getString ("_efk_TestID"));
      tOp.setTestName (tOp.getTestID ());

      // tOp.testName = tests.GetTestDisplayName(tOp.testKey); //we will
      // need to get this from client side
      // TODO: relying on getInt returning 0 for null in DB, change if
      // implementation changes
      tOp.setItemcount ((!reader.hasColumn ("ItemCount")) ? -1 : reader.getInt ("ItemCount"));
      tOp.setResponseCount ((!reader.hasColumn ("ResponseCount")) ? 0 : reader.getInt ("ResponseCount"));

      tOp.setScore ((!reader.hasColumn ("Score") || reader.getDOUBLE ("Score") == null) ? null : new Integer (reader.getDOUBLE ("Score").intValue ()));
      tOp.setRequestCount ((!reader.hasColumn ("RequestCount") ? 0 : reader.getInt ("RequestCount")));

      tOp.setAccs ((!reader.hasColumn ("Accommodations")) ? null : reader.getString ("Accommodations"));

      String strPauseMins = (!reader.hasColumn ("pauseMinutes") || reader.getINT ("pauseMinutes") == null) ? "" : TDSStringUtils.format (", {0} min", reader.getInt ("pauseMinutes"));

      if (tOp.getScore () != null)
        tOp.setDisplayStatus (TDSStringUtils.format ("{0}: {1}", tOp.getStatus (), tOp.getScore ()));
      else if (!reader.hasColumn ("DateCompleted") || reader.getDate ("DateCompleted") == null)
        tOp.setDisplayStatus (TDSStringUtils.format ("{0}: {1}/{2}{3}", tOp.getStatus (), tOp.getResponseCount (), tOp.getItemcount (), strPauseMins));
      else
        tOp.setDisplayStatus (tOp.getStatus ());
      tOp.setCustAccs (reader.getBoolean ("customAccommodations"));
      testOpps.add (tOp);

    }

  }

  public TestOpps getTestsForApproval (UUID sessionKey, long proctorKey, UUID browserKey) throws ReturnStatusException {
    final String sp = "{call P_GetTestsForApproval(?, ?, ?) }";
    TestOpps testOpps = null;
    try (SQLConnection connection = getSQLConnection ()) {
      try (CallableStatement callstatement = connection.prepareCall (sp)) {
        callstatement.setNString (1, sessionKey.toString ());
        callstatement.setLong (2, proctorKey);
        callstatement.setNString (3, browserKey.toString ());

        if (callstatement.execute ()) {
          ColumnResultSet reader = ColumnResultSet.getColumnResultSet (callstatement.getResultSet ());
          ReturnStatusException.getInstanceIfAvailable (reader, "Failed getting tests for approval");

          testOpps = new TestOpps ();
          loadTestsForApproval (testOpps, reader, callstatement);
        }
      }
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }

    return testOpps;
  }

  private void loadTestsForApproval (TestOpps testOpps, ColumnResultSet reader, CallableStatement callstatement) throws SQLException {
    if (reader == null)
      return;// nothing to load

    TestOpportunity tOp = null;
    UUID oppKey = Constants.UUIDEmpty;
    AccType accType;
    AccTypes accTypes;
    AccValue accValue;
    Map<UUID, Integer> testOppDic = new HashMap<UUID, Integer> (); // store
                                                                   // indexes
                                                                   // for lookup
                                                                   // purpose
    reader.setFixNulls (true);
    try {
      while (reader.next ()) // create all test opps
      {
        tOp = new TestOpportunity ();
        tOp.setAccTypesList (new ArrayList<AccTypes> ());
        // opp infor
        tOp.setOppKey (UUID.fromString (reader.getString ("opportunityKey")));
        tOp.setTestID (reader.getString ("_efk_TestID"));
        tOp.setOpp (reader.getInt ("Opportunity"));
        tOp.setTestKey (reader.getString ("_efk_AdminSubject"));
        // tOp.testName = tests.GetTestDisplayName(tOp.testKey);
        tOp.setStatus (reader.getString ("Status"));
        tOp.setSsid (reader.getString ("TesteeID"));
        tOp.setName (reader.getString ("TesteeName"));
        tOp.setWaitSegment (reader.getInt ("waitingForSegment"));

        testOpps.add (tOp);
        testOppDic.put (tOp.getOppKey (), testOpps.size () - 1);
      }

      tOp = null;
      if (callstatement.getMoreResults ()) {
        reader = ColumnResultSet.getColumnResultSet (callstatement.getResultSet ());
        while (reader.next ()) {
          oppKey = UUID.fromString (reader.getString ("opportunityKey"));
          if (tOp == null || tOp.getOppKey () != oppKey) // diff opp
            tOp = testOpps.get (testOppDic.get (oppKey));

          int segmentPos = reader.getInt ("segment");
          while (tOp.getAccTypesList ().size () <= segmentPos)
            tOp.getAccTypesList ().add (new AccTypes ());

          accTypes = tOp.getAccTypesList ().get (segmentPos); // the segment
                                                              // position
          // is the array position
          // as well

          String strAccType = reader.getString ("AccType");
          accType = accTypes.get (strAccType);
          if (accType == null) { // not exists
            accType = new AccType (strAccType);
            accTypes.put (strAccType, accType);
          }
          accValue = new AccValue (reader.getString ("AccValue"), reader.getString ("AccCode"), true);
          accType.add (accValue);

          tOp.setCustAccs (reader.getBoolean ("customAccommodations"));
          tOp.setLep (reader.getString ("LEP"));
        }
      }
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw e;
    }

    Collections.sort (testOpps);
  }

  public ReturnStatus approveOpportunity (UUID oppKey, UUID sessionKey, long proctorKey, UUID browserKey) throws ReturnStatusException {

    final String sp = "{call P_ApproveOpportunity(?, ?, ?, ?) }";
    ColumnResultSet reader = null;
    ReturnStatus returnStatus = null;
    try (SQLConnection connection = getSQLConnection ()) {
      try (CallableStatement callstatement = connection.prepareCall (sp)) {
        callstatement.setString (1, sessionKey.toString ());
        callstatement.setLong (2, proctorKey);
        callstatement.setString (3, browserKey.toString ());
        callstatement.setNString (4, oppKey.toString ());

        if (callstatement.execute ()){
          reader = ColumnResultSet.getColumnResultSet (callstatement.getResultSet ());
          ReturnStatusException.getInstanceIfAvailable (reader, "Failed to approve opportunity");
        }
      }
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    return returnStatus;

  }

  public ReturnStatus approveAccommodations (UUID oppKey, UUID sessionKey, long proctorKey, UUID browserKey, int segment, String segmentAccs) throws ReturnStatusException {

    final String sp = "{call P_ApproveAccommodations(?, ?, ?, ?, ?, ?) }";

    ReturnStatus returnStatus = null;
    ColumnResultSet reader = null;
    try (SQLConnection connection = getSQLConnection ()) {
      try (CallableStatement callstatement = connection.prepareCall (sp)) {
        callstatement.setNString (1, sessionKey.toString ());
        callstatement.setLong (2, proctorKey);
        callstatement.setNString (3, browserKey.toString ());
        callstatement.setNString (4, oppKey.toString ());
        callstatement.setInt (5, segment);
        callstatement.setString (6, segmentAccs);

        if (callstatement.execute ()) {
          reader = ColumnResultSet.getColumnResultSet (callstatement.getResultSet ());
          ReturnStatusException.getInstanceIfAvailable (reader, "Failed to approve accommodations");
        }
      }
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }

    return returnStatus;
  }

  public ReturnStatus denyOpportunity (UUID oppKey, UUID sessionKey, long proctorKey, UUID browserKey, String reason) throws ReturnStatusException {

    final String sp = "{call P_DenyApproval(?, ?, ?, ?, ?) }";
    ReturnStatus returnStatus = null;
    try (SQLConnection connection = getSQLConnection ()) {
      try (CallableStatement callstatement = connection.prepareCall (sp)) {
        callstatement.setNString (1, sessionKey.toString ());
        callstatement.setNString (2, browserKey.toString ());
        callstatement.setLong (3, proctorKey);
        callstatement.setNString (4, oppKey.toString ());
        callstatement.setString (5, reason);
        
        if (callstatement.execute ()) {
          ColumnResultSet reader = ColumnResultSet.getColumnResultSet (callstatement.getResultSet ());
          ReturnStatusException.getInstanceIfAvailable (reader, "Failed denying the opportunity");
        }
      }
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }

    return returnStatus;
  }

  public ReturnStatus pauseOpportunity (UUID oppKey, UUID sessionKey, long proctorKey, UUID browserKey) throws ReturnStatusException {

    final String sp = "{call P_PauseOpportunity(?, ?, ?, ?) }";
    ReturnStatus returnStatus = null;
    try (SQLConnection connection = getSQLConnection ()) {
      try (CallableStatement callstatement = connection.prepareCall (sp)) {
        callstatement.setNString (1, sessionKey.toString ());
        callstatement.setLong (2, proctorKey);
        callstatement.setNString (3, browserKey.toString ());
        callstatement.setNString (4, oppKey.toString ());
        if (callstatement.execute ()) {
          ColumnResultSet reader = ColumnResultSet.getColumnResultSet (callstatement.getResultSet ());
          ReturnStatusException.getInstanceIfAvailable (reader, "Failed to pause the opportunity");
        }
      }
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }

    return returnStatus;
  }

}
