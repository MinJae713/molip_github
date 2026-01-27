package com.edumax.molip.domain.session.repository;

import com.edumax.molip.domain.session.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionRepository extends JpaRepository<Session, Long> {
}
