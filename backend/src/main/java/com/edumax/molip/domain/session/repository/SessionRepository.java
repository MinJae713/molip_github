package com.edumax.molip.domain.session.repository;

import com.edumax.molip.domain.session.entity.Session;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionRepository extends JpaRepository<Session, Long> {
  Optional<Session> findByJanusRoomId(Long janusRoomId);

  java.util.List<Session> findAllByCourseCourseId(Long courseId);
}
