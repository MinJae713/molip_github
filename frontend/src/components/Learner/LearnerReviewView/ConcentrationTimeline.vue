<template>
    <div class="h-full flex flex-col bg-white border border-slate-200 rounded-2xl shadow-sm overflow-hidden">
        <!-- 상단 탭 (네비게이션) -->
        <div class="flex border-b border-slate-100 bg-slate-50/50">
            <button 
                @click="activeTab = 'concentration'"
                class="flex-1 py-3 text-sm font-bold transition-all border-b-2 flex items-center justify-center gap-2"
                :class="activeTab === 'concentration' 
                    ? 'text-indigo-600 border-indigo-600 bg-indigo-50/30' 
                    : 'text-slate-500 border-transparent hover:text-slate-700 hover:bg-slate-50'">
                <span class="material-symbols-outlined text-[18px]">monitoring</span>
                <span>집중도 흐름</span>
            </button>
            <button 
                @click="activeTab = 'emotion'"
                class="flex-1 py-3 text-sm font-bold transition-all border-b-2 flex items-center justify-center gap-2"
                :class="activeTab === 'emotion' 
                    ? 'text-purple-600 border-purple-600 bg-purple-50/30' 
                    : 'text-slate-500 border-transparent hover:text-slate-700 hover:bg-slate-50'">
                <span class="material-symbols-outlined text-[18px]">face</span>
                <span>개인감정 타임라인</span>
            </button>
        </div>

        <div class="flex-1 p-6 overflow-hidden relative">
            <!-- 1. 집중도 흐름 탭 -->
            <div v-if="activeTab === 'concentration'" class="h-full flex flex-col animate-fadeIn">
                <!-- 차트 헤더 (범례 등) -->
                <div class="flex items-center justify-between flex-none mb-6">
                    <div>
                        <p class="text-xs text-slate-500">세션 진행 시간에는 따른 집중도 변화 그래프</p>
                    </div>
                    <!-- 범례 -->
                    <div class="flex items-center gap-3 text-[11px]">
                         <div class="flex items-center gap-1.5">
                            <span class="w-2.5 h-2.5 bg-yellow-100 border border-yellow-200"></span>
                            <span class="text-slate-500">피버(고집중)</span>
                        </div>
                        <div class="flex items-center gap-1.5">
                            <span class="w-2.5 h-2.5 bg-slate-100 border border-slate-200"></span>
                            <span class="text-slate-500">쉬는 시간</span>
                        </div>
                        <div class="flex items-center gap-1.5">
                            <span class="w-2.5 h-2.5 bg-gray-100 border border-gray-200"></span>
                            <span class="text-slate-500">미감지</span>
                        </div>
                        <div class="flex items-center gap-1.5">
                            <span class="w-2.5 h-2.5 bg-red-50 border border-red-100"></span>
                            <span class="text-slate-500">데드존</span>
                        </div>
                        <div class="flex items-center gap-1.5">
                            <span class="w-6 h-[2px] bg-indigo-600"></span>
                            <span class="text-slate-600 font-bold">내 집중도</span>
                        </div>
                    </div>
                </div>

                <!-- 차트 컨테이너 -->
                <div class="relative flex-1 min-h-[200px] w-full" ref="chartRef" @mousemove="onMouseMove" @mouseleave="onMouseLeave">
                    
                    <!-- Y축 (점수) -->
                    <div class="absolute left-0 top-0 bottom-6 w-8 flex flex-col justify-between text-[11px] text-slate-400 font-medium text-right pr-2 select-none">
                        <span>100</span>
                        <span>80</span>
                        <span>60</span>
                        <span>40</span>
                        <span>20</span>
                        <span>0</span>
                    </div>

                    <!-- 그래프 영역 -->
                    <div class="absolute left-10 right-0 top-0 bottom-6">
                        <!-- 수평 그리드 라인 -->
                        <div class="absolute inset-0 flex flex-col justify-between pointer-events-none z-0">
                            <div v-for="i in 6" :key="i" class="w-full h-px bg-slate-100"></div>
                        </div>

                        <!-- Empty Zone (미감지) -->
                         <div class="absolute inset-0 z-0 pointer-events-none">
                            <div v-for="(zone, i) in emptyZones" :key="'em-' + i"
                                class="absolute top-0 bottom-0 bg-gray-100/60 border-x border-gray-200 pattern-diagonal-lines"
                                :style="{ left: zone.left + '%', width: zone.width + '%' }">
                            </div>
                        </div>

                        <!-- 데드존(저조 구간) 표시 -->
                        <div class="absolute inset-0 z-0 pointer-events-none">
                            <div v-for="(zone, i) in deadZones" :key="'dz-' + i"
                                class="absolute top-0 bottom-0 bg-gradient-to-b from-red-100/60 via-red-50/40 to-red-100/60 border-x-2 border-red-300 transition-colors hover:bg-red-100/70 flex flex-col gap-1 items-center justify-center pointer-events-auto group backdrop-blur-[1px]"
                                :style="{ left: zone.left + '%', width: zone.width + '%' }">
                                <div class="absolute top-0 left-1/2 -translate-x-1/2 bg-red-400 text-white text-[9px] font-bold px-1.5 py-0.5 rounded-b-md shadow-sm whitespace-nowrap opacity-70 group-hover:opacity-100 transition-opacity">
                                    ⚠️ Dead Zone
                                </div>
                                <!-- 데드존 이동 버튼 -->
                                <button 
                                    @click.stop="$emit('jump-to-time', (zone.left / 100) * totalDuration * 60)"
                                    class="opacity-0 group-hover:opacity-100 transition-all transform scale-90 group-hover:scale-100 bg-white border border-red-200 text-red-500 rounded-full px-2.5 py-1 shadow-sm flex items-center gap-1 text-[10px] font-bold whitespace-nowrap z-20 hover:bg-red-50"
                                    title="이 구간으로 이동">
                                    <span class="material-symbols-outlined text-[14px]">play_arrow</span>
                                    <span>이동</span>
                                </button>
                                <!-- 데드존 AI 퀴즈 생성 버튼 -->
                                <button 
                                    @click.stop="$emit('create-ai-quiz', (zone.left / 100) * totalDuration * 60)"
                                    class="opacity-0 group-hover:opacity-100 transition-all transform scale-90 group-hover:scale-100 bg-indigo-600 border border-indigo-700 text-white rounded-full px-2.5 py-1 shadow-sm flex items-center gap-1 text-[10px] font-bold whitespace-nowrap z-20 hover:bg-indigo-700"
                                    title="이 구간 기반 AI 퀴즈 생성">
                                    <span class="material-symbols-outlined text-[14px]">psychology</span>
                                    <span>AI 퀴즈</span>
                                </button>
                            </div>
                        </div>

                         <!-- Fever Zone (고집중) - 가장 위에 표시 -->
                         <div class="absolute inset-0 z-0 pointer-events-none">
                            <div v-for="(zone, i) in feverZones" :key="'fv-' + i"
                                class="absolute top-0 bottom-0 bg-gradient-to-b from-yellow-200/50 to-yellow-100/30 border-x-2 border-yellow-400 shadow-[0_0_15px_rgba(250,204,21,0.3)] backdrop-blur-[1px]"
                                :style="{ left: zone.left + '%', width: zone.width + '%' }">
                                <div class="absolute top-0 left-1/2 -translate-x-1/2 bg-yellow-400 text-yellow-900 text-[9px] font-bold px-1.5 py-0.5 rounded-b-md shadow-sm whitespace-nowrap">
                                    🔥 FEVER
                                </div>
                            </div>
                        </div>

                        <!-- SVG 차트 (중간) -->
                        <svg class="absolute inset-0 w-full h-full overflow-visible z-10 pointer-events-none" preserveAspectRatio="none">
                            <!-- 클래스 평균 라인 -->
                            <path v-if="averagePathD" :d="averagePathD" fill="none" stroke="#94a3b8" stroke-width="2" 
                                stroke-dasharray="4 4" stroke-linejoin="round" stroke-linecap="round" class="opacity-60" />
                            <!-- 내 집중도 라인 -->
                            <path :d="pathD" fill="none" stroke="#4F46E5" stroke-width="2" stroke-linejoin="round"
                                stroke-linecap="round" />
                        </svg>

                        <!-- 쉬는 시간(Rest Period) 표시 (오버레이 - 가장 앞) -->
                        <div class="absolute inset-0 z-20 pointer-events-none">
                            <div v-for="(period, i) in restPeriodStyles" :key="'rp-' + i"
                                class="absolute top-0 bottom-0 bg-slate-100/90 border-x border-slate-200 flex items-center justify-center overflow-hidden"
                                :style="period.style">
                            </div>
                        </div>

                        <!-- 툴팁 -->
                        <div v-if="tooltip" class="absolute top-0 bottom-0 pointer-events-none z-30"
                            :style="{ left: tooltip.x + 'px' }">
                            <div class="absolute top-0 bottom-0 w-px bg-indigo-400 border-r border-dashed"></div>
                            <div class="absolute w-2.5 h-2.5 bg-indigo-600 border-2 border-white rounded-full -translate-x-[4px]"
                                :style="{ top: tooltip.y + 'px' }"></div>
                            <div class="absolute -top-10 left-1/2 -translate-x/2 bg-slate-800 text-white text-xs px-2 py-1.5 rounded shadow-lg whitespace-nowrap">
                                <span class="font-bold">{{ tooltip.time }}</span>
                                <span class="mx-1 text-slate-400">|</span>
                                <span class="text-indigo-300">{{ tooltip.score }}%</span>
                            </div>
                        </div>
                    </div>

                    <!-- X축 (시간) -->
                    <div class="absolute bottom-0 left-10 right-0 h-6 flex justify-between items-end text-[11px] text-slate-400 font-medium select-none">
                        <span>00:00</span>
                        <span>{{ formatTime(totalDuration * 0.25) }}</span>
                        <span>{{ formatTime(totalDuration * 0.5) }}</span>
                        <span>{{ formatTime(totalDuration * 0.75) }}</span>
                        <span>{{ formatTime(totalDuration) }}</span>
                    </div>
                </div>
            </div>

            <!-- 2. 개인감정 타임라인 탭 -->
            <div v-else-if="activeTab === 'emotion'" class="h-full animate-fadeIn">
                <EmotionTimeline 
                    :duration="duration"
                    :emotion-data="emotionData"
                />
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue';
import EmotionTimeline from './EmotionTimeline.vue';

