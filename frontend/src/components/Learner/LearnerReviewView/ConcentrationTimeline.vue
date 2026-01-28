<template>
    <div class="h-full flex flex-col bg-white border border-slate-200 rounded-2xl p-6 shadow-sm">
        <!-- 상단 헤더 -->
        <div class="flex items-center justify-between flex-none mb-6">
            <div>
                <h2 class="text-lg font-bold text-slate-900 flex items-center gap-2">
                    <span class="material-symbols-outlined text-indigo-600">monitoring</span>
                    집중도 흐름
                </h2>
                <p class="text-xs text-slate-500 mt-1">세션 진행 시간에 따른 집중도 변화 그래프</p>
            </div>
            <!-- 범례 -->
            <div class="flex items-center gap-4 text-xs">
                <div class="flex items-center gap-1.5">
                    <span class="w-3 h-3 bg-red-50 border border-red-100 dark:border-red-200"></span>
                    <span class="text-slate-600 font-medium">집중 저조(&lt;30%)</span>
                </div>
                <div class="flex items-center gap-1.5">
                    <span class="w-8 h-[2px] bg-indigo-600"></span>
                    <span class="text-slate-600 font-medium">집중도(%)</span>
                </div>
            </div>
        </div>

        <!-- 차트 컨테이너 -->
        <div class="relative flex-1 min-h-[200px] w-full" ref="chartRef" @mousemove="onMouseMove"
            @mouseleave="onMouseLeave">

            <!-- Y축 (점수) -->
            <div
                class="absolute left-0 top-0 bottom-6 w-8 flex flex-col justify-between text-[11px] text-slate-400 font-medium text-right pr-2 select-none">
                <span>100</span>
                <span>80</span>
                <span>60</span>
                <span>40</span>
                <span>20</span>
                <span>0</span>
            </div>

            <!-- 그래프 영역 -->
            <div class="absolute left-10 right-0 top-0 bottom-6">
                <!-- 수평 그리드 라인 (0, 20, 40, 60, 80, 100) -->
                <div class="absolute inset-0 flex flex-col justify-between pointer-events-none z-0">
                    <div class="w-full h-px bg-slate-100"></div>
                    <div class="w-full h-px bg-slate-100"></div>
                    <div class="w-full h-px bg-slate-100"></div>
                    <div class="w-full h-px bg-slate-100"></div>
                    <div class="w-full h-px bg-slate-100"></div>
                    <div class="w-full h-px bg-slate-100"></div>
                </div>

                <!-- 데드존(저조 구간) 표시 -->
                <div class="absolute inset-0 z-0">
                    <div v-for="(zone, i) in deadZones" :key="'dz-' + i"
                        class="absolute top-0 bottom-0 bg-red-50/80 border-x border-red-100 transition-colors hover:bg-red-100/50"
                        :style="{ left: zone.left + '%', width: zone.width + '%' }">
                    </div>
                </div>

                <!-- SVG 차트 -->
                <svg class="absolute inset-0 w-full h-full overflow-visible z-10" preserveAspectRatio="none">
                    <!-- 데이터 라인 -->
                    <path :d="pathD" fill="none" stroke="#4F46E5" stroke-width="2" stroke-linejoin="round"
                        stroke-linecap="round" />

                    <!-- 데이터 포인트 (옵션: 마우스 근처만 보이게 하거나 항상 보이게) -->
                    <!-- 여기서는 깔끔함을 위해 호버 시에만 포인트 표시 -->
                </svg>

                <!-- 인터랙션: 툴팁 및 가이드 -->
                <div v-if="tooltip" class="absolute top-0 bottom-0 pointer-events-none z-20"
                    :style="{ left: tooltip.x + 'px' }">
                    <!-- 수직선 -->
                    <div class="absolute top-0 bottom-0 w-px bg-indigo-400 border-r border-dashed"></div>
                    <!-- 포인트 마커 -->
                    <div class="absolute w-2.5 h-2.5 bg-indigo-600 border-2 border-white rounded-full -translate-x-[4px]"
                        :style="{ top: tooltip.y + 'px' }"></div>
                    <!-- 툴팁 박스 -->
                    <div
                        class="absolute -top-10 left-1/2 -translate-x/2 bg-slate-800 text-white text-xs px-2 py-1.5 rounded shadow-lg whitespace-nowrap z-30">
                        <span class="font-bold">{{ tooltip.time }}</span>
                        <span class="mx-1 text-slate-400">|</span>
                        <span class="text-indigo-300">{{ tooltip.score }}%</span>
                    </div>
                </div>
            </div>

            <!-- X축 (시간) -->
            <div
                class="absolute bottom-0 left-10 right-0 h-6 flex justify-between items-end text-[11px] text-slate-400 font-medium select-none">
                <span>00:00</span>
                <span>{{ formatTime(totalDuration * 0.25) }}</span>
                <span>{{ formatTime(totalDuration * 0.5) }}</span>
                <span>{{ formatTime(totalDuration * 0.75) }}</span>
                <span>{{ formatTime(totalDuration) }}</span>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue';

