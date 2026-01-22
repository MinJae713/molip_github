package com.edumax.molip.domain.review.entity;

import com.edumax.molip.domain.chatting.entity.ReviewChattingRoom;
import com.edumax.molip.domain.session.entity.Session;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "LeanerAnalysis")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LearnerAnalysis {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "session_id", nullable = false)
  private Long sessionId;

  /** PK = FK */
  @MapsId
  @OneToOne
  @JoinColumn(name = "session_id", nullable = false)
  private Session session;

  @Column(name = "video_url", length = 255)
  private String videoUrl;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  /** AI 데드존 퀴즈 */
  @OneToMany(mappedBy = "learnerAnalysis")
  private List<AiDeadzoneQuiz> aiDeadzoneQuizzes;

  /** 강의 스크립트 */
  @OneToMany(mappedBy = "learnerAnalysis")
  private List<Script> scripts;

  /** 분위기 그래프 */
  @OneToMany(mappedBy = "learnerAnalysis")
  private List<FeelingGraph> feelingGraphs;

  /** AI 분석 타임라인 */
  @OneToMany(mappedBy = "learnerAnalysis")
  private List<AiAnalysisTimeline> aiAnalysisTimelines;

  /** 복습 채팅방 */
  @OneToMany(mappedBy = "learnerAnalysis")
  private List<ReviewChattingRoom> reviewChattingRooms;
}
