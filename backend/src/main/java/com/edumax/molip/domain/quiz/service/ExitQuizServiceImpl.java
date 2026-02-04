package com.edumax.molip.domain.quiz.service;

import com.edumax.molip.domain.course.entity.Course;
import com.edumax.molip.domain.member.entity.Member;
import com.edumax.molip.domain.quiz.dto.request.CreateExitQuizRequest;
import com.edumax.molip.domain.quiz.dto.request.CreateExitQuizRequest.CreateExitQuizAnswerRequest;
import com.edumax.molip.domain.quiz.dto.request.SubmitExitQuizListRequest;
import com.edumax.molip.domain.quiz.dto.response.CreateExitQuizResponse;
import com.edumax.molip.domain.quiz.dto.response.FindExitQuizResponse;
import com.edumax.molip.domain.quiz.dto.response.SubmitExitQuizListResponse;
import com.edumax.molip.domain.quiz.entity.ExitQuiz;
import com.edumax.molip.domain.quiz.entity.ExitQuizAnswer;
import com.edumax.molip.domain.quiz.exception.NoCreateExitQuizPermitException;
import com.edumax.molip.domain.quiz.repository.ExitQuizAnswerRepository;
import com.edumax.molip.domain.quiz.repository.ExitQuizRepository;
import com.edumax.molip.domain.quiz.repository.ExitQuizResultRepository;
import com.edumax.molip.domain.session.entity.Session;
import com.edumax.molip.domain.session.exception.NoSessionException;
import com.edumax.molip.domain.session.repository.SessionRepository;
import com.edumax.molip.domain.workspace.entity.WorkspaceMember;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExitQuizServiceImpl implements ExitQuizService {
  private final ExitQuizRepository exitQuizRepository;
  private final ExitQuizAnswerRepository exitQuizAnswerRepository;
  private final ExitQuizResultRepository exitQuizResultRepository;

  private final SessionRepository sessionRepository;

  @Override
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
          ExitQuiz exitQuiz = exitQuizRepository.save(ExitQuiz.from(exitQuizRequest, session));
          List<CreateExitQuizAnswerRequest> exitQuizAnswers = exitQuizRequest.getExitQuizAnswers();
          exitQuizAnswers.forEach(
              exitQuizAnswer ->
                  exitQuizAnswerRepository.save(ExitQuizAnswer.from(exitQuizAnswer, exitQuiz)));
        });
    return CreateExitQuizResponse.builder().apiResult(true).build();
  }

  @Override
  public List<FindExitQuizResponse> findExitQuizList(Long sessionId) {
    System.out.println("파인두");
    return List.of();
  }

  @Override
  public SubmitExitQuizListResponse submitExitQuizList(
      Long sessionId, Long memberId, SubmitExitQuizListRequest submitExitQuizListRequest) {
    System.out.println("증답 제출");
    return null;
  }
}
