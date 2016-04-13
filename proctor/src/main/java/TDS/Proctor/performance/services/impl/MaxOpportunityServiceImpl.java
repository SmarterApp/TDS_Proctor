package TDS.Proctor.performance.services.impl;

import TDS.Proctor.performance.services.MaxOpportunityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tds.dll.common.performance.dao.ConfigurationDao;
import tds.dll.common.performance.dao.ItemBankDao;
import tds.dll.common.performance.domain.SetOfAdminSubject;

/**
 * Created by emunoz on 4/6/16.
 */
@Service
public class MaxOpportunityServiceImpl implements MaxOpportunityService {

    @Autowired
    private ConfigurationDao configurationDao;

    @Autowired
    private ItemBankDao itemBankDao;

    @Override
    public void updateClientTestPropertyMaxOpportunities(final String testName, final Integer maxOpportunities) {
        SetOfAdminSubject subjects = itemBankDao.get(testName);

        if (subjects != null) {
            configurationDao.updateClientTestPropertyMaxOpportunities(subjects.getClientName(), subjects.getTestId(), maxOpportunities);
        } else {
            throw new IllegalArgumentException(String.format(
                    "No AdminSubject found for test name %s: Unable to update maximum test opportunities for the assessment.", testName));
        }
    }
}
