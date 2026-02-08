package com.edumax.molip.domain.chatting.repository;

import com.edumax.molip.domain.chatting.entity.ReviewChattingMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewChattingMessageRepository
    extends JpaRepository<ReviewChattingMessage, Long> {}
