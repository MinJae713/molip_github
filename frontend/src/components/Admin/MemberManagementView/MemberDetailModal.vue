<script setup lang="ts">
import { computed } from 'vue';
import type { WorkspaceMember } from '@/types';
import BaseModal from '@/components/common/CommonComponents/BaseModal.vue';
import StatusBadge from '@/components/common/CommonComponents/StatusBadge.vue';
import ProgressBar from '@/components/common/CommonComponents/ProgressBar.vue';

interface Props {
  isOpen: boolean;
  member: WorkspaceMember | null;
}

const props = defineProps<Props>();
const emit = defineEmits(['close', 'open-role', 'open-status']);

const roleVariant = computed(() => (props.member?.role?.toLowerCase() || 'learner') as any);

const enrolledCourses = computed(() => [
  { id: 1, title: '2024 핵심 리더십 역량 강화', progress: 65, status: 'ongoing' },
  { id: 2, title: 'B2B 영업 제안서 마스터 클래스', progress: 100, status: 'completed' }
]);

const stats = computed(() => ({
  attendance: '84%',
  courseCount: '12개',
  avgQuiz: '92점'
}));
</script>

<template>
  <BaseModal :open="isOpen" @close="emit('close')" maxWidth="max-w-md">
    <template #header>
      <div class="flex items-center gap-2">
        <span class="material-symbols-outlined text-indigo-600 font-light text-xl">account_circle</span>
        <h3 class="text-lg font-bold text-slate-900">참여진 상세 정보</h3>
      </div>
    </template>

    <div class="flex flex-col items-center mb-6">
      <div class="size-20 rounded-full p-1 ring-4 ring-indigo-500/5 mb-3 shadow-sm">
        <div class="size-full rounded-full bg-slate-50 overflow-hidden flex items-center justify-center border border-slate-100">
          <img v-if="member?.profileImage" :src="member.profileImage" class="size-full object-cover" />
          <span v-else class="material-symbols-outlined text-3xl text-slate-300 font-light">person</span>
        </div>
      </div>
      <h3 class="text-xl font-bold text-slate-900 mb-1">{{ member?.name }}</h3>
      <div class="flex flex-col items-center gap-1.5">
        <StatusBadge :variant="roleVariant" class="px-3 py-0.5 text-xs" />
        <span class="text-xs text-slate-400 font-medium">{{ member?.email }}</span>
      </div>
    </div>

    <div class="grid grid-cols-3 gap-0 border-y border-slate-50 mb-6 py-4">
      <div class="text-center border-r border-slate-50">
        <div class="text-[10px] font-bold text-slate-400 mb-0.5">출석률</div>
        <div class="text-base font-bold text-indigo-600">{{ stats.attendance }}</div>
      </div>
      <div class="text-center border-r border-slate-50">
        <div class="text-[10px] font-bold text-slate-400 mb-0.5">수강 강의</div>
        <div class="text-base font-bold text-slate-800">{{ stats.courseCount }}</div>
      </div>
      <div class="text-center">
        <div class="text-[10px] font-bold text-slate-400 mb-0.5">평균 퀴즈</div>
        <div class="text-base font-bold text-slate-800">{{ stats.avgQuiz }}</div>
      </div>
    </div>

    <div class="w-full">
      <div class="flex items-center justify-between mb-3 px-1">
        <h4 class="text-xs font-bold text-slate-900 flex items-center gap-1.5">
          참여 중인 강의
          <span class="text-indigo-500 font-medium">{{ enrolledCourses.length }}</span>
        </h4>
        <button class="text-[11px] font-bold text-indigo-600 hover:text-indigo-700">전체보기</button>
      </div>

      <div class="space-y-2 max-h-60 overflow-y-auto pr-1 scrollbar-thin scrollbar-thumb-slate-200">
        <div v-for="course in enrolledCourses" :key="course.id" class="p-3 border border-slate-50 rounded-xl bg-white">
          <div class="flex items-center justify-between mb-2">
            <span class="text-xs font-bold text-slate-700 truncate max-w-45">{{ course.title }}</span>
            <StatusBadge 
              :variant="course.status === 'ongoing' ? 'ongoing' : 'completed'" 
              :label="course.status === 'ongoing' ? '진행' : '종료'" 
              class="scale-90 origin-right"
              show-dot 
            />
          </div>
          <div class="flex flex-col gap-1">
            <ProgressBar :progress="course.progress" size="xs" />
          </div>
        </div>
      </div>
    </div>

    <template #footer>
      <div class="flex items-center gap-2 w-full pt-2">
        <button 
          @click="emit('open-role', member)" 
          class="flex-1 px-3 py-2.5 border border-slate-200 rounded-lg text-xs font-bold text-slate-600 hover:bg-slate-50 transition-all flex items-center justify-center gap-1.5"
        >
          <span class="material-symbols-outlined text-base">manage_accounts</span>
          권한 변경
        </button>
        <button 
          @click="emit('open-status', member)" 
          class="flex-1 px-3 py-2.5 border border-red-50 rounded-lg text-xs font-bold text-red-500 hover:bg-red-50 transition-all flex items-center justify-center gap-1.5"
        >
          <span class="material-symbols-outlined text-base">person_remove</span>
          강제 탈퇴
        </button>
      </div>
    </template>
  </BaseModal>
</template>

<style scoped>
.scrollbar-thin::-webkit-scrollbar { width: 3px; }
.scrollbar-thumb-slate-200::-webkit-scrollbar-thumb { 
  background-color: #f1f5f9; 
  border-radius: 10px;
}
</style>