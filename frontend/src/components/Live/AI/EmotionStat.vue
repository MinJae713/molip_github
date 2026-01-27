<script setup lang="ts">
interface Props {
  rank: number;
  label: string;
  emoji: string;
  percentage: number;
  color: string;
}
defineProps<Props>();
</script>

<template>
  <div class="bg-white p-4 rounded-2xl border border-slate-100 flex-1">
    <span class="text-[10px] font-bold text-indigo-500 uppercase">Top {{ rank }}</span>
    <div class="flex flex-col items-center my-2">
      <span class="text-4xl mb-1">{{ emoji }}</span>
      <span class="font-bold text-slate-800">{{ label }}</span>
    </div>
    <div class="text-center">
      <span class="text-2xl font-black text-indigo-600">{{ percentage }}%</span>
      <div class="w-full h-1.5 bg-slate-100 rounded-full mt-2 overflow-hidden">
        <div 
          class="h-full transition-all duration-500" 
          :style="{ width: `${percentage}%`, backgroundColor: color }"
        ></div>
      </div>
    </div>
  </div>
</template>