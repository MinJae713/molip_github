<template>
    <header
        class="h-24 border-b-2 border-slate-100 flex-none px-8 flex items-center justify-between bg-white shadow-sm">
        <div class="flex items-center gap-6">
            <div
                class="size-12 bg-indigo-600 rounded-xl flex items-center justify-center text-white shadow-indigo-100 shadow-lg">
                <span class="material-symbols-outlined text-3xl">auto_stories</span>
            </div>

            <div class="flex flex-col gap-1">
                <h1 class="text-2xl font-black text-slate-900 tracking-tight">{{ title || '학습 콘텐츠' }}</h1>

                <div class="flex items-center gap-3 text-xs text-slate-500 font-bold uppercase tracking-widest">
                    <span class="px-2 py-0.5 bg-indigo-50 text-indigo-600 rounded">Review Mode</span>
                </div>
            </div>
        </div>

    </header>
</template>
<script setup lang="ts">
defineProps<{
    title?: string | null;
    course_id?: string | number;
}>();
</script>