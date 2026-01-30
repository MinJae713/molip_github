from app.grpc.generated import quiz_pb2, quiz_pb2_grpc
from app.services.quiz_service import QuizService
from app.core.logger import log
from datetime import datetime  # 1. datetime 모듈 임포트

class QuizServicer(quiz_pb2_grpc.QuizServiceServicer):
    def __init__(self):
        # 엔진(Service)을 주입받거나 생성
        self.quiz_service = QuizService()

    async def createQuiz(self, request, context):
        current_time = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
        log.info(f"💡 [QuizServicer] 퀴즈 생성 요청 수신 {current_time}")
        log.debug(f"📦 [수신] session: {request.session_id}, sttText: {request.sttText}")

        # 1. 엔진에게 퀴즈 생성 요청
        quiz_result = await self.quiz_service.create_quiz_logic(
            request.session_id, request.sttText
        )

        end_time = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
        log.info(f"💡 [QuizServicer] 퀴즈 생성 요청 완료 {end_time}")
        
        if quiz_result:
            log.debug(quiz_result.question)
            log.debug(quiz_result.options)
            log.debug(quiz_result.answer_idx)
            log.debug(quiz_result.explanation)

            # 2. gRPC 규격(Proto)에 맞춰 포장해서 전송
            return quiz_pb2.QuizResponse(
                session_id=request.session_id,
                question=quiz_result.question,
                answer1=quiz_result.options[0],
                answer2=quiz_result.options[1],
                answer3=quiz_result.options[2],
                answer4=quiz_result.options[3],
                correct_answer=quiz_result.answer_idx,
                explanation=quiz_result.explanation
            )
        
        return quiz_pb2.QuizResponse()
