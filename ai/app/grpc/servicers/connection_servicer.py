from app.grpc.generated import connection_test_pb2, connection_test_pb2_grpc
from app.core.logger import log

# gRPC Servicer 설정
class ConnectionServicer(connection_test_pb2_grpc.ConnectionServiceServicer):
    def Ping(self, request, context):
        log.info(f"수신 메시지: {request.message}")
        return connection_test_pb2.PongResponse(
            reply=f"FastAPI 응답: {request.message}",
            status=True
        )