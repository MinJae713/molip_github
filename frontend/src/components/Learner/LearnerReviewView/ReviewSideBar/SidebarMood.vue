<template>
    <div class="h-full flex flex-col p-5 overflow-y-auto custom-scrollbar">
        <h3 class="text-lg font-bold text-slate-800 mb-6 text-center">전체 클래스 분위기 분석</h3>
        <div class="relative w-full aspect-square max-w-[280px] mx-auto mb-6">
            <svg class="w-full h-full overflow-visible" viewBox="0 0 200 200">
                <g class="stroke-slate-200 fill-none" stroke-width="1">
                    <!-- 20%, 40%, 60%, 80%, 100% 가이드 -->
                    <polygon v-for="scale in [0.2, 0.4, 0.6, 0.8, 1]" :key="scale" :points="getHexagonPoints(scale)" />
                </g>
            </svg>

        </div>
    </div>
</template>

<script setup lang="ts">

function getHexagonPoints(scale: number) {
    const radius = 50;
    const points = [];
    for (let i = 0; i < 6; i++) {
        const angle = (i * Math.PI) / 3;
        const x = radius * Math.cos(angle) * scale;
        const y = radius * Math.sin(angle) * scale;
        points.push(`${x},${y}`);
    }
    return points.join(' ');
}

</script>