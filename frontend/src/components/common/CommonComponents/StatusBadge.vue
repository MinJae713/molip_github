<script setup lang="ts">
/**
 * 확장된 StatusBadge 컴포넌트
 */
interface Props {
  // 기존 variant + 신규 상태 추가
  variant: 'ongoing' | 'completed' | 'upcoming' | 'active' | 'inactive' | 'admin' | 'instructor' | 'learner';
  showDot?: boolean; // 점(dot) 표시 여부 선택 옵션
}

const props = withDefaults(defineProps<Props>(), {
  showDot: false // 기본적으로는 점을 표시하지 않음 (기존 강의 상태에서만 명시적으로 true 권장)
});

const statusConfig = {
  // 1. 강의 상태 (기존 유지)
  ongoing: { bg: 'bg-emerald-50 text-emerald-600 border-emerald-100', dot: 'bg-emerald-500', text: '진행' },
  completed: { bg: 'bg-slate-100 text-slate-500 border-slate-200', dot: 'bg-slate-400', text: '종료' },
  upcoming: { bg: 'bg-blue-50 text-blue-600 border-blue-100', dot: 'bg-blue-500', text: '예정' },

  // 2. 사용자 계정 상태
  active: { bg: 'bg-emerald-50 text-emerald-600 border-emerald-100', dot: 'bg-emerald-500', text: 'ACTIVE' },
  inactive: { bg: 'bg-slate-100 text-slate-400 border-slate-200', dot: 'bg-slate-400', text: 'INACTIVE' },

  // 3. 사용자 역할 (Role)
  admin: { bg: 'bg-indigo-50 text-indigo-600 border-indigo-100', dot: 'bg-indigo-500', text: 'Admin' },
  instructor: { bg: 'bg-cyan-50 text-cyan-600 border-cyan-100', dot: 'bg-cyan-500', text: 'Instructor' },
  learner: { bg: 'bg-slate-50 text-slate-600 border-slate-100', dot: 'bg-slate-400', text: 'Learner' },
};
</script>

<template>
  <span
    :class="[
      'inline-flex items-center px-2 py-0.5 rounded-full text-[11px] font-bold border whitespace-nowrap',
      statusConfig[variant].bg
    ]"
  >
    <span v-if="showDot" :class="['w-1.5 h-1.5 rounded-full mr-1.5', statusConfig[variant].dot]"></span>
    {{ statusConfig[variant].text }}
  </span>
</template>