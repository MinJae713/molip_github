from app.services.rag_service import RAGService

class QuizService:
    def __init__(self):
        self.rag_engine = RAGService()

    async def create_quiz_logic(self, session_id: str, stt_text: str):
        # 유효성 검사
        if not stt_text or len(stt_text) < 20:
            return None
        
        # RAG 생성로직 호출
        return await self.rag_engine.generate_quiz(stt_text)