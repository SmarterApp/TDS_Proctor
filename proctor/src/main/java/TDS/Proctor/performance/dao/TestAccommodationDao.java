package TDS.Proctor.performance.dao;


import TDS.Proctor.performance.domain.TestAccommodationFamily;

public interface TestAccommodationDao {
    TestAccommodationFamily getTestAccommodationFamily(String clientName, String testKey);
}
