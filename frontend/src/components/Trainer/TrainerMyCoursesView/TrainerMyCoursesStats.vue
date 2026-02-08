<template>
    <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
        <!-- 학습 중인 과정 -->
        <div class="stat-card group">
            <div class="icon-wrapper bg-indigo-50 text-indigo-600 group-hover:bg-indigo-600 group-hover:text-white">
                <span class="material-symbols-outlined text-2xl">{{ ongoingIcon }}</span>
            </div>
            <div class="content">
                <div class="label text-slate-500">{{ ongoingLabel }}</div>
                <div class="value-wrapper">
                    <span class="value text-slate-900">{{ ongoingCount }}</span>
                    <span class="unit text-slate-400">{{ ongoingUnit }}</span>
                </div>
                <div v-if="ongoingDesc" class="text-xs text-slate-400 mt-1">{{ ongoingDesc }}</div>
            </div>
            <div class="card-glow bg-indigo-400"></div>
        </div>

        <!-- 수료 완료 -->
        <div class="stat-card group">
            <div class="icon-wrapper bg-emerald-50 text-emerald-600 group-hover:bg-emerald-600 group-hover:text-white">
                <span class="material-symbols-outlined text-2xl">{{ completedIcon }}</span>
            </div>
            <div class="content">
                <div class="label text-slate-500">{{ completedLabel }}</div>
                <div class="value-wrapper">
                    <span class="value text-slate-900">{{ completedCount }}</span>
                    <span class="unit text-slate-400">{{ completedUnit }}</span>
                </div>
                <div v-if="completedDesc" class="text-xs text-slate-400 mt-1">{{ completedDesc }}</div>
            </div>
            <div class="card-glow bg-emerald-400"></div>
        </div>

        <!-- 평균 출석률 -->
        <div class="stat-card group">
            <div class="icon-wrapper bg-amber-50 text-amber-600 group-hover:bg-amber-600 group-hover:text-white">
                <span class="material-symbols-outlined text-2xl">{{ attendanceIcon }}</span>
            </div>
            <div class="content">
                <div class="label text-slate-500">{{ attendanceLabel }}</div>
                <div class="value-wrapper">
                    <span class="value text-slate-900">{{ attendanceRate }}</span>
                    <span class="unit text-slate-400">{{ attendanceUnit }}</span>
                </div>
                <div v-if="attendanceDesc" class="text-xs text-slate-400 mt-1">{{ attendanceDesc }}</div>
            </div>
            <div class="card-glow bg-amber-400"></div>
        </div>
    </div>
</template>

<script setup lang="ts">
interface Props {
    ongoingCount: number;
    completedCount: number;
    attendanceRate: number;
    ongoingUnit?: string;
    completedUnit?: string;
    attendanceUnit?: string;
    ongoingIcon?: string;
    completedIcon?: string;
    attendanceIcon?: string;
    ongoingDesc?: string;
    completedDesc?: string;
    attendanceDesc?: string;
    ongoingLabel?: string;
    completedLabel?: string;
    attendanceLabel?: string;
}

withDefaults(defineProps<Props>(), {
    ongoingLabel: '학습 중인 과정',
    completedLabel: '수료 완료',
    attendanceLabel: '이번 달 평균 출석률',
    ongoingUnit: '개',
    completedUnit: '개',
    attendanceUnit: '%',
    ongoingIcon: 'play_circle',
    completedIcon: 'verified',
    attendanceIcon: 'trending_up',
    ongoingDesc: '',
    completedDesc: '',
    attendanceDesc: ''
});
</script>

<style scoped>
.stat-card {
    position: relative;
    background: white;
    padding: 1.5rem;
    border-radius: 1rem;
    border: 1px solid #e2e8f0;
    display: flex;
    align-items: center;
    gap: 1.25rem;
    overflow: hidden;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
}

.stat-card:hover {
    transform: translateY(-4px);
    box-shadow: 0 12px 20px -8px rgba(0, 0, 0, 0.1);
    border-color: transparent;
}

.icon-wrapper {
    width: 3.5rem;
    height: 3.5rem;
    border-radius: 0.75rem;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
    transition: all 0.3s ease;
}

.content {
    display: flex;
    flex-direction: column;
}

.label {
    font-size: 0.875rem;
    font-weight: 600;
    margin-bottom: 0.25rem;
}

.value-wrapper {
    display: flex;
    align-items: baseline;
    gap: 0.25rem;
}

.value {
    font-size: 1.75rem;
    font-weight: 800;
    line-height: 1;
}

.unit {
    font-size: 0.875rem;
    font-weight: 400;
}

.card-glow {
    position: absolute;
    bottom: -20px;
    right: -20px;
    width: 60px;
    height: 60px;
    border-radius: 50%;
    filter: blur(30px);
    opacity: 0;
    transition: opacity 0.3s ease;
    z-index: 0;
}

.stat-card:hover .card-glow {
    opacity: 0.15;
}

.stat-card>* {
    position: relative;
    z-index: 1;
}
</style>
