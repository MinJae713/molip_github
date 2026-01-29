<script setup lang="ts">
import { ref, watch, computed, onBeforeUnmount } from 'vue';
import { useLiveStore } from '@/stores/live';

const liveStore = useLiveStore();
const monitorVideoRef = ref<HTMLVideoElement | null>(null);

// [중요] 모니터링 중인 학생 객체 찾기
// 스토어의 participants 목록에서 현재 monitoringStudentId와 일치하는 학생 정보를 실시간으로 가져옵니다.
const targetStudent = computed(() => 
  liveStore.participants.find(p => Number(p.id) === liveStore.monitoringStudentId)
);

// 모니터링 스트림 연결 (srcObject 주입)
watch(() => liveStore.monitoringStream, (stream) => {
  if (monitorVideoRef.value) {
    // Vue의 반응형 시스템과 비디오 태그를 연결하는 핵심 코드
    monitorVideoRef.value.srcObject = stream;
  }
}, { immediate: true });

// 컴포넌트가 사라지기 전에 모니터링 정리 (리소스 해제 안전장치)
onBeforeUnmount(() => {
  liveStore.stopMonitoring();
});
</script>

<template>
  <div v-if="liveStore.monitoringStudentId && targetStudent" 
       class="bg-white rounded-3xl border border-slate-100 shadow-2xl overflow-hidden flex flex-col h-full ring-1 ring-slate-100 transition-all">
    
    <div class="px-5 py-3 flex items-center justify-between border-b border-slate-50">
      <div class="flex items-center gap-2">
        <span class="material-symbols-outlined text-indigo-500 text-[18px]">monitor</span>
        <h3 class="font-bold text-slate-800 text-[13px]">
          모니터링: {{ targetStudent.name }}
        </h3>
      </div>
      <button @click="liveStore.stopMonitoring()" class="text-slate-300 hover:text-slate-500 transition-colors">
        <span class="material-symbols-outlined text-[18px]">close</span>
      </button>
    </div>

    <div class="relative w-full aspect-video bg-slate-900 overflow-hidden group">
      <video 
        ref="monitorVideoRef"
        autoplay 
        playsinline 
        class="w-full h-full object-cover opacity-80 group-hover:opacity-100 transition-opacity"
      ></video>
      
      <div class="absolute top-3 right-3 bg-black/40 backdrop-blur-md px-2 py-1 rounded-lg border border-white/10 flex items-center gap-1.5">
        <span class="size-1.5 bg-red-500 rounded-full animate-pulse"></span>
        <p class="text-[9px] text-white font-black tracking-tighter uppercase">
          Attention: {{ targetStudent.concentrationScore }}%
        </p>
      </div>

      <div v-if="!liveStore.monitoringStream" class="absolute inset-0 flex flex-col items-center justify-center bg-slate-950/80">
        <span class="material-symbols-outlined text-3xl text-slate-700 mb-2">videocam_off</span>
        <p class="text-[10px] font-bold text-slate-500 uppercase tracking-widest">Connecting Stream...</p>
      </div>
    </div>
    
    <div class="p-4 bg-white">
      <button 
        class="w-full py-3.5 bg-indigo-600 text-white rounded-2xl font-bold text-[11px] flex items-center justify-center gap-2 hover:bg-indigo-700 transition-all active:scale-95 shadow-lg shadow-indigo-100"
      >
        <span class="material-symbols-outlined text-base">notifications_active</span>
        경고 보내기
      </button>
    </div>
  </div>
</template>