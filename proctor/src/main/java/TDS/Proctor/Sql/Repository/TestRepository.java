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
import java.util.Iterator;
import java.util.List;

import TDS.Proctor.performance.services.AppConfigService;
import TDS.Proctor.performance.services.ProctorUserService;
import org.slf4j.Logger;

import AIR.Common.DB.AbstractDAO;
import AIR.Common.DB.SQLConnection;
import AIR.Common.DB.results.DbResultRecord;
import AIR.Common.DB.results.MultiDataResultSet;
import AIR.Common.DB.results.SingleDataResultSet;
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
import TDS.Shared.Exceptions.ReturnStatusException;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import tds.dll.api.ICommonDLL;
import tds.dll.api.IProctorDLL;
import tds.dll.api.IRtsDLL;

/**
 * @author temp_ukommineni
 * 
 */

public class TestRepository extends AbstractDAO implements ITestRepository
{

  private static final Logger _logger = LoggerFactory.getLogger(TestRepository.class);
  @Autowired
  ICommonDLL _cdll    = null;
  @Autowired
  IProctorDLL _pdll    = null;
  @Autowired
  IRtsDLL _rdll        = null;

    @Autowired
    private ProctorUserService proctorUserService;

    @Autowired
    private AppConfigService appConfigService;
  

  public List<Test> getSelectableTests (String clientname, int sessionType, Long proctorId) throws ReturnStatusException {
    return getAllTests (clientname, true, sessionType,proctorId);
  }

