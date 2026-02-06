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

// 스토어에서 채팅/퀴즈 데이터 가져오기 (WebSocket은 store에서 관리)
const chatList = computed(() => trainerLiveStore.chatList);
const qaList = computed(() => trainerLiveStore.qaList);
const quizData = computed(() => trainerLiveStore.quizData);

const analysisData = ref<any>(null); // ai 분석 데이터 (UI에서만 사용)
const engagementHistory = ref<number[]>(new Array(120).fill(0)); // 몰입도 이력 (최대 120개)

// // --- 통계 (Computed) ---
// const focusCount = computed(() => trainerLiveStore.participants.filter(p => p.status === 'focus').length);

const emotionMap: Record<string, { label: string, emoji: string, color: string }> = {
  happy: { label: '행복 (Happy)', emoji: '😊', color: 'bg-yellow-400' },
  sad: { label: '슬픔 (Sad)', emoji: '😢', color: 'bg-blue-400' },
  angry: { label: '분노 (Angry)', emoji: '😠', color: 'bg-red-500' },
  surprise: { label: '놀람 (Surprise)', emoji: '😲', color: 'bg-purple-400' },
  fear: { label: '공포 (Fear)', emoji: '😨', color: 'bg-indigo-700' },
  disgust: { label: '혐오 (Disgust)', emoji: '🤢', color: 'bg-green-500' },
  neutral: { label: '평온 (Neutral)', emoji: '😐', color: 'bg-slate-400' }
};

const processedEmotions = computed(() => {
  if (!analysisData.value?.emotionMessage) return [];

  const msg = analysisData.value.emotionMessage;
  const emotions: { key: string; confidence: number }[] = [];

  for (let i = 1; i <= 7; i++) {
    const emotionKey = (msg as any)[`emotion${i}`];
    const confidence = (msg as any)[`confidence${i}`];
    if (emotionKey && emotionMap[emotionKey]) {
      emotions.push({
        key: emotionKey,
        confidence: confidence || 0
      });
    }
  }
  return emotions
    .sort((a, b) => b.confidence - a.confidence)
    .slice(0, 2)
    .map(emo => {
      const key = emo.key as keyof typeof emotionMap;
      const data = emotionMap[key];
      return {
        label: data?.label || 'Unknown',
        emoji: data?.emoji || '❓',
        percentage: Math.round(emo.confidence), // HEAD used this, keeping consistent
        color: data?.color || 'bg-slate-400'
      };
    });
});

// 몰입도 데이터 변화 감시 및 큐 업데이트
watch(() => analysisData.value?.concentrateMessage, (newMsg) => {
  if (newMsg && typeof newMsg.concentration === 'number') {
    engagementHistory.value.push(newMsg.concentration);
    // 최대 120개 유지 (2분 분량)
    if (engagementHistory.value.length > 120) {
      engagementHistory.value.shift();
    }
  }
});

// [Merge Resolution] Added from fe-develop, mapped to trainerlive types
const warningCount = computed(() => trainerLiveStore.participants.filter(p => p.status === 'NORMAL').length);
const absentCount = computed(() => trainerLiveStore.participants.filter(p => p.status === 'EMPTY').length);

// --- AI 분석 결과만 구독 (채팅/퀴즈는 스토어에서 처리) ---
let stompClient: Client | null = null;

const connectAnalysisWebSocket = () => {
  const sessionId = route.params.sessionId as string;
  const socketUrl = import.meta.env.VITE_CHAT_SOCKET_URL;

  stompClient = new Client({
    webSocketFactory: () => new SockJS(socketUrl),
    connectHeaders: {
      Authorization: `Bearer ${localStorage.getItem('accessToken')}`
    },
    debug: () => { }, // 디버그 비활성화
    onConnect: () => {
      console.log('[TrainerLiveSessionView] AI Analysis Connected ✅');
      // AI 분석결과만 구독 (UI 업데이트용)
      stompClient?.subscribe(`/topic/analysis/${sessionId}`, (res) => {
        console.log('[Trainer Analysis] Received:', res.body);
        const data = JSON.parse(res.body);
        analysisData.value = data;

        // ✅ stateChangeList 처리: 학생 상태 변경 반영
        if (data.stateChangeList && Array.isArray(data.stateChangeList)) {
          data.stateChangeList.forEach((change: { memberId: number; newState: string }) => {
            console.log(`[Status Change] memberId: ${change.memberId}, newState: ${change.newState}`);
            trainerLiveStore.updateParticipantStatus(change.memberId, change.newState as "NORMAL" | "FEVER" | "EMPTY");
          });
        }
      });
    },
    onStompError: (frame) => {
      console.error('[TrainerLiveSessionView] Broker error:', frame.headers['message']);
    },
    onWebSocketClose: () => {
      console.log('[TrainerLiveSessionView] AI Analysis Connection Closed ❌');
    }
  });

  stompClient.activate();
};

// --- 메시지 전송 래퍼 함수 (템플릿에서 사용) ---
const sendChatMessage = (content: string, type: string, questionId?: number | string | null) => {
  trainerLiveStore.sendChatMessage(content, type, questionId);
};

// --- 메시지 전송 함수는 스토어에서 제공 ---
// provide로 하위 컴포넌트에 스토어 함수 전달
provide('sendQuizToStudents', trainerLiveStore.sendQuizToStudents);
provide('getQuiz', trainerLiveStore.getQuiz);

// --- 퀵 퀴즈 모달 관리 ---
// quizData가 업데이트되면 모달 자동 열기
watch(quizData, (newData) => {
  console.log('[TrainerLiveSessionView] quizData watch 감지:', newData);
  if (newData && Object.keys(newData).length > 0) {
    console.log('[TrainerLiveSessionView] 퀵 퀴즈 모달 열기');
    isQuickQuizModalOpen.value = true;
  } else {
    console.log('[TrainerLiveSessionView] quizData가 비어있음');
  }
});

const handleQuickQuizClose = () => {
  isQuickQuizModalOpen.value = false;
};

const handleQuickQuizSend = (quiz: any) => {
  trainerLiveStore.sendQuizToStudents(quiz);
  isQuickQuizModalOpen.value = false;
  alert('퀴즈가 학생들에게 전달되었습니다.');
};

onMounted(async () => {
  const sessionId = route.params.sessionId as string;
  await trainerLiveStore.initJanus(sessionId);
  trainerLiveStore.openSession(sessionId); // [New] 세션 시작 알림
  connectAnalysisWebSocket();
});

onUnmounted(() => {
  const sessionId = route.params.sessionId as string;
  trainerLiveStore.closeSession(sessionId); // [New] 세션 종료 알림
  stompClient?.deactivate();
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

        <!-- 📹 학생 모니터링 (선택 시에만 표시) -->
        <StudentMonitor v-if="trainerLiveStore.selectedStudentId" />

        <div class="grid grid-cols-12 gap-6 min-h-70">
          <GlobalEngagementCard class="col-span-7" :history="engagementHistory" />
          <SessionEmotionAnalysis class="col-span-5" :emotions="processedEmotions" />
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

          <!-- <SidebarStats :focus-count="focusCount" :warning-count="warningCount" :absent-count="absentCount" /> -->
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