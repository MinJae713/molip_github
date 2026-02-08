from pydantic import BaseModel, Field
from typing import List

class QuizSchema(BaseModel):
    question: str = Field(description="강의 내용 기반 객관식 문제")
    options: List[str] = Field(description="보기 4개 (리스트 형태)", min_items=4, max_items=4)
    correct_answer_text: str = Field(description="정답 보기의 텍스트 (options 중 하나와 정확히 일치해야 함)")
    answer_idx: int = Field(description="정답 번호 (1-4)")
    explanation: str = Field(description="문제에 대한 상세 해설")