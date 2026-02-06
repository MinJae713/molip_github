<template>
    <!-- 강사 워크스페이스 -->
    <div class="space-y-8">
        <!-- Stats Section -->
        <section>
            <TrainerMyCoursesStats :ongoingCount="workspaceStore.trainerWorkspaceStats?.activeCourseCount || 0"
                :completedCount="workspaceStore.trainerWorkspaceStats?.learnersCount || 0"
                :attendanceRate="workspaceStore.trainerWorkspaceStats?.todaySessionCount || 0" ongoingLabel="운영 중인 강의"
                completedLabel="전체 수강생" attendanceLabel="오늘 예정된 세션" ongoingUnit="개" completedUnit="명" attendanceUnit="개"
                ongoingIcon="school" completedIcon="groups" attendanceIcon="calendar_month"
                ongoingDesc="현재 진행 중인 활성 강의 수" completedDesc="등록된 전체 수강생 수" attendanceDesc="오늘 예정된 라이브/코스 세션 수" />
        </section>
        <!-- Course List Section -->
        <section>
            <div class="flex flex-col md:flex-row md:items-center justify-between mb-6 gap-4">
                <h2 class="text-lg font-bold text-slate-900 flex items-center gap-2">
                    <span class="material-symbols-outlined text-indigo-brand">school</span>
                    전체 강의 목록
                </h2>
                <div class="flex flex-wrap items-center gap-3">
                    <div class="relative">
                        <span class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                            <span class="material-symbols-outlined text-slate-400 text-sm">search</span>
                        </span>
                        <input v-model="searchQuery"
                            class="pl-9 pr-4 py-2 border border-slate-200 rounded-lg text-sm focus:outline-none focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500 w-48 md:w-64 transition-all"
                            placeholder="강의 검색..." type="text" />
                    </div>
                    <!-- Sort Dropdown -->
                    <div class="relative group">
                        <button
                            class="flex items-center gap-2 px-4 py-2 text-sm font-medium text-slate-600 bg-white border border-slate-200 rounded-lg hover:bg-slate-50 transition-colors">
                            <span class="material-symbols-outlined text-lg">sort</span>
                            <span>{{ sortOptionLabel }}</span>
                            <span class="material-symbols-outlined text-slate-400 text-sm">expand_more</span>
                        </button>
                        <div
                            class="absolute right-0 mt-2 w-36 bg-white border border-slate-200 rounded-xl shadow-lg z-20 hidden group-hover:block filter-dropdown-content">
                            <div class="p-1">
                                <button @click="setSort('newest')"
                                    :class="['w-full text-left px-3 py-2 text-sm font-medium rounded-lg transition-colors', sortOption === 'newest' ? 'text-indigo-600 bg-indigo-50' : 'text-slate-600 hover:bg-slate-50']">최신순</button>
                                <button @click="setSort('deadline')"
                                    :class="['w-full text-left px-3 py-2 text-sm font-medium rounded-lg transition-colors', sortOption === 'deadline' ? 'text-indigo-600 bg-indigo-50' : 'text-slate-600 hover:bg-slate-50']">마감
                                    임박순</button>
                                <button @click="setSort('oldest')"
                                    :class="['w-full text-left px-3 py-2 text-sm font-medium rounded-lg transition-colors', sortOption === 'oldest' ? 'text-indigo-600 bg-indigo-50' : 'text-slate-600 hover:bg-slate-50']">오래된순</button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <!-- 코스(강의 영역) -->
            <div class="grid grid-cols-1 lg:grid-cols-2 gap-6" v-if="workspaceAllCourses.length > 0">
                <TrainerDashboardCourseCard v-for="course in workspaceAllCourses" :key="course.courseId"
                    :course="(course as any)" @manage-course="handleEnterCourse" @open-curriculum="openCurriculumModal"
                    @view-history="" />
            </div>
            <div v-else class="text-center py-20 bg-slate-50 rounded-xl border border-dashed border-slate-200">
                <p class="text-slate-500">등록된 강의가 없습니다.</p>
            </div>
        </section>
        <!-- Course Curriculum Modal -->
        <TrainerCourseCurriculumModal :isOpen="isModalOpen" :course="selectedCourse" :sessions="selectedCourseSessions"
            @close="closeModal" @register="handleRegister" />
    </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { storeToRefs } from 'pinia';
