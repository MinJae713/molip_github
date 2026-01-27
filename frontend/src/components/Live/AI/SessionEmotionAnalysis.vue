<script setup lang="ts">
// 감정 데이터 인터페이스
interface Emotion {
  label: string;
  emoji: string;
  percentage: number;
  color: string;
  rank: number;
}

const topEmotions: Emotion[] = [
  { rank: 1, label: '놀람 (Surprise)', emoji: '😲', percentage: 45, color: 'bg-indigo-500' },
  { rank: 2, label: '평온 (Calm)', emoji: '😌', percentage: 22, color: 'bg-indigo-300' }
];
</script>

<template>
  <div class="bg-white p-7 rounded-[2.5rem] border border-slate-100 shadow-sm flex flex-col h-full group hover:shadow-md transition-shadow">
    <div class="flex items-center justify-between mb-8 px-1">
      <div class="flex items-center gap-2">
        <div class="size-8 bg-indigo-50 rounded-xl flex items-center justify-center text-indigo-600">
          <span class="material-symbols-outlined text-xl font-light">leaderboard</span>
        </div>
        <p class="text-[11px] font-black text-slate-400 tracking-widest uppercase">실시간 감정 분석</p>
      </div>
      <div class="flex items-center gap-1.5 bg-indigo-50/50 px-3 py-1 rounded-full border border-indigo-100/50">
        <span class="size-1.5 bg-indigo-500 rounded-full animate-pulse"></span>
        <span class="text-[9px] font-black text-indigo-500 uppercase tracking-tighter">Live Analysis</span>
      </div>
    </div>

    <div class="flex gap-4 items-end flex-1">
      <div v-for="emo in topEmotions" :key="emo.rank" class="flex-1 bg-white rounded-[2rem] p-5 shadow-sm border border-slate-100 relative overflow-hidden group/card hover:border-indigo-200 transition-colors">
        <div class="absolute top-3 left-3 px-2 py-0.5 bg-slate-50 rounded-lg group-hover/card:bg-indigo-50 transition-colors">
            <p class="text-[8px] font-black text-slate-400 group-hover/card:text-indigo-400">Top {{ emo.rank }}</p>
        </div>
        
        <div class="flex flex-col items-center pt-4">
          <span class="text-4xl mb-3 drop-shadow-sm">{{ emo.emoji }}</span>
          <p class="text-[11px] font-bold text-slate-500 mb-1">{{ emo.label }}</p>
          <div class="flex items-baseline gap-0.5">
            <span class="text-3xl font-black text-slate-800 tracking-tight">{{ emo.percentage }}</span>
            <span class="text-xs font-bold text-slate-400">%</span>
          </div>
          
          <div class="w-full h-2 bg-slate-100 rounded-full mt-4 overflow-hidden">
            <div 
              class="h-full rounded-full transition-all duration-1000 ease-out" 
              :class="emo.color"
              :style="{ width: `${emo.percentage}%` }"
            ></div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
