<template>
    <div class="max-w-7xl mx-auto space-y-8">
        <!-- 상단 breadcrumb -->
        <nav aria-label="Breadcrumb" class="flex text-sm text-slate-500">
            <ol class="flex items-center space-x-2">
                <li class="flex items-center">
                    <router-link :to="homeRoute" class="hover:text-indigo-600">홈</router-link>
                </li>
                <li class="flex items-center">
                    <span class="material-symbols-outlined !text-[1rem] leading-none text-slate-400">
                        chevron_right
                    </span>
                </li>
                <li class="flex items-center">
                    <span class="text-slate-500">학습 관리</span>
                </li>
                <li class="flex items-center">
                    <span class="material-symbols-outlined !text-[1rem] leading-none text-slate-400">
                        chevron_right
                    </span>
                </li>
                <li class="font-medium text-slate-800 flex items-center">학습 통계</li>
            </ol>
        </nav>

        <!-- 제목 + 코스 선택 영역 -->
        <div class="flex flex-col md:flex-row md:items-center justify-between gap-4">
            <div>
                <h1 class="text-2xl sm:text-3xl font-bold text-slate-900">나의 학습 리포트</h1>
                <p class="text-slate-500 text-sm mt-1">학습 패턴과 세션별 성취도를 정밀하게 분석하여 시각화합니다.</p>
            </div>
            <!-- 코스 선택기 -->
            <div class="flex items-center gap-3 bg-white p-1 pr-2 rounded-lg border border-slate-200 shadow-sm">
                <div class="pl-3 text-xs font-bold text-slate-400 uppercase tracking-wide">Course</div>
                <select v-model="selectedCourseId" class="...">
                    <option value="all">전체 코스 통계</option>
                    <option v-for="course in courseStore.myEnrolledCourses" :key="course.courseId"
                        :value="course.courseId">
                        {{ course.title }}
                    </option>
                </select>
            </div>
        </div>

        <!-- 통계 카드 그리드 (4열) -->
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4" v-if="learnerWorkspaceTopCardStats">
            <StatCard v-for="stat in learnerWorkspaceTopCardStats.values" :key="stat.title" :data="stat" />
        </div>

        <!-- 차트 영역 (2열) -->
        <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
            <!-- 주간 학습 참여도 -->
            <WeeklyLearningChart :weekly-data="learnerWeeklyStats" />
            <!-- 세션별 집중도 트렌드 -->
            <SessionConcentrationChart :stats="learnerSessionStats" :session_name="currentCourseName"
                :is-all="selectedCourseId === 'all'" />
        </div>

        <!-- 상세 리포트 테이블 영역 -->
        <div class="mt-6" v-if="selectedCourseId !== 'all'">
            <SessionReportTable :reports="learnerSessionReports?.sessions" :course-name="currentCourseName" />
        </div>
    </div>
</template>

<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router';
import { computed, onMounted, ref, watch } from 'vue';

import { useWorkspaceStore } from '@/stores/workspace';
import { useStatsStore } from '@/stores/stats';
import { useUserStore } from '@/stores/user';
import { useLearnerCourseStore } from '@/stores/learnerCourse';
import { storeToRefs } from 'pinia';

// 컴포넌트 임포트
import StatCard from '@/components/common/CommonComponents/StatCard.vue';
import WeeklyLearningChart from '@/components/Learner/LearnerStatisticsView/WeeklyLearningChart.vue';
import SessionConcentrationChart from '@/components/Learner/LearnerStatisticsView/SessionConcentrationChart.vue';
import SessionReportTable from '@/components/Learner/LearnerStatisticsView/SessionReportTable.vue';

const route = useRoute();
const router = useRouter();

const workspaceStore = useWorkspaceStore();
const statsStore = useStatsStore();
const userStore = useUserStore();
const courseStore = useLearnerCourseStore();

const {
    learnerWorkspaceTopCardStats,
    learnerWeeklyStats,
    learnerSessionStats,
    selectedCourseId,
    learnerSessionReports,
} = storeToRefs(statsStore);


// 현재 선택된 코스 이름 찾기 (Computed)
const currentCourseName = computed(() => {
    if (selectedCourseId.value === 'all') return '전체 강의';

    const course = courseStore.myEnrolledCourses.find(
        c => c.courseId == selectedCourseId.value
    );
    return course ? course.title : '알 수 없는 강의';
});

// 통합 데이터 로드 함수
const loadStatistics = async (workspaceId: string, courseId: string | number, userId: string | number) => {
    await Promise.all([
        statsStore.fetchLearnerWorkspaceTopCardStats(workspaceId, courseId, userId),
        statsStore.fetchLearnerWeeklyStats(workspaceId, courseId, userId),
        statsStore.fetchLearnerSessionStats(workspaceId, courseId, userId),
        statsStore.fetchLearnerSessionReports(workspaceId, courseId, userId)
    ]);
};

onMounted(async () => {
    const workspaceId = route.params.id as string;
    const userId = userStore.user?.memberId;

    if (workspaceId && userId) {
        // 초기 로딩
        await loadStatistics(workspaceId, selectedCourseId.value, userId);
    }

    // 코스 목록 로드
    await courseStore.fetchMyEnrolledCourses();

    // 코스 선택 변경 감지 및 데이터 리로드
    watch(selectedCourseId, async (newId) => {
        if (workspaceId && userId) {
            await loadStatistics(workspaceId, newId, userId);
        }
    });

    // 워크스페이스 정보가 없을 경우 fetch
    if (workspaceStore.myWorkspaces.length === 0) {
        await workspaceStore.fetchMyWorkspaces();
    }
});

// 현재 route에 따라 home 경로 결정
const homeRoute = computed(() => {
    const isTrainer = route.name?.toString().includes('Trainer');
    return {
        name: isTrainer ? 'workspaceTrainer' : 'workspaceLearner',
        params: { id: workspaceStore.currentWorkspace?.workspaceId || route.params.id }
    };
});
</script>
