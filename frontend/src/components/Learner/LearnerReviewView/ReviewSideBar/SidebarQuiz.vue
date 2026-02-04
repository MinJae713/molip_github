<template>
    <div class="h-full flex flex-col bg-slate-50">
        <!-- 헤더: 점수 요약 -->
        <div class="flex-none p-5 bg-white border-b border-slate-200">
            <div class="flex items-center justify-between mb-2">
                <h3 class="font-bold text-slate-800 text-lg flex items-center gap-2">
                    <span class="material-symbols-outlined text-indigo-500">quiz</span>
                    퇴실 퀴즈
                </h3>
                <div class="text-sm font-bold px-3 py-1 bg-indigo-50 text-indigo-600 rounded-full border border-indigo-100">
                    점수: {{ correctCount }}/{{ totalQuestions }}
                </div>
            </div>
            <div class="w-full bg-slate-100 h-2 rounded-full overflow-hidden mt-2">
                <div class="bg-indigo-500 h-full rounded-full transition-all duration-1000 ease-out"
                     :style="{ width: `${(correctCount / totalQuestions) * 100}%` }"></div>
            </div>
        </div>

        <!-- 퀴즈 목록 -->
        <div class="flex-1 overflow-y-auto p-4 custom-scrollbar">
            <div class="space-y-3">
                <div v-for="(quiz, index) in quizList" :key="quiz.id"
                     @click="openModal(quiz)"
                     class="group bg-white p-4 rounded-xl border border-slate-200 shadow-sm hover:shadow-md hover:border-indigo-200 transition-all cursor-pointer relative overflow-hidden">
                    
                    <div class="flex items-start gap-4">
                        <!-- 번호 -->
                        <div class="flex-none w-8 h-8 rounded-lg bg-slate-50 flex items-center justify-center font-bold text-slate-500 group-hover:bg-indigo-50 group-hover:text-indigo-600 transition-colors">
                            Q{{ index + 1 }}
                        </div>

                        <!-- 문제 요약 -->
                        <div class="flex-1 min-w-0 pt-1">
                            <p class="text-sm font-medium text-slate-800 truncate pr-6">
                                {{ quiz.question }}
                            </p>
                            <p class="text-xs text-slate-400 mt-0.5">
                                {{ getStatusText(quiz) }}
                            </p>
                        </div>

                        <!-- 상태 아이콘 -->
                        <div class="flex-none pt-1">
                            <span v-if="quiz.submittedAnswer === null" 
                                  class="material-symbols-outlined text-slate-300" 
                                  title="미제출">
                                remove_circle_outline
                            </span>
                            <span v-else-if="quiz.submittedAnswer === quiz.answer" 
                                  class="material-symbols-outlined text-emerald-500" 
                                  title="정답">
                                check_circle
                            </span>
                            <span v-else 
                                  class="material-symbols-outlined text-rose-500" 
                                  title="오답">
                                cancel
                            </span>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- 퀴즈 상세 모달 -->
        <div v-if="isModalOpen && selectedQuiz" 
             class="fixed inset-0 z-[100] flex items-center justify-center p-4 bg-slate-900/50 backdrop-blur-sm"
             @click.self="closeModal">
            <div class="bg-white rounded-2xl shadow-2xl w-full max-w-md overflow-hidden animate-in fade-in zoom-in duration-200">
                <!-- 모달 헤더 -->
                <div class="px-6 py-4 border-b border-slate-100 flex items-center justify-between bg-slate-50">
                    <h4 class="font-bold text-slate-800 flex items-center gap-2">
                        <span class="w-6 h-6 rounded bg-indigo-100 text-indigo-600 flex items-center justify-center text-xs">Q{{ selectedQuiz.id }}</span>
                        문제 상세
                    </h4>
                    <button @click="closeModal" class="text-slate-400 hover:text-slate-600 transition-colors">
                        <span class="material-symbols-outlined">close</span>
                    </button>
                </div>

                <!-- 모달 내용 -->
                <div class="p-6">
                    <p class="text-lg font-bold text-slate-800 mb-6 leading-relaxed">
                        {{ selectedQuiz.question }}
                    </p>

                    <div class="space-y-3">
                        <div v-for="(option, idx) in selectedQuiz.options" :key="idx"
                             @click="selectOption(idx)"
                             class="flex items-center gap-3 p-3 rounded-xl border-2 transition-all relative overflow-hidden"
                             :class="[
                                getOptionClass(selectedQuiz, idx),
                                canSelect(selectedQuiz) ? 'cursor-pointer hover:border-indigo-300' : ''
                             ]">
                            
                            <!-- 옵션 번호/체크 -->
                            <div class="flex-none w-8 h-8 rounded-full flex items-center justify-center text-sm font-bold transition-colors"
                                 :class="getOptionNumberClass(selectedQuiz, idx)">
                                <span v-if="isCorrectOption(selectedQuiz, idx)" class="material-symbols-outlined text-lg">check</span>
                                <span v-else-if="isWrongSelection(selectedQuiz, idx)" class="material-symbols-outlined text-lg">close</span>
                                <span v-else>{{ idx + 1 }}</span>
                            </div>

                            <!-- 옵션 텍스트 -->
                            <span class="text-sm font-medium flex-1 z-10"
                                  :class="getOptionTextClass(selectedQuiz, idx)">
                                {{ option }}
                            </span>

                            <!-- 상태 라벨 (정답/오답) -->
                            <div v-if="isCorrectOption(selectedQuiz, idx)" class="absolute right-3 text-xs font-bold text-emerald-600 bg-white/80 px-2 py-1 rounded shadow-sm">
                                정답
                            </div>
                            <div v-if="isWrongSelection(selectedQuiz, idx)" class="absolute right-3 text-xs font-bold text-rose-600 bg-white/80 px-2 py-1 rounded shadow-sm">
                                내 답안
                            </div>
                        </div>
                    </div>
                </div>

                <!-- 모달 푸터 -->
                <div class="px-6 py-4 bg-slate-50 border-t border-slate-100 flex justify-end gap-2">
                    <button v-if="isUnsubmitted(selectedQuiz)" 
                            @click="submitAnswer"
                            :disabled="tempSelectedAnswer === null"
                            class="px-5 py-2.5 bg-indigo-600 text-white text-sm font-bold rounded-xl hover:bg-indigo-700 transition-colors shadow-lg shadow-indigo-200 disabled:opacity-50 disabled:cursor-not-allowed">
                        제출하기
                    </button>
                    <button v-else 
                            @click="closeModal" 
                            class="px-5 py-2.5 bg-slate-800 text-white text-sm font-bold rounded-xl hover:bg-slate-700 transition-colors shadow-lg shadow-slate-200">
                        닫기
                    </button>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';

