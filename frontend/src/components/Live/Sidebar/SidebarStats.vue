<script setup lang="ts">
// 실제 데이터는 Props나 Store에서 가져올 수 있습니다.
interface Props {
  focusCount?: number;
  warningCount?: number;
  absentCount?: number;
}

withDefaults(defineProps<Props>(), {
  focusCount: 38,
  warningCount: 3,
  absentCount: 1
});
</script>

<template>
  <div class="grid grid-cols-3 bg-slate-50/50 rounded-2xl overflow-hidden border border-slate-100">
    <div class="p-4 flex flex-col items-center border-r border-slate-100">
      <span class="text-xs font-bold text-slate-400 mb-1 leading-none uppercase tracking-tighter">집중</span>
      <span class="text-2xl font-black text-indigo-600 tracking-tight tracking-tight">38</span>
    </div>
    <div class="p-4 flex flex-col items-center border-r border-slate-100">
      <span class="text-xs font-bold text-slate-400 mb-1 leading-none uppercase tracking-tighter">주의</span>
      <span class="text-2xl font-black text-red-500 tracking-tight">3</span>
    </div>
    <div class="p-4 flex flex-col items-center">
      <span class="text-xs font-bold text-slate-400 mb-1 leading-none uppercase tracking-tighter">부재</span>
      <span class="text-2xl font-black text-slate-400 tracking-tight">1</span>
    </div>
  </div>
</template>
