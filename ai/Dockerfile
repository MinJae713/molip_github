# 베이스 이미지
FROM python:3.12-slim

# 필수 시스템 패키지 설치
RUN apt-get update && apt-get install -y \
    ffmpeg \
    libgl1 \
    libglib2.0-0 \
    && rm -rf /var/lib/apt/lists/*

# 작업 디렉토리 설정
WORKDIR /app

# 종속성 파일 복사 및 설치
COPY requirements.txt .
RUN pip install --no-cache-dir --upgrade pip && \
    pip install --no-cache-dir -r requirements.txt

# 소스 코드 복사
COPY . .

# [중요] gRPC 코드 생성 및 경로 문제 해결
# 1. 생성 디렉토리 보장
# 2. 프로토 생성 (생성된 파일 내부의 import 경로 문제를 방지하기 위해 PYTHONPATH 설정과 병행)
RUN mkdir -p ./app/grpc/generated && \
    touch ./app/grpc/generated/__init__.py && \
    python -m grpc_tools.protoc -I./protos \
    --python_out=./app/grpc/generated \
    --grpc_python_out=./app/grpc/generated \
    ./protos/*.proto

# 생성된 파일 내부에서 서로를 찾을 수 있도록 환경 변수 설정
ENV PYTHONPATH=/app:/app/app/grpc/generated

# 포트 열기
EXPOSE 8000
EXPOSE 50051

# FastAPI 서버 실행
# main.py 하단에 uvicorn.run 설정이 이미 있지만, 도커 CMD에서는 직접 명시하는 것이 안정적입니다.
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000", "--proxy-headers", "--forwarded-allow-ips", "*"]