interface Quiz {
    id: number;
    question: string;
    options: string[];
    answer: number; // 0-indexed correct option
    submittedAnswer: number | null; // null if not submitted
}

// Mock Data
const quizList = ref<Quiz[]>([
    {
        id: 1,
        question: "다음 중 Vue.js의 라이프사이클 훅이 아닌 것은?",
        options: ["onMounted", "onCreated", "onUpdate", "onRender"],
        answer: 3,
        submittedAnswer: 3 // Correct
    },
    {
        id: 2,
        question: "반응형 상태를 선언하기 위해 사용하는 API는?",
        options: ["reactive", "response", "state", "redux"],
        answer: 0,
        submittedAnswer: 0 // Correct
    },
    {
        id: 3,
        question: "Pinia는 무엇을 위한 라이브러리인가?",
        options: ["라우팅", "상태 관리", "HTTP 통신", "UI 컴포넌트"],
        answer: 1,
        submittedAnswer: 2 // Incorrect (wrong answer)
    },
    {
        id: 4,
        question: "v-bind 디렉티브의 약어는?",
        options: ["@", ":", "#", "$"],
        answer: 1,
        submittedAnswer: null // Unsubmitted
    },
    {
        id: 5,
        question: "SPA의 약자는?",
        options: ["Single Page Application", "Simple Page Action", "Server Protocol API", "Standard Process App"],
        answer: 0,
        submittedAnswer: 0 // Correct
    }
]);

