package TDS.Proctor.performance.dao.impl;

import TDS.Proctor.performance.dao.ProctorUserDao;
import TDS.Proctor.performance.dao.mappers.ByteArrayMapper;
import TDS.Proctor.performance.dao.mappers.ProctorPackageInfoMapper;
import TDS.Proctor.performance.domain.ProctorPackageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import tds.dll.common.performance.caching.CacheType;
import tds.dll.common.performance.utils.UuidAdapter;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Repository
public class ProctorUserDaoImpl implements ProctorUserDao {
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private static final Logger logger = LoggerFactory.getLogger(ProctorUserDaoImpl.class);

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    /**
     * This replaces CommonDLL.ValidateProctorSession_FN
     * Changed to make a single query to get the info for the session and check the values afterwards instead of 3 separate queries
     */
    @Override
    public String validateProctorSession(Long proctorKey, UUID sessionKey, UUID browserKey) {
        final String SQL = "select _efk_Proctor as proctorKey, _fk_browser as browserKey from session where _key = :sessionkey and status = 'open' "
                + "and now(3) between datebegin and dateend  limit 1";

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("sessionkey", UuidAdapter.getBytesFromUUID(sessionKey));

        List<Map<String, Object>> rows = namedParameterJdbcTemplate.queryForList(
                SQL,
                parameters);

        // first check if the session is open
        if (rows.size() == 0) {
            return "The session is closed.";
        }

        Map<String, Object> row = rows.get(0);

        // check if the proctor matches
        if (!proctorKey.equals(row.get("proctorKey"))) {
            return "The session is not owned by this proctor";
        }

        // check if the browser matches
        if (!browserKey.equals(UuidAdapter.getUUIDFromBytes((byte[])row.get("browserKey")))) {
            return "Unauthorized session access";
        }

        return null;
    }

    @Override
    public void updateDateVisited(Long proctorKey, UUID sessionKey) {
        final String SQL_UPDATE = "update session set DateVisited = now(3) where _key = :sessionkey";

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("sessionkey", UuidAdapter.getBytesFromUUID(sessionKey));

        namedParameterJdbcTemplate.update(SQL_UPDATE, parameters);
    }

    @Override
    @Cacheable(CacheType.MediumTerm)
    public ProctorPackageInfo getPackage(Long proctorKey, String clientName) {
        final String SQL= "select iscurrent, version, TestType, Package from r_proctorpackage where ProctorKey = :key and ClientName = :clientname";

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("key", proctorKey);
        parameters.addValue("clientname", clientName);

        try {
            return namedParameterJdbcTemplate.queryForObject(SQL, parameters, new ProctorPackageInfoMapper());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return null;
    }
}
