package TDS.Proctor.performance.services.impl;

import AIR.Common.DB.*;
import AIR.Common.DB.results.DbResultRecord;
import AIR.Common.DB.results.MultiDataResultSet;
import AIR.Common.DB.results.SingleDataResultSet;
import AIR.Common.Helpers.CaseInsensitiveMap;
import AIR.Common.Helpers._Ref;
import TDS.Proctor.performance.dao.ItemBankDao;
import TDS.Proctor.performance.dao.ProctorUserDao;
import TDS.Proctor.performance.dao.TestAccommodationDao;
import TDS.Proctor.performance.dao.TestSessionDao;
import TDS.Proctor.performance.domain.TestAccommodationFamily;
import TDS.Proctor.performance.services.TestSessionService;
import TDS.Shared.Exceptions.ReturnStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tds.dll.api.ICommonDLL;
import tds.dll.common.performance.domain.Externs;
import tds.dll.common.performance.services.ConfigurationService;
import tds.dll.common.performance.services.DbLatencyService;
import tds.dll.common.performance.utils.DateUtility;
import tds.dll.common.performance.utils.HostNameHelper;
import tds.dll.common.performance.utils.LegacyComparer;
import tds.dll.common.performance.utils.LegacySqlConnection;
import tds.dll.common.rtspackage.student.data.AccommodationOther;

import java.sql.SQLException;
import java.util.*;

@Service
public class TestSessionServiceImpl extends AbstractDLL implements TestSessionService {
    private static final Logger logger = LoggerFactory.getLogger(TestApprovalServiceImpl.class);

    @Autowired
    private TestSessionDao testSessionDao;

    @Autowired
    private ConfigurationService configurationService;

    @Autowired
    LegacySqlConnection legacySqlConnection;

    @Autowired
    private DateUtility dateUtility;

    @Autowired
    private DbLatencyService dbLatencyService;

    @Autowired
    private ProctorUserDao proctorUserDao;

    @Autowired
    private ICommonDLL commonDll;

    @Autowired
    private ItemBankDao itemBankDao;

    @Autowired
    private TestAccommodationDao testAccommodationDao;

    /**
     * Replaces ProctorDll.P_CreateSession_SP
     *
     * @param clientName
     * @param browserKey
     * @param sessionName
     * @param proctorKey
     * @param procId
     * @param procName
     * @param dateBegin
     * @param dateEnd
     * @param sessionType
     * @return
     * @throws ReturnStatusException
     */
    public SingleDataResultSet createSession(String clientName, UUID browserKey, String sessionName, Long proctorKey, String procId, String procName, Date dateBegin,
                                                   Date dateEnd, Integer sessionType) throws ReturnStatusException {

        try (SQLConnection connection = legacySqlConnection.get()) {
            SingleDataResultSet result = null;
            UUID sessionKey = null;
            _Ref<String> sessionId = new _Ref<> ();
            String environment = null;
            String prefix = null;
            String status = "closed";
            String errMsg = null;

            Date dbLatencyTime = dateUtility.getLocalDate();
            Date now = dateUtility.getDbDate();

            // CommonDLL.AuditSessions checks to see if the "sessions" flag is on or not
            Integer audit = configurationService.isFlagOn(clientName, "sessions") ? 1 : 0;

            //final String SQL_QUERY1 = "select environment from _externs where clientname = ${clientname};";
            Externs externs = configurationService.getExterns(clientName);
            if (externs != null) {
                environment = externs.getEnvironment();
            }
            if (environment == null) {
                errMsg = String.format ("Unknown client: %s", clientName);
                return commonDll._ReturnError_SP (connection, clientName, "P_CreateSession", errMsg);
            }

            final String SQL_QUERY2 = "select _Key from session S where clientname = ${clientname} and _efk_Proctor = ${proctorKey} and ${now} between S.DateBegin and S.DateEnd and sessiontype = ${sessiontype} limit 1 ";
            SqlParametersMaps parms2 = (new SqlParametersMaps ()).put ("clientname", clientName).put ("proctorKey", proctorKey).put ("now", now).put ("sessiontype", sessionType);
            if (exists (executeStatement (connection, SQL_QUERY2, parms2, true))) {
                return commonDll._ReturnError_SP (connection, clientName, "P_CreateSession", "There already is an active session for this user.");
            }
            prefix = commonDll._CoreSessName_FN (connection, clientName, procName);
            sessionKey = UUID.randomUUID();
            if (dateBegin == null) {
                dateBegin = now;
            }
            if (dateEnd == null) {
                dateEnd = dateUtility.addHours(dateBegin, 8);
            } else if (LegacyComparer.lessOrEqual (dateEnd, dateBegin)) {
                dateEnd = dateUtility.addHours(dateBegin, 8);
            }
            if ((now.equals (dateBegin) || now.after (dateBegin)) && (now.equals (dateEnd) || now.before (dateEnd))) {
                status = "open";
            }
            commonDll._CreateClientSessionID_SP (connection, clientName, prefix, sessionId);
            if (sessionId.get () == null) {
                return commonDll._ReturnError_SP (connection, clientName, "P_CreateSession", "Failed to insert new session into database");
            }
            try {
                final String SQL_INSERT = "insert into session (_Key, Name, _efk_Proctor, ProctorID, ProctorName, status, DateBegin, DateEnd, SessionID, _fk_browser, clientname, environment, dateVisited, sessiontype, datecreated, serveraddress) "
                        + " values (${sessionKey}, ${sessionName}, ${proctorKey}, ${procID}, ${procName}, ${status}, ${dateBegin}, ${dateEnd}, ${sessionID}, ${browserKey}, ${clientname}, ${environment}, ${now}, ${sessiontype}, now(3), ${hostname});";
                SqlParametersMaps parms3 = new SqlParametersMaps ();
                parms3.put ("sessionKey", sessionKey);
                parms3.put ("sessionName", sessionName);
                parms3.put ("proctorKey", proctorKey);
                parms3.put ("procID", procId);
                parms3.put ("procName", procName);
                parms3.put ("status", status);
                parms3.put ("dateBegin", dateBegin);
                parms3.put ("dateEnd", dateEnd);
                parms3.put ("sessionID", sessionId.toString ());
                parms3.put ("browserKey", browserKey);
                parms3.put ("clientname", clientName);
                parms3.put ("environment", environment);
                parms3.put ("now", now);
                parms3.put ("sessiontype", sessionType);
                parms3.put ("hostname", HostNameHelper.getHostName());
                executeStatement (connection, SQL_INSERT, parms3, false).getUpdateCount ();

            } catch (Exception e) {
                String err = e.getMessage ();
                commonDll._LogDBError_SP (connection, "P_CreateSession", err, null, null, null, null, clientName, null);
                dbLatencyService.logLatency("P_CreateSession", dbLatencyTime, proctorKey, null, null, null, clientName, null);
                return commonDll._ReturnError_SP (connection, clientName, "P_CreateSession", "Failed to insert new session into database");
            }
            String localhostname = HostNameHelper.getHostName();

            String sessionDB = getTdsSettings ().getTDSSessionDBName ();
            // String sessionDB = getAppSettings ().get ("TDSSessionDBName");
            if (DbComparator.notEqual (audit, 0)) {
                final String SQL_INSERT1 = "insert into ${ArchiveDB}.sessionaudit (_fk_session, DateAccessed, AccessType, hostname, browserKey, dbname) values (${sessionKey}, ${now}, ${status}, ${hostname}, ${browserKey}, ${dbname});";
                SqlParametersMaps parms4 = new SqlParametersMaps ().put ("sessionKey", sessionKey).put ("now", now).put ("status", status).put ("hostname", localhostname).put ("browserKey", browserKey)
                        .put ("dbname", sessionDB);
                executeStatement (connection, fixDataBaseNames (SQL_INSERT1), parms4, false).getUpdateCount ();
            }
            List<CaseInsensitiveMap<Object>> resultlist = new ArrayList<CaseInsensitiveMap<Object>>();
            CaseInsensitiveMap<Object> rcrd = new CaseInsensitiveMap<Object> ();
            rcrd.put ("sessionKey", sessionKey);
            rcrd.put ("sessionID", sessionId.get ());
            rcrd.put ("Name", sessionName);
            rcrd.put ("sessionStatus", status);
            resultlist.add (rcrd);

            result = new SingleDataResultSet ();
            result.addColumn ("sessionKey", SQL_TYPE_To_JAVA_TYPE.UNIQUEIDENTIFIER);
            result.addColumn ("sessionID", SQL_TYPE_To_JAVA_TYPE.VARCHAR);
            result.addColumn ("Name", SQL_TYPE_To_JAVA_TYPE.VARCHAR);
            result.addColumn ("sessionStatus", SQL_TYPE_To_JAVA_TYPE.VARCHAR);
            result.addRecords (resultlist);

            dbLatencyService.logLatency("P_CreateSession", now, proctorKey, null, null, sessionKey, clientName, null);
            return result;
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
            throw new ReturnStatusException(e);
        }
    }

