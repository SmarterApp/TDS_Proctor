package TDS.Proctor.performance.dao.mappers;


import TDS.Proctor.performance.domain.ProctorPackageInfo;
import org.springframework.jdbc.core.RowMapper;

import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ProctorPackageInfoMapper implements RowMapper<ProctorPackageInfo> {
    @Override
    public ProctorPackageInfo mapRow(ResultSet resultSet, int i) throws SQLException {
        ProctorPackageInfo info = new ProctorPackageInfo();

        Blob column = resultSet.getBlob("Package");
        byte[] bytes = column.getBytes(1, (int)column.length());
        column.free();

        info.setTestType(resultSet.getString("TestType"));
        info.setPackage(bytes);

        return info;
    }
}
