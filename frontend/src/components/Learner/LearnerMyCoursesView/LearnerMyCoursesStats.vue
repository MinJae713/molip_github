<template>
    <div class="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
        <!-- 학습 중인 과정 -->
        <div class="bg-white p-5 rounded-xl border border-slate-200 shadow-sm flex items-center gap-4">
            <div class="size-12 rounded-lg bg-indigo-50 text-indigo-600 flex items-center justify-center flex-none">
                <span class="material-symbols-outlined text-2xl">play_circle</span>
            </div>
            <div>
                <div class="text-sm font-medium text-slate-500">학습 중인 과정</div>
                <div class="text-2xl font-bold text-slate-900">
                    {{ ongoingCount }} <span class="text-sm font-normal text-slate-400">개</span>
                </div>
            </div>
        </div>
        <!-- 수료 완료 -->
        <div class="bg-white p-5 rounded-xl border border-slate-200 shadow-sm flex items-center gap-4">
            <div class="size-12 rounded-lg bg-emerald-50 text-emerald-600 flex items-center justify-center flex-none">
                <span class="material-symbols-outlined text-2xl">verified</span>
            </div>
            <div>
                <div class="text-sm font-medium text-slate-500">수료 완료</div>
                <div class="text-2xl font-bold text-slate-900">
                    {{ completedCount }} <span class="text-sm font-normal text-slate-400">개</span>
                </div>
            </div>
        </div>
        <!-- 평균 출석률 -->
        <div class="bg-white p-5 rounded-xl border border-slate-200 shadow-sm flex items-center gap-4">
            <div class="size-12 rounded-lg bg-cyan-50 text-cyan-600 flex items-center justify-center flex-none">
                <span class="material-symbols-outlined text-2xl">trending_up</span>
            </div>
            <div>
                <div class="text-sm font-medium text-slate-500">이번 달 평균 출석률</div>
                <div class="text-2xl font-bold text-slate-900">
                    {{ attendanceRate }} <span class="text-sm font-normal text-slate-400">%</span>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
interface Props {
    ongoingCount: number;
    completedCount: number;
    attendanceRate: number;
}
const props = defineProps<Props>();
</script>