  public List<Test> getAllTests (String clientname, boolean selectableOnly, int sessionType,Long proctorId) throws ReturnStatusException {
    List<Test> result = new ArrayList<Test> ();

    try (SQLConnection connection = getSQLConnection ()) {
      //TODO Clarify this interface. If it has to go by proctor key, pass it from handler
//      SingleDataResultSet resultset = _rdll.P_GetAllTests_SP (connection, clientname, sessionType, proctorId);
      SingleDataResultSet resultset = proctorUserService.getAllTests(clientname, sessionType, proctorId);
      ReturnStatusException.getInstanceIfAvailable (resultset, "No tests are available for this proctor");
      Iterator<DbResultRecord> records = resultset.getRecords ();
      
      resultset.setFixNulls (true);
      while (records.hasNext ()) {
        DbResultRecord record = records.next ();
        boolean isSelectable = record.<Boolean> get ("Isselectable");
        if (selectableOnly && !isSelectable)
          continue;
        Test test = new Test ();
        test.set_key (record.<String> get ("TestKey"));
        test.setId (record.<String> get ("TestID"));
        test.setGradeText (record.<String> get ("GradeText"));
        test.setSubject (record.<String> get ("Subject"));
        test.setDisplayName (record.<String> get ("DisplayName"));
        test.setSortOrder (record.<Integer> get ("SortOrder"));
        test.setAccFamily (record.<String> get ("AccommodationFamily"));
        test.setIsselectable (record.<Boolean> get ("isSelectable"));
        test.setSegmented (record.<Boolean> get ("IsSegmented"));

        if(record.hasColumn("Category"))
            test.setCategory(record.<String> get("Category"));
        if (record.hasColumn("SortGrade"))
            test.setSortGrade(record.<Integer> get("SortGrade"));
        
        result.add (test);
      }
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    return result;
  }

  public Segments getSegments (String clientname, int sessionType) throws ReturnStatusException {
    Segments result = new Segments ();
    
    try (SQLConnection connection = getSQLConnection ()) {
      MultiDataResultSet resultsets = _pdll.IB_GetSegments_SP (connection, clientname, sessionType);
      Iterator<SingleDataResultSet> results = resultsets.getResultSets ();

      SingleDataResultSet firstResultSet = results.next ();
      ReturnStatusException.getInstanceIfAvailable (firstResultSet);
      Iterator<DbResultRecord> records = firstResultSet.getRecords ();

      while (records.hasNext ()) {
        DbResultRecord record = records.next ();
        Segment segment = new Segment ();
        // TestKey TestID segmentID segmentPosition SegmentLabel
        // IsPermeable entryApproval exitApproval itemReview
        segment.setTestKey (record.<String> get ("TestKey"));
        segment.setId (record.<String> get ("segmentID"));
        // unique segment
        segment.setKey (String.format ("%s_%s", segment.getTestKey (), segment.getId ())); 
        // key, dynamically generated for lookup
        segment.setPosition (record.<Integer> get ("segmentPosition"));
        segment.setLabel (record.<String> get ("SegmentLabel"));
        segment.setIsPermeable (record.<Integer> get ("IsPermeable"));
        segment.setEntryApproval (record.<Integer> get ("entryApproval"));
        segment.setExitApproval (record.<Integer> get ("exitApproval"));
        segment.setItemReview (record.<Boolean> get ("itemReview"));
        result.add (segment);
      }
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    return result;
  }

  public Accs getTestAccs (String testkey) throws ReturnStatusException {
    Accs accs = null;
    try (SQLConnection connection = getSQLConnection ()) {
      MultiDataResultSet resultsets = _cdll.IB_GetTestAccommodations_SP (connection, testkey);

      Iterator<SingleDataResultSet> results = resultsets.getResultSets ();
      SingleDataResultSet firstResultSet = results.next ();

      ReturnStatusException.getInstanceIfAvailable (firstResultSet);
      accs = new Accs ();
      loadAccs (testkey, accs, resultsets);

    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }

    return accs;
  }

  public Accs getGlobalAccs (String clientname, String context) throws ReturnStatusException {
    Accs accs = null;
    try {
      //MultiDataResultSet resultsets = _pdll.IB_GlobalAccommodations_SP (connection, clientname, context);
      MultiDataResultSet resultsets = appConfigService.getGlobalAccommodations(clientname, context);

      Iterator<SingleDataResultSet> results = resultsets.getResultSets ();
      SingleDataResultSet firstResultSet = results.next ();

      ReturnStatusException.getInstanceIfAvailable (firstResultSet);
      accs = new Accs ();
      loadAccs (context, accs, resultsets);

    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    return accs;

  }

  /** Load test and segment Accs
  * 
  * <param name="testkey"></param>
  * <param name="accs"></param>
  * <param name="reader"></param>
  */

  private boolean loadAccs (String testkey, Accs accs, MultiDataResultSet resultsets) throws SQLException {

    Iterator<SingleDataResultSet> results = resultsets.getResultSets ();
    SingleDataResultSet firstResultSet = results.next ();

    if (firstResultSet == null)
      return false;

    if (firstResultSet != null) {
      Iterator<DbResultRecord> records = firstResultSet.getRecords ();

      AccTypes accTypes;
      AccType accType = null;
      AccValue accValue;
      String strAccType, strAccValue, strAccCode;
      firstResultSet.setFixNulls (true);
      String key = "";
      boolean allowChange, isSelected, allowCombine, isSelectable, isVisible;
      String dependOnToolType = "";

      while (records.hasNext ()) {
	  DbResultRecord record = records.next ();
	  int toolTypeSortOrder = 0, toolValueSortOrder = 0;
	  int segmentPosition = 0;
	  if (record.hasColumn ("Segment"))
	    segmentPosition = record.<Integer> get ("Segment");

	  if (segmentPosition < 1) // segment's acc or test's acc
	    key = testkey;
	  else
	    key = String.format ("%s_%d", testkey, segmentPosition);

	  strAccType = record.<String> get ("AccType");
	  strAccValue = record.<String> get ("AccValue");
	  strAccCode = record.<String> get ("AccCode");
	  isSelected = record.<Boolean> get ("IsDefault");
	  allowCombine = record.<Boolean> get ("allowCombine");
	  allowChange = record.<Boolean> get ("AllowChange");
	  isSelectable = record.<Boolean> get ("IsSelectable");
	  isVisible = record.<Boolean> get ("isVisible");
	  if (record.hasColumn ("ToolTypeSortOrder"))
	    toolTypeSortOrder = record.<Integer> get ("ToolTypeSortOrder");
	  if (record.hasColumn ("ToolValueSortOrder"))
	    toolValueSortOrder = record.<Integer> get ("ToolValueSortOrder");
	  if (record.hasColumn ("DependsOnToolType"))
	    dependOnToolType = record.<String> get ("DependsOnToolType"); // NOTE:
	  // we need to load the tool dependencies as well.
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
	    accType.setDependOnType (dependOnToolType);
	    accTypes.put (strAccType, accType);
	  }
	  accValue = new AccValue (strAccValue, strAccCode, isSelected, allowCombine, toolValueSortOrder);
	  if (allowCombine)
	    accType.setAllowCombineCount (accType.getAllowCombineCount () + 1);
	  accType.add (accValue);
	}

	if (results.hasNext ()) {
	  SingleDataResultSet secondResultSet = results.next ();
	  if (secondResultSet != null)
	    records = secondResultSet.getRecords ();
	  if (records != null) {
	    while (records.hasNext ()) {
	      DbResultRecord record = records.next ();
	      String contextType = record.<String> get ("ContextType");
	      String context = record.<String> get ("Context");
	      key = testkey;
	      accTypes = accs.get (key);
	      if (accTypes == null)
	        continue;

	      String ifType = record.<String> get ("IfType");
	      accType = accTypes.get (ifType);
	      if (accType == null)// if parent acc type is not exists,
	        // skip the dependencies
	        continue;
	      if (accType.getAccDepParentTypes () == null)
	        accType.setAccDepParentTypes (new AccDepParentTypes ());
	      String ifValue = record.<String> get ("IfValue");
	      AccDepParentType accDepType = accType.getAccDepParentTypes ().get (ifType, ifValue);
	      if (accDepType == null) {
	        accDepType = new AccDepParentType (ifType, ifValue);
	        accType.getAccDepParentTypes ().add (accDepType);
	      }
	      String thenType = record.<String> get ("ThenType");
	      AccDepChildType accDepChildType = accDepType.getChildType (thenType);
	      if (accDepChildType == null) {
	        accDepChildType = new AccDepChildType (thenType);
	        accDepType.getAccDepChildTypes ().add (accDepChildType);
	      }
	      AccDepChildValue accDepChildValue = new AccDepChildValue (record.<String> get ("ThenValue"), record.<Boolean> get ("IsDefault"));
	      accDepChildType.getThenValues ().add (accDepChildValue);
	    }
	  }
	}
      accs.sortValuesBySortOrder ();
    }

    return true;
  }
}
