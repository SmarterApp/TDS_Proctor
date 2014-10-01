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
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.UUID;

import org.slf4j.Logger;

import AIR.Common.DB.AbstractDAO;
import AIR.Common.DB.SQLConnection;
import TDS.Proctor.Sql.Data.TesteeRequest;
import TDS.Proctor.Sql.Data.TesteeRequests;
import TDS.Proctor.Sql.Data.Abstractions.ITesteeRequestRepository;
import TDS.Shared.Data.ColumnResultSet;
import TDS.Shared.Exceptions.ReturnStatusException;
import org.slf4j.LoggerFactory;

public class TesteeRequestRepository extends AbstractDAO implements ITesteeRequestRepository
{

private static final Logger _logger = LoggerFactory.getLogger(TesteeRequestRepository.class);

  public TesteeRequests getCurrentTesteeRequests (UUID opportunityKey, UUID sessionKey, long proctorKey, UUID browserKey) throws ReturnStatusException {
    // final String sp = "{call P_GetCurrentTesteeRequests(?, ?, ?, ?) }";
    final String sp = "BEGIN; SET NOCOUNT ON; exec  P_GetCurrentTesteeRequests ?, ?, ?, ?; end;";
    ColumnResultSet reader = null;

    TesteeRequests requests = null;
    try (SQLConnection connection = getSQLConnection ()) {
      try (PreparedStatement callstatement = connection.prepareStatement (sp)) {
        callstatement.setNString (1, sessionKey.toString ());
        callstatement.setLong (2, proctorKey);
        callstatement.setNString (3, browserKey.toString ());
        callstatement.setNString (4, opportunityKey.toString ());

        if (callstatement.execute ())
          reader = ColumnResultSet.getColumnResultSet(callstatement.getResultSet ());

        // handle sql exceptions, null result sets and result sets with columns
        // reason and status inside repository layer
        ReturnStatusException.getInstanceIfAvailable (reader, "Failed getting current Testee Requests");

        requests = new TesteeRequests ();
        loadTesteeRequests (reader, requests);

      }
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    return requests;
  }

  public TesteeRequests getApprovedTesteeRequests (UUID sessionKey, long proctorKey, UUID browserKey) throws ReturnStatusException {
    final String sp = "BEGIN; SET NOCOUNT ON; exec  P_GetApprovedTesteeRequests ?, ?, ?; end;";

    // final String sp = "{call P_GetApprovedTesteeRequests(?, ?, ?) }";
    ColumnResultSet reader = null;

    TesteeRequests requests = null;
    try (SQLConnection connection = getSQLConnection ()) {
      try (PreparedStatement callstatement = connection.prepareStatement (sp)) {
        callstatement.setNString (1, sessionKey.toString ());
        callstatement.setLong (2, proctorKey);
        callstatement.setNString (3, browserKey.toString ());

        if (callstatement.execute ())
          reader = ColumnResultSet.getColumnResultSet (callstatement.getResultSet ());

        // handle sql exceptions, null result sets and result sets with columns
        // reason and status inside repository layer
        ReturnStatusException.getInstanceIfAvailable (reader, "Failed getting approved Testee Requests");

        requests = new TesteeRequests ();
        loadTesteeRequests (reader, requests);

      }
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    return requests;
  }

  public TesteeRequest getTesteeRequestValues (UUID sessionKey, long proctorKey, UUID browserKey, UUID requestKey, boolean markFulfilled) throws ReturnStatusException {
    ColumnResultSet reader = null;
    final String sp = "BEGIN; SET NOCOUNT ON; exec  P_GetTesteeRequestValues ?, ?, ?, ?, ?; end;";
    // final String sp = "{call P_GetTesteeRequestValues(?, ?, ?, ?, ?) }";

    TesteeRequest testeeRequest = null;
    try (SQLConnection connection = getSQLConnection ()) {
      try (PreparedStatement callstatement = connection.prepareStatement (sp)) {
        callstatement.setNString (1, sessionKey.toString ());
        callstatement.setLong (2, proctorKey);
        callstatement.setNString (3, browserKey.toString ());
        callstatement.setNString (4, requestKey.toString ());
        callstatement.setBoolean (5, markFulfilled);

        if (callstatement.execute ())
          reader = ColumnResultSet.getColumnResultSet (callstatement.getResultSet ());

        // handle sql exceptions, null result sets and result sets with columns
        // reason and status inside repository layer
        ReturnStatusException.getInstanceIfAvailable (reader, "Failed getting  Testee Request values");

        // if (reader == null) {
        // _logger.error ("Invalid request or request key");
        // throw new Exception ("Invalid request or request key"); //
        // ReturnStatusException(rs);
        // }

        reader.next ();
        testeeRequest = new TesteeRequest (requestKey);
        testeeRequest.setSessionKey (sessionKey);
        testeeRequest.setOppKey (UUID.fromString (reader.getString ("opportunityKey")));
        testeeRequest.setTesteeKey (reader.getLong ("_efk_Testee"));
        testeeRequest.setTestID (reader.getString ("_efk_TestID"));
        testeeRequest.setOpportunity (reader.getInt ("Opportunity"));
        testeeRequest.setTesteeID (reader.getString ("TesteeID"));
        testeeRequest.setTesteeName (reader.getString ("TesteeName"));
        testeeRequest.setRequestType (reader.getString ("RequestType"));
        if (testeeRequest.getRequestType () == "PRINTITEM") {
          testeeRequest.setRequestValue (reader.getString ("ItemFile"));
          String itemResponse = reader.getString ("ItemResponse");
          if (itemResponse != null)
            testeeRequest.setItemResponse (itemResponse);
        } else if (testeeRequest.getRequestType () == "EMBOSSPASSAGE" || testeeRequest.getRequestType () == "EMBOSSITEM") {
          testeeRequest.setRequestValue (reader.getString ("RequestValue"));
        } else
          testeeRequest.setRequestType (reader.getString ("StimulusFile"));

        testeeRequest.setRequestDesc (reader.getString ("RequestDescription"));
        if (reader.hasColumn("ItemPosition"))
            testeeRequest.setItemPosition(reader.getInt("ItemPosition"));
        testeeRequest.setAccCode (reader.getString ("AccCode"));
        testeeRequest.setLanguage (reader.getString ("Language"));
        String requestParameters = reader.getString ("RequestParameters");
        if (requestParameters != null)
          testeeRequest.setRequestParameters (reader.getString ("RequestParameters"));
      }
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    return testeeRequest;
  }

  public void denyTesteeRequest (UUID sessionKey, long proctorKey, UUID browserKey, UUID requestKey, String reason) throws ReturnStatusException {

    final String sp = "{call P_DenyTesteeRequest(?, ?, ?, ?, ?) }";
    ColumnResultSet reader = null;

    try (SQLConnection connection = getSQLConnection ()) {
      try (CallableStatement callstatement = connection.prepareCall (sp)) {
        callstatement.setNString (1, sessionKey.toString ());
        callstatement.setLong (2, proctorKey);
        callstatement.setNString (3, browserKey.toString ());
        callstatement.setNString (4, requestKey.toString ());
        callstatement.setString (5, reason);

        // id callstatement.execute() returns false, it means no resultset, i.e.
        // error message returns back
        // meaning our request was executed successfully
        if (callstatement.execute ()) {
          reader = ColumnResultSet.getColumnResultSet (callstatement.getResultSet ());

          // handle sql exceptions, null result sets and result sets with
          // columns
          // reason and status inside repository layer
          ReturnStatusException.getInstanceIfAvailable (reader, "Failed denying testee request");

        }
      }
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
  }

  private boolean loadTesteeRequests (ColumnResultSet reader, TesteeRequests testeeRequests) throws SQLException {

    if (reader == null)
      return true;// nothing to load

    try {
      reader.setFixNulls (true);
      while (reader.next ()) {
        TesteeRequest testeeRequest = new TesteeRequest ();

        testeeRequest.setKey (UUID.fromString (reader.getString ("_Key")));

        if (reader.hasColumn ("_fk_TestOpportunity"))
          testeeRequest.setOppKey (UUID.fromString (reader.getString ("_fk_TestOpportunity")));

        testeeRequest.setSessionKey (UUID.fromString (reader.getString ("_fk_Session")));

        testeeRequest.setRequestType (reader.getString ("RequestType"));
        testeeRequest.setRequestValue (reader.getString ("RequestValue"));        
        testeeRequest.setDateSubmitted (reader.getTimestamp ("DateSubmitted"));
        Date dateFulfilled = reader.getTimestamp ("DateFulfilled");
        testeeRequest.setDateFulfilled (reader.wasNull () ? null : dateFulfilled);
        testeeRequest.setDeniedReason (reader.getString ("Denied"));
        testeeRequest.setItemPage (reader.getINT ("ItemPage"));
        testeeRequest.setItemPosition (reader.getINT ("ItemPosition"));
        testeeRequest.setRequestDesc (reader.getString ("RequestDescription"));
        testeeRequests.add (testeeRequest);
      }
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw e;
    }

    return true;
  }

}
