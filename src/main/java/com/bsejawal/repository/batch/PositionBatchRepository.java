package com.bsejawal.repository.batch;

import com.bsejawal.entity.PositionEntity;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PositionBatchRepository {

    private static final String INSERT_SQL = """
            insert into "position" (account_no, stock, "value")
            values (?, ?, ?)
            """;

    private final JdbcTemplate jdbcTemplate;

    public int batchInsert(List<PositionEntity> positions) {
        if (positions == null || positions.isEmpty()) {
            return 0;
        }

        jdbcTemplate.batchUpdate(INSERT_SQL, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int index) throws SQLException {
                PositionEntity position = positions.get(index);
                ps.setString(1, position.getAccountNo());
                ps.setString(2, position.getStock());
                ps.setBigDecimal(3, position.getValue());
            }

            @Override
            public int getBatchSize() {
                return positions.size();
            }
        });

        return positions.size();
    }
}
