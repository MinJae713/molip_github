package com.edumax.molip.domain.session.service;

import com.edumax.molip.domain.course.entity.Course;
import com.edumax.molip.domain.course.repository.CourseRepository;
import com.edumax.molip.domain.quiz.entity.ExitQuiz;
import com.edumax.molip.domain.quiz.entity.ExitQuiz.ExitQuizType;
import com.edumax.molip.domain.quiz.entity.ExitQuizAnswer;
import com.edumax.molip.domain.quiz.repository.ExitQuizAnswerRepository;
import com.edumax.molip.domain.quiz.repository.ExitQuizRepository;
import com.edumax.molip.domain.session.dto.request.SessionCreateRequest;
import com.edumax.molip.domain.session.dto.request.SessionCreateRequest.QuizDto;
import com.edumax.molip.domain.session.entity.Session;
import com.edumax.molip.domain.session.repository.SessionRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SessionServiceImpl implements SessionService {

  private final SessionRepository sessionRepository;
  private final CourseRepository courseRepository;
  private final ExitQuizRepository exitQuizRepository;
  private final ExitQuizAnswerRepository exitQuizAnswerRepository;

  @Override
  @Transactional
  public Long createSession(SessionCreateRequest request) {
    Course course = courseRepository.findById(request.getCourseId())
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 코스입니다. ID: " + request.getCourseId()));

    Session session = Session.builder()
        .course(course)
        .title(request.getTitle())
        .description(request.getDescription())
        .date(request.getDate())
        .startAt(request.getStartAt())
        .endAt(request.getEndAt())
        .useFocus(request.getUseFocus())
        .useEmotion(request.getUseEmotion())
        .useQuiz(request.getUseQuiz())
        .quizCount(request.getQuizCount())
        .build();

    Session savedSession = sessionRepository.save(session);

    if (Boolean.TRUE.equals(request.getUseQuiz()) && request.getQuizzes() != null) {
      createQuizzes(savedSession, request.getQuizzes());
    }

    return savedSession.getSessionId();
  }

  private void createQuizzes(Session session, List<QuizDto> quizDtos) {
    for (QuizDto quizDto : quizDtos) {
      ExitQuiz exitQuiz = ExitQuiz.builder()
          .session(session)
          .question(quizDto.getQuestion())
          .answerNumber(quizDto.getAnswerNumber())
          .type(ExitQuizType.LOCAL)
          .build();
      
      ExitQuiz savedQuiz = exitQuizRepository.save(exitQuiz);

      // 보기가 있다면 저장 (보통 4개)
      if (quizDto.getChoices() != null) {
        int choiceNumber = 1;
        for (String choiceText : quizDto.getChoices()) {
          ExitQuizAnswer answer = ExitQuizAnswer.builder()
              .exitQuiz(savedQuiz)
              .number(choiceNumber++)
              .answer(choiceText)
              .build();
          exitQuizAnswerRepository.save(answer);
        }
      }
    }
  }
}
