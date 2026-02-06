<template>
    <div class="space-y-8">
        <!-- 페이지 제목 -->
        <div>
            <h1 class="text-2xl sm:text-3xl font-bold text-slate-900 mb-6">내 강의실</h1>
            <!-- 통계 카드 -->
            <!-- 통계 카드 -->
            <TrainerMyCoursesStats :ongoingCount="workspaceStore.trainerWorkspaceStats?.activeCourseCount || 0"
                :completedCount="workspaceStore.trainerWorkspaceStats?.learnersCount || 0"
                :attendanceRate="workspaceStore.trainerWorkspaceStats?.todaySessionCount || 0" ongoingLabel="운영 중인 강의"
                completedLabel="전체 수강생" attendanceLabel="오늘 예정된 세션" ongoingUnit="개" completedUnit="명" attendanceUnit="개"
                ongoingIcon="school" completedIcon="groups" attendanceIcon="calendar_month"
                ongoingDesc="현재 진행 중인 활성 강의 수" completedDesc="등록된 전체 수강생 수" attendanceDesc="오늘 예정된 라이브/코스 세션 수" />
        </div>
        <div class="border-b border-slate-200 flex items-center justify-between">
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

            <button @click="handleAddCourse"
                class="px-6 py-2.5 bg-indigo-600 text-white font-bold rounded-lg hover:bg-indigo-700 transition-colors">
                과정 추가하기
            </button>
        </div>
        <!-- 강의 목록 -->
        <div v-if="displayedCourses.length > 0" class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-6">
            <TrainerMyCourseCard v-for="course in displayedCourses" :key="course.courseId" :course="course"
                @enter-course="handleEnterCourse" @enroll-course="handleEnrollCourse" />
        </div>

        <!-- 데이터 없음 안내 -->
        <div v-else class="bg-slate-50 rounded-2xl border-2 border-dashed border-slate-200 p-12 text-center">
            <div
                class="w-16 h-16 bg-slate-100 rounded-full flex items-center justify-center mx-auto mb-4 text-slate-400">
                <span class="material-symbols-outlined text-3xl">school</span>
            </div>
            <h3 class="text-lg font-bold text-slate-900 mb-2">과정이 없습니다.</h3>
            <p class="text-slate-500 mb-6">등록된 과정이 없습니다.</p>
            <button @click="handleAddCourse"
                class="px-6 py-2.5 bg-indigo-600 text-white font-bold rounded-lg hover:bg-indigo-700 transition-colors">
                과정 추가하기
            </button>
        </div>

        <!-- 과정 생성 모달 -->
        <CreateCourseModal v-if="workspaceId" :is-open="isCreateModalOpen" :workspace-id="workspaceId"
            :user-id="userStore.user.memberId" @close="isCreateModalOpen = false" @created="handleCourseCreated" />
    </div>
</template>
<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { storeToRefs } from 'pinia';

import { useTrainerCourseStore } from '@/stores/trainerCourse';
import { useUserStore } from '@/stores/user';
import { useWorkspaceStore } from '@/stores/workspace';
import TrainerMyCoursesStats from '@/components/Trainer/TrainerMyCoursesView/TrainerMyCoursesStats.vue';
import TrainerMyCourseCard from '@/components/Trainer/TrainerMyCoursesView/TrainerMyCourseCard.vue';
import CreateCourseModal from '@/components/Trainer/TrainerMyCoursesView/CreateCourseModal.vue';

const trainerCourseStore = useTrainerCourseStore();
const userStore = useUserStore();
const workspaceStore = useWorkspaceStore();
const route = useRoute();
const router = useRouter();
const activeTab = ref('ongoing');
const isCreateModalOpen = ref(false);

const { managedCourses } = storeToRefs(trainerCourseStore);

const workspaceId = computed(() => {
    const id = route.params.id;
    return Array.isArray(id) ? id[0] : (id || '');
});

// 활성화 된 코스 필터링
const trainerOngoingCourses = computed(() =>
    managedCourses.value.filter(course =>
        !course.status || course.status === 'PROCEED'
    )
);
// 모집중 코스 필터링
const recruitingCourses = computed(() =>
    managedCourses.value.filter(course =>
        course.status === 'RECRUIT'
    )
);
// 종료된 코스 필터링
const completedCourses = computed(() =>
    managedCourses.value.filter(course =>
        course.status === 'END'
    )
);

// 탭별 동적 카운트
const tabs = computed(() => [
    { id: 'ongoing', label: '운영 중인 과정', count: trainerOngoingCourses.value.length },
    { id: 'recruiting', label: '모집 중인 과정', count: recruitingCourses.value.length },
    { id: 'completed', label: '종료된 과정', count: completedCourses.value.length }
]);

onMounted(async () => {
    await userStore.fetchUserInfo();
    if (workspaceId.value) {
        // [수정] 강사 본인의 강의 목록 조회 (RESTful API)
        await Promise.all([
            trainerCourseStore.fetchMyCourses(workspaceId.value),
            workspaceStore.fetchWorkspaceStats(workspaceId.value)
        ]);
    }
});

const displayedCourses = computed(() => {
    switch (activeTab.value) {
        case 'ongoing': return trainerOngoingCourses.value;
        case 'recruiting': return recruitingCourses.value;
        case 'completed': return completedCourses.value;
        default: return [];
    }
});


const handleEnterCourse = (courseId: string | number) => {
    console.log('강의실 입장:', courseId);
    router.push({
        name: 'workspaceTrainerCourseDetail',
        params: {
            id: workspaceId.value,
            courseId: courseId
        }
    });
};

// 강사는 수강신청이 아니라 과정 개설/수정 버튼이 필요함
const handleEnrollCourse = async (courseId: string | number) => {
    // Empty for Trainer View
};
const handleAddCourse = () => {
    console.log('새 과정 신청');
    isCreateModalOpen.value = true;
};

const handleCourseCreated = () => {
    alert('새 과정이 성공적으로 개설되었습니다.');
};
</script>