// props 정의
interface DataPoint {
    minute: number;
    value: number;
}

const props = withDefaults(defineProps<{
    data?: DataPoint[];
    duration?: number; // 분 단위
}>(), {
    data: () => [],
    duration: 60
});

// 샘플 데이터 (데이터가 없을 경우 사용)
const defaultData: DataPoint[] = [
    { minute: 0, value: 50 },
    { minute: 5, value: 55 },
    { minute: 10, value: 70 },
    { minute: 15, value: 85 },
    { minute: 20, value: 60 },
    { minute: 25, value: 45 },
    { minute: 28, value: 20 }, // 데드존 진입
    { minute: 30, value: 15 }, // 저점
    { minute: 35, value: 25 },
    { minute: 38, value: 40 }, // 탈출
    { minute: 40, value: 75 },
    { minute: 45, value: 90 },
    { minute: 50, value: 80 },
    { minute: 55, value: 65 },
    { minute: 60, value: 55 },
];

const chartData = computed(() => {
    return (props.data && props.data.length > 0) ? props.data : defaultData;
});

const totalDuration = computed(() => props.duration || 60);

// 데드존(저조 구간) 자동 감지 로직 (< 30%)
interface DeadZone {
    left: number;  // %
    width: number; // %
}

const deadZones = computed(() => {
    const zones: DeadZone[] = [];
    if (!chartData.value || chartData.value.length === 0) return zones;

    let start: number | null = null;
    const threshold = 30;
    const duration = totalDuration.value;

    // 데이터를 순회하며 구간 추출
    for (let i = 0; i < chartData.value.length; i++) {
        const point = chartData.value[i];
        if (!point) continue;

        const isDead = point.value < threshold;

        if (isDead && start === null) {
            start = point.minute;
        } else if (!isDead && start !== null) {
            // 구간 종료
            zones.push({
                left: (start / duration) * 100,
                width: ((point.minute - start) / duration) * 100
            });
            start = null;
        }
    }
    // 마지막까지 데드존인 경우
    if (start !== null) {
        zones.push({
            left: (start / duration) * 100,
            width: ((duration - start) / duration) * 100
        });
    }

    return zones;
});

// 차트 크기 반응형 감지
const chartRef = ref<HTMLDivElement | null>(null);
const width = ref(0);
const height = ref(0);

const updateSize = () => {
    if (chartRef.value) {
        // padding/axis area 제외하고 실제 그래프 영역 크기 계산
        // left-10 (40px)
        // bottom-6 (24px)
        const rect = chartRef.value.getBoundingClientRect();
        width.value = rect.width - 40;
        height.value = rect.height - 24;
    }
};

