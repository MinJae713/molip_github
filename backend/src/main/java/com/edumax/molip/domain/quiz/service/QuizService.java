package com.edumax.molip.domain.quiz.service;

import com.edumax.molip.domain.quiz.dto.response.AiQuizMessage;
import com.edumax.molip.domain.review.repository.ScriptRepository;
import com.edumax.molip.grpc.quiz.QuizRequest;
import com.edumax.molip.grpc.quiz.QuizResponse;
import com.edumax.molip.grpc.quiz.QuizServiceGrpc;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QuizService {
  private final StringRedisTemplate redisTemplate;
  private final QuizServiceGrpc.QuizServiceBlockingStub quizServiceBlockingStub;
  private final ScriptRepository scriptRepository;

  String KEY_FORMAT = "quiz:stt:room-id:%s";

  public AiQuizMessage createQuizRedis(String roomId, int textLength) {
    String key = generateKey(roomId);
    StringBuilder sb = new StringBuilder();

    Long totalCntLong = redisTemplate.opsForZSet().zCard(key);
    if (totalCntLong == null || totalCntLong == 0) {
      return new AiQuizMessage();
    }

    List<String> reverseTextList = new ArrayList<>();
    int totalCnt = totalCntLong.intValue();
    int chunkSize = 10;
    boolean finished = false;
    int curLength = 0;
    int maxPage = (int) Math.ceil((double) totalCnt / chunkSize);

    for (int i = 0; i <= maxPage; i++) {
      int s = i * chunkSize;
      int e = s + chunkSize - 1;
      List<String> textList =
          redisTemplate.opsForZSet().reverseRange(key, s, e).stream()
              .map(
                  value -> {
                    return value.split("|")[1];
                  })
              .toList();

      for (String text : textList) {
        if (text == null) {
          continue;
        }

        if (textLength - curLength >= text.length() + 1) {
          reverseTextList.add(text);
          curLength += text.length() + 1;
        } else {
          reverseTextList.add(text.substring(0, textLength - curLength));
          finished = true;
          break;
        }
      }

      if (finished) {
        break;
      }
    }

    for (int i = reverseTextList.size() - 1; i >= 0; i--) {
      sb.append(" ").append(reverseTextList.get(i));
    }

    QuizRequest request =
        QuizRequest.newBuilder().setSessionId(roomId).setSttText(sb.toString().trim()).build();

    QuizResponse response = quizServiceBlockingStub.createQuiz(request);

    return AiQuizMessage.builder()
        .question(response.getQuestion())
        .answer1(response.getAnswer1())
        .answer2(response.getAnswer2())
        .answer3(response.getAnswer3())
        .answer4(response.getAnswer4())
        .correctAnswer(response.getCorrectAnswer())
        .explanation(response.getExplanation())
        .build();
  }

  private String generateKey(String roomId) {
    return KEY_FORMAT.formatted(roomId);
  }

  public AiQuizMessage createQuizDB(Long sessionId, int time, int textLength) {
    StringBuilder sb = new StringBuilder();

    Long totalCntLong = scriptRepository.countBySessionIdWithTime(sessionId, time);
    if (totalCntLong == null || totalCntLong == 0) {
      return new AiQuizMessage();
    }

    List<String> reverseTextList = new ArrayList<>();
    int totalCnt = totalCntLong.intValue();
    int chunkSize = 10;
    boolean finished = false;
    int curLength = 0;
    int maxPage = (int) Math.ceil((double) totalCnt / chunkSize);

    for (int i = 0; i <= maxPage; i++) {
      List<String> textList =
          scriptRepository.findMessageBySessionIdWithTime(
              sessionId, time, PageRequest.of(i, chunkSize));

      for (String text : textList) {
        if (text == null) {
          continue;
        }

        if (textLength - curLength >= text.length() + 1) {
          reverseTextList.add(text);
          curLength += text.length() + 1;
        } else {
          reverseTextList.add(text.substring(0, textLength - curLength));
          finished = true;
          break;
        }
      }

      if (finished) {
        break;
      }
    }

    for (int i = reverseTextList.size() - 1; i >= 0; i--) {
      sb.append(" ").append(reverseTextList.get(i));
    }

    QuizRequest request =
        QuizRequest.newBuilder()
            .setSessionId(String.valueOf(sessionId))
            .setSttText(sb.toString().trim())
            .build();

    QuizResponse response = quizServiceBlockingStub.createQuiz(request);

    return AiQuizMessage.builder()
        .question(response.getQuestion())
        .answer1(response.getAnswer1())
        .answer2(response.getAnswer2())
        .answer3(response.getAnswer3())
        .answer4(response.getAnswer4())
        .correctAnswer(response.getCorrectAnswer())
        .explanation(response.getExplanation())
        .build();
  }
}
