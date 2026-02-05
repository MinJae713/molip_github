package com.edumax.molip.domain.chatting.handler;

import com.edumax.molip.domain.chatting.dto.QnaMessageSaveEvent;
import com.edumax.molip.domain.chatting.entity.ReviewChattingMessage;
import com.edumax.molip.domain.chatting.entity.ReviewChattingRoom;
import com.edumax.molip.domain.chatting.repository.ReviewChattingMessageRepository;
import com.edumax.molip.domain.chatting.repository.ReviewChattingRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class QnaMessageSaveListener {
  private final ReviewChattingMessageRepository messageRepository;
  private final ReviewChattingRoomRepository roomRepository;

  @Async("qnaMessageSaveExecutor")
  @EventListener
  @Transactional
  public void qnaMessageSave(QnaMessageSaveEvent event) {
    ReviewChattingRoom chattingRoom =
        roomRepository
            .findByLearnerAnalysisSessionIdAndMemberId(event.getSessionId(), event.getSenderId())
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "채팅방이 존재하지 않습니다. 세션ID: "
                            + event.getSessionId()
                            + ", 멤버ID: "
                            + event.getSenderId()));

    ReviewChattingMessage userMessage =
        ReviewChattingMessage.builder()
            .message(event.getQuestion())
            .createdAt(event.getTimestamp())
            .room(chattingRoom)
            .senderId(event.getSenderId())
            .build();

    ReviewChattingMessage aiMessage =
        ReviewChattingMessage.builder()
            .message(event.getAnswer())
            .createdAt(event.getTimestamp().plusNanos(100000)) // 사용자 메시지보다 0.1초 뒤에 생성된 것으로 설정
            .room(chattingRoom)
            .senderId(0L) // AI의 senderId는 0으로 설정
            .build();

    chattingRoom.addMessage(userMessage);
    chattingRoom.addMessage(aiMessage);

    roomRepository.save(chattingRoom);
  }
}
