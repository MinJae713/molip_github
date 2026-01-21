<script setup lang="ts">
/**
 * StatusBadge 컴포넌트
 * @param {string} variant - 상태 유형 ('ongoing', 'completed', 'upcoming')
 */
interface Props {
  variant: 'ongoing' | 'completed' | 'upcoming';
}

const props = defineProps<Props>();

// 상태별 설정 (색상 클래스 및 고정 텍스트)
const statusConfig = {
  ongoing: {
    bg: 'bg-emerald-50 text-emerald-600 border-emerald-100',
    dot: 'bg-emerald-500',
    text: '진행'
  },
  completed: {
    bg: 'bg-slate-100 text-slate-500 border-slate-200',
    dot: 'bg-slate-400',
    text: '종료'
  },
  upcoming: {
    bg: 'bg-blue-50 text-blue-600 border-blue-100',
    dot: 'bg-blue-500',
    text: '예정'
  },
};
</script>

<template>
  <span
    :class="[
      'inline-flex items-center px-2.5 py-0.5 rounded-full text-[11px] font-bold border whitespace-nowrap',
      statusConfig[variant].bg
    ]"
  >
    <span :class="['w-1.5 h-1.5 rounded-full mr-1.5', statusConfig[variant].dot]"></span>
    {{ statusConfig[variant].text }}
  </span>
</template>