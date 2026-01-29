import { defineStore } from 'pinia';
import { ref } from 'vue';
import type { QuizItem } from '@/types';

export const useQuizStore = defineStore('quiz', () => {
  // --- [1] 상태 (State) ---
  const isQuizModalOpen = ref(false);
  const selectedQuizzes = ref<QuizItem[]>([]);
  
  // AI 추천 문항 (정답 데이터 포함)
  const recommendedQuizzes = ref<QuizItem[]>([
    {
      id: 1,
      timeRange: '14:20~16:05',
      reason: '구간 집중도 하락 기반',
      question: '디지털 비즈니스 전략의 3단계 중 "전환" 단계의 핵심 지표는?',
      options: ['고객 전환율', '데이터 자산화 비중'],
      type: 'single',
      correctAnswer: [0], // 인덱스로 관리
      userAnswer: [],
      isEditing: false
    },
    {
      id: 2,
      timeRange: '28:45~30:12',
      reason: '구간 대규모 이탈 기반',
      question: '네트워크 효과가 발생하는 최소한의 임계점은?',
      type: 'subjective',
      correctAnswer: ['임계 질량'], // 텍스트로 관리
      userAnswer: [],
      isEditing: false
    }
  ]);

  // --- [2] 액션 (Actions) ---

  const toggleModal = (state: boolean) => {
    isQuizModalOpen.value = state;
  };

  /** [강사용] AI 추천 문항을 최종 리스트에 추가 */
	const addQuiz = (quiz: QuizItem) => {
		// 1. 최대 5개 제한 확인
		if (selectedQuizzes.value.length >= 5) {
			alert("최대 5개까지만 구성할 수 있습니다.");
			return;
		}

		// 2. 질문 내용(question)이 이미 리스트에 있는지 확인 (ID가 아닌 내용 기준)
		const isDuplicate = selectedQuizzes.value.some(q => q.question === quiz.question);

		if (!isDuplicate) {
			selectedQuizzes.value.push({ 
				...JSON.parse(JSON.stringify(quiz)), 
				id: Date.now() + Math.random(), 
				isEditing: false 
			});
		} else {
			alert("이미 추가된 문항입니다.");
		}
	};

  /** [강사용] 빈 문항 직접 추가 */
  const addEmptyQuiz = () => {
    if (selectedQuizzes.value.length < 5) {
      selectedQuizzes.value.push({
        id: Date.now(),
        question: '',
        type: 'single', // 기본값 객관식
        options: ['', ''],
        correctAnswer: [],
        userAnswer: [],
        isEditing: true // 추가 시 바로 편집 모드
      });
    }
  };

  /** [강사용] 특정 퀴즈의 편집 모드 토글 (컴포넌트에서 호출) */
  const toggleEdit = (id: number) => {
    const quiz = selectedQuizzes.value.find(q => q.id === id);
    if (quiz) {
      quiz.isEditing = !quiz.isEditing;
    }
  };

  /** [강사용] 정답 설정 (편집 모드에서 선택 시) */
  const updateCorrectAnswer = (id: number, value: string | number) => {
    const quiz = selectedQuizzes.value.find(q => q.id === id);
    if (!quiz) return;
    if (!quiz.correctAnswer) quiz.correctAnswer = [];

    if (quiz.type === 'multiple') {
      const index = quiz.correctAnswer.indexOf(value);
      index > -1 ? quiz.correctAnswer.splice(index, 1) : quiz.correctAnswer.push(value);
    } else {
      quiz.correctAnswer = [value]; // 단일 선택 및 주관식
    }
  };

  /** [수강생용] 응답 선택 */
  const toggleUserAnswer = (id: number, value: string | number) => {
    const quiz = selectedQuizzes.value.find(q => q.id === id);
    if (!quiz) return;
    if (!quiz.userAnswer) quiz.userAnswer = [];

    if (quiz.type === 'multiple') {
      const index = quiz.userAnswer.indexOf(value);
      index > -1 ? quiz.userAnswer.splice(index, 1) : quiz.userAnswer.push(value);
    } else {
      quiz.userAnswer = [value];
    }
  };

  /** [공통] 채점 로직 */
  const gradeQuiz = (id: number) => {
    const quiz = selectedQuizzes.value.find(q => q.id === id);
    if (!quiz || !quiz.userAnswer || !quiz.correctAnswer) return false;

    const isCorrect = 
      quiz.correctAnswer.length === quiz.userAnswer.length &&
      quiz.correctAnswer.every(val => quiz.userAnswer!.includes(val));
    
    quiz.isCorrect = isCorrect;
    return isCorrect;
  };

  const removeQuiz = (id: number) => {
    selectedQuizzes.value = selectedQuizzes.value.filter(q => q.id !== id);
  };

  const resetSelected = () => {
    selectedQuizzes.value = [];
  };

  return {
    isQuizModalOpen,
    recommendedQuizzes,
    selectedQuizzes,
    toggleModal,
    addQuiz,
    addEmptyQuiz,
    toggleEdit, // 추가됨
    updateCorrectAnswer,
    toggleUserAnswer,
    gradeQuiz,
    removeQuiz,
    resetSelected
  };
});