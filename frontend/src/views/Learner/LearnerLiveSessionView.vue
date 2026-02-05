<template>
    <div class="bg-slate-50 text-slate-900 h-screen flex flex-col overflow-hidden relative">
        <!-- Device Setup Modal -->
        <DeviceSetupModal v-if="!isSetupComplete" @join="handleJoinSession" />

        <!-- INFO: 임시 연결 종료 버튼 -->
        <button @click="disconnectAll"
            class="absolute top-20 left-6 z-50 bg-red-600 text-white px-3 py-2 rounded-md shadow-lg text-sm font-bold hover:bg-red-700 transition-colors">
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
                <LiveChatContainer :chatList="chatList" :qaList="qaList" @send-message="sendChatMessage" />
            </LiveSidebar>

            <!-- MediaPipe용 숨겨진 Video (Janus Stream 미러링) -->
            <video ref="mediaPipeVideo" class="hidden" autoplay playsinline muted></video>
        </main>

        <!-- 서프라이즈 퀴즈 모달 -->
        <LearnerQuizModal :isOpen="isQuizModalOpen" :quizData="surpriseQuizData" @close="handleQuizClose" />
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, computed } from 'vue';
import { useRoute } from 'vue-router';
import { useVideoRoomStore } from '@/stores/videoRoom';
import { useWorkspaceStore } from '@/stores/workspace';
import { useUserStore } from '@/stores/user';
import { MediaPipeManager } from '@/utils/mediapipe';
import type { GazeResult } from '@/utils/gazeTracker';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

// Components
import LiveSessionHeader from '@/components/Learner/LearnerLiveSession/LiveSessionHeader.vue';
import LiveVideoArea from '@/components/Learner/LearnerLiveSession/LiveVideoArea.vue';
import LiveControlDock from '@/components/Learner/LearnerLiveSession/LiveControlDock.vue';
import LiveSidebar from '@/components/Learner/LearnerLiveSession/LiveSidebar.vue';
import LiveQuizCard from '@/components/Learner/LearnerLiveSession/LiveQuizCard.vue';
import EngagementStats from '@/components/Learner/LearnerLiveSession/EngagementStats.vue';
import LiveChatContainer from '@/components/Learner/LearnerLiveSession/LiveChatContainer.vue';
import DeviceSetupModal from '@/components/Learner/LearnerLiveSession/DeviceSetupModal.vue';
import LearnerQuizModal from '@/components/Learner/LearnerLiveSession/LearnerQuizModal.vue';

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

// Time Sync (서버 시간 동기화)
const lectureStartTime = ref<Date | null>(null); // 서버에서 받은 강의 시작 시간
const isSynced = ref<boolean>(false); // 시간 동기화 완료 여부
const timeOffset = ref<number>(0); // 클라이언트-서버 시간 오프셋 (ms)
const isAnalysisActive = ref<boolean>(false); // ✅ 분석 활성화 여부 (강사 시작 신호 후에만 true)
const lastAnalysisTime = ref<number>(0);
const isAnonymous = ref(false);
// [REMOVED] diffInSeconds - 미사용 변수 제거
// WebSocket 상태 관리
const stompClient = ref<Client | null>(null);
const chatList = ref<any[]>([]);
const qaList = ref<any[]>([]);
const surpriseQuizData = ref<any>(null);
const isQuizModalOpen = ref(false);

const socketUrl = import.meta.env.VITE_CHAT_SOCKET_URL;


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

// ✅ computed로 변경하여 userStore가 업데이트될 때 자동으로 가져옴
const memberId = computed(() => userStore.user?.memberId || 0);
const accessToken = localStorage.getItem('accessToken');

console.log('[Init] memberId:', memberId.value, 'sessionId:', sessionId);

// [REMOVED] sessionStartTime - lectureStartTime으로 대체



let binarySocket: WebSocket | null = null;

