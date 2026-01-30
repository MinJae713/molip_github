from app.core.config import settings
from app.core.logger import log

# 비동기 처리
import asyncio
from contextlib import asynccontextmanager

import uvicorn
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

# gRPC
import grpc
from grpc import aio
from concurrent import futures

# STT
import openai
from openai import AsyncOpenAI

from app.grpc.server import register_servicers

# gRPC 서버 객체 생성 및 관리 함수
async def start_grpc_server():
    # grpc.aio.server 사용
    server = aio.server()

    register_servicers(server)

    listen_addr = f'0.0.0.0:{settings.GRPC_PORT}'
    server.add_insecure_port(listen_addr)
    await server.start()
    log.info(f"✅ [gRPC] 비동기 서버가 {listen_addr}에서 시작되었습니다.")
    return server

# FastAPI Lifespan 설정 (서버 시작/종료 로직)
@asynccontextmanager
async def lifespan(app: FastAPI):
    # [Startup] FastAPI가 시작될 때 gRPC 서버 실행
    grpc_server = await start_grpc_server()
    log.info("✅ gRPC 서버가 50051 포트에서 시작되었습니다.")

    yield # FastAPI 앱이 실행되는 지점

    # [Shutdown] FastAPI가 종료될 때 gRPC 서버 종료
    log.info("🛑 gRPC 서버를 정지합니다...")
    await grpc_server.stop(0)
    log.info("✅ gRPC 서버가 정지되었습니다.")


# FastAPI 서버 생성
app = FastAPI(
    title="molip AI Server",
    description="FastAPI + gRPC for Real-time Server",
    version="1.0.0",
    lifespan=lifespan
)

# CORS 설정
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

if __name__ == "__main__":
    # Uvicorn 서버 설정
    # reload=True: 개발 환경에서 코드 수정 시 자동 재시작을 위해 사용
    uvicorn.run(
        "main:app",
        host="0.0.0.0",
        port=settings.FASTAPI_PORT,
        reload=False,
        workers=1,
    )
