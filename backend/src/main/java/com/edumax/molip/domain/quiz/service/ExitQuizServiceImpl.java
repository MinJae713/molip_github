package com.edumax.molip.domain.quiz.service;

import com.edumax.molip.domain.quiz.dto.request.CreateExitQuizRequest;
import com.edumax.molip.domain.quiz.dto.request.SubmitExitQuizListRequest;
import com.edumax.molip.domain.quiz.dto.response.CreateExitQuizResponse;
import com.edumax.molip.domain.quiz.dto.response.FindExitQuizResponse;
import com.edumax.molip.domain.quiz.dto.response.SubmitExitQuizListResponse;
import com.edumax.molip.domain.quiz.repository.ExitQuizAnswerRepository;
import com.edumax.molip.domain.quiz.repository.ExitQuizRepository;
import com.edumax.molip.domain.quiz.repository.ExitQuizResultRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExitQuizServiceImpl implements ExitQuizService {
  private final ExitQuizRepository exitQuizRepository;
  private final ExitQuizAnswerRepository exitQuizAnswerRepository;
  private final ExitQuizResultRepository exitQuizResultRepository;

  @Override
  public CreateExitQuizResponse createExitQuiz(
      Long sessionId, Long memberId, List<CreateExitQuizRequest> createExitQuizRequest) {
    createExitQuizRequest.forEach(System.out::println);
    return null;
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
