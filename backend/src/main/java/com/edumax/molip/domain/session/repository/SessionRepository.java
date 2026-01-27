package com.edumax.molip.domain.session.repository;

import com.edumax.molip.domain.session.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SessionRepository extends JpaRepository<Session, Long> {
  Optional<Session> findByJanusRoomId(Long janusRoomId);
  java.util.List<Session> findAllByCourse_CourseId(Long courseId);
}
