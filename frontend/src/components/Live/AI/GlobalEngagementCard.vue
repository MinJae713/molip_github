<script setup lang="ts">
// 실시간 데이터를 Props로 받을 수 있도록 구성 가능
interface Props {
  score?: number;
  status?: string;
}

const props = withDefaults(defineProps<Props>(), {
  score: 78,
  status: '안정'
});
</script>

<template>
  <div class="bg-white p-7 rounded-[2.5rem] border border-slate-100 shadow-sm flex flex-col h-full group hover:shadow-md transition-shadow">
    <div class="flex items-center justify-between mb-6">
      <div class="flex items-center gap-2">
        <div class="size-8 bg-blue-50 rounded-xl flex items-center justify-center text-blue-600">
          <span class="material-symbols-outlined text-xl font-light">monitoring</span>
        </div>
        <p class="text-[11px] font-black text-slate-400 tracking-widest uppercase">전체 학습 몰입도</p>
      </div>
      <div class="bg-slate-50 px-3 py-1 rounded-full border border-slate-100">
        <p class="text-[10px] font-bold text-slate-500">
          실시간: <span class="text-blue-600">{{ props.score }}% ({{ props.status }})</span>
        </p>
      </div>
    </div>

    <!-- 임시 그래프 (SVG) -->
    <div class="flex-1 w-full relative min-h-32 mt-2">
      <svg class="w-full h-full" viewBox="0 0 200 60" preserveAspectRatio="none">
        <!-- 배경 가이드 라인 -->
        <line x1="0" y1="10" x2="200" y2="10" stroke="#f1f5f9" stroke-width="0.5" stroke-dasharray="2,2" />
        <line x1="0" y1="30" x2="200" y2="30" stroke="#f1f5f9" stroke-width="0.5" stroke-dasharray="2,2" />
        <line x1="0" y1="50" x2="200" y2="50" stroke="#f1f5f9" stroke-width="0.5" stroke-dasharray="2,2" />
        
        <!-- 데이터 라인 -->
        <path 
          d="M0,45 C20,40 40,25 70,28 C100,31 130,15 160,18 C180,20 200,25 200,25" 
          fill="none" 
          stroke="url(#blueGradient)" 
          stroke-width="3" 
          stroke-linecap="round"
        />
        
        <!-- 그라데이션 정의 -->
        <defs>
          <linearGradient id="blueGradient" x1="0%" y1="0%" x2="100%" y2="0%">
            <stop offset="0%" stop-color="#3b82f6" />
            <stop offset="100%" stop-color="#60a5fa" />
          </linearGradient>
        </defs>
      </svg>
      
      <!-- 포인트 데코레이션 -->
      <div class="absolute right-[10%] top-[30%] size-3 bg-blue-500 border-2 border-white rounded-full shadow-lg shadow-blue-200"></div>
    </div>
  </div>
</template>

<style scoped>
/* 추가적인 미세 애니메이션이 필요하다면 여기에 작성 */
</style>
