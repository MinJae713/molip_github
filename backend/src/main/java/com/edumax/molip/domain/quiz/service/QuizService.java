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
                "안녕하세요! 오늘은 자바스크립트의 핵심, '변수'를 알아봅니다. 변수는 데이터를 저장하는 공간으로, 다양한 종류가 있습니다. 예를 들어, 숫자를 저장하는 'number', 문자를 저장하는 'string', 참과 거짓을 나타내는 'boolean' 등이 있습니다. 변수를 선언할 때는 'var', 'let', 'const' 키워드를 사용합니다.")
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
