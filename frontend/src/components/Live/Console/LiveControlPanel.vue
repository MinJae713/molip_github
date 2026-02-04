<script setup lang="ts">
import { ref, inject } from 'vue';
import { useVideoRoomStore } from '@/stores/videoRoom'; // Unified Store
import { useQuizStore } from '@/stores/quiz';
import QuizGenerationModal from '@/components/Live/Sidebar/QuizGenerationModal.vue';
import { useTimer } from '@/composables/useTimer';

const videoRoomStore = useVideoRoomStore();
const quizStore = useQuizStore();
const { startTimer } = useTimer();
const getQuiz = inject<() => void>('getQuiz');

// 모달 상태 관리 (쉬는 시간은 로컬, 퀴즈는 스토어)
const isTimeModalOpen = ref(false);
const timeOptions = [5, 10, 15, 20];

// 쉬는 시간 시작 함수
const handleStartBreak = (minutes: number) => {
  // liveStore.isBreakTime = true; // TODO: videoRoomStore에 쉬는 시간 로직 추가 필요
  startTimer(minutes * 60);
  isTimeModalOpen.value = false;
  console.log(`${minutes}분 쉬는 시간 시작 (임시: 기능 미연동)`);
};

// 퀴즈 모달 열기 함수 (Store 액션 활용)
const openQuizModal = () => {
  quizStore.toggleModal(true); // store에 정의한 toggleModal 함수 호출
};

// 퀵 퀴즈 요청 함수
const handleQuickQuiz = () => {
  if (getQuiz) {
    getQuiz();
    console.log('[LiveControlPanel] Quick quiz requested');
  } else {
    console.error('[LiveControlPanel] getQuiz function not available');
  }
};
</script>

<template>
  <div
    class="relative bg-white border-t border-slate-100 p-6 flex flex-col gap-4 shadow-[0_-4px_20px_rgba(0,0,0,0.03)]">

    <Transition name="pop">
      <div v-if="isTimeModalOpen"
        class="absolute bottom-full left-6 right-6 mb-4 bg-white rounded-4xl shadow-2xl border border-slate-100 p-6 z-60">
        <div class="flex items-center justify-between mb-4">
          <span class="text-sm font-bold text-slate-800">쉬는 시간 설정</span>
          <button @click="isTimeModalOpen = false" class="text-slate-400 hover:text-slate-600">
            <span class="material-symbols-outlined text-lg">close</span>
          </button>
        </div>

        <div class="grid grid-cols-2 gap-3">
          <button v-for="min in timeOptions" :key="min" @click="handleStartBreak(min)"
            class="py-3 rounded-2xl bg-slate-50 text-slate-700 font-bold text-sm hover:bg-indigo-50 hover:text-indigo-600 transition-all border border-transparent hover:border-indigo-100">
            {{ min }}분
          </button>
        </div>

        <div class="mt-4 pt-4 border-t border-slate-50 flex items-center gap-2">
          <span class="material-symbols-outlined text-indigo-400 text-sm">info</span>
          <p class="text-[11px] text-slate-400 font-medium tracking-tight">시작 즉시 학생들에게 알림이 전송됩니다.</p>
        </div>
      </div>
    </Transition>

    <div class="space-y-3">
      <div class="flex items-center justify-between group">
        <div class="flex items-center gap-2">
          <span class="material-symbols-outlined text-slate-400 text-lg">videocam_off</span>
          <span class="text-sm font-bold text-slate-600">학생 카메라 강제 끄기</span>
        </div>
        <label class="relative inline-flex items-center cursor-pointer">
          <input type="checkbox" class="sr-only peer">
          <div
            class="w-10 h-5 bg-slate-200 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-0.5 after:left-0.5 after:bg-white after:border-gray-300 after:border after:rounded-full after:h-4 after:w-4 after:transition-all peer-checked:bg-indigo-600">
          </div>
        </label>
      </div>

      <div class="flex items-center justify-between group">
        <div class="flex items-center gap-2">
          <span class="material-symbols-outlined text-slate-400 text-lg">chat_bubble_outline</span>
          <span class="text-sm font-bold text-slate-600">채팅 잠금</span>
        </div>
        <label class="relative inline-flex items-center cursor-pointer">
          <input type="checkbox" class="sr-only peer">
          <div
            class="w-10 h-5 bg-slate-200 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-0.5 after:left-0.5 after:bg-white after:border-gray-300 after:border after:rounded-full after:h-4 after:w-4 after:transition-all peer-checked:bg-indigo-600">
          </div>
        </label>
      </div>
    </div>

    <button
      class="w-full py-3 bg-red-50 text-red-500 rounded-2xl font-bold flex items-center justify-center gap-2 hover:bg-red-100 transition-colors border border-red-100 active:scale-[0.98]">
      <span class="material-symbols-outlined text-xl">mic_off</span>
      전체 음소거
    </button>

    <div class="grid grid-cols-3 gap-3">
      <button @click="isTimeModalOpen = !isTimeModalOpen" :class="[
        'flex items-center justify-center gap-2 py-3 rounded-xl text-xs font-bold border transition-all active:scale-95',
        isTimeModalOpen ? 'bg-indigo-50 border-indigo-200 text-indigo-600' : 'bg-slate-50 border-slate-100 text-slate-700 hover:bg-slate-100'
      ]">
        <span class="material-symbols-outlined text-lg">coffee</span>
        쉬는 시간
      </button>

      <button @click="handleQuickQuiz"
        class="flex items-center justify-center gap-2 py-3 bg-purple-600 text-white rounded-xl text-xs font-bold shadow-lg shadow-purple-100 hover:bg-purple-700 transition-all active:scale-95">
        <span class="material-symbols-outlined text-lg">bolt</span>
        퀵 퀴즈
      </button>

      <button @click="openQuizModal"
        class="flex items-center justify-center gap-2 py-3 bg-indigo-600 text-white rounded-xl text-xs font-bold shadow-lg shadow-indigo-100 hover:bg-indigo-700 transition-all active:scale-95">
        <span class="material-symbols-outlined text-lg">quiz</span>
        퇴실 퀴즈
      </button>

      <QuizGenerationModal />
    </div>
  </div>
</template>

<style scoped>
/* 팝업 애니메이션 */
.pop-enter-active,
.pop-leave-active {
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.pop-enter-from,
.pop-leave-to {
  opacity: 0;
  transform: translateY(10px) scale(0.95);
}
</style>