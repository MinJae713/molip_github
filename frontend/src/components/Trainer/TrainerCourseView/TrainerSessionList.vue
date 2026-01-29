<template>
    <div class="space-y-6">
        <!-- 검색 및 필터 -->
        <div class="flex items-center justify-between">
            <div class="flex items-center gap-3">
                <div class="relative">
                    <span class="absolute inset-y-0 left-0 flex items-center pl-3 text-slate-400">
                        <span class="material-symbols-outlined text-[18px]">search</span>
                    </span>
                    <input
                        class="pl-9 w-64 border-slate-200 bg-white w-full border border-slate-300 rounded-lg px-3 py-2 text-sm focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 transition-colors placeholder:text-slate-400"
                        placeholder="세션 검색" type="text" />
                </div>
                <select
                    class="w-36 border-slate-200 bg-white w-full border border-slate-300 rounded-lg px-3 py-2 text-sm focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 transition-colors placeholder:text-slate-400">
                    <option>전체 상태</option>
                    <option>예정</option>
                    <option>진행중</option>
                    <option>종료</option>
                </select>
            </div>
            <button
                class="px-4 py-2 bg-indigo-600 hover:bg-indigo-700 text-white rounded-lg text-sm font-bold shadow-sm shadow-indigo-200 transition-colors flex items-center gap-2">
                <span class="material-symbols-outlined text-[18px]">add</span>
                세션 추가
            </button>
        </div>

        <!-- 세션 테이블 -->
        <div class="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
            <div class="overflow-x-auto">
                <table class="w-full text-left border-collapse">
                    <thead>
                        <tr
                            class="bg-slate-50 border-b border-slate-200 text-xs text-slate-500 uppercase tracking-wider">
                            <th class="px-6 py-4 font-bold w-16 text-center">No</th>
                            <th class="px-6 py-4 font-bold">세션 정보</th>
                            <th class="px-6 py-4 font-bold w-48">유형</th>
                            <th class="px-6 py-4 font-bold w-48">일시</th>
                            <th class="px-6 py-4 font-bold w-32 text-center">상태</th>
                            <th class="px-6 py-4 font-bold w-40 text-center">관리</th>
                        </tr>
                    </thead>
                    <tbody class="divide-y divide-slate-100">
                        <!-- 세션 아이템 (반복) -->
                        <tr v-for="(session, index) in sessions" :key="index"
                            class="hover:bg-slate-50/80 transition-colors group">
                            <td class="px-6 py-4 text-center">
                                <span class="text-sm font-bold text-slate-400">{{ String(index +
                                    1).padStart(2, '0') }}</span>
                            </td>
                            <td class="px-6 py-4">
                                <div class="flex flex-col">
                                    <span
                                        class="text-sm font-bold text-slate-900 mb-0.5 group-hover:text-indigo-600 transition-colors">{{
                                            session.title }}</span>
                                    <span class="text-xs text-slate-500">{{ session.description }}</span>
                                </div>
                            </td>
                            <td class="px-6 py-4">
                                <div class="flex items-center gap-2">

                                    <span class="p-1.5 rounded-md"
                                        :class="session.type === 'Live 강의' ? 'bg-indigo-50 text-indigo-600' : 'bg-orange-50 text-orange-600'">
                                        <span class="material-symbols-outlined text-[18px]">
                                            {{ session.type === 'Live 강의' ? 'videocam' : 'assignment' }}
                                        </span>
                                    </span>
                                    <span class="text-sm font-medium text-slate-600">{{ session.type }}</span>
                                </div>
                            </td>
                            <td class="px-6 py-4">
                                <div class="flex flex-col">
                                    <span class="text-sm font-bold text-slate-700">{{ session.date }}</span>
                                    <span class="text-xs text-slate-500">{{ session.time }}</span>
                                </div>
                            </td>
                            <td class="px-6 py-4">
                                <div class="flex justify-center">
                                    <span
                                        class="border px-2 py-0.5 rounded text-xs font-bold flex items-center gap-1 w-fit"
                                        :class="{
                                            'bg-emerald-50 text-emerald-600 border-emerald-100': session.status === '예정',
                                            'bg-slate-100 text-slate-500 border-slate-200': session.status === '준비중'
                                        }">
                                        <span class="size-1.5 rounded-full"
                                            :class="session.status === '예정' ? 'bg-emerald-500' : 'bg-slate-400'"></span>
                                        {{ session.status }}
                                    </span>
                                </div>
                            </td>
                            <!-- 관리 -->
                            <td class="px-6 py-4">
                                <div class="flex items-center justify-center gap-2">
                                    <button v-if="session.status === '예정'" @click="$emit('start-live', session)"
                                        class="bg-indigo-600 hover:bg-indigo-700 text-white text-xs font-bold px-3 py-1.5 rounded-lg shadow-sm transition-colors flex items-center gap-1.5">
                                        <span class="material-symbols-outlined text-[16px]">play_arrow</span>
                                        라이브 시작
                                    </button>
                                    <button
                                        class="text-slate-400 hover:text-indigo-600 hover:bg-indigo-50 p-1.5 rounded-md transition-colors"
                                        title="관리">
                                        <span class="material-symbols-outlined text-[20px]">more_vert</span>
                                    </button>
                                </div>
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>
            <div class="p-4 border-t border-slate-100 bg-slate-50/50 flex items-center justify-between">
                <div class="text-xs text-slate-500 font-medium">
                    총 <span class="text-indigo-600 font-bold">{{ sessions.length }}</span>개의 세션이 등록되어 있습니다.
                </div>
                <div class="flex items-center gap-2">
                    <button
                        class="px-3 py-1.5 bg-white border border-slate-300 rounded text-xs font-bold text-slate-600 hover:bg-slate-50 disabled:opacity-50">이전</button>
                    <button
                        class="px-3 py-1.5 bg-white border border-slate-300 rounded text-xs font-bold text-slate-600 hover:bg-slate-50">다음</button>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';

const emit = defineEmits(['start-live']);

// 임시 데이터
const sessions = ref([
    {
        title: 'Session 01: 데이터 기반 의사결정의 이해',
        description: '데이터 리터러시의 중요성과 기본 개념 학습',
        type: 'Live 강의',
        date: '2023.10.15 (월)',
        time: '14:00 - 16:00',
        status: '예정'
    },
    {
        title: 'Session 02: 고객 여정 지도(CJM) 실습',
        description: '실제 고객 데이터를 활용한 CJM 작성 워크샵',
        type: 'Live 강의',
        date: '2023.10.17 (수)',
        time: '14:00 - 17:00',
        status: '예정'
    },
    {
        title: '중간 과제: 고객 페르소나 정의서 제출',
        description: '팀별 프로젝트 1차 결과물 제출',
        type: '과제 제출',
        date: '2023.10.20 (금)',
        time: '23:59 까지',
        status: '준비중'
    },
    {
        title: 'Session 03: 데이터 시각화 도구 활용',
        description: 'Tableau를 활용한 기초 데이터 시각화 실습',
        type: 'Live 강의',
        date: '2023.10.24 (화)',
        time: '14:00 - 17:00',
        status: '준비중'
    }
]);
</script>
