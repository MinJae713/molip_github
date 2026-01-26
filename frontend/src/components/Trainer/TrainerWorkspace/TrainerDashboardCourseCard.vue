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

                <div class="flex items-center gap-4 text-xs font-medium text-slate-500 pt-2">
                    <span class="flex items-center gap-1">
                        <span class="material-symbols-outlined text-[16px]">play_lesson</span>
                        {{ course.total_lessons || 0 }} 강의
                    </span>
                    <span class="flex items-center gap-1" v-if="course.duration">
                        <span class="material-symbols-outlined text-[16px]">schedule</span>
                        {{ course.duration }}
                    </span>
                </div>
            </div>

            <div class="flex flex-col justify-end items-end gap-2 min-w-[100px]">
                <!-- Action Buttons -->
                <button v-if="course.status === 'active'" @click.stop="$emit('enter-course', course.course_id)"
                    class="px-3 py-1.5 text-xs font-bold text-white bg-indigo-600 border border-indigo-600 rounded-lg hover:bg-indigo-700 hover:border-indigo-700 transition-all flex items-center gap-1.5 shadow-sm">
                    <span class="material-symbols-outlined text-[16px]">logout</span>
                    강의실 입장
                </button>

                <button @click="$emit('open-curriculum', course.course_id)"
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

const props = defineProps<{
    course: Course;
}>();

defineEmits<{
    (e: 'open-curriculum', courseId: string | number): void;
    (e: 'enter-course', courseId: string | number): void;
}>();

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
/* No styles needed */
</style>
