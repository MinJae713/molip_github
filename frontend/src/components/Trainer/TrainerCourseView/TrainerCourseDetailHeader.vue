<template>
    <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
            <div class="flex items-center gap-2 mb-2 text-sm text-slate-500 font-medium">
                <span>교육과정 목록</span>
                <span class="material-symbols-outlined text-[14px]">chevron_right</span>
                <span class="text-indigo-600 font-bold">과정 관리</span>
            </div>
            <h2 class="text-2xl font-bold text-slate-900 tracking-tight">{{ title }}</h2>
        </div>
        <div class="flex gap-3">

        </div>
    </div>
</template>

<script setup lang="ts">
defineProps<{
    title: string;
}>();
</script>
