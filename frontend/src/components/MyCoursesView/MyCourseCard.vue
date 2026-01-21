<template>
    <div
        class="group bg-white rounded-xl border border-slate-200 shadow-sm hover:shadow-card hover:border-indigo-200 transition-all duration-300 flex flex-col h-full relative overflow-hidden">
        <!-- 상단 강조 바 (필수 과정만) -->
        <div v-if="course.is_mandatory" class="h-1.5 w-full bg-gradient-to-r from-indigo-500 to-cyan-400"></div>
        <div v-else class="h-1.5 w-full bg-slate-200"></div>
        <div class="p-6 flex-1 flex flex-col">
            <!-- 헤더: 태그 + 더보기 버튼 -->
            <div class="flex justify-between items-start mb-4">
                <span :class="[
                    'px-2.5 py-1 text-[11px] font-bold rounded-full border uppercase tracking-wide',
                    course.is_mandatory
                        ? 'bg-indigo-50 text-indigo-600 border-indigo-100'
                        : 'bg-slate-100 text-slate-600 border-slate-200'
                ]">
                    {{ course.tag }}
                </span>
                <button class="text-slate-400 hover:text-slate-600">
                    <span class="material-symbols-outlined">more_vert</span>
                </button>
            </div>
            <!-- 강의 제목 및 설명 -->
            <div class="mb-2">
                <span class="text-xs font-bold text-slate-400 block mb-1">{{ course.category }}</span>
                <h3 class="text-xl font-bold text-slate-900 leading-snug group-hover:text-indigo-600 transition-colors">
                    {{ course.title }}
                </h3>
            </div>
            <p class="text-sm text-slate-500 line-clamp-2 mb-6">{{ course.description }}</p>
            <!-- 하단 영역 -->
            <div class="mt-auto">
                <!-- 진도율 -->
                <div class="mb-5">
                    <div class="flex justify-between items-end mb-2">
                        <span class="text-xs font-bold text-slate-700">진도율</span>
                        <span class="text-lg font-bold text-indigo-600">{{ course.progress }}%</span>
                    </div>
                    <div class="w-full bg-slate-100 rounded-full h-2 overflow-hidden">
                        <div class="bg-indigo-600 h-full rounded-full"
                            :class="{ 'progress-bar-striped animate-pulse': course.has_live_session }"
                            :style="{ width: `${course.progress}%` }">
                        </div>
                    </div>
                    <div class="flex justify-between mt-2 text-xs text-slate-500">
                        <span>{{ course.currentLessons }}/{{ course.totalLessons }} 세션 완료</span>
                        <span>잔여 {{ course.remaining_hours }}시간</span>
                    </div>
                </div>
                <!-- 다음 일정 -->
                <div class="bg-slate-50 rounded-lg p-3 border border-slate-100 mb-5 flex items-start gap-3">
                    <div :class="[
                        'mt-0.5',
                        course.next_schedule.type === 'live' ? 'text-red-500 animate-pulse' :
                            course.next_schedule.type === 'assignment' ? 'text-indigo-500' :
                                'text-emerald-500'
                    ]">
                        <span class="material-symbols-outlined text-[18px]">
                            {{ course.next_schedule.icon }}
                        </span>
                    </div>
                    <div>
                        <div class="text-xs font-bold text-slate-900">{{ course.next_schedule.title }}</div>
                        <div class="text-xs text-slate-500 mt-0.5">{{ course.next_schedule.time }}</div>
                    </div>
                </div>
                <!-- 입장 버튼 -->
                <a :class="[
                    'w-full py-2.5 text-sm font-bold rounded-lg transition-all flex items-center justify-center gap-2',
                    course.progress > 80
                        ? 'bg-indigo-600 text-white hover:bg-indigo-700 shadow-md hover:shadow-lg'
                        : course.has_live_session
                            ? 'bg-white border border-indigo-600 text-indigo-600 hover:bg-indigo-600 hover:text-white group-hover:shadow-md'
                            : 'bg-white border border-slate-300 text-slate-700 hover:border-indigo-600 hover:text-indigo-600'
                ]" href="#" @click.prevent="$emit('enter-course', course.id)">
                    강의실 입장
                    <span class="material-symbols-outlined text-[18px]">
                        {{ course.progress > 80 ? 'login' : 'arrow_forward' }}
                    </span>
                </a>
            </div>
        </div>
    </div>
</template>
<script setup lang="ts">
import type { Course } from '@/types';

interface Props {
    course: Course;
}
const props = defineProps<Props>();
defineEmits<{
    (e: 'enter-course', courseId: string | number): void;
}>();
</script>
<style scoped>
.progress-bar-striped {
    background-image: linear-gradient(45deg, rgba(255, 255, 255, .15) 25%, transparent 25%, transparent 50%, rgba(255, 255, 255, .15) 50%, rgba(255, 255, 255, .15) 75%, transparent 75%, transparent);
    background-size: 1rem 1rem;
}
</style>