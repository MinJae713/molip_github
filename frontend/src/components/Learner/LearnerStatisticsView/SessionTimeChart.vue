<!-- src/components/Learner/LearnerStatisticsView/SessionTimeChart.vue -->
<template>
    <div class="bg-white border border-slate-200 rounded-xl p-6 shadow-sm">
        <h2 class="text-lg font-bold text-slate-900 mb-4">
            세션별 학습 시간<span class="text-purple-600 ml-1"></span>
        </h2>
        <!-- ... 헤더 영역 동일 ... -->
        <div v-if="!isAllZero" class="h-72 w-full flex items-end justify-between gap-2 px-2 overflow-x-auto pb-2">
            <div v-for="item in stats" :key="item.label" class="flex flex-col items-center gap-2 group min-w-[40px] w-full">

                <div
                    class="w-full max-w-[40px] bg-slate-100 rounded-t-lg relative h-48 flex items-end group-hover:bg-indigo-50 transition-colors">
                    <!-- 시간에 비례하여 투명도(불투명도) 조절 -->
                    <div class="w-full rounded-t-md absolute bottom-0 transition-all duration-500 bg-indigo-500" :style="{
                        height: getBarHeight(item.time) + '%',
                        opacity: getOpacity(item.time)
                    }">
                    </div>
                    <div
                        class="opacity-0 group-hover:opacity-100 absolute -top-10 left-1/2 -translate-x-1/2 bg-slate-800 text-white text-[11px] font-bold py-1.5 px-3 rounded-lg shadow-lg pointer-events-none transition-all duration-200 transform translate-y-2 group-hover:translate-y-0 whitespace-nowrap z-30">
                        {{ item.time }}분
                        <!-- 화살표 -->
                        <div class="absolute -bottom-1 left-1/2 -translate-x-1/2 border-4 border-transparent border-t-slate-800"></div>
                    </div>
                </div>
                <!-- 가장 긴 시간에만 포인트 색상 적용 -->
                <span class="text-[10px] leading-tight text-center w-full line-clamp-2 h-8 flex items-center justify-center break-words px-1"
                    :class="item.time === maxTime && item.time > 0 ? 'font-bold text-indigo-600' : 'text-slate-500'">
                    {{ item.label }}
                </span>
            </div>
        </div>
        <!-- 데이터 없음 (Empty State) -->
        <div v-else class="h-72 w-full flex flex-col items-center justify-center text-slate-400">
            <span class="material-symbols-outlined text-4xl mb-2 text-slate-300">history_toggle_off</span>
            <p class="text-sm font-medium">아직 학습 내역이 없습니다.</p>
        </div>
    </div>
</template>
<script setup lang="ts">
import { computed } from 'vue';
import type { WeeklyStat } from '@/types';
const props = defineProps<{
    stats: WeeklyStat[];
}>();
const maxTime = computed(() => {
    if (!props.stats || props.stats.length === 0) return 0;
    return Math.max(...props.stats.map(d => d.time));
});

const isAllZero = computed(() => {
    if (!props.stats || props.stats.length === 0) return true;
    return props.stats.every(item => item.time === 0);
});

const getBarHeight = (time: number) => {
     if (maxTime.value === 0) return 0;
     return (time / maxTime.value) * 100;
}

// 투명도 계산 함수
// 가장 긴 시간을 1.0(100%), 0시간을 0.2(20%)
const getOpacity = (time: number) => {
    if (maxTime.value === 0) return 0.2;
    const ratio = time / maxTime.value;
    // (비율 * 0.8) + 0.2 => 최소 0.2에서 최대 1.0 사이의 값 반환
    return ratio * 0.8 + 0.2;
};
</script>