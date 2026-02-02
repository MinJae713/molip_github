package com.edumax.molip.domain.review.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TimelineElement {
  int timeline;
  boolean empty;
  float concentrateValue;
  boolean fever;
  float[] emotionValueList;
}
