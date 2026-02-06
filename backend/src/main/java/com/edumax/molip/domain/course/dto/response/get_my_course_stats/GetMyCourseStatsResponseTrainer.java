package com.edumax.molip.domain.course.dto.response.get_my_course_stats;

import com.edumax.molip.domain.course.entity.CourseLearnerAnalysis;
import com.edumax.molip.domain.review.entity.LearnerAnalysis;
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
public class GetMyCourseStatsResponseTrainer extends GetMyCourseStatsResponse {
  private Float avgAttandance;
  private Float avgConcentrate;
  private Float avgQuizCorrect;
  private List<SessionAnalysisResponse> sessionAnalysis;
  private List<CourseLearnerAnalysisResponse> courseLearnerAnalysis;

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class SessionAnalysisResponse {
    private Long sessionId;
    private Float avgAttandance;
    private Float avgConcentrate;
    private Float avgQuizCorrect;

    public static SessionAnalysisResponse from(LearnerAnalysis learnerAnalysis) {
      Long sessionId = learnerAnalysis.getSessionId();
      Float avgAttandance = learnerAnalysis.getAvgAttendance();
      Float avgConcentrate = learnerAnalysis.getAvgConcentrate();
      Float avgQuizCorrect = learnerAnalysis.getAvgQuizCorrect();

      return SessionAnalysisResponse.builder()
          .sessionId(sessionId)
          .avgAttandance(avgAttandance)
          .avgConcentrate(avgConcentrate)
          .avgQuizCorrect(avgQuizCorrect)
          .build();
    }
  }

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class CourseLearnerAnalysisResponse {
    private Long memberId;
    private Long workspaceMemberId;
    private Long courseLearnerId;
    private Float sumConcentrate;
    private Integer sumAttendance;
    private Float sumQuizCorrect;

    public static CourseLearnerAnalysisResponse from(
        CourseLearnerAnalysis courseLearnerAnalysis, Long workspaceMemberId) {
      Long memberId = courseLearnerAnalysis.getMemberId();
      Long courseLearnerId = courseLearnerAnalysis.getCourseLearnerId();
      Float sumConcentrate = courseLearnerAnalysis.getSumConcentrate();
      Integer sumAttendance = courseLearnerAnalysis.getSumAttendance();
      Float sumQuizCorrect = courseLearnerAnalysis.getSumQuizCorrect();

      return CourseLearnerAnalysisResponse.builder()
          .memberId(memberId)
          .workspaceMemberId(workspaceMemberId)
          .courseLearnerId(courseLearnerId)
          .sumConcentrate(sumConcentrate)
          .sumAttendance(sumAttendance)
          .sumQuizCorrect(sumQuizCorrect)
          .build();
    }
  }
}
