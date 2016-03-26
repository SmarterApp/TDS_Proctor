package TDS.Proctor.performance.dao.impl;

import TDS.Proctor.performance.dao.TestAccommodationDao;
import TDS.Proctor.performance.dao.mappers.ProctorPackageInfoMapper;
import TDS.Proctor.performance.domain.TestAccommodationFamily;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import tds.dll.common.performance.caching.CacheType;
import tds.dll.common.performance.utils.LegacyDbNameUtility;

import javax.sql.DataSource;

@Repository
public class TestAccommodationDaoImpl implements TestAccommodationDao {
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private static final Logger logger = LoggerFactory.getLogger(TestAccommodationDaoImpl.class);

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Autowired
    private LegacyDbNameUtility legacyDbNameUtility;

    @Override
    @Cacheable(CacheType.LongTerm)
    public TestAccommodationFamily getTestAccommodationFamily(String clientName, String testKey) {
        final String SQL = "select K.testID,  AccommodationFamily as family from  ${configDB}.client_testmode K, ${configDB}.client_testproperties P "
                + " where P.clientname = :clientname and K.clientname = :clientname and K.testkey = :testkey and K.testID = P.testID";

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("testkey", testKey);
        parameters.addValue("clientname", clientName);

        try {
            return namedParameterJdbcTemplate.queryForObject(
                    legacyDbNameUtility.setDatabaseNames(SQL),
                    parameters,
                    new BeanPropertyRowMapper<TestAccommodationFamily>(TestAccommodationFamily.class));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return null;
    }
}
