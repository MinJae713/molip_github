<template>
    <div v-if="isOpen" class="fixed inset-0 z-[100] flex items-center justify-center p-4">
        <!-- Backdrop -->
        <div class="absolute inset-0 bg-slate-900/40 backdrop-blur-sm transition-opacity" @click="$emit('close')"></div>

        <!-- Modal Content -->
        <div class="relative w-full max-w-2xl bg-white rounded-2xl shadow-2xl overflow-hidden transform transition-all animate-scaleIn flex flex-col max-h-[90vh]">
            
            <!-- Header -->
            <div class="flex items-center justify-between p-6 border-b border-slate-100 bg-slate-50/50">
                <div class="flex items-center gap-3">
                    <div class="w-10 h-10 rounded-full bg-indigo-100 flex items-center justify-center text-indigo-600">
                        <span class="material-symbols-outlined text-[24px]">psychology</span>
                    </div>
                    <div>
                        <h2 class="text-lg font-bold text-slate-800">AI 맞춤 퀴즈</h2>
                        <p class="text-xs text-slate-500">데드존 구간 내용을 바탕으로 생성된 퀴즈입니다.</p>
                    </div>
                </div>
                <button @click="$emit('close')" class="w-8 h-8 flex items-center justify-center rounded-full hover:bg-slate-100 text-slate-400 hover:text-slate-600 transition-colors">
                    <span class="material-symbols-outlined text-[20px]">close</span>
                </button>
            </div>

            <!-- Quiz Content -->
            <div class="p-8 overflow-y-auto custom-scrollbar">
                <div v-if="quizData" class="space-y-8">
                    <!-- Question -->
                    <div>
                        <div class="inline-block px-3 py-1 bg-indigo-100 text-indigo-700 text-xs font-bold rounded-full mb-3">
                            Q. 문제
                        </div>
                        <h3 class="text-xl font-bold text-slate-800 leading-relaxed break-keep">
                            {{ quizData.question }}
                        </h3>
                    </div>

                    <!-- Options -->
                    <div class="space-y-3">
                        <div v-for="(answer, index) in [quizData.answer1, quizData.answer2, quizData.answer3, quizData.answer4]" 
                             :key="index"
                             class="group relative p-4 rounded-xl border-2 transition-all duration-200 cursor-pointer hover:bg-slate-50"
                             :class="[
                                selectedAnswer === index + 1 
                                    ? (isSubmitted 
                                        ? (index + 1 === quizData.correctAnswer ? 'border-emerald-500 bg-emerald-50 hover:bg-emerald-50' : 'border-red-500 bg-red-50 hover:bg-red-50')
                                        : 'border-indigo-500 bg-indigo-50 hover:bg-indigo-50')
                                    : 'border-slate-100'
                             ]"
                             @click="!isSubmitted && selectAnswer(index + 1)">
                            
                            <div class="flex items-start gap-4">
                                <div class="flex-none w-6 h-6 rounded-full border-2 flex items-center justify-center text-xs font-bold transition-colors"
                                     :class="[
                                        selectedAnswer === index + 1
                                            ? (isSubmitted
                                                ? (index + 1 === quizData.correctAnswer ? 'border-emerald-500 text-emerald-600' : 'border-red-500 text-red-600')
                                                : 'border-indigo-500 text-indigo-600')
                                            : 'border-slate-300 text-slate-400 group-hover:border-slate-400'
                                     ]">
                                    {{ index + 1 }}
                                </div>
                                <span class="text-sm font-medium text-slate-700 leading-6"
                                      :class="{ 'text-slate-900': selectedAnswer === index + 1 }">
                                    {{ answer }}
                                </span>
                            </div>

                            <!-- Result Icon -->
                            <div v-if="isSubmitted && (selectedAnswer === index + 1 || index + 1 === quizData.correctAnswer)" 
                                 class="absolute right-4 top-1/2 -translate-y-1/2">
                                <span v-if="index + 1 === quizData.correctAnswer" class="material-symbols-outlined text-emerald-500">check_circle</span>
                                <span v-else-if="selectedAnswer === index + 1" class="material-symbols-outlined text-red-500">cancel</span>
                            </div>
                        </div>
                    </div>

                    <!-- Explanation -->
                    <div v-if="isSubmitted" class="animate-fadeIn p-5 rounded-xl bg-slate-50 border border-slate-200">
                        <div class="flex items-center gap-2 mb-2">
                            <span class="material-symbols-outlined text-slate-400 text-[18px]">lightbulb</span>
                            <span class="text-sm font-bold text-slate-700">해설</span>
                        </div>
                        <p class="text-sm text-slate-600 leading-relaxed">{{ quizData.explanation }}</p>
                    </div>
                </div>
                
                <!-- Loading State (Optional) -->
                <div v-else class="flex flex-col items-center justify-center py-12 text-slate-400">
                    <span class="material-symbols-outlined text-4xl mb-2 animate-spin">sync</span>
                    <span class="text-sm">퀴즈 정보를 불러오는 중입니다...</span>
                </div>
            </div>

            <!-- Footer -->
            <div class="p-6 border-t border-slate-100 bg-slate-50/50 flex justify-end gap-3">
                <button 
                    @click="$emit('close')" 
                    class="px-5 py-2.5 rounded-xl text-sm font-bold text-slate-500 hover:text-slate-700 hover:bg-slate-100 transition-colors">
                    닫기
                </button>
                <button 
                    v-if="!isSubmitted && quizData"
                    @click="submitQuiz"
                    class="px-5 py-2.5 rounded-xl text-sm font-bold text-white shadow-md shadow-indigo-200 transition-all transform active:scale-95 disabled:opacity-50 disabled:cursor-not-allowed"
                    :class="selectedAnswer ? 'bg-indigo-600 hover:bg-indigo-700' : 'bg-slate-300 cursor-not-allowed'"
                    :disabled="!selectedAnswer">
                    정답 확인하기
                </button>
                <button 
                    v-else-if="isSubmitted"
                    @click="$emit('close')"
                    class="px-5 py-2.5 rounded-xl text-sm font-bold bg-slate-800 text-white hover:bg-slate-900 shadow-md transition-all">
                    완료
                </button>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import type { AiQuizResponse } from '@/api/review';

