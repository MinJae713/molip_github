<template>
    <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
        <!-- 교육 기간 -->
        <div class="bg-white p-5 rounded-xl border border-slate-200 shadow-sm flex items-center gap-4">
            <div class="size-12 rounded-lg bg-indigo-50 text-indigo-600 flex items-center justify-center shrink-0">
                <span class="material-symbols-outlined text-2xl">date_range</span>
            </div>
            <div>
                <p class="text-xs font-bold text-slate-500 uppercase tracking-wide">교육 기간</p>
                <p class="text-lg font-bold text-slate-900 mt-0.5">8주 과정</p>
            </div>
        </div>

        <!-- 총 수강생 -->
        <div class="bg-white p-5 rounded-xl border border-slate-200 shadow-sm flex items-center gap-4">
            <div class="size-12 rounded-lg bg-blue-50 text-blue-600 flex items-center justify-center shrink-0">
                <span class="material-symbols-outlined text-2xl">groups</span>
            </div>
            <div>
                <p class="text-xs font-bold text-slate-500 uppercase tracking-wide">총 수강생</p>
                <div class="flex items-end gap-2">
                    <p class="text-lg font-bold text-slate-900 mt-0.5">42명</p>
                    <span class="text-xs text-emerald-600 font-bold mb-1 bg-emerald-50 px-1.5 rounded">신규 신청 2명</span>
                </div>
            </div>
        </div>

        <!-- 수업 상태 -->
        <div class="bg-white p-5 rounded-xl border border-slate-200 shadow-sm flex items-center gap-4">
            <div class="size-12 rounded-lg bg-purple-50 text-purple-600 flex items-center justify-center shrink-0">
                <span class="material-symbols-outlined text-2xl">event_upcoming</span>
            </div>
            <div>
                <p class="text-xs font-bold text-slate-500 uppercase tracking-wide">수업 상태</p>
                <div class="flex items-end gap-2">
                    <p class="text-lg font-bold text-slate-900 mt-0.5">시작 전</p>
                    <span class="text-xs text-indigo-600 font-bold mb-1 bg-indigo-50 px-1.5 rounded">D-3</span>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
// Props can be added here for dynamic data
</script>
