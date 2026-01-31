<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useLiveStore } from '@/stores/live';

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
const liveStore = useLiveStore();

// --- 초기화 로직 ---
onMounted(async () => {
  // route.params의 값은 항상 string이므로 명시적으로 변환이 필요합니다.
  const courseId = Number(route.params.courseId);
  const sessionId = Number(route.params.sessionId);
  
  try {
    if (!isNaN(courseId) && !isNaN(sessionId)) {
      // 스토어의 액션에 number 타입을 전달합니다.
      await liveStore.initLiveRoom(courseId, sessionId);
    } else {
      console.error("유효하지 않은 ID 값입니다.");
    }
  } catch (error) {
    console.error('라이브 입장 실패:', error);
  }
});

// --- UI 상태 관리 ---
const isEndModalOpen = ref(false); 
const activeTab = ref<'status' | 'chat' | 'qna'>('status');

// --- 수업 종료 로직 ---
const handleEndSession = async () => {
  await liveStore.endLiveSession();
  isEndModalOpen.value = false;
  router.push('/trainer/dashboard'); //고쳐야함
};

/** 학생 클릭 시 모니터링 시작 핸들러 */
const handleStudentClick = (student: any) => {
  liveStore.startMonitoring(Number(student.id));
};
</script>

<template>
  <div class="h-screen bg-slate-50 flex flex-col overflow-hidden text-slate-900">
    <LiveHeader @end-class="isEndModalOpen = true" />
    
    <div class="flex-1 flex overflow-hidden">
      <main class="flex-1 flex flex-col p-6 gap-6 overflow-y-auto bg-slate-50 custom-scrollbar">
        <LiveVideoConsole />
        
        <div class="grid grid-cols-12 gap-6 min-h-[280px]">
          <AIActionRecommendation class="col-span-4" />
          <GlobalEngagementCard class="col-span-3" />
          <SessionEmotionAnalysis class="col-span-5" />
        </div>
      </main>

      <aside class="w-96 bg-white border-l border-slate-200 flex flex-col shadow-sm shrink-0 overflow-hidden">
        <div class="p-7 pb-0 space-y-6 shrink-0">
          <div class="flex items-center justify-between px-1">
            <h2 class="text-xl font-bold text-slate-800">
              참여자 목록 
              <span class="text-indigo-600 ml-1 text-base">{{ liveStore.participants?.length || 0 }}</span>
            </h2>
            <button class="p-2 hover:bg-slate-100 rounded-xl transition-colors text-slate-500">
              <span class="material-symbols-outlined text-xl">tune</span>
            </button>
          </div>

          <SidebarStats />
          <SidebarTabs v-model="activeTab" />
        </div>

        <div class="flex-1 overflow-hidden flex flex-col bg-white">
          <!-- Transition 제거 및 v-show 적용으로 컴포넌트 유지 (연결 끊김 방지) -->
          <div v-show="activeTab === 'status'" class="flex-1 overflow-y-auto p-7 pt-4 space-y-2 custom-scrollbar">
            <div v-if="liveStore.participants?.length > 0" class="flex flex-col gap-2">
              <ParticipantItem 
                v-for="p in liveStore.participants" 
                :key="p.id" 
                :participant="p"
                @click="handleStudentClick(p)"
                class="cursor-pointer hover:ring-2 hover:ring-indigo-100 transition-all rounded-2xl active:scale-[0.98]" 
              />
            </div>
            <div v-else class="h-full flex flex-col items-center justify-center text-slate-400 opacity-60">
              <span class="material-symbols-outlined text-5xl mb-2">group_off</span>
              <p class="text-sm font-medium tracking-tight">참여자가 없습니다.</p>
            </div>
          </div>

          <div v-show="activeTab !== 'status'" class="flex-1 flex flex-col min-h-0">
              <TrainerLiveChatContainer 
                :room-id="String(route.params.sessionId)" 
                :view-type="activeTab === 'chat' ? 'chat' : 'qa'" 
              />
          </div>
        </div>

        <div class="flex flex-col shrink-0">
          <Transition name="slide-up">
            <div v-if="liveStore.monitoringStudentId"
              class="px-6 pb-6 pt-2 border-t border-slate-50 bg-white/50 backdrop-blur-sm">
              <StudentMonitor />
            </div>
          </Transition>

          <LiveControlPanel />
        </div>
      </aside>
    </div>

    <Transition name="fade">
      <div v-if="isEndModalOpen" class="fixed inset-0 z-[100] flex items-center justify-center bg-slate-900/60 backdrop-blur-sm p-4">
        <div class="bg-white p-8 rounded-[40px] shadow-2xl max-w-sm w-full text-center">
          <span class="material-symbols-outlined text-red-500 text-6xl mb-4">cancel</span>
          <h2 class="text-2xl font-bold mb-2">수업을 종료할까요?</h2>
          <p class="text-slate-500 mb-8">종료 후에는 다시 강의실을 생성해야 합니다.</p>
          <div class="flex gap-4">
            <button @click="isEndModalOpen = false" class="flex-1 py-4 bg-slate-100 rounded-2xl font-bold">취소</button>
            <button @click="handleEndSession" class="flex-1 py-4 bg-red-500 text-white rounded-2xl font-bold">수업 종료</button>
          </div>
        </div>
      </div>
    </Transition>
  </div>
</template>

<style scoped>
/* 애니메이션 스타일 */
.slide-up-enter-active, .slide-up-leave-active {
  transition: all 0.4s cubic-bezier(0.16, 1, 0.3, 1);
}
.slide-up-enter-from, .slide-up-leave-to {
  transform: translateY(20px);
  opacity: 0;
  max-height: 0;
  margin-top: 0;
}
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s ease;
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
}
</style>