// STOMP 연결 및 구독 통합
const connectWebSocket = () => {
    // base_url이 이미 https://를 포함하는지 확인
    const socketUrl = base_url.startsWith('http')
        ? `${base_url}/message`
        : `https://${base_url}/message`;

    console.log(`[Learner Live] Connecting to: ${socketUrl}`);

    stompClient.value = new Client({
        webSocketFactory: () => new SockJS(socketUrl),
        connectHeaders: {
            Authorization: `Bearer ${accessToken}`
        },
        reconnectDelay: 5000,
        debug: (str) => {
            // console.log('[STOMP Debug]', str);
        },
        onConnect: () => {
            console.log("[Learner Live] STOMP Connected! ✅");
            console.log("[Learner Live] stompClient:", stompClient.value);
            console.log("[Learner Live] stompClient.connected:", stompClient.value?.connected);
            isConnected.value = true;

            // 1. 분석 결과 구독 (memberId 포함)
            stompClient.value?.subscribe(`/topic/analysis/${sessionId}/${memberId.value}`, (res) => {
                const result = JSON.parse(res.body);
                console.log("[Learner Live] 분석 결과 수신:", result);
                // 강의 시작 시간 수신 (강사가 녹화 시작 버튼 클릭 시)
                if (result.startTime) {
                    handleLectureStartTime(result.startTime);
                    return;
                }
                // 집중도 데이터
                if (result.concentrateValue !== undefined) {
                    engagementScore.value = result.concentrateValue;
                    console.log("[Learner Live] 집중도 데이터 수신:", result.concentrateValue);
                }
            });
            // 2. 채팅/질문 구독
            stompClient.value?.subscribe(`/topic/chat-room/${sessionId}`, (res) => {
                console.log('[Learner Live] Chat/QA Message:', res.body);
                const msg = JSON.parse(res.body);
                if (!msg.questionId) {
                    chatList.value.push(msg);
                } else {
                    qaList.value.push(msg);
                }
            });

            // 3. 서프라이즈 퀴즈 구독
            stompClient.value?.subscribe(`/topic/surprise-quiz/${sessionId}`, (res) => {
                console.log('[Learner Live] Surprise Quiz:', res.body);
                surpriseQuizData.value = JSON.parse(res.body);
                isQuizModalOpen.value = true;
            });

            // ✅ 4. 수업 시작 시간 구독 (강사가 브로드캐스트)
            stompClient.value?.subscribe(`/topic/session/${sessionId}/start-time`, (res) => {
                console.log('[Learner Live] Start Time Received:', res.body);
                const data = JSON.parse(res.body);
                if (data.startTime) {
                    handleLectureStartTime(data.startTime);
                }
            });

            // Binary Socket 연결 트리거
            connectBinarySocket();
        },
        onStompError: (frame) => {
            console.error('[Learner Live] Broker error:', frame.headers['message']);
        },
        onWebSocketClose: () => {
            console.log('[Learner Live] Connection Closed ❌');
            isConnected.value = false;
        }
    });

    stompClient.value.activate();
}

