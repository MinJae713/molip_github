from app.grpc import generated as pb2
from app.grpc.servicers import (
    ConnectionServicer,
    STTServicer,
    QuizServicer,
    QnaServicer,
    EmotionServicer
)

def register_servicers(server):
    """모든 gRPC 서비스를 서버에 등록합니다."""
    pb2.connection_test_pb2_grpc.add_ConnectionServiceServicer_to_server(ConnectionServicer(), server)
    pb2.stt_pb2_grpc.add_STTServiceServicer_to_server(STTServicer(), server)
    pb2.quiz_pb2_grpc.add_QuizServiceServicer_to_server(QuizServicer(), server)
    pb2.qna_pb2_grpc.add_QnaServiceServicer_to_server(QnaServicer(), server)
    pb2.emotion_pb2_grpc.add_EmotionServiceServicer_to_server(EmotionServicer(), server)