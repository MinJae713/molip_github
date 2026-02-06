<template>
    <div class="space-y-6">
        <!-- 상단 통계 카드 3개 -->
        <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
            <!-- 평균 출석률 -->
            <div class="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm">
                <div class="flex items-center gap-3 mb-2">
                    <div class="size-10 rounded-full bg-emerald-100 flex items-center justify-center text-emerald-600">
                        <span class="material-symbols-outlined">check_circle</span>
                    </div>
                    <span class="text-slate-500 font-bold text-sm">평균 출석률</span>
                    <span class="ml-auto text-xs text-slate-400">전체 세션 기준</span>
                </div>
                <div class="flex items-end gap-3 mt-4">
                    <span class="text-4xl font-bold text-slate-900">{{ (courseStatistics?.avgAttandance || 0).toFixed(1)
                        }}%</span>
                </div>
            </div>

            <!-- 평균 집중도 -->
            <div class="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm">
                <div class="flex items-center gap-3 mb-2">
                    <div class="size-10 rounded-full bg-amber-100 flex items-center justify-center text-amber-600">
                        <span class="material-symbols-outlined">psychology</span>
                    </div>
                    <span class="text-slate-500 font-bold text-sm">평균 집중도 점수</span>
                    <span class="ml-auto text-xs text-slate-400">전체 세션 기준</span>
                </div>
                <div class="flex items-end gap-3 mt-4">
                    <span class="text-4xl font-bold text-slate-900">{{ (courseStatistics?.avgConcentrate ||
                        0).toFixed(1) }}<span class="text-xl ml-1">점</span></span>
                </div>
            </div>

            <!-- 퀴즈 달성률 -->
            <div class="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm">
                <div class="flex items-center gap-3 mb-2">
                    <div class="size-10 rounded-full bg-indigo-100 flex items-center justify-center text-indigo-600">
                        <span class="material-symbols-outlined">quiz</span>
                    </div>
                    <span class="text-slate-500 font-bold text-sm">퀴즈 달성률</span>
                    <span class="ml-auto text-xs text-slate-400">지난주 대비</span>
                </div>
                <div class="flex items-end gap-3 mt-4">
                    <span class="text-4xl font-bold text-slate-900">{{ (courseStatistics?.avgQuizCorrect ||
                        0).toFixed(1) }}%</span>
                </div>
            </div>
        </div>

        <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
            <!-- 세션별 평균 집중도 추이 (차트) -->
            <div class="lg:col-span-2 bg-white p-6 rounded-2xl border border-slate-200 shadow-sm">
                <div class="flex items-center justify-between mb-6">
                    <h3 class="font-bold text-slate-900 text-lg">세션별 평균 집중도 추이</h3>
                </div>
                <div class="h-80 w-full relative">
                    <!-- Chart.js Mockup -->
                    <Line :data="chartData" :options="chartOptions" />
                </div>
            </div>

            <!-- 참여 우수 수강생 (랭킹) -->
            <div class="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm flex flex-col">
                <div class="flex items-center justify-between mb-6">
                    <h3 class="font-bold text-slate-900 text-lg">참여 우수 수강생</h3>
                    <span class="px-2 py-0.5 bg-indigo-50 text-indigo-600 text-xs font-bold rounded">Top 3</span>
                </div>

                <div class="flex-1 overflow-y-auto pr-1 custom-scrollbar">
                    <div class="border-b border-slate-100 flex text-xs font-bold text-slate-400 pb-2 mb-2">
                        <span class="w-10 text-center">순위</span>
                        <span class="flex-1">이름</span>
                        <span class="w-12 text-right">점수</span>
                    </div>

                    <div class="space-y-5">
                        <div v-for="(student, index) in topStudents" :key="student.id" class="flex items-center py-2">
                            <div class="w-10 flex justify-center">
                                <span class="size-6 rounded flex items-center justify-center text-xs font-bold"
                                    :class="index === 0 ? 'bg-amber-100 text-amber-600' : index === 1 ? 'bg-slate-100 text-slate-600' : index === 2 ? 'bg-orange-50 text-orange-600' : 'text-slate-400'">
                                    {{ index + 1 }}
                                </span>
                            </div>
                            <div class="flex-1 flex items-center gap-3">
                                <div
                                    class="size-9 rounded-full bg-slate-100 text-slate-500 font-bold flex items-center justify-center text-xs">
                                    {{ student.initial }}
                                </div>
                                <div>
                                    <div class="text-sm font-bold text-slate-900">{{ student.name }}</div>
                                    <div class="text-[11px] text-slate-400 flex items-center gap-1">
                                        <span v-if="index === 0"
                                            class="text-amber-500 material-symbols-outlined text-[10px]">bolt</span>
                                        {{ student.badge }}
                                    </div>
                                </div>
                            </div>
                            <div class="w-12 text-right font-bold text-slate-900 text-sm">
                                {{ student.score }}점
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue';
import { useRoute } from 'vue-router';
import { useTrainerCourseStore } from '@/stores/trainerCourse';
import { storeToRefs } from 'pinia';
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