// Binary WebSocket 연결 (이미지 전송)
const connectBinarySocket = () => {
    // 쿼리 파라미터로 토큰 전송
    const url = `wss://i14a309.p.ssafy.io/emotion/${sessionId}?member-id=${memberId.value}`;
    console.log(`[Binary] Connecting to: ${url}`, 'memberId:', memberId.value);

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
    const socket = binarySocket;

    // ✅ 분석이 활성화되지 않았으면 전송하지 않음
    if (!isAnalysisActive.value) {
        return;
    }

    // 1. 소켓 및 권한 상태 확인
    if (!socket || socket.readyState !== WebSocket.OPEN || !isAuthorized.value) {
        return;
    }

    // 2. 타임라인 계산 (안전 장치 추가)
    let currentTimeline = 0;
    if (isSynced.value && lectureStartTime.value) {
        const now = Date.now();
        const serverTime = now - (timeOffset.value || 0);
        const startTime = lectureStartTime.value.getTime();
        // 경과 시간 계산 (ms -> s)
        const elapsed = Math.floor((serverTime - startTime) / 1000);
        // 음수 방지 및 숫자 여부 체크 (NaN 방어)
        currentTimeline = isNaN(elapsed) ? 0 : Math.max(0, elapsed);
    }

    // 3. 패킷 생성 (4바이트 타임라인 + 이미지 데이터)
    const packet = new ArrayBuffer(4 + arrayBuffer.byteLength);
    const uint8View = new Uint8Array(packet);

    // 4. 타임라인 주입 (Big-endian 방식 수동 기록)
    // shift 연산자를 사용하면 Int32를 가장 확실하게 4개 바이트로 쪼갤 수 있습니다.
    uint8View[0] = (currentTimeline >>> 24) & 0xFF;
    uint8View[1] = (currentTimeline >>> 16) & 0xFF;
    uint8View[2] = (currentTimeline >>> 8) & 0xFF;
    uint8View[3] = currentTimeline & 0xFF;

    // 5. 이미지 데이터 복사 (인덱스 4부터 끝까지)
    uint8View.set(new Uint8Array(arrayBuffer), 4);

    // 6. 전송 및 디버깅
    socket.send(packet);

    // [디버그 로그] 콘솔에서 실제 전송되는 첫 4바이트를 직접 확인 가능
    console.log(
        `[Sent] Time: ${currentTimeline}s | ` +
        `Size: ${packet.byteLength} bytes | ` +
        `Header: [${uint8View.slice(0, 4).join(', ')}]`
    );
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
    if (stompClient.value) {
        stompClient.value.deactivate();
        console.log("✅ STOMP Deactivated");
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
        // ✅ 스트림이 없어지면 MediaPipe 중지
        if (!newStream) {
            stopMediaPipe();
        }
        // ⚠️ 스트림이 생겼다고 자동 시작하지 않음! (강사 신호 대기)
        // startMediaPipe()는 handleLectureStartTime()에서만 호출됨
    },
    { immediate: true }
);

// MediaPipe 실시간 분석 결과 처리
const handleAnalysisResult = (focusData: GazeResult) => {
    // ✅ 분석이 활성화되지 않았으면 전송하지 않음
    if (!isAnalysisActive.value) {
        return;
    }

    // ✅ 1초에 한 번만 전송 (Throttling)
    const now = Date.now();
    if (now - lastAnalysisTime.value < 1000) {
        return;
    }
    lastAnalysisTime.value = now;

    // 동기화되지 않았으면 timeline 계산 불가

    // 동기화되지 않았으면 timeline 계산 불가
    if (!isSynced.value || !lectureStartTime.value) {
        console.warn('[Analysis] 시간 동기화 대기 중...');
        return;
    }
    // Spring Boot로 전송할 데이터 포맷 매핑
    // timeline: string
    // focusOn: boolean (시선이 강의화면 내부)
    // movement: boolean (움직임 여부 -> !isStable)
    // empty: boolean (자리비움)

    if (!stompClient.value || !stompClient.value.connected) return;

    // 타임라인 계산: 동기화된 시간 기준
    let currentDiff = 0;
    if (isSynced.value && lectureStartTime.value) {
        const elapsed = Date.now() - lectureStartTime.value.getTime() - timeOffset.value;
        currentDiff = Math.max(0, Math.round(elapsed / 1000));
    }

    const payload = {
        timeline: currentDiff,
        focusOn: focusData.isLookingAtMonitor,
        movement: !focusData.isStable,
        empty: focusData.isAbsent
    }
    console.log("세션 시작 시간: ", lectureStartTime.value, "현재 timeline:", currentDiff);

    console.log(`📤 [Learner] Sending analysis data to /app/concentrate/${sessionId}/${memberId.value}`, payload);
    // @stomp/stompjs Client는 publish() 메서드 사용
    stompClient.value.publish({
        destination: `/app/concentrate/${sessionId}/${memberId.value}`,
        body: JSON.stringify(payload)
    });
};

//================================================================================