    /**
     * Replaces ProctorDLL.P_InsertSessionTest_SP
     * @param sessionKey
     * @param proctorKey
     * @param browserKey
     * @param testKey
     * @param testId
     * @return
     * @throws ReturnStatusException
     */
    public SingleDataResultSet insertSessionTest(UUID sessionKey, Long proctorKey, UUID browserKey, String testKey, String testId) throws ReturnStatusException {

        Date dbLatencyTime = dateUtility.getLocalDate();
        SingleDataResultSet result = null;

        try (SQLConnection connection = legacySqlConnection.get()) {
            String accessDenied = proctorUserDao.validateProctorSession(proctorKey, sessionKey, browserKey);

            if (accessDenied != null) {
                String client = testSessionDao.getClientName(sessionKey);
                commonDll._LogDBError_SP (connection, "P_InsertSessionTest", accessDenied, proctorKey, null, null, sessionKey);
                dbLatencyService.logLatency("P_InsertSessionTest", dbLatencyTime, proctorKey, null, null, sessionKey, null, null);
                return commonDll._ReturnError_SP (connection, client, "P_InsertSessionTest", accessDenied, null, null, "ValidateProctorSession");
            }
            final String SQL_QUERY1 = "select _fk_Session from sessiontests where _fk_Session = ${sessionKey} and _efk_AdminSubject = ${testKey} limit 1";
            SqlParametersMaps parms1 = new SqlParametersMaps ().put ("sessionKey", sessionKey).put ("testKey", testKey);
            if (exists (executeStatement (connection, SQL_QUERY1, parms1, false))) {
                return commonDll._ReturnError_SP (connection, null, "P_InsertSessionTest", "SessionTestExists");
            }
            final String SQL_INSERT = "INSERT INTO sessiontests (_efk_AdminSubject, _efk_TestID, _fk_Session) VALUES (${testKey}, ${testID}, ${sessionKey});";
            SqlParametersMaps parms2 = new SqlParametersMaps ().put ("sessionKey", sessionKey).put ("testID", testId).put ("testKey", testKey);
            executeStatement (connection, SQL_INSERT, parms2, false).getUpdateCount ();

            result = commonDll.ReturnStatusReason ("success", null);

            dbLatencyService.logLatency("P_InsertSessionTest", dbLatencyTime, null, null, null, sessionKey, null, null);
            return result;
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
            throw new ReturnStatusException(e);
        }
    }

    /**
     * Replaces ProctorDLL.P_GetSessionTests_SP
     * @param sessionKey
     * @param proctorKey
     * @param browserKey
     * @return
     * @throws ReturnStatusException
     */
    public SingleDataResultSet getSessionTests(UUID sessionKey, Long proctorKey, UUID browserKey) throws ReturnStatusException {
        Date dbLatencyTime = dateUtility.getLocalDate();

        try (SQLConnection connection = legacySqlConnection.get()) {
            // TODO: see if this can be moved outside where the API call originates
            String accessDenied = proctorUserDao.validateProctorSession(proctorKey, sessionKey, browserKey);

            if (accessDenied != null) {
                String client = testSessionDao.getClientName(sessionKey);
                commonDll._LogDBError_SP (connection, "P_GetSessionTests", accessDenied, proctorKey, null, null, sessionKey);
                dbLatencyService.logLatency("P_GetSessionTests", dbLatencyTime, proctorKey, null, null, sessionKey, null,null);
                return commonDll._ReturnError_SP (connection, client, "P_GetSessionTests", accessDenied, null, null, "ValidateProctorSession");
            }
            final String SQL_QUERY = "SELECT _efk_TestID AS TestID, _efk_AdminSubject as TestKey  FROM sessiontests WHERE _fk_Session = ${sessionKey}";
            SqlParametersMaps parms = new SqlParametersMaps ().put ("sessionKey", sessionKey);
            SingleDataResultSet result = executeStatement (connection, SQL_QUERY, parms, true).getResultSets ().next ();

            dbLatencyService.logLatency("P_GetSessionTests", dbLatencyTime, proctorKey, null, null, sessionKey, null, null);
            return result;
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
            throw new ReturnStatusException(e);
        }


    }

