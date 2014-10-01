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
import java.util.Date;
import java.util.Iterator;
import java.util.UUID;

import org.slf4j.Logger;

import AIR.Common.DB.AbstractDAO;
import AIR.Common.DB.DbComparator;
import AIR.Common.DB.SQLConnection;
import AIR.Common.DB.results.DbResultRecord;
import AIR.Common.DB.results.SingleDataResultSet;
import TDS.Proctor.Sql.Data.TesteeRequest;
import TDS.Proctor.Sql.Data.TesteeRequests;
import TDS.Proctor.Sql.Data.Abstractions.ITesteeRequestRepository;
import TDS.Shared.Exceptions.ReturnStatusException;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import tds.dll.api.IProctorDLL;

/**
 * @author efurman
 * 
 */
public class TesteeRequestRepository extends AbstractDAO implements ITesteeRequestRepository
{
  private static final Logger _logger = LoggerFactory.getLogger(TesteeRequestRepository.class);
  @Autowired
  IProctorDLL dll     = null;

  public TesteeRequests getCurrentTesteeRequests (UUID opportunityKey, UUID sessionKey, long proctorKey, UUID browserKey) throws ReturnStatusException {
    TesteeRequests requests = null;
    try (SQLConnection connection = getSQLConnection ()) {
      SingleDataResultSet result = dll.P_GetCurrentTesteeRequests_SP (connection, sessionKey, proctorKey, browserKey, opportunityKey);

      Iterator<DbResultRecord> records = result.getRecords ();
      ReturnStatusException.getInstanceIfAvailable (result);

      requests = new TesteeRequests ();
      result.setFixNulls (true);
      loadTesteeRequests (records, requests);

    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    return requests;
  }

  public TesteeRequests getApprovedTesteeRequests (UUID sessionKey, long proctorKey, UUID browserKey) throws ReturnStatusException {
    TesteeRequests requests = null;
    try (SQLConnection connection = getSQLConnection ()) {
      SingleDataResultSet result = dll.P_GetApprovedTesteeRequests_SP (connection, sessionKey, proctorKey, browserKey);

      Iterator<DbResultRecord> records = result.getRecords ();
      ReturnStatusException.getInstanceIfAvailable (result);

      result.setFixNulls (true);
      requests = new TesteeRequests ();
      loadTesteeRequests (records, requests);

    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    return requests;
  }

  public TesteeRequest getTesteeRequestValues (UUID sessionKey, long proctorKey, UUID browserKey, UUID requestKey, boolean markFulfilled) throws ReturnStatusException {
    TesteeRequest testeeRequest = null;
    try (SQLConnection connection = getSQLConnection ()) {
      SingleDataResultSet result = dll.P_GetTesteeRequestValues_SP (connection, sessionKey, proctorKey, browserKey, requestKey, markFulfilled);

      Iterator<DbResultRecord> records = result.getRecords ();
      ReturnStatusException.getInstanceIfAvailable (result);

      // if (reader == null) {
      // _logger.error ("Invalid request or request key");
      // throw new Exception ("Invalid request or request key"); //
      // ReturnStatusException(rs);
      // }

      while (records.hasNext ()) {
        DbResultRecord record = records.next ();
        testeeRequest = new TesteeRequest (requestKey);
        testeeRequest.setSessionKey (sessionKey);
        testeeRequest.setOppKey (record.<UUID> get ("opportunityKey"));
        testeeRequest.setTesteeKey (record.<Long> get ("_efk_Testee"));
        testeeRequest.setTestID (record.<String> get ("_efk_TestID"));
        testeeRequest.setOpportunity (record.<Integer> get ("Opportunity"));
        testeeRequest.setTesteeID (record.<String> get ("TesteeID"));
        testeeRequest.setTesteeName (record.<String> get ("TesteeName"));
        testeeRequest.setRequestType (record.<String> get ("RequestType"));
        if (DbComparator.isEqual ("PRINTITEM", testeeRequest.getRequestType ())) {
          testeeRequest.setRequestValue (record.<String> get ("ItemFile"));
          String itemResponse = record.<String> get ("ItemResponse");
          if (itemResponse != null)
            testeeRequest.setItemResponse (itemResponse);
        } else if (DbComparator.isEqual ("EMBOSSPASSAGE", testeeRequest.getRequestType ()) || DbComparator.isEqual ("EMBOSSITEM", testeeRequest.getRequestType ())) {
          testeeRequest.setRequestValue (record.<String> get ("RequestValue"));
        } else
          testeeRequest.setRequestValue (record.<String> get ("StimulusFile"));

        testeeRequest.setRequestDesc (record.<String> get ("RequestDescription"));
        if (record.hasColumn("ItemPosition"))
            testeeRequest.setItemPosition(record.<Integer> get("ItemPosition"));
        
        testeeRequest.setAccCode (record.<String> get ("AccCode"));
        testeeRequest.setLanguage (record.<String> get ("Language"));
        String requestParameters = record.<String> get ("RequestParameters");
        if (requestParameters != null)
          testeeRequest.setRequestParameters (record.<String> get ("RequestParameters"));
      }
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    return testeeRequest;
  }

  public void denyTesteeRequest (UUID sessionKey, long proctorKey, UUID browserKey, UUID requestKey, String reason) throws ReturnStatusException {
    try (SQLConnection connection = getSQLConnection ()) {
      SingleDataResultSet result = dll.P_DenyTesteeRequest_SP (connection, sessionKey, proctorKey, browserKey, requestKey, reason);
      ReturnStatusException.getInstanceIfAvailable (result);

    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
  }

  private boolean loadTesteeRequests (Iterator<DbResultRecord> records, TesteeRequests testeeRequests) {

    if (records == null)
      return true;// nothing to load

    while (records.hasNext ()) {
        DbResultRecord record = records.next ();
        TesteeRequest testeeRequest = new TesteeRequest ();

        testeeRequest.setKey (record.<UUID> get ("_Key"));

        if (record.hasColumn ("_fk_TestOpportunity"))
          testeeRequest.setOppKey (record.<UUID> get ("_fk_TestOpportunity"));

        testeeRequest.setSessionKey (record.<UUID> get ("_fk_Session"));

        testeeRequest.setRequestType (record.<String> get ("RequestType"));
        testeeRequest.setRequestType (record.<String> get ("RequestValue"));
        testeeRequest.setDateSubmitted (record.<Date> get ("DateSubmitted"));
        // Date dateFulfilled = record.<Date> get ("DateFulfilled");
        testeeRequest.setDateFulfilled (record.<Date> get ("DateFulfilled"));
        // testeeRequest.setDateFulfilled (reader.wasNull () ? null :
        // dateFulfilled);
        testeeRequest.setDeniedReason (record.<String> get ("Denied"));
        testeeRequest.setItemPage (record.<Integer> get ("ItemPage"));
        testeeRequest.setItemPosition (record.<Integer> get ("ItemPosition"));
        testeeRequest.setRequestDesc (record.<String> get ("RequestDescription"));
        testeeRequests.add (testeeRequest);
      }

    return true;
  }

}
