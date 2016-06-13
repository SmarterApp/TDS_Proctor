package TDS.Proctor.performance.domain;

import tds.dll.common.performance.utils.UuidAdapter;

import java.util.UUID;

public class TesteeAccommodation {
    private UUID opportunityId;
    private String accommodationType;
    private String accommodationCode;
    private String accommodationValue;
    private Integer segment;
    private Boolean isSelectable;

    public UUID getOpportunityId() {
        return opportunityId;
    }

    public void setOpportunityId(byte[] opportunityId) {
        this.opportunityId = UuidAdapter.getUUIDFromBytes(opportunityId);
    }

    public String getAccommodationType() {
        return accommodationType;
    }

    public void setAccommodationType(String accommodationType) {
        this.accommodationType = accommodationType;
    }

    public String getAccommodationCode() {
        return accommodationCode;
    }

    public void setAccommodationCode(String accommodationCode) {
        this.accommodationCode = accommodationCode;
    }

    public String getAccommodationValue() {
        return accommodationValue;
    }

    public void setAccommodationValue(String accommodationValue) {
        this.accommodationValue = accommodationValue;
    }

    public Integer getSegment() {
        return segment;
    }

    public void setSegment(Integer segment) {
        this.segment = segment;
    }

    public Boolean getSelectable() {
        return isSelectable;
    }

    public void setSelectable(Boolean selectable) {
        isSelectable = selectable;
    }
}
