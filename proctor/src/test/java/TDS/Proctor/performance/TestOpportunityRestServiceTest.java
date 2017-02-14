package TDS.Proctor.performance;

import TDS.Proctor.Services.TestOpportunityRestService;
import TDS.Proctor.Services.WebConfiguration;
import TDS.Proctor.Sql.Data.TestOpps;
import org.junit.Test;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.util.Random;
import java.util.UUID;

import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.anything;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

public class TestOpportunityRestServiceTest {
    RestTemplate restTemplate;
    MockRestServiceServer mockServer;
    TestOpportunityRestService testOpportunityRestService;

    String emptyResponseBody = "[]";
    String pendingExamsResponseBody = "[\n" +
        "  {\n" +
        "    \"id\": \"f8bbdc2b-4801-4862-8013-83461466f23f\",\n" +
        "    \"sessionId\": \"f810eaba-3ae1-4dac-b8cb-d2060e7644a3\",\n" +
        "    \"browserId\": \"205bd85d-8a20-4d21-acba-3699151d5e67\",\n" +
        "    \"assessmentId\": \"SBAC-ELA-3\",\n" +
        "    \"studentId\": 1,\n" +
        "    \"attempts\": 1,\n" +
        "    \"maxItems\": 0,\n" +
        "    \"status\": {\n" +
        "      \"code\": \"pending\",\n" +
        "      \"stage\": \"IN_USE\"\n" +
        "    },\n" +
        "    \"statusChangedAt\": \"2017-02-12T23:14:56.898Z\",\n" +
        "    \"clientName\": \"SBAC_PT\",\n" +
        "    \"subject\": \"ELA\",\n" +
        "    \"createdAt\": \"2017-02-12T23:15:17.385Z\",\n" +
        "    \"loginSSID\": \"ADV001\",\n" +
        "    \"studentName\": \"Vader, Darth Anakin\",\n" +
        "    \"dateJoined\": \"2017-02-12T23:15:17.383Z\",\n" +
        "    \"assessmentWindowId\": \"ANNUAL\",\n" +
        "    \"assessmentAlgorithm\": \"fixedform\",\n" +
        "    \"assessmentKey\": \"(SBAC_PT)SBAC-ELA-3-Spring-2013-2015\",\n" +
        "    \"environment\": \"Development\",\n" +
        "    \"segmented\": false,\n" +
        "    \"abnormalStarts\": 0,\n" +
        "    \"waitingForSegmentApproval\": false,\n" +
        "    \"currentSegmentPosition\": 0,\n" +
        "    \"customAccommodations\": false,\n" +
        "    \"resumptions\": 0,\n" +
        "    \"restartsAndResumptions\": 0\n" +
        "  }\n" +
        "]";

