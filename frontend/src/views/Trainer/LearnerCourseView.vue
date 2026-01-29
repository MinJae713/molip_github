<template>
    <div class="max-w-7xl mx-auto space-y-6">
        <!-- 헤더 (타이틀 및 버튼) -->
        <TrainerCourseDetailHeader title="데이터 기반 고객 경험 최적화 전략" />

        <!-- 통계 요약 (기간, 수강생, 상태) -->
        <TrainerCourseStats />

        <!-- 탭 네비게이션 -->
        <div class="border-b border-slate-200">
            <div class="flex space-x-8">
                <button v-for="tab in tabs" :key="tab.id" @click="activeTab = tab.id"
                    class="pb-3 px-1 border-b-2 text-sm font-medium transition-colors"
                    :class="activeTab === tab.id ? 'border-indigo-600 text-indigo-600 font-bold' : 'border-transparent text-slate-500 hover:text-slate-700 hover:border-slate-300'">
                    {{ tab.label }}
                </button>
            </div>
        </div>

        <!-- 탭 내용 영역 -->
        <div>
            <!-- 세션 일정 관리 -->
            <div v-if="activeTab === 'session'">
                <TrainerSessionList />
            </div>
            <!-- 다른 탭 placeholder -->
            <div v-else class="py-20 text-center bg-slate-50 rounded-xl border border-dashed border-slate-200">
                <p class="text-slate-500">준비 중인 페이지입니다.</p>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import TrainerCourseDetailHeader from '@/components/Trainer/TrainerCourseView/TrainerCourseDetailHeader.vue';
import TrainerCourseStats from '@/components/Trainer/TrainerCourseView/TrainerCourseStats.vue';
import TrainerSessionList from '@/components/Trainer/TrainerCourseView/TrainerSessionList.vue';

const activeTab = ref('session');
const tabs = [
    { id: 'session', label: '세션 일정 관리' },
    { id: 'student', label: '수강생 관리' },
    { id: 'material', label: '자료실' },
    { id: 'stats', label: '교육통계' }
];
</script>
