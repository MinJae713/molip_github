package com.edumax.molip.domain.workspace.dto.response.workspace_courses;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class FindWorkspaceCoursesByIdTrainerResponse extends FindWorkspaceCoursesByIdResponse {
  private Boolean isOwn;
  private String trainerName;
  private Double progressRate;
  private LocalDate startDate;
  private Integer learnersCount;
}
