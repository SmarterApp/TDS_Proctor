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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;

import AIR.Common.DB.AbstractDAO;
import AIR.Common.DB.SQLConnection;
import AIR.Common.DB.results.DbResultRecord;
import AIR.Common.DB.results.MultiDataResultSet;
import AIR.Common.DB.results.SingleDataResultSet;
import AIR.Common.Helpers.Constants;
import TDS.Proctor.Sql.Data.TestOpportunity;
import TDS.Proctor.Sql.Data.TestOpps;
import TDS.Proctor.Sql.Data.Abstractions.ITestOpportunityRepository;
import TDS.Proctor.Sql.Data.Accommodations.AccType;
import TDS.Proctor.Sql.Data.Accommodations.AccTypes;
import TDS.Proctor.Sql.Data.Accommodations.AccValue;
import TDS.Shared.Data.ReturnStatus;
import TDS.Shared.Exceptions.ReturnStatusException;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import tds.dll.api.IProctorDLL;

/**
 * @author temp_ukommineni
 * 
 */
public class TestOpportunityRepository extends AbstractDAO implements ITestOpportunityRepository
{
  private static final Logger _logger = LoggerFactory.getLogger(TestOpportunityRepository.class);
  @Autowired
  IProctorDLL dll     = null;

  public TestOpps getCurrentSessionTestees (UUID sessionKey, long proctorKey, UUID browserKey) throws ReturnStatusException {
    TestOpps testOpps = null;
    try (SQLConnection connection = getSQLConnection ()) {
      SingleDataResultSet result = dll.P_GetCurrentSessionTestees_SP (connection, sessionKey, proctorKey, browserKey);

      Iterator<DbResultRecord> records = result.getRecords ();
      ReturnStatusException.getInstanceIfAvailable (result);
      testOpps = new TestOpps ();
      loadCurrentSessionTestees (records, testOpps);

    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }

    return testOpps;
  }

  //TODO should be void
  private ReturnStatus loadCurrentSessionTestees (Iterator<DbResultRecord> records, TestOpps testOpps) throws SQLException {

    if (records == null)
      return null;// nothing to load
    // check for errors

    // if (hasColumn (reader, "status") && hasColumn (reader, "reason")) {
    // return ReturnStatus.parse (reader);
    // }
    while (records.hasNext ()) {
      DbResultRecord record = records.next ();
      // load stuff here ...
      TestOpportunity tOp = new TestOpportunity ();
      tOp.setOppKey (record.<UUID> get ("opportunityKey"));
      tOp.setName (record.<String> get ("TesteeName"));
      tOp.setOpp (record.<Integer> get ("Opportunity"));
      tOp.setTestKey (record.<String> get ("_efk_AdminSubject"));

      tOp.setSsid (record.<String> get ("TesteeID"));
      tOp.setStatus (record.<String> get ("Status"));
      tOp.setTestID (record.<String> get ("_efk_TestID"));
      tOp.setTestName (tOp.getTestID ());

      // tOp.testName = tests.GetTestDisplayName(tOp.testKey); //we will
      // need to get this from client side
      // TODO: relying on getInt returning 0 for null in DB, change if
      // implementation changes
      tOp.setItemcount ((!record.hasColumn ("ItemCount")) ? -1 : record.<Integer> get ("ItemCount"));
      tOp.setResponseCount ((!record.hasColumn ("ResponseCount")) ? 0 : record.<Integer> get ("ResponseCount"));

      Integer score = null;
      
      try {
        score = (!record.hasColumn ("Score") || record.<Float> get ("Score") == null) ? null : new Integer (record.<Float> get ("Score").intValue ());
      } catch (ClassCastException e) {
        if(record. get ("Score")!=null) {
          _logger.error ("Error while converting record to float :: "+record. get ("Score").toString ());
        }
      }
      
      tOp.setScore (score);
      
      
      tOp.setRequestCount ((!record.hasColumn ("RequestCount") ? 0 : record.<Integer> get ("RequestCount")));

      tOp.setAccs ((!record.hasColumn ("Accommodations")) ? null : record.<String> get ("Accommodations"));

      String strPauseMins = (!record.hasColumn ("pauseMinutes") || record.<String> get ("pauseMinutes") == null) ? "" : String.format (", %s min", record.<String> get ("pauseMinutes"));

      if (tOp.getScore () != null)
        tOp.setDisplayStatus (String.format ("%s: %d", tOp.getStatus (), tOp.getScore ()));
      else if (!record.hasColumn ("DateCompleted") || record.<Date> get ("DateCompleted") == null)
        tOp.setDisplayStatus (String.format ("%s: %d/%d%s", tOp.getStatus (), tOp.getResponseCount (), tOp.getItemcount (), strPauseMins));
      else
        tOp.setDisplayStatus (tOp.getStatus ());
      tOp.setCustAccs (record.<Boolean> get ("customAccommodations"));
      testOpps.add (tOp);

    }

    return null;
  }

  public TestOpps getTestsForApproval (UUID sessionKey, long proctorKey, UUID browserKey) throws ReturnStatusException {
    TestOpps testOpps = null;
    try (SQLConnection connection = getSQLConnection ()) {
      MultiDataResultSet resultsets = dll.P_GetTestsForApproval_SP (connection, sessionKey, proctorKey, browserKey);

      Iterator<SingleDataResultSet> results = resultsets.getResultSets ();
      SingleDataResultSet firstResultSet = results.next ();
      ReturnStatusException.getInstanceIfAvailable (firstResultSet);
      Iterator<DbResultRecord> records = firstResultSet.getRecords ();

      testOpps = new TestOpps ();
      loadTestsForApproval (testOpps, records, resultsets);

    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }

    return testOpps;
  }

