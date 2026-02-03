import asyncio
from app.models.model_loader import models
from app.core.config import settings
import logging

log = logging.getLogger(__name__)

async def test_openai():
    try:
        print(f"OpenAI API Key: {settings.OPENAI_API_KEY}")
        print(f"OpenAI Base URL: {settings.GPT_BASE_URL}")
        client = models.get_openai_client()
        print(f"OpenAI Client: {client}")
        response = await client.chat.completions.create(
            model="gpt-4o-mini",
            messages=[{"role": "user", "content": "Hello"}]
        )
        print("AsyncOpenAI Success:", response.choices[0].message.content)
    except Exception as e:
        print("AsyncOpenAI Error:", e)

    try:
        chat_model = models.get_chat_model()
        response = await chat_model.ainvoke("Hello")
        print("ChatOpenAI Success:", response.content)
    except Exception as e:
        print("ChatOpenAI Error:", e)

if __name__ == "__main__":
    asyncio.run(test_openai())
