<template>
    <div class="min-h-full flex flex-col p-4 items-center">
        <div class="my-auto w-full flex flex-col items-center">
            <!-- <h3 class="text-lg font-bold text-slate-800 mb-2 text-center flex-none"></h3> -->
            <div class="relative w-full aspect-square max-w-[280px] mx-auto flex-none">
                <!-- 범례 (Legend) -->
                <div class="absolute top-0 right-0 flex gap-3 z-10 pointer-events-none">
                    <div class="flex items-center gap-1.5">
                        <div class="w-2.5 h-2.5 bg-emerald-500/30 border border-emerald-600"></div>
                        <span class="text-[10px] text-slate-500">나</span>
                    </div>
                    <div class="flex items-center gap-1.5">
                        <div class="w-2.5 h-2.5 bg-indigo-500/20 border border-indigo-400 border-dashed"></div>
                        <span class="text-[10px] text-slate-500">전체</span>
                    </div>
                </div>
                <svg class="w-full h-full overflow-visible" viewBox="0 0 240 240">
                    <!-- 배경 가이드 라인 (7각형) -->
                    <g class="stroke-slate-200 fill-none" stroke-width="1">
                        <polygon v-for="scale in [0.2, 0.4, 0.6, 0.8, 1]" :key="scale" :points="getPolygonPoints(scale)" />
                    </g>
                    
                    <!-- 축 라인 (중심에서 각 꼭짓점으로) -->
                    <g class="stroke-slate-200" stroke-width="1">
                        <line v-for="(point, index) in getPolygonPointsArray(1)" :key="index" v-bind="point" x1="120" y1="120" />
                    </g>

                    <!-- 데이터 영역 (전체 클래스) -->
                    <polygon :points="getDataPoints(classMoodData)" 
                            class="fill-indigo-500/20 stroke-indigo-400 stroke-dashed" 
                            stroke-width="1.5" stroke-dasharray="4 4" />
                    
                    <!-- 데이터 영역 (나의 무드) -->
                    <polygon :points="getDataPoints(personalMoodData)" 
                            class="fill-emerald-500/30 stroke-emerald-600" 
                            stroke-width="2" />
                    
                    <!-- 데이터 포인트 (나의 무드만 표시하거나 둘 다 표시) -->
                    <circle v-for="(point, index) in getDataPointsArray(personalMoodData)" :key="'p-'+index"
                            :cx="point.x" :cy="point.y" r="3" class="fill-emerald-600" />
                    

                    <!-- 라벨 -->
                    <g v-for="(label, index) in labels" :key="index">
                        <!-- 라벨 좌표 계산 -->
                        <text :x="getLabelPos(index).x" :y="getLabelPos(index).y" 
                            class="text-[11px] fill-slate-500 font-medium"
                            text-anchor="middle" dominant-baseline="middle">
                            {{ label.kor }}
                        </text>
                        <text :x="getLabelPos(index).x" :y="getLabelPos(index).y + 12" 
                            class="text-[10px] fill-slate-400"
                            text-anchor="middle" dominant-baseline="middle">
                            ({{ label.eng }})
                        </text>
                    </g>
                </svg>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
const radius = 65; // 차트 반지름
const centerX = 120; // 중심 X
const centerY = 120; // 중심 Y

const labels = [
    { kor: '행복', eng: 'happy' },
    { kor: '놀람', eng: 'surprise' },
    { kor: '공포', eng: 'fear' },
    { kor: '혐오', eng: 'disgust' },
    { kor: '슬픔', eng: 'sad' },
    { kor: '분노', eng: 'anger' },
    { kor: '중립', eng: 'neutral' },
];

// 예시 데이터 (0~1 사이 값)
const classMoodData = [0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5];
const personalMoodData = [0.8, 0.6, 0.4, 0.3, 0.2, 0.5, 0.7];

// 7각형 꼭짓점 좌표 계산 (문자열 반환)
function getPolygonPoints(scale: number) {
    return getPolygonPointsArray(scale).map(p => `${p.x2},${p.y2}`).join(' ');
}

// 7각형 꼭짓점 좌표 배열 반환
function getPolygonPointsArray(scale: number) {
    const points = [];
    const totalPoints = 7;
    // -Math.PI / 2 : 12시 방향부터 시작
    const startAngle = -Math.PI / 2;

    for (let i = 0; i < totalPoints; i++) {
        const angle = startAngle + (i * 2 * Math.PI) / totalPoints;
        const x = centerX + radius * Math.cos(angle) * scale;
        const y = centerY + radius * Math.sin(angle) * scale;
        points.push({ x2: x, y2: y });
    }
    return points;
}

// 데이터 값에 따른 실제 좌표 계산 (문자열)
function getDataPoints(data: number[]) {
    return getDataPointsArray(data).map(p => `${p.x},${p.y}`).join(' ');
}

// 데이터 값에 따른 실제 좌표 배열
function getDataPointsArray(data: number[]) {
    const points = [];
    const totalPoints = 7;
    const startAngle = -Math.PI / 2;

    for (let i = 0; i < totalPoints; i++) {
        const angle = startAngle + (i * 2 * Math.PI) / totalPoints;
        const value = data[i] || 0;
        const x = centerX + radius * Math.cos(angle) * value;
        const y = centerY + radius * Math.sin(angle) * value;
        points.push({ x, y });
    }
    return points;
}

// 라벨 위치 계산
function getLabelPos(index: number) {
    const totalPoints = 7;
    const startAngle = -Math.PI / 2;
    const angle = startAngle + (index * 2 * Math.PI) / totalPoints;
    const labelRadius = radius + 25; // 차트보다 조금 더 바깥쪽

    return {
        x: centerX + labelRadius * Math.cos(angle),
        y: centerY + labelRadius * Math.sin(angle)
    };
}

</script>