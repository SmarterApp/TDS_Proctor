package TDS.Proctor.performance.dao.mappers;

import org.springframework.jdbc.core.RowMapper;

import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ByteArrayMapper implements RowMapper<byte[]> {
    @Override
    public byte[] mapRow(ResultSet resultSet, int i) throws SQLException {
        Blob column = resultSet.getBlob(0);
        byte[] bytes = column.getBytes(1, (int)column.length());
        column.free();

        return bytes;
    }
}