import type { TrainerCourse, Session } from '@/types';

import { useTrainerCourseStore } from '@/stores/trainerCourse';
import { useLearnerCourseStore } from '@/stores/learnerCourse';
import { useWorkspaceStore } from '@/stores/workspace';

import TrainerMyCoursesStats from '@/components/Trainer/TrainerMyCoursesView/TrainerMyCoursesStats.vue';
import TrainerDashboardCourseCard from '@/components/Trainer/TrainerWorkspace/TrainerDashboardCourseCard.vue';
import TrainerCourseCurriculumModal from '@/components/Trainer/TrainerWorkspace/TrainerCourseCurriculumModal.vue';

const route = useRoute();
const router = useRouter();

// 워크스페이스 스토어
const trainerCourseStore = useTrainerCourseStore();
const { workspaceAllCourses } = storeToRefs(trainerCourseStore);

const workspaceStore = useWorkspaceStore();

// 학생 코스 스토어(워크스페이스 모달용)
const learnerCourseStore = useLearnerCourseStore();

// 현재 워크스페이스 아이디 획득
const currentWorkspaceId = computed(() => {
    const id = route.params.id;
    return Array.isArray(id) ? id[0] : id;
});

// 검색어
const searchQuery = ref('');
// 정렬 옵션
const sortOption = ref<'newest' | 'deadline' | 'oldest'>('newest');

// 모달 상태관리
const isModalOpen = ref(false);
const selectedCourse = ref<TrainerCourse>({} as TrainerCourse);

// 커리큘럼 확인 모달 열기
const selectedCourseSessions = ref<Session[]>([]);
const openCurriculumModal = async (courseId: string | number) => {
    console.log(courseId)
    const course = workspaceAllCourses.value.find(c => String(c.courseId) === String(courseId));
    if (course) {
        selectedCourse.value = course;
        const sessions = await learnerCourseStore.fetchCourseSessions(courseId);
        selectedCourseSessions.value = sessions;
        isModalOpen.value = true;
    }
};
const closeModal = () => {
    isModalOpen.value = false;
};

const handleRegister = () => {
    alert('수강 신청이 완료되었습니다! (Mock Action)');
    closeModal();
};

const handleEnterCourse = (courseId: string | number) => {
    console.log('과정 관리 페이지 이동:', courseId);
    router.push({
        name: 'workspaceTrainerCourseDetail',
        params: {
            id: currentWorkspaceId.value,
            courseId: courseId
        }
    });
};

const sortOptionLabel = computed(() => {
    switch (sortOption.value) {
        case 'newest': return '최신순';
        case 'deadline': return '마감 임박순';
        case 'oldest': return '오래된순';
        default: return '정렬';
    }
});
const setSort = (option: 'newest' | 'deadline' | 'oldest') => {
    sortOption.value = option;
};

// 대시보드 초기화
const initDashboard = async () => {
    const workspaceId = route.params.id;
    if (workspaceId) {
        const id = Array.isArray(workspaceId) ? workspaceId[0] : workspaceId;
        if (!id) return;
        await trainerCourseStore.fetchWorkspaceAllCourses(id);
    }
};
onMounted(() => {
    initDashboard();
});
watch(() => route.params.id, () => {
    initDashboard();
});
</script>
<style scoped>
.text-indigo-brand {
    color: #4F46E5;
}

/* Dropdown fix */
.filter-dropdown-content {
    display: none;
}

.group:hover .filter-dropdown-content,
.group:focus-within .filter-dropdown-content {
    display: block;
}
</style>