    String examAccommodations = "[\n" +
        "  {\n" +
        "    \"id\": \"0a64892f-7ba5-442f-adbc-f62baf5fec91\",\n" +
        "    \"examId\": \"f8bbdc2b-4801-4862-8013-83461466f23f\",\n" +
        "    \"segmentKey\": \"(SBAC_PT)SBAC-ELA-3-Spring-2013-2015\",\n" +
        "    \"segmentPosition\": 1,\n" +
        "    \"type\": \"Non-Embedded Designated Supports\",\n" +
        "    \"code\": \"NEDS0\",\n" +
        "    \"value\": \"None\",\n" +
        "    \"description\": \"None\",\n" +
        "    \"selectable\": false,\n" +
        "    \"allowChange\": false,\n" +
        "    \"createdAt\": \"2017-02-12T23:15:17.401Z\",\n" +
        "    \"totalTypeCount\": 9,\n" +
        "    \"custom\": false,\n" +
        "    \"approved\": true\n" +
        "  },\n" +
        "  {\n" +
        "    \"id\": \"0ad6e4e4-3173-44bc-aaf1-260b346cf5a7\",\n" +
        "    \"examId\": \"f8bbdc2b-4801-4862-8013-83461466f23f\",\n" +
        "    \"segmentKey\": \"(SBAC_PT)SBAC-ELA-3-Spring-2013-2015\",\n" +
        "    \"segmentPosition\": 0,\n" +
        "    \"type\": \"Audio Playback Controls\",\n" +
        "    \"code\": \"TDS_APC_SCRUBBER\",\n" +
        "    \"value\": \"Scrubber\",\n" +
        "    \"description\": \"Scrubber\",\n" +
        "    \"selectable\": false,\n" +
        "    \"allowChange\": true,\n" +
        "    \"deniedAt\": \"2017-02-12T23:27:07.495Z\",\n" +
        "    \"createdAt\": \"2017-02-12T23:27:07.567Z\",\n" +
        "    \"totalTypeCount\": 2,\n" +
        "    \"custom\": false,\n" +
        "    \"approved\": false\n" +
        "  },\n" +
        "  {\n" +
        "    \"id\": \"0c416835-5f1c-4956-82e5-f41496b71afb\",\n" +
        "    \"examId\": \"f8bbdc2b-4801-4862-8013-83461466f23f\",\n" +
        "    \"segmentKey\": \"(SBAC_PT)SBAC-ELA-3-Spring-2013-2015\",\n" +
        "    \"segmentPosition\": 1,\n" +
        "    \"type\": \"Font Type\",\n" +
        "    \"code\": \"TDS_FT_Serif\",\n" +
        "    \"value\": \"Times New Roman\",\n" +
        "    \"description\": \"Times New Roman\",\n" +
        "    \"selectable\": false,\n" +
        "    \"allowChange\": false,\n" +
        "    \"createdAt\": \"2017-02-12T23:15:17.398Z\",\n" +
        "    \"totalTypeCount\": 1,\n" +
        "    \"custom\": false,\n" +
        "    \"approved\": true\n" +
        "  }\n" +
        "]";

    String assessmentAccommodations = "[{\n" +
                "    \"segmentPosition\": 0,\n" +
                "    \"disableOnGuestSession\": false,\n" +
                "    \"toolTypeSortOrder\": 0,\n" +
                "    \"toolValueSortOrder\": 1,\n" +
                "    \"toolMode\": \"ALL\",\n" +
                "    \"type\": \"Non-Embedded Designated Supports\",\n" +
                "    \"value\": \"None\",\n" +
                "    \"code\": \"NEDS0\",\n" +
                "    \"defaultAccommodation\": true,\n" +
                "    \"allowCombine\": false,\n" +
                "    \"allowChange\": true,\n" +
                "    \"functional\": false,\n" +
                "    \"selectable\": true,\n" +
                "    \"visible\": true,\n" +
                "    \"studentControl\": false,\n" +
                "    \"entryControl\": false,\n" +
                "    \"typeMode\": \"ALL\",\n" +
                "    \"segmentKey\": \"(SBAC_PT)SBAC-ELA-3-Spring-2013-2015\",\n" +
                "    \"context\": \"SBAC-ELA-3\",\n" +
                "    \"typeTotal\": 9\n" +
                "  },\n" +
                "  {\n" +
                "    \"segmentPosition\": 0,\n" +
                "    \"disableOnGuestSession\": false,\n" +
                "    \"toolTypeSortOrder\": 0,\n" +
                "    \"toolValueSortOrder\": 1,\n" +
                "    \"toolMode\": \"ALL\",\n" +
                "    \"type\": \"Audio Playback Controls\",\n" +
                "    \"value\": \"Scrubber\",\n" +
                "    \"code\": \"TDS_APC_SCRUBBER\",\n" +
                "    \"defaultAccommodation\": true,\n" +
                "    \"allowCombine\": false,\n" +
                "    \"allowChange\": true,\n" +
                "    \"functional\": true,\n" +
                "    \"selectable\": false,\n" +
                "    \"visible\": false,\n" +
                "    \"studentControl\": false,\n" +
                "    \"entryControl\": false,\n" +
                "    \"typeMode\": \"ALL\",\n" +
                "    \"segmentKey\": \"(SBAC_PT)SBAC-ELA-3-Spring-2013-2015\",\n" +
                "    \"context\": \"SBAC-ELA-3\",\n" +
                "    \"typeTotal\": 2\n" +
                "  },\n" +
                "  {\n" +
                "    \"segmentPosition\": 0,\n" +
                "    \"disableOnGuestSession\": false,\n" +
                "    \"toolTypeSortOrder\": 0,\n" +
                "    \"toolValueSortOrder\": 1,\n" +
                "    \"toolMode\": \"ALL\",\n" +
                "    \"type\": \"Font Type\",\n" +
                "    \"value\": \"Times New Roman\",\n" +
                "    \"code\": \"TDS_FT_Serif\",\n" +
                "    \"defaultAccommodation\": true,\n" +
                "    \"allowCombine\": false,\n" +
                "    \"allowChange\": true,\n" +
                "    \"functional\": true,\n" +
                "    \"selectable\": false,\n" +
                "    \"visible\": false,\n" +
                "    \"studentControl\": false,\n" +
                "    \"entryControl\": false,\n" +
                "    \"typeMode\": \"ALL\",\n" +
                "    \"segmentKey\": \"(SBAC_PT)SBAC-ELA-3-Spring-2013-2015\",\n" +
                "    \"context\": \"SBAC-ELA-3\",\n" +
                "    \"typeTotal\": 1\n" +
                "  }]\n";