    /**
     * Replaces ProctorDLL.P_ApproveAccommodations_SP
     * Note: Proctor validation was moved outside of this method and moved to the API endpoint /ApproveOpportunity
     * @param sessionKey
     * @param proctorKey
     * @param browserKey
     * @param opportunityKey
     * @param segment
     * @param segmentAccoms
     * @return
     * @throws ReturnStatusException
     */
    public SingleDataResultSet approveAccommodations(SQLConnection connection, UUID sessionKey, Long proctorKey, UUID browserKey, UUID opportunityKey, Integer segment, String segmentAccoms)
            throws ReturnStatusException {

        UUID oppsession = null;
        String teststatus = null;
        Integer numitems = null;
        _Ref<String> error = new _Ref<> ();
        String clientName = null;
        Date dbLatencyTime = dateUtility.getLocalDate();

        // ProctorValidation has ben moved to the API endpoint /ApproveOpportunity

        final String SQL_QUERY1 = "SELECT _fk_Session as oppsession, status as teststatus, maxitems as numitems, clientname from testopportunity O where O._Key = ${opportunitykey} ;";
        SqlParametersMaps parms1 = new SqlParametersMaps ().put ("opportunitykey", opportunityKey);
        SingleDataResultSet result = executeStatement (connection, SQL_QUERY1, parms1, true).getResultSets ().next ();
        DbResultRecord record = result.getCount () > 0 ? result.getRecords ().next () : null;
        if (record != null) {
            oppsession = record.<UUID> get ("oppsession");
            teststatus = record.<String> get ("teststatus");
            numitems = record.<Integer> get ("numitems");
            clientName = record.<String> get ("clientname");
        }

        if (teststatus == null) {
            error.set ("The test opportunity does not exist");
        }
        if (DbComparator.notEqual ("pending", teststatus) && DbComparator.notEqual ("suspended", teststatus) && DbComparator.notEqual ("segmentEntry", teststatus)
                && DbComparator.notEqual ("segmentExit", teststatus)) {
            error.set ("The test opportunity is not pending approval");
        }
        if (sessionKey != null && oppsession != null && DbComparator.notEqual (sessionKey, oppsession)) {
            error.set ("The test opportunity is not enrolled in this session");
        }
        if (error.get () != null) {
            commonDll._LogDBError_SP (connection, "P_ApproveAccommodations", error.get (), proctorKey, null, null, opportunityKey, null, sessionKey);
            dbLatencyService.logLatency ("P_ApproveAccommodations", dbLatencyTime, proctorKey, 0, null, sessionKey, null, null);
            return commonDll._ReturnError_SP (connection, clientName, "P_ApproveAccommodations", error.get (), null, opportunityKey, null);
        }
        try {
            updateOpportunityAccommodations(connection, opportunityKey, segment, segmentAccoms, numitems, error, 0);
            if (error.get () != null) {
                // we are having trouble with deadlocks on _Update so try one more time
                error.set (String.format ("Accommodations update failed. Making second attempt. %s", error.get ()));
                commonDll._LogDBError_SP (connection, "P_ApproveAccommodations", error.get (), proctorKey, null, null, opportunityKey, null, sessionKey);
                error.set (null);
                updateOpportunityAccommodations(connection, opportunityKey, segment, segmentAccoms, numitems,  error, 0);
                if (error.get () != null) {
                    commonDll._LogDBError_SP (connection, "P_ApproveAccommodations", error.get (), proctorKey, null, null, opportunityKey, null, sessionKey);
                    dbLatencyService.logLatency("P_ApproveAccommodations", dbLatencyTime, proctorKey, 0, null, sessionKey, null, null);
                    return commonDll._ReturnError_SP (connection, clientName, "P_ApproveAccommodations", error.get (), null, opportunityKey, null);
                }
            }
        } catch (Exception e) {
            String msg = e.getMessage ();
            commonDll._LogDBError_SP (connection, "P_ApproveAccommodations", msg, proctorKey, null, null, opportunityKey, null, sessionKey);
            dbLatencyService.logLatency("P_ApproveAccommodations", dbLatencyTime, proctorKey, 0, null, sessionKey, null, null);
            return commonDll._ReturnError_SP (connection, clientName, "P_ApproveAccommodations", "Accommodations update failed", null, opportunityKey, null);
        }

        dbLatencyService.logLatency("P_ApproveAccommodations", dbLatencyTime, proctorKey, 0, null, sessionKey, null, null);
        return null;
    }

