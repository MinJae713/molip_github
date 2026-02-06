<template>
    <h1>{{ courseStore.courseInfo?.title }}</h1>
    <div class="bg-white border border-slate-200 rounded-2xl p-6 shadow-sm">
        <div class="flex flex-col md:flex-row md:items-start justify-between gap-6">
            <div class="flex-1">
                <div class="flex items-center gap-2 mb-3">
                    <span
                        class="px-2.5 py-1 bg-indigo-50 text-indigo-600 text-[11px] font-bold rounded-full border border-indigo-100 uppercase tracking-wide">{{
                            courseStore.courseInfo?.category }}</span>
                </div>
                <h1 class="text-2xl sm:text-3xl font-bold text-slate-900 mb-2">{{ courseStore.courseInfo?.title }}</h1>
                <p class="text-slate-500 text-sm max-w-2xl leading-relaxed">{{ courseStore.courseInfo?.description }}
                </p>
            </div>
            <div class="w-full md:w-80 bg-slate-50 rounded-xl p-5 border border-slate-100 flex-none">
                <h3 class="font-bold text-slate-900 mb-4 flex items-center gap-2">
                    <span class="material-symbols-outlined text-indigo-600">face</span>
                    담당 강사
                </h3>
                <div class="flex items-start gap-4">
                    <div class="size-12 rounded-full bg-slate-100 flex items-center justify-center text-slate-400">
                        <span class="material-symbols-outlined text-3xl">person</span>
                    </div>
                    <div>
                        <h4 class="font-bold text-slate-900">{{ courseStore.courseInfo?.ownerName || '담당 강사' }}</h4>
                        <p class="text-xs text-indigo-600 font-medium mb-1">{{ courseStore.courseInfo?.category }} > {{
                            courseStore.courseInfo?.subCategory }}</p>
                    </div>
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
