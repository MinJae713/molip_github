from pydantic import BaseModel, Field

class QnaSchema(BaseModel):
    question: str = Field(description="강의 내용 기반 질문")
    answer: str = Field(description="질문에 대한 강의 내용 기반 답변")