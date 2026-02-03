package com.edumax.molip.domain.course.dto.response;

import java.time.LocalDate;
import java.util.List;
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
public class GetMyCourseStatsResponse {
  private Integer totalLearningTime;
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
