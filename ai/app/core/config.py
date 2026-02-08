import os
import sys
import warnings
import logging
from dotenv import load_dotenv

# 설정값 상수화
class Settings:
    def __init__(self):    
        load_dotenv()
        self.PROJECT_NAME = "A309 AI Server"
        self.OPENAI_API_KEY = os.getenv("OPENAI_API_KEY", "")
        self.GRPC_PORT = os.getenv("GRPC_PORT", "50051")
        self.FASTAPI_PORT = int(os.getenv("FASTAPI_PORT", 8000))
        self.GPT_BASE_URL = os.getenv("GPT_BASE_URL", "")

# 시스템 초기화(TF 임포트 전 실행 필수)
def initialize_env():
    # 로그 제어
    os.environ['TF_CPP_MIN_LOG_LEVEL'] = '3'
    os.environ['TF_ENABLE_ONEDNN_OPTS'] = '0'
    os.environ['GRPC_VERBOSITY'] = 'ERROR'

    # 경고 무시
    warnings.filterwarnings("ignore", category=DeprecationWarning)
    warnings.filterwarnings("ignore", category=UserWarning)
    
    # 로깅 레벨 설정
    logging.basicConfig(level=logging.INFO)
    for logger_name in ['tensorflow', 'tf_keras', 'openai']:
        logging.getLogger(logger_name).setLevel(logging.ERROR)

# 경로 설정
CORE_DIR = os.path.dirname(os.path.abspath(__file__))
BASE_DIR = os.path.dirname(CORE_DIR) # ai/app/
ROOT_DIR = os.path.dirname(BASE_DIR) # ai/
GENERATED_PATH = os.path.join(BASE_DIR, "grpc", "generated")

if GENERATED_PATH not in sys.path:
    sys.path.append(GENERATED_PATH)

initialize_env() # 모듈 호출 시 즉시 실행
settings = Settings()