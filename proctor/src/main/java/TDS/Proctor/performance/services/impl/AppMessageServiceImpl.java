package TDS.Proctor.performance.services.impl;

import AIR.Common.DB.AbstractDLL;
import AIR.Common.DB.DataBaseTable;
import AIR.Common.DB.SQLConnection;
import AIR.Common.DB.SqlParametersMaps;
import AIR.Common.DB.results.DbResultRecord;
import AIR.Common.DB.results.SingleDataResultSet;
import TDS.Proctor.performance.services.AppMessageService;
import TDS.Shared.Exceptions.ReturnStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import tds.dll.api.IProctorDLL;
import tds.dll.common.performance.caching.CacheType;
import tds.dll.common.performance.services.DbLatencyService;
import tds.dll.common.performance.utils.DateUtility;
import tds.dll.common.performance.utils.LegacySqlConnection;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class AppMessageServiceImpl extends AbstractDLL implements AppMessageService {
    private static final Logger logger = LoggerFactory.getLogger(AppMessageServiceImpl.class);

    @Autowired
    private DateUtility dateUtility;

    @Autowired
    private DbLatencyService dbLatencyService;

    @Autowired
    private IProctorDLL proctorDLL;

    @Autowired
    private LegacySqlConnection legacySqlConnection;

    public SingleDataResultSet getAppMessagesByContext(String systemID, String client, String language, String contextList) throws ReturnStatusException {
        return getAppMessagesByContext(systemID, client, language, contextList, ',');
    }
    /**
     * This methods check to see if the data is in the __appMessages tables and if not it populates them
     * Therefore it is okay to cache.
     * This is a port of ProctorDLL.AppMessagesByContext_SP
     * @param systemID
     * @param client
     * @param language
     * @param contextList
     * @param delimiter
     * @return
     * @throws ReturnStatusException
     */
    @Cacheable(CacheType.LongTerm)
    public SingleDataResultSet getAppMessagesByContext(String systemID, String client, String language, String contextList, Character delimiter)
            throws ReturnStatusException {
        Date dbLatencyTime = dateUtility.getLocalDate();
        SingleDataResultSet result = null;

        try (SQLConnection connection = legacySqlConnection.get()) {
            int end = (contextList.length () > 50 ? 49 : contextList.length ());
            String contextIndex = contextList.substring (0, end);

            final String cmd1 = "select   M.msgkey, M.msgSource, M.MessageID, M.ContextType, M.Context, M.Appkey, M.ParaLabels, M.Message, "
                    + " M.Grade, M.Subject, M.Language"
                    + " from ${ConfigDB}.__appmessagecontexts A, ${ConfigDB}.__appmessages M "
                    + " where A.clientname = ${client} and A.systemid = ${systemid} and "
                    + " A.language = ${language} and A.contextindex = ${contextindex} and "
                    + " A.contextlist = ${contextlist} and "
                    + " A._key = M._fk_AppMessageContext and A.dateGenerated is not null";
            String finalcmd = fixDataBaseNames (cmd1);
            SqlParametersMaps par1 = new SqlParametersMaps ().put ("client", client).put ("systemid", systemID).
                    put ("language", language).put ("contextindex", contextIndex).put ("contextlist", contextList);
            result = executeStatement (connection, finalcmd, par1, false).getResultSets ().next ();
            if (result.getCount () > 0) {
                DbResultRecord rec = result.getRecords ().next ();
                Long key = rec.<Long> get ("msgkey");
                dbLatencyService.logLatency("AppMessagesByContext", dbLatencyTime, key, null, null, null, client, null);

                return result;
            }

            // still call the main lgoic in ProctorDLL, no need to add performance improvements since it is only called once to populate the other table
            DataBaseTable tbl = proctorDLL.TDS_GetMessages_SP (connection, systemID, client, language, contextList, delimiter);
            final String cmd = "select * from ${tblName} order by ContextType, Context";
            Map<String, String> unquotedParms = new HashMap<>();
            unquotedParms.put ("tblName", tbl.getTableName ());
            result = executeStatement (connection, fixDataBaseNames (cmd, unquotedParms), null, false).getResultSets ().next ();

            populateAppMessages (connection, tbl, client, language, systemID, contextList, delimiter);
            connection.dropTemporaryTable (tbl);
            dbLatencyService.logLatency("AppMessagesByContext", dbLatencyTime, null, null, null, null, client, null);
            return result;
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
            throw new ReturnStatusException(e);
        }
    }

    // Direct copy from ProctorDLL.populatetAppMessages() which is protected
    protected void populateAppMessages (SQLConnection connection, DataBaseTable msgsTable, String client, String language,
                                        String systemID, String contextList, Character delimiter) throws ReturnStatusException {
        long startTime = System.currentTimeMillis ();
        int insertedCnt;
        Long theKey = null;
        int end = (contextList.length () > 50 ? 49 : contextList.length ());
        String contextIndex = contextList.substring (0, end);
        try {

            final String cmd1 = "insert into ${ConfigDB}.__appmessagecontexts (clientname, systemID, language, contextList,  contextIndex, delim) "
                    + " select ${client}, ${systemID}, ${language}, ${contextList},  ${contextIndex}, ${delimiter} from dual "
                    + " where not exists (select * from ${ConfigDB}.__appmessagecontexts where clientname = ${client} and systemID = ${systemID} "
                    + "                   and language = ${language} and contextindex = ${contextIndex} and contextList = ${contextList})";

            SqlParametersMaps par1 = new SqlParametersMaps ().put ("client", client).put ("systemid", systemID).put ("language", language).
                    put ("contextlist", contextList).put ("contextIndex", contextIndex).put ("delimiter", delimiter.toString ());
            insertedCnt = executeStatement (connection, fixDataBaseNames (cmd1), par1, false).getUpdateCount ();
            if (insertedCnt < 1) {
                logger.info (String.format ("populateAppMessages: No need to insert into __appmessagecontextsfor %s, %s, %s, %s", client, language, systemID, contextIndex));
                return;
            }
        } catch (ReturnStatusException re) {
            logger.error (String.format ("populateAppMessages: Failed inserting rec into __appmessagecontexts for %s, %s, %s, %s: %s",
                    client, language, systemID, contextIndex, re.getMessage ()), re);
            return;
        }
        final String cmd2 = "select cast(LAST_INSERT_ID() as SIGNED) as theKey";
        SingleDataResultSet result = executeStatement (connection, cmd2, null, false).getResultSets ().next ();
        DbResultRecord record = (result.getCount () > 0 ? result.getRecords ().next () : null);
        if (record != null) {
            theKey = record.<Long> get ("theKey");
        }
        // if (theKey == null) {
        // _logger.error (String.format
        // ("Strange, getting null key after successfully inserting __appmessagecontexts for %s, %s, %s, %s",
        // client, language, systemID, contextIndex));
        // return;
        // }
        try {
            final String cmd3 = "insert into ${ConfigDB}.__appmessages (_fk_AppMessageContext, msgkey, msgSource, MessageID,"
                    + " ContextType, Context, Appkey, ParaLabels, Message, Grade, Subject, Language)"
                    + " select ${theKey}, msgkey, msgSource, MessageID, ContextType, Context, Appkey, ParaLabels, Message, Grade, Subject, Language "
                    + " from ${msgsTableName}";
            String finalQuery = fixDataBaseNames (cmd3);
            Map<String, String> unquotedParms3 = new HashMap<> ();
            unquotedParms3.put ("msgsTableName", msgsTable.getTableName ());
            SqlParametersMaps par3 = new SqlParametersMaps ().put ("thekey", theKey);
            insertedCnt = executeStatement (connection, fixDataBaseNames (finalQuery, unquotedParms3), par3, false).getUpdateCount ();
        } catch (ReturnStatusException re) {
            logger.error (String.format ("populateAppMessages: Failed inserting rec into __appmessages for %s, %s, %s, %s: %s",
                    client, language, systemID, contextIndex, re.getMessage ()), re);
            try {
                final String delcmd = "delete from ${ConfigDB}.__appmessagecontexts where "
                        + "where clientname = ${client} and systemID = ${systemID} "
                        + "and language = ${language} and contextindex = ${contextIndex} and contextList = ${contextList}";
                SqlParametersMaps pardel = new SqlParametersMaps ().put ("client", client).put ("systemid", systemID).put ("language", language).
                        put ("contextlist", contextList).put ("contextIndex", contextIndex);
                insertedCnt = executeStatement (connection, fixDataBaseNames (delcmd), pardel, false).getCount ();
            } catch (ReturnStatusException re1) {
                logger.error (String.format ("populateAppMessages: Problem removing rec from __appmessagecontexts: %s", re1.getMessage ()), re1);
            }
            return;
        }
        try {
            final String cmd4 = "update ${ConfigDB}.__appmessagecontexts set dateGenerated = now(3) where _Key = ${thekey}";
            SqlParametersMaps par4 = new SqlParametersMaps ().put ("thekey", theKey);
            int updatedCnt = executeStatement (connection, fixDataBaseNames (cmd4), par4, false).getUpdateCount ();
            // connection.commit ();
            logger.info (String.format ("populateAppMessages: Inserted %d recs into _appmessages for %s, %s, %s, %s", insertedCnt, client, language, systemID, contextIndex));
        } catch (ReturnStatusException re) {
            logger.error (String.format ("populateAppMessages: Failed updating dategenerated in __appmessagecontexts for %s, %s, %s, %s: %s",
                    client, language, systemID, contextIndex, re.getMessage ()), re);
            try {
                final String delcmd = "delete from ${ConfigDB}.__appmessagecontexts where "
                        + "where clientname = ${client} and systemID = ${systemID} "
                        + "and language = ${language} and contextindex = ${contextIndex} and contextList = ${contextList}";
                SqlParametersMaps pardel = new SqlParametersMaps ().put ("client", client).put ("systemid", systemID).put ("language", language).
                        put ("contextlist", contextList).put ("contextIndex", contextIndex);
                insertedCnt = executeStatement (connection, fixDataBaseNames (delcmd), pardel, false).getCount ();
            } catch (ReturnStatusException re1) {

                logger.error (String.format ("Problem removing rec from __appmessagecontexts: %s", re1.getMessage ()), re1);
            }
        }
    }
}
