package TDS.Proctor.performance.domain;

import tds.dll.common.performance.utils.UuidAdapter;

import java.util.UUID;

public class TestOpportunityInfo {
    private UUID opportunityId;
    private Integer testee;
    private String testId;
    private Integer opportunity;
    private String adminSubject;
    private String status;
    private String testeeId;
    private String testeeName;
    private Boolean customAccommodations;
    private Integer waitingForSegment;
    private String mode;
    private String lepValue;

    public UUID getOpportunityId() {
        return opportunityId;
    }

    public void setOpportunityId(byte[] opportunityId) {
        this.opportunityId = UuidAdapter.getUUIDFromBytes(opportunityId);
    }

    public Integer getTestee() {
        return testee;
    }

    public void setTestee(Integer testee) {
        this.testee = testee;
    }

    public String getTestId() {
        return testId;
    }

    public void setTestId(String testId) {
        this.testId = testId;
    }

    public Integer getOpportunity() {
        return opportunity;
    }

    public void setOpportunity(Integer opportunity) {
        this.opportunity = opportunity;
    }

    public String getAdminSubject() {
        return adminSubject;
    }

    public void setAdminSubject(String adminSubject) {
        this.adminSubject = adminSubject;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTesteeId() {
        return testeeId;
    }

    public void setTesteeId(String testeeId) {
        this.testeeId = testeeId;
    }

    public String getTesteeName() {
        return testeeName;
    }

    public void setTesteeName(String testeeName) {
        this.testeeName = testeeName;
    }

    public Boolean getCustomAccommodations() {
        return customAccommodations;
    }

    public void setCustomAccommodations(Boolean customAccommodations) {
        this.customAccommodations = customAccommodations;
    }

    public Integer getWaitingForSegment() {
        return waitingForSegment;
    }

    public void setWaitingForSegment(Integer waitingForSegment) {
        this.waitingForSegment = waitingForSegment;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getLepValue() {
        return lepValue;
    }

    public void setLepValue(String lepValue) {
        this.lepValue = lepValue;
    }
}
