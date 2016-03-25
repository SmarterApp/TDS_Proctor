package TDS.Proctor.performance.dao;


import TDS.Proctor.performance.domain.TimeLimits;

public interface TimeLimitsDao {
    TimeLimits getClientTimeLimits(String clientName);
}
