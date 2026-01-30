<template>
    <div
        class="group bg-white rounded-xl border border-slate-200 shadow-sm hover:shadow-card hover:border-indigo-200 transition-all duration-300 flex flex-col h-full relative overflow-hidden">
        <!-- 상단 강조 바 (모든 코스 공통 그라데이션) -->
        <div class="h-1.5 w-full bg-gradient-to-r from-indigo-500 to-cyan-400">
        </div>
        <div class="p-6 flex-1 flex flex-col">
            <!-- 헤더: 태그 + 더보기 버튼 -->
            <div class="flex justify-between items-start mb-4">
                <span :class="[
                    'px-2.5 py-1 text-[11px] font-bold rounded-full border uppercase tracking-wide',
                    displayCourse('isMandatory')
                        ? 'bg-indigo-50 text-indigo-600 border-indigo-100'
                        : 'bg-slate-100 text-slate-600 border-slate-200'
                ]">
                    {{ displayCourse('tag') }}
                </span>
                <button class="text-slate-400 hover:text-slate-600">
                    <span class="material-symbols-outlined">more_vert</span>
                </button>
            </div>
            <!-- 강의 제목 및 설명 -->
            <div class="mb-2">
                <span class="text-xs font-bold text-slate-400 block mb-1">{{ displayCourse('category') }}</span>
                <h3 class="text-xl font-bold text-slate-900 leading-snug group-hover:text-indigo-600 transition-colors">
                    {{ displayCourse('title') }}
                </h3>
            </div>
            <p class="text-sm text-slate-500 line-clamp-2 mb-6">{{ displayCourse('description') }}</p>
            <!-- 하단 영역 -->
            <div class="mt-auto">
                <!-- 진도율 -->
                <div class="mb-5">
                    <div class="flex justify-between items-end mb-2">
                        <span class="text-xs font-bold text-slate-700">진도율</span>
                        <span class="text-lg font-bold text-indigo-600">{{ displayCourse('progressRate') || 0
                        }}%</span>
                    </div>
                    <div class="w-full bg-slate-100 rounded-full h-2 overflow-hidden">
                        <div class="bg-indigo-600 h-full rounded-full"
                            :style="{ width: `${displayCourse('progressRate') || 0}%` }">
                        </div>
                    </div>
                    <div class="flex justify-between mt-2 text-xs text-slate-500">
                        <span>{{ displayCourse('currentLessons') || 0 }}/{{ displayCourse('totalLessons') || 0 }} 세션
                            완료</span>
                        <span v-if="displayCourse('duration')">{{ displayCourse('duration') }}</span>
                        <span v-else-if="displayCourse('dDay')">D-{{ displayCourse('dDay') }}</span>
                    </div>
                </div>
                <!-- 다음 일정 -->
                <div v-if="displayCourse('nextSchedule')"
                    class="bg-slate-50 rounded-lg p-3 border border-slate-100 mb-5 flex items-start gap-3">
                    <div :class="[
                        'mt-0.5',
                        displayCourse('nextSchedule').type === 'live' ? 'text-red-500 animate-pulse' :
                            displayCourse('nextSchedule').type === 'vod' ? 'text-indigo-500' : 'text-emerald-500'
                    ]">
                        <span class="material-symbols-outlined text-[18px]">
                            {{ displayCourse('nextSchedule').icon || 'event' }}
                        </span>
                    </div>
                    <div>
                        <div class="text-xs font-bold text-slate-900">{{ displayCourse('nextSchedule').title }}</div>
                        <div class="text-xs text-slate-500 mt-0.5">{{ displayCourse('nextSchedule').time }}</div>
                    </div>
                </div>
                <!-- 상태별 버튼 -->
                <button v-if="displayCourse('enrollmentStatus') === 'pending_approval'"
                    class="w-full py-2.5 text-sm font-bold rounded-lg bg-gray-200 text-gray-900 border border-gray-300 cursor-not-allowed flex items-center justify-center gap-2"
                    disabled>
                    승인 대기중
                    <span class="material-symbols-outlined text-[18px]">hourglass_empty</span>
                </button>

                <button v-else-if="displayCourse('enrollmentStatus') === 'recruiting'"
                    class="w-full py-2.5 text-sm font-bold rounded-lg bg-emerald-600 text-white border border-emerald-500 hover:bg-emerald-700 shadow-md hover:shadow-lg transition-all flex items-center justify-center gap-2"
                    @click.prevent="$emit('enroll-course', displayCourse('courseId'))">
                    수강신청하기
                    <span class="material-symbols-outlined text-[18px]">add_circle</span>
                </button>

                <button v-else-if="displayCourse('enrollmentStatus') === 'active'"
                    class="w-full py-2.5 text-sm font-bold rounded-lg bg-indigo-600 text-white border border-indigo-600 hover:bg-indigo-700 shadow-md hover:shadow-lg transition-all flex items-center justify-center gap-2"
                    @click.prevent="$emit('enter-course', displayCourse('courseId'))">
                    강의실 입장
                    <span class="material-symbols-outlined text-[18px]">login</span>
                </button>
            </div>
        </div>
    </div>
</template>
<script setup lang="ts">
import { ref, onMounted } from 'vue';
import type { Course } from '@/types';
import { useLearnerCourseStore } from '@/stores/learnerCourse';

interface Props {
    course: Course;
}
const props = defineProps<Props>();
defineEmits<{
    (e: 'enter-course', courseId: string | number): void;
    (e: 'enroll-course', courseId: string | number): void;
}>();

const courseStore = useLearnerCourseStore();
const nowCouse = ref<any>(null);

onMounted(async () => {
    if (props.course.courseId) {
        // 코스 번호로 상세 정보 가져오기
        const detailedData = await courseStore.fetchCourseInfo(props.course.courseId);
        if (detailedData) {
            nowCouse.value = detailedData;
        }
    }
});

/**
 * 표시할 데이터를 결정하는 헬퍼 (패치된 데이터 우선, 아니면 props 데이터)
 */
const displayCourse = (field: string) => {
    if (nowCouse.value && nowCouse.value[field] !== undefined) {
        return nowCouse.value[field];
    }
    return (props.course as any)[field];
};

</script>
<style scoped>
.progress-bar-striped {
    background-image: linear-gradient(45deg, rgba(255, 255, 255, .15) 25%, transparent 25%, transparent 50%, rgba(255, 255, 255, .15) 50%, rgba(255, 255, 255, .15) 75%, transparent 75%, transparent);
    background-size: 1rem 1rem;
}
</style>