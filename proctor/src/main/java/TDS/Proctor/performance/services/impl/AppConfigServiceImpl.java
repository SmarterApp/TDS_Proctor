package TDS.Proctor.performance.services.impl;

import AIR.Common.DB.AbstractDLL;
import AIR.Common.DB.SQLConnection;
import AIR.Common.DB.SQL_TYPE_To_JAVA_TYPE;
import AIR.Common.DB.SqlParametersMaps;
import AIR.Common.DB.results.DbResultRecord;
import AIR.Common.DB.results.MultiDataResultSet;
import AIR.Common.DB.results.SingleDataResultSet;
import AIR.Common.Helpers.CaseInsensitiveMap;
import TDS.Proctor.performance.dao.TimeLimitsDao;
import TDS.Proctor.performance.domain.TimeLimits;
import TDS.Proctor.performance.services.AppConfigService;
import TDS.Shared.Exceptions.ReturnStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import tds.dll.common.performance.caching.CacheType;
import tds.dll.common.performance.domain.ClientSystemFlag;
import tds.dll.common.performance.domain.Externs;
import tds.dll.common.performance.services.ConfigurationService;
import tds.dll.common.performance.utils.LegacySqlConnection;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
public class AppConfigServiceImpl extends AbstractDLL implements AppConfigService {
    private static final Logger logger = LoggerFactory.getLogger(AppConfigServiceImpl.class);

    @Autowired
    private ConfigurationService configurationService;

    @Autowired
    private LegacySqlConnection legacySqlConnection;

    @Autowired
    private TimeLimitsDao timeLimitsDao;

    /**
     * @param clientName
     * @return
     * @throws ReturnStatusException
     */
    public SingleDataResultSet getConfigs(String clientName) throws ReturnStatusException {

        Integer TAInterfaceTimeout = null;
        Integer refreshValue = null;
        Integer refreshValueMultiplier = null;

        String institution = null;
        SingleDataResultSet result = null;

        try (SQLConnection connection = legacySqlConnection.get()) {
            //final String SQL_QUERY1 = "select refreshValue, TAInterfaceTimeout, RefreshValueMultiplier as refreshValueMultiplier from timelimits "
            //        + " where _efk_TestID is null and clientname = ${clientname};";
            //  Replace above SQL with a DAO that caches the results since it will be the same for each proctor
            // TODO: think about moving TestSessionDao timelimits configuration mehod to the common project and use here as well
            TimeLimits timeLimits = timeLimitsDao.getClientTimeLimits(clientName);

            if (timeLimits != null) {
                refreshValue = timeLimits.getRefreshValue();
                TAInterfaceTimeout = timeLimits.getProctorInterfaceTimeout();
                refreshValueMultiplier = timeLimits.getRefreshValueMultiplier();
            }

            //final String SQL_QUERY2 = " select IsOn as training from ${ConfigDB}.client_systemflags where ClientName = ${ClientName} and AuditOBject = ${ProctorTraining};";
            //  Replace the SQL with the configuration service which has caching
            //  need to translate from Boolean to Integer
            //  NOTE: the isProctorFlagOn is called instead of isFlagOn because the SQL here does not join on the externs view like it does on the student side
            //      in practice I don't think it would be different but we would rather be safe and make sure the SQL is the same
            Integer training = configurationService.isProctorFlagOn(clientName, "ProctorTraining") ? 1 : 0;

            //final String SQL_QUERY3 = "select Description as institution from ${ConfigDB}.client_systemflags S where S.ClientName = ${clientname} and AuditObject = ${MatchTesteeProctorSchool} and IsOn = 1;";
            //  this query checks that the MatchTesteeProctorSchool flag is on, and if so returns the description
            //  we replaced with a call to our configuration service which has a cache on the system flags
            //  it will return the flag whether it is on or not, so we need to check the flag before setting the institution variable since it should only be set if the flag is on
            //  NOTE: the getProctorSystemFlag is called instead of getSystemFlag because the SQL here does not join on the externs view like it does on the student side
            //      in practice I don't think it would be different but we would rather be safe and make sure the SQL is the same
            ClientSystemFlag matchTesteeProctorFlag = configurationService.getProctorSystemFlag(clientName, "MatchTesteeProctorSchool");

            if (matchTesteeProctorFlag != null && matchTesteeProctorFlag.getIsOn()) {
                institution = matchTesteeProctorFlag.getDescription();
            }

            Externs externs = configurationService.getExterns(clientName);

            // map the Externs object and individual values to a SingleDataResult so that the calling code doesn't have to change yet
            return createResultEntity(externs, training, institution, refreshValue, TAInterfaceTimeout, refreshValueMultiplier);

//            final String SQL_QUERY4 = "select cast(null as CHAR) as AnonymousLogin, ClientName, Environment, ClientStylePath, TimeZoneOffset, bigtoint(${refreshValue}) as refreshValue, "
//                    + " bigtoint(${TAInterfaceTimeout}) as TAInterfaceTimeout, bigtoint(${training}) as ProctorTraining, cast(${institution} as CHAR) as MatchTesteeProctorSchool, bigtoint(${refreshValueMultiplier}) as refreshValueMultiplier,"
//                    + " proctorCheckin as checkinURL from externs where clientname = ${clientname};";
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
            throw new ReturnStatusException(e);
        }
    }

