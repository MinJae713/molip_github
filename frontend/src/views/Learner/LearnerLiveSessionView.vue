<template>
    <div class="bg-slate-50 text-slate-900 min-h-screen flex flex-col overflow-hidden">
        <LiveSessionHeader sessionTitle="인공지능 윤리와 실제 실습" sessionSubtitle="디지털 비즈니스 전략 강의 (4주차)"
            :elapsedTime="elapsedTime" />
        <main class="flex flex-1 overflow-hidden p-6 gap-6 relative">
            <LiveVideoArea :focusStatus="focusStatus" :showWarning="showWarning" @close-warning="showWarning = false"
                :mainStream="videoStore.mainStream" :pipStream="videoStore.pipStream"
                :userRole="workspaceStore.currentWorkspace?.my_role">
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

onMounted(async () => {
    // 1. 타이머 시작
    timerInterval = window.setInterval(() => {
        elapsedTime.value++;
    }, 1000);
    // 2. 방 입장 로직 (학생 시나리오)
    const sessionId = route.params.sessionId;
});

onUnmounted(() => {
    if (timerInterval) clearInterval(timerInterval);
});

// 화면 컨트롤 토글
const toggleMic = () => {
    isMicOn.value = !isMicOn.value;
    // TODO: videoStore.toggleAudio(isMicOn.value)
};
const toggleCamera = () => {
    isCameraOn.value = !isCameraOn.value;
    if (isCameraOn.value) {
        videoStore.publishCamera();
    } else {
        // TODO: unpublish logic
    }
};

const raiseHand = () => {
    isHandRaised.value = !isHandRaised.value;
    // TODO: signal logic
};
</script>

<style scoped></style>
