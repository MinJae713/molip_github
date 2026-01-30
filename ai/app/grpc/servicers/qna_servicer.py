from app.grpc.generated import qna_pb2, qna_pb2_grpc
from app.services.qna_service import QnaService
from app.core.logger import log
from datetime import datetime

class QnaServicer(qna_pb2_grpc.QnaServiceServicer):
    def __init__(self):
        self.qna_service = QnaService()

    async def createQna(self, request, context):
        current_time = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
        log.info(f"💡 [QnaServicer] Q&A 생성 요청 수신 {current_time}")
        log.debug(f"📦 [수신] session: {request.session_id}, member: {request.member_id}, question: {request.question}")

        # 1. 엔진에게 Qna 생성 요청
        qna_result = await self.qna_service.create_qna_logic(
            request.session_id, request.member_id, request.question
        )

        end_time = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
        log.info(f"💡 [QnaServicer] Q&A 생성 요청 완료 {end_time}")
        if qna_result:
            log.debug(qna_result.question)
            log.debug(qna_result.answer)

            return qna_pb2.QnaResponse(
                session_id=request.session_id,
                member_id=request.member_id,
                question=request.question,
                answer=qna_result.answer
            )

        return qna_pb2.QnaResponse()