<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed, nextTick, provide, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useTrainerLiveStore } from '@/stores/trainerlive';
import { useUserStore } from '@/stores/user';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

// 스토어
const trainerLiveStore = useTrainerLiveStore();
const userStore = useUserStore();

// [컴포넌트 임포트]
import LiveHeader from '@/components/Live/Console/LiveHeader.vue';
import LiveControlPanel from '@/components/Live/Console/LiveControlPanel.vue';
import ParticipantItem from '@/components/Live/Participant/ParticipantItem.vue';
import StudentMonitor from '@/components/Live/Monitor/StudentMonitor.vue';
import LiveVideoConsole from '@/components/Live/Console/LiveVideoConsole.vue';

// AI 분석 컴포넌트
import AIActionRecommendation from '@/components/Live/AI/AIActionRecommendation.vue';
import GlobalEngagementCard from '@/components/Live/AI/GlobalEngagementCard.vue';
import SessionEmotionAnalysis from '@/components/Live/AI/SessionEmotionAnalysis.vue';

// 사이드바 컴포넌트
import SidebarStats from '@/components/Live/Sidebar/SidebarStats.vue';
import SidebarTabs from '@/components/Live/Sidebar/SidebarTabs.vue';
import TrainerLiveChatContainer from '@/components/Live/Sidebar/TrainerLiveChatContainer.vue';
import QuickQuizModal from '@/components/Live/Sidebar/QuickQuizModal.vue';

const router = useRouter();
const route = useRoute();

// --- UI 상태 관리 ---
const isEndModalOpen = ref(false); // 수업 종료 확인 모달
const activeTab = ref<'status' | 'chat' | 'qna'>('status'); // 사이드바 활성 탭 (기본: 상태)
const isQuickQuizModalOpen = ref(false); // 퀵 퀴즈 모달

// --- WebSocket 상태 관리 ---
interface Message {
  id?: number;
  type?: string;
  questionId?: number | string;
  content: string;
  senderName?: string;
  name?: string;
  profileImageUrl?: string;
  createdAt?: string;
  timestamp?: string;
}

const chatList = ref<Message[]>([]);
const qaList = ref<Message[]>([]);
const quizData = ref<any>(null); // 퀴즈 데이터 (추후 타입 정의)
const stompClient = ref<Client | null>(null);
const socketUrl = import.meta.env.VITE_CHAT_SOCKET_URL;

// --- 통계 (Computed) ---
const focusCount = computed(() => trainerLiveStore.participants.filter(p => p.status === 'focus').length);
const warningCount = computed(() => trainerLiveStore.participants.filter(p => p.status === 'distracted').length); // distracted status check
const absentCount = computed(() => trainerLiveStore.participants.filter(p => p.status === 'away').length);

// --- WebSocket 연결 ---
const connectWebSocket = () => {
  const sessionId = route.params.sessionId as string;
  console.log(`[Trainer Live] Connecting to WebSocket. SessionID: ${sessionId}`);

  stompClient.value = new Client({
    webSocketFactory: () => new SockJS(socketUrl),
    connectHeaders: {
      Authorization: `Bearer ${localStorage.getItem('accessToken')}`
    },
    debug: (str) => {
      console.log('[Trainer Live Stomp Debug]', str);
    },
    onConnect: () => {
      console.log(`[Trainer Live] Connected ✅`);

      // 1. 채팅/질문 구독
      stompClient.value?.subscribe(`/topic/chat-room/${sessionId}`, (res) => {
        console.log(`[Trainer Live] Chat/QA Message:`, res.body);
        const msg: Message = JSON.parse(res.body);
        // questionId 유무로 채팅/질문 구분
        if (!msg.questionId) {
          chatList.value.push(msg);
        } else {
          qaList.value.push(msg);
        }
      });
      // 2. ai 퀴즈 받아오는 방 구독
      stompClient.value?.subscribe(`/topic/quiz/${sessionId}/test`, (res) => {
        // stompClient.value?.subscribe(`/topic/quiz/${sessionId}`, (res) => {
        console.log(`[Trainer Live] AI Quiz Received:`, res.body);
        quizData.value = JSON.parse(res.body);
      });
    },
    onStompError: (frame) => {
      console.error('[Trainer Live] Broker error:', frame.headers['message']);
      console.error('[Trainer Live] Details:', frame.body);
    },
    onWebSocketClose: () => {
      console.log('[Trainer Live] Connection Closed ❌');
    }
  });

  stompClient.value.activate();
};

