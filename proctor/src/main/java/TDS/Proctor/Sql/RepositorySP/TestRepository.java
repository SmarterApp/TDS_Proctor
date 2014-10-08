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
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;

import AIR.Common.DB.AbstractDAO;
import AIR.Common.DB.SQLConnection;
import AIR.Common.Utilities.TDSStringUtils;
import TDS.Proctor.Sql.Data.Segment;
import TDS.Proctor.Sql.Data.Segments;
import TDS.Proctor.Sql.Data.Test;
import TDS.Proctor.Sql.Data.Abstractions.ITestRepository;
import TDS.Proctor.Sql.Data.Accommodations.AccDepChildType;
import TDS.Proctor.Sql.Data.Accommodations.AccDepChildValue;
import TDS.Proctor.Sql.Data.Accommodations.AccDepParentType;
import TDS.Proctor.Sql.Data.Accommodations.AccDepParentTypes;
import TDS.Proctor.Sql.Data.Accommodations.AccType;
import TDS.Proctor.Sql.Data.Accommodations.AccTypes;
import TDS.Proctor.Sql.Data.Accommodations.AccValue;
import TDS.Proctor.Sql.Data.Accommodations.Accs;
import TDS.Shared.Data.ColumnResultSet;
import TDS.Shared.Exceptions.ReturnStatusException;

import org.slf4j.LoggerFactory;

public class TestRepository extends AbstractDAO implements ITestRepository
{

private static final Logger _logger = LoggerFactory.getLogger(TestRepository.class);

  public List<Test> getSelectableTests (String clientname, int sessionType, Long proctorId) throws ReturnStatusException {
    return getAllTests (clientname, true, sessionType, proctorId);
  }

