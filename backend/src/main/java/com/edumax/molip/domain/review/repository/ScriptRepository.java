package com.edumax.molip.domain.review.repository;

import com.edumax.molip.domain.review.entity.Script;
import io.lettuce.core.dynamic.annotation.Param;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ScriptRepository extends JpaRepository<Script, Long> {
  @Query(
      "SELECT s.message FROM Script s WHERE s.learnerAnalysis.sessionId = :sessionId AND time < :time ORDER BY s.time DESC")
  List<String> findMessageBySessionIdWithTime(
      @Param("sessionId") Long sessionId, @Param("time") Integer time, Pageable pageable);

  @Query(
      "SELECT count(s) FROM Script s WHERE s.learnerAnalysis.sessionId = :sessionId AND time < :time")
  Long countBySessionIdWithTime(@Param("sessionId") Long sessionId, @Param("time") Integer time);
}
