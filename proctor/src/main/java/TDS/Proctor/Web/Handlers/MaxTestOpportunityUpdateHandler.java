package TDS.Proctor.Web.Handlers;

import TDS.Proctor.performance.services.MaxOpportunityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by emunoz on 4/6/16.
 */
@Controller
@Scope("prototype")
public class MaxTestOpportunityUpdateHandler {

    @Autowired
    private MaxOpportunityService maxOpportunityService;

    /**
     * Updates the number of max opportunities for a given test name.
     *
     * @param testName
     * @param maxOpportunities
     */
    @RequestMapping(value = "/maxopportunities", method = RequestMethod.PUT)
    @Secured({ "ROLE_Assessment Modify" })
    @ResponseBody
    public void updateMaxOpportunities (@RequestParam (value = "testName") String testName,
                                        @RequestParam (value = "maxOpportunities") Integer maxOpportunities) {
        maxOpportunityService.updateClientTestPropertyMaxOpportunities(testName, maxOpportunities);
    }
}