// --- 메시지 전송 함수 ---
const sendChatMessage = (content: string, type: string, questionId?: number | string | null) => {
  if (!content.trim() || !stompClient.value?.connected) return;
  const sessionId = route.params.sessionId as string;
  const payload = {
    type,
    content,
    questionId: questionId || null
  };

  stompClient.value.publish({
    destination: `/app/chat-room/${sessionId}`,
    body: JSON.stringify(payload)
  });
};

const getQuiz = () => {
  if (!stompClient.value?.connected) return;
  const sessionId = route.params.sessionId as string;
  const payload = {
    content: "퀴즈주세요",
  }
  stompClient.value.publish({
    destination: `/app/quiz/${sessionId}`,
    body: JSON.stringify(payload)
  });
};

// 학생들에게 퀴즈 전송
const sendQuizToStudents = (quizzes: any) => {
  if (!stompClient.value?.connected) return;
  const sessionId = route.params.sessionId as string;

  stompClient.value.publish({
    destination: `/app/quiz/send/${sessionId}`,
    body: JSON.stringify(quizzes)
  });

  console.log('[Trainer Live] Quiz sent to students:', quizzes);
};

// provide로 하위 컴포넌트에 함수 전달
provide('sendQuizToStudents', sendQuizToStudents);
provide('getQuiz', getQuiz);

// --- 퀵 퀴즈 모달 관리 ---
// quizData가 업데이트되면 모달 자동 열기
watch(quizData, (newData) => {
  if (newData && Object.keys(newData).length > 0) {
    isQuickQuizModalOpen.value = true;
  }
});

const handleQuickQuizClose = () => {
  isQuickQuizModalOpen.value = false;
};

const handleQuickQuizSend = (quiz: any) => {
  sendQuizToStudents(quiz);
  isQuickQuizModalOpen.value = false;
  alert('퀴즈가 학생들에게 전달되었습니다.');
};

onMounted(async () => {
  const sessionId = route.params.sessionId as string;
  await trainerLiveStore.initJanus(sessionId);
  connectWebSocket();
});

onUnmounted(() => {
  stompClient.value?.deactivate();
});

// --- 수업 종료 로직 ---
const handleEndSession = async () => {
  trainerLiveStore.stopClass(); // 방송 중단
  isEndModalOpen.value = false;
  router.push('/dashboard');
};
</script>

