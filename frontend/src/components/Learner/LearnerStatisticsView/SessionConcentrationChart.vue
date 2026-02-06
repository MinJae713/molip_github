<!-- src/components/Learner/LearnerStatisticsView/SessionConcentrationChart.vue -->
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
                    <span class="text-slate-500">나의 집중도</span>
                </div>
                <div class="flex items-center gap-1.5">
                    <span class="size-2.5 rounded-full bg-slate-300"></span>
                    <span class="text-slate-400">전체 평균</span>
                </div>
            </div>
        </div>
        <!-- 차트(SVG) 영역 -->
        <!-- 데이터가 있을 때만 렌더링 -->
        <div v-if="stats" class="flex-1 relative flex items-end pb-12 pr-2 pl-8 min-h-[200px]">
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

                <!-- Case 1: 전체 통계 (막대 그래프) -->
                <g v-if="isAll">
                    <g v-for="(group, i) in barGroups" :key="'bar-gr-' + i">
                        <!-- 전체 평균 막대 -->
                        <rect :x="group.avg.x" :y="group.avg.y" width="14" :height="group.avg.h" fill="#cbd5e1" rx="2"
                            class="cursor-pointer hover:fill-slate-300 transition-colors"
                            @mouseenter="onMouseEnter({ x: group.avg.x + 7, y: group.avg.y, value: group.avg.val }, 'avg')"
                            @mouseleave="onMouseLeave" />
                        <!-- 나의 점수 막대 -->
                        <rect :x="group.my.x" :y="group.my.y" width="14" :height="group.my.h" fill="#a855f7" rx="2"
                            class="cursor-pointer hover:fill-purple-600 transition-colors"
                            @mouseenter="onMouseEnter({ x: group.my.x + 7, y: group.my.y, value: group.my.val }, 'my')"
                            @mouseleave="onMouseLeave" />
                    </g>
                </g>

                <!-- Case 2: 특정 코스 트렌드 (꺾은선 그래프) - 기존 로직 -->
                <g v-else>
                    <!-- 전체 평균선 (점선) -->
                    <path :d="avgPath" fill="none" stroke="#cbd5e1" stroke-dasharray="4,4" stroke-width="2" />
                    <!-- 나의 집중도 곡선 -->
                    <path :d="myPath" fill="none" stroke="#a855f7" stroke-linecap="round" stroke-width="3" />
                    <!-- 1. 평균 점수 포인트 -->
                    <g v-for="(point, index) in avgPoints" :key="'avg-p-' + index">
                        <circle :cx="point.x" :cy="point.y" fill="#fff" r="4" stroke="#cbd5e1" stroke-width="2"
                            class="cursor-pointer hover:r-6 transition-all" @mouseenter="onMouseEnter(point, 'avg')"
                            @mouseleave="onMouseLeave" />
                    </g>
                    <!-- 2. 나의 점수 포인트 -->
                    <g v-for="(point, index) in myPoints" :key="'my-p-' + index">
                        <circle :cx="point.x" :cy="point.y" fill="#fff" r="4" stroke="#a855f7" stroke-width="2"
                            class="cursor-pointer hover:r-6 transition-all" @mouseenter="onMouseEnter(point, 'my')"
                            @mouseleave="onMouseLeave" />
                    </g>
                </g>

                <!-- 3. 공통 호버 툴팁 -->
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
            <div class="absolute bottom-4 w-full flex text-[10px] text-slate-400 font-medium px-1 left-0 pl-8 box-border"
                :class="isAll ? 'justify-around' : 'justify-between'">
                <span v-for="label in stats.labels" :key="label">{{ label }}</span>
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
import type { SessionConcentrationStats } from '@/types';
interface Props {
    title?: string;
    description?: string;
    stats: SessionConcentrationStats | null;
    session_name: string;
    isAll?: boolean;
}
const props = withDefaults(defineProps<Props>(), {
    title: '세션별 평균 집중도',
    description: '데이터 기반 학습 집중도 트렌드',
    stats: null,
    session_name: '',
    isAll: false
});

const VIEWBOX_WIDTH = 400;
const VIEWBOX_HEIGHT = 150;

// [전체 통계용] 막대 그래프 데이터 계산
const barGroups = computed(() => {
    if (!props.isAll || !props.stats || !props.stats.labels) return [];

    const { labels, myScores, avgScores } = props.stats;
    if (!myScores || !avgScores) return [];

    const count = labels.length;
    const groupWidth = VIEWBOX_WIDTH / count;
    const bW = 14; // bar width
    const bG = 4;  // gap between bars

    return labels.map((_, i) => {
        const groupCenterX = i * groupWidth + groupWidth / 2;
        const myScore = myScores[i] ?? 0;
        const avgScore = avgScores[i] ?? 0;

        return {
            my: {
                x: groupCenterX - bW - bG / 2,
                y: VIEWBOX_HEIGHT - (myScore / 100) * VIEWBOX_HEIGHT,
                h: Math.max((myScore / 100) * VIEWBOX_HEIGHT, 2),
                val: myScore
            },
            avg: {
                x: groupCenterX + bG / 2,
                y: VIEWBOX_HEIGHT - (avgScore / 100) * VIEWBOX_HEIGHT,
                h: Math.max((avgScore / 100) * VIEWBOX_HEIGHT, 2),
                val: avgScore
            }
        };
    });
});

// [특정 코스용] 꺾은선 그래프 좌표 계산 로직
const getPoints = (scores: number[]) => {
    if (!scores || scores.length === 0) return [];
    const count = scores.length;
    const step = count > 1 ? VIEWBOX_WIDTH / (count - 1) : 0;
    return scores.map((score, index) => ({
        x: count > 1 ? index * step : VIEWBOX_WIDTH / 2,
        y: VIEWBOX_HEIGHT - (score / 100) * VIEWBOX_HEIGHT,
        value: score
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

        // Clamp Y values to stay within chart bounds (0 to VIEWBOX_HEIGHT)
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
    type: 'my' | 'avg';
}
const hoveredPoint = ref<HoveredPoint | null>(null);
const onMouseEnter = (point: { x: number, y: number, value: number }, type: 'my' | 'avg') => {
    hoveredPoint.value = { ...point, type };
};
const onMouseLeave = () => {
    hoveredPoint.value = null;
};

// Computed Properties
const myPoints = computed(() => getPoints(props.stats?.myScores || []));
const avgPoints = computed(() => getPoints(props.stats?.avgScores || []));

const myPath = computed(() => createPath(myPoints.value));
const avgPath = computed(() => createPath(avgPoints.value));
</script>