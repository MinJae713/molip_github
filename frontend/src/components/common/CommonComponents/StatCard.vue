<template>
    <div class="bg-white rounded-xl p-6 border border-slate-200 shadow-sm flex items-center gap-4">

        <div class="size-12 rounded-full flex items-center justify-center flex-none"
             :class="[data.iconBgClass || 'bg-indigo-50', data.iconColorClass || 'text-indigo-600']">
            <span class="material-symbols-outlined text-2xl">{{ data.icon }}</span>
        </div>

        <div class="flex-1">
            <p class="text-sm font-medium text-slate-500 mb-1">{{ data.title }}</p>
            <div class="flex items-baseline gap-1">
                <h3 class="text-2xl font-bold text-slate-900">{{ data.value }}</h3>
                <span class="text-sm font-medium text-slate-500">{{ data.unit }}</span>
            </div>
        </div>

    </div>
</template>
<script setup lang="ts">
interface CardData {
    icon: string;
    title: string;
    unit: string;
    value: string | number;
    iconBgClass?: string;
    iconColorClass?: string;
}
const props = defineProps<{
    data: CardData;
}>();
</script>