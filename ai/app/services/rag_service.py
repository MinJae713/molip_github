from typing import Optional
import re
from app.models.model_loader import models
from app.schemas.quiz_schema import QuizSchema
from app.schemas.qna_schema import QnaSchema
from langchain_core.prompts import ChatPromptTemplate
from app.db.chroma_client import get_vectorstore
from app.core.logger import log


class RAGService:
    def __init__(self):
        self.vector_store = get_vectorstore()
        self.llm = models.get_chat_model("gpt-4o-mini")

        # 용도별 구조화된 LLM 미리 생성
        self.quiz_llm = self.llm.with_structured_output(QuizSchema)
        self.qna_llm = self.llm.with_structured_output(QnaSchema)

        self.structured_llm = self.llm.with_structured_output(QuizSchema)

    async def generate_quiz(self, stt_text: str):
        """
        [Generation Only] Spring Boot로부터 받은 텍스트를 바탕으로 즉시 퀴즈 생성
        """
        prompt = ChatPromptTemplate.from_messages([
            ("system", "너는 강의 내용을 기반으로 핵심을 찌르는 퀴즈를 만드는 교육 전문가야."),
            ("user", "다음 내용을 바탕으로 객관식 퀴즈를 1개를 만들어줘:\n\n{text}")
        ])

        chain = prompt | self.quiz_llm

        try:
            result = await chain.ainvoke({"text": stt_text})

            # [Logic Fix] 인덱스 검증 및 보정
            # LLM이 뱉은 answer_idx가 의도와 다를 수 있으므로, correct_answer_text를 기준으로 재계산
            if hasattr(result, "correct_answer_text") and result.correct_answer_text:
                try:
                    # 텍스트 기반으로 인덱스 찾기 (1-based index)
                    # 만약 정확히 일치하는게 2개 이상이면 첫 번째 것 기준, 없으면 기존 index 유지
                    real_idx = result.options.index(result.correct_answer_text) + 1
                    if real_idx != result.answer_idx:
                        log.warning(f"[Correction] 정답 인덱스 보정: {result.answer_idx} -> {real_idx} (정답: {result.correct_answer_text})")
                        result.answer_idx = real_idx
                except ValueError:
                    log.warning(f"[Warning] 정답 텍스트({result.correct_answer_text})가 보기 목록에 없음. 기존 인덱스({result.answer_idx}) 유지.")
            
            return result
        except Exception as e:
            log.error(f"퀴즈 생성 오류: {e}")
            return None

    async def generate_qna(self, question: str, session_id: str) -> Optional[QnaSchema]:
        """
        [Generation Only] Spring Boot로부터 받은 질문을 바탕으로 답변 생성
        """

        # VectorDB 검색을 통해 질문에 해당하는 텍스트를 가져옴
        context = await self._retrieve_context(question, session_id)

        if not context:
            return QnaSchema(question=question, answer="관련 내용을 강의에서 찾을 수 없습니다.")

        prompt = ChatPromptTemplate.from_messages([
            ("system", "너는 강의 내용을 기반으로 답변하는 보조 강사야. 제공된 context만을 사용해 답변해줘"),
            ("user", f"Context: {context}\n\nQuestion: {question}")
        ])

        chain = prompt | self.qna_llm
        try:
            return await chain.ainvoke({})
        except Exception as e:
            log.error(f"Q&A 생성 실패: {e}")
            return None

    async def _retrieve_context(self, question: str, session_id: str) -> str:
        """
        질문을 분석해 시간 기반 또는 유사도 기반으로 컨텍스트 추출
        """

        # 질문에서 시간 키워드 추출
        target_seconds = self._extract_time_keyword(question)

        if target_seconds is not None:
            # 시간 기반 탐색
            # 특정시간 앞뒤 30초 정도의 문맥을 가져옴
            margin = 30
            search_filter = {
                "$and": [
                    {"session_id": {"$eq": session_id}},
                    {"timestamp": {"$gte": target_seconds - margin}},
                    {"timestamp": {"$lte": target_seconds + margin}},
                ]
            }

            # 시간 필터링을 우선하고, 그 안에서 질문과 가장 관련 있는 내용을 뽑는다.
            docs = await self.vector_store.asimilarity_search(
                query=question,
                k=5,
                filter=search_filter
            )

            log.debug(f"시간 기반 검색 실행: {target_seconds}초 지점 (범위: ±{margin}초)")
        else:
            # 일반 유사도 기반 검색
            # 현재 세션 안에서만 유사한 내용 탐색
            search_filter = {"session_id": {"$eq": session_id}}
            docs = await self.vector_store.asimilarity_search(
                query=question,
                k=5,
                filter=search_filter
            )
            log.debug(f"유사도 기반 검색 실행")
        
        # 검색 결과 병합
        if not docs:
            return ""

        # 검색된 문서들을 시간순으로 정렬하면 LLM이 이해하기 더 좋다
        sorted_docs = sorted(docs, key=lambda x: x.metadata.get("timestamp", 0))
        context_text = "\n".join(f"[{d.metadata.get('timestamp')}초] {d.page_content}" for d in sorted_docs)

        return context_text

    def _extract_time_keyword(self, text: str) -> Optional[int]:
        """
        문장에서 'N분 M초' 형태를 찾아 초 단위 정수로 변환
        """
        # "15분", "1분 30초", "90초" 등 대응
        minute_pattern = re.search(r'(\d+)\s*분', text)
        second_pattern = re.search(r'(\d+)\s*초', text)

        if not minute_pattern and not second_pattern:
            return None
        
        minutes = int(minute_pattern.group(1)) if minute_pattern else 0
        seconds = int(second_pattern.group(1)) if second_pattern else 0

        return (minutes * 60) + seconds