// props 정의
interface DataPoint {
    minute: number;
    value: number;
    empty?: boolean;
    fever?: boolean;
}

interface EmotionSegment {
    start: number;
    end: number;
    type: 'happy' | 'surprise' | 'fear' | 'disgust' | 'sad' | 'anger' | 'neutral';
}

const props = withDefaults(defineProps<{
    data?: DataPoint[];
    averageData?: DataPoint[]; // 클래스 평균 데이터
    duration?: number; // 분 단위
    restPeriods?: { start: number, end: number }[]; // 쉬는 시간 (분 단위)
    emotionData?: EmotionSegment[];
    deadzoneThreshold?: number;
}>(), {
    data: () => [],
    averageData: () => [],
    duration: 60,
    restPeriods: () => [],
    emotionData: () => [],
    deadzoneThreshold: 30
});

const activeTab = ref<'concentration' | 'emotion'>('concentration');

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

// 데드존(저조 구간) 자동 감지 로직 (< deadzoneThreshold)
interface DeadZone {
    left: number;  // %
    width: number; // %
}

// 헬퍼 함수: 특정 조건(conditionFn)을 만족하는 연속 구간 추출
const getZones = (conditionFn: (pt: DataPoint) => boolean) => {
    const zones: DeadZone[] = [];
    if (!chartData.value || chartData.value.length === 0) return zones;

    let start: number | null = null;
    const duration = totalDuration.value;

    for (let i = 0; i < chartData.value.length; i++) {
        const point = chartData.value[i];
        if (!point) continue;

        const match = conditionFn(point);

        if (match && start === null) {
            start = point.minute;
        } else if (!match && start !== null) {
            zones.push({
                left: (start / duration) * 100,
                width: ((point.minute - start) / duration) * 100
            });
            start = null;
        }
    }
    if (start !== null) {
        zones.push({
            left: (start / duration) * 100,
            width: ((duration - start) / duration) * 100
        });
    }
    return zones;
};

