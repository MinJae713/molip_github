package com.edumax.molip.domain.session.entity;

import com.edumax.molip.domain.chatting.entity.QnARoom;
import com.edumax.molip.domain.chatting.entity.SessionChattingRoom;
import com.edumax.molip.domain.course.entity.Course;
import com.edumax.molip.domain.course.entity.SessionAttachment;
import com.edumax.molip.domain.quiz.entity.ExitQuiz;
import com.edumax.molip.domain.review.entity.LearnerAnalysis;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "Session")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Session {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "session_id", nullable = false)
  private Long sessionId;

  @Column(nullable = false, length = 50)
  private String title;

  @Column(length = 255)
  private String description;


  @Column(name = "start_at", nullable = false)
  private LocalTime startAt;

  @Column(nullable = false)
  private LocalDate date;

  @Column(name = "end_at", nullable = false)
  private LocalTime endAt;
  @Column(name = "use_focus", nullable = false)
  private Boolean useFocus = false;

  @Column(name = "use_emotion", nullable = false)
  private Boolean useEmotion = false;

  @Column(name = "use_quiz", nullable = false)
  private Boolean useQuiz = false;

  @Column(name = "quiz_count", nullable = false)
  private Integer quizCount = 0;
  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "janus_room_id")
  private Long janusRoomId;

  @Column(length = 255)
  private String pin;

  @ManyToOne
  @JoinColumn(name = "course_id", nullable = false)
  private Course course;

  /* =======================
   * 1 : N
   * ======================= */

  /** 세션별 수강생 이수 정보 */
  @OneToMany(mappedBy = "session")
  @Builder.Default
  private List<CourseLearnerSession> courseLearnerSessions = new ArrayList<>();

  /** 세션 퇴실 퀴즈 */
  @OneToMany(mappedBy = "session")
  @Builder.Default
  private List<ExitQuiz> exitQuizzes = new ArrayList<>();

  @OneToMany(mappedBy = "session")
  @Builder.Default
  private List<SessionAttachment> sessionAttachments = new ArrayList<>();

  /* =======================
   * 1 : 1
   * ======================= */

  /** 세션 채팅방 */
  @OneToOne(mappedBy = "session")
  private SessionChattingRoom sessionChattingRoom;

  /** 세션 QnA 방 */
  @OneToOne(mappedBy = "session")
  private QnARoom qnaRoom;

  /** 세션 분석 결과 */
  @OneToOne(mappedBy = "session")
  private LearnerAnalysis learnerAnalysis;

  public void update(String title, String description, LocalDate date, LocalTime startAt,
      LocalTime endAt, Boolean useFocus, Boolean useEmotion, Boolean useQuiz, Integer quizCount) {
    if (title != null) this.title = title;
    if (description != null) this.description = description;
    if (date != null) this.date = date;
    if (startAt != null) this.startAt = startAt;
    if (endAt != null) this.endAt = endAt;
    if (useFocus != null) this.useFocus = useFocus;
    if (useEmotion != null) this.useEmotion = useEmotion;
    if (useQuiz != null) this.useQuiz = useQuiz;
    if (quizCount != null) this.quizCount = quizCount;
  }
}
