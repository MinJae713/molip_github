<template>
    <div class="bg-slate-50 text-slate-900 min-h-screen flex flex-col overflow-hidden">
        <!-- Header -->
        <LiveSessionHeader sessionTitle="인공지능 윤리와 실제 실습" sessionSubtitle="디지털 비즈니스 전략 강의 (4주차)"
            :elapsedTime="elapsedTime" />

        <!-- Main Content -->
        <main class="flex flex-1 overflow-hidden p-6 gap-6 relative">
            <!-- Video Area -->
            <!-- 강의 화면, 집중도 정보, 경고 메시지, 채팅 버튼 -->
            <LiveVideoArea :focusStatus="focusStatus" :showWarning="showWarning" @close-warning="showWarning = false"
                :mainStream="videoStore.mainStream" :pipStream="videoStore.pipStream"
                :userRole="workspaceStore.currentWorkspace?.my_role">
                <template #controls>
                    <LiveControlDock :isMicOn="isMicOn" :isCameraOn="isCameraOn" :isHandRaised="isHandRaised"
                        @toggle-mic="toggleMic" @toggle-camera="toggleCamera" @raise-hand="raiseHand" />
                </template>
            </LiveVideoArea>

            <!-- Sidebar -->
            <LiveSidebar>
                <!-- Live Quiz Card -->
                <LiveQuizCard title="LIVE QUIZ 진행중" subtitle="인공지능 윤리 가이드라인 핵심..." timeRemaining="00:15" />

                <!-- Engagement Score -->
                <EngagementStats :engagementScore="engagementScore" :currentEmotion="currentEmotion"
                    :focusStatus="focusStatus" />

                <!-- Chat/Q&A Panel -->
                <ChatPanel v-model:activeTab="activeTab" v-model="newMessage" v-model:isAnonymous="isAnonymous"
                    :chatMessages="chatMessages" :qaMessages="qaMessages" @send-message="sendMessage" />
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
import ChatPanel from '@/components/Learner/LearnerLiveSession/ChatPanel.vue';

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

const currentEmotion = ref({
    emoji: '🤩',
    label: '흥미로움'
});

// Mock Data
const chatMessages = ref([
    { id: 1, name: '김민수', time: '10:25', message: '오늘 강의 정말 유익하네요!' },
    { id: 2, name: '이지은', time: '10:27', message: '질문 있습니다~' },
]);

const qaMessages = ref([
    {
        id: 1,
        status: 'answered',
        time: '오전 10:30',
        question: 'AI 윤리 파트에서 \'책임성\'의 주체는 개발자인가요, 기업인가요?',
        answer: '가이드라인에 따르면 개발자, 운영자, 사용자 모두가 각자의 역할에 따른 책임을 집니다.'
    },
    {
        id: 2,
        status: 'new',
        time: '방금 전',
        question: '실습 데이터셋의 출처가 궁금합니다.',
        answer: null
    },
]);

// Timer
let timerInterval: number | null = null;

onMounted(async () => {
    // 1. 타이머 시작
    timerInterval = window.setInterval(() => {
        elapsedTime.value++;
    }, 1000);

    // 2. [Live Session] 방 입장 로직 (학생 시나리오)
    const sessionId = route.params.sessionId;
    if (!sessionId) {
        console.error("Session ID is missing");
        return;
    }

    try {
        console.log(`[Session] 입장 시도: Session ${sessionId}`);

        // 2-1. 방 정보 확인
        const hasRoom = await videoStore.fetchRoomInfo(sessionId as string);

        if (!hasRoom) {
            alert("아직 강의가 시작되지 않았습니다. (방이 없습니다)");
            // 필요한 경우 여기서 뒤로가기(router.back()) 처리 가능
            return;
        }

        // 2-2. 방이 있으면 Janus 연결 절차 시작
        await videoStore.initJanus();

        // 2-3. 플러그인 부착 (카메라)
        await videoStore.attachPlugin("camera"); // camera 핸들 생성

        // 2-4. 방 입장 요청 (join) - 성공 콜백(onSuccess)은 store 내부에서 처리하거나 promise chain으로 처리
        // attachPlugin 내부에서 success 콜백이 발생하면 핸들이 할당됨.
        // joinRoom은 핸들이 있어야 하므로, attachPlugin 완료 후 호출해야 함.
        // 현재 store구조상 attachPlugin이 promise를 반환하므로 await 가능.

        // videoStore.joinRoom(videoStore.camHandle, "camera"); 
        // -> store에서 camHandle을 직접 참조할 수 없거나, ref이므로 .value 필요
        // -> 편의를 위해 store 내부에 joinRoom을 호출하는 wrapper action이 있으면 좋겠지만, 
        // -> 우선은 store의 joinRoom을 camHandle(반환값)과 함께 호출

        // attachPlugin이 handle을 resolve한다고 가정 (store 수정 필요)
        // 만약 store가 handle을 반환하지 않는다면, store 상태(camHandle)를 참조

        if (videoStore.camHandle) {
            videoStore.joinRoom(videoStore.camHandle, "camera");
        }

    } catch (err) {
        console.error("[Session] 입장 실패:", err);
        alert("강의실 입장 중 오류가 발생했습니다.");
    }
});

onUnmounted(() => {
    if (timerInterval) clearInterval(timerInterval);
    // Janus 세션 정리 로직이 필요하다면 store에서 destroy 호출
    // videoStore.janus?.destroy();
});

// Methods
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

const sendMessage = () => {
    if (!newMessage.value.trim()) return;

    if (activeTab.value === 'chat') {
        chatMessages.value.push({
            id: Date.now(),
            name: '나',
            time: new Date().toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit' }),
            message: newMessage.value
        });
    } else {
        qaMessages.value.unshift({
            id: Date.now(),
            status: 'new',
            time: '방금 전',
            question: newMessage.value,
            answer: null
        });
    }

    newMessage.value = '';
};
</script>

<style scoped>
/*
  The custom scrollbar styles can be moved to a global stylesheet 
  or kept in ChatPanel.vue if they are only for the messages area.
  For now, I'll keep them here or move them to ChatPanel.
*/
</style>
