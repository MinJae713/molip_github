<script setup lang="ts">
interface Props {
  progress: number;
}

const props = withDefaults(defineProps<Props>(), {
  progress: 0
});
</script>

<template>
  <div class="flex items-center gap-3 w-full">
    <div class="flex-1 h-2 bg-slate-100 rounded-full overflow-hidden">
      <div 
        class="h-2 rounded-full transition-all duration-700 ease-out"
        :class="[
          progress === 100 ? 'bg-indigo-600' : 'bg-blue-500'
        ]"
        :style="{ width: `${props.progress}%` }"
      ></div>
    </div>
    
    <span class="text-[12px] font-bold text-slate-600 w-9 text-right tabular-nums">
      {{ progress }}%
    </span>
  </div>
</template>

<style scoped>
.tabular-nums {
  font-variant-numeric: tabular-nums;
}
</style>