<template>
  <div class="space-y-8">
    <!-- Header -->
    <header class="flex flex-col md:flex-row md:items-center justify-between gap-4">
      <div>
        <nav class="flex items-center text-sm text-slate-500 mb-1">
          <span>교육과정</span>
          <span class="material-symbols-outlined text-sm mx-1">chevron_right</span>
          <span>종료된 과정</span>
        </nav>
        <h1 class="text-2xl font-bold text-slate-900">비즈니스 AI 윤리 가이드: 최종 결과 리포트</h1>
      </div>
      <div class="flex items-center gap-3">
      </div>
    </header>
    <!-- Chart Section -->
    <section class="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm">
      <div class="flex items-center justify-between mb-6">
        <h2 class="text-lg font-bold text-slate-900 flex items-center gap-2">
          <span class="material-symbols-outlined text-indigo-500">monitoring</span>
          실시간 강의 집중도 분석
        </h2>
        <div class="flex items-center gap-2">
          <span class="size-2 rounded-full bg-indigo-500"></span>
          <span class="text-xs text-slate-500 font-bold">평균 집중도 (%)</span>
        </div>
      </div>
      <div class="h-64 w-full">
        <Line :data="chartData" :options="chartOptions" />
      </div>
    </section>

    <!-- Student Details Table -->
    <section>
      <div class="flex items-center justify-between mb-4">
        <h2 class="text-lg font-bold text-slate-900 flex items-center gap-2">
          <span class="material-symbols-outlined text-indigo-500">groups</span>
          수강생별 상세 성과
        </h2>
        <div class="flex gap-2">
          <div class="relative">
            <span class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
              <span class="material-symbols-outlined text-slate-400 text-sm">search</span>
            </span>
            <input type="text" placeholder="이름 검색..."
              class="pl-9 pr-4 py-1.5 border border-slate-200 rounded-lg text-sm focus:outline-none focus:border-indigo-500 w-48 transition-colors">
          </div>
          <button class="p-1.5 border border-slate-200 rounded-lg hover:bg-slate-50 text-slate-500">
            <span class="material-symbols-outlined text-lg">filter_list</span>
          </button>
        </div>
      </div>

      <div class="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
        <table class="w-full text-left border-collapse">
          <thead class="bg-slate-50 border-b border-slate-200">
            <tr>
              <th class="px-6 py-3 text-xs font-bold text-slate-500 uppercase">이름</th>
              <th class="px-6 py-3 text-xs font-bold text-slate-500 uppercase">부서</th>
              <th class="px-6 py-3 text-xs font-bold text-slate-500 uppercase">진도율 (%)</th>
              <th class="px-6 py-3 text-xs font-bold text-slate-500 uppercase text-center">평균 집중도</th>
              <th class="px-6 py-3 text-xs font-bold text-slate-500 uppercase text-center">종료 퀴즈 평균</th>
              <th class="px-6 py-3 text-xs font-bold text-slate-500 uppercase text-center">수료 여부</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-slate-100">
            <tr v-for="student in studentData" :key="student.id" class="hover:bg-slate-50 transition-colors">
              <td class="px-6 py-4">
                <div class="flex items-center gap-3">
                  <div
                    class="size-8 rounded-full bg-indigo-100 text-indigo-600 flex items-center justify-center font-bold text-xs">
                    {{ student.name.slice(0, 2) }}
                  </div>
                  <div>
                    <div class="font-bold text-slate-900 text-sm">{{ student.name }}</div>
                    <div class="text-xs text-slate-500">{{ student.email }}</div>
                  </div>
                </div>
              </td>
              <td class="px-6 py-4 text-sm text-slate-600">{{ student.department }}</td>
              <td class="px-6 py-4">
                <div class="flex items-center gap-2">
                  <span class="text-sm font-bold text-slate-900 w-8">{{ student.progress }}%</span>
                  <div class="w-24 h-1.5 bg-slate-100 rounded-full overflow-hidden">
                    <div class="h-full bg-emerald-500" :style="{ width: student.progress + '%' }"></div>
                  </div>
                </div>
              </td>
              <td class="px-6 py-4 text-center">
                <span :class="['inline-block px-2 py-0.5 rounded text-xs font-bold', getFocusClass(student.focus)]">
                  {{ student.focus }}%
                </span>
              </td>
              <td class="px-6 py-4 text-center text-sm font-bold text-slate-900">{{ student.quizScore }}점</td>
              <td class="px-6 py-4 text-center">
                <span class="px-2 py-0.5 bg-indigo-100 text-indigo-700 rounded-full text-xs font-bold">Completed</span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
  Filler
} from 'chart.js';
import { Line } from 'vue-chartjs';

ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
  Filler
);

// --- Chart Data (Mock) ---
const chartData = {
  labels: ['Session 01', 'Session 02', 'Session 03', 'Session 04'],
  datasets: [
    {
      label: '평균 집중도',
      backgroundColor: (ctx: any) => {
        const canvas = ctx.chart.ctx;
        const gradient = canvas.createLinearGradient(0, 0, 0, 300);
        gradient.addColorStop(0, 'rgba(79, 70, 229, 0.2)');
        gradient.addColorStop(1, 'rgba(79, 70, 229, 0)');
        return gradient;
      },
      borderColor: '#4F46E5',
      pointBackgroundColor: '#ffffff',
      pointBorderColor: '#4F46E5',
      pointBorderWidth: 2,
      pointRadius: 6,
      data: [82, 75, 92, 88],
      fill: true,
      tension: 0.4
    }
  ]
};

const chartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: { display: false },
    tooltip: {
      backgroundColor: 'rgba(15, 23, 42, 0.9)',
      padding: 12,
      cornerRadius: 8,
      callbacks: {
        label: (context: any) => `집중도: ${context.parsed.y}%`
      }
    }
  },
  scales: {
    y: {
      min: 0,
      max: 100,
      grid: {
        color: '#f1f5f9',
        borderDash: [5, 5]
      },
      ticks: {
        font: { size: 11 },
        color: '#64748b'
      }
    },
    x: {
      grid: { display: false },
      ticks: {
        font: { size: 12, weight: 'bold' },
        color: '#64748b'
      }
    }
  }
};

// --- Table Data (Mock) ---
const studentData = ref([
  { id: 1, name: '김민지', email: 'minji.kim@techcorp.com', department: '마케팅팀', progress: 100, focus: 88, quizScore: 92 },
  { id: 2, name: '이준호', email: 'junho.lee@techcorp.com', department: '개발팀', progress: 95, focus: 92, quizScore: 88 },
  { id: 3, name: '박성훈', email: 'vark.sh@techcorp.com', department: '인사팀', progress: 80, focus: 65, quizScore: 74 },
  { id: 4, name: '최수진', email: 'sujin.choi@techcorp.com', department: '영업팀', progress: 100, focus: 78, quizScore: 85 },
  { id: 5, name: '정우성', email: 'ws.jung@techcorp.com', department: '디자인팀', progress: 100, focus: 95, quizScore: 98 },
]);

const getFocusClass = (score: number) => {
  if (score >= 80) return 'bg-emerald-100 text-emerald-700';
  if (score >= 60) return 'bg-amber-100 text-amber-700';
  return 'bg-red-100 text-red-700';
};
</script>
