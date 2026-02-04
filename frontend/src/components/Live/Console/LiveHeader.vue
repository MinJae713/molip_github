<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';
import { useTrainerLiveStore } from '@/stores/trainerlive';
import { useUserStore } from '@/stores/user';
import { useTimer } from '@/composables/useTimer';
import { useRecording } from '@/composables/useRecording';
import { useRoute } from 'vue-router';

// [1] 스토어 및 컴포넌트 선언
const trainerLiveStore = useTrainerLiveStore();
const userStore = useUserStore();
const { recordingTime, startRecording } = useRecording();
const { formattedTime, timeLeft } = useTimer();

const route = useRoute();

// [2] 부모(InstructorConsole)로 보낼 이벤트 선언
defineEmits(['end-class']);

// [3] 상태 관리
const isBreakEndModalOpen = ref(false);

// [강의 시작 핸들러]
const handleStartLive = async () => {
  const sId = String(route.params.sessionId);
  if (!trainerLiveStore.janus) {
    await trainerLiveStore.initJanus(sId);
  }
  await trainerLiveStore.startSession();
};

// [분석 시작 핸들러]
const handleStartAnalysis = async () => {
  await trainerLiveStore.startAnalysis();
  startRecording();
};

onMounted(async () => {
  // 컴포넌트 마운트 시 유저 정보 fetch (이 부분이 있어야 프로필이 뜹니다!)
  await userStore.fetchUserInfo();
});
</script>

<template>
  <header class="h-18 bg-white border-b border-slate-200 flex items-center justify-between px-6 shrink-0 z-50">

    <div class="flex items-center gap-3">
      <div
        class="size-10 bg-indigo-600 rounded-xl flex items-center justify-center text-white shadow-lg shadow-indigo-200">
        <span class="material-symbols-outlined">school</span>
      </div>
      <div class="flex flex-col">
        <div class="flex items-center gap-2">
          <h1 class="text-lg font-bold text-slate-900">
            강의실 {{ trainerLiveStore.roomInfo?.janusRoomId || '' }}
          </h1>
          <span v-if="trainerLiveStore.isAnalysisStarted"
            class="text-[10px] font-bold text-red-500 bg-red-50 px-1.5 py-0.5 rounded animate-pulse">ON AIR</span>
          <span v-else class="text-[10px] font-bold text-slate-400 bg-slate-100 px-1.5 py-0.5 rounded">READY</span>
        </div>
        <p class="text-xs text-slate-400 font-medium">실시간 AI 분석 세션</p>
      </div>
    </div>

    <div class="flex items-center gap-4">
      <div class="flex items-center gap-3">
        <!-- 1단계: 강의 시작 전 -->
        <button v-if="!trainerLiveStore.isSessionStarted" @click="handleStartLive"
          class="bg-indigo-600 hover:bg-indigo-700 text-white px-6 py-2.5 rounded-xl text-sm font-bold transition-all shadow-lg shadow-indigo-100 flex items-center gap-2">
          <span class="material-symbols-outlined text-sm">play_circle</span>
          강의 시작
        </button>

        <!-- 2단계: 강의는 켰지만 분석 전 -->
        <template v-else-if="!trainerLiveStore.isAnalysisStarted">
          <button @click="handleStartAnalysis"
            class="bg-rose-500 hover:bg-rose-600 text-white px-6 py-2.5 rounded-xl text-sm font-bold transition-all shadow-lg shadow-rose-100 flex items-center gap-2 active:scale-95">
            <span class="material-symbols-outlined text-sm">analytics</span>
            분석 시작
          </button>
          <button @click="$emit('end-class')"
            class="bg-slate-800 hover:bg-red-600 text-white px-5 py-2.5 rounded-xl text-sm font-bold transition-all shadow-md shadow-slate-200 active:scale-95">
            수업 종료
          </button>
        </template>

        <!-- 3단계: 분석 중 -->
        <template v-else>
          <div class="flex items-center gap-2 bg-red-50 px-3 py-2 rounded-xl border border-red-100">
            <div class="size-2 bg-red-500 rounded-full animate-pulse"></div>
            <div class="flex flex-col leading-none">
              <span class="text-[9px] font-black text-red-500 uppercase tracking-tighter">REC</span>
              <span class="text-[11px] font-mono font-bold text-red-600">{{ recordingTime }}</span>
            </div>
          </div>

          <button @click="$emit('end-class')"
            class="bg-slate-800 hover:bg-red-600 text-white px-5 py-2.5 rounded-xl text-sm font-bold transition-all shadow-md shadow-slate-200 active:scale-95">
            수업 종료
          </button>
        </template>
      </div>

      <div class="flex items-center gap-3 ml-2 border-l border-slate-100 pl-4">
        <div class="hidden sm:flex flex-col items-end leading-tight">
          <span class="text-xs font-bold text-slate-800">{{ userStore.user?.name || '강사' }}</span>
          <span class="text-[10px] text-slate-400">{{ userStore.user?.email || '로딩 중...' }}</span>
        </div>
        <button
          class="size-10 rounded-full border-2 border-slate-100 overflow-hidden hover:border-indigo-500 transition-all shadow-sm">
          <img :src="userStore.user?.profileImageUrl || 'https://i.pravatar.cc/100'" :alt="userStore.user?.name"
            class="w-full h-full object-cover" />
        </button>
      </div>
    </div>

    <Transition name="fade">
      <div v-if="isBreakEndModalOpen"
        class="fixed inset-0 z-110 flex items-center justify-center bg-slate-900/60 backdrop-blur-sm p-4">
        <div class="bg-white rounded-[2.5rem] p-8 max-w-sm w-full text-center shadow-2xl border border-slate-100">
          <div class="size-20 bg-orange-50 text-orange-500 rounded-3xl flex items-center justify-center mx-auto mb-6">
            <span class="material-symbols-outlined text-5xl">notifications_active</span>
          </div>
          <h3 class="text-2xl font-bold text-slate-900 mb-2">Break Time Over!</h3>
          <p class="text-slate-500 mb-8 leading-relaxed font-medium">쉬는 시간이 모두 경과되었습니다. 다시 활기찬 수업을 시작해 볼까요?</p>
          <button @click="isBreakEndModalOpen = false"
            class="w-full py-4 bg-indigo-600 text-white rounded-2xl font-bold">수업
            재개하기</button>
        </div>
      </div>
    </Transition>
  </header>
</template>

<style scoped>
/* 애니메이션 스타일 생략 (동일) */
.animate-spin-slow {
  animation: spin 4s linear infinite;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }

  to {
    transform: rotate(360deg);
  }
}

.slide-fade-enter-active,
.slide-fade-leave-active {
  transition: all 0.4s cubic-bezier(0.16, 1, 0.3, 1);
}

.slide-fade-enter-from,
.slide-fade-leave-to {
  transform: translateX(30px);
  opacity: 0;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>``