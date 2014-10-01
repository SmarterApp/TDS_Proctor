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
package TDS.Proctor.Services.UnitTests;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import AIR.Common.Configuration.AppSettingsHelper;
import AIR.Common.DB.AbstractConnectionManager;
import AIR.Common.DB.SQLConnection;
import AIR.Common.Utilities.TDSStringUtils;
import TDS.Proctor.Services.UnitTests.student.sql.data.OpportunityInfo;
import TDS.Proctor.Services.UnitTests.student.sql.data.TestSelection;
import TDS.Proctor.Services.UnitTests.student.sql.data.TestSelectionList;
import TDS.Proctor.Services.UnitTests.student.sql.data.TesteeAttribute;
import TDS.Proctor.Services.UnitTests.student.sql.data.TesteeAttributes;
import TDS.Proctor.Sql.Data.TestSession;
import TDS.Shared.Data.ColumnResultSet;
import TDS.Shared.Data.ReturnStatus;
import TDS.Shared.Exceptions.ReturnStatusException;

/**
 * @author temp_rreddy
 * 
 */
public class StudentSimulator
{
  private String                    _clientName            = null;
  private TestSession               _testSession           = null;
  private UUID                      _oppbrowserKey         = UUID.randomUUID ();

  // Start attributes to be filled in as part of simulation
  private TesteeAttributes          _testeeAttributes      = null;
  private long                      _entityKey             = -1;
  private TestSelectionList         _eligibleTestsListData = null;
  private String                    _studentGrade          = null;
  private String                    _idKeyValues           = null;
  private OpportunityInfo           _oppInfo               = null;

  // private ApprovalInfo oppApprovalInfo = null;

  // End attributes to be filled in as part of simulation
  private static final Logger       _logger                = LoggerFactory.getLogger (StudentSimulator.class);

  @Autowired
  private AbstractConnectionManager _connectionManager;

  public StudentSimulator (String clientName, TestSession session, String keyValues) {
    this._clientName = clientName;
    this._testSession = session;
    this._idKeyValues = keyValues;
  }

  public long getEntityKey () {
    return _entityKey;
  }

  public TesteeAttributes studentLogin () throws ReturnStatusException {

    ResultSet reader = null;
    _testeeAttributes = new TesteeAttributes ();

    final String cmd = "BEGIN; SET NOCOUNT ON; exec T_Login '{0}', '{1}', '{2}'; end;";
    String sqlQuery = TDSStringUtils.format (cmd, _clientName, _idKeyValues, _testSession.getId ());

    try (SQLConnection connection = _connectionManager.getConnection ()) {
      try (Statement callstatement = connection.createStatement ()) {
        callstatement.execute (sqlQuery);
        reader = callstatement.getResultSet ();
        if (reader != null) {
          while (reader.next ()) {
            _entityKey = reader.getLong ("entityKey");
          }

          if (callstatement.getMoreResults ()) {
            reader = callstatement.getResultSet ();
            while (reader.next ()) {
              TesteeAttribute testeeAttribute = new TesteeAttribute ();
              testeeAttribute.setTdsId (reader.getString ("TDS_ID"));
              testeeAttribute.setValue (reader.getString ("value"));
              testeeAttribute.setLabel (reader.getString ("label"));
              testeeAttribute.setSortOrder (reader.getString ("sortOrder"));
              testeeAttribute.setAtLogin (reader.getString ("atLogin"));
              _testeeAttributes.add (testeeAttribute);
            }

          }
        }

      }
    } catch (SQLException e) {
      _logger.error ("ERROR:" + e.getMessage ());
      ReturnStatus rs = new ReturnStatus ("failed", e.getMessage ());
      throw new ReturnStatusException (rs);
    }
    setGrade (_testeeAttributes);
    return _testeeAttributes;
  }

  public TestSelectionList getEligibleTests () throws ReturnStatusException {

    _eligibleTestsListData = new TestSelectionList ();

    ResultSet reader = null;
    String CMD_GET_ELIGIBLE = "{call T_GetEligibleTests(?, ?, ?) }";
    try (SQLConnection connection = _connectionManager.getConnection ()) {
      try (CallableStatement callstatement = connection.prepareCall (CMD_GET_ELIGIBLE)) {
        callstatement.setLong (1, _entityKey);
        callstatement.setNString (2, _testSession.getKey ().toString ());
        callstatement.setString (3, _studentGrade);

        callstatement.execute ();
        reader = callstatement.getResultSet ();

        if (reader != null) {
          while (reader.next ()) {
            TestSelection eligibleTestsData = new TestSelection ();
            eligibleTestsData.setTestKey (reader.getString ("testKey"));
            eligibleTestsData.setTestID (reader.getString ("test"));
            eligibleTestsData.setOpportunity (reader.getInt ("opportunity"));
            _eligibleTestsListData.add (eligibleTestsData);
          }
        }
      }

    } catch (SQLException e) { // TODO Auto-generated catch block
      _logger.error ("ERROR:" + e.getMessage ());
      ReturnStatus rs = new ReturnStatus ("failed", e.getMessage ());
      throw new ReturnStatusException (rs);
    }
    return _eligibleTestsListData;
  }

  public boolean openRandomTestOpportunity () throws ReturnStatusException {
    // we will pick the first eligible test.
    for (TestSelection test : _eligibleTestsListData) {
      if (isEligibleTest (test)) {
        openTestOpportunity (test.getTestKey ());
        return true;
      }
    }
    return false;
  }

