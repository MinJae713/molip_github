package com.edumax.molip.domain.session.repository;

import com.edumax.molip.domain.session.entity.CourseLearnerSession;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseLearnerSessionRepository extends JpaRepository<CourseLearnerSession, Long> {
  List<CourseLearnerSession> findAllBySessionSessionId(Long sessionId);
}
