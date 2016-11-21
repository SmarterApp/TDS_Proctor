package TDS.Proctor.performance.dao.impl;

import AIR.Common.DB.AbstractDLL;
import AIR.Common.DB.SQLConnection;
import AIR.Common.DB.SQL_TYPE_To_JAVA_TYPE;
import AIR.Common.DB.SqlParametersMaps;
import AIR.Common.DB.results.DbResultRecord;
import AIR.Common.DB.results.SingleDataResultSet;
import AIR.Common.Sql.AbstractDateUtilDll;
import TDS.Proctor.performance.dao.TestSessionDao;
import TDS.Shared.Exceptions.ReturnStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import tds.dll.common.performance.caching.CacheType;
import tds.dll.common.performance.services.DbLatencyService;
import tds.dll.common.performance.utils.DateUtility;
import tds.dll.common.performance.utils.LegacySqlConnection;
import tds.dll.common.performance.utils.UuidAdapter;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.*;

@Repository
public class TestSessionDaoImpl extends AbstractDLL implements TestSessionDao {
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private static final Logger logger = LoggerFactory.getLogger(TestSessionDaoImpl.class);

    @Autowired
    private DateUtility dateUtility;

    @Autowired
    private AbstractDateUtilDll _dateUtil  = null;

    @Autowired
    private DbLatencyService dbLatencyService;

    @Autowired
    private LegacySqlConnection legacySqlConnection;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }


    @Override
    @Cacheable(CacheType.LongTerm)
    // TODO: this not appear to be working for some reason
    public String getClientName(UUID sessionKey) {
        final String SQL_QUERY = "select clientname from session where _key = :sessionkey";

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("sessionkey", UuidAdapter.getBytesFromUUID(sessionKey));

        return namedParameterJdbcTemplate.queryForObject(SQL_QUERY, parameters, String.class);
    }

    /**
     * Replaces ProctorDll.P_GetCurrentSessionTestees_SP
     * The proctor validation was moved to the Controller so that it is only done once when the Controller calls multiple methods.
     * If this method is reached, then the proctor was authenticated and validated already.
     * @param sessionKey
     * @param proctorKey
     * @param browserKey
     * @return
     */
    @Override
    public SingleDataResultSet getCurrentSessionTestees(UUID sessionKey, Long proctorKey, UUID browserKey) throws ReturnStatusException {
        Date dbLatencyTime = dateUtility.getLocalDate();

        try (SQLConnection connection = legacySqlConnection.get()) {
            List<Date> midnights = getMidnightsWRetStatus(connection);
            Date midnightAM = midnights.get (0);
            Date midnightPM = midnights.get (1);

            String client = getClientName(sessionKey);

            // Line 713: suppress scores value is never used, so removing the call
            //Integer suppressScores  = _SuppressScores_FN (client);

            // replaced the now parameter, with now(3) in the SQL itself to remove the round trip to the DB to retrieve the date
            final String SQL_QUERY = "select tOpp._efk_AdminSubject, tOpp._fk_TestOpportunity as opportunityKey, tOpp._efk_Testee, tOpp._efk_TestID, tOpp.Opportunity,"
                    + " tOpp.TesteeName, tOpp.TesteeID, tOpp.Status, tOpp.DateCompleted, tOpp._fk_Session, tOpp.SessID as SessionID, '' as sessionName,  tOpp.maxitems as ItemCount,"
                    + " ts._efk_segment as SegmentName,"
                    + " case when tOpp.status = ${paused} and tOpp.datePaused is not null "
                    + "   then timestampdiff(MINUTE, tOpp.datePaused, now(3)) "
                    + "   else cast(null as CHAR) end as pauseMinutes,"
                    + " tOpp.numResponses as ResponseCount, (select count(*) from testopprequest REQ where REQ._fk_TestOpportunity = tOpp._fk_TestOpportunity and REQ._fk_Session = ${sessionKey}"
                    + " and DateFulfilled is null and DateSubmitted > ${midnightAM} and DateSubmitted < ${midnightPM}) as RequestCountN, (select value as score from testopportunityscores S, "
                    + " ${ConfigDB}.client_testscorefeatures F  where F.ClientName = ${clientname} and ReportToProctor = 1 and S._fk_TestOpportunity = tOpp._fk_TestOpportunity and S.IsOfficial = 1 "
                    + " and S.MeasureOf = F.MeasureOf and S.MeasureLabel = F.MeasureLabel limit 1) as Score, AccommodationString as Accommodations, tOpp.customAccommodations, tOpp.mode, ctp.msb "
                    + " from testopportunity_readonly tOpp left outer join ${configDB}.client_testproperties ctp on tOpp._efk_testid = ctp.testid and tOpp.clientname = ctp.clientname "
                    + " join testopportunitysegment ts "
                    + " on ts._fk_testopportunity = tOpp._fk_testopportunity"
                    + " where _fk_Session = ${sessionKey} and tOpp.DateChanged > ${midnightAM} and tOpp.DateChanged < ${midnightPM}  and tOpp.status not in ('pending', 'suspended', 'denied');";

            SqlParametersMaps parms = new SqlParametersMaps ().put ("paused", "paused").put ("clientname", client).put ("sessionKey", sessionKey).put ("midnightPM", midnightPM)
                    .put ("midnightAM", midnightAM);
            SingleDataResultSet result = executeStatement (connection, fixDataBaseNames (SQL_QUERY), parms, true).getResultSets ().next ();
            result.addColumn ("RequestCount", SQL_TYPE_To_JAVA_TYPE.INT);
            Iterator<DbResultRecord> records = result.getRecords ();
            while (records.hasNext ()) {
                DbResultRecord record = records.next ();
                record.addColumnValue ("RequestCount", (record.<Long> get ("RequestCountN") == null ? null : record.<Long> get ("RequestCountN").intValue ()));
            }

            dbLatencyService.logLatency("P_GetCurrentSessionTestees", dbLatencyTime, proctorKey, null, null, sessionKey, null, null);
            return result;
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }

        return null;
    }

    protected List<Date> getMidnightsWRetStatus (SQLConnection connection) throws ReturnStatusException {

        List<Date> midnights = new ArrayList<Date> ();
        int timezoneOffset = 0;
        Date midnightAM = null;
        Date midnightPM = null;
        try {
            _dateUtil.calculateMidnights (connection, timezoneOffset);
            midnightAM = _dateUtil.getMidnightAM ();
            midnightPM = _dateUtil.getMidnightPM ();
            midnights.add (midnightAM);
            midnights.add (midnightPM);
        } catch (SQLException se) {
            throw new ReturnStatusException (se);
        }
        return midnights;
    }
}