<template>
  <div class="h-screen bg-slate-50 flex flex-col overflow-hidden font-sans text-slate-900">
    <LiveHeader @end-class="isEndModalOpen = true" />
    <div class="flex-1 flex overflow-hidden">
      <!-- 메인 강의 영역 (전체 너비 사용) -->
      <main class="flex-1 flex flex-col p-6 gap-6 overflow-y-auto bg-slate-50">
        <LiveVideoConsole />
        <div class="grid grid-cols-12 gap-6 min-h-70">
          <AIActionRecommendation class="col-span-4" />
          <GlobalEngagementCard class="col-span-3" />
          <SessionEmotionAnalysis class="col-span-5" />
        </div>
      </main>

      <!-- 사이드바 패널 -->
      <aside class="w-96 bg-white border-l border-slate-200 flex flex-col shadow-sm shrink-0 overflow-hidden">

        <!-- 사이드바 상단 헤더 & 탭 -->
        <div class="p-7 pb-0 space-y-6 shrink-0">
          <div class="flex items-center justify-between px-1">
            <h2 class="text-xl font-bold text-slate-800">
              참여자 목록 <span class="text-indigo-600 ml-1 text-base">{{ trainerLiveStore.participants.length }}</span>
            </h2>
            <button class="p-2 hover:bg-slate-100 rounded-xl transition-colors text-slate-500">
              <span class="material-symbols-outlined text-xl">tune</span>
            </button>
          </div>

          <SidebarStats :focus-count="focusCount" :warning-count="warningCount" :absent-count="absentCount" />
          <SidebarTabs v-model="activeTab" />
        </div>

        <!-- 탭 별 콘텐츠 영역 (공간 자동 차지) -->
        <div class="flex-1 overflow-hidden flex flex-col bg-white">
          <!-- 1. 참여자 상태 목록 (v-show로 제어) -->
          <div v-show="activeTab === 'status'" class="flex-1 overflow-hidden">
            <ParticipantItem />
          </div>

          <!-- 2. 채팅/질문 패널 영역 (Transition으로 제어) -->
          <div v-show="activeTab !== 'status'" class="flex-1 overflow-hidden">
            <Transition name="fade" mode="out-in">
              <!-- 2-1. 채팅 패널 -->
              <TrainerLiveChatContainer v-if="activeTab === 'chat'" key="tab-chat" :chatList="chatList" :qaList="qaList"
                :viewType="'chat'" @send-message="sendChatMessage" />
              <!-- 2-2. 질문 패널 -->
              <TrainerLiveChatContainer v-else key="tab-qa" :chatList="chatList" :qaList="qaList" :viewType="'qa'"
                @send-message="sendChatMessage" />
            </Transition>
          </div>
        </div>
        <!-- 하단 섹션: 학생 모니터링 (동적) & 조작 패널 -->
        <div class="flex flex-col shrink-0">
          <!-- 학생 클릭 시에만 부드럽게 나타나는 모니터링 창 -->
          <Transition name="slide-up">
            <div v-if="trainerLiveStore.selectedStudentId"
              class="px-6 pb-6 pt-2 border-t border-slate-50 bg-white/50 backdrop-blur-sm">
              <StudentMonitor />
            </div>
          </Transition>
          <LiveControlPanel />
        </div>
      </aside>
    </div>

    <!-- 수업 종료 모달 -->
    <Transition name="fade">
      <div v-if="isEndModalOpen"
        class="fixed inset-0 z-100 flex items-center justify-center bg-slate-900/60 backdrop-blur-sm p-4">
        <div class="w-full max-w-md bg-white rounded-[2.5rem] p-10 shadow-2xl border border-slate-100">
          <div class="text-center">
            <div class="size-20 bg-red-50 text-red-500 rounded-3xl flex items-center justify-center mx-auto mb-6">
              <span class="material-symbols-outlined text-5xl font-light">logout</span>
            </div>
            <h3 class="text-2xl font-bold text-slate-900 mb-3">수업을 종료하시겠습니까?</h3>
            <p class="text-slate-500 mb-10 leading-relaxed font-medium">
              종료 시 모든 학생과의 연결이 해제되며<br />AI 분석 리포트가 최종 저장됩니다.
            </p>
          </div>
          <div class="flex gap-4">
            <button @click="isEndModalOpen = false"
              class="flex-1 py-4 bg-slate-100 text-slate-600 rounded-2xl font-bold hover:bg-slate-200 transition-all">취소</button>
            <button @click="handleEndSession"
              class="flex-1 py-4 bg-red-500 text-white rounded-2xl font-bold hover:bg-red-600 transition-all shadow-lg shadow-red-200">수업
              종료</button>
          </div>
        </div>
      </div>
    </Transition>

    <!-- 퀵 퀴즈 모달 -->
    <QuickQuizModal :isOpen="isQuickQuizModalOpen" :quizData="quizData" @close="handleQuickQuizClose"
      @send-to-students="handleQuickQuizSend" />
  </div>
</template>

<style scoped>
/* 커스텀 스크롤바 */
.custom-scrollbar::-webkit-scrollbar {
  width: 4px;
}

.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}

.custom-scrollbar::-webkit-scrollbar-thumb {
  background: #e2e8f0;
  border-radius: 10px;
}

.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background: #cbd5e1;
}

/* 공통 페이드 애니메이션 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 사이드바 전용 슬라이딩 애니메이션 */
.slide-up-enter-active,
.slide-up-leave-active {
  transition: all 0.4s cubic-bezier(0.16, 1, 0.3, 1);
}

.slide-up-enter-from,
.slide-up-leave-to {
  transform: translateY(20px);
  opacity: 0;
  max-height: 0;
  margin-top: 0;
}
</style>