onMounted(() => {
    updateSize();
    window.addEventListener('resize', updateSize);
});
onUnmounted(() => {
    window.removeEventListener('resize', updateSize);
});

// SVG Path 생성
const pathD = computed(() => {
    if (width.value === 0 || height.value === 0 || chartData.value.length === 0) return '';

    const points = chartData.value.map(pt => {
        // x: 시간 비율 * 너비
        const x = (pt.minute / totalDuration.value) * width.value;
        // y: (1 - 점수 비율) * 높이
        const y = height.value - (pt.value / 100) * height.value;
        return { x, y };
    });

    // 포인트가 2개 미만이면 패스 생성 불가
    if (points.length < 2) return '';

    const firstPoint = points[0];
    if (!firstPoint) return ''; // 방어 코드

    // 부드러운 곡선 (Catmull-Rom or Cubic Bezier)
    let d = `M ${firstPoint.x} ${firstPoint.y}`;

    for (let i = 0; i < points.length - 1; i++) {
        const p1 = points[i];
        const p2 = points[i + 1];

        if (!p1 || !p2) continue; // 방어 코드

        // p0: 이전 점이 없으면 p1 사용
        const p0 = points[i - 1] || p1;
        // p3: 다다음 점이 없으면 p2 사용
        const p3 = points[i + 2] || p2;

        const cp1x = p1.x + (p2.x - p0.x) * 0.2;
        const cp1y = p1.y + (p2.y - p0.y) * 0.2;
        const cp2x = p2.x - (p3.x - p1.x) * 0.2;
        const cp2y = p2.y - (p3.y - p1.y) * 0.2;

        d += ` C ${cp1x} ${cp1y}, ${cp2x} ${cp2y}, ${p2.x} ${p2.y}`;
    }

    return d;
});

// 툴팁 로직
const tooltip = ref<{ x: number, y: number, time: string, score: number } | null>(null);

const onMouseMove = (e: MouseEvent) => {
    if (!chartRef.value || width.value === 0) return;

    // 마우스 위치 계산 (그래프 영역 기준)
    const rect = chartRef.value.getBoundingClientRect();
    const offsetX = e.clientX - rect.left - 40; // 왼쪽 마진 40px

    if (offsetX < 0 || offsetX > width.value) {
        tooltip.value = null;
        return;
    }

    // 마우스 위치에 해당하는 시간 계산
    const timeAtCursor = (offsetX / width.value) * totalDuration.value;

    // 가장 가까운 데이터 포인트 찾기 (또는 보간)
    // 여기서는 보간(Linear Interpolation) 사용
    const val = interpolateValue(timeAtCursor);

    // Y좌표 계산
    const y = height.value - (val / 100) * height.value;

    tooltip.value = {
        x: offsetX,
        y: y,
        time: formatTime(timeAtCursor),
        score: Math.round(val)
    };
};

const onMouseLeave = () => {
    tooltip.value = null;
};

const interpolateValue = (t: number) => {
    const data = chartData.value;
    if (!data || data.length === 0) return 0;

    const first = data[0];
    const last = data[data.length - 1];

    if (!first || !last) return 0; // 방어 코드

    if (t <= first.minute) return first.value;
    if (t >= last.minute) return last.value;

    for (let i = 0; i < data.length - 1; i++) {
        const p1 = data[i];
        const p2 = data[i + 1];

        if (!p1 || !p2) continue;

        if (t >= p1.minute && t <= p2.minute) {
            const ratio = (t - p1.minute) / (p2.minute - p1.minute);
            return p1.value + (p2.value - p1.value) * ratio;
        }
    }
    return 0;
};

// 유틸리티
function formatTime(totalMinutes: number) {
    const mm = Math.floor(totalMinutes);
    const ss = Math.floor((totalMinutes - mm) * 60);
    return `${mm.toString().padStart(2, '0')}:${ss.toString().padStart(2, '0')}`;
}
</script>