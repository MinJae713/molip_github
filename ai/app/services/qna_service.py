from app.services.rag_service import RAGService

class QnaService:
    def __init__(self):
        self.rag_engine = RAGService()

    async def create_qna_logic(self, session_id: str, member_id: str, question: str):
        # 유효성 검사
        if not question:
            return None
        
        # RAG 생성로직 호출
        return await self.rag_engine.generate_qna(question, session_id)