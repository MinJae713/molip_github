<template>
    <article
        class="bg-white border border-slate-200 rounded-xl p-5 hover:border-indigo-300 hover:shadow-md transition-all group relative">
        <div class="flex gap-5">
            <div class="flex-1">
                <div class="flex items-center gap-1 mb-2">
                    <span
                        :class="['px-2 py-0.5 text-[10px] font-bold rounded border uppercase tracking-wide flex items-center gap-1', badgeColorClass]">
                        {{ courseStatus }}
                    </span>
                </div>
                <h3 class="font-bold text-slate-900 text-lg mb-1 group-hover:text-indigo-600 transition-colors">
                    {{ course.title }}
                </h3>
                <p class="text-sm text-slate-500 line-clamp-2 mb-4">
                    {{ course.description }}
                </p>
                <div v-if="courseStatus === '신청가능'"
                    class="flex items-center gap-4 text-xs font-medium text-slate-500 pt-2">
                    <span class="flex items-center gap-1"><span
                            class="material-symbols-outlined text-[16px]">play_lesson</span> {{ course.totalLessons }}
                        강의</span>
                </div>
                <div v-else-if="courseStatus === '수강중'" class="space-y-2">
                    <div class="flex justify-between text-xs font-bold">
                        <span :class="progressRate > 0 ? 'text-indigo-600' : 'text-slate-500'">
                            진도율 {{ progressRate }}%
                        </span>
                        <span class="text-slate-400">{{ course.currentLessons }}/{{ course.totalLessons }} 강의</span>
                    </div>
                    <div class="w-full bg-slate-100 rounded-full h-1.5 overflow-hidden">
                        <div class="bg-indigo-500 h-full rounded-full" :style="{ width: `${progressRate}%` } as any">
                        </div>
                    </div>
                </div>
                <div v-else-if="courseStatus === '승인대기'"
                    class="flex items-center gap-4 text-xs font-medium text-slate-500 pt-2">
                    <span class="flex items-center gap-1"><span
                            class="material-symbols-outlined text-[16px]">play_lesson</span> {{ course.totalLessons }}
                        강의</span>
                </div>
            </div>

            <div class="flex flex-col justify-between items-end gap-4 min-w-25">
                <div class="flex flex-col items-end gap-2 relative">
                     <!-- 찜하기 버튼 추가 -->
                    <button @click.prevent.stop="toggleLike" class="transition-colors p-1 group/like absolute -top-1 -right-1"
                        :class="isHeartFilled ? 'text-red-500 hover:text-red-600' : 'text-slate-300 hover:text-indigo-500'"
                        title="찜하기">
                        <span
                            class="material-symbols-outlined text-2xl transition-transform active:scale-95 group-hover/like:scale-110"
                            :style="{ 'font-variation-settings': `'FILL' ${isHeartFilled ? 1 : 0}` } as any">
                            favorite
                        </span>
                    </button>

                    <div class="h-8"></div> <!-- 공간 확보용 -->

                    <button v-if="courseStatus === '수강중'" @click.stop="$emit('enter-course', course.courseId)"
                        class="size-10 rounded-full bg-indigo-50 flex items-center justify-center text-indigo-600 hover:bg-indigo-600 hover:text-white transition-colors cursor-pointer mt-2">
                        <span class="material-symbols-outlined">
                            {{ progressRate > 0 ? 'resume' : 'play_arrow' }}
                        </span>
                    </button>
                </div>
                <div v-if="courseStatus === '신청가능'" class="flex flex-col items-stretch gap-2">
                    <button 
                        @click.stop="handleEnrollClick" 
                        :disabled="isSubmitting"
                        :class="{ 'opacity-50 cursor-not-allowed': isSubmitting }"
                        class="px-4 py-2 bg-indigo-600 hover:bg-indigo-700 text-white text-sm font-bold rounded-lg border border-indigo-600 transition-all flex items-center justify-center gap-1.5 whitespace-nowrap shadow-sm"
                    >
                        <span class="material-symbols-outlined text-[18px]">
                            {{ isSubmitting ? 'sync' : 'how_to_reg' }} 
                        </span>
                        {{ isSubmitting ? '처리 중...' : '수강 신청하기' }}
                    </button>
                    <button @click="$emit('open-curriculum', course.courseId)"
                        class="px-4 py-2 bg-white hover:bg-slate-50 text-indigo-600 text-sm font-bold rounded-lg border border-indigo-200 transition-all flex items-center justify-center gap-1.5 whitespace-nowrap">
                        <span class="material-symbols-outlined text-[18px]">menu_book</span>
                        커리큘럼 확인
                    </button>
                </div>
                <div v-else-if="courseStatus === '승인대기'">
                    <span
                        class="text-xs font-bold text-amber-600/80 flex items-center gap-1 px-2 py-1 bg-amber-50/50 rounded">
                        검토 중...
                    </span>
                </div>
                <button v-else @click="$emit('open-curriculum', course.courseId)"
                    class="px-3 py-1.5 text-xs font-bold text-indigo-600 bg-indigo-50 border border-indigo-100 rounded-lg hover:bg-indigo-100 hover:border-indigo-200 transition-all flex items-center gap-1.5">
                    <span class="material-symbols-outlined text-[16px]">menu_book</span>
                    커리큘럼 확인
                </button>
            </div>
        </div>
    </article>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import type { LearnerCourse } from '@/types';