const props = defineProps<{
    isOpen: boolean;
    quizData: AiQuizResponse | null;
}>();

defineEmits(['close']);

const isSubmitted = ref(false);
const selectedAnswer = ref<number | null>(null);

const selectAnswer = (index: number) => {
    if (isSubmitted.value) return;
    selectedAnswer.value = index;
};

const submitQuiz = () => {
    if (!selectedAnswer.value) return;
    isSubmitted.value = true;
};

// Reset state
watch(() => props.isOpen, (newVal) => {
    if (newVal) {
        selectedAnswer.value = null;
        isSubmitted.value = false;
    }
});

watch(() => props.quizData, () => {
    selectedAnswer.value = null;
    isSubmitted.value = false;
});
</script>

<style scoped>
.animate-scaleIn {
    animation: scaleIn 0.2s ease-out;
}

@keyframes scaleIn {
    from {
        opacity: 0;
        transform: scale(0.95);
    }
    to {
        opacity: 1;
        transform: scale(1);
    }
}

.animate-fadeIn {
    animation: fadeIn 0.3s ease-out;
}

@keyframes fadeIn {
    from { opacity: 0; transform: translateY(5px); }
    to { opacity: 1; transform: translateY(0); }
}

.custom-scrollbar::-webkit-scrollbar {
    width: 6px;
}
.custom-scrollbar::-webkit-scrollbar-track {
    background: transparent;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
    background-color: #e2e8f0;
    border-radius: 3px;
}
.custom-scrollbar::-webkit-scrollbar-thumb:hover {
    background-color: #cbd5e1;
}
</style>
