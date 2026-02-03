<script setup lang="ts">
import { computed, onMounted } from 'vue';
import { useTrainerLiveStore } from '@/stores/trainerlive';

const trainerLiveStore = useTrainerLiveStore();

/** 1. 실시간 카메라 송출 여부 확인 */
const isPublishing = (studentId: string | number) => {
  return trainerLiveStore.studentStreams.has(String(studentId));
};

/** 2. 집중도 경고 기준 (예: 40% 미만일 때) */
const isLowConcentration = (score: number) => score < 40;

/** 3. 모니터링 시작 핸들러 */
const handleMonitor = (studentId: string | number) => {
  // 카메라를 켠 학생만 모니터링 가능
  if (isPublishing(studentId)) {
    trainerLiveStore.selectStudent(String(studentId));
  } else {
    alert("해당 학생의 카메라가 꺼져 있어 모니터링할 수 없습니다.");
  }
};
</script>

<template>
  <div class="flex flex-col gap-4 p-5 bg-white rounded-[32px] border border-slate-100 shadow-sm h-full overflow-hidden">
    <div class="flex items-center justify-between px-2">
      <div class="flex items-center gap-2">
        <span class="material-symbols-outlined text-indigo-500 font-variation-fill">group</span>
        <h2 class="font-bold text-slate-800 text-base">참여자 목록</h2>
      </div>
      <span class="px-3 py-1 bg-slate-100 rounded-full text-[11px] font-bold text-slate-500">
        {{ trainerLiveStore.participants.length }}명 참여 중
      </span>
    </div>

    <div class="flex-1 overflow-y-auto space-y-2 pr-1 custom-scrollbar">
      <div v-for="student in trainerLiveStore.participants" :key="student.id" @click="handleMonitor(student.id)" :class="[
        'relative flex items-center justify-between p-3.5 rounded-2xl border transition-all cursor-pointer group',
        // 모니터링 중일 때 강조
        trainerLiveStore.selectedStudentId === String(student.id)
          ? 'bg-indigo-50 border-indigo-200 shadow-sm'
          : 'bg-white border-slate-50 hover:border-slate-200 hover:shadow-md',
        // 집중도 저하 시 빨간색 깜빡임 애니메이션 (Tailwind animate-pulse 활용)
        isLowConcentration(student.concentrationScore) ? 'bg-red-50 border-red-200 animate-[pulse_2s_infinite]' : ''
      ]">
        <div class="flex items-center gap-3">
          <div class="relative">
            <div class="size-11 rounded-full overflow-hidden bg-slate-100 border border-slate-200">
              <img v-if="student.avatar" :src="student.avatar" class="w-full h-full object-cover" />
              <span v-else
                class="material-symbols-outlined flex items-center justify-center h-full text-slate-400">person</span>
            </div>
            <div class="absolute -bottom-0.5 -right-0.5 size-3.5 border-2 border-white rounded-full transition-colors"
              :class="isPublishing(student.id) ? 'bg-emerald-500' : 'bg-slate-300'"></div>
          </div>

          <div class="flex flex-col gap-0.5">
            <div class="flex items-center gap-1.5">
              <span class="text-[14px] font-bold text-slate-800">{{ student.name }}</span>
              <span v-if="isLowConcentration(student.concentrationScore)"
                class="text-[9px] font-black text-red-500 bg-red-100 px-1.5 rounded uppercase">Warning</span>
            </div>
            <div class="flex items-center gap-2">
              <div class="w-16 h-1 bg-slate-100 rounded-full overflow-hidden">
                <div class="h-full transition-all duration-500"
                  :class="isLowConcentration(student.concentrationScore) ? 'bg-red-500' : 'bg-indigo-500'"
                  :style="{ width: `${student.concentrationScore}%` }"></div>
              </div>
              <span class="text-[10px] font-bold text-slate-400">{{ student.concentrationScore }}%</span>
            </div>
          </div>
        </div>

        <div class="flex items-center gap-2.5">
          <div class="flex gap-1">
            <span class="material-symbols-outlined text-[18px]"
              :class="student.isMicOn ? 'text-slate-400' : 'text-red-300'">
              {{ student.isMicOn ? 'mic' : 'mic_off' }}
            </span>
            <span class="material-symbols-outlined text-[18px]"
              :class="isPublishing(student.id) ? 'text-indigo-400' : 'text-slate-300'">
              {{ isPublishing(student.id) ? 'videocam' : 'videocam_off' }}
            </span>
          </div>

          <span v-if="trainerLiveStore.selectedStudentId === String(student.id)"
            class="material-symbols-outlined text-indigo-500 animate-bounce">
            visibility
          </span>
        </div>
      </div>
    </div>

    <div
      class="mt-auto pt-4 border-t border-slate-50 flex justify-around text-[10px] font-bold text-slate-400 uppercase tracking-tighter">
      <div class="flex items-center gap-1"><span class="size-2 bg-emerald-500 rounded-full"></span> Live</div>
      <div class="flex items-center gap-1"><span class="size-2 bg-red-500 rounded-full animate-pulse"></span> Low Focus
      </div>
      <div class="flex items-center gap-1"><span class="size-2 bg-slate-300 rounded-full"></span> Offline</div>
    </div>
  </div>
</template>

<style scoped>
/* 커스텀 스크롤바 디자인 */
.custom-scrollbar::-webkit-scrollbar {
  width: 4px;
}

.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}

.custom-scrollbar::-webkit-scrollbar-thumb {
  background: #e2e8f0;
  border-radius: 10px;
}

.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background: #cbd5e1;
}

/* 집중도 저하 시 깜빡임 애니메이션 커스텀 (필요시) */
@keyframes pulse {

  0%,
  100% {
    opacity: 1;
  }

  50% {
    background-color: #fef2f2;
    border-color: #fecaca;
  }
}
</style>