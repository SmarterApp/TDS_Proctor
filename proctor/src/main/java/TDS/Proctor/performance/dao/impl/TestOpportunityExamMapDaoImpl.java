/***************************************************************************************************
 * Educational Online Test Delivery System
 * Copyright (c) 2017 Regents of the University of California
 *
 * Distributed under the AIR Open Source License, Version 1.0
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 *
 * SmarterApp Open Source Assessment Software Project: http://smarterapp.org
 * Developed by Fairway Technologies, Inc. (http://fairwaytech.com)
 * for the Smarter Balanced Assessment Consortium (http://smarterbalanced.org)
 **************************************************************************************************/

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
        final String SQL_QUERY = "select mp.testopportunity_id \n" +
            "from \n" +
            "   testopportunity_exam_map mp \n" +
            "join \n" +
            "   testopportunity op on unhex(replace(mp.testopportunity_id,'-','')) = op._key \n" +
            "where \n" +
            "   mp.exam_id = :examId \n" +
            "order by \n" +
            "   op.datechanged desc \n" +
            "limit 0, 1";

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("examId", examId.toString());

        return UUID.fromString(namedParameterJdbcTemplate.queryForObject(SQL_QUERY, parameters, String.class));
    }

    @Override
    public UUID getExamId(final UUID testOpportunityId) {
        final String SQL_QUERY = "select exam_id from testopportunity_exam_map where testopportunity_id = :testOpportunityId";

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("testOpportunityId", testOpportunityId.toString());

        return UUID.fromString(namedParameterJdbcTemplate.queryForObject(SQL_QUERY, parameters, String.class));
    }
}
