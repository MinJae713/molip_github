package com.edumax.molip.domain.review.repository;

import com.edumax.molip.domain.review.entity.EmotionGraph;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmotionGraphRepository extends JpaRepository<EmotionGraph, Long> {
  @Query(
      "SELECT e FROM EmotionGraph e WHERE e.learnerAnalysis.sessionId = :sessionId AND e.memberId = :memberId")
  List<EmotionGraph> findBySessionIdAndMemberId(
      @Param("sessionId") Long sessionId, @Param("memberId") Long memberId);
}
