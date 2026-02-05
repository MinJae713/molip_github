package com.edumax.molip.common.config;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
@EnableAsync
@EnableScheduling
public class AsyncConfig {
  @Bean
  public Executor sttSaveExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(4); // 최소 쓰레드 개수
    executor.setMaxPoolSize(16); // 최대 쓰레드 개수
    executor.setQueueCapacity(100); // broking 큐 사이즈
    executor.setThreadNamePrefix("quiz-stt-save-"); // 쓰레드 이름 접두사
    // 큐가 가득 찼을 때, 이벤트를 버리지 않고 호출한 쓰레드(Main)가 직접 처리하게 함 (데이터 유실 방지)
    executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
    executor.initialize(); // 초기화
    return executor;
  }

  @Bean
  public Executor qnaMessageSaveExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(4); // 최소 쓰레드 개수
    executor.setMaxPoolSize(16); // 최대 쓰레드 개수
    executor.setQueueCapacity(100); // broking 큐 사이즈
    executor.setThreadNamePrefix("qna-message-save-"); // 쓰레드 이름 접두사
    // 큐가 가득 찼을 때, 이벤트를 버리지 않고 호출한 쓰레드(Main)가 직접 처리하게 함 (데이터 유실 방지)
    executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
    executor.initialize(); // 초기화
    return executor;
  }

  @Bean
  public Executor emotionSaveExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(10); // 최소 쓰레드 개수
    executor.setMaxPoolSize(40); // 최대 쓰레드 개수
    executor.setQueueCapacity(1000); // broking 큐 사이즈
    executor.setThreadNamePrefix("review-emotion-save-"); // 쓰레드 이름 접두사
    // 큐가 가득 찼을 때, 이벤트를 버리지 않고 호출한 쓰레드(Main)가 직접 처리하게 함 (데이터 유실 방지)
    executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
    executor.initialize(); // 초기화
    return executor;
  }

  @Bean
  public Executor concentrateSaveExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(10); // 최소 쓰레드 개수
    executor.setMaxPoolSize(40); // 최대 쓰레드 개수
    executor.setQueueCapacity(1000); // broking 큐 사이즈
    executor.setThreadNamePrefix("review-concentrate-save-"); // 쓰레드 이름 접두사
    // 큐가 가득 찼을 때, 이벤트를 버리지 않고 호출한 쓰레드(Main)가 직접 처리하게 함 (데이터 유실 방지)
    executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
    executor.initialize(); // 초기화
    return executor;
  }

  @Bean
  public ThreadPoolTaskScheduler taskScheduler() {
    ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
    scheduler.setPoolSize(5); // 동시에 실행할 스케줄러 쓰레드 개수
    scheduler.setThreadNamePrefix("emotion-scheduler-");
    scheduler.initialize();
    return scheduler;
  }
}