    /**
     * Replaces P_ApproveOpportunity_SP
     * Note: Proctor validation was moved outside of this method and moved to the API endpoint /ApproveOpportunity
     * @param sessionKey
     * @param proctorKey
     * @param opportunityKey
     * @return
     * @throws ReturnStatusException
     */
    public SingleDataResultSet approveOpportunity(SQLConnection connection, UUID sessionKey, Long proctorKey, UUID opportunityKey) throws ReturnStatusException {

        UUID oppsession = null;
        String teststatus = null;
        Integer numitems = null;
        _Ref<String> error = new _Ref<> ();
        String clientName = null;
        Date dbLatencyTime = dateUtility.getLocalDate();

        final String SQL_QUERY1 = "SELECT _fk_Session as oppsession, status as teststatus, maxitems as numitems, clientname from testopportunity  where _Key = ${opportunitykey} ;";
        SqlParametersMaps parms1 = new SqlParametersMaps ().put ("opportunitykey", opportunityKey);
        SingleDataResultSet result = executeStatement (connection, SQL_QUERY1, parms1, true).getResultSets ().next ();
        DbResultRecord record = result.getCount () > 0 ? result.getRecords ().next () : null;
        if (record != null) {
            oppsession = record.<UUID> get ("oppsession");
            teststatus = record.<String> get ("teststatus");
            numitems = record.<Integer> get ("numitems");
            clientName = record.<String> get ("clientname");
        }

        if (teststatus == null) {
            error.set ("The test opportunity does not exist");
        }
        if (DbComparator.notEqual ("pending", teststatus) && DbComparator.notEqual ("suspended", teststatus) && DbComparator.notEqual ("segmentEntry", teststatus)
                && DbComparator.notEqual ("segmentExit", teststatus)) {
            error.set ("The test opportunity is not pending approval");
        }
        if (sessionKey != null && oppsession != null && DbComparator.notEqual (sessionKey, oppsession)) {
            error.set ("The test opportunity is not enrolled in this session");
        }
        if (error.get () != null) {
            commonDll._LogDBError_SP(connection, "P_ApproveOpportunity", error.get (), proctorKey, null, null, sessionKey, null, null);
            dbLatencyService.logLatency("P_ApproveOpportunity", dbLatencyTime, proctorKey, 0, null, sessionKey, null, null);
            return commonDll._ReturnError_SP (connection, clientName, "P_ApproveOpportunity", error.get (), null, opportunityKey, null);
        }

        result = commonDll.SetOpportunityStatus_SP(connection, opportunityKey, "approved", false, sessionKey.toString ());

        dbLatencyService.logLatency("P_ApproveOpportunity", dbLatencyTime, proctorKey, 0, null, sessionKey, null, null);
        return result;
    }

