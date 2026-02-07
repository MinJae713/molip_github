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
                    <option v-for="course in courseStore.myEnrolledCourses" :key="course.courseId"
                        :value="course.courseId">
                        {{ course.title }}
                    </option>
                </select>
            </div>
        </div>

        <!-- 로딩 중일 때 (Skeleton UI) -->
        <template v-if="isLoading">
            <!-- 통계 카드 스켈레톤 (3열) -->
            <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div v-for="n in 3" :key="n" class="bg-white rounded-xl border border-slate-200 p-6 shadow-sm animate-pulse">
                    <div class="flex items-start justify-between">
                        <div class="space-y-3 flex-1">
                            <div class="h-4 bg-slate-100 rounded w-1/3"></div>
                            <div class="h-8 bg-slate-100 rounded w-1/2"></div>
                        </div>
                        <div class="size-12 rounded-xl bg-slate-100"></div>
                    </div>
                </div>
            </div>

            <!-- 차트 영역 스켈레톤 (2열) -->
            <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
                <!-- 세션별 학습 시간 스켈레톤 -->
                <div class="bg-white rounded-xl border border-slate-200 p-6 shadow-sm animate-pulse h-80">
                    <div class="h-6 bg-slate-100 rounded w-1/4 mb-6"></div>
                    <div class="flex items-end justify-between h-48 gap-2">
                        <div v-for="n in 7" :key="n" class="w-full bg-slate-100 rounded-t" :style="{ height: `${Math.random() * 80 + 20}%` }"></div>
                    </div>
                </div>
                <!-- 세션별 집중도 트렌드 스켈레톤 -->
                <div class="bg-white rounded-xl border border-slate-200 p-6 shadow-sm animate-pulse h-80">
                    <div class="h-6 bg-slate-100 rounded w-1/3 mb-6"></div>
                    <div class="h-48 bg-slate-100 rounded"></div>
                </div>
            </div>

            <!-- 상세 리포트 테이블 영역 스켈레톤 -->
            <div class="bg-white rounded-xl border border-slate-200 shadow-sm animate-pulse mt-6">
                <div class="p-6 border-b border-slate-100">
                    <div class="h-6 bg-slate-100 rounded w-1/4"></div>
                </div>
                <div class="p-6 space-y-4">
                    <div v-for="n in 5" :key="n" class="h-12 bg-slate-100 rounded"></div>
                </div>
            </div>
        </template>

        <!-- 통계 데이터가 있는 경우 -->
        <template v-else-if="learnerWorkspaceTopCardStats">
            <!-- 통계 카드 그리드 (3열) -->
            <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
                <StatCard v-for="stat in learnerWorkspaceTopCardStats.values" :key="stat.title" :data="stat" />
            </div>

            <!-- 차트 영역 (2열) -->
            <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
                <!-- 세션별 학습 시간 -->
                <SessionTimeChart :stats="learnerWeeklyStats" />
                <!-- 세션별 집중도 트렌드 -->
                <SessionConcentrationChart :stats="learnerSessionStats" :session_name="currentCourseName" />
            </div>

            <!-- 상세 리포트 테이블 영역 -->
            <div class="mt-6">
                <SessionReportTable :reports="learnerSessionReports?.sessions || []" :course-name="currentCourseName" />
            </div>
        </template>

        <!-- 데이터가 없는 경우 (Empty State) -->
        <div v-else class="flex flex-col items-center justify-center py-32 bg-slate-50 border border-dashed border-slate-200 rounded-xl text-center">
            <div class="bg-white p-4 rounded-full shadow-sm mb-4">
                <span class="material-symbols-outlined text-4xl text-slate-300">bar_chart</span>
            </div>
            <h3 class="text-lg font-bold text-slate-900 mb-1">표시할 학습 통계가 없습니다</h3>
            <p class="text-slate-500 text-sm max-w-md">아직 학습 데이터가 집계되지 않았거나, 수강 중인 강의가 없습니다.<br>세션에 참여하여 나만의 학습 리포트를 만들어보세요!</p>
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
import SessionTimeChart from '@/components/Learner/LearnerStatisticsView/SessionTimeChart.vue';
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


const isLoading = ref(true);

// 현재 선택된 코스 이름 찾기 (Computed)
const currentCourseName = computed(() => {
    const course = courseStore.myEnrolledCourses.find(
        c => c.courseId == selectedCourseId.value
    );
    return course ? course.title : '선택된 강의 없음';
});

// 통합 데이터 로드 함수
const loadStatistics = async (courseId: string | number) => {
    isLoading.value = true;
    try {
        // Single API call now
        await statsStore.fetchCourseStats(courseId);
    } finally {
        isLoading.value = false;
    }
};

onMounted(async () => {
    isLoading.value = true;
    try {
        const workspaceId = route.params.id as string;
        const userId = userStore.user?.memberId;

        // 1. 워크스페이스 정보가 없을 경우 fetch
        if (workspaceStore.myWorkspaces.length === 0) {
            await workspaceStore.fetchMyWorkspaces();
        }
        
        // 2. 코스 목록 로드
        if (workspaceId) {
            await courseStore.fetchMyEnrolledCourses(workspaceId);
        }
        
        // 3. 초기 코스 선택 (목록이 있으면 첫 번째 코스 선택)
        if (courseStore.myEnrolledCourses.length > 0) {
             // 이미 선택된 값이 유효한 코스인지 확인
            const isValid = courseStore.myEnrolledCourses.some(c => c.courseId == selectedCourseId.value);
            
            const firstCourse = courseStore.myEnrolledCourses[0];
            if ((!isValid || selectedCourseId.value === 'all') && firstCourse) {
                 selectedCourseId.value = firstCourse.courseId;
            }
        }

        // 4. 통계 로드
        if (userId && selectedCourseId.value !== 'all') {
            await loadStatistics(selectedCourseId.value);
        } else {
             isLoading.value = false; // 데이터 로드할 필요가 없는 경우 로딩 종료
        }
    } catch (e) {
        isLoading.value = false;
    }

    // 코스 선택 변경 감지 및 데이터 리로드
    watch(selectedCourseId, async (newId) => {
        if (newId && newId !== 'all') {
            await loadStatistics(newId);
        }
    });

});


// 현재 route에 따라 home 경로 결정
const homeRoute = computed(() => {
    const routeName = route.name?.toString() || '';
    const isTrainer = routeName.includes('Trainer');
    return {
        name: isTrainer ? 'workspaceTrainer' : 'workspaceLearner',
        params: { id: workspaceStore.currentWorkspace?.workspaceId || route.params.id }
    };
});
</script>
