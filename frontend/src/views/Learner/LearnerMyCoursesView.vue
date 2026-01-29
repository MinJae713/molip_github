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
            <LearnerMyCoursesStats :ongoingCount="ongoingCourses.length" :completedCount="completedCourses.length"
                :attendanceRate="myCoursesAttendanceRate || 0" />
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

        <!-- 강의 목록 -->
        <div v-if="displayedCourses.length > 0" class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-6">
            <LearnerMyCourseCard v-for="course in displayedCourses" :key="course.course_id" :course="course"
                @enter-course="handleEnterCourse" @enroll-course="handleEnrollCourse" />
        </div>

        <!-- 데이터 없음 안내 -->
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

const { myEnrolledCourses, myCoursesAttendanceRate, myLikedCourses } = storeToRefs(courseStore);
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

// Store에서 가져온 데이터를 상태별로 필터링
// progress_rate로 학습 진행 상태를 구분 (0-99: 진행중/대기중, 100: 완료)
const ongoingCourses = computed(() =>
    myEnrolledCourses.value.filter(course =>
        course.progress_rate < 100
    )
);

const completedCourses = computed(() =>
    myEnrolledCourses.value.filter(course =>
        course.progress_rate === 100
    )
);
const likedCoursesCount = computed(() => likedCourses.value.length);
const likedCourses = computed(() => myLikedCourses.value);

// 탭별 동적 카운트
const tabs = computed(() => [
    { id: 'ongoing', label: '학습 중인 과정', count: ongoingCourses.value.length },
    { id: 'liked', label: '찜한 과정', count: likedCoursesCount },
    { id: 'completed', label: '수료한 과정', count: completedCourses.value.length }
]);

onMounted(async () => {
    // 사용자 정보 로드 (memberId 확보용)
    // 내 강의 목록과 이번달 출석률 불러오기
    const wsId = Array.isArray(workspaceId.value) ? workspaceId.value[0] : workspaceId.value;
    if (wsId) {
        await courseStore.fetchMyEnrolledCourses(
            wsId,
            userStore.user.member_id,
            'LEARNER'
        );
    }
    await courseStore.fetchMyCoursesAttendanceRate();
    await courseStore.fetchLikeCourses();
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
    console.log('강의실 입장:', courseId);
    router.push({
        name: 'workspaceLearnerCourseDetail',
        params: {
            id: workspaceId.value,
            courseId: courseId
        }
    });
};
const handleEnrollCourse = async (courseId: string | number) => {
    if (confirm('이 과정을 수항신청하시겠습니까?')) {
        const success = await courseStore.enrollCourse(courseId, user.value.member_id);
        if (success) {
            alert('수강신청이 성공적으로 완료되었습니다. 관리자 승인을 기다려주세요.');
            // 목록 새로고침
            const wsId = Array.isArray(workspaceId.value) ? workspaceId.value[0] : workspaceId.value;
            if (wsId) {
                await courseStore.fetchMyEnrolledCourses(wsId, user.value.member_id, 'LEARNER');
            }
        } else {
            alert('수강신청에 실패했습니다. 다시 시도해주세요.');
        }
    }
};
const handleAddCourse = () => {
    console.log('새 과정 신청');
    // TODO: 과정 검색 모달 열기
};
const handleViewRecommendation = () => {
    console.log('추천 과정 보기');
    // TODO: 추천 과정 페이지로 이동
};
</script>