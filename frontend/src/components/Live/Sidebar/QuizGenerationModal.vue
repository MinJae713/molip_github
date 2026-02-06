<script setup lang="ts">
/**
 * QuizGenerationModal.vue
 * 
 * [기능]
 * - 강사가 수업 종료 시 수강생들에게 보낼 '퇴실 퀴즈'를 구성하고 발송하는 모달입니다.
 * - 4지선다 객관식 문항만 지원하며, 최대 5개까지 구성 가능합니다.
 * - 구성된 퀴즈는 부모 컴포넌트(TrainerLiveSessionView)에서 제공하는 WebSocket 발송 함수를 통해 전달됩니다.
 */
import { useRoute } from 'vue-router';
import { useQuizStore } from '@/stores/quiz';

const quizStore = useQuizStore();
const route = useRoute();

/**
 * 퀴즈 저장 처리
 * 1. 선택된 퀴즈가 있는지 확인
 * 2. 모든 문항이 제대로 채워졌는지 검증 (질문, 4개 보기, 정답 선택)
 * 3. REST API로 서버에 저장
 */
const handlePublish = async () => {
  if (quizStore.selectedQuizzes.length === 0) {
    alert('최소 1개 이상의 퀴즈를 생성해주세요.');
    return;
  }

  // 데이터 정합성 체크: 질문이 비어있거나, 정답 인덱스가 없거나, 4개 보기가 모두 채워지지 않은 경우 체크
  const isValid = quizStore.selectedQuizzes.every(q => {
    const isQuestionEmpty = q.question.trim() === '';
    const isCorrectAnswerMissing = q.correctAnswer.length === 0;
    const isOptionsIncomplete = q.options?.some(opt => !opt || opt.trim() === '');
    const isOptionsNotFour = q.options?.length !== 4;

    return !isQuestionEmpty && !isCorrectAnswerMissing && !isOptionsIncomplete && !isOptionsNotFour;
  });

  if (!isValid) {
    alert('모든 문항의 질문, 4개 보기, 그리고 정답을 정확히 입력해주세요.');
    return;
  }

  try {
    const sessionId = route.params.sessionId as string;

    // 서버에 저장 (REST API)
    const result = await quizStore.publishExitQuiz(sessionId);
    if (result.apiResult) {
      alert('퇴실 퀴즈가 성공적으로 저장되었습니다.');
      quizStore.toggleModal(false); // 모달 닫기
    } else {
      alert('퀴즈 저장에 실패했습니다. 다시 시도해주세요.');
    }
  } catch (error) {
    console.error('[QuizModal] Save error:', error);
    alert('퀴즈 저장 중 오류가 발생했습니다.');
  }
};

/** 닫기 핸들러 */
const handleClose = () => {
  quizStore.toggleModal(false);
};
</script>