    public TestOpportunityRestServiceTest() {
        restTemplate = new WebConfiguration().getRestTemplate();
        mockServer = MockRestServiceServer.createServer(restTemplate);
        testOpportunityRestService = new TestOpportunityRestService(null, restTemplate, "http://example.com", "http://example.com", false, true);

    }

    @Test
    public void shouldRetrieveExamsPendingApproval() throws Exception {

        mockServer.expect(requestTo(containsString("pending-approval"))).andRespond(
            withSuccess(pendingExamsResponseBody, APPLICATION_JSON));

        mockServer.expect(requestTo(containsString("assessments/accommodations"))).andRespond(
            withSuccess(assessmentAccommodations, APPLICATION_JSON));

        mockServer.expect(requestTo(containsString("accommodations"))).andRespond(
            withSuccess(examAccommodations, APPLICATION_JSON));

        TestOpps testsForApproval = testOpportunityRestService.getTestsForApproval(UUID.randomUUID(), new Random().nextLong(), UUID.randomUUID());

        assertTrue(testsForApproval.size() == 1);
    }

    @Test
    public void shouldRetrieveNoExamsPendingApproval() throws Exception {
        MockRestServiceServer mockServer = MockRestServiceServer.createServer(restTemplate);

        mockServer.expect(anything()).andRespond(
            withSuccess(emptyResponseBody, APPLICATION_JSON));

        TestOpps testsForApproval = testOpportunityRestService.getTestsForApproval(UUID.randomUUID(), new Random().nextLong(), UUID.randomUUID());

        assertTrue(testsForApproval.size() == 0);
    }

    @Test
    public void shouldApproveExam() throws Exception {
        MockRestServiceServer mockServer = MockRestServiceServer.createServer(restTemplate);

        mockServer.expect(anything()).andRespond(
            withSuccess(emptyResponseBody, APPLICATION_JSON));

        TestOpps testsForApproval = testOpportunityRestService.getTestsForApproval(UUID.randomUUID(), new Random().nextLong(), UUID.randomUUID());

        assertTrue(testsForApproval.size() == 0);
    }

    @Test
    public void shouldDenyExamWithoutException() throws Exception {
        MockRestServiceServer mockServer = MockRestServiceServer.createServer(restTemplate);

        mockServer.expect(anything()).andRespond(
            withSuccess(emptyResponseBody, APPLICATION_JSON));

        testOpportunityRestService.denyOpportunity(UUID.randomUUID(), UUID.randomUUID(), new Random().nextLong(), UUID.randomUUID(),"deny reason text");
    }
}
