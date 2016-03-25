package TDS.Proctor.performance.services.impl;

import AIR.Common.DB.AbstractDLL;
import AIR.Common.DB.SQLConnection;
import AIR.Common.DB.SqlParametersMaps;
import AIR.Common.DB.results.DbResultRecord;
import AIR.Common.DB.results.SingleDataResultSet;
import TDS.Proctor.performance.dao.ProctorUserDao;
import TDS.Proctor.performance.domain.ProctorPackageInfo;
import TDS.Proctor.performance.services.ProctorUserService;
import TDS.Shared.Exceptions.ReturnStatusException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tds.dll.common.performance.services.DbLatencyService;
import tds.dll.common.performance.utils.DateUtility;
import tds.dll.common.performance.utils.LegacySqlConnection;
import tds.dll.common.rtspackage.IRtsPackageReader;
import tds.dll.common.rtspackage.common.exception.RtsPackageReaderException;
import tds.dll.common.rtspackage.common.table.RtsRecord;
import tds.dll.common.rtspackage.common.table.RtsTable;
import tds.dll.common.rtspackage.proctor.ProctorPackageReader;

import java.sql.SQLException;
import java.util.*;

@Service
public class ProctorUserServiceImpl extends AbstractDLL implements ProctorUserService {
    private static final Logger logger = LoggerFactory.getLogger(ProctorUserServiceImpl.class);

    @Autowired
    private DateUtility dateUtility;

    @Autowired
    private DbLatencyService dbLatencyService;

    @Autowired
    private ProctorUserDao proctorUserDao;

    @Autowired
    private LegacySqlConnection legacySqlConnection;

    /**
     * Replaces ProctorDLL.P_SetSessionDateVisited_SP
     * This method no longer validates the proctor as this is done in the Controller.  If it gets here then the proctor is authenticated and validated already.
     * @param sessionKey
     * @param proctorKey
     * @param browserKey
     * @throws ReturnStatusException
     */
    @Override
    public void updateDateVisited(UUID sessionKey, Long proctorKey, UUID browserKey) throws ReturnStatusException {
        Date dbLatencyTime = dateUtility.getLocalDate();

        proctorUserDao.updateDateVisited(proctorKey, sessionKey);

        dbLatencyService.logLatency("P_SetSessionDateVisited", dbLatencyTime, proctorKey, null, null, sessionKey, null, null);
    }

    @Override
    public SingleDataResultSet getAllTests(String clientname, int sessionType, Long proctorKey) throws ReturnStatusException {
        Map<String, Object> testKeyMap = new HashMap<>();

        // Changed to get the package info once.  Includes the TestType as well as the byte[] Package
        ProctorPackageInfo packageInfo = proctorUserDao.getPackage(proctorKey, clientname);
        String testTypes = null;
        if (packageInfo != null) {
            IRtsPackageReader packageReader = new ProctorPackageReader();
            try {
                if (packageReader.read(packageInfo.getPackage())) {
                    RtsTable testList = packageReader.getRtsTable ("Tests");
                    testTypes = packageInfo.getTestType();
                    for (RtsRecord packageRecord : testList.getRecords ()) {
                        testKeyMap.put(packageRecord.get("TestKey").trim(), null);
                    }
                }
            } catch (NumberFormatException | RtsPackageReaderException e) {
                logger.error (e.getMessage (), e);
            }
            packageReader = null;
        }
        SingleDataResultSet resultSet = null;

        if(testKeyMap.size() != 0) {
            try (SQLConnection connection = legacySqlConnection.get()) {
                String testTypesStr = listToQuotedString(testTypes);
                final String SQL_QUERY = "select distinct P.TestID, P.GradeText, P.SubjectName as Subject, P.label as DisplayName, "
                        + " P.SortOrder, P.AccommodationFamily,P.IsSelectable, M.IsSegmented, M.TestKey"
                        + " from ${ConfigDB}.client_testproperties P, ${ConfigDB}.client_testmode M, ${ItemBankDB}.tblsetofadminsubjects S "
                        + " where P.clientname = ${clientname}  and M.clientname = ${clientname} and M.testID = P.testID and M.testkey = S._Key"
                        + " and S.testtype in (${testTypesStr})"
                        + " order by sortorder";
                Map<String, String> unquotedparms = new HashMap<> ();
                unquotedparms.put ("testTypesStr", testTypesStr);
                String query = fixDataBaseNames (SQL_QUERY, unquotedparms);

                SqlParametersMaps parms = new SqlParametersMaps ().put ("clientname", clientname);
                resultSet = executeStatement(connection, fixDataBaseNames(query), parms, false).getResultSets().next();
                Iterator<DbResultRecord> records = resultSet.getRecords ();
                while (records.hasNext()) {
                    DbResultRecord record = records.next();
                    String key = record.get("testkey");
                    if (!testKeyMap.containsKey(key))
                        records.remove ();
                }
            } catch (SQLException e) {
                logger.error(e.getMessage(), e);
                throw new ReturnStatusException(e);
            }
        } else {
            logger.warn("Proctor Package has no tests for proctorKey: " + proctorKey);
        }
        return resultSet;
    }

    private String listToQuotedString (String theLine) {

        String retLine = "";
        String splits[] = StringUtils.split(theLine, ",");
        if (splits.length == 0)
            return retLine;

        for (String split : splits) {
            if (retLine.isEmpty ())
                retLine = String.format("'%s'", split);
            else
                retLine += String.format(", '%s'", split);
        }
        return retLine;
    }
}
