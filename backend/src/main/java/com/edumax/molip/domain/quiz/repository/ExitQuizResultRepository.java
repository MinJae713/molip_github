package com.edumax.molip.domain.quiz.repository;

import com.edumax.molip.domain.quiz.entity.ExitQuiz;
import com.edumax.molip.domain.quiz.entity.ExitQuizResult;
import com.edumax.molip.domain.workspace.entity.WorkspaceMember;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExitQuizResultRepository extends JpaRepository<ExitQuizResult, Long> {
  Optional<ExitQuizResult> findByExitQuizAndWorkspaceMember(
      ExitQuiz exitQuiz, WorkspaceMember workspaceMember);
}
