package TDS.Proctor.performance.dao.impl;


import TDS.Proctor.performance.dao.TimeLimitsDao;
import TDS.Proctor.performance.domain.TimeLimits;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import tds.dll.common.performance.caching.CacheType;

import javax.sql.DataSource;

@Repository
public class TimeLimitsDaoImpl implements TimeLimitsDao {
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private static final Logger logger = LoggerFactory.getLogger(TimeLimitsDaoImpl.class);

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }


    @Override
    @Cacheable(CacheType.LongTerm)
    public TimeLimits getClientTimeLimits(String clientName) {
        final String SQL = "SELECT refreshValue, TAInterfaceTimeout as proctorInterfaceTimeout, refreshValueMultiplier FROM timelimits " +
                "WHERE _efk_TestID is null and clientname = :clientname";

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("clientname", clientName);

        return namedParameterJdbcTemplate.queryForObject(
                SQL,
                parameters,
                new BeanPropertyRowMapper<TimeLimits>(TimeLimits.class));
    }
}
