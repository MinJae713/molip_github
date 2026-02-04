<template>
    <div class="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
        <div
            class="px-6 py-4 border-b border-slate-100 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
            <h3 class="font-bold text-slate-800 text-lg">수강생 명단</h3>
            <div class="flex items-center gap-2">
                <div class="relative hidden sm:block">
                    <span
                        class="material-symbols-outlined absolute left-3 top-2 text-slate-400 text-[18px]">search</span>
                    <input v-model="searchQuery"
                        class="pl-9 pr-4 py-1.5 border border-slate-300 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 placeholder:text-slate-400 w-48"
                        placeholder="수강생 검색" type="text" />
                </div>
                <button
                    class="size-9 border border-slate-300 rounded-lg flex items-center justify-center text-slate-600 hover:bg-slate-50 hover:text-indigo-600 transition-colors">
                    <span class="material-symbols-outlined text-[20px]">filter_list</span>
                </button>
            </div>
        </div>
        <div class="overflow-x-auto">
            <table class="w-full text-left border-collapse">
                <thead>
                    <tr
                        class="bg-slate-50/80 border-b border-slate-200 text-xs font-bold text-slate-500 uppercase tracking-wider">
                        <th class="px-6 py-3">이름 / 이메일</th>
                        <th class="px-6 py-3">부서</th>
                        <th class="px-6 py-3 text-center">출석률</th>
                        <th class="px-6 py-3 text-center">최근 접속</th>
                        <th class="px-6 py-3 text-center">상태</th>
                        <th class="px-6 py-3 text-right">관리</th>
                    </tr>
                </thead>
                <tbody class="divide-y divide-slate-100 text-sm">
                    <tr v-if="filteredStudents.length === 0">
                        <td colspan="6" class="px-6 py-8 text-center text-slate-500">
                            등록된 수강생이 없습니다.
                        </td>
                    </tr>
                    <tr v-for="student in filteredStudents" :key="student.id"
                        class="hover:bg-slate-50/50 transition-colors group">
                        <td class="px-6 py-4">
                            <div class="flex items-center gap-3">
                                <div
                                    class="size-9 rounded-full bg-teal-100 text-teal-700 flex items-center justify-center font-bold text-xs">
                                    {{ getInitials(student.name) }}
                                </div>
                                <div>
                                    <div class="font-bold text-slate-900">{{ student.name }}</div>
                                    <div class="text-xs text-slate-500">{{ student.email }}</div>
                                </div>
                            </div>
                        </td>
                        <td class="px-6 py-4 text-slate-600">{{ student.department }}</td>
                        <td class="px-6 py-4 text-center">
                            <span
                                class="inline-block px-2 py-0.5 rounded text-xs font-bold bg-slate-100 text-slate-500 border border-slate-200">
                                {{ student.attendanceRate }}%
                            </span>
                        </td>
                        <td class="px-6 py-4 text-center text-slate-500 text-xs">
                            {{ student.lastConnection ? formatTime(student.lastConnection) : '미접속' }}
                        </td>
                        <td class="px-6 py-4 text-center">
                            <div class="flex items-center justify-center gap-1.5">
                                <span class="size-2 rounded-full"
                                    :class="student.status === 'ACTIVE' ? 'bg-emerald-400' : 'bg-slate-400'"></span>
                                <span class="text-slate-700 font-medium text-xs">
                                    {{ student.status === 'ACTIVE' ? '수강 중' : '비활성' }}
                                </span>
                            </div>
                        </td>
                        <td class="px-6 py-4 text-right">
                            <button class="text-slate-400 hover:text-indigo-600 transition-colors">
                                <span class="material-symbols-outlined text-[20px]">more_vert</span>
                            </button>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>
        <div class="px-6 py-3 border-t border-slate-100 bg-slate-50 flex items-center justify-between"
            v-if="filteredStudents.length > 0">
            <span class="text-xs text-slate-500">총 {{ filteredStudents.length }}명 표시</span>
            <div class="flex gap-1">
                <!-- Pagination Buttons (Mock) -->
                <button
                    class="size-8 rounded border border-slate-300 bg-white flex items-center justify-center hover:bg-slate-50 text-slate-500 disabled:opacity-50">
                    <span class="material-symbols-outlined text-[16px]">chevron_left</span>
                </button>
                <button
                    class="size-8 rounded border border-indigo-500 bg-indigo-50 flex items-center justify-center text-indigo-700 font-bold text-xs">1</button>
                <button
                    class="size-8 rounded border border-slate-300 bg-white flex items-center justify-center hover:bg-slate-50 text-slate-500">
                    <span class="material-symbols-outlined text-[16px]">chevron_right</span>
                </button>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { useTrainerCourseStore } from '@/stores/trainerCourse';
import dayjs from 'dayjs';
import relativeTime from 'dayjs/plugin/relativeTime';
import 'dayjs/locale/ko';

dayjs.extend(relativeTime);
dayjs.locale('ko');

const store = useTrainerCourseStore();
const searchQuery = ref('');

const filteredStudents = computed(() => {
    if (!searchQuery.value) return store.students;
    const query = searchQuery.value.toLowerCase();
    return store.students.filter(
        (s) =>
            s.name.toLowerCase().includes(query) ||
            s.email.toLowerCase().includes(query) ||
            s.department.toLowerCase().includes(query)
    );
});

const getInitials = (name: string) => {
    return name.slice(0, 2);
};

const formatTime = (time: string) => {
    return dayjs(time).fromNow();
};
</script>