// State
const isModalOpen = ref(false);
const selectedQuiz = ref<Quiz | null>(null);
const tempSelectedAnswer = ref<number | null>(null); // For unsubmitted quizzes

// Computed
const totalQuestions = computed(() => quizList.value.length);
const correctCount = computed(() => 
    quizList.value.filter(q => q.submittedAnswer === q.answer).length
);

// Methods
function openModal(quiz: Quiz) {
    selectedQuiz.value = quiz;
    tempSelectedAnswer.value = null; // Reset temp answer
    isModalOpen.value = true;
}

function closeModal() {
    isModalOpen.value = false;
    setTimeout(() => {
        selectedQuiz.value = null;
    }, 200);
}

function selectOption(idx: number) {
    if (!selectedQuiz.value || !isUnsubmitted(selectedQuiz.value)) return;
    tempSelectedAnswer.value = idx;
}

function submitAnswer() {
    if (!selectedQuiz.value || tempSelectedAnswer.value === null) return;
    
    // Update the actual quiz object
    selectedQuiz.value.submittedAnswer = tempSelectedAnswer.value;
    
    // Find in list and update (reactivity should handle this since we reference the object, but explicit update ensures safety if copies were used)
    const quizInList = quizList.value.find(q => q.id === selectedQuiz.value!.id);
    if (quizInList) {
        quizInList.submittedAnswer = tempSelectedAnswer.value;
    }
}

function getStatusText(quiz: Quiz) {
    if (quiz.submittedAnswer === null) return '미제출';
    return quiz.submittedAnswer === quiz.answer ? '정답' : '오답';
}

function isUnsubmitted(quiz: Quiz) {
    return quiz.submittedAnswer === null;
}

function canSelect(quiz: Quiz) {
    return isUnsubmitted(quiz);
}

// Style Helpers (퀴즈 옵션 스타일)
function isCorrectOption(quiz: Quiz, idx: number) {
    // Only show correct answer if submitted
    return !isUnsubmitted(quiz) && idx === quiz.answer;
}

function isWrongSelection(quiz: Quiz, idx: number) {
    // Only show wrong selection if submitted
    return !isUnsubmitted(quiz) && quiz.submittedAnswer === idx && idx !== quiz.answer;
}

function isTempSelected(idx: number) {
    return tempSelectedAnswer.value === idx;
}

function getOptionClass(quiz: Quiz, idx: number) {
    // Submitted state styles
    if (isCorrectOption(quiz, idx)) {
        return 'border-emerald-500 bg-emerald-50/50';
    }
    if (isWrongSelection(quiz, idx)) {
        return 'border-rose-500 bg-rose-50/50';
    }
    
    // Result logic: Show muted style for non-selected answers after submission
    if (!isUnsubmitted(quiz)) {
         return 'border-slate-100 bg-white text-slate-500 opacity-60';
    }

    // Unsubmitted state styles (Selection)
    if (isTempSelected(idx)) {
        return 'border-indigo-500 bg-indigo-50 text-indigo-700';
    }

    return 'border-slate-200 bg-white text-slate-600 hover:bg-slate-50';
}

function getOptionNumberClass(quiz: Quiz, idx: number) {
    if (isCorrectOption(quiz, idx)) {
        return 'bg-emerald-500 text-white';
    }
    if (isWrongSelection(quiz, idx)) {
        return 'bg-rose-500 text-white';
    }
    if (isUnsubmitted(quiz) && isTempSelected(idx)) {
         return 'bg-indigo-500 text-white';
    }
    return 'bg-slate-100 text-slate-400';
}

function getOptionTextClass(quiz: Quiz, idx: number) {
    if (isCorrectOption(quiz, idx)) {
        return 'text-emerald-700';
    }
    if (isWrongSelection(quiz, idx)) {
        return 'text-rose-700';
    }
    if (isUnsubmitted(quiz) && isTempSelected(idx)) {
        return 'text-indigo-700 font-bold';
    }
    return 'text-slate-500';
}
</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar {
  width: 4px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background-color: #cbd5e1;
  border-radius: 4px;
}
</style>