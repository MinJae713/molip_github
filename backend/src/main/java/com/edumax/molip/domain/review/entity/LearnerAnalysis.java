package com.edumax.molip.domain.review.entity;

import com.edumax.molip.domain.chatting.entity.ReviewChattingRoom;
import com.edumax.molip.domain.session.entity.Session;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "LeanerAnalysis")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LearnerAnalysis {

  @Id
  @Column(name = "session_id", nullable = false)
  private Long sessionId;

  /** PK = FK */
  @MapsId
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "session_id", nullable = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  private Session session;

  @Column(name = "video_url", length = 255)
  private String videoUrl;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "total_learner", nullable = false)
  private Integer totalLearner;

  @Column(name = "total_analysis_time", nullable = false)
  private Integer totalAnalysisTime;

  @Column(name = "avg_attendance", nullable = false)
  private Float avgAttendance;

  @Column(name = "avg_quiz_correct", nullable = false)
  private Float avgQuizCorrect;

  @Column(name = "avg_concentrate", nullable = false)
  private Float avgConcentrate;

  @Column(name = "avg_emotion", nullable = false, length = 127)
  private String avgEmotion;

  /** AI 데드존 퀴즈 */
  @OneToMany(mappedBy = "learnerAnalysis")
  private List<AiDeadzoneQuiz> aiDeadzoneQuizzes;

  /** 강의 스크립트 */
  @OneToMany(mappedBy = "learnerAnalysis")
  private List<Script> scripts;

  /** 분위기 그래프 */
  @OneToMany(mappedBy = "learnerAnalysis")
  private List<EmotionGraph> emotionGraph;

  //  /** AI 분석 타임라인 */ -> MongoDB로 이전
  //  @OneToMany(mappedBy = "learnerAnalysis")
  //  private List<AiAnalysisTimeline> aiAnalysisTimelines;

  /** 복습 채팅방 */
  @OneToMany(mappedBy = "learnerAnalysis")
  private List<ReviewChattingRoom> reviewChattingRooms;
}
