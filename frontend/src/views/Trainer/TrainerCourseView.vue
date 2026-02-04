<template>
    <div class="max-w-7xl mx-auto space-y-6">
        <!-- 헤더 (타이틀 및 버튼) -->
        <TrainerCourseDetailHeader title="데이터 기반 고객 경험 최적화 전략" />

        <!-- 통계 요약 (기간, 수강생, 상태) -->
        <TrainerCourseStats />

        <!-- 탭 네비게이션 -->
        <div class="border-b border-slate-200">
            <div class="flex space-x-8">
                <button v-for="tab in tabs" :key="tab.id" @click="handleTabChange(tab.id)"
                    class="pb-3 px-1 border-b-2 text-sm font-medium transition-colors"
                    :class="activeTab === tab.id ? 'border-indigo-600 text-indigo-600 font-bold' : 'border-transparent text-slate-500 hover:text-slate-700 hover:border-slate-300'">
                    {{ tab.label }}
                </button>
            </div>
        </div>

        <!-- 탭 내용 영역 -->
        <div>
            <!-- 세션 일정 관리 -->
            <div v-if="activeTab === 'session'">
                <TrainerSessionList :sessions="sessionList" @start-live="handleStartLive" />
            </div>
            <!-- 수강생 관리 -->
            <div v-else-if="activeTab === 'student'" class="space-y-6">
                <!-- 참가 신청 현황 -->
                <TrainerStudentRequestList />
                <!-- 수강생 명단 -->
                <TrainerStudentList />
            </div>
            <!-- 다른 탭 placeholder -->
            <div v-else class="py-20 text-center bg-slate-50 rounded-xl border border-dashed border-slate-200">
                <p class="text-slate-500">준비 중인 페이지입니다.</p>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import type { Session } from '@/types';
// 컴포넌트
import TrainerCourseDetailHeader from '@/components/Trainer/TrainerCourseView/TrainerCourseDetailHeader.vue';
import TrainerCourseStats from '@/components/Trainer/TrainerCourseView/TrainerCourseStats.vue';
import TrainerSessionList from '@/components/Trainer/TrainerCourseView/TrainerSessionList.vue';
import TrainerStudentRequestList from '@/components/Trainer/TrainerCourseView/TrainerStudentRequestList.vue';
import TrainerStudentList from '@/components/Trainer/TrainerCourseView/TrainerStudentList.vue';
const router = useRouter();
const route = useRoute();

import { useTrainerCourseStore } from '@/stores/trainerCourse';
const trainerCourseStore = useTrainerCourseStore();

const sessionList = ref<Session[]>([]);
const activeTab = ref('session');
const tabs = [
    { id: 'session', label: '세션 일정 관리' },
    { id: 'student', label: '수강생 관리' },
    { id: 'material', label: '자료실' },
    { id: 'stats', label: '교육통계' }
];

const handleTabChange = async (tabId: string) => {
    activeTab.value = tabId;
    const courseId = route.params.courseId;
    if (!courseId) return;

    if (tabId === 'student') {
        await Promise.all([
            trainerCourseStore.fetchStudentRequests(courseId as string),
            trainerCourseStore.fetchStudents(courseId as string)
        ]);
    }
};

onMounted(async () => {
    const courseId = route.params.courseId;
    console.log("courseId", courseId);
    if (courseId) {
        sessionList.value = await trainerCourseStore.fetchCourseSessions(Number(courseId));
        // 초기 로드 시 탭에 따라 데이터 로드
        if (activeTab.value === 'student') {
            await Promise.all([
                trainerCourseStore.fetchStudentRequests(courseId as string),
                trainerCourseStore.fetchStudents(courseId as string)
            ]);
        }
    }
});

// 테스트용 임시 코드
import api from "@/api";
import { useTrainerLiveStore } from '@/stores/trainerlive';
const trainerLiveStore = useTrainerLiveStore();


interface LiveSessionData {
    janusRoomId: number;
    pin: string;
    sessionId: number;
    updatedAt: number; // 데이터의 최신성을 확인하기 위한 타임스탬프
}

const handleStartLive = async (session: any) => {
    try {
        const response = await api.post(`https://i14a309.p.ssafy.io/api/session/${session.sessionId}/start`);
        const { janusRoomId, pin, sessionId } = response.data;
        const liveData: LiveSessionData = {
            janusRoomId,
            pin,
            sessionId,
            updatedAt: Date.now()
        };
        localStorage.setItem('live_session_info', JSON.stringify(liveData));
        console.log("성공 응답 및 로컬스토리지 저장 완료:", liveData);
        alert("라이브가 성공적으로 시작되었습니다.");
        // 라이브 세션 입장
        const resolvedRoute = router.resolve({
            name: 'trainerLiveSession',
            params: {
                courseId: route.params.courseId,
                sessionId: sessionId
            }
        });
        window.open(resolvedRoute.href, '_blank', 'noopener,noreferrer');

    } catch (error: any) {
        console.error("프론트 에러 로그:", error.response?.data || error.message);
        alert(`에러 발생: ${error.response?.status} - 서버 메시지를 확인하세요.`);
    }
};
</script>
