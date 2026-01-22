<template>
    <div class="space-y-8">
        <!-- Breadcrumb -->
        <nav aria-label="Breadcrumb" class="flex text-sm text-slate-500">
            <ol class="flex items-center space-x-2">
                <li><router-link :to="homeRoute" class="hover:text-indigo-600">
                        홈
                    </router-link></li>
                <li><span class="material-symbols-outlined text-sm text-slate-400">chevron_right</span></li>
                <li><span class="font-medium text-slate-800">학습 관리</span></li>
                <li><span class="material-symbols-outlined text-sm text-slate-400">chevron_right</span></li>
                <li class="font-medium text-indigo-600">내 강의실</li>
            </ol>
        </nav>
        <!-- 페이지 제목 -->
        <div>
            <h1 class="text-2xl sm:text-3xl font-bold text-slate-900 mb-6">내 강의실</h1>
            <!-- 통계 카드 -->
            <MyCoursesStats :ongoingCount="ongoingCourses.length" :completedCount="completedCourses.length"
                :attendanceRate="92" />
        </div>
        <!-- 탭 네비게이션 -->
        <div class="border-b border-slate-200">
            <nav aria-label="Tabs" class="flex gap-8">
                <a v-for="tab in tabs" :key="tab.id" :class="[
                    'border-b-2 py-4 px-1 text-sm font-medium flex items-center gap-2 transition-colors',
                    activeTab === tab.id
                        ? 'border-indigo-600 text-indigo-600 font-bold'
                        : 'border-transparent text-slate-500 hover:text-slate-700 hover:border-slate-300'
                ]" href="#" @click.prevent="activeTab = tab.id">
                    {{ tab.label }}
                    <span :class="[
                        'py-0.5 px-2 rounded-full text-xs',
                        activeTab === tab.id
                            ? 'bg-indigo-100 text-indigo-700'
                            : 'bg-slate-100 text-slate-600'
                    ]">
                        {{ tab.count }}
                    </span>
                </a>
            </nav>
        </div>
    </div>
</template>
<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRoute } from 'vue-router';

import { useCourseStore } from '@/stores/course';
import MyCoursesStats from '@/components/Learner/LearnerMyCoursesView/MyCoursesStats.vue';
import MyCourseCard from '@/components/Learner/LearnerMyCoursesView/MyCourseCard.vue';

const courseStore = useCourseStore();
const route = useRoute();
const activeTab = ref('ongoing');

const workspaceId = computed(() => route.params.id);

// 현재 route에 따라 home 경로 결정
const homeRoute = computed(() => {
    const isTrainer = route.name?.toString().includes('Trainer');
    return {
        name: isTrainer ? 'workspaceTrainer' : 'workspaceLearner',
        params: { id: workspaceId.value }
    };
});

const tabs = [
    { id: 'ongoing', label: '학습 중인 과정', count: 3 },
    { id: 'liked', label: '찜한 과정', count: 2 },
    { id: 'completed', label: '수료한 과정', count: 12 }
];
// Mock data - 실제로는 courseStore에서 가져와야 함
const ongoingCourses = ref([
    {
        id: 1,
        title: '비즈니스 AI 윤리 가이드',
        description: '생성형 AI 도입 시 고려해야 할 기업 윤리, 데이터 보안, 저작권 이슈에 대한 포괄적 가이드.',
        category: 'Business AI Track > Level 1',
        tag: '필수 과정',
        is_mandatory: true,
        progress: 40,
        currentLessons: 2,
        totalLessons: 5,
        remaining_hours: 3,
        has_live_session: true,
        next_schedule: {
            type: 'live',
            icon: 'videocam',
            title: '다음 세션: 실시간 강의',
            time: '오늘 14:00 - 16:00'
        }
    },
    // ... 더 많은 강의 추가
]);
const completedCourses = ref([]);
const displayedCourses = computed(() => {
    switch (activeTab.value) {
        case 'ongoing': return ongoingCourses.value;
        case 'liked': return []; // 찜한 과정
        case 'completed': return completedCourses.value;
        default: return [];
    }
});
const handleEnterCourse = (courseId: string | number) => {
    console.log('강의실 입장:', courseId);
    // TODO: 강의실 페이지로 라우팅
};
const handleAddCourse = () => {
    console.log('새 과정 신청');
    // TODO: 과정 검색 모달 열기
};
const handleViewRecommendation = () => {
    console.log('추천 과정 보기');
    // TODO: 추천 과정 페이지로 이동
};
onMounted(() => {
    // courseStore.fetchMyEnrolledCourses();
});
</script>