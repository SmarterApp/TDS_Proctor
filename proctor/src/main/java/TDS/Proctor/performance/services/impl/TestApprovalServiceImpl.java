package TDS.Proctor.performance.services.impl;

import AIR.Common.DB.AbstractDLL;
import AIR.Common.DB.results.MultiDataResultSet;
import AIR.Common.DB.results.SingleDataResultSet;
import TDS.Proctor.performance.domain.TestOpportunityInfo;
import TDS.Proctor.performance.domain.TesteeAccommodation;
import TDS.Proctor.performance.services.TestApprovalService;
import TDS.Proctor.performance.utils.TestApprovalHelper;
import TDS.Shared.Exceptions.ReturnStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import tds.dll.common.performance.services.DbLatencyService;
import tds.dll.common.performance.utils.DateUtility;
import tds.dll.common.performance.utils.UuidAdapter;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class TestApprovalServiceImpl extends AbstractDLL implements TestApprovalService {
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private static final Logger logger = LoggerFactory.getLogger(TestApprovalServiceImpl.class);

    @Autowired
    private DateUtility dateUtility;

    @Autowired
    private DbLatencyService dbLatencyService;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    /**
     * Replaces ProctorDll.P_GetTestsForApproval_SP
     * The proctor validation was moved to the Controller so that it is only done once when the Controller calls multiple methods.
     * If this method is reached, then the proctor was authenticated and validated already.
     * @param sessionKey
     * @param proctorKey
     * @param browserKey
     * @return
     * @throws ReturnStatusException
     */
    public MultiDataResultSet getTestsForApproval(UUID sessionKey, Long proctorKey, UUID browserKey) throws ReturnStatusException {

        List<SingleDataResultSet> resultsets = new ArrayList<SingleDataResultSet>();
        SingleDataResultSet result = null;
        Date dbLatencyTime = dateUtility.getLocalDate();

//        DataBaseTable oppsTable = getDataBaseTable ("opps").addColumn ("opportunityKey", SQL_TYPE_To_JAVA_TYPE.UNIQUEIDENTIFIER).addColumn ("_efk_testee", SQL_TYPE_To_JAVA_TYPE.BIGINT)
//                .addColumn ("_efk_TestID", SQL_TYPE_To_JAVA_TYPE.VARCHAR, 200).addColumn ("Opportunity", SQL_TYPE_To_JAVA_TYPE.INT).addColumn ("_efk_AdminSubject", SQL_TYPE_To_JAVA_TYPE.VARCHAR, 250)
//                .addColumn ("status", SQL_TYPE_To_JAVA_TYPE.VARCHAR, 50).addColumn ("testeeID", SQL_TYPE_To_JAVA_TYPE.VARCHAR, 100).addColumn ("testeeName", SQL_TYPE_To_JAVA_TYPE.VARCHAR, 100)
//                .addColumn ("customAccommodations", SQL_TYPE_To_JAVA_TYPE.BIT).addColumn ("waitingForSegment", SQL_TYPE_To_JAVA_TYPE.INT).addColumn ("mode", SQL_TYPE_To_JAVA_TYPE.VARCHAR, 50)
//                .addColumn ("LEP", SQL_TYPE_To_JAVA_TYPE.VARCHAR, 100);
//        connection.createTemporaryTable (oppsTable);
//        Map<String, String> unquotedparms = new HashMap<String, String> ();
//        unquotedparms.put ("oppsTableName", oppsTable.getTableName ());
//
//        DataBaseTable accsTable = getDataBaseTable ("accs").addColumn ("oppKey", SQL_TYPE_To_JAVA_TYPE.UNIQUEIDENTIFIER).addColumn ("AccType", SQL_TYPE_To_JAVA_TYPE.VARCHAR, 100)
//                .addColumn ("AccCode", SQL_TYPE_To_JAVA_TYPE.VARCHAR, 1000).addColumn ("AccValue", SQL_TYPE_To_JAVA_TYPE.VARCHAR, 250).addColumn ("segment", SQL_TYPE_To_JAVA_TYPE.INT)
//                .addColumn ("isSelectable", SQL_TYPE_To_JAVA_TYPE.BIT);
//        connection.createTemporaryTable (accsTable);
//        Map<String, String> unquotedparms1 = new HashMap<String, String> ();
//        unquotedparms1.put ("accsTableName", accsTable.getTableName ());

        // this is looking for pending tests for this session that need approval
//        final String SQL_INSERT = "insert into ${oppsTableName} (opportunityKey, _efk_Testee, _efk_TestID, Opportunity, _efk_AdminSubject, status, testeeID, testeeName, customAccommodations, "
//                + " waitingForSegment, mode) select  _fk_TestOpportunity, _efk_Testee, _efk_TestID, Opportunity, _efk_AdminSubject, status, testeeID, testeeName, customAccommodations,"
//                + " waitingForSegment, mode from testopportunity_readonly O where _fk_Session = ${sessionKey} and O.status in (${pending}, ${suspended}, ${segmentEntry}, ${segmentExit});";
//        SqlParametersMaps parms1 = (new SqlParametersMaps ()).put ("sessionKey", sessionKey).put ("pending", "pending").put ("suspended", "suspended").put ("segmentEntry", "segmentEntry")
//                .put ("segmentExit", "segmentExit");
//        int insertedCnt = executeStatement (connection, fixDataBaseNames (SQL_INSERT, unquotedparms), parms1, false).getUpdateCount ();

        // included the LEP value in this query with a left join instead of a separate update statement
        final String SQL = "select " +
                    "O._fk_TestOpportunity as opportunityId, " +
                    "O._efk_Testee as testee, " +
                    "O._efk_TestID as testId, " +
                    "O.opportunity, " +
                    "O._efk_AdminSubject as adminSubject, O.status, O.testeeID, " +
                    "O.testeeName, O.customAccommodations, O.waitingForSegment, O.mode, " +
                    "A.attributeValue as lepValue, " +
                    "ctp.msb, " +
                    "tos._efk_segment as segmentName " +
                "from testopportunity_readonly O " +
                "left join ${configDB}.client_testproperties ctp on O._efk_testid = ctp.testid and O.clientname = ctp.clientname " +
                "left join testeeattribute A on O._fk_testopportunity = A._fk_testopportunity and A.tds_id = 'LEP' " +
                "left join testopportunitysegment tos on O._fk_testopportunity = tos._fk_testopportunity " +
                    "and O.insegment = tos.segmentposition " +
                "where _fk_Session = :sessionKey and O.status in ('pending', 'suspended', 'segmentEntry', 'segmentExit');";

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("sessionKey", UuidAdapter.getBytesFromUUID(sessionKey));

        List<TestOpportunityInfo> opportunities = namedParameterJdbcTemplate.query(this.fixDataBaseNames(SQL),
                parameters,
                new BeanPropertyRowMapper(TestOpportunityInfo.class));

        if (opportunities.size() == 0) {
            return null;
        }

        // getting here means that we have at least 1 test that needs approval, so now we can start getting the details
        List<byte[]> oppKeys = TestApprovalHelper.getOpportunityKeys(opportunities);

        final String SQL_ACCS = "select _fk_testopportunity as opportunityId, accType as accommodationType, accCode as accommodationCode, accValue as accommodationValue, segment, isSelectable " +
                "from testeeaccommodations " +
                "where _fk_testopportunity in (:opportunityKeys);";

        MapSqlParameterSource accParameters = new MapSqlParameterSource();
        accParameters.addValue("opportunityKeys", oppKeys);

        List<TesteeAccommodation> accommodations = namedParameterJdbcTemplate.query(SQL_ACCS,
                accParameters,
                new BeanPropertyRowMapper(TesteeAccommodation.class));

        // final String SQL_QUERY4 = "select distinct * from ${oppsTableName} where exists (select oppKey from ${accsTableName} where oppkey = opportunityKey);";
        List<TestOpportunityInfo> opportunitiesWithAccommodations = TestApprovalHelper.getOpportunitiesWithAccommodations(opportunities, accommodations);
        resultsets.add(TestApprovalHelper.getResultSet(opportunitiesWithAccommodations));


        // final String SQL_QUERY5 = "select distinct * from ${accsTableName}, ${oppsTableName} where oppkey = opportunityKey and (segment = 0 or  isSelectable = 1);";
        resultsets.add(TestApprovalHelper.getCombinedResultSet(opportunities, accommodations));

        dbLatencyService.logLatency("P_GetTestsForApproval", dbLatencyTime, proctorKey, null, null, sessionKey, null, null);

        return new MultiDataResultSet (resultsets);
    }

//    public MultiDataResultSet getTestsForApproval(SQLConnection connection, UUID sessionKey, Long proctorKey, UUID browserKey) throws ReturnStatusException {
//
//        List<SingleDataResultSet> resultsets = new ArrayList<SingleDataResultSet>();
//        SingleDataResultSet result = null;
//        Date dbLatencyTime = dateUtility.getLocalDate();
//
//        DataBaseTable oppsTable = getDataBaseTable ("opps").addColumn ("opportunityKey", SQL_TYPE_To_JAVA_TYPE.UNIQUEIDENTIFIER).addColumn ("_efk_testee", SQL_TYPE_To_JAVA_TYPE.BIGINT)
//                .addColumn ("_efk_TestID", SQL_TYPE_To_JAVA_TYPE.VARCHAR, 200).addColumn ("Opportunity", SQL_TYPE_To_JAVA_TYPE.INT).addColumn ("_efk_AdminSubject", SQL_TYPE_To_JAVA_TYPE.VARCHAR, 250)
//                .addColumn ("status", SQL_TYPE_To_JAVA_TYPE.VARCHAR, 50).addColumn ("testeeID", SQL_TYPE_To_JAVA_TYPE.VARCHAR, 100).addColumn ("testeeName", SQL_TYPE_To_JAVA_TYPE.VARCHAR, 100)
//                .addColumn ("customAccommodations", SQL_TYPE_To_JAVA_TYPE.BIT).addColumn ("waitingForSegment", SQL_TYPE_To_JAVA_TYPE.INT).addColumn ("mode", SQL_TYPE_To_JAVA_TYPE.VARCHAR, 50)
//                .addColumn ("LEP", SQL_TYPE_To_JAVA_TYPE.VARCHAR, 100);
//        connection.createTemporaryTable (oppsTable);
//        Map<String, String> unquotedparms = new HashMap<String, String> ();
//        unquotedparms.put ("oppsTableName", oppsTable.getTableName ());
//
//        DataBaseTable accsTable = getDataBaseTable ("accs").addColumn ("oppKey", SQL_TYPE_To_JAVA_TYPE.UNIQUEIDENTIFIER).addColumn ("AccType", SQL_TYPE_To_JAVA_TYPE.VARCHAR, 100)
//                .addColumn ("AccCode", SQL_TYPE_To_JAVA_TYPE.VARCHAR, 1000).addColumn ("AccValue", SQL_TYPE_To_JAVA_TYPE.VARCHAR, 250).addColumn ("segment", SQL_TYPE_To_JAVA_TYPE.INT)
//                .addColumn ("isSelectable", SQL_TYPE_To_JAVA_TYPE.BIT);
//        connection.createTemporaryTable (accsTable);
//        Map<String, String> unquotedparms1 = new HashMap<String, String> ();
//        unquotedparms1.put ("accsTableName", accsTable.getTableName ());
//
//        // this is looking for pending tests for this session that need approval
//        final String SQL_INSERT = "insert into ${oppsTableName} (opportunityKey, _efk_Testee, _efk_TestID, Opportunity, _efk_AdminSubject, status, testeeID, testeeName, customAccommodations, "
//                + " waitingForSegment, mode) select  _fk_TestOpportunity, _efk_Testee, _efk_TestID, Opportunity, _efk_AdminSubject, status, testeeID, testeeName, customAccommodations,"
//                + " waitingForSegment, mode from testopportunity_readonly O where _fk_Session = ${sessionKey} and O.status in (${pending}, ${suspended}, ${segmentEntry}, ${segmentExit});";
//        SqlParametersMaps parms1 = (new SqlParametersMaps ()).put ("sessionKey", sessionKey).put ("pending", "pending").put ("suspended", "suspended").put ("segmentEntry", "segmentEntry")
//                .put ("segmentExit", "segmentExit");
//        int insertedCnt = executeStatement (connection, fixDataBaseNames (SQL_INSERT, unquotedparms), parms1, false).getUpdateCount ();
//
////        final String SQL_QUERY1 = "select opportunityKey from ${oppsTableName} limit 1";
//        if (insertedCnt == 0) { // insertedCnt is zero if nothing was inserted, so no need to query the DB for this
//            final String SQL_QUERY2 = "select * from ${oppsTableName}";
//            result = executeStatement (connection, fixDataBaseNames (SQL_QUERY2, unquotedparms), null, false).getResultSets ().next ();
//            resultsets.add (result);
//
//            final String SQL_QUERY3 = "select * from ${accsTableName}, ${oppsTableName}; ";
//            Map<String, String> unquotedparms2 = new HashMap<String, String> ();
//            unquotedparms2.put ("accsTableName", accsTable.getTableName ());
//            unquotedparms2.put ("oppsTableName", oppsTable.getTableName ());
//            result = executeStatement (connection, fixDataBaseNames (SQL_QUERY3, unquotedparms2), null, false).getResultSets ().next ();
//            resultsets.add (result);
//
//            dbLatencyService.logLatency("P_GetTestsForApproval", dbLatencyTime, proctorKey, null, null, sessionKey, null, null);
//
//            connection.dropTemporaryTable (accsTable);
//            connection.dropTemporaryTable (oppsTable);
//            return new MultiDataResultSet (resultsets);
//        }
//
//        final String SQL_INDEX1 = "create index TMP_OPPS on ${oppsTableName} (opportunityKey);";
//        executeStatement (connection, fixDataBaseNames (SQL_INDEX1, unquotedparms), null, false).getUpdateCount ();
//
//        final String SQL_UPDATE1 = "update ${oppsTableName} O, testeeattribute A set O.LEP = A.attributeValue   where A._fk_TestOpportunity = O.opportunityKey and A.TDS_ID = ${LEP}";
//        SqlParametersMaps parms2 = (new SqlParametersMaps ()).put ("LEP", "LEP");
//        int updateCnt = executeStatement (connection, fixDataBaseNames (SQL_UPDATE1, unquotedparms), parms2, false).getUpdateCount ();
//
//        final String SQL_INSERT1 = "insert into ${accsTableName} (oppkey, AccType, AccCode, AccValue, segment, isSelectable) select opportunityKey, AccType, AccCode, AccValue, "
//                + " segment, isSelectable from testeeaccommodations A, ${oppsTableName} where opportunityKey = A._fk_TestOpportunity;";
//        Map<String, String> unquotedparms3 = new HashMap<String, String> ();
//        unquotedparms3.put ("accsTableName", accsTable.getTableName ());
//        unquotedparms3.put ("oppsTableName", oppsTable.getTableName ());
//        insertedCnt = executeStatement (connection, fixDataBaseNames (SQL_INSERT1, unquotedparms3), null, false).getUpdateCount ();
//
//        final String SQL_INDEX2 = "create index TMP_OPPS on ${accsTableName} (oppKey);";
//        executeStatement (connection, fixDataBaseNames (SQL_INDEX2, unquotedparms1), null, false).getUpdateCount ();
//
//        final String SQL_QUERY4 = "select distinct * from ${oppsTableName} where exists (select oppKey from ${accsTableName} where oppkey = opportunityKey);";
//        result = executeStatement (connection, fixDataBaseNames (SQL_QUERY4, unquotedparms3), null, false).getResultSets ().next ();
//        resultsets.add (result);
//
//        final String SQL_QUERY5 = "select distinct * from ${accsTableName}, ${oppsTableName} where oppkey = opportunityKey and (segment = 0 or  isSelectable = 1);";
//        result = executeStatement (connection, fixDataBaseNames (SQL_QUERY5, unquotedparms3), null, false).getResultSets ().next ();
//        resultsets.add (result);
//
//        dbLatencyService.logLatency("P_GetTestsForApproval", dbLatencyTime, proctorKey, null, null, sessionKey, null, null);
//        connection.dropTemporaryTable (accsTable);
//        connection.dropTemporaryTable (oppsTable);
//
//        return new MultiDataResultSet (resultsets);
//    }


}


