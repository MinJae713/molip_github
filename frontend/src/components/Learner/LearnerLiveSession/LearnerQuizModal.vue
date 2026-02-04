<template>
    <Teleport to="body">
        <Transition name="fade">
            <div v-if="isOpen"
                class="fixed inset-0 z-[100] flex items-center justify-center p-4 bg-slate-900/70 backdrop-blur-md">
                <div class="bg-white rounded-3xl w-full max-w-lg max-h-[90vh] overflow-hidden shadow-2xl flex flex-col border-2"
                    :class="isSubmitted ? (isCorrect ? 'border-green-500' : 'border-red-500') : 'border-indigo-500'">

                    <!-- 헤더 -->
                    <div class="p-6 bg-gradient-to-r from-indigo-500 to-purple-600 text-white">
                        <div class="flex items-center justify-between mb-2">
                            <div class="flex items-center gap-3">
                                <span class="text-3xl">🎯</span>
                                <h2 class="text-xl font-bold">Surprise Quiz!</h2>
                            </div>
                            <div v-if="timeRemaining !== null"
                                class="flex items-center gap-2 bg-white/20 px-3 py-1 rounded-full">
                                <span class="material-symbols-outlined text-lg">timer</span>
                                <span class="font-mono font-bold">{{ timeRemaining }}s</span>
                            </div>
                        </div>
                        <p class="text-indigo-50 text-sm">트레이너가 보낸 퀴즈입니다. 정답을 맞춰보세요!</p>
                    </div>

                    <!-- 퀴즈 내용 -->
                    <div class="flex-1 overflow-y-auto p-6 space-y-6">
                        <!-- 질문 -->
                        <div class="bg-slate-50 rounded-2xl p-4">
                            <p class="text-base font-bold text-slate-900 leading-relaxed">
                                {{ localQuizData?.question || '질문을 불러오는 중...' }}
                            </p>
                        </div>
                        <!-- 4지선다 객관식 -->
                        <div class="space-y-3">
                            <p class="text-xs font-bold text-slate-600 mb-2">선택지를 골라주세요</p>
                            <button v-for="idx in 4" :key="idx" @click="selectAnswer(idx - 1)" :disabled="isSubmitted"
                                class="w-full text-left p-4 rounded-xl border-2 transition-all"
                                :class="getOptionClass(idx - 1)">
                                <div class="flex items-center justify-between">
                                    <span class="text-sm font-medium">{{ localQuizData[`answer${idx}`] || `선택지 ${idx}`
                                    }}</span>
                                    <span v-if="userAnswer === (idx - 1)"
                                        class="material-symbols-outlined text-indigo-600">
                                        check_circle
                                    </span>
                                </div>
                            </button>
                        </div>

                        <!-- 결과 표시 -->
                        <Transition name="slide-up">
                            <div v-if="isSubmitted" class="rounded-2xl p-4 border-2"
                                :class="isCorrect ? 'bg-green-50 border-green-500' : 'bg-red-50 border-red-500'">
                                <div class="flex items-center gap-3 mb-2">
                                    <span class="text-3xl">{{ isCorrect ? '🎉' : '😅' }}</span>
                                    <p class="text-lg font-bold" :class="isCorrect ? 'text-green-700' : 'text-red-700'">
                                        {{ isCorrect ? '정답입니다!' : '아쉽네요!' }}
                                    </p>
                                </div>
                                <div v-if="!isCorrect && localQuizData?.correctAnswer !== undefined"
                                    class="text-sm text-slate-700">
                                    정답: <span class="font-bold">
                                        {{ localQuizData[`answer${localQuizData.correctAnswer + 1}`] }}
                                    </span>
                                </div>
                                <div v-if="localQuizData?.explanation" class="mt-3 pt-3 border-t border-slate-200">
                                    <p class="text-xs text-slate-600">{{ localQuizData.explanation }}</p>
                                </div>
                            </div>
                        </Transition>
                    </div>

                    <!-- 푸터 -->
                    <div class="p-4 bg-slate-50 border-t border-slate-100">
                        <button v-if="!isSubmitted" @click="submitAnswer"
                            class="w-full py-3 bg-indigo-600 text-white rounded-xl text-sm font-bold shadow-lg hover:bg-indigo-700 transition-all flex items-center justify-center gap-2">
                            <span class="material-symbols-outlined">check</span>
                            제출하기
                        </button>
                        <button v-else @click="handleClose"
                            class="w-full py-3 bg-slate-600 text-white rounded-xl text-sm font-bold hover:bg-slate-700 transition-all">
                            닫기
                        </button>
                    </div>
                </div>
            </div>
        </Transition>
    </Teleport>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';

