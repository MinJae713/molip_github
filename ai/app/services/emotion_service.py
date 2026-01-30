import cv2
import numpy as np
import asyncio
from deepface import DeepFace
from app.core.logger import log

class EmotionService:
    def __init__(self):
        # 서버 시작시 모델 미리 로드
        try:
            # 'facial_attribute' 태그를 사용하면 Emotion 모델을 정확히 찾아 로드합니다.
            self.model = DeepFace.build_model(model_name="Emotion", task="facial_attribute")
            log.info("✅ [EmotionService] 감정 분석 모델 로드 완료")
        except Exception as e:
            # 만약 위 코드가 실패하면, DeepFace가 내부적으로 사용하는 원본 build_model 호출
            from deepface.modules import modeling
            self.model = modeling.build_model(model_name="Emotion", task="facial_attribute")
            log.info(f"✅ [EmotionService] 백업 경로를 통해 모델 로드 완료")

    async def analyze_emotion(self, image_data: bytes):
        # 1. 이미지 디코딩 (I/O 병목 방지를 위해 별도 스레드 권장)
        loop = asyncio.get_event_loop()
        img = await loop.run_in_executor(None, self._decode, image_data)

        if img is None:
            return None
            
        # 2. DeepFace 추론 (CPU/GPU 집중 연산)
        # 30명이 동시에 들어와도 여기서 스레드 풀만큼 병렬로 돌아감
        
        try:
            result = await asyncio.to_thread(
                DeepFace.analyze,
                img_path=img,
                actions=['emotion'],
                enforce_detection=False,
                detector_backend='opencv'
            )

            return result[0] if isinstance(result, list) else result
        except Exception as e:
            log.error(f"❌ [EmotionService] 분석 에러: {e}")
            return None

    def _decode(self, data):
        nparr = np.frombuffer(data, np.uint8)
        return cv2.imdecode(nparr, cv2.IMREAD_COLOR)