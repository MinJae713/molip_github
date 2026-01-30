import os
from langchain_chroma import Chroma
from langchain_openai import OpenAIEmbeddings
from app.core.config import settings, ROOT_DIR

def get_vectorstore(collection_name: str = "s14p11a309_rag"):
    persist_directory = os.path.join(ROOT_DIR, "chroma_db")
    
    embedding_function = OpenAIEmbeddings(
        model="text-embedding-3-small",
        openai_api_key=settings.OPENAI_API_KEY
    )

    vectorstore = Chroma(
        collection_name=collection_name,
        embedding_function=embedding_function,
        persist_directory=persist_directory
    )
    
    return vectorstore