const props = defineProps<{
    isOpen: boolean,
    quizData: any
}>();

const emit = defineEmits<{
    'close': []
}>();

const localQuizData = ref<any>({
    question: '',
    answer1: '',
    answer2: '',
    answer3: '',
    answer4: '',
    correctAnswer: 0,
    explanation: ''
});
const userAnswer = ref<number | null>(null);
const isSubmitted = ref(false);
const isCorrect = ref(false);
const timeRemaining = ref<number | null>(null);
let timerInterval: number | null = null;

// quizData 변경 시 초기화
watch(() => props.quizData, (newData) => {
    if (newData) {
        // 백엔드 DTO 형식으로 직접 저장
        localQuizData.value = {
            question: newData.question || '',
            answer1: newData.answer1 || '',
            answer2: newData.answer2 || '',
            answer3: newData.answer3 || '',
            answer4: newData.answer4 || '',
            correctAnswer: newData.correctAnswer ?? 0,
            explanation: newData.explanation || ''
        };

        userAnswer.value = null;
        isSubmitted.value = false;
        isCorrect.value = false;

        // 타이머 설정
        if (newData.timeLimit && newData.timeLimit > 0) {
            timeRemaining.value = newData.timeLimit;
            startTimer();
        } else {
            timeRemaining.value = null;
        }
    }
}, { immediate: true });

// 타이머 시작
const startTimer = () => {
    if (timerInterval) clearInterval(timerInterval);
    timerInterval = window.setInterval(() => {
        if (timeRemaining.value !== null && timeRemaining.value > 0) {
            timeRemaining.value--;
        } else {
            clearInterval(timerInterval!);
            if (!isSubmitted.value) {
                submitAnswer(); // 시간 초과 시 자동 제출
            }
        }
    }, 1000);
};

// 답안 선택
const selectAnswer = (idx: number) => {
    if (!isSubmitted.value) {
        userAnswer.value = idx;
    }
};

// 답안 제출
const submitAnswer = () => {
    if (userAnswer.value === null) {
        alert('답을 선택하세요');
        return;
    }
    isSubmitted.value = true;
    // 정답 체크 (인덱스 비교)
    isCorrect.value = userAnswer.value === localQuizData.value.correctAnswer;
    // 타이머 정지
    if (timerInterval) {
        clearInterval(timerInterval);
    }
};

// 옵션 스타일
const getOptionClass = (idx: number) => {
    if (!isSubmitted.value) {
        return userAnswer.value === idx
            ? 'border-indigo-600 bg-indigo-50'
            : 'border-slate-200 hover:border-indigo-300 bg-white';
    } else {
        if (idx === localQuizData.value?.correctAnswer) {
            return 'border-green-500 bg-green-50';
        } else if (idx === userAnswer.value) {
            return 'border-red-500 bg-red-50';
        } else {
            return 'border-slate-200 bg-slate-100 opacity-50';
        }
    }
};

// 닫기
const handleClose = () => {
    if (timerInterval) clearInterval(timerInterval);
    emit('close');
};

// 컴포넌트 언마운트 시 타이머 정리
watch(() => props.isOpen, (newVal) => {
    if (!newVal && timerInterval) {
        clearInterval(timerInterval);
    }
});
</script>

<style scoped>
.fade-enter-active,
.fade-leave-active {
    transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
    opacity: 0;
}

.slide-up-enter-active {
    transition: all 0.4s ease-out;
}

.slide-up-enter-from {
    opacity: 0;
    transform: translateY(20px);
}
</style>