    /**
     * @param clientName
     * @param context
     * @return
     * @throws ReturnStatusException
     */
    @Cacheable(CacheType.LongTerm)
    public MultiDataResultSet getGlobalAccommodations(String clientName, String context) throws ReturnStatusException {
        try (SQLConnection connection = legacySqlConnection.get()) {
            List<SingleDataResultSet> resultsSets = new ArrayList<SingleDataResultSet> ();
            final String SQL_QUERY1 = "select TType.ContextType, TType.Context, Type as AccType, Value as AccValue, Code as AccCode, IsDefault, AllowCombine, AllowChange, " +
                    " TType.IsSelectable, IsVisible, studentControl, 1 as IsFunctionalN, DependsOnToolType from ${ConfigDB}.client_testtooltype TType, ${ConfigDB}.client_testtool TT" +
                    " where TType.ContextType = ${FAMILY} and TType.clientname = ${clientName} and TT.context = ${context} and TType.Context = TT.context and TT.clientname = ${clientName} " +
                    " and TT.ContextType = ${FAMILY} and TT.Type = TType.ToolName";
            String query1 = fixDataBaseNames (SQL_QUERY1);
            SqlParametersMaps parameters1 = new SqlParametersMaps ().put ("clientName", clientName).put ("context", context).put ("FAMILY", "FAMILY");
            SingleDataResultSet result1 = executeStatement (connection, query1, parameters1, false).getResultSets ().next ();
            result1.addColumn ("IsFunctional", SQL_TYPE_To_JAVA_TYPE.BIT);
            Iterator<DbResultRecord> records = result1.getRecords ();
            while (records.hasNext ()) {
                DbResultRecord record = records.next ();
                record.addColumnValue ("IsFunctional", (record.<Long> get ("IsFunctionalN") == 1 ? true : false));
            }
            resultsSets.add (result1);

            final String SQL_QUERY2 = "select clientname, ContextType, Context, IfType, IfValue, ThenType, ThenValue, IsDefault " +
                    "from ${ConfigDB}.client_tooldependencies where clientname = ${clientName} and ContextType = ${FAMILY}";
            String query2 = fixDataBaseNames (SQL_QUERY2);
            SqlParametersMaps parameters2 = new SqlParametersMaps ().put ("clientName", clientName).put ("FAMILY", "FAMILY");
            SingleDataResultSet result2 = executeStatement (connection, query2, parameters2, true).getResultSets ().next ();
            resultsSets.add (result2);

            MultiDataResultSet results = new MultiDataResultSet (resultsSets);

            return results;
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
            throw new ReturnStatusException(e);
        }

    }

    private SingleDataResultSet createResultEntity(Externs externs, Integer trainingFlag, String institution, Integer refreshValue, Integer taInterfaceTimeout, Integer refreshValueMultiplier)
        throws ReturnStatusException{

        // select 'success' as status, @entity as entityKey;
        List<CaseInsensitiveMap<Object>> resultList = new ArrayList<>();
        CaseInsensitiveMap<Object> rcd = new CaseInsensitiveMap<>();

        // these are the one off values
        rcd.put("ProctorTraining", trainingFlag);
        rcd.put("MatchTesteeProctorSchool", institution);
        rcd.put("refreshValue", refreshValue);
        rcd.put("TAInterfaceTimeout", taInterfaceTimeout);
        rcd.put("refreshValueMultiplier", refreshValueMultiplier);

        // now the data from the externs query
        rcd.put("AnonymousLogin", null);
        rcd.put("ClientName", externs.getClientName());
        rcd.put("Environment", externs.getEnvironment());
        rcd.put("ClientStylePath", externs.getClientStylePath());
        rcd.put("TimeZoneOffset", externs.getTimeZoneOffset());
        rcd.put("checkinURL", externs.getProctorCheckin());
        resultList.add(rcd);

        SingleDataResultSet resultSet = new SingleDataResultSet();
        resultSet.addColumn("ProctorTraining", SQL_TYPE_To_JAVA_TYPE.INT);
        resultSet.addColumn("MatchTesteeProctorSchool", SQL_TYPE_To_JAVA_TYPE.VARCHAR);
        resultSet.addColumn("refreshValue", SQL_TYPE_To_JAVA_TYPE.INT);
        resultSet.addColumn("TAInterfaceTimeout", SQL_TYPE_To_JAVA_TYPE.INT);
        resultSet.addColumn("refreshValueMultiplier", SQL_TYPE_To_JAVA_TYPE.INT);
        resultSet.addColumn("AnonymousLogin", SQL_TYPE_To_JAVA_TYPE.VARCHAR);
        resultSet.addColumn("ClientName", SQL_TYPE_To_JAVA_TYPE.VARCHAR);
        resultSet.addColumn("Environment", SQL_TYPE_To_JAVA_TYPE.VARCHAR);
        resultSet.addColumn("ClientStylePath", SQL_TYPE_To_JAVA_TYPE.VARCHAR);
        resultSet.addColumn("TimeZoneOffset", SQL_TYPE_To_JAVA_TYPE.INT);
        resultSet.addColumn("checkinURL", SQL_TYPE_To_JAVA_TYPE.VARCHAR);
        resultSet.addRecords(resultList);

        return resultSet;
    }

}