// 메시지 전송
const sendChatMessage = (content: string, type: string, questionId?: number | string | null) => {
    console.log('[Chat] sendChatMessage 호출됨:', { content, type, questionId });
    console.log('[Chat] stompClient:', stompClient.value);
    console.log('[Chat] stompClient connected:', stompClient.value?.connected);

    if (!content.trim() || !stompClient.value?.connected) {
        console.warn('[Chat] 메시지 전송 실패: content 비어있거나 연결 안됨');
        return;
    }
    const sessionId = route.params.sessionId as string;

    const payload = {
        type,
        content,
        questionId: questionId || null
    };

    console.log('[Chat] 메시지 전송 시도:', payload, 'to:', `/app/chat-room/${sessionId}`);

    // @stomp/stompjs Client는 publish() 메서드 사용
    stompClient.value.publish({
        destination: `/app/chat-room/${sessionId}`,
        body: JSON.stringify(payload)
    });

    console.log('[Chat] 메시지 전송 완료');
};

// 퀴즈 모달 닫기
const handleQuizClose = () => {
    isQuizModalOpen.value = false;
};

const handleJoinSession = async () => {
    isSetupComplete.value = true;
    startSessionTimer();

    // 방 입장 로직: videoStore의 initJanus가 방 조회, 연결, 플러그인 부착 및 입장을 모두 수행합니다.
    const sessionId = route.params.sessionId;
    if (sessionId) {
        try {
            await videoStore.initJanus(sessionId as string);
            console.log("방 입장 성공 - 카메라 송출 대기 중...");

            // ✅ Janus Publish 완료 대기 (최대 10초)
            const checkPublish = setInterval(() => {
                if (videoStore.isPublished) {
                    console.log("✅ Janus Publish 완료! STOMP 연결 시작");
                    clearInterval(checkPublish);
                    connectWebSocket();
                }
            }, 500);

            // 10초 후에도 안되면 강제 연결 (Fallback)
            setTimeout(() => {
                if (!videoStore.isPublished) {
                    console.warn("⚠️ Janus Publish 타임아웃. 강제 연결 시도.");
                    clearInterval(checkPublish);
                    connectWebSocket();
                }
            }, 10000);

        } catch (e) {
            console.error("Session Entry Failed", e);
            // TODO: User notification UI
        }
    }
};

// 시간 동기화 핸들러
const handleLectureStartTime = (startTimeString: string) => {
    console.log('[Time Sync] startTime 수신:', startTimeString);

    // 중복 수신 방지
    if (isSynced.value) {
        console.warn('[Time Sync] 이미 동기화됨, 무시');
        return;
    }

    // Java LocalDateTime.toString() 파싱
    const serverTime = new Date(startTimeString);
    const clientTime = new Date();

    lectureStartTime.value = serverTime;
    timeOffset.value = clientTime.getTime() - serverTime.getTime();
    isSynced.value = true;

    console.log('[Time Sync] 동기화 완료');
    console.log('[Time Sync] 서버 시작 시간:', serverTime);
    console.log('[Time Sync] 시간 오프셋:', timeOffset.value, 'ms');

    // ✅ 분석 시작! (MediaPipe 시작)
    isAnalysisActive.value = true;
    console.log('[Analysis] 분석 활성화! MediaPipe 시작...');
    startMediaPipe();

    // 타이머 재시작
    restartTimer();
};

const startSessionTimer = () => {
    timerInterval = window.setInterval(() => {
        if (isSynced.value && lectureStartTime.value) {
            // 동기화된 시간 기준으로 경과 시간 계산
            const now = new Date();
            const elapsed = now.getTime() - lectureStartTime.value.getTime() - timeOffset.value;
            elapsedTime.value = Math.floor(elapsed / 1000); // 초 단위
        } else {
            // 동기화 안됐으면 기존 방식 (페이지 로드 시점 기준)
            elapsedTime.value++;
        }
    }, 1000);
};

const restartTimer = () => {
    // 기존 타이머 정리
    if (timerInterval) {
        clearInterval(timerInterval);
    }
    // 동기화된 시간으로 타이머 재시작
    startSessionTimer();
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
    stompClient.value?.deactivate();

    // ✅ 분석 상태 초기화
    isAnalysisActive.value = false;
    isSynced.value = false;
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
