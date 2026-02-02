<script setup lang="ts">
/**
 * 초대코드 배너 컴포넌트 Props 정의
 */
interface Props {
  title?: string;
  description?: string;
}

withDefaults(defineProps<Props>(), {
  title: '워크스페이스 초대코드 발급',
  description: '새로운 팀원을 초대하기 위한 고유 코드를 즉시 생성하고 배포하세요.'
});

const emit = defineEmits<{
  (e: 'generate'): void;
}>();
</script>

<template>
  <div class="bg-white border border-slate-200 rounded-2xl p-6 shadow-sm flex items-center justify-between">
    <div class="flex items-center gap-5">
      <div class="size-12 rounded-xl bg-slate-50 flex items-center justify-center border border-slate-100">
        <span class="material-symbols-outlined text-slate-500 text-2xl">person_add</span>
      </div>
      
      <div>
        <h3 class="text-lg font-bold text-slate-900 font-display tracking-tight">{{ title }}</h3>
        <p class="text-sm text-slate-500 mt-1">{{ description }}</p>
      </div>
    </div>

    <button 
      @click="emit('generate')"
      class="flex items-center gap-2 px-5 py-2.5 bg-indigo-600 text-white rounded-xl font-bold shadow-lg shadow-indigo-200 hover:bg-indigo-700 transition-all active:scale-95"
    >
      <span class="material-symbols-outlined text-xl">add_circle</span>
      <span class="text-sm">신규 코드 발급하기</span>
    </button>
  </div>
</template>

<style scoped>
.font-display {
  font-family: 'Lexend', 'Noto Sans KR', sans-serif;
}
</style>