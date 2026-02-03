<template>
  <!-- 모달 배경 -->
  <div class="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/60 backdrop-blur-sm px-4">
    <div class="bg-white rounded-4xl w-full max-w-lg shadow-2xl overflow-hidden animate-in fade-in zoom-in duration-200">
      
      <!-- 헤더 (공통) -->
      <div class="px-10 pt-10 pb-6 flex justify-between items-center">
        <div>
          <h3 class="text-2xl font-black text-slate-900">
            {{ createdCode ? '코드 발급 완료!' : '신규 코드 발급' }}
          </h3>
          <p class="text-slate-400 font-bold text-sm">
            {{ createdCode ? '아래 코드를 복사하여 전달하세요.' : '새로운 워크스페이스 초대 코드를 생성합니다.' }}
          </p>
        </div>
        <button 
          @click="closeModal" 
          class="size-10 flex items-center justify-center rounded-xl hover:bg-slate-100 transition-colors"
        >
          <span class="material-symbols-outlined text-slate-400">close</span>
        </button>
      </div>

      <!-- 모달 바디 (Transition 적용) -->
      <div class="px-10 pb-10 min-h-75 flex flex-col justify-center">
        
        <Transition name="fade" mode="out-in">
          
          <!-- 1️⃣ 입력 폼 (작성 모드) -->
          <div v-if="!createdCode" key="input-step" class="space-y-6">
            <!-- 코드명 입력 -->
            <div>
              <label class="block text-sm font-black text-slate-700 mb-2">코드 식별명</label>
              <input 
                v-model="form.name"
                type="text" 
                placeholder="예: 2026_신입사원_공채"
                maxlength="50"
                class="w-full px-5 py-4 bg-slate-50 border border-slate-200 rounded-2xl focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500 font-bold transition-all"
                @keyup.enter="handleCreate"
              />
              <div class="flex justify-between items-start mt-2">
                <div class="flex-1">
                  <p v-if="showNameWarning" class="text-[11px] text-rose-500 font-bold flex items-center gap-1 animate-pulse">
                    <span class="material-symbols-outlined text-[14px]">error</span>
                    최소 3글자 이상 입력해주세요.
                  </p>
                  <p v-else class="text-[11px] text-slate-400 font-bold">
                    관리 용도로 사용되는 이름입니다. (영문, 숫자, 언더바 권장)
                  </p>
                </div>
                <span class="text-[11px] font-bold text-slate-400 ml-2" :class="{ 'text-indigo-600': form.name.length >= 50 }">
                  {{ form.name.length }}/50
                </span>
              </div>
            </div>

            <!-- 만료일 입력 -->
            <div>
              <label class="block text-sm font-black text-slate-700 mb-2">만료일 설정</label>
              <div class="relative">
                <span class="material-symbols-outlined absolute left-4 top-3.5 text-slate-400">calendar_today</span>
                <input 
                  v-model="form.expiry"
                  type="date" 
                  :min="minDate"
                  class="w-full pl-12 pr-5 py-4 bg-slate-50 border border-slate-200 rounded-2xl focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500 font-bold transition-all"
                />
              </div>
            </div>
          </div>

          <!-- 2️⃣ 결과 화면 (성공 모드) -->
          <div v-else key="success-step" class="flex flex-col items-center justify-center space-y-6 animate-in slide-in-from-right-4 duration-300">
            
            <!-- 성공 아이콘 -->
            <div class="size-20 bg-emerald-100 text-emerald-500 rounded-full flex items-center justify-center mb-2 animate-bounce">
              <span class="material-symbols-outlined text-5xl">check_circle</span>
            </div>

            <!-- 발급된 코드 표시 영역 -->
            <div class="w-full">
              <label class="block text-center text-sm font-black text-slate-500 mb-3">생성된 초대 코드</label>
              <div 
                @click="copyCode"
                class="group relative w-full p-4 bg-indigo-50 border-2 border-indigo-100 border-dashed rounded-2xl text-center cursor-pointer hover:bg-indigo-100 hover:border-indigo-200 transition-all"
              >
                <p class="text-base font-black text-indigo-600 tracking-wider break-all">
                  {{ createdCode }}
                </p>

                <!-- 복사 툴팁 -->
                <div class="absolute inset-0 flex items-center justify-center bg-indigo-600/10 opacity-0 group-hover:opacity-100 transition-opacity rounded-xl">
                  <span class="text-indigo-700 font-bold text-sm">클릭해서 복사하기</span>
                </div>
              </div>
            </div>

          </div>

        </Transition>
      </div>

      <!-- 하단 버튼 -->
      <div class="flex gap-3 px-10 pb-10">
        <!-- 1️⃣ 취소 / 생성 버튼 -->
        <template v-if="!createdCode">
          <button 
            @click="closeModal" 
            class="flex-1 py-4 bg-slate-100 text-slate-500 rounded-2xl font-black hover:bg-slate-200 transition-colors"
          >
            취소
          </button>
          <button 
            @click="handleCreate"
            :disabled="!isValid"
            :class="[ 'flex-1 py-4 rounded-2xl font-black shadow-lg transition-all active:scale-95',
                      isValid ? 'bg-indigo-600 text-white shadow-indigo-200 hover:bg-indigo-700' 
                              : 'bg-slate-200 text-slate-400 cursor-not-allowed shadow-none' ]"
          >
            코드 발급하기
          </button>
        </template>

        <!-- 2️⃣ 복사 및 완료 버튼 -->
        <template v-else>
          <button 
            @click="copyCode"
            class="flex-2 py-4 bg-indigo-600 text-white rounded-2xl font-black shadow-lg shadow-indigo-200 hover:bg-indigo-700 transition-all flex items-center justify-center gap-2 active:scale-95"
          >
            <span class="material-symbols-outlined">{{ isCopied ? 'check' : 'content_copy' }}</span>
            {{ isCopied ? '복사 완료!' : '복사하기' }}
          </button>
          <button 
            @click="closeModal" 
            class="flex-1 py-4 bg-slate-100 text-slate-600 rounded-2xl font-black hover:bg-slate-200 transition-colors"
          >
            닫기
          </button>
        </template>
      </div>

    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';

