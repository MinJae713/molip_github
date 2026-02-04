<template>
    <div class="bg-slate-50 text-slate-900 h-screen flex flex-col overflow-hidden relative">
        <!-- Device Setup Modal -->
        <DeviceSetupModal v-if="!isSetupComplete" @join="handleJoinSession" />

        <!-- INFO: 임시 연결 종료 버튼 -->
        <button 
            @click="disconnectAll"
            class="absolute top-20 left-6 z-50 bg-red-600 text-white px-3 py-2 rounded-md shadow-lg text-sm font-bold hover:bg-red-700 transition-colors"
        >
            🔌 연결 끊기 (TEST)
        </button>

        <LiveSessionHeader sessionTitle="인공지능 윤리와 실제 실습" sessionSubtitle="디지털 비즈니스 전략 강의 (4주차)"
            :elapsedTime="elapsedTime" />
        <main class="flex flex-1 overflow-hidden p-6 gap-6 relative">
            <LiveVideoArea :focusStatus="focusStatus" :showWarning="showWarning" @close-warning="showWarning = false"
                :mainStream="videoStore.mainStream" :pipStream="videoStore.pipStream"
                :localStream="videoStore.localStream" :userRole="workspaceStore.currentWorkspace?.myRole">
                <template #controls>
                    <LiveControlDock :isMicOn="isMicOn" :isCameraOn="isCameraOn" :isHandRaised="isHandRaised"
                        @toggle-mic="toggleMic" @toggle-camera="toggleCamera" @raise-hand="raiseHand" />
                </template>
            </LiveVideoArea>

            <!-- 사이드바 -->
        <LiveSidebar>
                <!-- 라이브 퀴즈 관련 -->
                <LiveQuizCard title="LIVE QUIZ 진행중" subtitle="인공지능 윤리 가이드라인 핵심..." timeRemaining="00:15" />
                <!-- 참여도 -->
                <EngagementStats :engagementScore="engagementScore" :currentEmotion="currentEmotion"
                    :focusStatus="focusStatus" />
                <!-- 채팅/질문 -->
                <!-- 채팅/질문 -->
                <LiveChatContainer :roomId="(route.params.sessionId as string)" />
            </LiveSidebar>

            <!-- MediaPipe용 숨겨진 Video (Janus Stream 미러링) -->
            <video ref="mediaPipeVideo" class="hidden" autoplay playsinline muted></video>
        </main>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue';
import { useRoute } from 'vue-router';
import { useVideoRoomStore } from '@/stores/videoRoom';
import { useWorkspaceStore } from '@/stores/workspace';
import { useUserStore } from '@/stores/user';
import { MediaPipeManager } from '@/utils/mediapipe';
import type { GazeResult } from '@/utils/gazeTracker';
import SockJS from 'sockjs-client';
import Stomp from 'stompjs';

// Components
import LiveSessionHeader from '@/components/Learner/LearnerLiveSession/LiveSessionHeader.vue';
import LiveVideoArea from '@/components/Learner/LearnerLiveSession/LiveVideoArea.vue';
import LiveControlDock from '@/components/Learner/LearnerLiveSession/LiveControlDock.vue';
import LiveSidebar from '@/components/Learner/LearnerLiveSession/LiveSidebar.vue';
import LiveQuizCard from '@/components/Learner/LearnerLiveSession/LiveQuizCard.vue';
import EngagementStats from '@/components/Learner/LearnerLiveSession/EngagementStats.vue';
import LiveChatContainer from '@/components/Learner/LearnerLiveSession/LiveChatContainer.vue';
import DeviceSetupModal from '@/components/Learner/LearnerLiveSession/DeviceSetupModal.vue';

const route = useRoute();
const videoStore = useVideoRoomStore();
const workspaceStore = useWorkspaceStore();

// State
const elapsedTime = ref(0); // seconds (00:45:12)
const isMicOn = ref(false);
const isCameraOn = ref(false);
const isHandRaised = ref(false);
const showWarning = ref(true);
const engagementScore = ref(35);
const focusStatus = ref('집중도 낮음 (Warning)');
const activeTab = ref<'chat' | 'qa'>('qa');
const newMessage = ref('');
const isAnonymous = ref(false);


// 실시간 감정분석 이모지 라벨링
const currentEmotion = ref({
    emoji: '🤩',
    label: '흥미로움'
});
// Timer
let timerInterval: number | null = null;
const isSetupComplete = ref(false);

