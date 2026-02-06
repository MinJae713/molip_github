<template>
    <div v-if="isOpen" class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm p-4">
        <div
            class="bg-white rounded-2xl w-full max-w-2xl max-h-[85vh] flex flex-col shadow-2xl overflow-hidden relative">

            <!-- 헤더 -->
            <div class="px-6 py-4 border-b border-slate-100 flex items-center justify-between bg-slate-50">
                <div>
                    <h2 class="text-lg font-bold text-slate-900 flex items-center gap-2">
                        <span class="material-symbols-outlined text-indigo-600">quiz</span>
                        Daily Review Quiz
                    </h2>
                    <p class="text-xs text-slate-500 mt-0.5">오늘 배운 내용을 간단하게 복습하고 퇴실하세요!</p>
                </div>
                <!-- 닫기 버튼 (결과 확인 후 활성화 or 강제 종료용) -->
                <button v-if="isSubmitted" @click="handleClose" class="text-slate-400 hover:text-slate-600">
                    <span class="material-symbols-outlined">close</span>
                </button>
            </div>

            <!-- 로딩 상태 -->
            <div v-if="isLoading" class="flex-1 flex flex-col items-center justify-center p-12">
                <div class="size-10 border-4 border-slate-200 border-t-indigo-600 rounded-full animate-spin"></div>
                <p class="mt-4 text-slate-500 text-sm font-medium">퀴즈를 불러오는 중입니다...</p>
            </div>

            <!-- 퀴즈 컨텐츠 -->
            <div v-else class="flex-1 overflow-y-auto p-6 space-y-8 custom-scrollbar">
                <div v-for="(quiz, index) in quizzes" :key="quiz.id"
                    class="rounded-xl border border-slate-200 bg-slate-50/50 p-5 transition-all duration-300"
                    :class="{ 'ring-2 ring-indigo-50 border-indigo-200 bg-white shadow-sm': isCurrent(index) }">

                    <!-- 질문 -->
                    <div class="flex items-start gap-4 mb-4">
                        <span
                            class="flex-shrink-0 size-8 rounded-lg bg-indigo-600 text-white flex items-center justify-center font-bold text-sm shadow-sm selection-none">
                            Q{{ index + 1 }}
                        </span>
                        <div class="flex-1">
                            <h3 class="font-bold text-slate-900 text-lg leading-snug">{{ quiz.question }}</h3>
                        </div>
                        <!-- 채점 결과 아이콘 (제출 후) -->
                        <div v-if="isSubmitted" class="flex-shrink-0">
                            <span v-if="quiz.isCorrect"
                                class="text-emerald-500 material-symbols-outlined text-3xl animate-bounce-short">check_circle</span>
                            <span v-else
                                class="text-red-500 material-symbols-outlined text-3xl animate-shake">cancel</span>
                        </div>
                    </div>

                    <!-- 보기 (객관식) -->
                    <div class="grid grid-cols-1 gap-2.5 pl-12 pr-2">
                        <button v-for="(option, optIdx) in quiz.options" :key="optIdx"
                            @click="!isSubmitted && handleSelect(quiz.id, optIdx)" :disabled="isSubmitted"
                            class="w-full text-left px-4 py-3 rounded-xl border transition-all duration-200 flex items-center gap-3 relative group"
                            :class="getOptionClass(quiz, optIdx)">
                            <!-- 번호/체크 -->
                            <div class="size-6 rounded-full border flex items-center justify-center text-xs font-bold transition-colors"
                                :class="getOptionInitialClass(quiz, optIdx)">
                                {{ ['A', 'B', 'C', 'D'][optIdx] }}
                            </div>

                            <span class="text-sm font-medium transition-colors" :class="getTextClass(quiz, optIdx)">{{
                                option }}</span>
                        </button>
                    </div>

                    <!-- 정답 해설 (오답일 경우) -> API에 해설 필드가 없다면 생략, 여기선 정답 표시만 -->
                    <div v-if="isSubmitted && !quiz.isCorrect"
                        class="mt-4 ml-12 p-3 bg-red-50 rounded-lg border border-red-100 text-xs text-red-600 flex items-start gap-2">
                        <span class="material-symbols-outlined text-sm mt-0.5">info</span>
                        <div>
                            <span class="font-bold">정답:</span> {{ quiz.options?.[quiz.correctAnswer?.[0] || 0] }}
                        </div>
                    </div>
                </div>
            </div>

            <!-- 하단 액션 버튼 -->
            <div class="p-4 border-t border-slate-100 bg-white flex justify-end gap-3 rounded-b-2xl">
                <template v-if="!isSubmitted">
                    <div class="flex-1 flex items-center text-xs text-slate-400 px-2">
                        <span class="material-symbols-outlined text-sm mr-1">check_circle</span>
                        {{ answeredCount }} / {{ quizzes.length }} 문제 완료
                    </div>
                    <button @click="handleSubmit"
                        class="px-8 py-2.5 bg-indigo-600 text-white font-bold rounded-xl shadow-md hover:bg-indigo-700 hover:shadow-lg disabled:opacity-50 disabled:cursor-not-allowed transition-all transform active:scale-95 flex items-center gap-2"
                        :disabled="answeredCount < quizzes.length || isSubmitting">
                        <span v-if="isSubmitting"
                            class="size-4 border-2 border-white/30 border-t-white rounded-full animate-spin"></span>
                        {{ isSubmitting ? '제출 중...' : '제출하기' }}
                    </button>
                </template>

                <template v-else>
                    <div class="flex-1 flex items-center gap-2 px-2">
                        <div class="text-sm font-bold text-slate-900">
                            총점: <span class="text-indigo-600 text-lg">{{ score }}점</span>
                        </div>
                        <span class="text-xs text-slate-400">({{ correctCount }} / {{ quizzes.length }} 정답)</span>
                    </div>
                    <button @click="handleClose"
                        class="px-6 py-2.5 bg-slate-900 text-white font-bold rounded-xl hover:bg-slate-800 transition-colors shadow-sm">
                        확인 완료
                    </button>
                </template>
            </div>

        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue';
