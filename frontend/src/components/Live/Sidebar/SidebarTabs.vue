<script setup lang="ts">
type Tab = 'status' | 'chat' | 'qna';

defineProps<{ modelValue: Tab }>();
const emit = defineEmits(['update:modelValue']);
</script>

<template>
  <div class="flex border-b border-slate-100">
    <button v-for="tab in (['status', 'chat', 'qna'] as Tab[])" :key="tab" @click="emit('update:modelValue', tab)"
      :class="[
        'flex-1 py-4 text-sm font-bold transition-all relative',
        modelValue === tab ? 'text-indigo-600' : 'text-slate-400 hover:text-slate-600'
      ]">
      {{ tab === 'status' ? '참여자 목록' : tab === 'chat' ? '채팅' : tab === 'qna' ? '질문하기' : '' }}
      <!-- 하단 인디케이터 -->
      <div v-if="modelValue === tab" class="absolute bottom-0 left-0 right-0 h-0.5 bg-indigo-600 rounded-full"></div>
    </button>
  </div>
</template>