//================================================================================

// MediaPipe
const mediaPipeVideo = ref<HTMLVideoElement | null>(null);
let mediaPipeManager: MediaPipeManager | null = null;

// 연결 관련
const isAuthorized = ref(false);
const isConnected = ref(false); // [NEW] 연결 상태 관리
const base_url = import.meta.env.VITE_SOCKET_BASE_URL;
console.log(base_url);
const sessionId = route.params.sessionId;
const userStore = useUserStore();
const memberId = userStore.user.memberId;
const accessToken = localStorage.getItem('accessToken');


let stompClient: any = null;
let binarySocket: WebSocket | null = null;

// STOMP 연결 (결과 수신)
const connectStomp = () => {
    const socketUrl = `https://${base_url}/message`

    console.log(`[STOMP] Connecting to: ${socketUrl}`);
    const socket = new SockJS(socketUrl);
    stompClient = Stomp.over(socket);
    stompClient.debug = () => {}; // 디버그 로그 끄기
    
    stompClient.connect({ Authorization: `Bearer ${accessToken}` }, () => {
        console.log("STOMP Connected!");
        isConnected.value = true; // 연결 상태 업데이트
        // 결과 구독
        stompClient.subscribe(`/topic/analysis/${sessionId}/4`, (res: any) => {
            const result = JSON.parse(res.body);
            console.log("분석 결과 수신:", result);

            // 1. 강의 시작 시간 수신
            if (result.startTime) {
                console.log("강의 시작 시간:", result.startTime);
                // 필요한 경우 시작 시간 처리 로직 추가
            }

            // 2. 통합 분석 결과 수신 (TrainerSessionTest 구조 참조)
            console.log(`[Emotion Detail] 
                1위: ${result.emotion1} (${result.confidence1}%)
                2위: ${result.emotion2} (${result.confidence2}%)
                3위: ${result.emotion3} (${result.confidence3}%)`);

            // 3. 몰입도 분석 결과 수신 (concentrateValue 존재 시)
            if (result.concentrateValue !== undefined) {
                engagementScore.value = result.concentrateValue;

            }
        });
        connectBinarySocket();
        
    }, (error: any) => {
        console.error("STOMP Connection Error:", error);
    })
}

// Binary WebSocket 연결 (이미지 전송)
const connectBinarySocket = () => {
    // 쿼리 파라미터로 토큰 전송
    const url = `wss://i14a309.p.ssafy.io/emotion/${sessionId}?member-id=4`;
    console.log(`[Binary] Connecting to: ${url}`);

    try {
        binarySocket = new WebSocket(url);
        binarySocket.binaryType = "arraybuffer";

        binarySocket.onopen = () => {
            // JSON 형태로 토큰 전송
            console.log("Binary WebSocket connected!");
            // 인증절차 없이 데이터 전송 가능하도록 플래그 허용
            isAuthorized.value = true;
        }

        binarySocket.onerror = (error) => {
            console.error("Binary Socket Error:", error);
        };

        binarySocket.onclose = (event) => {
            console.log("Binary Socket Closed:", event.code, event.reason);
            isAuthorized.value = false;
        };

    } catch (error) {
        console.error("Failed to create WebSocket:", error);
    }
}

// Callbacks
const handleCropReady = (arrayBuffer: ArrayBuffer) => {
    console.log(`[MediaPipe] Cropped data ready: ${arrayBuffer.byteLength} bytes`);
    // TODO: Send to backend if needed
    const socket = binarySocket;
    if (!socket) {
        console.warn("[Debug] 소켓 객체가 아직 생성되지 않음");
        return;
    }

    // socket이 열려있고, 우리가 만든 isAuthorize가 true일 때 전송
    if (!socket || socket.readyState !== WebSocket.OPEN || !isAuthorized.value) return;

    const packet = new ArrayBuffer(4 + arrayBuffer.byteLength);
    const view = new DataView(packet);

    // 기존 HTML 방식과 동일하게 타임라인(elapsedTime)을 헤더에 삽입
    view.setFloat32(0, elapsedTime.value, false);

    const uint8View = new Uint8Array(packet, 4);
    uint8View.set(new Uint8Array(arrayBuffer));

    socket.send(packet);
    console.log(`[Sent] Timeline: ${elapsedTime.value}s, Size: ${packet.byteLength} bytes`);
};

