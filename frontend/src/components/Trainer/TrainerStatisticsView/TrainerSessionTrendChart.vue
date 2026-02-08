<!-- src/components/Trainer/TrainerStatisticsView/TrainerSessionTrendChart.vue -->
<template>
    <div class="bg-white border border-slate-200 rounded-xl p-6 shadow-sm flex flex-col h-full">
        <!-- 헤더 영역 -->
        <div class="flex items-center justify-between mb-6">
            <div>
                <h2 class="text-lg font-bold text-slate-900">{{ title }}</h2>
                <p class="text-xs text-slate-500">{{ description }}</p>
            </div>
            <div class="flex items-center gap-4 text-xs">
                <div class="flex items-center gap-1.5">
                    <span class="size-2.5 rounded-full bg-purple-500"></span>
                    <span class="text-slate-500">평균 집중도</span>
                </div>
            </div>
        </div>
        <!-- 차트(SVG) 영역 -->
        <!-- 데이터가 있을 때만 렌더링 -->
        <div v-if="stats && stats.length > 0" class="flex-1 relative flex items-end pb-12 pr-2 pl-8 min-h-[200px]">
            <svg class="w-full h-48 overflow-visible" preserveAspectRatio="none" viewBox="0 0 400 150">

                <!-- Y축 라벨 (0%, 50%, 100%) -->
                <g class="text-[10px] fill-slate-400 font-medium select-none" style="text-anchor: end">
                    <text x="-10" y="5">100%</text>
                    <text x="-10" y="80">50%</text>
                    <text x="-10" y="155">0%</text>
                </g>
                <!-- 가이드 라인 (0%, 50%, 100%) -->
                <line stroke="#f1f5f9" stroke-width="1" x1="0" x2="400" y1="150" y2="150"></line> <!-- 0% -->
                <line stroke="#f1f5f9" stroke-width="1" x1="0" x2="400" y1="75" y2="75"></line> <!-- 50% -->
                <line stroke="#f1f5f9" stroke-width="1" x1="0" x2="400" y1="0" y2="0"></line> <!-- 100% -->

                <!-- 트렌드 (꺾은선 그래프) -->
                <g>
                    <!-- 집중도 곡선 -->
                    <path :d="trendPath" fill="none" stroke="#a855f7" stroke-linecap="round" stroke-width="3" />
                    <!-- 점수 포인트 -->
                    <g v-for="(point, index) in points" :key="'p-' + index">
                        <circle :cx="point.x" :cy="point.y" fill="#fff" r="4" stroke="#a855f7" stroke-width="2"
                            class="cursor-pointer hover:r-6 transition-all" @mouseenter="onMouseEnter(point)"
                            @mouseleave="onMouseLeave" />
                    </g>
                </g>

                <!-- 공통 호버 툴팁 -->
                <g v-if="hoveredPoint" :transform="`translate(${hoveredPoint.x}, ${hoveredPoint.y - 35})`"
                    class="pointer-events-none transition-opacity duration-200">
                    <rect fill="#1e293b" height="28" rx="6" width="46" x="-23" y="-14" filter="url(#shadow)" />
                    <text fill="white" font-size="11" font-weight="bold" text-anchor="middle" x="0" y="4">
                        {{ Math.round(hoveredPoint.value) }}%
                    </text>
                    <path d="M-5,14 L0,19 L5,14" fill="#1e293b" />
                </g>

                <!-- 그림자 필터 정의 -->
                <defs>
                    <filter id="shadow" x="-20%" y="-20%" width="140%" height="140%">
                        <feDropShadow dx="0" dy="2" stdDeviation="2" flood-opacity="0.3" />
                    </filter>
                </defs>
            </svg>
            <!-- X축 라벨 (Flex로 균등 분배) -->
            <div
                class="absolute bottom-4 w-full flex text-[10px] text-slate-400 font-medium px-1 left-0 pl-8 box-border justify-between">
                <span v-for="session in stats" :key="session.sessionId">{{ session.sessionName || session.title || `Session
                    ${session.sessionId}` }}</span>
            </div>
        </div>
        <!-- 데이터 로딩 중이거나 없을 때 -->
        <div v-else class="flex-1 flex items-center justify-center text-slate-400 text-sm min-h-[200px]">
            <span class="material-symbols-outlined mr-2 animate-spin">refresh</span>
            데이터를 불러오는 중입니다...
        </div>
    </div>
</template>
<script setup lang="ts">
import { computed, ref } from 'vue';

interface SessionAnalysis {
    sessionId: number;
    sessionName?: string;
    title?: string;
    avgAttandance: number;
    avgConcentrate: number;
    avgQuizCorrect: number;
}

interface Props {
    title?: string;
    description?: string;
    stats: SessionAnalysis[] | undefined;
}
const props = withDefaults(defineProps<Props>(), {
    title: '세션별 평균 집중도',
    description: '전체 수강생의 세션별 평균 집중도 변화',
    stats: () => []
});

const VIEWBOX_WIDTH = 400;
const VIEWBOX_HEIGHT = 150;

// 꺾은선 그래프 좌표 계산 로직
const getPoints = (data: SessionAnalysis[]) => {
    if (!data || data.length === 0) return [];
    const count = data.length;
    const step = count > 1 ? VIEWBOX_WIDTH / (count - 1) : 0;
    return data.map((item, index) => ({
        x: count > 1 ? index * step : VIEWBOX_WIDTH / 2,
        y: VIEWBOX_HEIGHT - (item.avgConcentrate / 100) * VIEWBOX_HEIGHT,
        value: item.avgConcentrate
    }));
};

const createPath = (points: { x: number; y: number }[]) => {
    if (!points || points.length === 0) return '';
    const startPoint = points[0];
    if (!startPoint) return '';
    if (points.length === 1) return `M${startPoint.x},${startPoint.y}`;
    let d = `M${startPoint.x},${startPoint.y}`;
    const tension = 0.2;
    for (let i = 0; i < points.length - 1; i++) {
        const p1 = points[i];
        const p2 = points[i + 1];
        const p0 = points[i > 0 ? i - 1 : 0] || p1;
        const p3 = points[i < points.length - 2 ? i + 2 : i + 1] || p2;

        if (!p1 || !p2 || !p0 || !p3) continue;
        const cp1x = p1.x + (p2.x - p0.x) * tension;
        let cp1y = p1.y + (p2.y - p0.y) * tension;
        const cp2x = p2.x - (p3.x - p1.x) * tension;
        let cp2y = p2.y - (p3.y - p1.y) * tension;

        // Clamp Y values
        cp1y = Math.max(0, Math.min(VIEWBOX_HEIGHT, cp1y));
        cp2y = Math.max(0, Math.min(VIEWBOX_HEIGHT, cp2y));

        d += ` C${cp1x.toFixed(1)},${cp1y.toFixed(1)} ${cp2x.toFixed(1)},${cp2y.toFixed(1)} ${p2.x.toFixed(1)},${p2.y.toFixed(1)}`;
    }
    return d;
};

// 인터랙션 상태 관리
interface HoveredPoint {
    x: number;
    y: number;
    value: number;
}
const hoveredPoint = ref<HoveredPoint | null>(null);
const onMouseEnter = (point: { x: number, y: number, value: number }) => {
    hoveredPoint.value = point;
};
const onMouseLeave = () => {
    hoveredPoint.value = null;
};

// Computed Properties
const points = computed(() => getPoints(props.stats || []));
const trendPath = computed(() => createPath(points.value));
</script>
