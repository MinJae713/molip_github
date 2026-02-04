<template>
    <Teleport to="body">
        <Transition name="fade">
            <div v-if="isOpen"
                class="fixed inset-0 z-100 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-sm">
                <div
                    class="bg-white rounded-[2.5rem] w-full max-w-2xl max-h-[85vh] overflow-hidden shadow-2xl flex flex-col">
                    <!-- 헤더 -->
                    <div class="p-8 flex justify-between items-start border-b border-slate-100">
                        <div class="flex items-center gap-4">
                            <div
                                class="w-12 h-12 bg-purple-50 rounded-2xl flex items-center justify-center text-purple-600">
                                <span class="material-symbols-outlined text-2xl">bolt</span>
                            </div>
                            <div>
                                <h2 class="text-xl font-bold text-slate-900">퀵 퀴즈 확인</h2>
                                <p class="text-sm text-slate-500 mt-1">AI가 생성한 퀴즈를 확인하고 학생들에게 전달하세요.</p>
                            </div>
                        </div>
                        <button @click="handleClose" class="p-2 hover:bg-slate-100 rounded-full transition-colors">
                            <span class="material-symbols-outlined text-slate-400">close</span>
                        </button>
                    </div>

                    <!-- 퀴즈 내용 -->
                    <div class="flex-1 overflow-y-auto p-8 custom-scrollbar">
                        <div class="space-y-6">
                            <!-- 퀴즈 질문 -->
                            <div>
                                <label class="text-xs font-bold text-slate-600 mb-2 block">질문</label>
                                <textarea v-model="localQuizData.question"
                                    class="w-full text-sm font-bold text-slate-800 bg-slate-50 border border-slate-200 rounded-xl p-4 focus:outline-none focus:ring-2 focus:ring-purple-500/20 focus:border-purple-400 transition-all resize-none"
                                    rows="3" placeholder="퀴즈 질문을 입력하세요..."></textarea>
                            </div>

                            <!-- 4지선다 (고정) -->
                            <div class="space-y-3">
                                <label class="text-xs font-bold text-slate-600 block">선택지 (4지선다)</label>
                                <div v-for="idx in 4" :key="idx" class="flex items-center gap-3">
                                    <input type="radio" name="correct-answer"
                                        :checked="localQuizData.correctAnswer === (idx - 1)"
                                        @change="localQuizData.correctAnswer = idx - 1"
                                        class="w-5 h-5 cursor-pointer accent-green-500">
                                    <input v-model="localQuizData[`answer${idx}`]"
                                        class="flex-1 text-sm bg-white border border-slate-200 rounded-lg px-3 py-2 focus:border-purple-400 focus:outline-none"
                                        :placeholder="`선택지 ${idx}`" />
                                </div>
                            </div>

                            <!-- 해설 (옵션) -->
                            <div>
                                <label class="text-xs font-bold text-slate-600 mb-2 block">해설 (선택사항)</label>
                                <textarea v-model="localQuizData.explanation"
                                    class="w-full text-sm text-slate-700 bg-slate-50 border border-slate-200 rounded-xl p-4 focus:outline-none focus:ring-2 focus:ring-purple-500/20 focus:border-purple-400 transition-all resize-none"
                                    rows="2" placeholder="정답에 대한 해설을 입력하세요... (선택사항)"></textarea>
                            </div>
                        </div>
                    </div>

                    <!-- 푸터 -->
                    <div class="p-6 bg-slate-50 border-t border-slate-100 flex items-center justify-end gap-3">
                        <button @click="handleClose"
                            class="px-6 py-3 bg-white border border-slate-200 text-slate-700 rounded-xl text-sm font-bold hover:bg-slate-50 transition-all">
                            취소
                        </button>
                        <button @click="handleSendToStudents"
                            class="px-8 py-3 bg-purple-600 text-white rounded-xl text-sm font-bold shadow-lg shadow-purple-200 hover:bg-purple-700 transition-all flex items-center gap-2">
                            <span class="material-symbols-outlined text-lg">send</span>
                            학생들에게 전달
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
    'close': [],
    'send-to-students': [quiz: any]
}>();

// 로컬 수정 가능한 퀴즈 데이터
const localQuizData = ref<any>({
    question: '',
    answer1: '',
    answer2: '',
    answer3: '',
    answer4: '',
    correctAnswer: 0,
    explanation: ''
});

// quizData가 변경되면 로컬에 복사
watch(() => props.quizData, (newData) => {
    if (newData) {
        // 백엔드 DTO 형식으로 복사
        localQuizData.value = {
            question: newData.question || '',
            answer1: newData.answer1 || '',
            answer2: newData.answer2 || '',
            answer3: newData.answer3 || '',
            answer4: newData.answer4 || '',
            correctAnswer: newData.correctAnswer ?? 0,
            explanation: newData.explanation || ''
        };
    }
}, { immediate: true });


// 닫기
const handleClose = () => {
    emit('close');
};

// 학생들에게 전달
const handleSendToStudents = () => {
    if (!localQuizData.value?.question?.trim()) {
        alert('질문을 입력해주세요.');
        return;
    }

    // 4개 선택지 모두 입력되었는지 확인
    if (!localQuizData.value.answer1?.trim() ||
        !localQuizData.value.answer2?.trim() ||
        !localQuizData.value.answer3?.trim() ||
        !localQuizData.value.answer4?.trim()) {
        alert('모든 선택지를 입력해주세요.');
        return;
    }

    // 정답 선택 확인
    if (localQuizData.value.correctAnswer === undefined || localQuizData.value.correctAnswer === null) {
        alert('정답을 선택해주세요.');
        return;
    }

    // 백엔드 DTO 형식으로 전송
    const quizPayload = {
        question: localQuizData.value.question,
        answer1: localQuizData.value.answer1,
        answer2: localQuizData.value.answer2,
        answer3: localQuizData.value.answer3,
        answer4: localQuizData.value.answer4,
        correctAnswer: localQuizData.value.correctAnswer,
        explanation: localQuizData.value.explanation || ''
    };

    emit('send-to-students', quizPayload);
    emit('close');
};
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

.custom-scrollbar::-webkit-scrollbar {
    width: 6px;
}

.custom-scrollbar::-webkit-scrollbar-track {
    background: transparent;
}

.custom-scrollbar::-webkit-scrollbar-thumb {
    background: #e2e8f0;
    border-radius: 10px;
}

.custom-scrollbar::-webkit-scrollbar-thumb:hover {
    background: #cbd5e1;
}
</style>
