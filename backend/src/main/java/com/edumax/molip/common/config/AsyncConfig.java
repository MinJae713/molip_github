package com.edumax.molip.common.config;

import java.util.concurrent.Executor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {
  @Bean
  public Executor saveSttExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(4); // 최소 쓰레드 개수
    executor.setMaxPoolSize(16); // 최대 쓰레드 개수
    executor.setQueueCapacity(1000); // broking 큐 사이즈
    executor.setThreadNamePrefix("quiz-stt-save-"); // 쓰레드 이름 접두사
    executor.initialize(); // 초기화
    return executor;
  }
}
