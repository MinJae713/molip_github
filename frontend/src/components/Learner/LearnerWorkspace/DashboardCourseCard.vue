<template>
    <article
        class="bg-white border border-slate-200 rounded-xl p-5 hover:border-indigo-300 hover:shadow-md transition-all group relative">
        <div class="flex gap-5">
            <div class="flex-1">
                <!-- 맨 윗줄 강의 상태 태그-->
                <!-- 수강신청 된 강의면 수강중, 아니면 강의 상태(모집중, 준비중, 끝남)-->
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
                <!-- 신청가능이면 몇강짜린지 표시-->
                <div v-if="courseStatus === '신청가능'"
                    class="flex items-center gap-4 text-xs font-medium text-slate-500 pt-2">
                    <span class="flex items-center gap-1"><span
                            class="material-symbols-outlined text-[16px]">play_lesson</span> {{ course.totalLessons }}
                        강의</span>
                </div>
                <!-- 수강중이면 진도율 표기 -->
                <div v-else-if="courseStatus === '수강중'" class="space-y-2">
                    <div class="flex justify-between text-xs font-bold">
                        <span :class="progressRate > 0 ? 'text-indigo-600' : 'text-slate-500'">
                            진도율 {{ progressRate }}%
                        </span>
                        <span class="text-slate-400">{{ course.currentLessons }}/{{ course.totalLessons }} 강의</span>
                    </div>
                    <div class="w-full bg-slate-100 rounded-full h-1.5 overflow-hidden">
                        <div class="bg-indigo-500 h-full rounded-full" :style="{ width: `${progressRate}%` }">
                        </div>
                    </div>
                </div>
                <!-- 승인대기 -->
                <div v-else-if="courseStatus === '승인대기'"
                    class="flex items-center gap-4 text-xs font-medium text-slate-500 pt-2">
                    <span class="flex items-center gap-1"><span
                            class="material-symbols-outlined text-[16px]">play_lesson</span> {{ course.totalLessons }}
                        강의</span>
                </div>
            </div>

            <div class="flex flex-col justify-between items-end gap-4 min-w-[100px]">
                <!-- 찜하기 버튼 -->
                <div class="flex flex-col items-end gap-2">
                    <button @click.prevent.stop="toggleLike" class="transition-colors p-1 group/like"
                        :class="course.isLiked ? 'text-red-500 hover:text-red-600' : 'text-slate-300 hover:text-indigo-500'"
                        title="찜하기">
                        <span
                            class="material-symbols-outlined text-2xl transition-transform active:scale-95 group-hover/like:scale-110"
                            :style="{ 'font-variation-settings': `'FILL' ${course.isLiked ? 1 : 0}` }">
                            favorite
                        </span>
                    </button>
                    <!-- 강의실 들어가기 버튼 -->
                    <button v-if="courseStatus === '수강중'" @click.stop="$emit('enter-course', course.courseId)"
                        class="size-10 rounded-full bg-indigo-50 flex items-center justify-center text-indigo-600 hover:bg-indigo-600 hover:text-white transition-colors cursor-pointer">
                        <span class="material-symbols-outlined">
                            {{ progressRate > 0 ? 'resume' : 'play_arrow' }}
                        </span>
                    </button>
                </div>
                <!-- 수강 신청하기 버튼 -->
                <div v-if="courseStatus === '신청가능'" class="flex flex-col items-stretch gap-2">
                    <button @click="$emit('enroll-course', course.courseId)"
                        class="px-4 py-2 bg-indigo-600 hover:bg-indigo-700 text-white text-sm font-bold rounded-lg shadow-sm shadow-indigo-200 transition-all flex items-center justify-center gap-1.5 whitespace-nowrap">
                        <span class="material-symbols-outlined text-[18px]">how_to_reg</span>
                        수강 신청하기
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
import { computed } from 'vue';
import type { LearnerCourse } from '@/types';
import { useLearnerCourseStore } from '@/stores/learnerCourse';

const props = defineProps<{
    course: LearnerCourse;
    isLiked?: boolean; // Optional prop to override course.is_liked if needed, though we reactively use course object
}>();

// 수강 상태 관리
const courseStatus = computed(() => {
    if (props.course.applyStatus === 'APPROVED') {
        return '수강중';
    } else if (props.course.applyStatus === 'PENDING') {
        return '승인대기';
    } else {
        return '신청가능';
    }
});

// 프로그래스 바
const progressRate = computed(() => {
    return (props.course.currentLessons + 1) / props.course.totalLessons * 100;
});

defineEmits<{
    (e: 'open-curriculum', courseId: string | number): void;
    (e: 'enroll-course', courseId: string | number): void;
    (e: 'enter-course', courseId: string | number): void;
}>();

const courseStore = useLearnerCourseStore();

const toggleLike = async () => {
    if (props.course.courseId) {
        // props.course.is_liked가 현재 상태이므로, 이를 두 번째 인자로 전달합니다.
        await courseStore.toggleCourseLike(
            Number(props.course.courseId),
            !!props.course.isLiked // 확실하게 boolean으로 변환해서 넘겨주는 것이 좋습니다.
        );
    }
};
const badgeColorClass = computed(() => {
    // 승인 상태 따라서 컬러 지정
    const color = (props.course as any).applyStatus;
    const classes: Record<string, string> = {
        APPROVED: 'bg-indigo-50 text-indigo-600 border-indigo-100',
        PENDING: 'bg-amber-50 text-amber-600 border-amber-100',
        REJECTED: 'bg-red-50 text-red-600 border-red-100',
    };
    return classes[color]
});


</script>

<style scoped>
@keyframes shimmer {
    100% {
        transform: translateX(100%);
    }
}
</style>
