import sys
from loguru import logger
from app.core.config import settings

# 로그 포맷 설정
LOG_FORMAT = (
    "<green>{time:YYYY-MM-DD HH:mm:ss.SSS}</green> | "
    "<level>{level: <8}</level> | "
    "<cyan>{name}</cyan>:<cyan>{function}</cyan>:<cyan>{line}</cyan> - <level>{message}</level>"
)

def setup_logger():
    """로거 설정 초기화"""
    logger.remove()  # 기본 핸들러 제거
    
    # 콘솔 출력 (개발 환경)
    logger.add(
        sys.stderr,
        format=LOG_FORMAT,
        level="DEBUG",
        colorize=True
    )
    
    # 파일 출력 (선택 사항, 필요시 활성화)
    # logger.add("logs/app.log", rotation="500 MB", level="INFO")
    
    return logger

# 전역 로거 인스턴스
log = setup_logger()
