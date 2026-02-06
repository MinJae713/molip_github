<script setup lang="ts">
import { useTrainerLiveStore } from '@/stores/trainerlive';
import MediaButton from '../Monitor/MediaButton.vue';

const trainerLiveStore = useTrainerLiveStore();
</script>

<template>
  <div class="flex flex-col bg-slate-950 rounded-[2.5rem] overflow-hidden shadow-2xl border-4 border-white shrink-0">

    <div class="relative w-full aspect-video bg-black flex items-center justify-center overflow-hidden">

      <template v-if="trainerLiveStore.isAnalysisStarted && trainerLiveStore.screenStream">
        <video :srcObject="trainerLiveStore.screenStream" autoplay class="w-full h-full object-contain"></video>
      </template>

      <div v-else class="flex flex-col items-center text-slate-700">
        <span class="material-symbols-outlined text-6xl mb-3">present_to_all</span>
        <p class="font-bold text-slate-500 uppercase tracking-widest text-[10px]">수업 시작 전 (화면 공유 대기)</p>
      </div>
    </div>

    <div class="bg-slate-900 px-8 py-4 border-t border-slate-800 flex items-center justify-between">

      <div class="flex items-center gap-4">
        <!-- 카메라/마이크 제어 버튼은 현재 사용 안함 (추후 필요시 복구) -->

        <button v-if="trainerLiveStore.isAnalysisStarted" @click="trainerLiveStore.changeScreenShare()"
          class="flex items-center gap-2 px-6 py-2.5 bg-slate-700 text-white rounded-full font-bold text-sm hover:bg-slate-600 transition-all border border-slate-600">
          <span class="material-symbols-outlined text-[20px]">switch_video</span>
          화면 변경
        </button>
      </div>

      <div class="flex items-center gap-3 px-6 py-2.5 bg-black/30 rounded-full border border-white/5">
        <span class="text-indigo-400 material-symbols-outlined text-[20px]">psychology</span>
        <span class="text-slate-300 text-sm font-medium italic">
          {{ trainerLiveStore.isAnalysisStarted ? 'AI 실시간 분석 중...' : 'AI 분석 대기' }}
        </span>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 강사 본인 화면은 거울 모드 적용 X (화면 공유이므로) */
/* PIP 나타날 때 부드러운 페이드 효과 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

video {
  background-color: #000;
}
</style>