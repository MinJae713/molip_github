<template>
    <div
        class="flex flex-col gap-4 p-5 bg-white rounded-[32px] border border-slate-100 shadow-sm h-full overflow-hidden">
        <div class="flex items-center justify-between mb-2">
            <h3 class="text-lg font-bold text-slate-800">퀴즈</h3>
            <span class="text-xs text-slate-500">실시간 퀴즈 관리</span>
        </div>

        <div ref="contentContainer" class="flex-1 overflow-y-auto p-4 space-y-4 bg-slate-50/30 rounded-2xl">
            <!-- 퀴즈 데이터가 없을 때 -->
            <div v-if="!quizData" class="text-center py-20">
                <div
                    class="size-16 bg-indigo-50 text-indigo-500 rounded-2xl flex items-center justify-center mx-auto mb-4">
                    <span class="material-symbols-outlined text-4xl">quiz</span>
                </div>
                <p class="text-slate-500 text-sm">퀴즈를 시작하여<br />학생들의 이해도를 확인하세요</p>
            </div>

            <!-- 퀴즈 데이터가 있을 때 -->
            <div v-else class="space-y-4">
                <div class="bg-white rounded-xl p-4 border border-slate-200 shadow-sm">
                    <h4 class="text-sm font-bold text-slate-800 mb-2">{{ quizData.title || '퀴즈' }}</h4>
                    <p class="text-xs text-slate-600">{{ quizData.description || '퀴즈가 진행 중입니다.' }}</p>

                    <!-- 퀴즈 상태 -->
                    <div class="mt-3 flex items-center gap-2">
                        <span class="text-xs font-bold px-2 py-1 rounded"
                            :class="quizData.status === 'active' ? 'bg-green-100 text-green-700' : 'bg-slate-100 text-slate-600'">
                            {{ quizData.status === 'active' ? '진행 중' : '종료됨' }}
                        </span>
                        <span class="text-xs text-slate-400">
                            응답: {{ quizData.responseCount || 0 }}명
                        </span>
                    </div>
                </div>

                <!-- 추후 확장: 응답 현황, 통계 등 -->
            </div>
        </div>

        <!-- 퀴즈 액션 버튼 -->
        <div class="shrink-0 flex gap-2">
            <button @click="handleStartQuiz"
                class="flex-1 py-3 bg-indigo-600 text-white rounded-xl font-bold hover:bg-indigo-700 transition-all shadow-sm text-sm">
                퀴즈 시작
            </button>
            <button @click="handleEndQuiz" v-if="quizData && quizData.status === 'active'"
                class="flex-1 py-3 bg-slate-200 text-slate-700 rounded-xl font-bold hover:bg-slate-300 transition-all text-sm">
                퀴즈 종료
            </button>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';

const props = defineProps<{
    quizData: any // 추후 타입 정의
}>();

const emit = defineEmits<{
    'send-quiz-action': [action: any]
}>();

const contentContainer = ref<HTMLDivElement | null>(null);

const handleStartQuiz = () => {
    emit('send-quiz-action', {
        action: 'start',
        timestamp: new Date().toISOString()
    });
};

const handleEndQuiz = () => {
    emit('send-quiz-action', {
        action: 'end',
        timestamp: new Date().toISOString()
    });
};
</script>
