<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useTrainerLiveStore } from '@/stores/trainerlive';

// 스토어
const trainerLiveStore = useTrainerLiveStore();

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

const router = useRouter();
const route = useRoute();

// --- UI 상태 관리 ---
const isEndModalOpen = ref(false); // 수업 종료 확인 모달
const activeTab = ref<'status' | 'chat' | 'qna'>('status'); // 사이드바 활성 탭 (기본: 상태)

// --- 통계 (Computed) ---
const focusCount = computed(() => trainerLiveStore.participants.filter(p => p.status === 'focus').length);
const warningCount = computed(() => trainerLiveStore.participants.filter(p => p.status === 'distracted').length); // distracted status check
const absentCount = computed(() => trainerLiveStore.participants.filter(p => p.status === 'away').length);

onMounted(async () => {
  const sessionId = route.params.sessionId as string;
  await trainerLiveStore.initJanus(sessionId);
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
              <TrainerLiveChatContainer v-if="activeTab === 'chat'" key="tab-chat"
                :roomId="(route.params.sessionId as string)" :viewType="'chat'" />
              <!-- 2-2. 질문 패널 -->
              <TrainerLiveChatContainer v-else key="tab-qa" :roomId="(route.params.sessionId as string)"
                :viewType="'qa'" />
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