<template>
    <div class="bg-slate-50 text-slate-900 h-screen flex flex-col overflow-hidden relative">
        <!-- Device Setup Modal -->
        <DeviceSetupModal v-if="!isSetupComplete" :currentGaze="currentGaze" :rawGaze="lastRawGaze"
            @join="handleJoinSession" @calibrate="handleCalibration" />

        <!-- INFO: 임시 연결 종료 버튼 -->
        <button @click="disconnectAll"
            class="absolute top-20 left-6 z-50 bg-red-600 text-white px-3 py-2 rounded-md shadow-lg text-sm font-bold hover:bg-red-700 transition-colors">
            🔌 연결 끊기 (TEST)
        </button>

        <!-- INFO: Gaze Tracker 디버그 UI -->
        <div
            class="absolute top-36 left-6 z-50 bg-black/80 text-white p-4 rounded-lg shadow-xl text-xs space-y-2 pointer-events-none">
            <h3 class="font-bold text-yellow-400 mb-2">👀 Gaze Tracker Status</h3>
            <div class="grid grid-cols-2 gap-x-4 gap-y-1">
                <span class="text-slate-400">Looking At Monitor:</span>
                <span :class="debugGazeStatus.isLookingAtMonitor ? 'text-green-400 font-bold' : 'text-red-400'">
                    {{ debugGazeStatus.isLookingAtMonitor ? 'YES' : 'NO' }}
                </span>

                <span class="text-slate-400">Stable (No Move):</span>
                <span :class="debugGazeStatus.isStable ? 'text-green-400' : 'text-yellow-400 font-bold'">
                    {{ debugGazeStatus.isStable ? 'YES' : 'NO (!)' }}
                </span>

                <span class="text-slate-400">User Present:</span>
                <span :class="!debugGazeStatus.isAbsent ? 'text-blue-400' : 'text-red-500 font-bold'">
                    {{ !debugGazeStatus.isAbsent ? 'YES' : 'ABSENT' }}
                </span>
            </div>

            <!-- 시선 시각화 플롯 -->
            <div class="mt-3 border-t border-slate-700 pt-3">
                <div class="text-[10px] text-slate-400 mb-1">Live Gaze Plot</div>
                <div
                    class="relative w-[140px] h-[100px] bg-slate-900 border border-slate-600 rounded overflow-hidden mx-auto">
                    <!-- Monitor Area (Safe Zone) : x 0.2~0.8, y 0.1~0.9 -->
                    <!-- Width: 60% (0.8-0.2), Left: 20% | Height: 80% (0.9-0.1), Top: 10% -->
                    <div class="absolute border-2 border-dashed border-green-500/50"
                        style="left: 20%; top: 10%; width: 60%; height: 80%;">
                        <div class="absolute top-1 left-1 text-[8px] text-green-500/70">Monitor</div>
                    </div>

                    <!-- Center Crosshair -->
                    <div class="absolute top-1/2 left-0 w-full h-[1px] bg-slate-700/50"></div>
                    <div class="absolute left-1/2 top-0 h-full w-[1px] bg-slate-700/50"></div>

                    <!-- Gaze Point -->
                    <!-- Flip X coordinate because camera is mirrored? Usually webcam preview is mirrored. -->
                    <!-- Let's assume raw coordinates: x:0 is left, x:1 is right. -->
                    <div class="absolute w-2.5 h-2.5 bg-red-500 rounded-full shadow-[0_0_8px_rgba(239,68,68,0.8)] transition-all duration-75 ease-out transform -translate-x-1/2 -translate-y-1/2"
                        :style="{
                            left: `${currentGaze.x * 100}%`,
                            top: `${currentGaze.y * 100}%`
                        }">
                    </div>
                </div>
                <div class="text-[9px] text-slate-500 text-center mt-1">
                    X: {{ currentGaze.x.toFixed(2) }} / Y: {{ currentGaze.y.toFixed(2) }}
                </div>
            </div>

            <div class="mt-2 text-[10px] text-slate-500 border-t border-slate-700 pt-1">
                Last Update: {{ debugGazeStatus.timestamp }}
            </div>
        </div>
        <LiveSessionHeader :sessionTitle="learnerSessionStore.currentSessionInfo?.title as string"
            :sessionSubtitle="learnerSessionStore.currentSessionInfo?.description as string" :elapsedTime="elapsedTime"
            :userName="userStore.user?.name" />
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
                <EngagementStats :engagementScore="engagementScore" :currentEmotion="processedEmotions"
                    :focusStatus="focusStatus" />
                <!-- 채팅/질문 -->
                <LiveChatContainer :chatList="chatList" :qaList="qaList" @send-message="sendChatMessage" />
            </LiveSidebar>

            <!-- MediaPipe용 숨겨진 Video (Janus Stream 미러링) -->
            <video ref="mediaPipeVideo" class="hidden" autoplay playsinline muted></video>
        </main>

        <!-- 서프라이즈 퀴즈 모달 -->
        <LearnerQuizModal :isOpen="isQuizModalOpen" :quizData="surpriseQuizData" @close="handleQuizClose" />

        <!-- 퇴실 퀴즈 모달 -->
        <LearnerExitQuizModal :isOpen="isExitQuizOpen" :sessionId="sessionId as string" @close="handleExitQuizClose" />
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, computed } from 'vue';
import { useRoute } from 'vue-router';
import { useVideoRoomStore } from '@/stores/videoRoom';
import { useWorkspaceStore } from '@/stores/workspace';
import { useLearnerSessionStore } from '@/stores/learnerSession';
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
import LearnerExitQuizModal from '@/views/Learner/LearnerExitQuizModal.vue';

