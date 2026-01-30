# 🎓 molip AI Server

실시간 스트리밍 강의를 위한 **학생 몰입도 분석 및 RAG 서버**입니다.
Spring Boot 서버로부터 수신한 음성 및 시각 데이터를 분석하여 학생의 상태(몰입 및 여러 감정표현 등)를 실시간으로 분석하고, 강의 내용을 STT로 변환하여 지식 베이스(Vector DB)에 저장한 후 SpringBoot 서버로 전송합니다.

또한 SpringBoot 서버로부터 LLM 추론을 위한 Prompt Query(QnA, 문제생성 관련 등)을 받아 LLM을 통해 추론한 결과를 SpringBoot 서버로 전송합니다.

## 🚀 주요 기능

* **실시간 상태 분석**: MediaPipe(Face, Iris, Pose)를 활용한 학생의 몰입도 실시간 추론.
* **비동기 STT & 임베딩**: OpenAI Whisper를 통한 음성 처리 및 Chroma DB 기반 벡터 저장.
* **고성능 통신**: gRPC Bidirectional Streaming을 통한 SpringBoot 서버와의 저지연(Low-latency) 데이터 교환.
* **병렬 처리**: `asyncio`와 `ThreadPoolExecutor`를 활용한 실시간 연산 및 무거운 AI 모델 추론의 병렬화.
* **LLM 추론**: RAG 및 LangChain 기반 OpenAI LLM을 통한 QnA, 문제생성 등 추론.

---

## 📂 프로젝트 구조 (Folder Structure)

```text
app/
├── main.py              # FastAPI 엔트리 포인트 및 서버 실행 설정
├── api/                 # REST API 엔드포인트 (v1)
├── core/                # 공통 설정 (환경 변수, 보안 상수)
├── grpc/                # gRPC 관련 라이브러리 및 서비스 구현
│   ├── generated/       # protoc로 생성된 파이썬 파일들 (.pb2, .pb2_grpc)
│   └── services.py      # AnalyticsServicer (gRPC 비즈니스 로직)
├── services/            # 도메인 로직 (AI 모델 추론)
│   ├── analyzer.py      # MediaPipe 기반 상태 분석 로직
│   ├── stt_service.py   # Whisper 기반 STT 및 LLM 처리
│   └── embedding.py     # Chroma DB 데이터 저장 및 조회
├── models/              # AI 모델 로더 및 스키마
│   ├── model_loader.py  # 서버 시작 시 모델 싱글톤 로드
│   └── schemas.py       # Pydantic 데이터 검증 모델
└── db/                  # 데이터베이스 연결 설정
    └── chroma_client.py # Chroma DB 클라이언트 구성

```

### 폴더별 상세 설명

| 폴더/파일명 | 설명 |
| --- | --- |
| **`app/main.py`** | FastAPI 앱 객체 생성 및 gRPC 서버 비동기 실행 제어 |
| **`app/grpc/`** | gRPC 통신 인터페이스 정의 및 클라이언트 요청 핸들링 |
| **`app/services/`** | **실시간 연산(MediaPipe)** 및 **비동기 연산(Whisper)** 핵심 로직 |
| **`app/models/`** | 대용량 AI 모델의 효율적인 메모리 관리 및 전역 공유 설정 |
| **`app/db/`** | Chroma DB 인스턴스 관리 및 벡터 데이터 관리 로직 |
| **`protos/`** | 통신 규약인 `.proto` 파일 원본 보관소 |

---

## 🛠️ 시작하기 (Getting Started)

### 1. 가상환경 구축 (Miniforge)

Python 3.12 환경을 권장합니다. (for miniforge3)

```bash
conda create -n ai-server python=3.12 -y
conda activate ai-server
conda install -c conda-forge ffmpeg sqlite -y

```

### 2. 의존성 설치

```bash
pip install --upgrade pip
pip install -r requirements.txt

```

### 3. gRPC 코드 생성

`.proto` 정의 파일을 파이썬 코드로 컴파일합니다.

```bash
python -m grpc_tools.protoc -I./protos --python_out=./app/grpc/generated --grpc_python_out=./app/grpc/generated ./protos/analytics.proto

```

### 4. 환경 변수 설정

루트 디렉토리에 `.env` 파일을 생성하고 필요한 키를 입력합니다.

```text
OPENAI_API_KEY=your_api_key_here
CHROMA_DB_PATH=./chroma_db

```

---

## 🏃 실행 (Execution)

서버를 실행하면 FastAPI(8000)와 gRPC(50051)가 동시에 가동됩니다.

```bash
# python app/main.py

uvicorn app.main:app [--host] [--port] --reload

```

* **FastAPI**: `http://localhost:8000/docs` (Swagger UI)
* **gRPC**: `localhost:50051`

---

## 🏗️ 아키텍처 다이어그램 (Architecture)

> **Note**: 본 서버는 `Double Stream` 전략을 사용합니다. 실시간 상태 분석(Fast Track)은 즉시 응답하며, STT 및 벡터 저장은 백그라운드 태스크(Slow Track)로 처리되어 사용자 경험을 극대화합니다.