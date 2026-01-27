<template>
    <div class="bg-white border border-slate-200 rounded-xl p-6 shadow-sm">
        <h2 class="text-lg font-bold text-slate-900 mb-4">
            세션별 상세 리포트 <span class="text-purple-600 ml-1">- {{ courseName }}</span>
        </h2>

        <div class="overflow-x-auto">
            <table class="w-full text-left border-collapse">
                <thead>
                    <tr class="border-b border-slate-200 text-xs text-slate-500 bg-slate-50/50">
                        <th class="p-4 font-semibold w-1/3">세션명</th>
                        <th class="p-4 font-semibold text-center">날짜</th>
                        <th class="p-4 font-semibold text-center">학습 시간</th>
                        <th class="p-4 font-semibold text-center">집중도</th>
                        <th class="p-4 font-semibold text-center">참여도</th>
                        <th class="p-4 font-semibold text-center text-purple-600">종합 점수</th>
                    </tr>
                </thead>
                <tbody class="text-sm">
                    <tr v-if="!reports || reports.length === 0">
                        <td colspan="6" class="p-8 text-center text-slate-400">
                            리포트 내역이 없습니다.
                        </td>
                    </tr>

                    <tr v-else v-for="session in reports" :key="session.id"
                        class="border-b border-slate-100 hover:bg-slate-50 transition-colors last:border-0">
                        <td class="p-4 font-medium text-slate-800">{{ session.name }}</td>
                        <td class="p-4 text-center text-slate-500">{{ session.date }}</td>
                        <td class="p-4 text-center text-slate-600">{{ session.duration }}</td>
                        <td class="p-4 text-center">
                            <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium"
                                :class="getFocusColor(session.focus)">
                                {{ session.focus }}%
                            </span>
                        </td>
                        <td class="p-4 text-center text-slate-600">{{ session.participation }}</td>
                        <td class="p-4 text-center font-bold text-purple-600">{{ session.activity_score }}점</td>
                    </tr>
                </tbody>
            </table>
        </div>
    </div>
</template>
<script setup lang="ts">
import type { SessionReportItem } from '@/types';
interface Props {
    reports: SessionReportItem[] | undefined;
    courseName: string;
}
defineProps<Props>();
const getFocusColor = (score: number) => {
    if (score >= 90) return 'bg-green-100 text-green-700';
    if (score >= 70) return 'bg-blue-100 text-blue-700';
    if (score >= 50) return 'bg-yellow-100 text-yellow-700';
    return 'bg-red-100 text-red-700';
};
</script>