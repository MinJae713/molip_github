<template>
    <!-- 강사 워크스페이스 -->
    <div class="space-y-8">
        <!-- Stats Section -->
        <section class="grid grid-cols-1 md:grid-cols-3 gap-6" v-if="workspaceStats">
            <TrainerStatCard title="운영 중인 교육 과정" :value="`${workspaceStats.progress.value} 과정`" icon="school"
                color="indigo" />

            <TrainerStatCard title="참여 수강생 수" :value="`${workspaceStats.sessions.value} 명`" icon="groups"
                color="blue" />

            <TrainerStatCard title="오늘 예정된 세션" :value="`${workspaceStats.completed.value} 과정`" icon="schedule"
                color="orange" />
        </section>
        <!-- Loading State for Stats -->
        <div v-else class="grid grid-cols-1 md:grid-cols-3 gap-6">
            <div class="h-32 bg-slate-100 rounded-xl animate-pulse" v-for="n in 3" :key="n"></div>
        </div>
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

            <!-- Course Grid -->
            <div class="grid grid-cols-1 lg:grid-cols-2 gap-6" v-if="filteredCourses.length > 0">
                <TrainerDashboardCourseCard v-for="course in filteredCourses" :key="course.course_id" :course="course"
                    @open-curriculum="openCurriculumModal" @enter-course="handleEnterCourse" />
            </div>
            <div v-else class="text-center py-20 bg-slate-50 rounded-xl border border-dashed border-slate-200">
                <p class="text-slate-500">등록된 강의가 없습니다.</p>
            </div>
        </section>

        <!-- Course Curriculum Modal -->
        <TrainerCourseCurriculumModal :isOpen="isModalOpen" :course="selectedCourse" :sessions="mockSessions"
            @close="closeModal" @register="handleRegister" />
    </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useWorkspaceStore } from '@/stores/workspace';
import { useCourseStore } from '@/stores/course';
import { storeToRefs } from 'pinia';
import type { Course, Session } from '@/types';
import TrainerStatCard from '@/components/Trainer/TrainerWorkspace/TrainerStatCard.vue';
import TrainerDashboardCourseCard from '@/components/Trainer/TrainerWorkspace/TrainerDashboardCourseCard.vue';
import TrainerCourseCurriculumModal from '@/components/Trainer/TrainerWorkspace/TrainerCourseCurriculumModal.vue';

const route = useRoute();
const router = useRouter();
const workspaceStore = useWorkspaceStore();
const courseStore = useCourseStore();

const { workspaceStats } = storeToRefs(workspaceStore);
const { workspaceCourses } = storeToRefs(courseStore);

const searchQuery = ref('');
const sortOption = ref<'newest' | 'deadline' | 'oldest'>('newest');

// Date Filter State
const filterStartDate = ref('');
const filterEndDate = ref('');

// Modal State
const isModalOpen = ref(false);
const selectedCourse = ref<Course>({} as Course);
const mockSessions = ref<Session[]>([]);

const openCurriculumModal = async (courseId: string | number) => {
    const course = workspaceCourses.value.find(c => String(c.course_id) === String(courseId));
    if (course) {
        selectedCourse.value = course;

        // Fetch sessions from API
        const sessions = await courseStore.fetchCourseSessions(courseId);
        mockSessions.value = sessions;

        isModalOpen.value = true;
    }
};

const closeModal = () => {
    isModalOpen.value = false;
};



const handleEnterCourse = (courseId: string | number) => {
    const currentWorkspaceId = Array.isArray(route.params.id) ? route.params.id[0] : route.params.id;
    if (!currentWorkspaceId) return;

    // 강사용 강의실 뷰가 따로 없다면 LEARNER Classroom으로 이동하거나, 
    // 추후 강사용 라우트로 변경 (현재는 기존 로직 복원)
    router.push({
        name: 'workspaceLearnerClassroom',
        params: {
            id: currentWorkspaceId,
            courseId: courseId
        }
    });
};

const handleRegister = () => {
    alert('수강 신청이 완료되었습니다! (Mock Action)');
    closeModal();
};

const sortOptionLabel = computed(() => {
    switch (sortOption.value) {
        case 'newest': return '최신순';
        case 'deadline': return '마감 임박순';
        case 'oldest': return '오래된순';
        default: return '정렬';
    }
});

const filteredCourses = computed(() => {
    let result = [...workspaceCourses.value];

    // Filter by search query
    if (searchQuery.value) {
        const query = searchQuery.value.toLowerCase();
        result = result.filter(course =>
            course.title.toLowerCase().includes(query) ||
            course.description.toLowerCase().includes(query)
        );
    }

    // Filter by date range (last_accessed_at 기준)
    if (filterStartDate.value || filterEndDate.value) {
        result = result.filter(course => {
            if (!course.current_lessons || course.current_lessons === 0) {
                // 수강하지 않은 강의는 날짜 필터에서 제외하지 않음
                return true;
            }

            // last_accessed_at이 없는 경우는 필터링에서 제외
            const accessedAt = (course as any).last_accessed_at;
            if (!accessedAt) return false;

            const courseDate = new Date(accessedAt);

            if (filterStartDate.value) {
                const startDate = new Date(filterStartDate.value);
                if (courseDate < startDate) return false;
            }

            if (filterEndDate.value) {
                const endDate = new Date(filterEndDate.value);
                endDate.setHours(23, 59, 59, 999); // 종료일 포함
                if (courseDate > endDate) return false;
            }

            return true;
        });
    }

    // Sort (Simple implementation)
    switch (sortOption.value) {
        case 'newest':
            result.sort((a, b) => Number(b.course_id) - Number(a.course_id));
            break;
        case 'oldest':
            result.sort((a, b) => Number(a.course_id) - Number(b.course_id));
            break;
        case 'deadline':
            result.sort((a, b) => (a.d_day || 999) - (b.d_day || 999));
            break;
    }

    return result;
});

const setSort = (option: 'newest' | 'deadline' | 'oldest') => {
    sortOption.value = option;
};

const applyDateFilter = () => {
    // 날짜 필터는 이미 filteredCourses에서 자동으로 적용됨
    // 드롭다운을 닫기 위한 처리는 CSS로 이미 구현되어 있음
};

const resetDateFilter = () => {
    filterStartDate.value = '';
    filterEndDate.value = '';
};

const initDashboard = async () => {
    const workspaceId = route.params.id;
    if (workspaceId) {
        const id = Array.isArray(workspaceId) ? workspaceId[0] : workspaceId;

        if (!id) return;

        await workspaceStore.setCurrentWorkspaceById(id);
        await courseStore.fetchCoursesByWorkspace(id);
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