import { useLearnerCourseStore } from '@/stores/learnerCourse';

const props = defineProps<{
    course: LearnerCourse;
    workspaceId: string | number;
}>();

// --- 상태 및 상수 ---
const isSubmitting = ref(false);
const courseStore = useLearnerCourseStore();

// --- 수강 상태 및 UI 로직 ---
const courseStatus = computed(() => {
    if (props.course.applyStatus === 'APPROVED') return '수강중';
    if (props.course.applyStatus === 'PENDING') return '승인대기';
    return '신청가능';
});

// 찜 상태 판별
const isHeartFilled = computed(() => {
    // 1. props의 zzimId 확인 (만약 백엔드에서 준다면)
    if ((props.course as any).zzimId) return true;
    
    // 2. 스토어의 찜 목록에서 확인
    // 타이틀로 비교 (LearnerMyCourseCard와 로직 통일)
    const currentTitle = props.course.title;
    return courseStore.myLikedCourses.some(
        (liked: any) => liked.title === currentTitle
    );
});

// 찜 토글
const toggleLike = async () => {
    if (!props.workspaceId || !props.course.courseId) return;

    try {
        await courseStore.toggleCourseLike(
            props.workspaceId,
            props.course.courseId,
            isHeartFilled.value
        );
        // 스토어에서 이미 refresh를 수행하지만, 필요하다면 추가 로직 작성
    } catch (error) {
        console.error("찜 토글 실패:", error);
    }
};

const progressRate = computed(() => {
    if (!props.course.totalLessons) return 0;
    // 단순 계산 로직 유지
    return Math.round(((props.course.currentLessons || 0) / props.course.totalLessons) * 100);
});

const badgeColorClass = computed(() => {
    const status = props.course.applyStatus || '';
    const classes: Record<string, string> = {
        APPROVED: 'bg-indigo-50 text-indigo-600 border-indigo-100',
        PENDING: 'bg-amber-50 text-amber-600 border-amber-100',
        REJECTED: 'bg-red-50 text-red-600 border-red-100',
    };
    return classes[status] || 'bg-slate-50 text-slate-500 border-slate-100';
});

// --- 이벤트 및 액션 ---
const emit = defineEmits<{
    (e: 'open-curriculum', courseId: string | number): void;
    (e: 'enroll-course', courseId: string | number): void;
    (e: 'enter-course', courseId: string | number): void;
}>();

const handleEnrollClick = async () => {
    if (isSubmitting.value) return; 
    
    isSubmitting.value = true;
    try {
        await emit('enroll-course', props.course.courseId); 
    } finally {
        isSubmitting.value = false;
    }
};
</script>

<style scoped>
@keyframes shimmer {
    100% {
        transform: translateX(100%);
    }
}
</style>