  public boolean validateAccessTask () {
    return false;
  }

  // private TDSSqlResult<OpportunityStatus> validateAccessSP () throws
  // ReturnStatusException {
  //
  // AbstractConnectionManager _connectionManager = ResourceManager.getInstance
  // ().<AbstractConnectionManager> getResource
  // (AbstractConnectionManager.class.getName ());
  // TDSSqlResult<OpportunityStatus> tdsOpportunityStatus = null;
  // ColumnResultSet reader = null;
  // String CMD_INSERT_SESSIONTEST = "{call T_ValidateAccess(?, ?, ?) }";
  // try (SQLConnection connection = _connectionManager.getConnection ()) {
  // try (CallableStatement callstatement = connection.prepareCall
  // (CMD_INSERT_SESSIONTEST)) {
  // callstatement.setNString (1, _oppInfo._oppKey.toString ());
  // callstatement.setNString (2, _testSession.getKey ().toString ());
  // callstatement.setNString (3, _oppbrowserKey.toString ());
  //
  // callstatement.execute ();
  //
  // reader = ColumnResultSet.getColumnResultSet (callstatement.getResultSet
  // ());
  //
  // if (reader != null) {
  // if (reader.hasColumn ("oppStatus")) {
  // tdsOpportunityStatus._value.setStatus (OpportunityStatusType.valueOf
  // (reader.getString ("oppStatus")));
  // if (!reader.hasColumn ("comment") && reader.getString ("comment") != null)
  // tdsOpportunityStatus._value.setComment (reader.getString ("comment"));
  // } else {
  // tdsOpportunityStatus.setReturnStatus (ReturnStatus.parse (reader));
  // }
  // }
  // }
  // } catch (SQLException e) {
  // _logger.error ("ERROR:" + e.getMessage ());
  // ReturnStatus rs = new ReturnStatus ("failed", e.getMessage ());
  // throw new ReturnStatusException (rs);
  // }
  // return tdsOpportunityStatus;
  // }

  private OpportunityInfo openTestOpportunity (String testKey) throws ReturnStatusException {

    ResultSet reader = null;
    ColumnResultSet columnResultSet = null;
    String CMD_INSERT_SESSIONTEST = "{call T_OpenTestOpportunity(?, ?, ?, ?) }";
    try (SQLConnection connection = _connectionManager.getConnection ()) {
      try (CallableStatement callstatement = connection.prepareCall (CMD_INSERT_SESSIONTEST)) {
        callstatement.setLong (1, _entityKey);
        callstatement.setString (2, testKey);
        callstatement.setNString (3, _testSession.getKey ().toString ());
        callstatement.setNString (4, _oppbrowserKey.toString ());

        if (callstatement.execute ())
          columnResultSet = ColumnResultSet.getColumnResultSet (callstatement.getResultSet ());
        ReturnStatus returnStatus = ReturnStatus.parse (columnResultSet);
        if (returnStatus == null) {
          ReturnStatus rs = new ReturnStatus ("failed", "Invalid rows returned from T_OpenTestOpportunity");
          throw new ReturnStatusException (rs);
        }
        _oppInfo = new OpportunityInfo ();

        if (columnResultSet != null) {
          if (columnResultSet.hasColumn ("oppkey")) {
            _oppInfo.setBrowserKey (_oppbrowserKey);
            _oppInfo.setOppKey (UUID.fromString (columnResultSet.getNString ("oppkey")));
          }
        }
      }

    } catch (SQLException e) {
      _logger.error ("ERROR:" + e.getMessage ());
      ReturnStatus rs = new ReturnStatus ("failed", e.getMessage ());
      throw new ReturnStatusException (rs);
    }
    return _oppInfo;
  }

  private void setGrade (TesteeAttributes testeeAttribute) {
    if (testeeAttribute != null)
      for (int i = 0; i < testeeAttribute.size (); i++) {
        TesteeAttribute testeeAttributeValues = testeeAttribute.get (i);
        if (testeeAttributeValues.getTdsId ().equalsIgnoreCase ("Grade")) {
          _studentGrade = testeeAttributeValues.getValue ();
        }
      }
  }

  private boolean isEligibleTest (TestSelection testSelection) {
    // skip disabled tests
    if (StringUtils.equalsIgnoreCase (testSelection.getTestStatus (), TestSelection.Status.Disabled.toString ())) {
      _logger.warn (TDSStringUtils.format ("STUDENT: Cannot open test \"Grade {0} {1}\": {2} ({3})", _studentGrade, testSelection.getDisplayName (), testSelection.getReason (),
          testSelection.getTestStatus ()));
      return false;
    }

    // check if any opportunities (NOTE: I don't think this will ever occur
    // because test should be disabled)
    if (testSelection.getOpportunity () == 0) {
      _logger.warn (TDSStringUtils.format ("STUDENT: No more opportunities for test \"Grade {0} {1}\": {2} ({3})", _studentGrade, testSelection.getDisplayName (), testSelection.getReason (),
          testSelection.getTestStatus ()));
      return false;
    }

    // check if we have reached our max opps
    if (testSelection.getOpportunity () > AppSettingsHelper.getInt32 ("StudentMaxOpps", 1))
      return false;
    return true;
  }
}
