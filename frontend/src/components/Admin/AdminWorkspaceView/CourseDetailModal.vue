<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import { useLearnerCourseStore } from '@/stores/learnerCourse';
import BaseModal from '@/components/common/CommonComponents/BaseModal.vue';
import StatusBadge from '@/components/common/CommonComponents/StatusBadge.vue';

const store = useLearnerCourseStore();

// 모달 오픈 상태 제어
const isOpen = computed({
  get: () => !!store.courseInfo,
  set: (val) => { if (!val) store.courseInfo = null; }
});

const course = computed(() => store.courseInfo);
const sessions = ref<any[]>([]);

// 코스가 선택되면 세션 목록 로드
watch(() => course.value?.courseId, async (newId) => {
  if (newId) {
    sessions.value = await store.fetchCourseSessions(newId);
  } else {
    sessions.value = [];
  }
});

// 상태 매핑 유틸
const mapStatus = (status?: string) => {
  if (status === 'PROCEED') return 'ongoing';
  if (status === 'FINISH') return 'completed';
  return 'upcoming';
};

// 세션 상태별 스타일 설정 (API 데이터에 맞게 조정)
const getSessionStyles = (status: string) => {
  const configs = {
    'ended': { text: 'text-slate-400', bar: 'bg-indigo-400', isLive: false }, // 완료
    'live': { text: 'text-emerald-500 font-bold', bar: 'bg-emerald-500', isLive: true }, // 라이브
    'upcoming': { text: 'text-slate-300', bar: 'bg-slate-100', isLive: false } // 대기
  };
  return configs[status as keyof typeof configs] || configs['upcoming'];
};
</script>

<template>
  <BaseModal v-model:open="isOpen" maxWidth="max-w-4xl">
    <template #header>
      <div v-if="course" class="flex flex-col gap-2">
        <div class="flex items-center gap-2">
          <span class="px-2 py-0.5 bg-indigo-50 text-indigo-600 text-[11px] font-bold rounded uppercase">
            {{ course.category }}
          </span>
          <StatusBadge :variant="(mapStatus(course.status) as any)" />
        </div>
        <h2 class="text-3xl font-black text-slate-800 tracking-tight">
          {{ course.title }}
        </h2>
      </div>
    </template>

    <div v-if="store.isLoading" class="py-20 flex flex-col items-center justify-center">
      <div class="w-10 h-10 border-4 border-indigo-500 border-t-transparent rounded-full animate-spin mb-4"></div>
      <p class="text-slate-400 font-medium">강의 정보를 불러오는 중입니다...</p>
    </div>

    <div v-else-if="course" class="space-y-10">

      <section class="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div class="bg-slate-50 rounded-2xl p-6 border border-slate-100 shadow-sm">
          <p class="text-[11px] font-bold text-slate-400 mb-3 uppercase tracking-wider">전체 수강생</p>
          <p class="text-4xl font-black text-slate-800">
            {{ course.maxLearner ?? 0 }}<span class="text-xl ml-1 font-bold text-slate-300">명</span>
          </p>
        </div>

        <div class="bg-slate-50 rounded-2xl p-6 border border-slate-100 shadow-sm">
          <p class="text-[11px] font-bold text-slate-400 mb-3 uppercase tracking-wider">총 수업 수</p>
          <div class="flex items-center gap-4">
            <p class="text-4xl font-black text-slate-800">
              {{ course.totalLessons }}<span class="text-xl font-bold text-slate-300">강</span>
            </p>
          </div>
        </div>

        <div class="bg-slate-50 rounded-2xl p-6 border border-slate-100 shadow-sm">
          <p class="text-[11px] font-bold text-slate-400 mb-3 uppercase tracking-wider">기간</p>
          <p class="text-3xl font-black text-slate-800 tracking-tight">
            {{ course.period ? `${course.period}일` : '-' }}
          </p>
        </div>
      </section>

      <section>
        <div class="flex items-center justify-between mb-4">
          <h4 class="font-bold text-slate-800 flex items-center gap-2 text-lg">
            <span class="w-8 h-8 flex items-center justify-center bg-indigo-50 text-indigo-600 rounded-lg">📋</span>
            커리큘럼 및 실시간 참여 현황
          </h4>
          <span class="text-[10px] font-bold text-slate-300 tracking-widest uppercase">Live Monitoring via
            Web-RTC</span>
        </div>

        <div class="border border-slate-100 rounded-2xl overflow-hidden shadow-sm bg-white">
          <table class="w-full text-sm">
            <thead>
              <tr class="text-[11px] font-bold text-slate-400 border-b border-slate-50 uppercase bg-slate-50/30">
                <th class="px-6 py-4 text-left w-16 whitespace-nowrap">순서</th>
                <th class="px-4 py-4 text-left whitespace-nowrap">강의/모듈명</th>
                <th class="px-6 py-4 text-right w-24 whitespace-nowrap">상태</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-50">
              <tr v-for="(session, idx) in sessions" :key="session.sessionId"
                class="group hover:bg-slate-50/30 transition-colors">
                <td class="px-6 py-5 text-slate-400 font-medium tracking-tighter">
                  {{ String(idx + 1).padStart(2, '0') }}
                </td>
                <td class="px-4 py-5 font-bold text-slate-700">
                  {{ session.title }}
                  <span v-if="session.state === 'live'"
                    class="ml-2 px-1.5 py-0.5 bg-indigo-600 text-white text-[9px] rounded-sm font-black uppercase tracking-tighter">Now</span>
                </td>
                <td class="px-6 py-5 text-right font-bold text-[12px]" :class="getSessionStyles(session.state).text">
                  {{ session.state }}
                </td>
              </tr>
              <tr v-if="!sessions || sessions.length === 0">
                <td colspan="3" class="py-16 text-center text-slate-400 italic">등록된 강의 일정이 없습니다.</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      <section>
        <div v-if="course.ownerName"
          class="bg-slate-50 rounded-2xl p-6 border border-slate-100 shadow-sm flex items-center">
          <div class="flex items-center gap-5">
            <div class="w-16 h-16 rounded-full bg-white border-2 border-white shadow-md overflow-hidden shrink-0 flex items-center justify-center text-slate-300">
                <span class="text-3xl">👤</span>
            </div>
            <div>
              <p class="text-[10px] font-black text-indigo-600 uppercase tracking-widest mb-1">Assigned Trainer</p>
              <h5 class="font-bold text-slate-800 text-xl">
                {{ course.ownerName }}
              </h5>
            </div>
          </div>
        </div>
      </section>
    </div>

    <template #footer>
      <div class="flex justify-end pt-2">
        <button @click="isOpen = false"
          class="px-10 py-3 bg-white border border-slate-200 text-slate-700 rounded-xl font-bold hover:bg-slate-50 hover:border-slate-300 transition-all shadow-sm cursor-pointer">
          닫기
        </button>
      </div>
    </template>
  </BaseModal>
</template>