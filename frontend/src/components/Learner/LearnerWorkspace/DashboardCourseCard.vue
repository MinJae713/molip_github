<template>
    <article
        class="bg-white border border-slate-200 rounded-xl p-5 hover:border-indigo-300 hover:shadow-md transition-all group relative">
        <div class="flex gap-5">
            <div class="flex-1">
                <div class="flex items-center gap-2 mb-2">
                    <span
                        :class="['px-2 py-0.5 text-[10px] font-bold rounded border uppercase tracking-wide flex items-center gap-1', badgeColorClass]">
                        <span v-if="course.enrollment_status === 'pending_approval'"
                            class="size-1.5 rounded-full bg-amber-500 animate-pulse"></span>
                        {{ course.tag }}
                    </span>

                    <span v-if="course.d_day" class="text-xs font-medium text-slate-400 flex items-center gap-1">
                        <span class="material-symbols-outlined text-[14px]">schedule</span> D-{{ course.d_day }}
                    </span>
                    <span v-else-if="course.sub_category"
                        class="text-xs font-medium text-slate-400 flex items-center gap-1">
                        <span v-if="course.enrollment_status === 'recruiting'"
                            class="material-symbols-outlined text-[14px]">signal_cellular_alt</span>
                        {{ course.sub_category }}
                    </span>
                    <span v-else-if="course.request_date" class="text-xs font-medium text-slate-400">
                        {{ course.request_date }} 요청됨
                    </span>
                </div>

                <h3 class="font-bold text-slate-900 text-lg mb-1 group-hover:text-indigo-600 transition-colors">
                    {{ course.title }}
                </h3>
                <p class="text-sm text-slate-500 line-clamp-2 mb-4">
                    {{ course.description }}
                </p>

                <!-- Status: 모집중인 상태 -->
                <div v-if="course.enrollment_status === 'recruiting'"
                    class="flex items-center gap-4 text-xs font-medium text-slate-500 pt-2">
                    <span class="flex items-center gap-1"><span
                            class="material-symbols-outlined text-[16px]">play_lesson</span> {{ course.total_lessons }}
                        강의</span>
                    <span class="flex items-center gap-1"><span
                            class="material-symbols-outlined text-[16px]">schedule</span> {{ course.duration }}</span>
                </div>

                <!-- Status: 수강중인 상태 -->
                <div v-else-if="course.enrollment_status !== 'pending_approval'" class="space-y-2">
                    <div class="flex justify-between text-xs font-bold">
                        <span :class="course.progress_rate > 0 ? 'text-indigo-600' : 'text-slate-500'">
                            진도율 {{ course.progress_rate }}%
                        </span>
                        <span class="text-slate-400">{{ course.current_lessons }}/{{ course.total_lessons }} 강의</span>
                    </div>
                    <div class="w-full bg-slate-100 rounded-full h-1.5 overflow-hidden">
                        <div class="bg-indigo-500 h-full rounded-full" :style="{ width: `${course.progress_rate}%` }">
                        </div>
                    </div>
                </div>

                <!-- Status: 가입신청 후 승인대기 -->
                <div v-else-if="course.enrollment_status === 'pending_approval'" class="space-y-2">
                    <div class="flex justify-between text-xs font-bold">
                        <span class="text-amber-600">관리자 승인 대기</span>
                    </div>
                    <div class="w-full bg-slate-100 rounded-full h-1.5 overflow-hidden">
                        <div class="bg-slate-200 h-full rounded-full w-full relative overflow-hidden">
                            <div
                                class="absolute inset-0 bg-white/50 w-full h-full -translate-x-full animate-[shimmer_2s_infinite]">
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="flex flex-col justify-between items-end gap-4 min-w-[100px]">
                <div class="flex flex-col items-end gap-2">
                    <button @click.prevent.stop="toggleLike" class="transition-colors p-1 group/like"
                        :class="course.is_liked ? 'text-red-500 hover:text-red-600' : 'text-slate-300 hover:text-indigo-500'"
                        title="찜하기">
                        <span
                            class="material-symbols-outlined text-2xl transition-transform active:scale-95 group-hover/like:scale-110"
                            :style="{ 'font-variation-settings': `'FILL' ${course.is_liked ? 1 : 0}` }">
                            favorite
                        </span>
                    </button>

                    <!-- Icon Circle / Play Button -->
                    <div v-if="course.enrollment_status === 'recruiting'"
                        class="size-10 flex items-center justify-center"></div>
                    <div v-else-if="course.enrollment_status === 'pending_approval'"
                        class="size-10 rounded-full bg-amber-50 flex items-center justify-center text-amber-500 border border-amber-100">
                        <span class="material-symbols-outlined">hourglass_top</span>
                    </div>
                    <button v-else @click.stop="$emit('enter-course', course.course_id)"
                        class="size-10 rounded-full bg-indigo-50 flex items-center justify-center text-indigo-600 hover:bg-indigo-600 hover:text-white transition-colors cursor-pointer">
                        <span class="material-symbols-outlined">
                            {{ course.progress_rate > 0 ? 'resume' : 'play_arrow' }}
                        </span>
                    </button>
                </div>

                <!-- Action Buttons -->
                <div v-if="course.enrollment_status === 'recruiting'" class="flex flex-col items-stretch gap-2">
                    <button @click="$emit('enroll-course', course.course_id)"
                        class="px-4 py-2 bg-indigo-600 hover:bg-indigo-700 text-white text-sm font-bold rounded-lg shadow-sm shadow-indigo-200 transition-all flex items-center justify-center gap-1.5 whitespace-nowrap">
                        <span class="material-symbols-outlined text-[18px]">how_to_reg</span>
                        수강 신청하기
                    </button>
                    <button @click="$emit('open-curriculum', course.course_id)"
                        class="px-4 py-2 bg-white hover:bg-slate-50 text-indigo-600 text-sm font-bold rounded-lg border border-indigo-200 transition-all flex items-center justify-center gap-1.5 whitespace-nowrap">
                        <span class="material-symbols-outlined text-[18px]">menu_book</span>
                        커리큘럼 확인
                    </button>
                </div>
                <div v-else-if="course.enrollment_status === 'pending_approval'">
                    <span
                        class="text-xs font-bold text-amber-600/80 flex items-center gap-1 px-2 py-1 bg-amber-50/50 rounded">
                        검토 중...
                    </span>
                </div>
                <button v-else @click="$emit('open-curriculum', course.course_id)"
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
import type { Course } from '@/types';
import { useLearnerCourseStore } from '@/stores/learnerCourse';

