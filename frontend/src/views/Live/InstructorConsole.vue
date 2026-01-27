<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useLiveStore } from '@/stores/live';

// [컴포넌트 임포트]
import LiveHeader from '@/components/Live/Console/LiveHeader.vue';
import LiveControlPanel from '@/components/Live/Console/LiveControlPanel.vue';
import ParticipantItem from '@/components/Live/Participant/ParticipantItem.vue';
import EmotionStat from '@/components/Live/AI/EmotionStat.vue';
import StudentMonitor from '@/components/Live/Monitor/StudentMonitor.vue';
import LiveVideoConsole from '@/components/Live/Console/LiveVideoConsole.vue';

const router = useRouter();
const liveStore = useLiveStore();

// --- UI 상태 관리 ---
const isEndModalOpen = ref(false); // 수업 종료 확인 모달

onMounted(async () => {
  // 초기 데이터 로드 (코스 ID와 세션 ID는 실제 환경에 맞게 전달)
  // 예: const courseId = route.params.id;
  await liveStore.initLiveRoom('1', '1');
});

// --- 수업 종료 로직 ---
const handleEndSession = async () => {
  console.log("종료 프로세스 시작"); // 이게 찍히는지 확인!
  await liveStore.endLiveSession();
  isEndModalOpen.value = false;
  router.push('/dashboard');
};
</script>

<template>
  <div class="h-screen bg-slate-50 flex flex-col overflow-hidden font-sans text-slate-900">
    
    <LiveHeader @end-class="isEndModalOpen = true" />

    <div class="flex-1 flex overflow-hidden">
      
      <main class="flex-1 flex flex-col p-6 gap-6 overflow-y-auto bg-slate-50">
        
        <LiveVideoConsole />

        <div class="grid grid-cols-12 gap-6 min-h-70">
          <div class="col-span-4 bg-white p-7 rounded-[2.5rem] border border-slate-100 shadow-sm flex flex-col justify-between">
            <div>
              <p class="text-[10px] font-black text-slate-400 mb-4 tracking-widest uppercase">AI Action Recommendation</p>
              <h3 class="text-xl font-bold text-slate-800 mb-2 leading-snug">
                학생들의 집중도가 <span class="text-indigo-600">낮아지고 있어요.</span>
              </h3>
              <p class="text-slate-500 text-sm leading-relaxed">
                가벼운 퀴즈나 질문을 통해 분위기를 환기시켜보는 건 어떨까요?
              </p>
            </div>
            <button class="w-full bg-indigo-50 text-indigo-600 py-4 rounded-2xl font-bold hover:bg-indigo-100 transition-all mt-4">
              실시간 퀴즈 출제하기
            </button>
          </div>

          <div class="col-span-8 flex gap-4">
            <EmotionStat :rank="1" label="놀람" emoji="😲" :percentage="45" color="#6366f1" />
            <EmotionStat :rank="2" label="평온" emoji="😌" :percentage="22" color="#818cf8" />
            <EmotionStat :rank="3" label="집중" emoji="🧐" :percentage="18" color="#94a3b8" />
          </div>
        </div>
      </main>

      <aside class="w-100 bg-white border-l border-slate-200 flex flex-col shadow-sm shrink-0 overflow-hidden">
        
        <div class="flex-1 flex flex-col min-h-0 p-7 pb-4">
          <div class="flex items-center justify-between mb-6 px-1">
            <h2 class="text-xl font-bold text-slate-800">
              참여자 목록 <span class="text-indigo-600 ml-1 text-base">{{ liveStore.participants?.length || 0 }}</span>
            </h2>
            <div class="flex gap-1">
              <button class="p-2 hover:bg-slate-100 rounded-xl transition-colors text-slate-500">
                <span class="material-symbols-outlined">search</span>
              </button>
            </div>
          </div>

          <div class="flex-1 overflow-y-auto pr-2 custom-scrollbar">
            <div v-if="liveStore.participants?.length > 0" class="flex flex-col gap-2">
              <ParticipantItem 
                v-for="p in liveStore.participants" 
                :key="p.id" 
                :participant="p" 
                @click="liveStore.selectStudent(p)"
                class="cursor-pointer hover:ring-2 hover:ring-indigo-100 transition-all rounded-2xl"
              />
            </div>
            <div v-else class="h-full flex flex-col items-center justify-center text-slate-400 opacity-60">
              <span class="material-symbols-outlined text-5xl mb-2">group_off</span>
              <p class="text-sm">참여자가 없습니다.</p>
            </div>
          </div>
        </div>

        <div class="h-60 min-h-0 px-7 pt-4 border-t border-slate-50 bg-slate-50/30">
          <div v-if="!liveStore.selectedStudent" 
               class="h-full border-2 border-dashed border-slate-200 rounded-4xl flex flex-col items-center justify-center text-slate-300 text-center px-4">
            <span class="material-symbols-outlined text-3xl mb-2 opacity-30">visibility</span>
            <p class="text-[11px] font-medium leading-relaxed italic text-slate-400">
              목록에서 학생을 클릭하여<br/>화면을 확인하세요.
            </p>
          </div>
          <StudentMonitor v-else />
        </div>

        <LiveControlPanel />
      </aside>
    </div>

    <Transition name="fade">
      <div v-if="isEndModalOpen" class="fixed inset-0 z-100 flex items-center justify-center bg-slate-900/60 backdrop-blur-sm p-4">
        <div class="w-full max-w-md bg-white rounded-[2.5rem] p-10 shadow-2xl border border-slate-100">
          <div class="text-center">
            <div class="size-20 bg-red-50 text-red-500 rounded-3xl flex items-center justify-center mx-auto mb-6">
              <span class="material-symbols-outlined text-5xl font-light">logout</span>
            </div>
            <h3 class="text-2xl font-bold text-slate-900 mb-3">수업을 종료하시겠습니까?</h3>
            <p class="text-slate-500 mb-10 leading-relaxed font-medium">
              종료 시 모든 학생과의 연결이 해제되며<br/>AI 분석 리포트가 최종 저장됩니다.
            </p>
          </div>
          <div class="flex gap-4">
            <button @click="isEndModalOpen = false" class="flex-1 py-4 bg-slate-100 text-slate-600 rounded-2xl font-bold hover:bg-slate-200 transition-all">취소</button>
            <button @click="handleEndSession" class="flex-1 py-4 bg-red-500 text-white rounded-2xl font-bold hover:bg-red-600 transition-all shadow-lg shadow-red-200">수업 종료</button>
          </div>
        </div>
      </div>
    </Transition>
  </div>
</template>

<style scoped>
/* 커스텀 스크롤바 디자인 */
.custom-scrollbar::-webkit-scrollbar { width: 4px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: #e2e8f0; border-radius: 10px; }
.custom-scrollbar::-webkit-scrollbar-thumb:hover { background: #cbd5e1; }

/* 모달 애니메이션 */
.fade-enter-active, .fade-leave-active { transition: opacity 0.3s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>