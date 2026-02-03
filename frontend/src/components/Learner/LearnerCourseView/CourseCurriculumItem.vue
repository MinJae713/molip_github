<template>
    <div class="group bg-white border rounded-xl p-5 transition-all flex flex-col sm:flex-row gap-4 relative overflow-hidden"
        :class="[
            session.state === 'live' ? 'border-2 border-indigo-500/20 shadow-soft ring-1 ring-indigo-500/10' : 'border-slate-200 hover:border-indigo-300'
        ]">
        <!-- 라이브 중인 경우 우측 상단 라이브 빨간 깜빡이 표시 -->
        <div v-if="session.state === 'live'" class="absolute top-0 right-0 p-3">
            <span class="relative flex h-3 w-3">
                <span class="animate-ping absolute inline-flex h-full w-full rounded-full bg-red-400 opacity-75"></span>
                <span class="relative inline-flex rounded-full h-3 w-3 bg-red-500"></span>
            </span>
        </div>

        <!-- 좌측 세션 번호 표시 -->
        <div class="flex-none flex flex-col items-center justify-center w-12 pt-1 sm:pt-0">
            <!-- 라이브면 번호가 아니라 빨간 라이브 표기-->
            <div v-if="session.state === 'live'"
                class="size-8 rounded-full bg-red-500 text-white flex items-center justify-center font-bold text-[10px] shadow-md animate-pulse">
                LIVE
            </div>
            <!-- 완료된 과목이면 체크 표시-->
            <div v-else-if="session.state === 'ended'"
                class="size-8 rounded-full bg-indigo-100 text-indigo-600 flex items-center justify-center font-bold text-sm">
                <span class="material-symbols-outlined text-[18px]">check</span>
            </div>
            <!-- 예정된 과목이면 숫자 표시-->
            <div v-else-if="session.state === 'upcoming'"
                class="size-8 rounded-full bg-slate-100 text-slate-500 flex items-center justify-center font-bold text-sm border border-slate-200">
                {{ String(index + 1).padStart(2, '0') }}
            </div>
            <!-- 세션 번호 아래 세로선 -->
            <div class="h-full w-0.5 bg-slate-100 my-2 hidden sm:block group-last:hidden"></div>
        </div>

        <div class="flex-1">
            <div class="flex items-center gap-2 mb-1">
                <span class="text-xs font-bold uppercase tracking-wider"
                    :class="session.alive ? 'text-red-500' : 'text-slate-400'">
                    SESSION {{ String(index + 1).padStart(2, '0') }}
                </span>
            </div>
            <h3 class="font-bold text-lg" :class="isCompleted ? 'text-slate-900' : 'text-slate-800'">
                {{ session.title }}
            </h3>
            <p v-if="session.description" class="text-sm text-slate-600 mt-1 mb-2">{{ session.description }}</p>

            <div class="flex flex-wrap items-center gap-x-4 gap-y-2 mt-2 text-sm text-slate-500">
                <div class="flex items-center gap-1"
                    :class="{ 'text-slate-800 font-medium': session.state === 'live' }">
                    <span class="material-symbols-outlined text-[16px]"
                        :class="{ 'text-red-500': session.state === 'live' }">
                        {{ session.state === 'live' ? 'videocam' : 'calendar_today' }}
                    </span>
                    {{ session.date }} {{ session.state === 'live' ? '(진행 중)' : '' }}
                </div>
                <div v-if="session.duration" class="flex items-center gap-1">
                    <span class="material-symbols-outlined text-[16px]">schedule</span> {{ session.duration }}
                </div>

                <div v-if="session.is_top3"
                    class="flex items-center gap-1 text-amber-700 font-bold bg-amber-50 px-2 py-0.5 rounded text-xs border border-amber-200">
                    <span class="material-symbols-outlined text-[15px] fill-current text-amber-500">emoji_events</span>
                    집중도 Top 3
                </div>
                <div v-if="session.quiz_score"
                    class="flex items-center gap-1 text-indigo-600 font-medium bg-indigo-50 px-2 py-0.5 rounded text-xs">
                    <span class="material-symbols-outlined text-[14px]">assignment_turned_in</span> 퀴즈 완료 ({{
                        session.quiz_score }}점)
                </div>
            </div>
        </div>

        <!-- Action Buttons Group -->
        <div class="flex flex-col gap-2 min-w-[140px] sm:self-center items-stretch">
            <button v-if="session.janusRoomId !== null" @click="enterSession"
                class="w-full px-6 py-2.5 bg-indigo-600 hover:bg-indigo-700 text-white text-sm font-bold rounded-lg shadow-md transition-all flex items-center justify-center gap-2 group">
                <span class="material-symbols-outlined text-[18px]">login</span> 입장하기
                <span
                    class="material-symbols-outlined text-[16px] group-hover:translate-x-1 transition-transform">arrow_forward</span>
            </button>
            <button v-else-if="session.state === 'ended'" @click="enterReview"
                class="w-full px-4 py-2 bg-white border border-slate-300 hover:bg-slate-50 text-slate-700 text-sm font-bold rounded-lg flex items-center justify-center gap-2">
                <span class="material-symbols-outlined text-[18px]">history</span>
                복습하기
            </button>
            <button v-else-if="session.state === 'upcoming'"
                class="w-full px-4 py-2 bg-indigo-600 hover:bg-indigo-700 text-white text-sm font-bold rounded-lg flex items-center justify-center gap-2">
                <span class="material-symbols-outlined text-[18px]">calendar_today</span>강의예정
            </button>

            <!-- 학습자료 확인하기 버튼 -->
            <button @click="openModal"
                class="w-full px-4 py-2 bg-indigo-50 hover:bg-indigo-100 text-indigo-700 text-sm font-bold rounded-lg flex items-center justify-center gap-2 transition-colors">
                <span class="material-symbols-outlined text-[18px]">description</span>학습자료 확인하기
            </button>
            <MaterialDownloadModal v-model="isModalOpen" :courseId="session.course_id"
                :sessionId="session.session_id" />
        </div>

    </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import api from '@/api';
// 강의자료 다운로드 모달
import { ref } from 'vue';
import MaterialDownloadModal from '@/components/Learner/LearnerCourseView/MaterialDownloadModal.vue';
import type { LearnerCourse, Session } from '@/types';

const props = defineProps<{
    session: any;
    index: number;
}>();
// 라이브 세션 활성화 여부(젠킨스 서버값이 비어있다면 라이브가 아님.)
const isCompleted = computed(() => props.session.status === 'completed');
const typeBadgeClass = computed(() => {
    if (props.session.status === 'live') return 'bg-red-50 text-red-600 border-red-100';
    if (props.session.type === 'live') return 'bg-indigo-50 text-indigo-600 border-indigo-100';
    return 'bg-blue-50 text-blue-600 border-brlue-100';
});

// 모달 상태
const isModalOpen = ref(false);

const openModal = () => {
    isModalOpen.value = true;
};

const emit = defineEmits<{
    (e: 'close'): void;
    (e: 'update:modelValue', value: boolean): void;
}>();

import { useRouter } from 'vue-router';
const router = useRouter();

// 라이브 세션 입장
const enterSession = async () => {
    try {
        const sessionId = props.session.sessionId;
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

const enterReview = () => {
    const route = router.resolve({
        name: 'learnerReview',
        params: { sessionId: props.session.session_id }
    });
    window.open(route.href, '_blank', 'noopener,noreferrer');
};

</script>