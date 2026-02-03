<template>
    <div class="bg-slate-50 text-slate-900 h-screen flex flex-col overflow-hidden relative">
        <!-- Device Setup Modal -->
        <DeviceSetupModal v-if="!isSetupComplete" @join="handleJoinSession" />

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
                <LiveChatContainer />
            </LiveSidebar>
        </main>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';
import { useRoute } from 'vue-router';
import { useVideoRoomStore } from '@/stores/videoRoom';
import { useWorkspaceStore } from '@/stores/workspace';

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
const elapsedTime = ref(2712); // seconds (00:45:12)
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

const handleJoinSession = async () => {
    isSetupComplete.value = true;
    startSessionTimer();

    // 방 입장 로직: videoStore의 initJanus가 방 조회, 연결, 플러그인 부착 및 입장을 모두 수행합니다.
    const sessionId = route.params.sessionId;
    if (sessionId) {
        try {
            await videoStore.initJanus(sessionId as string);
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
