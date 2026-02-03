<template>
    <!-- 강사 워크스페이스 -->
    <div class="space-y-8">
        <!-- Stats Section -->
        <section>
            <TrainerMyCoursesStats :ongoingCount="ongoingCount" :completedCount="completedCount" :attendanceRate="0"
                ongoingLabel="운영 중인 강의" completedLabel="종료된 강의" attendanceLabel="전체 강의 진척도" />
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
            <div class="grid grid-cols-1 lg:grid-cols-2 gap-6" v-if="workspaceCourses.length > 0">
                <TrainerMyCourseCard v-for="course in workspaceCourses" :key="course.courseId" :course="(course as any)"
                    @enter-course="handleEnterCourse" />
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
import { useRoute } from 'vue-router';

// 워크스페이스 스토어
import { useWorkspaceStore } from '@/stores/workspace';
const workspaceStore = useWorkspaceStore();
const { workspaceStats } = storeToRefs(workspaceStore);
// 통계 데이터 직접 가공 (computed)
const ongoingCount = computed(() =>
    workspaceCourses.value.filter(c => c.status === 'PROCEED' || !c.status).length
);
const completedCount = computed(() =>
    workspaceCourses.value.filter(c => c.status === 'END').length
);
// 학생 코스 스토어(워크스페이스 모달용)
import { useLearnerCourseStore } from '@/stores/learnerCourse';
const learnerCourseStore = useLearnerCourseStore();
const { workspaceCourses } = storeToRefs(learnerCourseStore);


import { storeToRefs } from 'pinia';
import type { LearnerCourse, Session } from '@/types';
import { useRouter } from 'vue-router';
import TrainerMyCoursesStats from '@/components/Trainer/TrainerMyCoursesView/TrainerMyCoursesStats.vue';
import TrainerMyCourseCard from '@/components/Trainer/TrainerMyCoursesView/TrainerMyCourseCard.vue';
import TrainerCourseCurriculumModal from '@/components/Trainer/TrainerWorkspace/TrainerCourseCurriculumModal.vue';

const router = useRouter();

const route = useRoute();
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
const selectedCourse = ref<LearnerCourse>({} as LearnerCourse);
const mockSessions = ref<Session[]>([]);

// 커리큘럼 확인 모달 열기
const selectedCourseSessions = ref<Session[]>([]);
const openCurriculumModal = async (courseId: string | number) => {
    const course = workspaceCourses.value.find(c => String(c.courseId) === String(courseId));
    if (course) {
        selectedCourse.value = course;
        console.log("코스정보", course)
        const sessions = await learnerCourseStore.fetchCourseSessions(courseId);
        console.log(sessions)
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
        await workspaceStore.setCurrentWorkspaceById(id);
        await learnerCourseStore.fetchCoursesByWorkspace(id); // 강사: 관리 코스 목록 조회
        // await workspaceStore.fetchWorkspaceStats(id); // [Unused] 기존 통계 호출 제거
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