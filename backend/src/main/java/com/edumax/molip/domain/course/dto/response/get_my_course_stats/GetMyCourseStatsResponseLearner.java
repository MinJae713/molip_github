package com.edumax.molip.domain.course.dto.response.get_my_course_stats;

import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class GetMyCourseStatsResponseLearner extends GetMyCourseStatsResponse {
  // private Integer totalLearningTime;
  private Integer attendRate;
  private Integer exitQuizAverage;
  private Integer concentrateMyAverage;
  private List<SessionRecord> sessionRecords;

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class SessionRecord {
    private Long sessionId;
    private Integer learningTime;
    private Integer concentrate;
    private Integer concentrateTotalAverage;
    private String title;
    private LocalDate date;
    private Boolean isAttendance;
    private Integer exitQuiz;
    private Boolean isProceed;
  }
}