const startMediaPipe = async () => {
    if (!videoStore.localStream || !mediaPipeVideo.value) return;

    if (!mediaPipeManager) {
        mediaPipeManager = new MediaPipeManager(
            mediaPipeVideo.value,
            handleCropReady,
            undefined, // internal canvas
            handleAnalysisResult
        );
        await mediaPipeManager.init();
    }

    try {
        console.log("[MediaPipe] Starting with Janus Local Stream...");
        await mediaPipeManager.start(videoStore.localStream);
    } catch (e) {
        console.error("[MediaPipe] Start failed", e);
    }
};

const stopMediaPipe = () => {
    if (mediaPipeManager) {
        mediaPipeManager.stop();
        mediaPipeManager = null;
    }
};

const disconnectAll = () => {
    console.log("🛑 Force disconnecting all sockets...");
    
    // MediaPipe 중지 (더 이상 데이터 생성 X)
    stopMediaPipe();

    // STOMP 연결 해제
    if (stompClient) {
        try {
            stompClient.disconnect(() => {
                console.log("✅ STOMP Disconnected");
            });
        } catch (e) {
            console.error("STOMP Disconnect Error", e);
        }
    }

    // Binary Socket 연결 해제
    if (binarySocket) {
        binarySocket.close();
        binarySocket = null;
        console.log("✅ Binary Socket Closed");
    }

    // 상태 초기화
    isConnected.value = false;
    isAuthorized.value = false;
};

// Watch for local stream changes (Janus connection)
watch(
    () => videoStore.localStream,
    (newStream) => {
        if (newStream && isSetupComplete.value) {
            startMediaPipe();
        } else {
            stopMediaPipe();
        }
    },
    { immediate: true }
);

// 집중도 분석결과 전송
const handleAnalysisResult = (focusData: GazeResult) => {
    // Spring Boot로 전송할 데이터 포맷 매핑
    // timeline: string
    // focusOn: boolean (시선이 강의화면 내부)
    // movement: boolean (움직임 여부 -> !isStable)
    // empty: boolean (자리비움)

    if (!stompClient || !stompClient.connected) return;

    const payload = {
        timeline: elapsedTime.value,
        focusOn: focusData.isLookingAtMonitor,
        movement: !focusData.isStable,
        empty: focusData.isAbsent
    }

    console.log(`📤 [Learner] Sending analysis data to /app/concentrate/${sessionId}/4`, payload);

    stompClient.send(
        `/app/concentrate/${sessionId}/4`, 
        {}, 
        JSON.stringify(payload)
    );
    console.log("집중도 데이터 전송", JSON.stringify(payload))
}

//================================================================================

const handleJoinSession = async () => {
    isSetupComplete.value = true;
    startSessionTimer();

    // 방 입장 로직: videoStore의 initJanus가 방 조회, 연결, 플러그인 부착 및 입장을 모두 수행합니다.
    const sessionId = route.params.sessionId;
    if (sessionId) {
        try {
            await videoStore.initJanus(sessionId as string);
            console.log("방 입장 성공")
            connectStomp();
        } catch (e) {
            console.error("Session Entry Failed", e);
            // TODO: User notification UI
        }
    }
};

const startSessionTimer = () => {
    timerInterval = window.setInterval(() => {
        elapsedTime.value++;
    }, 1000);
}

onMounted(async () => {
    // 1. 타이머 시작 -> 입장 후 시작으로 변경
    // startSessionTimer();

    // 2. 방 정보 사전 로딩 (Device Setup 중에도 방 존재 여부는 확인 가능)
    // await videoStore.fetchRoomInfo(route.params.sessionId);
});

onUnmounted(() => {
    if (timerInterval) clearInterval(timerInterval);
    stopMediaPipe();
});

// 화면 컨트롤 토글
const toggleMic = () => {
    isMicOn.value = !isMicOn.value;
    videoStore.setAudioEnabled(isMicOn.value);
};
const toggleCamera = () => {
    isCameraOn.value = !isCameraOn.value;
    videoStore.setVideoEnabled(isCameraOn.value);

    if (isCameraOn.value && !videoStore.camHandle) {
        // 만약 어떤 이유로 퍼블리싱이 안되어 있다면 시도
        videoStore.publishCamera();
    }
};

const raiseHand = () => {
    isHandRaised.value = !isHandRaised.value;
    // TODO: signal logic
};
</script>

<style scoped></style>