import { useQuizStore } from '@/stores/quiz';
import { storeToRefs } from 'pinia';
import type { QuizItem } from '@/types';

const props = defineProps<{
    isOpen: boolean;
    sessionId: string | number;
}>();

const emit = defineEmits(['close']);

const quizStore = useQuizStore();
const { selectedQuizzes } = storeToRefs(quizStore);

const quizzes = computed(() => selectedQuizzes.value);
const isLoading = ref(false);
const isSubmitting = ref(false);
const isSubmitted = ref(false);

const answeredCount = computed(() => {
    return quizzes.value.filter(q => q.userAnswer && q.userAnswer.length > 0).length;
});

const correctCount = computed(() => {
    return quizzes.value.filter(q => q.isCorrect).length;
});

const score = computed(() => {
    if (quizzes.value.length === 0) return 0;
    return Math.round((correctCount.value / quizzes.value.length) * 100);
});

// 초기화 및 퀴즈 불러오기
onMounted(async () => {
    if (props.isOpen) {
        loadQuizzes();
    }
});

watch(() => props.isOpen, (newVal) => {
    if (newVal) {
        loadQuizzes();
    } else {
        // 닫힐 때 상태 초기화
        isSubmitted.value = false;
        isSubmitting.value = false;
    }
});

const loadQuizzes = async () => {
    isLoading.value = true;
    try {
        await quizStore.fetchExitQuiz(props.sessionId);
    } catch (e) {
        console.error(e);
        // alert("퀴즈를 불러오는데 실패했습니다.");
    } finally {
        isLoading.value = false;
    }
}

const handleSelect = (quizId: number, optionIdx: number) => {
    // 단일 선택 (single)
    quizStore.toggleUserAnswer(quizId, optionIdx); // store action uses 'value', but here we store index. Store logic might need index or value.
    // Wait, store toggleUserAnswer signature: (id: number, value: string | number).
    // fetchExitQuiz in store sets type 'single'.
    // We need to verify if store expects 'index' or 'string'.
    // Looking at store: toggleUserAnswer stores value in array.
    // Looking at submitExitQuiz: Number(q.userAnswer[0]) + 1. So it expects 0-based index.
    // So passing optionIdx is correct.
};

