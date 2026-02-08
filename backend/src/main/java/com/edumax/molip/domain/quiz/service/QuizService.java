package com.edumax.molip.domain.quiz.service;

import com.edumax.molip.domain.quiz.dto.response.AiQuizMessage;
import com.edumax.molip.domain.review.repository.ScriptRepository;
import com.edumax.molip.grpc.quiz.QuizRequest;
import com.edumax.molip.grpc.quiz.QuizResponse;
import com.edumax.molip.grpc.quiz.QuizServiceGrpc;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuizService {
  private final StringRedisTemplate redisTemplate;
  private final QuizServiceGrpc.QuizServiceBlockingStub quizServiceBlockingStub;
  private final ScriptRepository scriptRepository;

  String KEY_FORMAT = "quiz:stt:room-id:%d";

  public AiQuizMessage createQuizRedisTest(Long sessionId) {
    QuizRequest request =
        QuizRequest.newBuilder()
            .setSessionId(sessionId.toString())
            .setSttText(
                "그 다음에 여러분들이 공부하고 있는 동안에 아르바이트나 용돈 걱정하지 말라고 매월 교육지원금을 100만원씩 드리고 있습니다. 뿐만 아니라 서울을 제외한 대전, 광주, 구미, 부울경 캠퍼스는 정부에서 주는 훈련장려금 30만원도 추가로 받으실 수 있습니다.")
            .build();

    log.info("Quiz STT Text: {}", request.getSttText());
    QuizResponse response = quizServiceBlockingStub.createQuiz(request);
    log.info("Quiz Response: {}", response);

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

  public AiQuizMessage createQuizRedis(Long sessionId, int textLength) {
    String key = generateKey(sessionId);
    StringBuilder sb = new StringBuilder();

    Long totalCntLong = redisTemplate.opsForZSet().zCard(key);
    if (totalCntLong == null || totalCntLong == 0) {
      return new AiQuizMessage();
    }

    List<String> reverseTextList = new ArrayList<>();
    int totalCnt = totalCntLong.intValue();
    int chunkSize = 30;
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
                    return value.split("\\|")[1];
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
        QuizRequest.newBuilder()
            .setSessionId(sessionId.toString())
            .setSttText(sb.toString().trim())
            .build();

    log.info("Quiz STT Text: {}", sb.toString().trim());
    QuizResponse response = quizServiceBlockingStub.createQuiz(request);
    log.info("Quiz Response: {}", response);

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

  private String generateKey(Long sessionId) {
    return KEY_FORMAT.formatted(sessionId);
  }

  public AiQuizMessage createQuizDB(Long sessionId, int time, int textLength) {
    StringBuilder sb = new StringBuilder();

    Long totalCntLong = scriptRepository.countBySessionIdWithTime(sessionId, time);
    if (totalCntLong == null || totalCntLong == 0) {
      return new AiQuizMessage();
    }

    List<String> reverseTextList = new ArrayList<>();
    int totalCnt = totalCntLong.intValue();
    int chunkSize = 30;
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
            .setSessionId(sessionId.toString())
            .setSttText(sb.toString().trim())
            .build();

    log.info("Quiz STT Text: {}", sb.toString().trim());
    QuizResponse response = quizServiceBlockingStub.createQuiz(request);
    log.info("Quiz Response: {}", response);

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
