package com.edumax.molip.domain.chatting.repository;

import com.edumax.molip.domain.chatting.entity.ReviewChattingRoom;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewChattingRoomRepository extends JpaRepository<ReviewChattingRoom, Long> {
  Optional<ReviewChattingRoom> findByLearnerAnalysisSessionIdAndMemberId(
      Long sessionId, Long memberId);

  boolean existsByLearnerAnalysisSessionIdAndMemberId(Long sessionId, Long memberId);

  @Query(
      """
      SELECT DISTINCT r FROM ReviewChattingRoom r
      LEFT JOIN FETCH r.reviewChattingMessages m
      WHERE r.learnerAnalysis.sessionId = :sessionId AND r.memberId = :memberId
      ORDER BY m.createdAt ASC""")
  Optional<ReviewChattingRoom> findWithMessagesBySessionIdAndMemberId(
      @Param("sessionId") Long sessionId, @Param("memberId") Long memberId);
}
