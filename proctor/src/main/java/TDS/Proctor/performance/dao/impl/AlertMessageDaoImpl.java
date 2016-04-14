package TDS.Proctor.performance.dao.impl;

import AIR.Common.DB.AbstractDLL;
import AIR.Common.DB.SQLConnection;
import AIR.Common.DB.SqlParametersMaps;
import AIR.Common.DB.results.SingleDataResultSet;
import TDS.Proctor.performance.dao.AlertMessageDao;
import TDS.Shared.Exceptions.ReturnStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import tds.dll.common.performance.services.DbLatencyService;
import tds.dll.common.performance.utils.DateUtility;
import tds.dll.common.performance.utils.LegacySqlConnection;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Date;

@Repository
public class AlertMessageDaoImpl extends AbstractDLL implements AlertMessageDao {
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private static final Logger logger = LoggerFactory.getLogger(AlertMessageDaoImpl.class);

    @Autowired
    private DateUtility dateUtility;

    @Autowired
    private DbLatencyService dbLatencyService;

    @Autowired
    private LegacySqlConnection legacySqlConnection;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    /**
     * Port of ProctorDll.P_GetUnAcknowledgedAlertMessages_SP
     * Uses the new log latency which uses local time to save a call to the DB
     * @param clientName
     * @param proctorKey
     * @return
     * @throws ReturnStatusException
     */
    @Override
    public SingleDataResultSet getUnacknowledgedAlertMessages(String clientName, Long proctorKey) throws ReturnStatusException {
        Date dbLatencyTime = dateUtility.getLocalDate();
        Date now = dateUtility.getDbDate();

        try (SQLConnection connection = legacySqlConnection.get()) {
            // Note: Was going to replace passing in the now parameter with SQL now(3) in the statement,
            //  but since these are 2 separate queries here is a possibility of them getting different items because of the time take to complete the first query
            //  so instead leaving it as is
            final String SQL_QUERY = "select _key, title, message, dateCreated, createdUser, dateUpdated, updatedUser, dateStarted, dateEnded, dateCancelled, cancelledUser "
                    + " from alertmessages AM  left outer join setofproctoralertmessages S on AM._key = S._fk_AlertMessages and S._efk_Proctor= ${proctorKey} and "
                    + " S.dateChanged < ${now} and S.dateChanged > ${now} where AM.dateStarted <= ${now} and AM.dateEnded > ${now} and AM.dateCancelled is null "
                    + " and S._efk_Proctor is null and AM.clientname = ${clientname};";
            SqlParametersMaps parms = new SqlParametersMaps ().put ("proctorKey", proctorKey).put ("clientname", clientName).put ("now", now);
            SingleDataResultSet result = executeStatement (connection, SQL_QUERY, parms, true).getResultSets ().next ();

            if (result.getCount () > 0) {
                final String SQL_INSERT = "insert into setofproctoralertmessages (_efk_Proctor, _fk_AlertMessages, dateChanged) select ${proctorKey}, AM._key, ${now} from "
                        + " alertmessages AM left outer join setofproctoralertmessages S on AM._key = S._fk_AlertMessages and S._efk_Proctor = ${proctorKey} and S.dateChanged >= ${now} and S.dateChanged <= ${now} "
                        + " where AM.dateStarted <= ${now} and AM.dateEnded > ${now} and AM.dateCancelled is null and S._efk_Proctor is null and AM.clientname = ${clientname};";
                SqlParametersMaps parms1 = parms;
                executeStatement (connection, SQL_INSERT, parms1, true);
            }

            dbLatencyService.logLatency("P_GetUnAcknowledgedAlertMessages", dbLatencyTime, proctorKey, null, null, null, clientName, null);
            return result;
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
            throw new ReturnStatusException(e);
        }

    }

    @Override
    public SingleDataResultSet getCurrentAlertMessages(String clientName) throws ReturnStatusException {
        Date dbLatencyTime = dateUtility.getLocalDate();

        try (SQLConnection connection = legacySqlConnection.get()) {
            final String SQL_QUERY = "select _key, title, message, dateCreated, createdUser, dateUpdated, updatedUser, dateStarted, dateEnded, dateCancelled, cancelledUser "
                    + " from alertmessages where clientname = ${clientname} and dateStarted <= now(3) and dateEnded > now(3) and dateCancelled is null order by dateStarted desc;";
            SqlParametersMaps parameters = new SqlParametersMaps ().put ("clientname", clientName);
            SingleDataResultSet result = executeStatement (connection, SQL_QUERY, parameters, true).getResultSets ().next ();

            dbLatencyService.logLatency("P_GetCurrentAlertMessages", dbLatencyTime, null, null, null, null, clientName, null);
            return result;
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
            throw new ReturnStatusException(e);
        }
    }
}
