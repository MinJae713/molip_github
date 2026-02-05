<script setup lang="ts">
import { computed } from 'vue';

interface Props {
  score?: number;
  status?: string;
  history?: number[];
}

const props = withDefaults(defineProps<Props>(), {
  score: undefined,
  status: undefined,
  history: () => []
});

// 최신 점수 계산
const currentScore = computed(() => {
  if (props.score !== undefined) return props.score;
  const history = props.history;
  if (history && history.length > 0) return Math.round(history[history.length - 1]!);
  return 0;
});

// 상태 계산
const currentStatus = computed(() => {
  if (props.status !== undefined) return props.status;
  const s = currentScore.value;
  if (s >= 80) return '매우 높음';
  if (s >= 60) return '높음';
  if (s >= 40) return '보통';
  return '낮음';
});

// SVG 경로 데이터 생성
const pathData = computed(() => {
  const history = props.history;
  if (!history || history.length < 2) return '';

  const width = 200;
  const height = 60;
  const maxLen = Math.max(history.length, 120);
  const stepX = width / (maxLen - 1);

  const points = history.map((val, i) => {
    const x = i * stepX;
    const y = height - (val / 100 * (height - 10)) - 5;
    return `${i === 0 ? 'M' : 'L'}${x.toFixed(1)},${y.toFixed(1)}`;
  });

  return points.join(' ');
});

// 마지막 포인트 위치 (데코레이션용)
const lastPointPos = computed(() => {
  const history = props.history;
  if (!history || history.length === 0) return { right: '0%', top: '50%' };

  const width = 200;
  const height = 60;
  const maxLen = Math.max(history.length, 120);
  const i = history.length - 1;
  const x = i * (width / (maxLen - 1));
  const val = history[i]!;
  const y = height - (val / 100 * (height - 10)) - 5;

  return {
    left: `${(x / width) * 100}%`,
    top: `${(y / height) * 100}%`
  };
});
</script>

<template>
  <div
    class="bg-white p-7 rounded-[2.5rem] border border-slate-100 shadow-sm flex flex-col h-full group hover:shadow-md transition-shadow">
    <div class="flex items-center justify-between mb-6">
      <div class="flex items-center gap-2">
        <div class="size-8 bg-blue-50 rounded-xl flex items-center justify-center text-blue-600">
          <span class="material-symbols-outlined text-xl font-light">monitoring</span>
        </div>
        <p class="text-[11px] font-black text-slate-400 tracking-widest uppercase">전체 학습 몰입도</p>
      </div>
      <div class="bg-slate-50 px-3 py-1 rounded-full border border-slate-100">
        <p class="text-[10px] font-bold text-slate-500">
          실시간: <span class="text-blue-600">{{ currentScore }}% ({{ currentStatus }})</span>
        </p>
      </div>
    </div>

    <!-- 그래프 영역 -->
    <div class="flex-1 w-full relative min-h-32 mt-2">
      <svg class="w-full h-full" viewBox="0 0 200 60" preserveAspectRatio="none">
        <!-- 배경 가이드 라인 -->
        <line x1="0" y1="10" x2="200" y2="10" stroke="#f1f5f9" stroke-width="0.5" stroke-dasharray="2,2" />
        <line x1="0" y1="30" x2="200" y2="30" stroke="#f1f5f9" stroke-width="0.5" stroke-dasharray="2,2" />
        <line x1="0" y1="50" x2="200" y2="50" stroke="#f1f5f9" stroke-width="0.5" stroke-dasharray="2,2" />

        <!-- 데이터 라인 -->
        <path v-if="pathData" :export="pathData" :d="pathData" fill="none" stroke="url(#blueGradient)" stroke-width="3"
          stroke-linecap="round" stroke-linejoin="round" class="transition-all duration-300" />

        <!-- 데이터가 없을 때 안내 -->
        <text v-if="!pathData" x="100" y="35" text-anchor="middle" class="text-[8px] fill-slate-300 font-bold">
          WAITING FOR ANALYTICS...
        </text>

        <!-- 그라데이션 정의 -->
        <defs>
          <linearGradient id="blueGradient" x1="0%" y1="0%" x2="100%" y2="0%">
            <stop offset="0%" stop-color="#3b82f6" />
            <stop offset="100%" stop-color="#0ea5e9" />
          </linearGradient>
        </defs>
      </svg>

      <!-- 현재 포인트 데코레이션 -->
      <div v-if="props.history.length > 0"
        class="absolute size-3 bg-blue-500 border-2 border-white rounded-full shadow-lg shadow-blue-200 -translate-x-1/2 -translate-y-1/2 transition-all duration-300"
        :style="lastPointPos"></div>
    </div>
  </div>
</template>

<style scoped>
/* 추가적인 미세 애니메이션이 필요하다면 여기에 작성 */
</style>
