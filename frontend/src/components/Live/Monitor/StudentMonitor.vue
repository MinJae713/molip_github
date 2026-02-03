<script setup lang="ts">
import { computed } from 'vue';
import { useTrainerLiveStore } from '@/stores/trainerlive';

const trainerLiveStore = useTrainerLiveStore();

const selectedStudent = computed(() =>
  trainerLiveStore.participants.find(p => p.id === trainerLiveStore.selectedStudentId)
);
</script>

<template>
  <div v-if="selectedStudent"
    class="bg-white rounded-3xl border border-slate-100 shadow-2xl overflow-hidden flex flex-col h-full ring-1 ring-slate-100 transition-all">
    <div class="px-5 py-3 flex items-center justify-between border-b border-slate-50">
      <div class="flex items-center gap-2">
        <span class="material-symbols-outlined text-indigo-500 text-[18px]">monitor</span>
        <h3 class="font-bold text-slate-800 text-[13px]">모니터링: {{ selectedStudent.name }}</h3>
      </div>
      <button @click="trainerLiveStore.selectedStudentId = null"
        class="text-slate-300 hover:text-slate-500 transition-colors">
        <span class="material-symbols-outlined text-[18px]">close</span>
      </button>
    </div>

    <div class="relative w-full aspect-video bg-slate-900 overflow-hidden group">
      <!-- 실제 웹알티씨 스트림이 들어갈 자리 -->
      <video
        :srcObject="trainerLiveStore.selectedStudentId ? trainerLiveStore.studentStreams.get(trainerLiveStore.selectedStudentId) : null"
        autoplay playsinline class="w-full h-full object-cover"></video>

      <div
        class="absolute top-3 right-3 bg-black/40 backdrop-blur-md px-2 py-1 rounded-lg border border-white/10 flex items-center gap-1.5">
        <span class="size-1.5 bg-red-500 rounded-full animate-pulse"></span>
        <p class="text-[9px] text-white font-black tracking-tighter uppercase">Attention: {{
          selectedStudent.concentrationScore }}%</p>
      </div>

      <!-- 오버레이 안내 (카메라가 꺼져 있을 때) -->
      <div v-if="!selectedStudent.isCameraOn"
        class="absolute inset-0 flex flex-col items-center justify-center bg-slate-950/80">
        <span class="material-symbols-outlined text-3xl text-slate-700 mb-2">videocam_off</span>
        <p class="text-[10px] font-bold text-slate-500 uppercase tracking-widest">No Stream Available</p>
      </div>
    </div>

    <div class="p-4 bg-white">
      <button
        class="w-full py-3.5 bg-indigo-600 text-white rounded-2xl font-bold text-[11px] flex items-center justify-center gap-2 hover:bg-indigo-700 transition-all active:scale-95 shadow-lg shadow-indigo-100">
        <span class="material-symbols-outlined text-base">notifications_active</span>
        경고 보내기
      </button>
    </div>
  </div>
</template>