// 1. Dead Zone (threshold 미만)
const deadZones = computed(() => {
    return getZones((pt) => pt.value < props.deadzoneThreshold);
});

// 2. Fever Zone (fever == true) - 모든 구간 노출
const feverZones = computed(() => {
    return getZones((pt) => !!pt.fever);
});

// 3. Empty Zone (empty == true)
const emptyZones = computed(() => {
    return getZones((pt) => !!pt.empty);
});

// 쉬는 시간 스타일 계산
const restPeriodStyles = computed(() => {
    return props.restPeriods.map(period => {
        const left = (period.start / totalDuration.value) * 100;
        const width = ((period.end - period.start) / totalDuration.value) * 100;
        return {
            style: {
                left: `${left}%`,
                width: `${width}%`
            }
        };
    });
});

defineEmits(['jump-to-time', 'create-ai-quiz']);

// 차트 크기 반응형 감지
const chartRef = ref<HTMLDivElement | null>(null);
const width = ref(0);
const height = ref(0);

const updateSize = () => {
    if (chartRef.value) {
        // padding/axis area 제외하고 실제 그래프 영역 크기 계산
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
        const x = (pt.minute / totalDuration.value) * width.value;
        const y = height.value - (pt.value / 100) * height.value;
        return { x, y };
    });

    if (points.length < 2) return '';

    const firstPoint = points[0];
    if (!firstPoint) return '';

    let d = `M ${firstPoint.x} ${firstPoint.y}`;

    for (let i = 0; i < points.length - 1; i++) {
        const p1 = points[i];
        const p2 = points[i + 1];

        if (!p1 || !p2) continue;

        const p0 = points[i - 1] || p1;
        const p3 = points[i + 2] || p2;

        const cp1x = p1.x + (p2.x - p0.x) * 0.2;
        const cp1y = p1.y + (p2.y - p0.y) * 0.2;
        const cp2x = p2.x - (p3.x - p1.x) * 0.2;
        const cp2y = p2.y - (p3.y - p1.y) * 0.2;

        d += ` C ${cp1x} ${cp1y}, ${cp2x} ${cp2y}, ${p2.x} ${p2.y}`;
    }

    return d;
});