  private void loadTestsForApproval (TestOpps testOpps, Iterator<DbResultRecord> records, MultiDataResultSet resultsets) throws SQLException {

    Iterator<SingleDataResultSet> results = resultsets.getResultSets ();
    SingleDataResultSet firstResultSet = results.next ();

    if (firstResultSet == null)
      return;// nothing to load

    if (firstResultSet != null) {
      records = firstResultSet.getRecords ();

      TestOpportunity tOp = null;
      UUID oppKey = Constants.UUIDEmpty;
      AccType accType;
      AccTypes accTypes;
      AccValue accValue;
      // store indexes for lookup purpose
      Map<UUID, Integer> testOppDic = new HashMap<UUID, Integer> (); 
      firstResultSet.setFixNulls (true);
      while (records.hasNext ()) // create all test opps
	{
	  DbResultRecord record = records.next ();
	  tOp = new TestOpportunity ();
	  tOp.setAccTypesList (new ArrayList<AccTypes> ());
	  // opp infor
	  tOp.setOppKey (record.<UUID> get ("opportunityKey"));
	  tOp.setTestID (record.<String> get ("_efk_TestID"));
	  tOp.setOpp (record.<Integer> get ("Opportunity"));
	  tOp.setTestKey (record.<String> get ("_efk_AdminSubject"));
	  // tOp.testName = tests.GetTestDisplayName(tOp.testKey);
	  tOp.setStatus (record.<String> get ("Status"));
	  tOp.setSsid (record.<String> get ("TesteeID"));
	  tOp.setName (record.<String> get ("TesteeName"));
	  tOp.setWaitSegment (record.<Integer> get ("waitingForSegment"));

	  testOpps.add (tOp);
	  testOppDic.put (tOp.getOppKey (), testOpps.size () - 1);
	}

	tOp = null;
	if (results.hasNext ()) {
	  SingleDataResultSet secondResultSet = results.next ();
	  records = null;
	  records = secondResultSet.getRecords ();
	  if (records != null) {
	    while (records.hasNext ()) {
	      DbResultRecord record = records.next ();
	      oppKey = record.<UUID> get ("opportunityKey");
	      if (tOp == null || tOp.getOppKey () != oppKey) // diff opp
	        tOp = testOpps.get (testOppDic.get (oppKey));

	      int segmentPos = record.<Integer> get ("segment");
	      while (tOp.getAccTypesList ().size () <= segmentPos)
	        tOp.getAccTypesList ().add (new AccTypes ());

	      accTypes = tOp.getAccTypesList ().get (segmentPos); // the segment
	                                                          // position
	      // is the array position
	      // as well

	      String strAccType = record.<String> get ("AccType");
	      accType = accTypes.get (strAccType);
	      if (accType == null) { // not exists
	        accType = new AccType (strAccType);
	        accTypes.put (strAccType, accType);
	      }
	      accValue = new AccValue (record.<String> get ("AccValue"), record.<String> get ("AccCode"), true);
	      accType.add (accValue);

	      tOp.setCustAccs (record.<Boolean> get ("customAccommodations"));
	      tOp.setLep (record.<String> get ("LEP"));
	    }
	  }
	}

      Collections.sort (testOpps);
    }
  }

  //TODO should be void
  public ReturnStatus approveOpportunity (UUID oppKey, UUID sessionKey, long proctorKey, UUID browserKey) throws ReturnStatusException {

    try (SQLConnection connection = getSQLConnection ()) {
      SingleDataResultSet result = dll.P_ApproveOpportunity_SP (connection, sessionKey, proctorKey, browserKey, oppKey);
      ReturnStatusException.getInstanceIfAvailable (result);

    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }

    return null;
  }

  //TODO should be void
  public ReturnStatus approveAccommodations (UUID oppKey, UUID sessionKey, long proctorKey, UUID browserKey, int segment, String segmentAccs) throws ReturnStatusException {

    try (SQLConnection connection = getSQLConnection ()) {
      SingleDataResultSet result = dll.P_ApproveAccommodations_SP (connection, sessionKey, proctorKey, browserKey, oppKey, segment, segmentAccs);
      ReturnStatusException.getInstanceIfAvailable (result);
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }

    return null;
  }

  //TODO should be void
  public ReturnStatus denyOpportunity (UUID oppKey, UUID sessionKey, long proctorKey, UUID browserKey, String reason) throws ReturnStatusException {

    try (SQLConnection connection = getSQLConnection ()) {
      SingleDataResultSet result = dll.P_DenyApproval_SP (connection, sessionKey, proctorKey, browserKey, oppKey, reason);
      ReturnStatusException.getInstanceIfAvailable (result);

    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }

    return null;
  }

  //TODO should be void
  public ReturnStatus pauseOpportunity (UUID oppKey, UUID sessionKey, long proctorKey, UUID browserKey) throws ReturnStatusException {

    try (SQLConnection connection = getSQLConnection ()) {
      SingleDataResultSet result = dll.P_PauseOpportunity_SP (connection, sessionKey, proctorKey, browserKey, oppKey);
      ReturnStatusException.getInstanceIfAvailable (result);

    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }

    return null;
  }

}
