package TDS.Proctor.performance.domain;

public class TimeLimits {
    private Integer proctorInterfaceTimeout;
    private Integer refreshValue;
    private Integer refreshValueMultiplier;

    public Integer getProctorInterfaceTimeout() {
        return proctorInterfaceTimeout;
    }

    public void setProctorInterfaceTimeout(Integer proctorInterfaceTimeout) {
        this.proctorInterfaceTimeout = proctorInterfaceTimeout;
    }

    public Integer getRefreshValue() {
        return refreshValue;
    }

    public void setRefreshValue(Integer refreshValue) {
        this.refreshValue = refreshValue;
    }

    public Integer getRefreshValueMultiplier() {
        return refreshValueMultiplier;
    }

    public void setRefreshValueMultiplier(Integer refreshValueMultiplier) {
        this.refreshValueMultiplier = refreshValueMultiplier;
    }
}
