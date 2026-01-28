package com.edumax.molip.domain.session.service;

import com.edumax.molip.domain.course.service.CourseService;
import com.edumax.molip.domain.session.handler.EmotionManager;
import com.edumax.molip.domain.session.handler.EmotionManagerRegistry;
import com.edumax.molip.domain.session.handler.EmotionSaveListener;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SessionServiceImpl implements CourseService {
  private final EmotionManagerRegistry emotionManagerRegistry;
  private final EmotionSaveListener emotionSaveListener;

  public void startCourse(String sessionId) {
    emotionSaveListener.startLecture(sessionId);
    EmotionManager emotionManager = emotionManagerRegistry.getManager(sessionId);
    emotionManager.startScheduling(0, sessionId);
  }

  public void endCourse(String sessionId) {
    emotionSaveListener.stopLecture(sessionId);
    emotionManagerRegistry.removeManager(sessionId);
  }
}