const route = useRoute();
const videoStore = useVideoRoomStore();
const workspaceStore = useWorkspaceStore();
const learnerSessionStore = useLearnerSessionStore();

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
const isExitQuizOpen = ref(false);

const socketUrl = import.meta.env.VITE_CHAT_SOCKET_URL;

// Timer
let timerInterval: number | null = null;
const isSetupComplete = ref(false);

// 디버깅용 상태
const debugGazeStatus = ref({
    isLookingAtMonitor: false,
    isStable: true,
    isAbsent: false,
    timestamp: '-'
});

const currentGaze = ref({ x: 0.5, y: 0.5 }); // [NEW] 실시간 시선 좌표
const lastRawGaze = ref({ x: 0.5, y: 0.5 }); // Store raw gaze for calibration

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
// 이모지 처리 관련
const analysisData = ref({ emotionMessage: null })
const emotionMap: Record<string, { label: string, emoji: string, color: string }> = {
    happy: { label: '행복 (Happy)', emoji: '😊', color: 'bg-yellow-400' },
    sad: { label: '슬픔 (Sad)', emoji: '😢', color: 'bg-blue-400' },
    angry: { label: '분노 (Angry)', emoji: '😠', color: 'bg-red-500' },
    surprise: { label: '놀람 (Surprise)', emoji: '😲', color: 'bg-purple-400' },
    fear: { label: '공포 (Fear)', emoji: '😨', color: 'bg-indigo-700' },
    disgust: { label: '혐오 (Disgust)', emoji: '🤢', color: 'bg-green-500' },
    neutral: { label: '평온 (Neutral)', emoji: '😐', color: 'bg-slate-400' }
};

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
        debug: () => { }, // 디버그 로그 끄기
        onConnect: () => {
            console.log("[Learner Live] STOMP Connected! ✅");
            isConnected.value = true;

            // 1. 분석 결과 구독 (memberId 포함)
            stompClient.value?.subscribe(`/topic/analysis/${sessionId}/${memberId.value}`, (res) => {
                const result = JSON.parse(res.body);
                console.log("[Learner Live] 분석 결과 수신:", result);
                analysisData.value = {
                    ...analysisData.value,
                    emotionMessage: result
                }; // 수신한 result(emotion1, confidence1 등)를 할당

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

            // 4. 수업 시작 시간 구독 (강사가 브로드캐스트)
            stompClient.value?.subscribe(`/topic/session/${sessionId}/start-time`, (res) => {
                console.log('[Learner Live] Start Time Received:', res.body);
                const data = JSON.parse(res.body);
                if (data.startTime) {
                    handleLectureStartTime(data.startTime);
                }
            });

            // 5. 수업 종료 신호 구독 [NEW]
            stompClient.value?.subscribe(`/topic/session/${sessionId}/end`, (res) => {
                console.log('[Learner Live] 강의 종료 신호 수신');
                isAnalysisActive.value = false; // 분석 중지
                stopMediaPipe(); // 미디어파이프 중지

                // 기존 연결 해제 (단, 퀴즈 전송 등 필요한 소켓 연결은 유지해야 할 수도 있음. 
                // 하지만 요구사항이 "분석이고 뭐고 다 끊어라" 이므로 disconnectAll 호출)
                // disconnectAll(); <-- 이걸 부르면 STOMP도 끊겨서 퀴즈 제출이 안될 수 있음.
                // 퀴즈 제출은 axios(videoApi)를 쓰므로 STOMP 끊어도 상관없음.
                disconnectAll();

                alert("강의가 종료되었습니다. 퇴실 퀴즈를 풀어주세요!");
                isExitQuizOpen.value = true;
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

const lastValidEmotion = ref({
    label: '대기 중...',
    emoji: '⌛',
    percentage: 0,
    color: 'bg-slate-400'
});

const processedEmotions = computed(() => {
    const msg = analysisData.value?.emotionMessage as any;

    // 1. 값이 있고, emotion1이 존재할 때만 업데이트 로직 실행
    if (msg && msg.emotion1 && emotionMap[msg.emotion1]) {
        const key = msg.emotion1;
        const confidence = msg.confidence1 || 0;
        const data = emotionMap[key];
        if (data) {
            // 이전 값을 새 값으로 덮어씀 (데이터가 없으면 이 로직을 안 타서 이전 값 유지)
            lastValidEmotion.value = {
                label: data.label,
                emoji: data.emoji,
                percentage: Math.round(confidence), // 63.78 -> 64
                color: data.color
            };
        }
    }
    // 2. 항상 마지막으로 유효했던 감정 값을 반환 (배열 형태 유지)
    return [lastValidEmotion.value];
});


// Binary WebSocket 연결 (이미지 전송)
const connectBinarySocket = () => {
    // 쿼리 파라미터로 토큰 전송
    const url = `wss://i14a309.p.ssafy.io/emotion/${sessionId}?member-id=${memberId.value}`;
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
    const socket = binarySocket;

    // 1. 소켓 및 권한 상태 확인
    if (!socket || socket.readyState !== WebSocket.OPEN || !isAuthorized.value) {
        return;
    }

    // ✅ 강의 시작 전에는 이미지 전송하지 않음 (STOMP와 동일한 로직)
    if (!isSynced.value || !lectureStartTime.value) {
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
        if (newStream) {
            startMediaPipe();
        } else {
            stopMediaPipe();
        }
    },
    { immediate: true }
);

// [NEW] Watch for Session End (Janus Destroyed)
watch(
    () => videoStore.isLiveEnded,
    (isEnded) => {
        if (isEnded) {
            console.log("[Learner] Session Ended via Janus (isLiveEnded=true)");
            // 이미 cleanup이 안되어 있다면 수행
            if (isConnected.value || isAnalysisActive.value) {
                isAnalysisActive.value = false;
                disconnectAll();

                // 퇴실 퀴즈 모달 오픈 (이미 열려있지 않다면)
                if (!isExitQuizOpen.value) {
                    // videoRoom.ts에서 이미 alert가 떴을 수 있으므로 중복 alert는 생략하거나 필요한 경우 추가
                    isExitQuizOpen.value = true;
                }
            }
        }
    }
);

// 집중도 분석결과 전송
const handleAnalysisResult = (focusData: GazeResult) => {
    // 0. Update Last Raw Gaze (for calibration) - Always update
    if (focusData.rawGaze) {
        lastRawGaze.value = focusData.rawGaze;
    }

    // 1. Update Gaze UI (Always update this for feedback - 30fps for smooth calibration)
    currentGaze.value = focusData.gaze;

    // ✅ 1초 Throttling: 이후 로직은 1초에 한 번만 실행
    const now = Date.now();
    if (now - lastAnalysisTime.value < 1000) {
        return;
    }
    lastAnalysisTime.value = now;

    // 2. Debug 상태 업데이트 (1초마다)
    debugGazeStatus.value = {
        isLookingAtMonitor: focusData.isLookingAtMonitor,
        isStable: focusData.isStable,
        isAbsent: focusData.isAbsent,
        timestamp: new Date().toLocaleTimeString()
    };

    // Spring Boot로 전송할 데이터 포맷 매핑
    // timeline: string
    // focusOn: boolean (시선이 강의화면 내부)
    // movement: boolean (움직임 여부 -> !isStable)
    // empty: boolean (자리비움)

    if (!stompClient.value || !stompClient.value.connected) return;

    // ✅ 강사가 강의 시작하지 않았으면 전송하지 않음
    if (!isSynced.value || !lectureStartTime.value) {
        console.log('[Analysis] 강의 시작 대기 중... (분석은 계속, 전송 안 함)');
        return;
    }

    // 타임라인 계산: 동기화된 시간 기준
    const elapsed = Date.now() - lectureStartTime.value.getTime() - timeOffset.value;
    const currentDiff = Math.max(0, Math.round(elapsed / 1000));

    const payload = {
        timeline: currentDiff,
        focusOn: focusData.isLookingAtMonitor,
        movement: !focusData.isStable,
        empty: focusData.isAbsent
    }
    console.log("세션 시작 시간: ", lectureStartTime.value, "현재 timeline:", currentDiff);

    console.log(`📤 [Learner] Sending analysis data to /app/concentrate/${sessionId}/${memberId.value}`, payload);

    stompClient.value.publish({
        destination: `/app/concentrate/${sessionId}/${memberId.value}`,
        body: JSON.stringify(payload)
    });
    console.log("집중도 데이터 전송", JSON.stringify(payload))

}

// 캘리브레이션 핸들러 (시선 추적 보정)
const handleCalibration = (avgGaze?: { x: number, y: number }) => {
    if (mediaPipeManager) {
        // If averaged gaze is provided (from 3s intent), use it. Otherwise use last raw gaze.
        const currentRaw = avgGaze || lastRawGaze.value;

        // Calibrate: Map "Current/Averaged Raw Gaze" to "Center Target (0.5, 0.5)"
        console.log("Creating Calibration Profile...", currentRaw);
        mediaPipeManager.calibrate(0.5, 0.5, currentRaw.x, currentRaw.y);
        alert("✅ 시선 중앙 보정이 완료되었습니다.");
    }
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

const handleQuizClose = () => {
    isQuizModalOpen.value = false;
};

// 퇴실 퀴즈 종료 (확인 완료) -> 페이지 닫기 or 홈 이동
const handleExitQuizClose = () => {
    isExitQuizOpen.value = false;
    // 1. 창 닫기 시도
    window.close();

    // 2. 창이 안 닫히면 (스크립트로 열지 않은 경우) 홈으로 이동
    // 약간의 딜레이를 줘서 window.close()가 작동할 시간을 줌
    setTimeout(() => {
        window.location.href = '/';
    }, 100);
};

const handleJoinSession = async () => {
    isSetupComplete.value = true;
    startSessionTimer();

    // 방 입장 로직: videoStore의 initJanus가 방 조회, 연결, 플러그인 부착 및 입장을 모두 수행합니다.
    const sessionId = route.params.sessionId;
    if (sessionId) {
        try {
            // ✅ 1. 먼저 localStream 설정 (카메라/마이크 준비)
            console.log("[Join] 1. LocalStream 준비 중...");
            await videoStore.startLocalPreview();
            console.log("[Join] ✅ LocalStream 준비 완료");

            // ✅ 2. 그 다음 Janus 방 입장
            console.log("[Join] 2. Janus 방 입장 시작...");
            await videoStore.initJanus(sessionId as string);
            console.log("[Join] ✅ 방 입장 성공 - 카메라 송출 시작됨");

            // ✅ 3. Janus Publish 완료 대기 (최대 10초)
            const checkPublish = setInterval(() => {
                if (videoStore.isPublished) {
                    console.log("[Join] ✅ Janus Publish 완료! STOMP 연결 시작");
                    clearInterval(checkPublish);
                    connectWebSocket();
                }
            }, 500);

            // 10초 후에도 안되면 강제 연결 (Fallback)
            setTimeout(() => {
                if (!videoStore.isPublished) {
                    console.warn("[Join] ⚠️ Janus Publish 타임아웃. 강제 연결 시도.");
                    clearInterval(checkPublish);
                    connectWebSocket();
                }
            }, 10000);

        } catch (e) {
            console.error("[Join] Session Entry Failed", e);
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
    if (route.params.sessionId) {
        await learnerSessionStore.fetchSessionInfo(route.params.sessionId as string);
    }
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
