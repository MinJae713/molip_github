package com.edumax.molip.domain.review.repository;

import com.edumax.molip.domain.review.dto.TimelineAnalysis;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TimelineAnalysisRepository extends MongoRepository<TimelineAnalysis, String> {
  Optional<TimelineAnalysis> findBySessionIdAndMemberId(Long sessionId, Long memberId);
}