const props = defineProps<{
    course: Course;
    isLiked?: boolean; // Optional prop to override course.is_liked if needed, though we reactively use course object
}>();

defineEmits<{
    (e: 'open-curriculum', courseId: string | number): void;
    (e: 'enroll-course', courseId: string | number): void;
    (e: 'enter-course', courseId: string | number): void;
}>();

const courseStore = useLearnerCourseStore();

const toggleLike = async () => {
    if (props.course.course_id) {
        // props.course.is_liked가 현재 상태이므로, 이를 두 번째 인자로 전달합니다.
        await courseStore.toggleCourseLike(
            Number(props.course.course_id),
            !!props.course.is_liked // 확실하게 boolean으로 변환해서 넘겨주는 것이 좋습니다.
        );
    }
};

const badgeColorClass = computed(() => {
    // tag_color is not in Course type, so cast to any or check if backend provides it
    const color = (props.course as any).tag_color || 'slate';
    const classes: Record<string, string> = {
        red: 'bg-red-50 text-red-600 border-red-100',
        indigo: 'bg-indigo-50 text-indigo-600 border-indigo-100',
        slate: 'bg-slate-100 text-slate-600 border-slate-200',
        amber: 'bg-amber-50 text-amber-600 border-amber-100',
    };
    return classes[color] || classes.slate;
});
</script>

<style scoped>
@keyframes shimmer {
    100% {
        transform: translateX(100%);
    }
}
</style>
