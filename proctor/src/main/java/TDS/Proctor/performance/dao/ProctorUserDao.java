package TDS.Proctor.performance.dao;

import TDS.Proctor.performance.domain.ProctorPackageInfo;

import java.util.UUID;


public interface ProctorUserDao {
    String validateProctorSession(Long proctorKey, UUID sessionKey, UUID browserKey);
    void updateDateVisited(Long proctorKey, UUID sessionKey);
    ProctorPackageInfo getPackage(Long proctorKey, String clientName);
}
