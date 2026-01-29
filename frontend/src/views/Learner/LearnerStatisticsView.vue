<template>
    <div class="space-y-8">
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
                    <router-link :to="homeRoute" class="hover:text-indigo-600">학습 관리</router-link>
                </li>
                <li class="flex items-center">
                    <span class="material-symbols-outlined !text-[1rem] leading-none text-slate-400">
                        chevron_right
                    </span>
                </li>
                <li class="font-medium text-indigo-600 flex items-center">학습 통계</li>
            </ol>
        </nav>
        <!-- 제목 + 카드영역 -->
        <div>
            <h1 class="text-2xl sm:text-3xl font-bold text-slate-900 mb-6">나의 학습 리포트</h1>
            <!-- 통계 카드 -->
            <!-- <MyCourseCard v-for="course in displayedCourses" :key="course.course_id" :course="course"
                @enter-course="handleEnterCourse" @enroll-course="handleEnrollCourse" /> -->

            <StatCard v-for="stat in statsStore.learnerWorkspaceTopCardStats" :key="stat.id" title="나의 학습 진행률"
                :value="`${workspaceStats.progress.value}%`" icon="donut_large" color="indigo"
                :trend="workspaceStats.progress.trend" :trendType="workspaceStats.progress.trend_type"
                :trendIcon="workspaceStats.progress.trend_type === 'up' ? 'trending_up' : 'trending_down'" />
        </div>


    </div>


</template>

<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router';
import { computed, onMounted } from 'vue';

import { useWorkspaceStore } from '@/stores/workspace';
import { useStatsStore } from '@/stores/stats';

// 스탯 컴포넌트(상단)
import StatCard from '@/components/common/CommonComponents/StatCard.vue';


const route = useRoute();
const router = useRouter();

const workspaceStore = useWorkspaceStore();
const statsStore = useStatsStore();

onMounted(async () => {
    const workspaceId = route.params.id;
    const userId = route.params.user_id;
    if (workspaceId && userId && !Array.isArray(workspaceId) && !Array.isArray(userId)) {
        await statsStore.fetchLearnerWorkspaceTopCardStats(workspaceId, userId);
    } else {
        console.error("URL 파라미터가 올바르지 않습니다.");
    }
});

// 현재 route에 따라 home 경로 결정
const homeRoute = computed(() => {
    const isTrainer = route.name?.toString().includes('Trainer');
    return {
        name: isTrainer ? 'workspaceTrainer' : 'workspaceLearner',
        params: { id: workspaceStore.currentWorkspace?.workspace_id }
    };
});
</script>