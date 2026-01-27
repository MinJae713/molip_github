<!-- src/components/Learner/LearnerStatisticsView/SessionConcentrationChart.vue -->
<template>
    <div class="bg-white border border-slate-200 rounded-xl p-6 shadow-sm flex flex-col h-full">
        <!-- 헤더 영역 -->
        <div class="flex items-center justify-between mb-6">
            <div>
                <h2 class="text-lg font-bold text-slate-900">{{ title }} - {{ session_name }}</h2>
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
        <div v-if="stats" class="flex-1 relative flex items-end pb-10 pr-2 pl-8 min-h-[200px]">
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
                <!-- 전체 평균선 (점선) -->
                <path :d="avgPath" fill="none" stroke="#cbd5e1" stroke-dasharray="4,4" stroke-width="2" />
                <!-- 나의 집중도 곡선 -->
                <path :d="myPath" fill="none" stroke="#a855f7" stroke-linecap="round" stroke-width="3" />
                <!-- 1. 평균 점수 포인트 (투명도 조절하여 덜 강조) -->
                <g v-for="(point, index) in avgPoints" :key="'avg-p-' + index">
                    <circle :cx="point.x" :cy="point.y" fill="#fff" r="4" stroke="#cbd5e1" stroke-width="2"
                        class="cursor-pointer hover:r-6 transition-all" @mouseenter="onMouseEnter(point, 'avg')"
                        @mouseleave="onMouseLeave" />
                </g>

                <!-- 2. 나의 점수 포인트 (강조) -->
                <g v-for="(point, index) in myPoints" :key="'my-p-' + index">
                    <circle :cx="point.x" :cy="point.y" fill="#fff" r="4" stroke="#a855f7" stroke-width="2"
                        class="cursor-pointer hover:r-6 transition-all" @mouseenter="onMouseEnter(point, 'my')"
                        @mouseleave="onMouseLeave" />
                </g>

                <!-- 3. 동적 호버 툴팁 -->
                <g v-if="hoveredPoint" :transform="`translate(${hoveredPoint.x}, ${hoveredPoint.y - 35})`"
                    class="pointer-events-none transition-opacity duration-200">
                    <!-- 말풍선 배경 -->
                    <rect fill="#1e293b" height="28" rx="6" width="46" x="-23" y="-14" filter="url(#shadow)" />
                    <!-- 점수 텍스트 -->
                    <text fill="white" font-size="11" font-weight="bold" text-anchor="middle" x="0" y="4">
                        {{ Math.round(hoveredPoint.value) }}%
                    </text>
                    <!-- 말풍선 꼬리 -->
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
                class="absolute bottom-4 w-full flex justify-between text-xs text-slate-400 font-medium px-1 left-0 pl-8 box-border">
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
import { computed } from 'vue';
import type { SessionConcentrationStats } from '@/types';
interface Props {
    title?: string;
    description?: string;
    stats: SessionConcentrationStats | null; // 부모로부터 데이터 받음
    session_name: string;
}
const props = withDefaults(defineProps<Props>(), {
    title: '세션별 평균 집중도',
    description: '데이터 기반 학습 집중도 트렌드',
    stats: null,
    session_name: ''
});
// ViewBox: 0 0 400 150 (가로 400, 세로 150)
// Y축: 점수 100점 -> y=0, 0점 -> y=150 (반전됨 주의)
// SVG 좌표 계산 로직
// ViewBox: 0 0 400 150 (가로 400, 세로 150)
const VIEWBOX_WIDTH = 400;
const VIEWBOX_HEIGHT = 150;

// 점수 배열을 {x, y, value} 좌표 배열로 변환하는 함수
const getPoints = (scores: number[]) => {
    if (!scores || scores.length === 0) return [];

    const count = scores.length;
    const step = count > 1 ? VIEWBOX_WIDTH / (count - 1) : 0;
    return scores.map((score, index) => ({
        x: count > 1 ? index * step : VIEWBOX_WIDTH / 2,
        y: VIEWBOX_HEIGHT - (score / 100) * VIEWBOX_HEIGHT,
        value: score // 점수 값 포함
    }));
};

// 곡선 생성 함수
const createPath = (points: { x: number; y: number }[]) => {
    if (!points || points.length === 0) return '';
    const startPoint = points[0];
    if (!startPoint) return ''; // 방어 코드

    if (points.length === 1) return `M${startPoint.x},${startPoint.y}`; // 점 하나일 때
    let d = `M${startPoint.x},${startPoint.y}`;
    // 곡선의 둥근 정도 (0.1 ~ 0.5 추천, 0이면 직선)
    // 0.2 정도가 너무 과하지 않고 적당히 부드러움
    const tension = 0.2;
    for (let i = 0; i < points.length - 1; i++) {
        const p1 = points[i];
        const p2 = points[i + 1];

        if (!p1 || !p2) continue;

        // p0: 이전 점이 없으면(인덱스 0일 때) p1 사용, 배열 접근 결과가 undefined일 경우 대비 fallback
        const p0 = points[i > 0 ? i - 1 : 0] || p1;

        // p3: 다다음 점이 없으면 p2 사용, 배열 접근 결과가 undefined일 경우 대비 fallback
        const p3 = points[i < points.length - 2 ? i + 2 : i + 1] || p2;

        // 제어점 1 (cp1): p1에서 출발하는 기울기
        const cp1x = p1.x + (p2.x - p0.x) * tension;
        const cp1y = p1.y + (p2.y - p0.y) * tension;
        // 제어점 2 (cp2): p2로 들어오는 기울기
        const cp2x = p2.x - (p3.x - p1.x) * tension;
        const cp2y = p2.y - (p3.y - p1.y) * tension;
        // 3차 베지어 곡선 (C cp1x,cp1y cp2x,cp2y endx,endy)
        // 소수점 1자리까지만 써서 path 길이 최적화
        d += ` C${cp1x.toFixed(1)},${cp1y.toFixed(1)} ${cp2x.toFixed(1)},${cp2y.toFixed(1)} ${p2.x.toFixed(1)},${p2.y.toFixed(1)}`;
    }
    return d;
};

// 인터랙션 상태 관리
import { ref } from 'vue';

interface HoveredPoint {
    x: number;
    y: number;
    value: number;
    type: 'my' | 'avg'; // '나의 집중도' or '전체 평균'
}

const hoveredPoint = ref<HoveredPoint | null>(null);

const onMouseEnter = (point: { x: number, y: number, value: number }, type: 'my' | 'avg') => {
    hoveredPoint.value = { ...point, type };
};

const onMouseLeave = () => {
    hoveredPoint.value = null;
};

// Computed Properties
const myPoints = computed(() => getPoints(props.stats?.my_scores || []));
const avgPoints = computed(() => getPoints(props.stats?.avg_scores || []));
const myPath = computed(() => createPath(myPoints.value));
const avgPath = computed(() => createPath(avgPoints.value));
</script>