from langchain_openai import ChatOpenAI
from openai import AsyncOpenAI
from app.core.config import settings

class ModelLoader:
    _instance = None

    def __new__(cls):
        if cls._instance is None:
            cls._instance = super(ModelLoader, cls).__new__(cls)
            cls._instance.openai_client = AsyncOpenAI(
                api_key=settings.OPENAI_API_KEY,
                base_url=settings.GPT_BASE_URL
            )
            
        return cls._instance

    def get_chat_model(self, model_name: str = "gpt-4o-mini", temperature: float = 0):
        return ChatOpenAI(
            model=model_name,
            temperature=temperature,
            openai_api_key=settings.OPENAI_API_KEY,
            base_url=settings.GPT_BASE_URL
        )

    def get_openai_client(self) -> AsyncOpenAI:
        return self.openai_client

models = ModelLoader()
