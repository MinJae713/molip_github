# 베이스 이미지
FROM python:3.12-slim

# 필수 시스템 패키지 설치
RUN apt-get update && apt-get install -y \
    ffmpeg \
    libgl1-mesa-glx \
    libglib2.0-0 \
    && rm -rf /var/lib/apt/lists/*

# 작업 디렉토리 설정
WORKDIR /app

# 종속성 파일 복사 및 설치
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# 소스 코드 복사
COPY . .

# gRPC 코드 생성
RUN python -m grpc_tools.protoc -I./protos --python_out=./app/grpc/generated --grpc_python_out=./app/grpc/generated ./protos/*.proto

# 포트 열기
EXPOSE 8000
EXPOSE 50051

# FastAPI 서버 실행 (Uvicorn)
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]

