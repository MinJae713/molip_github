package com.edumax.molip.domain.course.entity;

import com.edumax.molip.domain.session.entity.CourseLearner;
import com.edumax.molip.domain.session.entity.Session;
import com.edumax.molip.domain.workspace.entity.WorkspaceMember;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "Course")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "course_id", nullable = false)
  private Long courseId;

  @Column(nullable = false, length = 100)
  private String title;

  @Column(nullable = false, length = 255)
  private String description;

  @Column(name = "sub_category", nullable = false, length = 20)
  private String subCategory;

  @Column(name = "max_learner", nullable = false)
  private Integer maxLearner;

  @Column(name = "is_engage_check", nullable = false)
  private Boolean isEngageCheck;

  @Column(name = "is_sentiment_analysis", nullable = false)
  private Boolean isSentimentAnalysis;

  @Column(nullable = false)
  private Integer attendance;

  private Integer engagement;

  private Integer exitQuiz;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private CourseStatus status;

  public enum CourseStatus {
    PREPARING,
    INVITE,
    PROCEED,
    FINISH
  }

  @Column(nullable = false)
  private Integer period; // 삭제될 수도 있음

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @ManyToOne
  @JoinColumn(name = "workspace_member_id", nullable = false)
  private WorkspaceMember leaner; // 테이블엔 workspace_member_id로 반영됨

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 50)
  private CourseCategory category;

  public enum CourseCategory {
    MARKETING // 보이는 것이 일단 하나라 이거만 넣었습니다..!
  }

  /** 수강 신청 */
  @OneToMany(mappedBy = "course")
  @Builder.Default
  private List<CourseApply> courseApplies = new ArrayList<>();

  /** 강의 찜 */
  @OneToMany(mappedBy = "course")
  @Builder.Default
  private List<Zzim> zzims = new ArrayList<>();

  /** 강의 수강생 */
  @OneToMany(mappedBy = "course")
  @Builder.Default
  private List<CourseLearner> courseLearners = new ArrayList<>();

  /** 강의 수료 */
  @OneToMany(mappedBy = "course")
  @Builder.Default
  private List<CourseCertificated> courseCertificateds = new ArrayList<>();

  /** 강의 세션 */
  @OneToMany(mappedBy = "course")
  @Builder.Default
  private List<Session> sessions = new ArrayList<>();
}
