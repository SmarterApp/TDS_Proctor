package TDS.Proctor.performance.dao.impl;

import TDS.Proctor.performance.dao.TestOpportunityExamMapDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.UUID;

import tds.dll.common.performance.caching.CacheType;

@Repository
public class TestOpportunityExamMapDaoImpl implements TestOpportunityExamMapDao {
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private static final Logger logger = LoggerFactory.getLogger(TestSessionDaoImpl.class);

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Override
    @Cacheable(CacheType.MediumTerm)
    public UUID getTestOpportunityId(UUID examId) {
        final String SQL_QUERY = "select testopportunity_id from testopportunity_exam_map where exam_id = :examId";

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("examId", examId.toString());

        return UUID.fromString(namedParameterJdbcTemplate.queryForObject(SQL_QUERY, parameters, String.class));
    }
}