const route = useRoute();
const trainerCourseStore = useTrainerCourseStore();
const { courseStatistics } = storeToRefs(trainerCourseStore);

const courseId = computed(() => route.params.courseId);

onMounted(async () => {
    if (courseId.value) {
        await trainerCourseStore.fetchCourseStats(courseId.value as string);
    }
});

// 차트 데이터 (세션별 집중도 + 출석률)
const chartData = computed(() => {
    if (!courseStatistics.value?.sessionAnalysis) return { labels: [], datasets: [] };

    const sessions = courseStatistics.value.sessionAnalysis;
    console.log("Session Analysis Data:", sessions); // Debugging log
    const labels = sessions.map(s => `Session ${s.sessionId}`);

    // 집중도 데이터
    const concentrateData = sessions.map(s => s.avgConcentrate);
    // 출석률 데이터
    const attendanceData = sessions.map(s => s.avgAttandance); // Note: typo in API response 'attandance' kept as is

    return {
        labels,
        datasets: [
            {
                label: '평균 집중도',
                backgroundColor: (ctx: any) => {
                    const canvas = ctx.chart.ctx;
                    const gradient = canvas.createLinearGradient(0, 0, 0, 300);
                    gradient.addColorStop(0, 'rgba(79, 70, 229, 0.2)'); // Indigo
                    gradient.addColorStop(1, 'rgba(79, 70, 229, 0)');
                    return gradient;
                },
                borderColor: '#4F46E5',
                pointBackgroundColor: '#ffffff',
                pointBorderColor: '#4F46E5',
                pointBorderWidth: 4,
                pointRadius: 6,
                data: concentrateData,
                fill: true,
                tension: 0.4
            },
            {
                label: '평균 출석률',
                backgroundColor: 'transparent',
                borderColor: '#EF4444', // Red-500
                pointBackgroundColor: '#ffffff',
                pointBorderColor: '#EF4444',
                pointBorderWidth: 4,
                pointRadius: 6,
                data: attendanceData,
                fill: false,
                tension: 0.4
            }
        ]
    };
});

const chartOptions: any = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
        legend: {
            display: true,
            position: 'top',
            align: 'end',
            labels: {
                usePointStyle: true,
                boxWidth: 8,
                font: { size: 12 }
            }
        },
        tooltip: {
            backgroundColor: 'rgba(15, 23, 42, 0.9)',
            padding: 12,
            cornerRadius: 8,
            callbacks: {
                label: (context: any) => `${context.dataset.label}: ${context.parsed.y.toFixed(1)}%`
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
                color: '#94a3b8',
                stepSize: 20
            }
        },
        x: {
            grid: { display: false },
            ticks: {
                font: { size: 11, weight: 'bold' },
                color: '#64748b'
            }
        }
    }
};

// 우수 수강생 데이터 (퀴즈 점수 합계 기준 정렬 예시)
const topStudents = computed(() => {
    if (!courseStatistics.value?.courseLearnerAnalysis) return [];

    // 점수(sumQuizCorrect) 내림차순 정렬 후 상위 5명
    // (만약 점수 기준이 아니면 sumConcentrate 등 다른 기준으로 변경 가능)
    const sorted = [...courseStatistics.value.courseLearnerAnalysis].sort((a, b) => b.sumQuizCorrect - a.sumQuizCorrect).slice(0, 5);

    return sorted.map((student, index) => ({
        id: student.memberId,
        name: student.name,
        initial: student.name.substring(0, 1), // 이름 첫 글자 (Mock Logic)
        score: student.sumQuizCorrect,
        badge: index === 0 ? '퀴즈 1위' : '' // 단순 뱃지 예시
    }));
});
</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar {
    width: 4px;
}

.custom-scrollbar::-webkit-scrollbar-track {
    background: transparent;
}

.custom-scrollbar::-webkit-scrollbar-thumb {
    background-color: #e2e8f0;
    border-radius: 20px;
}
</style>