const props = defineProps<{
  createdCode?: string | null; // 부모가 전달해주는 생성된 코드
}>();

// create: 생성 요청 (payload 전달)
// close: 모달 닫기 요청
const emit = defineEmits(['close', 'create']);

// -------------------------
// 1️⃣ 폼 상태
// -------------------------
const form = ref({
  name: '',   // joinCodeName
  expiry: ''  // endedDate (YYYY-MM-DD)
});

// 복사 상태
const isCopied = ref(false);

// -------------------------
// 2️⃣ 유효성 체크
// -------------------------
// -------------------------
// 2️⃣ 유효성 체크
// -------------------------
const nameLength = computed(() => form.value.name?.trim().length ?? 0);

// 오늘 날짜 계산 (YYYY-MM-DD)
const minDate = computed(() => {
  const today = new Date();
  const year = today.getFullYear();
  const month = String(today.getMonth() + 1).padStart(2, '0');
  const day = String(today.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
});

const isValid = computed(() => {
  return nameLength.value > 2 && !!form.value.expiry;
});

const showNameWarning = computed(() => {
  return nameLength.value > 0 && nameLength.value <= 2;
});

// -------------------------
// 3️⃣ 동작 핸들러
// -------------------------

// 생성 요청 -> 부모가 API 호출 후 props.createdCode 업데이트 -> 성공 화면 전환
const handleCreate = () => {
  if (!isValid.value) return;

  emit('create', {
    joinCodeName: form.value.name,
    endedDate: form.value.expiry
  });
};

// 클립보드 복사
const copyCode = async () => {
  if (!props.createdCode) return;
  
  try {
    await navigator.clipboard.writeText(props.createdCode);
    isCopied.value = true;
    
    // 2초 뒤 상태 복귀
    setTimeout(() => {
      isCopied.value = false;
    }, 2000);
  } catch (err) {
    console.error('Failed to copy:', err);
    alert('복사에 실패했습니다. 직접 복사해주세요.');
  }
};

// 모달 닫기
const closeModal = () => {
  // 상태 초기화는 부모가 닫으면서(v-if) 사라지므로 자동적으로 되지만,
  // 재오픈 시 깨끗한 상태를 보장하기 위해 emit 전 로컬 상태도 정리 가능함.
  form.value.name = '';
  form.value.expiry = '';
  isCopied.value = false;
  
  emit('close');
};

// -------------------------
// 4️⃣ Watchers
// -------------------------
// 성공 상태로 진입 시(코드가 생성되었을 때) 자동 초기화 할 것이 있다면 여기서 처리
watch(() => props.createdCode, (newVal) => {
  if (!newVal) {
    // 코드가 사라지면(초기화 되면) 폼도 초기화
    form.value.name = '';
    form.value.expiry = '';
    isCopied.value = false;
  }
});
</script>

<style scoped>
/* Vue Transition: Fade */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