// 평균 데이터 SVG Path 생성
const averagePathD = computed(() => {
    if (width.value === 0 || height.value === 0 || !props.averageData || props.averageData.length === 0) return '';

    const points = props.averageData.map(pt => {
        const x = (pt.minute / totalDuration.value) * width.value;
        const y = height.value - (pt.value / 100) * height.value;
        return { x, y };
    });

    if (points.length < 2) return '';

    const firstPoint = points[0];
    if (!firstPoint) return '';

    let d = `M ${firstPoint.x} ${firstPoint.y}`;

    for (let i = 0; i < points.length - 1; i++) {
        const p1 = points[i];
        const p2 = points[i + 1];

        if (!p1 || !p2) continue;

        const p0 = points[i - 1] || p1;
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

    const rect = chartRef.value.getBoundingClientRect();
    const offsetX = e.clientX - rect.left - 40;

    if (offsetX < 0 || offsetX > width.value) {
        tooltip.value = null;
        return;
    }

    const timeAtCursor = (offsetX / width.value) * totalDuration.value;
    const val = interpolateValue(timeAtCursor);
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

    if (!first || !last) return 0;

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

function formatTime(totalMinutes: number) {
    const mm = Math.floor(totalMinutes);
    const ss = Math.floor((totalMinutes - mm) * 60);
    return `${mm.toString().padStart(2, '0')}:${ss.toString().padStart(2, '0')}`;
}
</script>

<style scoped>
.animate-fadeIn {
    animation: fadeIn 0.3s ease-out;
}
@keyframes fadeIn {
    from { opacity: 0; transform: translateY(5px); }
    to { opacity: 1; transform: translateY(0); }
}
</style>