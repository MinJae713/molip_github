<template>
  <div class="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/60 backdrop-blur-sm px-4">
    <div class="bg-white rounded-4xl w-full max-w-lg shadow-2xl overflow-hidden animate-in fade-in zoom-in duration-200">
      <div class="px-10 pt-10 pb-6 flex justify-between items-center">
        <div>
          <h3 class="text-2xl font-black text-slate-900">신규 코드 발급</h3>
          <p class="text-slate-400 font-bold text-sm">새로운 워크스페이스 초대 코드를 생성합니다.</p>
        </div>
        <button @click="$emit('close')" class="size-10 flex items-center justify-center rounded-xl hover:bg-slate-100 transition-colors">
          <span class="material-symbols-outlined text-slate-400">close</span>
        </button>
      </div>

      <div class="px-10 pb-10">
        <div class="space-y-6">
          <div>
            <label class="block text-sm font-black text-slate-700 mb-2">코드 식별명</label>
            <input 
              v-model="form.name"
              type="text" 
              placeholder="예: 2026_신입사원_공채"
              class="w-full px-5 py-4 bg-slate-50 border border-slate-200 rounded-2xl focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500 font-bold transition-all"
            />
            <p class="mt-2 text-[11px] text-slate-400 font-bold">관리 용도로 사용되는 이름입니다. (영문, 숫자, 언더바 권장)</p>
          </div>

          <div>
            <label class="block text-sm font-black text-slate-700 mb-2">만료일 설정</label>
            <div class="relative">
              <span class="material-symbols-outlined absolute left-4 top-3.5 text-slate-400">calendar_today</span>
              <input 
                v-model="form.expiry"
                type="date" 
                class="w-full pl-12 pr-5 py-4 bg-slate-50 border border-slate-200 rounded-2xl focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500 font-bold transition-all"
              />
            </div>
          </div>

          <div class="bg-indigo-50/50 rounded-2xl p-6 border border-indigo-100/50">
            <div class="flex justify-between items-center mb-3">
              <span class="text-xs font-black text-indigo-600 uppercase tracking-widest">생성 예정 코드 미리보기</span>
              <span class="px-2 py-0.5 bg-indigo-100 text-indigo-600 text-[10px] rounded-md font-bold">자동 생성됨</span>
            </div>
            <div class="flex items-center gap-4">
              <div class="size-12 bg-white rounded-xl flex items-center justify-center shadow-sm">
                <span class="material-symbols-outlined text-indigo-500 text-2xl">key</span>
              </div>
              <div>
                <div class="text-xl font-black text-slate-900 tracking-tighter">
                  {{ generatedPreview }}
                </div>
                <div class="text-xs text-slate-400 font-bold">발급 후에는 코드를 수정할 수 없습니다.</div>
              </div>
            </div>
          </div>
        </div>

        <div class="flex gap-3 mt-10">
          <button 
            @click="$emit('close')" 
            class="flex-1 py-4 bg-slate-100 text-slate-500 rounded-2xl font-black hover:bg-slate-200 transition-colors"
          >
            취소
          </button>
          <button 
            @click="handleCreate"
            :disabled="!isValid"
            :class="[
              'flex-1 py-4 rounded-2xl font-black shadow-lg transition-all active:scale-95',
              isValid ? 'bg-indigo-600 text-white shadow-indigo-200 hover:bg-indigo-700' : 'bg-slate-200 text-slate-400 cursor-not-allowed shadow-none'
            ]"
          >
            코드 발급하기
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';

const emit = defineEmits(['close', 'confirm']);

// 1. 초기값 정의
const form = ref({
  name: '',
  expiry: ''
});

// 2. [에러 해결] 생성 예정 코드 미리보기
const generatedPreview = computed(() => {
  // form.value가 존재하고 name이 있을 때만 로직 실행
  if (!form.value?.name || form.value.name.trim() === '') {
    return '# ---- - ----';
  }
  
  // 랜덤 코드 생성 로직 (안전하게 계산)
  const part1 = Math.floor(1000 + Math.random() * 9000);
  const part2 = Math.floor(1000 + Math.random() * 9000);
  return `#${part1}-${part2}`;
});

const isValid = computed(() => {
  // 안전하게 null/undefined 체크 후 길이 확인
  return (form.value?.name?.length ?? 0) > 2 && !!form.value?.expiry;
});

const handleCreate = () => {
  if (!isValid.value) return;
  
  // 3. 날짜 포맷 안전하게 처리
  const today = new Date();
  const formattedDate = today.toISOString().split('T')[0]?.replace(/-/g, '.') ?? '2026.01.23';

  emit('confirm', {
    name: form.value.name,
    expiry: form.value.expiry.replace(/-/g, '.'), // YYYY-MM-DD -> YYYY.MM.DD
    code: generatedPreview.value,
    created: formattedDate
  });
};
</script>