const handleSubmit = async () => {
    if (answeredCount.value < quizzes.value.length) return;

    isSubmitting.value = true;
    try {
        const results = await quizStore.submitExitQuiz(props.sessionId);
        // results: [{ exitQuizId: number, result: boolean }, ...]

        // 결과 매핑
        results.forEach((res: any) => {
            const quiz = quizzes.value.find(q => q.id === res.exitQuizId);
            if (quiz) {
                quiz.isCorrect = res.result;
            }
        });

        isSubmitted.value = true;
    } catch (e) {
        console.error(e);
        alert("제출에 실패했습니다.");
    } finally {
        isSubmitting.value = false;
    }
};

const handleClose = () => {
    emit('close');
};

// --- Styling Helpers ---

const isCurrent = (index: number) => {
    // 가장 먼저 안 푼 문제 하이라이트 or 그냥 마우스 호버로 처리
    return false;
};

const getOptionClass = (quiz: QuizItem, optIdx: number) => {
    const isSelected = quiz.userAnswer?.includes(optIdx);

    if (isSubmitted.value) {
        // 결과 모드
        // 정답(API 응답이 아니라, 로컬에서 정답을 알고 있다면 표시 가능하지만, 
        // fetchExitQuiz에서 correctAnswer를 받긴 함)
        const isAnswer = quiz.correctAnswer?.includes(optIdx);

        if (isSelected && isAnswer) return 'bg-emerald-50 border-emerald-200 ring-1 ring-emerald-300';
        if (isSelected && !isAnswer) return 'bg-red-50 border-red-200 ring-1 ring-red-300';
        if (!isSelected && isAnswer) return 'bg-emerald-50 border-emerald-200 border-dashed opacity-70'; // 정답 표시
        return 'bg-white border-slate-200 opacity-50';
    } else {
        // 풀이 모드
        if (isSelected) return 'bg-indigo-50 border-indigo-200 ring-1 ring-indigo-300 shadow-sm z-10';
        return 'bg-white border-slate-200 hover:border-indigo-200 hover:bg-slate-50';
    }
}

const getOptionInitialClass = (quiz: QuizItem, optIdx: number) => {
    const isSelected = quiz.userAnswer?.includes(optIdx);

    if (isSubmitted.value) {
        const isAnswer = quiz.correctAnswer?.includes(optIdx);
        if (isSelected && isAnswer) return 'bg-emerald-500 border-emerald-500 text-white';
        if (isSelected && !isAnswer) return 'bg-red-500 border-red-500 text-white';
        if (!isSelected && isAnswer) return 'bg-emerald-100 border-emerald-200 text-emerald-600';
        return 'border-slate-200 text-slate-400';
    } else {
        if (isSelected) return 'bg-indigo-600 border-indigo-600 text-white';
        return 'border-slate-200 text-slate-400 group-hover:border-indigo-300 group-hover:text-indigo-400';
    }
}

const getTextClass = (quiz: QuizItem, optIdx: number) => {
    const isSelected = quiz.userAnswer?.includes(optIdx);
    if (isSubmitted.value) {
        const isAnswer = quiz.correctAnswer?.includes(optIdx);
        if (isSelected && isAnswer) return 'text-emerald-700 font-bold';
        if (isSelected && !isAnswer) return 'text-red-700 font-medium line-through decoration-red-300';
        if (!isSelected && isAnswer) return 'text-emerald-600 font-bold';
        return 'text-slate-400';
    } else {
        if (isSelected) return 'text-indigo-700 font-bold';
        return 'text-slate-600 group-hover:text-slate-900';
    }
}

</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar {
    width: 6px;
}

.custom-scrollbar::-webkit-scrollbar-track {
    background: transparent;
}

.custom-scrollbar::-webkit-scrollbar-thumb {
    background-color: #cbd5e1;
    border-radius: 10px;
    border: 2px solid #f8fafc;
}

@keyframes bounce-short {

    0%,
    100% {
        transform: translateY(0);
    }

    50% {
        transform: translateY(-15%);
    }
}

.animate-bounce-short {
    animation: bounce-short 0.5s ease-in-out 1;
}

@keyframes shake {

    0%,
    100% {
        transform: translateX(0);
    }

    25% {
        transform: translateX(-4px);
    }

    75% {
        transform: translateX(4px);
    }
}

.animate-shake {
    animation: shake 0.4s ease-in-out 1;
}
</style>