    /**
     * Replaces CommonDLL._UpdateOpportunityAccommodations_SP
     * @param oppKey
     * @param segment
     * @param accoms
     * @param isStarted
     * @param error
     * @param debug
     * @return
     * @throws ReturnStatusException
     */
    private MultiDataResultSet updateOpportunityAccommodations(SQLConnection connection, UUID oppKey, int segment, String accoms, int isStarted,
                                                                   _Ref<String> error, int debug) throws ReturnStatusException {

        List<SingleDataResultSet> resultsets = new ArrayList<SingleDataResultSet> ();
        SingleDataResultSet result = null;
        Date starttime = dateUtility.getDbDate();
        Date dbLatencyTime = dateUtility.getLocalDate();
        Boolean approved = true;
        Boolean restoreRTS = false;
        String clientName = null;
        String testKey = null;
        String testId = null;
        Boolean custom = false;


        final String SQL_QUERY1 = " select clientname, _efk_AdminSubject as testkey,  _efk_TestID as testID, customAccommodations as custom from testopportunity where _Key = ${oppkey};";
        SqlParametersMaps parms1 = (new SqlParametersMaps ()).put ("oppkey", oppKey);

        result = executeStatement (connection, SQL_QUERY1, parms1, false).getResultSets ().next ();
        DbResultRecord record = (result.getCount () > 0 ? result.getRecords ().next () : null);
        if (record != null) {
            clientName = record.<String> get ("clientname");
            testKey = record.<String> get ("testkey");
            testId = record.<String> get ("testID");
            custom = record.<Boolean> get ("custom");
        }

        // call local methods and not CommonDLL version
        DataBaseTable splitAccomCodesTbl = _SplitAccomCodes_FN (connection, clientName, testKey, accoms);
        DataBaseTable testKeyAccomsTbl = TestKeyAccommodations_FN(connection, testKey);

        // Currently, debug is always passed in as 0, so this block is not every run
        if (DbComparator.notEqual (debug, 0)) {
            final String SQL_QUERY2 = " select ${segment} as segment, ${clientname} as clientname, ${testkey} as testkey, ${accoms} as accoms;";
            SqlParametersMaps parms2 = (new SqlParametersMaps ()).put ("segment", segment).put ("clientname", clientName).put ("testkey", testKey).put ("accoms", accoms);
            SingleDataResultSet rs1 = executeStatement (connection, SQL_QUERY2, parms2, false).getResultSets ().next ();
            resultsets.add (rs1);

            final String SQL_QUERY3 = "  select * from ${splitTblName}";
            Map<String, String> unquotedParms1 = new HashMap<String, String> ();
            unquotedParms1.put ("splitTblName", splitAccomCodesTbl.getTableName ());
            SingleDataResultSet rs2 = executeStatement (connection, fixDataBaseNames (SQL_QUERY3, unquotedParms1), null, false).getResultSets ().next ();
            resultsets.add (rs2);

            final String SQL_QUERY5 = "select AccType, AccCode, AccValue, AllowChange, studentControl, IsDefault, IsSelectable, valcount from ${testTblName} C,"
                    + " ${splitTblName} S where S.code = C.AccCode  and segment = ${segment};";
            SqlParametersMaps parms3 = (new SqlParametersMaps ()).put ("segment", segment);
            Map<String, String> unquotedParms = new HashMap<String, String> ();
            unquotedParms.put ("splitTblName", splitAccomCodesTbl.getTableName ());
            unquotedParms.put ("testTblName", testKeyAccomsTbl.getTableName ());
            SingleDataResultSet rs4 = executeStatement (connection, fixDataBaseNames (SQL_QUERY5, unquotedParms), parms3, false).getResultSets ().next ();
            resultsets.add (rs4);
        }

        final DataBaseTable accomsTable = getDataBaseTable ("accoms").addColumn ("atype", SQL_TYPE_To_JAVA_TYPE.VARCHAR, 50).addColumn ("acode", SQL_TYPE_To_JAVA_TYPE.VARCHAR, 100)
                .addColumn ("avalue", SQL_TYPE_To_JAVA_TYPE.VARCHAR, 250).addColumn ("allow", SQL_TYPE_To_JAVA_TYPE.BIT).addColumn ("control", SQL_TYPE_To_JAVA_TYPE.BIT)
                .addColumn ("recordUsage", SQL_TYPE_To_JAVA_TYPE.BIT).addColumn ("isDefault", SQL_TYPE_To_JAVA_TYPE.BIT).addColumn ("isSelectable", SQL_TYPE_To_JAVA_TYPE.BIT)
                .addColumn ("valCount", SQL_TYPE_To_JAVA_TYPE.INT);
        connection.createTemporaryTable (accomsTable);
        Map<String, String> unquotedParms3 = new HashMap<String, String> ();
        unquotedParms3.put ("accomsTableName", accomsTable.getTableName ());

        //TODO: added IsEntryControl = 0 to prevent deletion of Other accommodation in testeeAccommodations table as some calls do not have the
        //the correct Othervalue. ApproveOpportunity for example does not. Other value is also not updated so no need of updating in these calls
        final String SQL_INSERT1 = " insert into ${accomsTableName} (atype, acode, avalue, allow, control, isDefault, isSelectable, valcount, recordUsage) "
                + " select distinct AccType, AccCode, AccValue, AllowChange, studentControl, IsDefault, IsSelectable, valcount, "
                + " (select count(*) from ${ConfigDB}.client_toolusage where clientname = ${clientname} "
                + " and testID = ${testID} and tooltype = AccType and (recordUsage = 1 or reportUsage = 1) limit 1) "
                + " from ${testTblName} C, ${splitTblName} S where S.code = C.AccCode and segment = ${segment} and IsEntryControl = 0;";
        SqlParametersMaps parms4 = (new SqlParametersMaps ()).put ("clientname", clientName).put ("testID", testId).put ("segment", segment);
        Map<String, String> unquotedParms4 = new HashMap<String, String> ();
        unquotedParms4.put ("accomsTableName", accomsTable.getTableName ());
        unquotedParms4.put ("splitTblName", splitAccomCodesTbl.getTableName ());
        unquotedParms4.put ("testTblName", testKeyAccomsTbl.getTableName ());

        final String query1 = fixDataBaseNames (SQL_INSERT1);
        int insertedCnt = executeStatement (connection, fixDataBaseNames (query1, unquotedParms4), parms4, false).getUpdateCount ();
        // System.err.println (insertedCnt); // for testing

        if (DbComparator.notEqual (debug, 0)) {
            final String SQL_QUERY6 = "select * from ${accomsTableName};";
            SingleDataResultSet rs5 = executeStatement (connection, fixDataBaseNames (SQL_QUERY6, unquotedParms3), null, false).getResultSets ().next ();
            resultsets.add (rs5);
        }

        if (DbComparator.notEqual (isStarted, 0)) {
            final String SQL_DELETE1 = "delete from ${accomsTableName} where allow = 0;";
            int deletedCnt = executeStatement (connection, fixDataBaseNames (SQL_DELETE1, unquotedParms3), null, false).getUpdateCount ();
            // System.err.println (deletedCnt); // for testing
        }

        if (DbComparator.isEqual (restoreRTS, true)) {
            final String SQL_DELETE2 = "delete from ${accomsTableName} where isSelectable = 1;";
            int deletedCnt = executeStatement (connection, fixDataBaseNames (SQL_DELETE2, unquotedParms3), null, false).getUpdateCount ();
            // System.err.println (deletedCnt); // for testing
        }
        final String SQL_QUERY7 = "select  isDefault from  ${accomsTableName} where isDefault = 0 limit 1";
        if (exists (executeStatement (connection, fixDataBaseNames (SQL_QUERY7, unquotedParms3), null, false))) {
            custom = true;
        }

        try {
            // TODO: is this a problem that we are using our own connection instead of the original being passed through to this method
            //  in approveAccommodations we use the legacySqlConnection to get a new connection instead of whatever value they might have used before
            boolean preexistingAutoCommitMode = connection.getAutoCommit ();
            connection.setAutoCommit (false);

            final String SQL_DELETE3 = "delete from testeeaccommodations where _fk_TestOpportunity = ${oppkey} and AccType in (select distinct atype from ${accomsTableName}) and segment = ${segment};";
            SqlParametersMaps parms5 = (new SqlParametersMaps ()).put ("oppkey", oppKey).put ("segment", segment);
            int deletedCnt = executeStatement (connection, fixDataBaseNames (SQL_DELETE3, unquotedParms3), parms5, false).getUpdateCount ();
            // System.err.println (deletedCnt); // for testing


            final String SQL_INSERT2 = "insert into testeeaccommodations (_fk_TestOpportunity, AccType, AccCode, AccValue, _date, allowChange, recordUsage, testeeControl, segment, "
                    + " valueCount, isApproved, IsSelectable)"
                    + " select distinct ${oppkey}, atype, acode, avalue, ${starttime}, allow, recordUsage, control, ${segment}, "
                    + " valcount, case valcount when 1 then 1 else ${approved} end, isSelectable from ${accomsTableName};";

            SqlParametersMaps parms6 = (new SqlParametersMaps ()).put ("oppkey", oppKey).put ("starttime", starttime).put ("segment", segment).put ("approved", approved);

            int insertedCnt1 = executeStatement (connection, fixDataBaseNames (SQL_INSERT2, unquotedParms3), parms6, false).getUpdateCount ();
            // System.err.println (insertedCnt1); // for testing

            //insert other accommodation
            //TODO: this is still needed until we can get all calls returning the correct Othervalue. ApproveOpportunity for example does not. Otherwise this part can be merged with insertion
            //of the rest of the accommodations
            final String SQL_SELECT_OTHER = "select code from ${splitTblName} where code like ${otherAccomPrefix} limit 1";
            Map<String, String> unquotedParmsOther = new HashMap<String, String> ();
            unquotedParmsOther.put ("splitTblName", splitAccomCodesTbl.getTableName ());
            SqlParametersMaps paramsOther = (new SqlParametersMaps ()).put ("otherAccomPrefix", AccommodationOther.VALUE_PREFIX + "%");
            String otherAccomValue =  null;
            SingleDataResultSet rsOther = executeStatement (connection, fixDataBaseNames (SQL_SELECT_OTHER, unquotedParmsOther), paramsOther, false).getResultSets ().next ();
            if (rsOther != null) {
                DbResultRecord recordOther = (rsOther.getCount() > 0 ? rsOther.getRecords().next() : null);
                if (recordOther != null) {
                    otherAccomValue = AccommodationOther.getActualValue (recordOther.<String> get ("code"));
                    final String SQL_INSERT_OTHER = "replace into testeeaccommodations (_fk_TestOpportunity, AccType, AccCode, AccValue, _date, allowChange, recordUsage, testeeControl, segment, "
                            + " valueCount, isApproved, IsSelectable)"
                            + " select ${oppkey}, ${otherType}, ${otherCode}, ${otherValue}, ${starttime}, ${otherAllowChange}, ${otherRecordUsage}, ${otherTesteeControl}, ${segment}, "
                            + " ${otherValueCount},  ${otherIsApproved}, ${otherIsSelectable}";
                    SqlParametersMaps parmsOther = (new SqlParametersMaps ()).put ("oppkey", oppKey).put ("starttime", starttime).put ("segment", segment);
                    parmsOther.put ("otherType", AccommodationOther.NAME).put("otherCode", AccommodationOther.CODE).put ("otherValue", otherAccomValue);
                    parmsOther.put ("otherAllowChange", 0).put("otherRecordUsage", 0).put("otherTesteeControl", 0).put("otherValueCount", 1).put("otherIsApproved", 1).put("otherIsSelectable", 0);
                    executeStatement (connection, SQL_INSERT_OTHER, parmsOther, false);
                }
            }

            final String SQL_QUERY8 = "select  atype from ${accomsTableName} where atype = 'Language' limit 1";
            if (exists (executeStatement (connection, fixDataBaseNames (SQL_QUERY8, unquotedParms3), null, false))) {
                final String SQL_UPDATE1 = " update testopportunity T, ${accomsTableName} set T.Language = avalue, T.customAccommodations = ${custom}  where atype = 'Language' and _Key = ${oppkey}; ";
                SqlParametersMaps parms7 = (new SqlParametersMaps ()).put ("custom", custom).put ("oppkey", oppKey);
                int updateCnt = executeStatement (connection, fixDataBaseNames (SQL_UPDATE1, unquotedParms3), parms7, false).getUpdateCount ();
                // System.err.println (updateCnt); // for testing
            } else {
                final String SQL_UPDATE2 = " update testopportunity set customAccommodations = ${custom} where _Key = ${oppkey};";
                SqlParametersMaps parms8 = (new SqlParametersMaps ()).put ("custom", custom).put ("oppkey", oppKey);
                int updateCnt = executeStatement (connection, SQL_UPDATE2, parms8, false).getUpdateCount ();
                // System.err.println (updateCnt); // for testing
            }

            connection.commit ();
            connection.setAutoCommit (preexistingAutoCommitMode);
        } catch (ReturnStatusException re) {
            try {
                connection.rollback ();
            } catch (SQLException e) {
                logger.error (String.format ("Problem rolling back transaction: %s", e.getMessage ()));
            }
            error.set (String.format ("Error setting accommodations: %s", re.getMessage ()));
            dbLatencyService.logLatency("_UpdateOpportunityAccommodations", dbLatencyTime, null, null, oppKey, null, null, null);

            connection.dropTemporaryTable (accomsTable);
            connection.dropTemporaryTable (testKeyAccomsTbl);
            connection.dropTemporaryTable (splitAccomCodesTbl);
            return null;

        } catch (SQLException se) {
            throw new ReturnStatusException (se);
        }

        connection.dropTemporaryTable (accomsTable);
        connection.dropTemporaryTable (testKeyAccomsTbl);
        connection.dropTemporaryTable (splitAccomCodesTbl);

        final DataBaseTable depsTable = getDataBaseTable ("deps").addColumn ("atype", SQL_TYPE_To_JAVA_TYPE.VARCHAR, 50).addColumn ("aval", SQL_TYPE_To_JAVA_TYPE.VARCHAR, 128)
                .addColumn ("acode", SQL_TYPE_To_JAVA_TYPE.VARCHAR, 100).addColumn ("del", SQL_TYPE_To_JAVA_TYPE.BIT);
        connection.createTemporaryTable (depsTable);
        Map<String, String> unquotedParms5 = new HashMap<String, String> ();
        unquotedParms5.put ("depsTableName", depsTable.getTableName ());

        final String SQL_INSERT3 = "  insert into ${depsTableName} (atype, aval, acode, del)"
                + " select AccType, AccValue, AccCode, 0 from testeeaccommodations A where _fk_TestOpportunity= ${oppkey}"
                + " and exists"
                + " (select * from ${ConfigDB}.client_tooldependencies D where D.ContextType = 'Test' and D.Context = ${testID} and"
                + " D.clientname = ${clientname} and A.AccType = D.ThenType and A.AccCode = D.ThenValue);";
        SqlParametersMaps parms9 = (new SqlParametersMaps ()).put ("oppkey", oppKey).put ("testID", testId).put ("clientname", clientName);
        final String query3 = fixDataBaseNames (SQL_INSERT3);
        int insertedCnt2 = executeStatement (connection, fixDataBaseNames (query3, unquotedParms5), parms9, false).getUpdateCount ();
        // System.err.println (insertedCnt2); // for testing

        final String SQL_UPDATE3 = " update ${depsTableName} set del = 1"
                + " where not exists (select * from testeeaccommodations B, ${ConfigDB}.client_tooldependencies D  where _fk_TestOpportunity = ${oppkey}"
                + " and D.ContextType = 'Test' and D.Context = ${testID} and D.clientname = ${clientname}"
                + " and D.ThenType = atype and D.ThenValue = acode and B.AccType = D.IfType and B.AccCode = D.IfValue)";
        SqlParametersMaps parms10 = parms9;
        final String query = fixDataBaseNames (SQL_UPDATE3);
        int updateCnt = executeStatement (connection, fixDataBaseNames (query, unquotedParms5), parms10, false).getUpdateCount ();
        // System.err.println (updateCnt); // for testing

        final String SQL_QUERY9 = "select  del from ${depsTableName} where del = 1 limit 1";
        if (exists (executeStatement (connection, fixDataBaseNames (SQL_QUERY9, unquotedParms5), null, false))) {
            final String SQL_DELETE4 = " delete from testeeaccommodations where _fk_Testopportunity = ${oppkey} and exists "
                    + " (select * from ${depsTableName} where del = 1 and AccType = atype and AccCode = acode)";
            SqlParametersMaps parms11 = (new SqlParametersMaps ()).put ("oppkey", oppKey);
            int deletedCnt = executeStatement (connection, fixDataBaseNames (SQL_DELETE4, unquotedParms5), parms11, false).getUpdateCount ();
            // System.err.println (deletedCnt); // for testing
        }

        String accomString = commonDll.P_FormatAccommodations_FN (connection, oppKey);

        final String SQL_UPDATE4 = " update testopportunity_readonly set AccommodationString = ${accomString} where _fk_TestOpportunity = ${oppkey};";
        SqlParametersMaps parms12 = (new SqlParametersMaps ()).put ("accomString", accomString).put ("oppkey", oppKey);
        int updateCnt1 = executeStatement (connection, SQL_UPDATE4, parms12, false).getUpdateCount ();
        // System.err.println (updateCnt1); // for testing

        dbLatencyService.logLatency("_UpdateOpportunityAccommodations", dbLatencyTime, null, null, oppKey, null, null, null);
        connection.dropTemporaryTable (depsTable);

        return new MultiDataResultSet (resultsets);
    }

