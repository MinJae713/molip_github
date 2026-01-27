<script setup lang="ts">
// Props나 Emit이 필요한 경우 여기에 정의 (예: 퀴즈 실행 버튼 클릭 등)
const emit = defineEmits(['run-quiz', 'dismiss']);
</script>

<template>
  <div class="bg-white p-7 rounded-[2.5rem] border border-slate-100 shadow-sm flex flex-col justify-between h-full group hover:shadow-md transition-shadow">
    <div class="space-y-4">
      <div class="flex items-center gap-2">
        <div class="size-8 bg-indigo-50 rounded-xl flex items-center justify-center text-indigo-600">
          <span class="material-symbols-outlined text-xl font-light">psychology</span>
        </div>
        <p class="text-[11px] font-black text-slate-400 tracking-widest uppercase">AI 행동 추천</p>
      </div>
      
      <div>
        <h3 class="text-xl font-bold text-slate-800 mb-2 leading-snug">
          현재 참여자들의 <span class="text-indigo-600 underline underline-offset-4 decoration-indigo-200">집중도가 22% 감소</span>했습니다.
        </h3>
        <p class="text-slate-500 text-sm leading-relaxed">
          분위기 전환을 위해 <span class="font-bold text-slate-700">돌발 퀴즈</span>를 추천합니다.
        </p>
      </div>
    </div>

    <div class="flex gap-3 mt-6">
      <button 
        @click="emit('run-quiz')"
        class="flex-1 bg-indigo-600 text-white py-4 rounded-2xl font-bold hover:bg-indigo-700 transition-all active:scale-95 shadow-lg shadow-indigo-100"
      >
        퀵 퀴즈 실행
      </button>
      <button 
        @click="emit('dismiss')"
        class="px-6 py-4 bg-slate-50 text-slate-500 rounded-2xl font-bold hover:bg-slate-100 transition-all border border-slate-200/50 active:scale-95"
      >
        나중에
      </button>
    </div>
  </div>
</template>
