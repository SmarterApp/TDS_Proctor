package TDS.Proctor.performance.dao.impl;

import AIR.Common.DB.*;
import TDS.Proctor.performance.dao.TestApprovalDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;

@Repository
public class TestApprovalDaoImpl extends AbstractDLL implements TestApprovalDao {
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private static final Logger logger = LoggerFactory.getLogger(TestApprovalDaoImpl.class);

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }



}
