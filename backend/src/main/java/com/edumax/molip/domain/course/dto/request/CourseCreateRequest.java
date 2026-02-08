package com.edumax.molip.domain.course.dto.request;

import com.edumax.molip.domain.course.types.CourseCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseCreateRequest {

  private Long workspaceId; // 워크스페이스 ID
  private String title;
  private String description;
  private String subCategory;
  private CourseCategory category; // 필요하다면 추가 (엔티티에 있음)

  private Integer maxLearner;
  private Integer period; // 수업 진행 주간

  // Options
  @Builder.Default private Boolean isEngageCheck = false;
  @Builder.Default private Boolean isSentimentAnalysis = false;

  // Completion Criteria (수료 조건)
  private Integer attendance; // 출석율
  private Integer engagement; // 집중도 (Optional)
  private Integer exitQuiz; // 퀴즈 점수 (Optional)
}
