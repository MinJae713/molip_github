import { defineStore } from 'pinia';
import { ref } from 'vue';
import type { QuizItem } from '@/types';

export const useQuizStore = defineStore('quiz', () => {
    // 1. 상태 (State)
    const isQuizModalOpen = ref(false);
    const selectedQuizzes = ref<QuizItem[]>([]);

    const recommendedQuizzes = ref<QuizItem[]>([
        {
            id: 1,
            timeRange: '14:20~16:05',
            reason: '구간 집중도 하락 기반',
            question: '디지털 비즈니스 전략의 3단계 중 "전환" 단계의 핵심 지표는?',
            options: ['고객 전환율', '데이터 자산화 비중'],
            type: 'single',
            isEditing: false // 초기값 추가
        },
        {
            id: 2,
            timeRange: '28:45~30:12',
            reason: '구간 대규모 이탈 기반',
            question: '네트워크 효과가 발생하는 최소한의 임계점은?',
            type: 'subjective',
            isEditing: false // 초기값 추가
        }
    ]);

    // 2. 액션 (Actions)
    const toggleModal = (state: boolean) => {
        isQuizModalOpen.value = state;
    };

    const addQuiz = (quiz: QuizItem) => {
        if (selectedQuizzes.value.length < 5 && !selectedQuizzes.value.find(q => q.id === quiz.id)) {
            // 원본 객체가 오염되지 않도록 복사해서 추가
            selectedQuizzes.value.push({ ...quiz, isEditing: false });
        }
    };

    // [중요] 누락되었던 직접 추가 함수
    const addEmptyQuiz = () => {
        if (selectedQuizzes.value.length < 5) {
            selectedQuizzes.value.push({
                id: Date.now(), // 고유 ID 생성
                question: '',
                type: 'subjective',
                isEditing: true // 추가 시 바로 편집 모드
            });
        }
    };

    const removeQuiz = (id: number) => {
        selectedQuizzes.value = selectedQuizzes.value.filter(q => q.id !== id);
    };

    const resetSelected = () => {
        selectedQuizzes.value = [];
    };

    // 3. 반환 (Return) - 여기에 모든 함수와 변수가 포함되어야 함
    return {
        isQuizModalOpen,
        recommendedQuizzes,
        selectedQuizzes,
        toggleModal,
        addQuiz,
        addEmptyQuiz, // <-- 컴포넌트에서 쓰기 위해 반드시 포함
        removeQuiz,
        resetSelected
    };
});