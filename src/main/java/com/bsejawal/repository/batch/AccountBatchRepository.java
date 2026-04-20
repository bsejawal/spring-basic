package com.bsejawal.repository.batch;

import com.bsejawal.entity.AccountEntity;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AccountBatchRepository {

    private static final String INSERT_SQL = """
            insert into "account" (account_no, pal_account, name, address, city, state)
            values (?, ?, ?, ?, ?, ?)
            """;

    private final JdbcTemplate jdbcTemplate;

    public int batchInsert(List<AccountEntity> accounts) {
        if (accounts == null || accounts.isEmpty()) {
            return 0;
        }

        jdbcTemplate.batchUpdate(INSERT_SQL, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int index) throws SQLException {
                AccountEntity account = accounts.get(index);
                ps.setString(1, account.getAccountNo());
                ps.setString(2, account.getPalAccount());
                ps.setString(3, account.getName());
                ps.setString(4, account.getAddress());
                ps.setString(5, account.getCity());
                ps.setString(6, account.getState());
            }

            @Override
            public int getBatchSize() {
                return accounts.size();
            }
        });

        return accounts.size();
    }
}
