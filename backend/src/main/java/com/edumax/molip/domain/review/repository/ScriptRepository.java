package com.edumax.molip.domain.review.repository;

import com.edumax.molip.domain.review.entity.Script;
import io.lettuce.core.dynamic.annotation.Param;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ScriptRepository extends JpaRepository<Script, Long> {
  @Query(
      "SELECT s.message FROM Script s WHERE s.learnerAnalysis.sessionId = :sessionId AND timeline < :timeline ORDER BY s.timeline DESC")
  List<String> findMessageBySessionIdWithTime(
      @Param("sessionId") Long sessionId, @Param("timeline") Integer timeline, Pageable pageable);

  @Query(
      "SELECT count(s) FROM Script s WHERE s.learnerAnalysis.sessionId = :sessionId AND timeline < :timeline")
  Long countBySessionIdWithTime(
      @Param("sessionId") Long sessionId, @Param("timeline") Integer timeline);

  @Query(
      "SELECT s FROM Script s WHERE s.learnerAnalysis.sessionId = :sessionId ORDER BY s.timeline ASC")
  List<Script> findAllBySessionId(@Param("sessionId") Long sessionId);
}