<template>
  <Teleport to="body">
    <Transition name="fade">
      <!-- 모달 배경 -->
      <div v-if="quizStore.isQuizModalOpen"
        class="fixed inset-0 z-100 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-sm">

        <!-- 모달 컨테이너 -->
        <div class="bg-white rounded-[2.5rem] w-full max-w-2xl max-h-[85vh] overflow-hidden shadow-2xl flex flex-col">

          <!-- 헤더 영역 -->
          <div class="p-8 flex justify-between items-start border-b border-slate-100">
            <div class="flex items-center gap-4">
              <div class="w-12 h-12 bg-indigo-50 rounded-2xl flex items-center justify-center text-indigo-600">
                <span class="material-symbols-outlined text-2xl">fact_check</span>
              </div>
              <div>
                <h2 class="text-xl font-bold text-slate-900">퇴실 퀴즈 구성</h2>
                <p class="text-sm text-slate-500 mt-1">학생들의 수업 이해도를 확인하기 위한 퀴즈를 작성하세요.</p>
              </div>
            </div>
            <button @click="handleClose" class="p-2 hover:bg-slate-100 rounded-full transition-colors">
              <span class="material-symbols-outlined text-slate-400">close</span>
            </button>
          </div>

          <!-- 메인 퀴즈 리스트 영역 (1단 구성으로 정리) -->
          <div class="flex-1 overflow-y-auto p-8 custom-scrollbar">
            <div class="flex items-center justify-between mb-6">
              <span class="text-sm font-bold text-slate-700">작성된 퀴즈 <span class="text-indigo-600 ml-1">{{
                quizStore.selectedQuizzes.length }}/5</span></span>
              <button @click="quizStore.resetSelected"
                class="text-[11px] text-slate-400 font-bold hover:text-slate-600 flex items-center gap-1">
                <span class="material-symbols-outlined text-sm">refresh</span> 전체 초기화
              </button>
            </div>

            <div class="space-y-4">
              <!-- 개별 퀴즈 카드 -->
              <div v-for="(quiz, idx) in quizStore.selectedQuizzes" :key="quiz.id"
                class="relative border-2 rounded-2xl p-6 transition-all"
                :class="quiz.isEditing ? 'border-indigo-400 bg-white shadow-md' : 'border-indigo-100 bg-indigo-50/30'">

                <div class="flex gap-4">
                  <!-- 순번 표시 -->
                  <span class="text-xs font-black text-indigo-300 mt-1">{{ String(idx + 1).padStart(2, '0') }}</span>

                  <!-- 편집 모드 UI -->
                  <div v-if="quiz.isEditing" class="flex-1 pr-6 space-y-4">
                    <!-- 질문 입력 -->
                    <div>
                      <label class="text-[11px] font-bold text-slate-500 mb-1.5 block">질문</label>
                      <textarea v-model="quiz.question"
                        class="w-full text-[13px] font-bold text-slate-700 bg-slate-50 border border-slate-200 rounded-xl p-3 focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-400 transition-all resize-none"
                        rows="2" placeholder="질문 내용을 입력하세요..."></textarea>
                    </div>

                    <!-- 4지선다 보기 입력 -->
                    <div class="space-y-2">
                      <label class="text-[11px] font-bold text-slate-500 mb-1.5 block">선택지 및 정답 설정 (4지선다)</label>
                      <div v-for="optIdx in 4" :key="optIdx" class="flex items-center gap-3">
                        <!-- 정답 선택 라디오 버튼 -->
                        <input type="radio" :name="'correct-' + quiz.id"
                          :checked="quiz.correctAnswer[0] === (optIdx - 1)" @change="quiz.correctAnswer = [optIdx - 1]"
                          class="w-5 h-5 cursor-pointer accent-green-500">
                        <!-- 보기 내용 입력 -->
                        <input v-model="quiz.options![optIdx - 1]"
                          class="flex-1 text-xs bg-white border border-slate-200 rounded-lg px-3 py-2 focus:border-indigo-400 focus:outline-none"
                          :placeholder="`선택지 ${optIdx}`" />
                      </div>
                    </div>
                  </div>

                  <!-- 조회 모드 UI (편집 완료 상태) -->
                  <div v-else class="flex-1 pr-14">
                    <p class="text-[14px] font-bold text-slate-700 leading-snug mb-3">{{ quiz.question || '내용을 작성해주세요.'
                    }}</p>

                    <div class="grid grid-cols-2 gap-2">
                      <div v-for="(opt, optIdx) in quiz.options" :key="optIdx"
                        class="flex items-center gap-2 text-xs p-2 rounded-lg border transition-colors" :class="quiz.correctAnswer[0] === optIdx
                          ? 'border-green-200 bg-green-50 text-green-700 font-bold'
                          : 'border-slate-100 bg-white text-slate-500'">
                        <span class="w-4 h-4 rounded-full border flex items-center justify-center text-[9px] shrink-0"
                          :class="quiz.correctAnswer[0] === optIdx ? 'border-green-500 bg-green-100 text-green-600' : 'border-slate-300'">
                          {{ optIdx + 1 }}
                        </span>
                        <span class="truncate">{{ opt || `(미입력)` }}</span>
                      </div>
                    </div>
                  </div>
                </div>

                <!-- 카드 우상단 액션 버튼 (편집/삭제) -->
                <div class="absolute top-6 right-6 flex items-center gap-1">
                  <button @click="quizStore.toggleEdit(quiz.id as number)"
                    class="w-8 h-8 flex items-center justify-center rounded-full transition-colors"
                    :class="quiz.isEditing ? 'bg-indigo-600 text-white shadow-lg' : 'text-slate-300 hover:text-indigo-500 hover:bg-indigo-50'">
                    <span class="material-symbols-outlined text-lg">{{ quiz.isEditing ? 'check' : 'edit' }}</span>
                  </button>
                  <button @click="quizStore.removeQuiz(quiz.id as number)"
                    class="w-8 h-8 flex items-center justify-center text-slate-300 hover:text-red-500 rounded-full hover:bg-red-50 transition-colors">
                    <span class="material-symbols-outlined text-lg">delete</span>
                  </button>
                </div>
              </div>

              <!-- 문항 추가 버튼 -->
              <button v-if="quizStore.selectedQuizzes.length < 5" @click="quizStore.addEmptyQuiz"
                class="w-full py-4 border-2 border-dashed border-slate-200 rounded-2xl text-slate-400 text-[13px] font-bold flex items-center justify-center gap-2 hover:bg-slate-50 transition-all">
                <span class="material-symbols-outlined text-indigo-400">add_circle</span>
                직접 문항 추가하기 (최대 5개)
              </button>
            </div>
          </div>

          <!-- 푸터 영역 (발송 버튼) -->
          <div class="p-6 bg-slate-50 border-t border-slate-100 flex items-center justify-end">
            <button @click="handlePublish"
              class="px-10 py-3.5 bg-indigo-600 text-white rounded-2xl text-sm font-bold shadow-xl shadow-indigo-200 hover:bg-indigo-700 transition-all flex items-center gap-2 active:scale-95">
              <span class="material-symbols-outlined text-xl">send</span>
              퇴실 퀴즈 저장
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

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