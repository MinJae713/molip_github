package com.edumax.molip.domain.chatting.repository;

import com.edumax.molip.domain.chatting.entity.QnAMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QnAMessageRepository extends JpaRepository<QnAMessage, Long> {
}
