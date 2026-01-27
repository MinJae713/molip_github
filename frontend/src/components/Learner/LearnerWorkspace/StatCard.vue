<template>
    <div class="bg-white rounded-xl p-6 border border-slate-200 shadow-sm flex items-start justify-between">
        <div>
            <p class="text-sm font-medium text-slate-500 mb-1">{{ title }}</p>
            <h3 class="text-3xl font-bold text-slate-900">{{ value }}</h3>
            <div v-if="trend"
                :class="['flex items-center gap-1 mt-2 text-xs font-medium px-2 py-0.5 rounded-full w-fit', trendClass]">
                <span class="material-symbols-outlined text-[14px]">{{ trendIcon }}</span>
                <span>{{ trend }}</span>
            </div>
            <p v-else-if="subText" class="text-xs text-slate-400 mt-2">
                {{ subText }}
            </p>
        </div>
        <div :class="['size-12 rounded-xl flex items-center justify-center', colorClass]">
            <span class="material-symbols-outlined text-2xl">{{ icon }}</span>
        </div>
    </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';

interface Props {
    title: string;
    value: string | number;
    icon: string;
    color?: 'indigo' | 'blue' | 'orange' | 'emerald';
    trend?: string;
    trendIcon?: string;
    trendType?: 'up' | 'down' | 'info';
    subText?: string;
}

const props = withDefaults(defineProps<Props>(), {
    color: 'indigo',
    trendType: 'info'
});

// 테마 색상 매핑
const colorClass = computed(() => {
    const mapping = {
        indigo: 'bg-indigo-50 text-indigo-600',
        blue: 'bg-blue-50 text-blue-600',
        orange: 'bg-orange-50 text-orange-600',
        emerald: 'bg-emerald-50 text-emerald-600'
    };
    return mapping[props.color];
});

// 트렌드 색상 매핑
const trendClass = computed(() => {
    if (props.trendType === 'up') return 'text-emerald-600 bg-emerald-50';
    if (props.trendType === 'down') return 'text-red-600 bg-red-50';
    return 'text-indigo-600 bg-indigo-50';
});
</script>