    /**
     * Port of CommonDLL.TestKeyAccommodations_FN
     * Changed to private since it is only called internal to this class
     * Uses ItemBankDao to get the test languages and caches that value since it doesn't change because it's not based on a specific opportunity and just on the test metadata
     * @param connection
     * @param testKey
     * @return
     * @throws ReturnStatusException
     */
    private DataBaseTable TestKeyAccommodations_FN(SQLConnection connection, String testKey) throws ReturnStatusException {

        String codeStr = itemBankDao.getTestLanguages(testKey);

        DataBaseTable testKeyAccomsTable = getDataBaseTable ("testKeyAccoms").addColumn ("Segment", SQL_TYPE_To_JAVA_TYPE.INT).addColumn ("DisableOnGuestSession", SQL_TYPE_To_JAVA_TYPE.BIT)
                .addColumn ("ToolTypeSortOrder", SQL_TYPE_To_JAVA_TYPE.INT).addColumn ("ToolValueSortOrder", SQL_TYPE_To_JAVA_TYPE.INT).addColumn ("TypeMode", SQL_TYPE_To_JAVA_TYPE.VARCHAR, 25)
                .addColumn ("ToolMode", SQL_TYPE_To_JAVA_TYPE.VARCHAR, 25).addColumn ("AccType", SQL_TYPE_To_JAVA_TYPE.VARCHAR, 255).addColumn ("AccValue", SQL_TYPE_To_JAVA_TYPE.VARCHAR, 255)
                .addColumn ("AccCode", SQL_TYPE_To_JAVA_TYPE.VARCHAR, 255).addColumn ("IsDefault", SQL_TYPE_To_JAVA_TYPE.BIT).addColumn ("AllowCombine", SQL_TYPE_To_JAVA_TYPE.BIT)
                .addColumn ("IsFunctional", SQL_TYPE_To_JAVA_TYPE.BIT).addColumn ("AllowChange", SQL_TYPE_To_JAVA_TYPE.BIT).addColumn ("IsSelectable", SQL_TYPE_To_JAVA_TYPE.BIT)
                .addColumn ("IsVisible", SQL_TYPE_To_JAVA_TYPE.BIT).addColumn ("studentControl", SQL_TYPE_To_JAVA_TYPE.BIT).addColumn ("ValCount", SQL_TYPE_To_JAVA_TYPE.INT)
                .addColumn ("DependsOnToolType", SQL_TYPE_To_JAVA_TYPE.VARCHAR, 50).addColumn ("IsEntryControl", SQL_TYPE_To_JAVA_TYPE.BIT);
        connection.createTemporaryTable (testKeyAccomsTable);

        final String SQL_INSERT = "insert into ${tblName} (Segment, DisableOnGuestSession, ToolTypeSortOrder, ToolValueSortOrder, TypeMode, ToolMode, AccType, AccValue, AccCode, IsDefault, AllowCombine, IsFunctional, AllowChange,"
                + "IsSelectable, IsVisible, studentControl, ValCount, DependsOnToolType, IsEntryControl)"
                + " (SELECT distinct 0 as Segment, TType.DisableOnGuestSession, TType.SortOrder as ToolTypeSortOrder, TT.SortOrder as ToolValueSortOrder, TType.TestMode as TypeMode,"
                + " TT.TestMode as ToolMode, Type as AccType, Value as AccValue, Code as AccCode, IsDefault, AllowCombine, IsFunctional, AllowChange, IsSelectable, IsVisible, studentControl, "
                + " (select count(*) from ${ConfigDB}.client_testtool TOOL where TOOL.ContextType = ${TEST} and TOOL.Context = MODE.testID  and TOOL.clientname = MODE.clientname and TOOL.Type = TT.Type) as ValCount, "
                + " DependsOnToolType, IsEntryControl FROM ${ConfigDB}.client_testtooltype TType, ${ConfigDB}.client_testtool TT, ${ConfigDB}.client_testmode MODE"
                + " where MODE.testkey = ${testkey} and TType.ContextType = ${TEST} and TType.Context = MODE.testID and TType.ClientName = MODE.clientname "
                + " and TT.ContextType = ${TEST} and TT.Context = MODE.testID and TT.ClientName = MODE.clientname and TT.Type = TType.Toolname and (TT.Type <> ${Language} or TT.Code in (${codeStr})) "
                + " and (TType.TestMode = ${ALL} or TType.TestMode = MODE.mode) and (TT.TestMode = ${ALL} or TT.TestMode = MODE.mode)) "
                + " union all "
                + " (SELECT distinct SegmentPosition ,TType.DisableOnGuestSession, TType.SortOrder , TT.SortOrder, TType.TestMode , TT.TestMode, Type , Value , Code , IsDefault, AllowCombine, IsFunctional, AllowChange,"
                + " IsSelectable, IsVisible, studentControl, (select count(*) from ${ConfigDB}.client_testtool TOOL where TOOL.ContextType = ${TEST} and TOOL.Context = MODE.testID and "
                + " TOOL.clientname = MODE.clientname and TOOL.Type = TT.Type) as ValCount, null, IsEntryControl FROM ${ConfigDB}.client_testtooltype TType, ${ConfigDB}.client_testtool TT, ${ConfigDB}.client_segmentproperties SEG, "
                + " ${ConfigDB}.client_testmode MODE where parentTest = MODE.testID and MODE.testkey = ${testkey} and SEG.modekey = ${testkey} and TType.ContextType = ${SEGMENT} and TType.Context = segmentID and "
                + " TType.ClientName = MODE.clientname and TT.ContextType = ${SEGMENT} and TT.Context = segmentID and TT.ClientName = MODE.clientname and TT.Type = TType.Toolname and (TType.TestMode = ${ALL} or "
                + " TType.TestMode = MODE.mode) and (TT.TestMode = ${ALL} or TT.TestMode = MODE.mode)) "
                + " union all "
                + " (select distinct 0,TType.DisableOnGuestSession,  TType.SortOrder , TT.SortOrder, TType.TestMode , TT.TestMode, Type, Value, Code, "
                + " IsDefault, AllowCombine, IsFunctional, AllowChange, IsSelectable, IsVisible, studentControl, (select count(*) from ${ConfigDB}.client_testtool TOOL where TOOL.ContextType = ${TEST} and TOOL.Context = ${starParam}"
                + " and TOOL.clientname = MODE.clientname and TOOL.Type = TT.Type) as ValCount, DependsOnToolType, IsEntryControl FROM  ${ConfigDB}.client_testtooltype TType, ${ConfigDB}.client_testtool TT, ${ConfigDB}.client_testmode MODE"
                + " where MODE.testkey = ${testkey} and TType.ContextType = ${TEST} and TType.Context = ${starParam} and TType.ClientName = MODE.clientname and TT.ContextType = ${TEST} and TT.Context = ${starParam} and TT.ClientName = MODE.clientname"
                + " and TT.Type = TType.Toolname and (TType.TestMode = ${ALL} or TType.TestMode = MODE.mode) and (TT.TestMode = ${ALL} or TT.TestMode = MODE.mode)"
                + " and not exists "
                + " (select * from ${ConfigDB}.client_testtooltype Tool where Tool.ContextType = ${TEST} and Tool.Context = MODE.testID and Tool.Toolname = TType.Toolname and Tool.Clientname = MODE.clientname))";

        // Note that codeStr var is already comma separated list of quoted strings
        String query = fixDataBaseNames (SQL_INSERT);
        Map<String, String> unquotedparms = new HashMap<String, String> ();
        unquotedparms.put ("tblName", testKeyAccomsTable.getTableName ());
        unquotedparms.put ("codeStr", codeStr);

        SqlParametersMaps parameters = (new SqlParametersMaps ()).put ("testkey", testKey).put ("TEST", "TEST").put ("ALL", "ALL").put ("SEGMENT", "SEGMENT").put ("starParam", "*")
                .put ("Language", "Language");

        executeStatement (connection, fixDataBaseNames (query, unquotedparms), parameters, false).getUpdateCount ();

        return testKeyAccomsTable;
    }

