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
            </div>
            <div class="flex flex-col justify-between items-end gap-4 min-w-[100px]">
                <!-- 수강 신청하기 버튼 -->
                <button @click="$emit('open-curriculum', course.courseId)"
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
