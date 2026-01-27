<script setup lang="ts">
import { ref } from 'vue';

const questions = ref([
  { id: 1, sender: '박지민', text: 'AI 윤리 파트에서 책임성의 주체는 누구인가요?', time: '10:30', answered: true },
  { id: 2, sender: '김민수', text: '실습 데이터셋의 출처가 궁금합니다.', time: '방금 전', answered: false },
]);
</script>

<template>
  <div class="flex-1 overflow-y-auto p-4 space-y-3 custom-scrollbar">
    <div v-for="q in questions" :key="q.id" class="p-4 bg-slate-50 border border-slate-100 rounded-3xl group hover:border-indigo-200 transition-all cursor-pointer">
      <div class="flex items-center justify-between mb-2">
        <div class="flex items-center gap-2">
          <span class="text-xs font-black text-slate-800">{{ q.sender }}</span>
          <span :class="[
            'text-[9px] font-black px-1.5 py-0.5 rounded-md uppercase tracking-tighter',
            q.answered ? 'bg-indigo-100 text-indigo-600' : 'bg-red-100 text-red-500'
          ]">
            {{ q.answered ? 'Answered' : 'New' }}
          </span>
        </div>
        <span class="text-[9px] text-slate-400 font-bold uppercase">{{ q.time }}</span>
      </div>
      <p class="text-sm text-slate-600 leading-snug font-medium line-clamp-2">{{ q.text }}</p>
    </div>
  </div>
</template>

<style scoped>
.custom-scrollbar::-webkit-scrollbar { width: 4px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: #f1f5f9; border-radius: 10px; }
</style>
