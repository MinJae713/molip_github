from app.grpc.generated import stt_pb2, stt_pb2_grpc
from app.services.stt_service import STTProcessingService
from app.core.logger import log
from datetime import datetime  # 1. datetime 모듈 임포트

class STTServicer(stt_pb2_grpc.STTServiceServicer):
    """
    비동기 스트리밍 처리 Servicer
    """
    async def sendSTT(self, request_iterator, context):
        """
        request_iterator: Spring Boot에서 10초마다 던지는 요청들의 스트림
        """
        log.info("🔔 [gRPC] 스트리밍 세션 시작")
        # 1. 연결 시 즉시 출력
        peer = context.peer()
        log.info(f"🤝 [NEW CONNECTION] Peer: {peer}")

        try:
            # 1. SpringBoot가 보내는 데이터를 비동기 반복문으로 수신
            async for request in request_iterator:
                log.debug(f"📦 [수신] session: {request.session_id}, timeline: {request.timeline}")

                # 2. 기존 STT 로직 호출 (비동기 서비스 await 가능)
                text, timeline = await STTProcessingService.process_audio_chunk(
                    timeline=request.timeline,
                    session_id=request.session_id,
                    audio_bytes=request.audio_data,
                )

                # 3. 결과가 나올 때마다 클라이언트로 즉시 전송(yield)
                yield stt_pb2.STTResponse(
                    text=text,
                    timeline=timeline,
                    status=True if text else False
                )
                log.debug(f"🚀 [전송] timeline {timeline} 처리 완료")

        except Exception as e:
            log.error(f"❌ [스트리밍 에러] {e}")
        finally:
            log.info("📴 [gRPC] 스트리밍 세션 종료")