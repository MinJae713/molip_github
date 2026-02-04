import { defineStore } from "pinia";
import { ref } from "vue";
import axios from "axios";
import type { QuizItem } from "@/types";

const videoApi = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 5000,
});

videoApi.interceptors.request.use((config) => {
  const token = localStorage.getItem("accessToken");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export const useQuizStore = defineStore("quiz", () => {
  // --- [1] 상태 (State) ---
  const isQuizModalOpen = ref(false);
  const selectedQuizzes = ref<QuizItem[]>([]);

  // AI 추천 문항은 더 이상 사용하지 않음 (직접 추가만 가능)
  const recommendedQuizzes = ref<QuizItem[]>([]);

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
    const isDuplicate = selectedQuizzes.value.some(
      (q) => q.question === quiz.question,
    );

    if (!isDuplicate) {
      selectedQuizzes.value.push({
        ...JSON.parse(JSON.stringify(quiz)),
        id: Date.now() + Math.random(),
        isEditing: false,
      });
    } else {
      alert("이미 추가된 문항입니다.");
    }
  };

  /** [강사용] 빈 문항 직접 추가 (4지선다 고정) */
  const addEmptyQuiz = () => {
    if (selectedQuizzes.value.length < 5) {
      selectedQuizzes.value.push({
        id: Date.now(),
        question: "",
        type: "single", // 무조건 객관식
        options: ["", "", "", ""], // 4지선다 고정
        correctAnswer: [0], // 기본 정답은 1번
        userAnswer: [],
        isEditing: true, // 추가 시 바로 편집 모드
      });
    }
  };

  /** [강사용] 특정 퀴즈의 편집 모드 토글 (컴포넌트에서 호출) */
  const toggleEdit = (id: number) => {
    const quiz = selectedQuizzes.value.find((q) => q.id === id);
    if (quiz) {
      quiz.isEditing = !quiz.isEditing;
    }
  };

  /** [강사용] 정답 설정 (4지선다용 단일 선택) */
  const updateCorrectAnswer = (id: number, value: number) => {
    const quiz = selectedQuizzes.value.find((q) => q.id === id);
    if (!quiz) return;
    quiz.correctAnswer = [value];
  };

  /** [수강생용] 응답 선택 */
  const toggleUserAnswer = (id: number, value: string | number) => {
    const quiz = selectedQuizzes.value.find((q) => q.id === id);
    if (!quiz) return;
    if (!quiz.userAnswer) quiz.userAnswer = [];

    if (quiz.type === "multiple") {
      const index = quiz.userAnswer.indexOf(value);
      index > -1
        ? quiz.userAnswer.splice(index, 1)
        : quiz.userAnswer.push(value);
    } else {
      quiz.userAnswer = [value];
    }
  };

  /** [공통] 채점 로직 */
  const gradeQuiz = (id: number) => {
    const quiz = selectedQuizzes.value.find((q) => q.id === id);
    if (!quiz || !quiz.userAnswer || !quiz.correctAnswer) return false;

    const isCorrect =
      quiz.correctAnswer.length === quiz.userAnswer.length &&
      quiz.correctAnswer.every((val) => quiz.userAnswer!.includes(val));

    quiz.isCorrect = isCorrect;
    return isCorrect;
  };

  const removeQuiz = (id: number) => {
    selectedQuizzes.value = selectedQuizzes.value.filter((q) => q.id !== id);
  };

  const resetSelected = () => {
    selectedQuizzes.value = [];
  };

  /** [강사용] 퇴실 퀴즈 서버 저장 (REST API) */
  const publishExitQuiz = async (sessionId: string | number) => {
    try {
      // 백엔드 명세에 맞게 데이터 변환
      const payload = selectedQuizzes.value.map((q) => ({
        question: q.question,
        answerNumber: Number(q.correctAnswer[0]) + 1, // 1-기반 인덱스
        exitQuizAnswers: (q.options || []).map((opt, idx) => ({
          number: idx + 1,
          answer: opt,
        })),
      }));

      const res = await videoApi.post(`/quiz/${sessionId}/exit`, payload);
      return res.data;
    } catch (error) {
      console.error("퇴실 퀴즈 저장 실패:", error);
      throw error;
    }
  };

  /** [수강생용] 퇴실 퀴즈 서버 조회 (REST API) */
  const fetchExitQuiz = async (sessionId: string | number) => {
    try {
      const res = await videoApi.get(`/quiz/${sessionId}/exit`);
      // 백엔드 데이터(ExitQuizDTO[])를 프론트엔드 QuizItem[] 형식으로 매핑
      const mappedQuizzes: QuizItem[] = res.data.map((q: any) => ({
        id: q.exitQuizId,
        question: q.question,
        type: "single", // 퇴실 퀴즈는 무조건 객관식(single)
        options: q.exitQuizAnswers
          .sort((a: any, b: any) => a.number - b.number) // 번호 순서대로 정렬
          .map((a: any) => a.answer),
        correctAnswer: [q.answerNumber - 1], // 백엔드(1-based) -> 프론트(0-based)
        userAnswer: [],
        isEditing: false,
      }));
      selectedQuizzes.value = mappedQuizzes;
      return mappedQuizzes;
    } catch (error) {
      console.error("퇴실 퀴즈 조회 실패:", error);
      throw error;
    }
  };

  return {
    isQuizModalOpen, // 퀴즈 출제 모달 여부
    recommendedQuizzes, // AI 추천 문항 리스트
    selectedQuizzes, // 선택된 퀴즈 리스트
    toggleModal, // 모달 토글
    addQuiz, // AI 추천 문항 추가
    addEmptyQuiz,
    toggleEdit, // 추가됨
    updateCorrectAnswer,
    toggleUserAnswer,
    gradeQuiz,
    removeQuiz,
    resetSelected,
    publishExitQuiz,
    fetchExitQuiz,
  };
});
