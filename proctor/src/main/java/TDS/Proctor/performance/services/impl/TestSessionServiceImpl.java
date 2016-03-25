package TDS.Proctor.performance.services.impl;

import AIR.Common.DB.AbstractDLL;
import TDS.Proctor.performance.dao.TestSessionDao;
import TDS.Proctor.performance.services.TestSessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class TestSessionServiceImpl extends AbstractDLL implements TestSessionService {
    private static final Logger logger = LoggerFactory.getLogger(TestApprovalServiceImpl.class);

    @Autowired
    private TestSessionDao testSessionDao;

}
