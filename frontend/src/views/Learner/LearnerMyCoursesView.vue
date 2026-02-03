<template>
    <div class="space-y-8">
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
        <div>
            <h1 class="text-2xl sm:text-3xl font-bold text-slate-900 mb-6">내 강의실</h1>
            <LearnerMyCoursesStats :ongoingCount="ongoingCourses.length" :completedCount="completedCourses.length"
                :attendanceRate="0" />
        </div>
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

        <div v-if="displayedCourses.length > 0" class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-6">
            <LearnerMyCourseCard 
                v-for="course in displayedCourses" 
                :key="(course as any).courseId || (course as any).zzimId" 
                :course="course as any" 
                :workspace-id="Array.isArray(workspaceId) ? workspaceId[0] : workspaceId"
                @enter-course="handleEnterCourse" />
        </div>

        <div v-else class="bg-slate-50 rounded-2xl border-2 border-dashed border-slate-200 p-12 text-center">
            <div
                class="w-16 h-16 bg-slate-100 rounded-full flex items-center justify-center mx-auto mb-4 text-slate-400">
                <span class="material-symbols-outlined text-3xl">school</span>
            </div>
            <h3 class="text-lg font-bold text-slate-900 mb-2">과정이 없습니다.</h3>
            <p class="text-slate-500 mb-6">아직 수강 중인 과정이 없습니다. 새로운 과정을 찾아보세요!</p>
            <button @click="handleAddCourse"
                class="px-6 py-2.5 bg-indigo-600 text-white font-bold rounded-lg hover:bg-indigo-700 transition-colors">
                과정 보러가기
            </button>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { storeToRefs } from 'pinia';

import { useLearnerCourseStore } from '@/stores/learnerCourse';
import { useUserStore } from '@/stores/user';
import LearnerMyCoursesStats from '@/components/Learner/LearnerMyCoursesView/LearnerMyCoursesStats.vue';
import LearnerMyCourseCard from '@/components/Learner/LearnerMyCoursesView/LearnerMyCourseCard.vue';

const courseStore = useLearnerCourseStore();
const userStore = useUserStore();
const route = useRoute();
const router = useRouter();
const activeTab = ref('ongoing');
const { myEnrolledCourses, myLikedCourses } = storeToRefs(courseStore);
const { user } = storeToRefs(userStore);
const workspaceId = computed(() => route.params.id);

// 현재 route에 따라 home 경로 결정
const homeRoute = computed(() => {
    const isTrainer = route.name?.toString().includes('Trainer');
    return {
        name: isTrainer ? 'workspaceTrainer' : 'workspaceLearner',
        params: { id: workspaceId.value }
    };
});

// 1. 운영 중인 과정
const ongoingCourses = computed(() =>
    myEnrolledCourses.value.filter(course =>
        course.status === "PROCEED" || course.status === "INVITE"
    )
);

// 2. 종료된 과정
const completedCourses = computed(() =>
    myEnrolledCourses.value.filter(course =>
        course.status === "FINISH"
    )
);

// 3. 찜한 과정 (computed로 깔끔하게 정리)
const likedCourses = computed(() => myLikedCourses.value);

// 탭별 동적 카운트
const tabs = computed(() => [
    { id: 'ongoing', label: '운영 중인 과정', count: ongoingCourses.value.length },
    { id: 'liked', label: '찜한 과정', count: likedCourses.value.length }, // [수정] count 로직 수정
    { id: 'completed', label: '종료된 과정', count: completedCourses.value.length }
]);

onMounted(async () => {
    const wsId = Array.isArray(workspaceId.value) ? workspaceId.value[0] : workspaceId.value;
    if (wsId) {
        // [수정] 찜 목록과 수강 목록을 동시에 로드
        await Promise.all([
            courseStore.fetchMyEnrolledCourses(wsId),
            courseStore.fetchLikeCourses(wsId)
        ]);
    }
});

const displayedCourses = computed(() => {
    switch (activeTab.value) {
        case 'ongoing': return ongoingCourses.value;
        case 'liked': return likedCourses.value;
        case 'completed': return completedCourses.value;
        default: return [];
    }
});

const handleEnterCourse = (courseId: string | number) => {
    router.push({
        name: 'workspaceLearnerCourseDetail',
        params: {
            id: workspaceId.value,
            courseId: courseId
        }
    });
};

const handleEnrollCourse = async (courseId: string | number) => {
    if (confirm('이 과정을 수강신청하시겠습니까?')) {
        // [수정] 스토어 enrollCourse 인자 순서 보정
        const success = await courseStore.enrollCourse(courseId, Array.isArray(workspaceId.value) ? workspaceId.value[0] : workspaceId.value);
        if (success) {
            alert('수강신청이 성공적으로 완료되었습니다. 관리자 승인을 기다려주세요.');
            const wsId = Array.isArray(workspaceId.value) ? workspaceId.value[0] : workspaceId.value;
            if (wsId) await courseStore.fetchMyEnrolledCourses(wsId);
        } else {
            alert('수강신청에 실패했습니다. 다시 시도해주세요.');
        }
    }
};

const handleAddCourse = () => {
    const wsId = Array.isArray(workspaceId.value) ? workspaceId.value[0] : workspaceId.value;
    router.push({ name: 'workspaceLearner', params: { id: wsId } });
};
</script>