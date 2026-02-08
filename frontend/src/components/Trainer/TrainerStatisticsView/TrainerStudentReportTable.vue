<!-- src/components/Trainer/TrainerStatisticsView/TrainerStudentReportTable.vue -->
<template>
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
                    <input type="text" placeholder="이름 검색..." v-model="searchQuery"
                        class="pl-9 pr-4 py-1.5 border border-slate-200 rounded-lg text-sm focus:outline-none focus:border-indigo-500 w-48 transition-colors">
                </div>
            </div>
        </div>

        <div class="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
            <table class="w-full text-left border-collapse">
                <thead class="bg-slate-50 border-b border-slate-200">
                    <tr>
                        <th class="px-6 py-3 text-xs font-bold text-slate-500 uppercase">이름</th>
                        <th class="px-6 py-3 text-xs font-bold text-slate-500 uppercase">이메일</th>
                        <th class="px-6 py-3 text-xs font-bold text-slate-500 uppercase text-center">총 출석 세션</th>
                        <th class="px-6 py-3 text-xs font-bold text-slate-500 uppercase text-center">총 집중도 점수</th>
                        <th class="px-6 py-3 text-xs font-bold text-slate-500 uppercase text-center">총 퀴즈 점수</th>
                        <th class="px-6 py-3 text-xs font-bold text-slate-500 uppercase text-center">수료 여부</th>
                    </tr>
                </thead>
                <tbody class="divide-y divide-slate-100">
                    <tr v-if="filteredStudents.length === 0">
                        <td colspan="6" class="px-6 py-8 text-center text-slate-500 text-sm">
                            검색 결과가 없습니다.
                        </td>
                    </tr>
                    <tr v-for="student in filteredStudents" :key="student.memberId"
                        class="hover:bg-slate-50 transition-colors">
                        <td class="px-6 py-4">
                            <div class="flex items-center gap-3">
                                <div
                                    class="size-8 rounded-full bg-indigo-100 text-indigo-600 flex items-center justify-center font-bold text-xs uppercase">
                                    {{ student.name.slice(0, 2) }}
                                </div>
                                <div class="font-bold text-slate-900 text-sm">{{ student.name }}</div>
                            </div>
                        </td>
                        <td class="px-6 py-4 text-sm text-slate-600">{{ student.email }}</td>
                        <td class="px-6 py-4 text-center">
                            <span class="text-sm font-bold text-slate-900">{{ student.sumAttendance }}회</span>
                        </td>
                        <td class="px-6 py-4 text-center">
                            <span
                                :class="['inline-block px-2 py-0.5 rounded text-xs font-bold', getFocusClass(student.sumConcentrate)]">
                                {{ student.sumConcentrate }}점
                            </span>
                        </td>
                        <td class="px-6 py-4 text-center text-sm font-bold text-slate-900">{{ student.sumQuizCorrect }}점
                        </td>
                        <td class="px-6 py-4 text-center">
                            <span v-if="student.isCertificated"
                                class="px-2 py-0.5 bg-indigo-100 text-indigo-700 rounded-full text-xs font-bold">수료
                                완료</span>
                            <span v-else
                                class="px-2 py-0.5 bg-slate-100 text-slate-500 rounded-full text-xs font-bold">미수료</span>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>
    </section>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';

interface CourseLearnerAnalysis {
    memberId: number;
    name: string;
    email: string;
    isCertificated: boolean;
    workspaceMemberId: number;
    courseLearnerId: number;
    sumConcentrate: number;
    sumAttendance: number;
    sumQuizCorrect: number;
}

const props = defineProps<{
    students: CourseLearnerAnalysis[] | undefined;
}>();

const searchQuery = ref('');

const filteredStudents = computed(() => {
    if (!props.students) return [];
    if (!searchQuery.value) return props.students;
    const query = searchQuery.value.toLowerCase();
    return props.students.filter(s =>
        s.name.toLowerCase().includes(query) ||
        s.email.toLowerCase().includes(query)
    );
});

const getFocusClass = (score: number) => {
    // Score interpretation might vary, adjusting thresholds roughly
    if (score >= 80) return 'bg-emerald-100 text-emerald-700';
    if (score >= 50) return 'bg-amber-100 text-amber-700';
    return 'bg-red-100 text-red-700';
};
</script>
