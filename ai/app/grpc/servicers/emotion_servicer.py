from app.grpc.generated import emotion_pb2, emotion_pb2_grpc
from app.services.emotion_service import EmotionService
from app.core.logger import log
import traceback

class EmotionServicer(emotion_pb2_grpc.EmotionServiceServicer):
    def __init__(self):
        self.emotion_service = EmotionService()

    # stream 구조
    async def AnalyzeEmotion(self, request_iterator, context):
        log.info("🚀 [EmotionServicer] 감정 분석 스트림 연결됨")

        async for request in request_iterator:
            try:
                # Service 호출
                result = await self.emotion_service.analyze_emotion(request.image_data)
                if result:
                    # 감정 점수 딕셔너리 가져오기
                    emotion_dict = result['emotion']
                    # 점수순 정렬 (내림차순)
                    sorted_emotions = sorted(emotion_dict.items(), key=lambda item: item[1], reverse=True)
                    
                    # 상위 3개 추출 (부족할 경우 대비)
                    top3 = sorted_emotions[:3]
                    while len(top3) < 3:
                        top3.append(("none", 0.0))

                    yield emotion_pb2.EmotionResponse(
                        emotion1=top3[0][0],
                        confidence1=float(top3[0][1]),
                        emotion2=top3[1][0],
                        confidence2=float(top3[1][1]),
                        emotion3=top3[2][0],
                        confidence3=float(top3[2][1]),
                        timeline=request.timeline
                    )
                    # 6. 결과 확인
                    log.debug("\n" + "="*30)
                    log.debug("📊 [Emotion 분석 결과]")
                    log.debug(f"👤 학생 ID: {request.member_id}")
                    log.debug(f"🥇 1순위: {top3[0][0]} ({float(top3[0][1])}%)")
                    log.debug(f"🥈 2순위: {top3[1][0]} ({float(top3[1][1])}%)")
                    log.debug(f"🥉 3순위: {top3[2][0]} ({float(top3[2][1])}%)")
                    log.debug(f"time: {request.timeline}")
                    log.debug("="*30)

                else:
                    yield emotion_pb2.EmotionResponse(
                        emotion1="unknown", confidence1=0.0,
                        emotion2="unknown", confidence2=0.0,
                        emotion3="unknown", confidence3=0.0,
                        timeline=request.timeline
                    )

            except Exception as e:
                log.error(f"❌ [EmotionServicer] 스트림 처리 중 에러: {e}")
                log.debug(traceback.format_exc())
                # 에러 발생 시 스트림을 종료거나 기본값 전송
                yield emotion_pb2.EmotionResponse(
                    emotion1="error", confidence1=0.0,
                    emotion2="error", confidence2=0.0,
                    emotion3="error", confidence3=0.0,
                    timeline=request.timeline
                )