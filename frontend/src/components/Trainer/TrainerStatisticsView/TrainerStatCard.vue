<template>
    <div
        class="bg-white rounded-xl border border-slate-200 p-6 shadow-sm flex items-start gap-4 h-full relative overflow-hidden transition-all hover:shadow-md hover:border-indigo-100">
        <!-- 배경 장식 (옵션) -->
        <div class="absolute -right-4 -bottom-4 opacity-5">
            <span class="material-symbols-outlined text-[100px]">{{ data.icon }}</span>
        </div>

        <div class="size-12 rounded-xl flex items-center justify-center shrink-0 shadow-sm"
            :class="[data.iconBgClass || 'bg-indigo-50', data.iconColorClass || 'text-indigo-600']">
            <span class="material-symbols-outlined text-2xl">{{ data.icon }}</span>
        </div>
        <div class="flex-1 min-w-0 z-10">
            <p class="text-sm font-medium text-slate-500 mb-1 flex items-center gap-1">{{ data.title }}</p>
            <div class="flex items-baseline gap-1">
                <span class="text-2xl font-bold text-slate-900 tracking-tight">{{ data.value }}</span>
                <span v-if="data.unit" class="text-sm font-semibold text-slate-400">{{ data.unit }}</span>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import type { StatItem } from '@/types';

defineProps<{
    data: StatItem;
}>();
</script>
