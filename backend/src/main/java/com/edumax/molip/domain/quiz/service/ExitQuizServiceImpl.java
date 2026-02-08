package com.edumax.molip.domain.quiz.service;

import com.edumax.molip.domain.course.entity.Course;
import com.edumax.molip.domain.member.entity.Member;
import com.edumax.molip.domain.quiz.dto.request.CreateExitQuizRequest;
import com.edumax.molip.domain.quiz.dto.request.CreateExitQuizRequest.CreateExitQuizAnswerRequest;
import com.edumax.molip.domain.quiz.dto.request.SubmitExitQuizRequest;
import com.edumax.molip.domain.quiz.dto.response.CreateExitQuizResponse;
import com.edumax.molip.domain.quiz.dto.response.FindExitQuizResponse;
import com.edumax.molip.domain.quiz.dto.response.FindExitQuizResponse.FindExitQuizAnswerResponse;
import com.edumax.molip.domain.quiz.dto.response.SubmitExitQuizListResponse;
import com.edumax.molip.domain.quiz.entity.ExitQuiz;
import com.edumax.molip.domain.quiz.entity.ExitQuizAnswer;
import com.edumax.molip.domain.quiz.entity.ExitQuizResult;
import com.edumax.molip.domain.quiz.exception.DuplicatedSolvedExitQuizException;
import com.edumax.molip.domain.quiz.exception.IncorrectExitQuizAnswerException;
import com.edumax.molip.domain.quiz.exception.InvalidCourseLearnerException;
import com.edumax.molip.domain.quiz.exception.InvalidExitQuizAnswerNumberException;
import com.edumax.molip.domain.quiz.exception.NoCreateExitQuizPermitException;
import com.edumax.molip.domain.quiz.exception.NoExitQuizException;
import com.edumax.molip.domain.quiz.repository.ExitQuizAnswerRepository;
import com.edumax.molip.domain.quiz.repository.ExitQuizRepository;
import com.edumax.molip.domain.quiz.repository.ExitQuizResultRepository;
import com.edumax.molip.domain.session.entity.CourseLearner;
import com.edumax.molip.domain.session.entity.Session;
import com.edumax.molip.domain.session.exception.NoSessionException;
import com.edumax.molip.domain.session.repository.SessionRepository;
import com.edumax.molip.domain.workspace.entity.WorkspaceMember;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ExitQuizServiceImpl implements ExitQuizService {
  private final ExitQuizRepository exitQuizRepository;
  private final ExitQuizAnswerRepository exitQuizAnswerRepository;
  private final ExitQuizResultRepository exitQuizResultRepository;

  private final SessionRepository sessionRepository;

  @Override
  @Transactional
  public CreateExitQuizResponse createExitQuiz(
      Long sessionId, Long memberId, List<CreateExitQuizRequest> createExitQuizRequest) {
    // 권한 체크 - 해당 세션을 만든 강사인지 확인
    Session session = sessionRepository.findById(sessionId).orElseThrow(NoSessionException::new);
    Course course = session.getCourse();
    WorkspaceMember trainer = course.getLeaner(); // 이름은 leaner지만 해당 강의 만든 강사
    Member member = trainer.getMember();
    if (!member.getId().equals(memberId)) {
      throw new NoCreateExitQuizPermitException();
    }
    // exit quiz/answer insert
    createExitQuizRequest.forEach(
        exitQuizRequest -> {
          Integer answerNumber = exitQuizRequest.getAnswerNumber();
          ExitQuiz exitQuiz = exitQuizRepository.save(ExitQuiz.from(exitQuizRequest, session));
          List<CreateExitQuizAnswerRequest> exitQuizAnswers = exitQuizRequest.getExitQuizAnswers();
          if (answerNumber > exitQuizAnswers.size()) {
            throw new InvalidExitQuizAnswerNumberException();
          }
          exitQuizAnswers.forEach(
              exitQuizAnswer ->
                  exitQuizAnswerRepository.save(ExitQuizAnswer.from(exitQuizAnswer, exitQuiz)));
        });
    return CreateExitQuizResponse.builder().apiResult(true).build();
  }

  @Override
  public List<FindExitQuizResponse> findExitQuizList(Long sessionId) {
    Session session = sessionRepository.findById(sessionId).orElseThrow(NoSessionException::new);
    List<ExitQuiz> exitQuizzes = session.getExitQuizzes();

    return exitQuizzes.stream()
        .map(
            exitQuiz -> {
              Long exitQuizId = exitQuiz.getExitQuizId();
              String question = exitQuiz.getQuestion();
              Integer answerNumber = exitQuiz.getAnswerNumber();

              List<ExitQuizAnswer> exitQuizAnswersEntity = exitQuiz.getExitQuizAnswers();
              List<FindExitQuizAnswerResponse> exitQuizAnswers =
                  exitQuizAnswersEntity.stream()
                      .map(
                          exitQuizAnswer -> {
                            Long exitQuizAnswerId = exitQuizAnswer.getExitQuizAnswerId();
                            Integer number = exitQuizAnswer.getNumber();
                            String answer = exitQuizAnswer.getAnswer();
                            return FindExitQuizAnswerResponse.builder()
                                .exitQuizAnswerId(exitQuizAnswerId)
                                .number(number)
                                .answer(answer)
                                .build();
                          })
                      .toList();

              return FindExitQuizResponse.builder()
                  .exitQuizId(exitQuizId)
                  .question(question)
                  .answerNumber(answerNumber)
                  .exitQuizAnswers(exitQuizAnswers)
                  .build();
            })
        .toList();
  }

  @Override
  @Transactional
  public List<SubmitExitQuizListResponse> submitExitQuizList(
      Long sessionId, Long memberId, List<SubmitExitQuizRequest> submitExitQuizListRequest) {
    // 멤버 검증 - 해당 세션 수강생인지 확인
    Session session = sessionRepository.findById(sessionId).orElseThrow(NoSessionException::new);
    Course course = session.getCourse();
    List<CourseLearner> courseLearners = course.getCourseLearners();
    boolean isCourseLearner = false;
    WorkspaceMember currentWorkspaceMember = null;
    for (CourseLearner courseLearner : courseLearners) {
      WorkspaceMember workspaceMember = courseLearner.getWorkspaceMember();
      Member member = workspaceMember.getMember();
      if (member.getId().equals(memberId)) {
        isCourseLearner = true;
        currentWorkspaceMember = workspaceMember;
        break;
      }
    }
    if (!isCourseLearner) {
      throw new InvalidCourseLearnerException();
    }
    // 퇴실 퀴즈 답안 검증 및 저장
    if (session.getExitQuizzes().size() != submitExitQuizListRequest.size()) {
      throw new IncorrectExitQuizAnswerException();
    }
    List<SubmitExitQuizListResponse> result = new ArrayList<>();
    for (SubmitExitQuizRequest submitExitQuizRequest : submitExitQuizListRequest) {
      Long exitQuizId = submitExitQuizRequest.getExitQuizId();
      ExitQuiz exitQuiz =
          exitQuizRepository.findById(exitQuizId).orElseThrow(NoExitQuizException::new);
      Optional<ExitQuizResult> exitQuizResult =
          exitQuizResultRepository.findByExitQuizAndWorkspaceMember(
              exitQuiz, currentWorkspaceMember);
      if (exitQuizResult.isPresent()) {
        throw new DuplicatedSolvedExitQuizException();
      }

      Integer number = submitExitQuizRequest.getNumber();
      boolean answerResult = exitQuiz.getAnswerNumber().equals(number);
      exitQuizResultRepository.save(
          ExitQuizResult.builder()
              .resultNumber(number)
              .result(answerResult)
              .exitQuiz(exitQuiz)
              .workspaceMember(currentWorkspaceMember)
              .build());
      result.add(
          SubmitExitQuizListResponse.builder().exitQuizId(exitQuizId).result(answerResult).build());
    }

    return result;
  }
}
