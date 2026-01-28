<!-- src/components/Learner/LearnerStatisticsView/WeeklyLearningChart.vue -->
<template>
    <div class="bg-white border border-slate-200 rounded-xl p-6 shadow-sm">
        <h2 class="text-lg font-bold text-slate-900 mb-4">
            요일별 학습 시간 분석<span class="text-purple-600 ml-1"></span>
        </h2>
        <!-- ... 헤더 영역 동일 ... -->
        <div class="h-64 w-full flex items-end justify-between gap-2 px-2">
            <div v-for="day in weeklyData" :key="day.label" class="flex flex-col items-center gap-2 group w-full">

                <div
                    class="w-full max-w-[40px] bg-slate-100 rounded-t-md relative h-48 group-hover:bg-indigo-50 transition-colors flex items-end">
                    <!-- 시간에 비례하여 투명도(불투명도) 조절 -->
                    <div class="w-full rounded-t-md absolute bottom-0 transition-all duration-500 bg-indigo-500" :style="{
                        height: day.percentage + '%',
                        opacity: getOpacity(day.time)
                    }">
                    </div>
                    <div
                        class="opacity-0 group-hover:opacity-100 absolute -top-8 left-1/2 -translate-x-1/2 bg-slate-800 text-white text-[10px] py-1 px-2 rounded pointer-events-none transition-opacity">
                        {{ day.time }}h
                    </div>
                </div>
                <!-- 가장 긴 시간에만 포인트 색상 적용 -->
                <span class="text-xs font-medium"
                    :class="day.time === maxTime && day.time > 0 ? 'font-bold text-indigo-600' : 'text-slate-500'">
                    {{ day.label }}
                </span>
            </div>
        </div>
    </div>
</template>
<script setup lang="ts">
import { computed } from 'vue';
import type { WeeklyStat } from '@/types';
const props = defineProps<{
    weeklyData: WeeklyStat[];
}>();
const maxTime = computed(() => {
    if (!props.weeklyData || props.weeklyData.length === 0) return 0;
    return Math.max(...props.weeklyData.map(d => d.time));
});
// 투명도 계산 함수
// 가장 긴 시간을 1.0(100%), 0시간을 0.2(20%)
const getOpacity = (time: number) => {
    if (maxTime.value === 0) return 0.2;
    const ratio = time / maxTime.value;
    // (비율 * 0.8) + 0.2 => 최소 0.2에서 최대 1.0 사이의 값 반환
    return ratio * 0.8 + 0.2;
};
</script>