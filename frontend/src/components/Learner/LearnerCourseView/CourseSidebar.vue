<template>
    <div class="space-y-6">
        <!-- Live Session Widget -->
        <div class="bg-indigo-50 border border-indigo-100 rounded-xl p-5 shadow-sm relative overflow-hidden group">
            <div class="absolute top-0 right-0 p-4 opacity-10 group-hover:opacity-20 transition-opacity">
                <span class="material-symbols-outlined text-6xl text-indigo-600">podium</span>
            </div>
            
            <h3 class="font-bold text-indigo-900 mb-4 flex items-center gap-2 relative z-10">
                <span class="relative flex h-3 w-3">
                  <span v-if="liveSession" class="animate-ping absolute inline-flex h-full w-full rounded-full bg-red-400 opacity-75"></span>
                  <span class="relative inline-flex rounded-full h-3 w-3" :class="liveSession ? 'bg-red-500' : 'bg-slate-300'"></span>
                </span>
                오늘의 라이브
            </h3>

            <div v-if="liveSession" class="relative z-10">
                <div class="flex items-center gap-2 mb-2">
                    <span class="text-xs font-bold text-indigo-600 bg-indigo-100 px-2 py-0.5 rounded-full border border-indigo-100">
                        SESSION {{ String(liveSessionIndex + 1).padStart(2, '0') }}
                    </span>
                    <span v-if="liveSession.date" class="text-xs text-slate-500 font-medium">
                        {{ liveSession.date }}
                    </span>
                </div>
                
                <h4 class="font-bold text-slate-900 mb-1 line-clamp-1">{{ liveSession.title }}</h4>
                <p v-if="liveSession.description" class="text-sm text-slate-500 mb-3 line-clamp-2">{{ liveSession.description }}</p>

                <div class="flex items-center gap-2 text-sm text-indigo-700 font-medium mb-4">
                     <span class="material-symbols-outlined text-[18px]">schedule</span>
                     <span v-if="liveSession.startAt && liveSession.endAt">
                        {{ liveSession.startAt.substring(0, 5) }} ~ {{ liveSession.endAt.substring(0, 5) }}
                     </span>
                     <span v-else>시간 미정</span>
                </div>
                <button @click="enterSession(liveSession.sessionId)"
                    class="w-full py-2.5 bg-indigo-600 hover:bg-indigo-700 text-white text-sm font-bold rounded-lg shadow-md transition-all flex items-center justify-center gap-2">
                    <span class="material-symbols-outlined text-[18px]">login</span> 지금 입장하기
                </button>
            </div>
            
            <div v-else class="relative z-10 text-center py-2">
                <p class="text-sm text-slate-500 font-medium">현재 예정된 라이브가 없습니다.</p>
            </div>
        </div>


         <!-- Course Info Summary -->
        <div class="bg-white border border-slate-200 rounded-xl p-5 shadow-sm">
            <h3 class="font-bold text-slate-900 mb-4 flex items-center gap-2">
                <span class="material-symbols-outlined text-indigo-600">info</span>
                강의 정보
            </h3>
            <ul class="space-y-3">
                 <li v-if="courseInfo?.period" class="flex justify-between items-center text-sm text-slate-600">
                    <span class="flex items-center gap-2">
                         <span class="material-symbols-outlined text-slate-400 text-[18px]">calendar_month</span>
                         기간
                    </span>
                    <span class="font-bold text-slate-900">{{ courseInfo.period }}주</span>
                </li>
                 <li v-if="courseInfo?.maxLearner" class="flex justify-between items-center text-sm text-slate-600">
                    <span class="flex items-center gap-2">
                         <span class="material-symbols-outlined text-slate-400 text-[18px]">group</span>
                         수강 정원
                    </span>
                    <span class="font-bold text-slate-900">{{ courseInfo.maxLearner }}명</span>
                </li>
            </ul>
        </div>

        <!-- Completion Criteria -->
        <div class="bg-white border border-slate-200 rounded-xl p-5 shadow-sm">
            <h3 class="font-bold text-slate-900 mb-4 flex items-center gap-2">
                <span class="material-symbols-outlined text-indigo-600">verified</span>
                수료 기준
            </h3>
            <ul class="space-y-3">
                <li v-if="courseInfo?.attendance" class="flex items-center gap-2 text-sm text-slate-600">
                    <span class="material-symbols-outlined text-emerald-500 text-[18px]">check_circle</span>
                    <span>필수 출석률 <span class="font-bold text-indigo-600">{{ courseInfo.attendance }}%</span> 이상</span>
                </li>
                <li v-if="courseInfo?.engagement" class="flex items-center gap-2 text-sm text-slate-600">
                     <span class="material-symbols-outlined text-emerald-500 text-[18px]">check_circle</span>
                    <span>평균 집중도 <span class="font-bold text-indigo-600">{{ courseInfo.engagement }}점</span> 이상</span>
                </li>
                <li v-if="courseInfo?.exitQuiz" class="flex items-center gap-2 text-sm text-slate-600">
                    <span class="material-symbols-outlined text-emerald-500 text-[18px]">check_circle</span>
                    <span>퇴실 퀴즈 평균 점수 <span class="font-bold text-indigo-600">{{ courseInfo.exitQuiz }}점</span> 이상</span>
                </li>
                 <li v-if="!courseInfo?.attendance && !courseInfo?.exitQuiz && !courseInfo?.engagement" class="text-sm text-slate-400 text-center py-2">
                    별도의 수료 기준이 없습니다.
                </li>
            </ul>
        </div>
    </div>
</template>
<script setup lang="ts">
import { useLearnerCourseStore } from '@/stores/learnerCourse';
import { useLearnerSessionStore } from '@/stores/learnerSession';
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import api from '@/api';

const router = useRouter();
const courseStore = useLearnerCourseStore();
const sessionStore = useLearnerSessionStore();

const courseInfo = computed(() => courseStore.courseInfo);
const liveSession = computed(() => {
    return sessionStore.sessionsList.find(session => session.state === 'live');
});
const liveSessionIndex = computed(() => {
    return sessionStore.sessionsList.findIndex(session => session.state === 'live');
});

// 라이브 세션 입장 (API 체크 포함)
const enterSession = async (sessionId: number | string) => {
    try {
        const response = await api.get(`/session/${sessionId}/connection`);
        if (response.data && response.data.janusRoomId) {
            const route = router.resolve({
                name: 'learnerLiveSession',
                params: { sessionId: sessionId }
            });
            window.open(route.href, '_blank', 'noopener,noreferrer');
        } else {
            alert("현재 라이브 강의가 진행 중이지 않습니다.");
        }
    } catch (error) {
        console.error("세션 연결 정보 조회 실패:", error);
        alert("강의에 입장할 수 없습니다. (연결 정보 조회 실패)");
    }
}
</script>
