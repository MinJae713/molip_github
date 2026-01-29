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
            <button
                class="px-4 py-2 bg-white border border-slate-300 text-slate-700 rounded-lg text-sm font-bold hover:bg-slate-50 shadow-sm transition-colors">
                강의실 입장
            </button>
            <button
                class="px-4 py-2 bg-indigo-600 hover:bg-indigo-700 text-white rounded-lg text-sm font-bold shadow-sm shadow-indigo-200 transition-colors flex items-center gap-2">
                <span class="material-symbols-outlined text-[18px]">edit</span>
                과정 수정
            </button>
        </div>
    </div>
</template>

<script setup lang="ts">
defineProps<{
    title: string;
}>();
</script>
