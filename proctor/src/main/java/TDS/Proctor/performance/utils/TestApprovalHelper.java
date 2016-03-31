package TDS.Proctor.performance.utils;

import AIR.Common.DB.SQL_TYPE_To_JAVA_TYPE;
import AIR.Common.DB.results.SingleDataResultSet;
import AIR.Common.Helpers.CaseInsensitiveMap;
import TDS.Proctor.performance.domain.TestOpportunityInfo;
import TDS.Proctor.performance.domain.TesteeAccommodation;
import TDS.Shared.Exceptions.ReturnStatusException;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import tds.dll.common.performance.utils.UuidAdapter;
import tds.dll.common.rtspackage.proctor.data.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TestApprovalHelper {
    public static List<byte[]> getOpportunityKeys(List<TestOpportunityInfo> opportunities) {
        List<byte[]> keys = new ArrayList<>();

        for (TestOpportunityInfo oppInfo : opportunities) {
            keys.add(UuidAdapter.getBytesFromUUID(oppInfo.getOpportunityId()));
        }

        return keys;
    }

    public static List<UUID> getOpportunityIds(List<TesteeAccommodation> accommodations) {
        List<UUID> keys = new ArrayList<>();

        for (TesteeAccommodation acc : accommodations) {
            keys.add(acc.getOpportunityId());
        }

        return keys;
    }

    public static List<TestOpportunityInfo> getOpportunitiesWithAccommodations(List<TestOpportunityInfo> opportunities, List<TesteeAccommodation> accommodations) {
        final List<UUID> opportunityIds = getOpportunityIds(accommodations);

        return FluentIterable
                .from(opportunities)
                .filter(new Predicate<TestOpportunityInfo>() {
                    @Override
                    public boolean apply(TestOpportunityInfo input) {
                        return input != null && opportunityIds.contains(input.getOpportunityId());
                    }
                })
                .toList();
    }

    public static SingleDataResultSet getResultSet(List<TestOpportunityInfo> opportunities) throws ReturnStatusException {

        SingleDataResultSet rs = new SingleDataResultSet();
        rs.addColumn ("opportunityKey", SQL_TYPE_To_JAVA_TYPE.UNIQUEIDENTIFIER);
        rs.addColumn ("_efk_testee", SQL_TYPE_To_JAVA_TYPE.BIGINT);
        rs.addColumn ("_efk_TestID", SQL_TYPE_To_JAVA_TYPE.VARCHAR);
        rs.addColumn ("Opportunity", SQL_TYPE_To_JAVA_TYPE.INT);
        rs.addColumn ("_efk_AdminSubject", SQL_TYPE_To_JAVA_TYPE.VARCHAR);
        rs.addColumn ("status", SQL_TYPE_To_JAVA_TYPE.VARCHAR);
        rs.addColumn ("testeeID", SQL_TYPE_To_JAVA_TYPE.VARCHAR);
        rs.addColumn ("testeeName", SQL_TYPE_To_JAVA_TYPE.VARCHAR);
        rs.addColumn ("customAccommodations", SQL_TYPE_To_JAVA_TYPE.BIT);
        rs.addColumn ("waitingForSegment", SQL_TYPE_To_JAVA_TYPE.INT);
        rs.addColumn ("mode", SQL_TYPE_To_JAVA_TYPE.VARCHAR);
        rs.addColumn ("LEP", SQL_TYPE_To_JAVA_TYPE.VARCHAR);

        List<CaseInsensitiveMap<Object>> resultList = new ArrayList<>();

        for (TestOpportunityInfo oppInfo : opportunities) {
            CaseInsensitiveMap<Object> rcd = new CaseInsensitiveMap<>();
            rcd.put("opportunityKey", oppInfo.getOpportunityId());
            rcd.put("_efk_testee", oppInfo.getTestee());
            rcd.put("_efk_TestID", oppInfo.getTestId());
            rcd.put("Opportunity", oppInfo.getOpportunity());
            rcd.put("_efk_AdminSubject", oppInfo.getAdminSubject());
            rcd.put("status", oppInfo.getStatus());
            rcd.put("testeeID", oppInfo.getTesteeId());
            rcd.put("testeeName", oppInfo.getTesteeName());
            rcd.put("customAccommodations", oppInfo.getCustomAccommodations());
            rcd.put("waitingForSegment", oppInfo.getWaitingForSegment());
            rcd.put("mode", oppInfo.getMode());
            rcd.put("LEP", oppInfo.getLepValue());
            resultList.add(rcd);
        }

        rs.addRecords(resultList);

        return rs;
    }

    public static SingleDataResultSet getCombinedResultSet(List<TestOpportunityInfo> opportunities, List<TesteeAccommodation> accommodations) throws ReturnStatusException {
        // original query on temp tables
        //      select distinct * from ${accsTableName}, ${oppsTableName} where oppkey = opportunityKey and (segment = 0 or  isSelectable = 1)

        // first step is to filter based on the segment or isSelectable
        List<TesteeAccommodation> filteredAccommodations = filterAccommodationsBySegmentOrIsSelectable(accommodations, 0, true);

        // TODO: clean this up some
        // now let's loop through and do the join on opportunity key to build the result set
        // TODO: need to deal with distinct in the select statement???? not sure

        SingleDataResultSet rs = new SingleDataResultSet();

        // opportunity temp table columns
        rs.addColumn ("opportunityKey", SQL_TYPE_To_JAVA_TYPE.UNIQUEIDENTIFIER);
        rs.addColumn ("_efk_testee", SQL_TYPE_To_JAVA_TYPE.BIGINT);
        rs.addColumn ("_efk_TestID", SQL_TYPE_To_JAVA_TYPE.VARCHAR);
        rs.addColumn ("Opportunity", SQL_TYPE_To_JAVA_TYPE.INT);
        rs.addColumn ("_efk_AdminSubject", SQL_TYPE_To_JAVA_TYPE.VARCHAR);
        rs.addColumn ("status", SQL_TYPE_To_JAVA_TYPE.VARCHAR);
        rs.addColumn ("testeeID", SQL_TYPE_To_JAVA_TYPE.VARCHAR);
        rs.addColumn ("testeeName", SQL_TYPE_To_JAVA_TYPE.VARCHAR);
        rs.addColumn ("customAccommodations", SQL_TYPE_To_JAVA_TYPE.BIT);
        rs.addColumn ("waitingForSegment", SQL_TYPE_To_JAVA_TYPE.INT);
        rs.addColumn ("mode", SQL_TYPE_To_JAVA_TYPE.VARCHAR);
        rs.addColumn ("LEP", SQL_TYPE_To_JAVA_TYPE.VARCHAR);

        // accommodations temp table columns
        rs.addColumn ("oppKey", SQL_TYPE_To_JAVA_TYPE.UNIQUEIDENTIFIER);
        rs.addColumn ("AccType", SQL_TYPE_To_JAVA_TYPE.VARCHAR);
        rs.addColumn ("AccCode", SQL_TYPE_To_JAVA_TYPE.VARCHAR);
        rs.addColumn ("AccValue", SQL_TYPE_To_JAVA_TYPE.VARCHAR);
        rs.addColumn ("segment", SQL_TYPE_To_JAVA_TYPE.INT);
        rs.addColumn ("isSelectable", SQL_TYPE_To_JAVA_TYPE.BIT);


        List<CaseInsensitiveMap<Object>> resultList = new ArrayList<>();

        for (TestOpportunityInfo oppInfo : opportunities) {
            for (TesteeAccommodation acc : filteredAccommodations) {
                if (oppInfo.getOpportunityId() == acc.getOpportunityId()) {

                    CaseInsensitiveMap<Object> rcd = new CaseInsensitiveMap<>();

                    // opportunity columns
                    rcd.put("opportunityKey", oppInfo.getOpportunityId());
                    rcd.put("_efk_testee", oppInfo.getTestee());
                    rcd.put("_efk_TestID", oppInfo.getTestId());
                    rcd.put("Opportunity", oppInfo.getOpportunity());
                    rcd.put("_efk_AdminSubject", oppInfo.getAdminSubject());
                    rcd.put("status", oppInfo.getStatus());
                    rcd.put("testeeID", oppInfo.getTesteeId());
                    rcd.put("testeeName", oppInfo.getTesteeName());
                    rcd.put("customAccommodations", oppInfo.getCustomAccommodations());
                    rcd.put("waitingForSegment", oppInfo.getWaitingForSegment());
                    rcd.put("mode", oppInfo.getMode());
                    rcd.put("LEP", oppInfo.getLepValue());

                    // accommodations values
                    rcd.put("oppKey", acc.getOpportunityId());
                    rcd.put("AccType", acc.getAccommodationType());
                    rcd.put("AccCode", acc.getAccommodationCode());
                    rcd.put("AccValue", acc.getAccommodationValue());
                    rcd.put("segment", acc.getSegment());
                    rcd.put("isSelectable", acc.getSelectable());

                    resultList.add(rcd);
                }
            }
        }

        rs.addRecords(resultList);

        return rs;
    }

    public static List<TesteeAccommodation> filterAccommodationsBySegmentOrIsSelectable(List<TesteeAccommodation> accommodations, final Integer segment, final Boolean isSelectable) {
        return FluentIterable
                .from(accommodations)
                .filter(new Predicate<TesteeAccommodation>() {
                    @Override
                    public boolean apply(TesteeAccommodation input) {
                        return input != null && (input.getSegment() == segment || input.getSelectable() == isSelectable);
                    }
                })
                .toList();
    }
}
