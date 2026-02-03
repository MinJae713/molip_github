package com.edumax.molip.domain.chatting.service;

import com.edumax.molip.domain.chatting.controller.ChattingController;
import com.edumax.molip.domain.chatting.dto.QnaMessageSaveEvent;
import com.edumax.molip.domain.chatting.entity.ReviewChattingMessage;
import com.edumax.molip.domain.chatting.entity.ReviewChattingRoom;
import com.edumax.molip.domain.chatting.repository.ReviewChattingRoomRepository;
import com.edumax.molip.domain.review.entity.LearnerAnalysis;
import com.edumax.molip.domain.review.repository.LearnerAnalysisRepository;
import com.edumax.molip.grpc.qna.QnaRequest;
import com.edumax.molip.grpc.qna.QnaResponse;
import com.edumax.molip.grpc.qna.QnaServiceGrpc;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChattingService {
  private final QnaServiceGrpc.QnaServiceBlockingStub qnaServiceBlockingStub;

  private final ReviewChattingRoomRepository reviewChattingRoomRepository;
  private final LearnerAnalysisRepository learnerAnalysisRepository;

  private final ApplicationEventPublisher eventPublisher;

  @Transactional
  public void createChatRoom(Long sessionId, Long memberId) {
    boolean existsChatRoom =
        reviewChattingRoomRepository.existsByLearnerAnalysisSessionIdAndMemberId(
            sessionId, memberId);
    if (existsChatRoom) {
      return; // 이미 채팅방이 존재하면 아무 작업도 수행하지 않음
    }

    LearnerAnalysis session =
        learnerAnalysisRepository
            .findById(sessionId)
            .orElseThrow(() -> new EntityNotFoundException("세션이 없습니다."));

    ReviewChattingRoom newRoom =
        ReviewChattingRoom.builder()
            .createdAt(LocalDateTime.now())
            .learnerAnalysis(session) // ID가 아닌 객체를 전달
            .memberId(memberId) // 이건 필드 자체가 Long이므로 그대로 전달
            .build();

    reviewChattingRoomRepository.save(newRoom);
  }

  public List<ReviewChattingMessage> readAllMessages(Long sessionId, Long memberId) {
    ReviewChattingRoom chatRoom =
        reviewChattingRoomRepository
            .findWithMessagesBySessionIdAndMemberId(sessionId, memberId)
            .orElseThrow(() -> new EntityNotFoundException("채팅방이 없습니다."));

    return chatRoom.getReviewChattingMessages();
  }

  public ChattingController.QnaMessageResponse getAiAnswerAndSaveMessage(
      Long sessionId, Long memberId, String content) {
    QnaRequest request =
        QnaRequest.newBuilder()
            .setSessionId(sessionId.toString())
            .setMemberId(memberId.toString())
            .setQuestion(content)
            .build();

    QnaResponse response = qnaServiceBlockingStub.createQna(request);

    log.info("QNA AI ANSWER: {}", response.getAnswer());

    LocalDateTime timestamp = LocalDateTime.now();

    eventPublisher.publishEvent(
        new QnaMessageSaveEvent(timestamp, content, response.getAnswer(), sessionId, memberId));

    return new ChattingController.QnaMessageResponse(timestamp, 0L, response.getAnswer());
  }
}