    public DataBaseTable _SplitAccomCodes_FN (SQLConnection connection, String clientname, String testkey, String accoms) throws ReturnStatusException {

        String testId = null, family = null;
//        final String SQL_QUERY1 = "select K.testID,  AccommodationFamily as family from  ${ConfigDB}.client_testmode K, ${ConfigDB}.client_testproperties P "
//                + " where P.clientname = ${clientname} and K.clientname = ${clientname} and K.testkey = ${testkey} and K.testID = P.testID";

        TestAccommodationFamily accommodationFamily = testAccommodationDao.getTestAccommodationFamily(clientname, testkey);
        if (accommodationFamily != null) {
            testId = accommodationFamily.getTestId();
            family = accommodationFamily.getFamily();
        }

        Character codeDelim = '|';
        Character delim = ';';
        Character familyDelim = ':';
        String famLine = null;
        if (family != null)
            famLine = String.format ("%s%s", family, familyDelim);
        // famLine = (family == null ? String.format ("%s", familyDelim) :
        // String.format ("%s%s", family, familyDelim));

        // Just sanity check to avoid exception on checking splits.length
        if (accoms == null)
            accoms = "";
        String[] splits = commonDll._BuildTableAsArray (accoms, delim.toString (), -1);
        String cset1 = null;
        for (int i = 0; i < splits.length; i++) {
            String rec = splits[i];
            if (rec.indexOf (':') > -1 && famLine != null && rec.indexOf (famLine) == -1)
                // if (rec.indexOf (':') > -1 && rec.indexOf (famLine) == -1)
                splits[i] = null;
            if (famLine != null && rec.indexOf (famLine) >= 0) {
                // if (rec.indexOf (famLine) >= 0) {
                rec = rec.substring (family.length () + 1);
                splits[i] = rec;
            }
            if (splits[i] != null) {
                if (cset1 == null)
                    cset1 = splits[i];
                else
                    cset1 = String.format ("%s%s%s", cset1, codeDelim, splits[i]);
            }
        }
        // MA:A402;MA:A501;SS:A208;SS:A204;SS:A307;SS:A402;SS:A104;SS:A302;SS:A212;SS:A213;SS:A107;SS:A308;SS:A501;SS:A103;SS:A401;SS:A105;SS:A303;SS:A101;SS:A404;SC:ENU;RE:ENU-Braille;WR:ENU;SS:TDS_TTS0;MA:TDS_TTS_Item;SC:TDS_TTS0
        String[] split1 = commonDll._BuildTableAsArray (cset1, codeDelim.toString (), -1);

        DataBaseTable tbl = getDataBaseTable ("sac").addColumn ("idx", SQL_TYPE_To_JAVA_TYPE.INT).addColumn ("code", SQL_TYPE_To_JAVA_TYPE.VARCHAR, 100);

        final String[] split1Final = split1;
        executeMethodAndInsertIntoTemporaryTable (connection, new AbstractDataResultExecutor ()
        {
            @Override
            public SingleDataResultSet execute (SQLConnection connection) throws ReturnStatusException {

                List<CaseInsensitiveMap<Object>> resultList = new ArrayList<CaseInsensitiveMap<Object>> ();
                int idx = 1;
                if (split1Final != null) {
                    for (String split : split1Final) {
                        CaseInsensitiveMap<Object> record = new CaseInsensitiveMap<Object> ();
                        record.put ("code", (split.length () > 100 ? split.substring (0, 100) : split));
                        record.put ("idx", idx++);
                        resultList.add (record);
                    }
                }
                SingleDataResultSet rs = new SingleDataResultSet ();
                rs.addColumn ("code", SQL_TYPE_To_JAVA_TYPE.VARCHAR);
                rs.addColumn ("idx", SQL_TYPE_To_JAVA_TYPE.INT);
                rs.addRecords (resultList);

                return rs;
            }
        }, tbl, true);

        return tbl;
    }
}
