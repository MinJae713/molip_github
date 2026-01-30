package com.edumax.molip.domain.course.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
public class ToggleZzimCourseRequest {

  @NotNull(message = "강의 아이디는 필수값입니다.")
  @Positive(message = "강의 아이디는 양수여야 합니다.")
  private Long courseId;

  @NotNull(message = "찜 여부는 필수값입니다.")
  private Boolean isZzim;
}
