package com.edumax.molip.domain.session.service;

import com.edumax.molip.domain.course.entity.Course;
import com.edumax.molip.domain.course.repository.CourseApplyRepository;
import com.edumax.molip.domain.course.repository.CourseRepository;
import com.edumax.molip.domain.quiz.entity.ExitQuiz;
import com.edumax.molip.domain.quiz.entity.ExitQuiz.ExitQuizType;
import com.edumax.molip.domain.quiz.entity.ExitQuizAnswer;
import com.edumax.molip.domain.quiz.repository.ExitQuizAnswerRepository;
import com.edumax.molip.domain.quiz.repository.ExitQuizRepository;
import com.edumax.molip.domain.session.dto.request.SessionCreateRequest;
import com.edumax.molip.domain.session.dto.request.SessionCreateRequest.QuizDto;
import com.edumax.molip.domain.session.dto.response.SessionDetailResponse;
import com.edumax.molip.domain.session.dto.response.SessionSimpleResponse;
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
  private final CourseApplyRepository courseApplyRepository;

  @Override
  @Transactional
  public Long createSession(SessionCreateRequest request, Long memberId) {
    Course course = courseRepository.findById(request.getCourseId())
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 코스입니다. ID: " + request.getCourseId()));

    // 권한 체크: 요청한 memberId가 해당 코스의 강사(Leaner -> WorkspaceMember -> Member.id)와 일치하는지 확인
    if (!course.getLeaner().getMember().getId().equals(memberId)) {
      throw new IllegalStateException("해당 코스에 대한 세션 생성 권한이 없습니다.");
    }

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

  @Override
  @Transactional
  public void updateSession(Long sessionId, com.edumax.molip.domain.session.dto.request.SessionUpdateRequest request, Long memberId) {
    Session session = sessionRepository.findById(sessionId)
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 세션입니다. ID: " + sessionId));
    
    // 1. 권한 체크 (강사만 수정 가능)
    if (!session.getCourse().getLeaner().getMember().getId().equals(memberId)) {
      throw new IllegalStateException("해당 세션에 대한 수정 권한이 없습니다.");
    }

    // 2. 시작 시간 경과 체크 (이미 시작된 세션은(날짜/시간 모두 조합) 수정 불가)
    // 날짜 + 시간 조합으로 현재 시간과 비교
    java.time.LocalDateTime sessionStartDateTime = java.time.LocalDateTime.of(session.getDate(), session.getStartAt());
    if (java.time.LocalDateTime.now().isAfter(sessionStartDateTime)) {
      throw new IllegalStateException("이미 시작 시간이 지난 세션은 수정할 수 없습니다.");
    }

    // 3. 세션 정보 업데이트
    session.update(
        request.getTitle(),
        request.getDescription(),
        request.getDate(),
        request.getStartAt(),
        request.getEndAt(),
        request.getUseFocus(),
        request.getUseEmotion(),
        request.getUseQuiz(),
        request.getQuizCount()
    );
    
    // 4. 퀴즈 정보 업데이트 (기존 퀴즈 모두 삭제 후 재생성)
    // request.getQuizzes() 가 null이면 퀴즈 수정은 없다는 뜻으로 간주하거나, 
    // 빈 리스트면 퀴즈를 모두 지우는 것으로 간주할 수 있음.
    // 여기서는 request.getQuizzes() != null 일 때만 퀴즈 업데이트를 수행하도록 함.
    if (request.getQuizzes() != null) {
      // 4-1. 기존 퀴즈 삭제 (Cascade설정이 있다면 부모만 지워도 되지만, 안전하게 answer부터 삭제 고려)
      // fetch join이 안된 상태이므로 exitQuizzes 접근 시 select 쿼리 나감
      for (ExitQuiz quiz : session.getExitQuizzes()) {
          // answer 삭제 (CascadeType.ALL + orphanRemoval이 아니므로 수동 삭제)
          // 하지만 ExitQuiz Entity에 Cascade 설정이 없으므로 Repository로 지워야 함
          exitQuizAnswerRepository.deleteAll(quiz.getExitQuizAnswers());
      }
      exitQuizRepository.deleteAll(session.getExitQuizzes());
      
      // 세션 엔티티의 컬렉션도 비워줌 (1차 캐시 동기화)
      session.getExitQuizzes().clear();

      // 4-2. 새 퀴즈 생성
      if (Boolean.TRUE.equals(request.getUseQuiz())) {
          createQuizzes(session, request.getQuizzes());
      }
    }
  }

  @Override
  @Transactional
  public void deleteSession(Long sessionId, Long memberId) {
    Session session = sessionRepository.findById(sessionId)
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 세션입니다. ID: " + sessionId));

    // 1. 권한 체크 (강사만 삭제 가능)
    if (!session.getCourse().getLeaner().getMember().getId().equals(memberId)) {
      throw new IllegalStateException("해당 세션에 대한 삭제 권한이 없습니다.");
    }

    // 2. 연관 데이터 삭제 (Cascade 설정이 없거나 미흡할 경우를 대비해 수동 삭제)
    // 2-1. 퀴즈 및 보기 삭제
    for (ExitQuiz quiz : session.getExitQuizzes()) {
      exitQuizAnswerRepository.deleteAll(quiz.getExitQuizAnswers());
    }
    exitQuizRepository.deleteAll(session.getExitQuizzes());

    // 2-2. 세션 자체 삭제
    sessionRepository.delete(session);
  }

  @Override
  public SessionSimpleResponse getSessionSimple(Long sessionId, Long memberId) {
    Session session = sessionRepository.findById(sessionId)
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 세션입니다. ID: " + sessionId));

    validateSessionAccess(session, memberId);
    
    return SessionSimpleResponse.from(session);
  }

  @Override
  public SessionDetailResponse getSessionDetail(Long sessionId, Long memberId) {
    Session session = sessionRepository.findById(sessionId)
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 세션입니다. ID: " + sessionId));

    validateSessionAccess(session, memberId);

    // ExitQuiz와 Answer는 Fetch Join이나 Batch Size로 최적화가 필요할 수 있으나, 
    // 현재는 간단히 Lazy Loading을 통해 가져오도록 처리 (엔티티 그래프 내에서 조회됨)
    List<ExitQuiz> exitQuizzes = session.getExitQuizzes();
    
    return SessionDetailResponse.of(session, exitQuizzes);
  }

  private void validateSessionAccess(Session session, Long memberId) {
    Course course = session.getCourse();
    
    // 1. 강사인지 확인
    boolean isInstructor = course.getLeaner().getMember().getId().equals(memberId);
    if (isInstructor) {
      return;
    }

    // 2. 수강생인지 확인 (수강신청 후 승인된 상태)
    boolean isStudent = courseApplyRepository.existsByCourse_CourseIdAndWorkspaceMember_Member_IdAndIsAppliedTrue(
        course.getCourseId(), memberId);
    
    if (!isStudent) {
      throw new IllegalStateException("해당 세션을 조회할 권한이 없습니다. (강사 또는 승인된 수강생만 가능)");
    }
  }

  private void createQuizzes(Session session, List<QuizDto> quizDtos) {
    for (QuizDto quizDto : quizDtos) {
      ExitQuiz exitQuiz = ExitQuiz.builder()
          .session(session)
          .question(quizDto.getQuestion())
          .answerNumber(quizDto.getAnswerNumber())
          .build();
      
      ExitQuiz savedQuiz = exitQuizRepository.save(exitQuiz);
      
      // 보기 생성
      // 이전에 List<String> 일 때는 index+1로 번호를 매겼으나
      // 지금은 List<ChoiceDto> 로 들어오므로, DTO의 number와 content를 사용
      if (quizDto.getChoices() != null) {
        for (SessionCreateRequest.ChoiceDto choiceDto : quizDto.getChoices()) {
          ExitQuizAnswer answer = ExitQuizAnswer.builder()
              .exitQuiz(savedQuiz)
              .number(choiceDto.getNumber())
              .answer(choiceDto.getContent())
              .build();
          exitQuizAnswerRepository.save(answer);
        }
      }
    }
  }
}