  public List<Test> getAllTests (String clientname, boolean selectableOnly, int sessionType, Long proctorId) throws ReturnStatusException {
    final String sp = "{call P_GetAllTests(?) }";

    List<Test> result = new ArrayList<Test>();
    try (SQLConnection connection = getSQLConnection ()) {
      try (CallableStatement callstatement = connection.prepareCall (sp)) {
        callstatement.setString (1, clientname);
        // TODO add the session type parameter. it is not required as in SP the
        // default value is 0.
        if (callstatement.execute ()) {
          ColumnResultSet reader = ColumnResultSet.getColumnResultSet (callstatement.getResultSet ());
          ReturnStatusException.getInstanceIfAvailable (reader, "failed to get all the tests");
          reader.setFixNulls (true);
          while (reader.next ()) {
            boolean isSelectable = reader.getBoolean ("Isselectable");
            if (selectableOnly && !isSelectable)
              continue;
            Test test = new Test ();
            test.set_key (reader.getString ("TestKey"));
            test.setId (reader.getString ("TestID"));
            test.setGradeText (reader.getString ("GradeText"));
            test.setSubject (reader.getString ("Subject"));
            test.setDisplayName (reader.getString ("DisplayName"));
            test.setSortOrder (reader.getInt ("SortOrder"));
            test.setAccFamily (reader.getString ("AccommodationFamily"));
            test.setIsselectable (isSelectable);
            test.setSegmented (reader.getBoolean ("IsSegmented"));
            
            if(reader.hasColumn("Category"))
                test.setCategory(reader.getString("Category"));
            if (reader.hasColumn("SortGrade"))
                test.setSortGrade(reader.getInt("SortGrade"));
            
            
            result.add (test);
          }
        }
      }
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    return result;
  }

  public Segments getSegments (String clientname, int sessionType) throws ReturnStatusException {

    //final String sp = "{call IB_GetSegments(?) }";
    final String sp = "BEGIN; SET NOCOUNT ON; exec IB_GetSegments '{0}'; end; ";
    String sqlQuery = TDSStringUtils.format (sp, clientname);
    
    Segments segments = new Segments ();
    try (SQLConnection connection = getSQLConnection ()) {
      try (Statement callstatement = connection.createStatement ()) {
        //callstatement.setString (1, clientname);
        if (callstatement.execute (sqlQuery)) {
          ColumnResultSet reader = ColumnResultSet.getColumnResultSet (callstatement.getResultSet ());
          ReturnStatusException.getInstanceIfAvailable (reader, "Failed to get the segments");          
           while (reader.next ()) {
            Segment segment = new Segment ();
            // TestKey TestID segmentID segmentPosition SegmentLabel
            // IsPermeable entryApproval exitApproval itemReview
            segment.setTestKey (reader.getString ("TestKey"));
            segment.setId (reader.getString ("segmentID"));
            segment.setKey (TDSStringUtils.format ("{0}_{1}", segment.getTestKey (), segment.getId ())); // unique
            // segment
            // key,
            // dynamically
            // generated for lookup
            segment.setPosition (reader.getInt ("segmentPosition"));
            segment.setLabel (reader.getString ("SegmentLabel"));
            segment.setIsPermeable (reader.getInt ("IsPermeable"));
            segment.setEntryApproval (reader.getInt ("entryApproval"));
            segment.setExitApproval (reader.getInt ("exitApproval"));
            segment.setItemReview (reader.getBoolean ("itemReview"));
            segments.add (segment);
          }
        }
      }
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    return segments;
  }

  public Accs getTestAccs (String testkey) throws ReturnStatusException {

    ColumnResultSet reader = null;
    Accs accs = null;
    final String CMD_GET_TEST_ACCS = "{call IB_GetTestAccommodations(?) }";
    try (SQLConnection connection = getSQLConnection ()) {
      try (CallableStatement callstatement = connection.prepareCall (CMD_GET_TEST_ACCS)) {
        callstatement.setString (1, testkey);
        boolean exeStoredProc = callstatement.execute ();
        if (exeStoredProc) {
          accs = new Accs ();
          reader = ColumnResultSet.getColumnResultSet (callstatement.getResultSet ());
          // handle sql exceptions, null result sets and result sets with
          // columns reason and status inside repository layer
          ReturnStatusException.getInstanceIfAvailable (reader, "Failed Getting TestAccs");
          loadAccs (testkey, accs, reader, callstatement);
        }
      }
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    return accs;
  }

  public Accs getGlobalAccs (String clientname, String context) throws ReturnStatusException {

    ColumnResultSet reader = null;
    Accs accs = null;
    final String CMD_GET_GLOBAL_ACCS = "{call IB_GlobalAccommodations(?,?) }";
    try (SQLConnection connection = getSQLConnection ()) {
      try (CallableStatement callstatement = connection.prepareCall (CMD_GET_GLOBAL_ACCS)) {
        callstatement.setString (1, clientname);
        callstatement.setString (2, context);
        boolean exeStoredProc = callstatement.execute ();
        if (exeStoredProc)
          reader = ColumnResultSet.getColumnResultSet (callstatement.getResultSet ());
        // handle sql exceptions, null result sets and result sets with
        // columns reason and status inside repository layer
        ReturnStatusException.getInstanceIfAvailable (reader, "Failed Getting GlobalAccs");
        accs = new Accs ();
        loadAccs (context, accs, reader, callstatement);
      }
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    return accs;

  }

  // / <summary>
  // / Load test and segment Accs
  // / </summary>
  // / <param name="testkey"></param>
  // / <param name="accs"></param>
  // / <param name="reader"></param>
  // / <returns></returns>
  private boolean loadAccs (String testkey, Accs accs, ColumnResultSet reader, CallableStatement callstatement) throws SQLException {
    if (reader == null)
      return false;

    AccTypes accTypes;
    AccType accType = null;
    AccValue accValue;
    String strAccType, strAccValue, strAccCode;
    reader.setFixNulls (true);
    String key = "";
    boolean allowChange, isSelected, allowCombine, isSelectable, isVisible;

    String dependOnToolType = "";

    try {
      while (reader.next ()) {
        int toolTypeSortOrder = 0, toolValueSortOrder = 0;
        int segmentPosition = 0;
        if (reader.hasColumn ("Segment"))
          segmentPosition = reader.getInt ("Segment");

        if (segmentPosition < 1) // segment's acc or test's acc
          key = testkey;
        else
          key = TDSStringUtils.format ("{0}_{1}", testkey, segmentPosition);

        strAccType = reader.getString ("AccType");
        strAccValue = reader.getString ("AccValue");
        strAccCode = reader.getString ("AccCode");
        isSelected = reader.getBoolean ("IsDefault");
        allowCombine = reader.getBoolean ("allowCombine");
        allowChange = reader.getBoolean ("AllowChange");
        isSelectable = reader.getBoolean ("IsSelectable");
        isVisible = reader.getBoolean ("isVisible");
        if (reader.hasColumn ("ToolTypeSortOrder"))
          toolTypeSortOrder = Integer.parseInt (reader.getString ("ToolTypeSortOrder"));
        if (reader.hasColumn ("ToolValueSortOrder"))
          toolValueSortOrder = Integer.parseInt (reader.getString ("ToolValueSortOrder"));
        if (reader.hasColumn ("DependsOnToolType"))
          dependOnToolType = reader.getString ("DependsOnToolType"); // NOTE:
        // we
        // need
        // to
        // load
        // the
        // tool
        // dependencies
        // as
        // well.
        accTypes = accs.get (key);
        if (accTypes == null) // not already exists then add
        {
          accTypes = new AccTypes ();
          accs.add (key, accTypes);
        }

        accType = accTypes.get (strAccType);
        if (accType == null)// not already exists then add
        {
          accType = new AccType (strAccType, strAccType, allowChange, isVisible, isSelectable, toolTypeSortOrder);
          accType.setDependOnType(dependOnToolType);
          accTypes.put (strAccType, accType);
        }
        accValue = new AccValue (strAccValue, strAccCode, isSelected, allowCombine, toolValueSortOrder);
        if (allowCombine)
          accType.setAllowCombineCount(accType.getAllowCombineCount() + 1);
        accType.add (accValue);
      }

      if (callstatement.getMoreResults ()) {
        reader = ColumnResultSet.getColumnResultSet (callstatement.getResultSet ());
        while (reader.next ()) {
          key = testkey;
          accTypes = accs.get (key);
          if (accTypes == null)
            continue;

          String ifType = reader.getString ("IfType");
          accType = accTypes.get (ifType);
          if (accType == null)// if parent acc type is not exists,
            // skip the
            // dependencies
            continue;
          if (accType.getAccDepParentTypes() == null)
            accType.setAccDepParentTypes(new AccDepParentTypes ());
          String ifValue = reader.getString ("IfValue");
          AccDepParentType accDepType = accType.getAccDepParentTypes().get (ifType, ifValue);
          if (accDepType == null) {
            accDepType = new AccDepParentType (ifType, ifValue);
            accType.getAccDepParentTypes().add (accDepType);
          }
          String thenType = reader.getString ("ThenType");
          AccDepChildType accDepChildType = accDepType.getChildType (thenType);
          if (accDepChildType == null) {
            accDepChildType = new AccDepChildType (thenType);
            accDepType.getAccDepChildTypes ().add (accDepChildType);
          }
          AccDepChildValue accDepChildValue = new AccDepChildValue (reader.getString ("ThenValue"), reader.getBoolean ("IsDefault"));
          accDepChildType.getThenValues ().add (accDepChildValue);
        }
      }
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw e;
    }
    accs.sortValuesBySortOrder ();

    return true;
  }

}
