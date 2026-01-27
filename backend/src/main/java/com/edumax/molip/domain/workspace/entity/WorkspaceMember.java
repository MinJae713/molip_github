package com.edumax.molip.domain.workspace.entity;

import com.edumax.molip.domain.chatting.entity.QnAMessage;
import com.edumax.molip.domain.chatting.entity.ReviewChattingMessage;
import com.edumax.molip.domain.chatting.entity.ReviewChattingRoom;
import com.edumax.molip.domain.chatting.entity.SessionChattingMessage;
import com.edumax.molip.domain.course.entity.*;
import com.edumax.molip.domain.member.entity.Member;
import com.edumax.molip.domain.quiz.entity.ExitQuizResult;
import com.edumax.molip.domain.review.entity.*;
import com.edumax.molip.domain.session.entity.CourseLearner;
import com.edumax.molip.domain.workspace.types.WorkspaceMemberRole;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "WorkspaceMember")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkspaceMember {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "workspace_member_id", nullable = false)
  private Long workspaceMemberId;

  @Column(name = "connected_at")
  private LocalDateTime connectedAt;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private WorkspaceMemberStatus status;

  public enum WorkspaceMemberStatus {
    ACTIVE,
    INACTIVE
  }

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private WorkspaceMemberRole role;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  /* =======================
   * N : 1 관계
   * ======================= */

  @ManyToOne
  @JoinColumn(name = "member_id", nullable = false)
  private Member member;

  @ManyToOne
  @JoinColumn(name = "workspace_id", nullable = false)
  private Workspace workspace;

  /* =======================
   * 1 : N 관계
   * ======================= */

  @OneToMany(mappedBy = "workspaceMember")
  @Builder.Default
  private List<CourseApply> courseApplies = new ArrayList<>();

  @OneToMany(mappedBy = "leaner")
  @Builder.Default
  private List<Course> courses = new ArrayList<>();

  @OneToMany(mappedBy = "workspaceMember")
  @Builder.Default
  private List<Zzim> zzims = new ArrayList<>();

  @OneToMany(mappedBy = "workspaceMember")
  @Builder.Default
  private List<CourseLearner> courseLearners = new ArrayList<>();

  @OneToMany(mappedBy = "workspaceMember")
  @Builder.Default
  private List<SessionChattingMessage> sessionChattingMessages = new ArrayList<>();

  @OneToMany(mappedBy = "workspaceMember")
  @Builder.Default
  private List<QnAMessage> qnaMessages = new ArrayList<>();

  @OneToMany(mappedBy = "workspaceMember")
  @Builder.Default
  private List<ExitQuizResult> exitQuizResults = new ArrayList<>();

  @OneToMany(mappedBy = "workspaceMember")
  @Builder.Default
  private List<FeelingGraph> feelingGraphs = new ArrayList<>();

  @OneToMany(mappedBy = "workspaceMember")
  @Builder.Default
  private List<AiAnalysisTimeline> aiAnalysisTimelines = new ArrayList<>();

  @OneToMany(mappedBy = "workspaceMember")
  @Builder.Default
  private List<ReviewChattingRoom> reviewChattingRooms = new ArrayList<>();

  @OneToMany(mappedBy = "workspaceMember")
  @Builder.Default
  private List<ReviewChattingMessage> reviewChattingMessages = new ArrayList<>();
}
