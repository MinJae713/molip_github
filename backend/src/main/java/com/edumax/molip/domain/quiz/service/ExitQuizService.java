package com.edumax.molip.domain.quiz.service;

import com.edumax.molip.domain.quiz.dto.request.CreateExitQuizRequest;
import com.edumax.molip.domain.quiz.dto.request.SubmitExitQuizListRequest;
import com.edumax.molip.domain.quiz.dto.response.CreateExitQuizResponse;
import com.edumax.molip.domain.quiz.dto.response.FindExitQuizResponse;
import com.edumax.molip.domain.quiz.dto.response.SubmitExitQuizListResponse;
import java.util.List;

public interface ExitQuizService {
  CreateExitQuizResponse createExitQuiz(
      Long sessionId, Long memberId, List<CreateExitQuizRequest> createExitQuizRequest);

  List<FindExitQuizResponse> findExitQuizList(Long sessionId);

  //  void updateExitQuiz();

  //  void deleteExitQuiz();

  SubmitExitQuizListResponse submitExitQuizList(
      Long sessionId, Long memberId, SubmitExitQuizListRequest submitExitQuizListRequest);
}
