@app.get("/api/health")
def health_check():
    """서버 상태 확인용 엔드포인트"""
    return {"status": "healthy", "service": "AI Server"}