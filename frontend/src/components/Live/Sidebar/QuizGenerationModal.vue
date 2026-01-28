<script setup lang="ts">
import { useQuizStore } from '@/stores/quiz';
import { useLiveStore } from '@/stores/live';

const quizStore = useQuizStore();
const liveStore = useLiveStore();

// 편집 모드 토글 함수
const toggleEdit = (idx: number) => {
  const quiz = quizStore.selectedQuizzes[idx];
  if (quiz) {
    quiz.isEditing = !quiz.isEditing;
  }
};

// 퀴즈 발송 함수
const handlePublish = () => {
  if (quizStore.selectedQuizzes.length === 0) {
    alert('최소 1개 이상의 퀴즈를 선택해주세요.');
    return;
  }
  
  // 선택된 퀴즈 리스트 전체 발송
  liveStore.publishQuiz(quizStore.selectedQuizzes);
  
  alert('퀴즈가 전체 수강생에게 발송되었습니다.');
  quizStore.toggleModal(false);
};
</script>

<template>
  <Teleport to="body">
    <Transition name="fade">
      <div v-if="quizStore.isQuizModalOpen" class="fixed inset-0 z-100 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-sm">
        <div class="bg-white rounded-4xl w-full max-w-4xl max-h-[85vh] overflow-hidden shadow-2xl flex flex-col">
          
          <div class="p-8 flex justify-between items-start">
            <div class="flex items-center gap-4">
              <div class="w-12 h-12 bg-indigo-50 rounded-2xl flex items-center justify-center text-indigo-600">
                <span class="material-symbols-outlined text-2xl">fact_check</span>
              </div>
              <div>
                <h2 class="text-xl font-bold text-slate-900">퇴실 퀴즈 구성 및 발송</h2>
                <p class="text-sm text-slate-500 mt-1">AI 추천 문항을 활용하거나 직접 문항을 작성하여 퀴즈를 완성하세요.</p>
              </div>
            </div>
            <button @click="quizStore.toggleModal(false)" class="p-2 hover:bg-slate-100 rounded-full transition-colors">
              <span class="material-symbols-outlined text-slate-400">close</span>
            </button>
          </div>

          <div class="flex-1 overflow-hidden grid grid-cols-2 border-t border-slate-100">
            
            <div class="bg-slate-50/50 p-8 overflow-y-auto border-r border-slate-100">
              <div class="flex items-center justify-between mb-6">
                <span class="font-bold text-slate-700 flex items-center gap-2">
                  <span class="material-symbols-outlined text-indigo-500">psychology</span>
                  AI 추천 문항 <span class="text-xs text-slate-400 font-normal">(Dead Zone 기반)</span>
                </span>
                <span class="px-2 py-1 bg-indigo-100 text-[10px] font-bold text-indigo-600 rounded-md">AI Analysis</span>
              </div>

              <div class="space-y-4">
                <div v-for="quiz in quizStore.recommendedQuizzes" :key="quiz.id" class="bg-white border border-slate-200 rounded-2xl p-5 shadow-sm transition-all hover:border-indigo-200 group">
                  <div class="flex justify-between items-start mb-3">
                    <span class="px-2 py-1 bg-indigo-50 text-[10px] font-bold text-indigo-600 rounded-md">{{ quiz.timeRange }} {{ quiz.reason }}</span>
                    <button @click="quizStore.addQuiz(quiz)" class="text-indigo-600 text-sm font-bold flex items-center gap-1 active:scale-95 transition-transform">
                      <span class="material-symbols-outlined text-lg">add</span> 추가
                    </button>
                  </div>
                  <p class="text-sm font-bold text-slate-800 leading-relaxed mb-4">Q. {{ quiz.question }}</p>
                </div>
              </div>
            </div>

            <div class="p-8 overflow-y-auto flex flex-col">
              <div class="flex items-center justify-between mb-6">
                <span class="text-sm font-bold text-slate-700">최종 퀴즈 리스트 <span class="text-indigo-600 ml-1">{{ quizStore.selectedQuizzes.length }}/5</span></span>
                <button @click="quizStore.selectedQuizzes = []" class="text-[11px] text-slate-400 font-bold hover:text-slate-600 flex items-center gap-1">
                  <span class="material-symbols-outlined text-sm">refresh</span> 전체 초기화
                </button>
              </div>

              <div class="space-y-3 flex-1 overflow-y-auto pr-1">
                <div v-for="(quiz, idx) in quizStore.selectedQuizzes" :key="idx" 
                     class="relative border-2 rounded-2xl p-4 transition-all"
                     :class="quiz.isEditing ? 'border-indigo-400 bg-white shadow-md' : 'border-indigo-100 bg-indigo-50/30'">
                  
                  <div class="flex gap-3">
                    <span class="text-xs font-black text-indigo-300 mt-1">{{ String(idx + 1).padStart(2, '0') }}</span>
                    
                    <div v-if="quiz.isEditing" class="flex-1 pr-6 space-y-3">
                      <!-- 질문 입력 -->
                      <textarea 
                        v-model="quiz.question" 
                        class="w-full text-[13px] font-bold text-slate-700 bg-slate-50 border border-slate-200 rounded-xl p-3 focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-400 transition-all resize-none"
                        rows="2"
                        placeholder="퀴즈 문항을 입력하세요..."
                      ></textarea>

                      <!-- 유형 선택 -->
                      <div class="flex gap-2">
                        <label class="flex items-center gap-1 cursor-pointer">
                          <input type="radio" v-model="quiz.type" value="single" class="accent-indigo-600">
                          <span class="text-xs text-slate-600">객관식</span>
                        </label>
                        <label class="flex items-center gap-1 cursor-pointer">
                          <input type="radio" v-model="quiz.type" value="subjective" class="accent-indigo-600">
                          <span class="text-xs text-slate-600">주관식</span>
                        </label>
                      </div>

                      <!-- 객관식 보기 편집 -->
                      <div v-if="quiz.type === 'single' || quiz.type === 'multiple'" class="space-y-2">
                        <div v-for="(opt, optIdx) in (quiz.options || [])" :key="optIdx" class="flex items-center gap-2">
                           <!-- 정답 선택 라디오/체크박스 -->
                          <input 
                            type="radio" 
                            name="correct-answer"
                            :checked="quiz.answer === String(optIdx)"
                            @change="quiz.answer = String(optIdx)"
                            class="w-4 h-4 cursor-pointer accent-green-500"
                            title="정답으로 설정"
                          >
                          
                          <span class="w-5 h-5 rounded-full border border-slate-300 flex items-center justify-center text-[10px] bg-white font-bold text-slate-400 shrink-0">{{ optIdx + 1 }}</span>
                          <input 
                            v-model="quiz.options![optIdx]"
                            class="flex-1 text-xs bg-slate-50 border border-slate-200 rounded-lg px-2 py-1.5 focus:outline-none focus:border-indigo-400"
                            placeholder="보기 내용을 입력하세요"
                          />
                          <button @click="quiz.options?.splice(optIdx, 1)" class="text-slate-400 hover:text-red-500">
                            <span class="material-symbols-outlined text-base">remove_circle</span>
                          </button>
                        </div>
                        <button 
                          @click="() => { if(!quiz.options) quiz.options = []; quiz.options.push(''); }"
                          class="text-xs text-indigo-600 font-bold flex items-center gap-1 hover:bg-indigo-50 px-2 py-1 rounded-lg w-fit"
                        >
                          <span class="material-symbols-outlined text-sm">add</span> 보기 추가
                        </button>
                      </div>

                      <!-- 주관식 정답 입력 -->
                       <div v-if="quiz.type === 'subjective'" class="mt-2">
                          <label class="text-[11px] font-bold text-slate-500 ml-1">정답</label>
                          <input 
                            v-model="quiz.answer"
                            class="w-full text-xs bg-green-50 border border-green-200 rounded-lg px-3 py-2 mt-1 focus:outline-none focus:border-green-400 text-green-700 font-bold placeholder-green-300"
                            placeholder="정답 키워드를 입력하세요 (예: 딥러닝)"
                          />
                       </div>

                    </div>

                    <!-- 뷰 모드 -->
                    <div v-else class="flex-1 pr-14">
                      <div class="flex items-center gap-2 mb-1">
                        <span class="px-1.5 py-0.5 bg-slate-200 text-slate-600 text-[10px] font-bold rounded">{{ quiz.type === 'subjective' ? '주관식' : '객관식' }}</span>
                        <p class="text-[13px] font-bold text-slate-700 leading-snug">{{ quiz.question || '내용을 작성해주세요.' }}</p>
                      </div>
                      
                      <!-- 객관식 보기 뷰 -->
                      <div v-if="(quiz.type === 'single' || quiz.type === 'multiple') && quiz.options" class="mt-2 space-y-1">
                        <div v-for="(opt, optIdx) in quiz.options" :key="optIdx" 
                             class="flex items-center gap-2 text-xs"
                             :class="quiz.answer === String(optIdx) ? 'text-green-600 font-bold' : 'text-slate-600'">
                          <span class="w-4 h-4 rounded-full border flex items-center justify-center text-[9px]"
                                :class="quiz.answer === String(optIdx) ? 'border-green-500 bg-green-50 text-green-600' : 'border-slate-300 bg-white'">
                                {{ optIdx + 1 }}
                          </span>
                          {{ opt }}
                          <span v-if="quiz.answer === String(optIdx)" class="material-symbols-outlined text-[14px]">check</span>
                        </div>
                      </div>

                      <!-- 주관식 정답 뷰 -->
                      <div v-if="quiz.type === 'subjective' && quiz.answer" class="mt-2 px-3 py-2 bg-green-50 border border-green-100 rounded-lg flex items-center gap-2">
                        <span class="text-[11px] font-bold text-green-600 bg-green-100 px-1.5 rounded">정답</span>
                        <span class="text-xs font-bold text-green-700">{{ quiz.answer }}</span>
                      </div>
                    </div>
                  </div>

                  <div class="absolute top-4 right-4 flex items-center gap-1">
                    <button @click="toggleEdit(idx)" class="w-8 h-8 flex items-center justify-center rounded-full transition-colors"
                            :class="quiz.isEditing ? 'bg-indigo-600 text-white' : 'text-slate-300 hover:text-indigo-500 hover:bg-indigo-50'">
                      <span class="material-symbols-outlined text-lg">{{ quiz.isEditing ? 'check' : 'edit' }}</span>
                    </button>
                    <button @click="quizStore.removeQuiz(quiz.id)" class="w-8 h-8 flex items-center justify-center text-slate-300 hover:text-red-500 hover:bg-red-50 rounded-full transition-colors">
                      <span class="material-symbols-outlined text-lg">delete</span>
                    </button>
                  </div>
                </div>

                <button 
                  @click="quizStore.addEmptyQuiz"
                  class="w-full py-4 border-2 border-dashed border-slate-200 rounded-2xl text-slate-400 text-[13px] font-bold flex items-center justify-center gap-2 hover:bg-slate-50 hover:border-slate-300 transition-all group"
                >
                  <span class="material-symbols-outlined text-indigo-400 group-hover:scale-110 transition-transform">add_circle</span>
                  직접 문항 추가하기
                </button>
              </div>
            </div>
          </div>

          <div class="p-6 bg-slate-50 border-t border-slate-100 flex items-center justify-between">
            <div class="flex gap-6">
              <label class="flex items-center gap-2 cursor-pointer group">
                <input type="checkbox" checked class="w-4 h-4 rounded border-slate-300 text-indigo-600 focus:ring-indigo-500 transition-all">
                <span class="text-xs font-bold text-slate-600 group-hover:text-indigo-600">퀴즈 결과 즉시 공개</span>
              </label>
              <label class="flex items-center gap-2 cursor-pointer group">
                <input type="checkbox" class="w-4 h-4 rounded border-slate-300 text-indigo-600 focus:ring-indigo-500 transition-all">
                <span class="text-xs font-bold text-slate-600 group-hover:text-indigo-600">성적 데이터에 반영</span>
              </label>
            </div>
            
            <button 
              @click="handlePublish"
              class="px-10 py-3.5 bg-indigo-600 text-white rounded-2xl text-sm font-bold shadow-xl shadow-indigo-200 hover:bg-indigo-700 hover:-translate-y-0.5 active:translate-y-0 transition-all flex items-center gap-2"
            >
              <span class="material-symbols-outlined text-xl">send</span>
              퇴실 퀴즈 즉시 발송 (수업 종료)
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s ease;
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
}

/* 스크롤바 커스텀 */
::-webkit-scrollbar {
  width: 6px;
}
::-webkit-scrollbar-track {
  background: transparent;
}
::-webkit-scrollbar-thumb {
  background: #e2e8f0;
  border-radius: 10px;
}
::-webkit-scrollbar-thumb:hover {
  background: #cbd5e1;
}
</style>