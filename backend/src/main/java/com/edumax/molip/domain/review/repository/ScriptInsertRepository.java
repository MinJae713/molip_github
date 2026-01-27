package com.edumax.molip.domain.review.repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ScriptInsertRepository {
  private final JdbcTemplate jdbcTemplate;

  public void bulkInsertScripts(List<Long> timeList, List<String> textList, Long sessionId) {
    String sql = "INSERT INTO script (time, message, session_id) VALUES (?, ?, ?)";

    jdbcTemplate.batchUpdate(
        sql,
        new BatchPreparedStatementSetter() {
          @Override
          public void setValues(PreparedStatement ps, int i) throws SQLException {
            ps.setLong(1, timeList.get(i));
            ps.setString(2, textList.get(i));
            ps.setLong(3, sessionId);
          }

          @Override
          public int getBatchSize() {
            return timeList.size();
          }
        });
  }
}
