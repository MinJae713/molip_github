<template>
    <h1>{{ courseStore.courseInfo?.title }}</h1>
    <div class="bg-white border border-slate-200 rounded-2xl p-6 shadow-sm">
        <div class="flex flex-col md:flex-row md:items-start justify-between gap-6">
            <div class="flex-1">
                <div class="flex items-center gap-2 mb-3">
                    <span
                        class="px-2.5 py-1 bg-indigo-50 text-indigo-600 text-[11px] font-bold rounded-full border border-indigo-100 uppercase tracking-wide">{{
                            courseStore.courseInfo?.tag }}</span>
                </div>
                <h1 class="text-2xl sm:text-3xl font-bold text-slate-900 mb-2">{{ courseStore.courseInfo?.title }}</h1>
                <p class="text-slate-500 text-sm max-w-2xl leading-relaxed">{{ courseStore.courseInfo?.description }}
                </p>
            </div>
            <div class="w-full md:w-80 bg-slate-50 rounded-xl p-5 border border-slate-100 flex-none">
                <div class="flex justify-between items-center mb-3">
                    <span class="text-sm font-bold text-slate-700">나의 학습 현황</span>
                    <!-- 다 완료했으면 검은 배경에 흰색 점-->
                    <div v-if="courseStore.courseInfo?.progress_rate === 100">
                        <span
                            class="px-2 py-0.5 bg-slate-900 text-white text-xs font-bold rounded-full flex items-center gap-1">
                            <span class="size-1.5 rounded-full bg-white"></span>
                            완료
                        </span>
                    </div>
                    <!-- 덜 끝났다면 초록색-->
                    <div v-else>
                        <span
                            class="px-2 py-0.5 bg-emerald-100 text-emerald-700 text-xs font-bold rounded-full flex items-center gap-1">
                            <span class="size-1.5 rounded-full bg-emerald-500"></span>
                            학습 중
                        </span>
                    </div>
                </div>
                <div class="flex items-end gap-1 mb-2">
                    <span class="text-3xl font-bold text-indigo-600">{{ courseStore.courseInfo?.progress_rate }}<span
                            class="text-lg text-slate-400 font-normal">%</span></span>
                </div>
                <div class="w-full bg-slate-200 rounded-full h-2.5 overflow-hidden">
                    <div class="bg-indigo-600 h-full rounded-full transition-all duration-1000 ease-out"
                        :style="`width: ${courseStore.courseInfo?.progress_rate || 0}%`"></div>
                </div>
                <div class="flex justify-between mt-3 text-xs text-slate-500 font-medium">
                    <span>{{ courseStore.courseInfo?.current_lessons }} / {{ courseStore.courseInfo?.total_lessons }} 세션
                        완료</span>
                </div>
            </div>
        </div>
    </div>
</template>
<script setup lang="ts">
import { useLearnerCourseStore } from '@/stores/learnerCourse';
import { couldStartTrivia } from 'typescript';

const courseStore = useLearnerCourseStore()

// Variables for dynamic data can be added here
</script>
