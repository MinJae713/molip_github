<script setup lang="ts">
import { useTrainerLiveStore } from '@/stores/trainerlive';

const trainerLiveStore = useTrainerLiveStore();

/** 1. 실시간 카메라 송출 여부 확인 */
const isPublishing = (studentId: string | number) => {
  return trainerLiveStore.studentStreams.has(String(studentId));
};

/** 2. 상태별 스타일 정보 */
const getStatusInfo = (status: string) => {
  switch (status) {
    case 'FEVER':
      return { label: '집중중', color: 'bg-emerald-500', textColor: 'text-emerald-600', bgClass: 'bg-emerald-50 border-emerald-200' };
    case 'EMPTY':
      return { label: '자리비움', color: 'bg-amber-500', textColor: 'text-amber-600', bgClass: 'bg-amber-50 border-amber-200' };
    case 'NORMAL':
    default:
      return { label: '보통', color: 'bg-slate-400', textColor: 'text-slate-500', bgClass: '' };
  }
};

/** 3. 모니터링 시작 핸들러 */
const handleMonitor = (studentId: string | number) => {
  const studentIdStr = String(studentId);
  const participant = trainerLiveStore.participants.find(p => p.id === studentIdStr);

  if (!participant) {
    console.warn('[ParticipantItem] ⚠️ Participant not found:', studentIdStr);
    return;
  }

  console.log('[ParticipantItem] 🖱️ handleMonitor called:', {
    studentId: studentIdStr,
    isVideoStreamReady: participant.isVideoStreamReady,
    isCameraOn: participant.isCameraOn,
    studentStreamsKeys: Array.from(trainerLiveStore.studentStreams.keys()),
  });

  // 1. 카메라가 꺼져 있는 경우
  if (!participant.isCameraOn) {
    console.warn('[ParticipantItem] ❌ Camera is off for:', studentIdStr);
    alert("해당 학생이 카메라를 켜지 않았습니다.");
    return;
  }

  // 2. 스트림이 아직 준비되지 않은 경우
  if (!participant.isVideoStreamReady) {
    console.warn('[ParticipantItem] ⏳ Stream not ready yet for:', studentIdStr);
    alert("학생의 영상 스트림을 연결하는 중입니다. 잠시 후 다시 시도해주세요.");
    return;
  }

  // 3. 모든 조건 충족 - 모니터링 시작
  console.log('[ParticipantItem] ✅ Starting monitor for:', studentIdStr);
  trainerLiveStore.selectStudent(studentIdStr);
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
        // 상태별 배경색 (FEVER: 초록, EMPTY: 노랑)
        getStatusInfo(student.status).bgClass
      ]">
        <div class="flex items-center gap-3">

          <div class="relative">
            <div class="size-11 rounded-full overflow-hidden bg-slate-100 border border-slate-200">
              <img v-if="student.avatar" :src="student.avatar" class="w-full h-full object-cover" />
              <span v-else
                class="material-symbols-outlined flex items-center justify-center h-full text-slate-400">person</span>
            </div>
            <div class="absolute -bottom-0.5 -right-0.5 size-3.5 border-2 border-white rounded-full transition-colors"
              :class="[getStatusInfo(student.status).color, 'text-white']"></div>
          </div>

          <div class="flex flex-col gap-0.5">
            <div class="flex items-center gap-1.5">
              <span class="text-[14px] font-bold text-slate-800">{{ student.name }}</span>
              <!-- 상태 뱃지 -->
              <span class="text-[9px] font-black px-1.5 py-0.5 rounded uppercase"
                :class="[getStatusInfo(student.status).color, 'text-white']">
                {{ getStatusInfo(student.status).label }}
              </span>
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
              :class="student.isVideoStreamReady ? 'text-indigo-400' : 'text-slate-300'">
              {{ student.isVideoStreamReady ? 'videocam' : 'videocam_off' }}
            </span>
          </div>

          <!-- 스트림 로딩 중 표시 -->
          <span v-if="student.isCameraOn && !student.isVideoStreamReady"
            class="material-symbols-outlined text-amber-500 text-[18px] animate-spin">
            progress_activity
          </span>

          <span v-if="trainerLiveStore.selectedStudentId === String(student.id)"
            class="material-symbols-outlined text-indigo-500 animate-bounce">
            visibility
          </span>
        </div>
      </div>
    </div>

    <!-- 범례 -->
    <div
      class="mt-auto pt-4 border-t border-slate-50 flex justify-around text-[10px] font-bold text-slate-400 uppercase tracking-tighter">
      <div class="flex items-center gap-1"><span class="size-2 bg-emerald-500 rounded-full"></span> 집중중</div>
      <div class="flex items-center gap-1"><span class="size-2 bg-slate-400 rounded-full"></span> 보통</div>
      <div class="flex items-center gap-1"><span class="size-2 bg-amber-500 rounded-full"></span> 자리비움</div>
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
</style>