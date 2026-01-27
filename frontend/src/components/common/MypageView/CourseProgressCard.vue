<template>
    <article class="glass-panel overflow-hidden flex flex-col group hover:shadow-lg transition-shadow h-full">
        <div :class="['h-32 relative p-4 flex flex-col justify-end bg-gradient-to-r', styles.gradient]">
            <span v-if="course.tag"
                class="absolute top-3 right-3 bg-white/20 backdrop-blur text-white text-[10px] font-bold px-2 py-0.5 rounded">
                {{ course.tag }}
            </span>
            <h4 class="text-white font-bold text-lg leading-tight">{{ course.title }}</h4>
        </div>

        <div class="p-5 flex flex-col flex-1">
            <div class="mb-4">
                <div class="flex justify-between text-xs mb-1.5 font-bold">
                    <span :class="styles.textAccent">{{ course.progress }}% 진행중</span>
                    <span class="text-slate-400">{{ course.currentLessons }}/{{ course.totalLessons }} 강의</span>
                </div>
                <div class="w-full bg-slate-100 h-2 rounded-full overflow-hidden">
                    <div :class="['h-full rounded-full transition-all duration-700 ease-out', styles.barColor]"
                        :style="{ width: `${clampedProgress}%` }"></div>
                </div>
            </div>

            <p class="text-xs text-slate-500 mb-4 line-clamp-2">
                {{ course.description }}
            </p>

            <button @click="$emit('resume')" :class="[
                'mt-auto w-full py-2 border font-bold text-sm rounded-lg transition-colors flex items-center justify-center gap-1',
                styles.button
            ]">
                <span class="material-symbols-outlined text-sm">play_arrow</span>
                이어 듣기
            </button>
        </div>
    </article>
</template>

<script setup lang="ts">
import { computed } from 'vue';

import type { Course } from '@/types';

interface Props {
    course: Course;
}

const props = defineProps<Props>();

defineEmits<{
    (e: 'resume'): void;
}>();

const clampedProgress = computed(() => Math.min(100, Math.max(0, props.course.progress)));

const styles = computed(() => {
    const themes = {
        indigo: {
            gradient: 'from-indigo-500 to-purple-600',
            textAccent: 'text-indigo-600',
            barColor: 'bg-indigo-500',
            button: 'border-indigo-200 text-indigo-600 hover:bg-indigo-50'
        },
        blue: {
            gradient: 'from-blue-500 to-cyan-500',
            textAccent: 'text-blue-600',
            barColor: 'bg-blue-500',
            button: 'border-blue-200 text-blue-600 hover:bg-blue-50'
        },
        slate: {
            gradient: 'from-slate-600 to-slate-800',
            textAccent: 'text-slate-600',
            barColor: 'bg-slate-500',
            button: 'border-slate-200 text-slate-600 hover:bg-slate-50'
        },
        amber: {
            gradient: 'from-amber-500 to-orange-500',
            textAccent: 'text-amber-600',
            barColor: 'bg-amber-500',
            button: 'border-amber-200 text-amber-600 hover:bg-amber-50'
        }
    };
    return themes[props.course.theme || 'indigo'] || themes.indigo;
});
</script>