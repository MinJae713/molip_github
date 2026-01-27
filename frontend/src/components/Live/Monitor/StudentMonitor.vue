<script setup lang="ts">
import { useLiveStore } from '@/stores/live';
const liveStore = useLiveStore();
</script>

<template>
  <div v-if="liveStore.selectedStudent" class="bg-white rounded-4xl border border-slate-100 shadow-xl overflow-hidden flex flex-col h-full">
    <div class="p-4 flex items-center justify-between border-b border-slate-50">
      <div class="flex items-center gap-2">
        <span class="size-2 bg-emerald-500 rounded-full"></span>
        <h3 class="font-bold text-slate-800 text-sm">모니터링: {{ liveStore.selectedStudent.name }}</h3>
      </div>
      <button @click="liveStore.selectedStudent = null" class="text-slate-400 hover:text-slate-600">
        <span class="material-symbols-outlined text-sm">close</span>
      </button>
    </div>

    <div class="flex-1 bg-slate-900 relative">
      <video autoplay playsinline class="w-full h-full object-cover"></video>
      
      <div class="absolute top-4 left-4 bg-black/50 backdrop-blur-md p-2 rounded-xl border border-white/10">
        <p class="text-[10px] text-white font-medium uppercase tracking-tighter">Current Attention</p>
        <p class="text-lg font-black text-emerald-400">{{ liveStore.selectedStudent.concentrationScore }}%</p>
      </div>
    </div>
    
    <div class="p-4 bg-slate-50 h-24">
       <p class="text-[10px] font-bold text-slate-400 uppercase mb-2">Focus Trend</p>
       <div class="w-full h-8 bg-slate-200 rounded-lg animate-pulse"></div>
    </div>
  </div>
</template>