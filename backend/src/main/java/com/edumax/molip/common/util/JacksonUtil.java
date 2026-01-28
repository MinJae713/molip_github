package com.edumax.molip.common.util;

import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.datatype.jsr310.JavaTimeModule;

@Component
public class JacksonUtil {
  private static final JsonMapper jsonMapper =
      JsonMapper.builder().addModule(new JavaTimeModule()).build();

  public static String toJson(Object object) {
    try {
      return jsonMapper.writeValueAsString(object);
    } catch (JacksonException e) { // JsonProcessingException 대신 사용
      throw new RuntimeException("Jackson 3 직렬화 실패: " + e.getMessage(), e);
    }
  }

  public static <T> T fromJson(String json, Class<T> clazz) {
    try {
      return jsonMapper.readValue(json, clazz);
    } catch (JacksonException e) { // JsonProcessingException 대신 사용
      throw new RuntimeException("Jackson 3 역직렬화 실패: " + e.getMessage(), e);
    }
  }
}
