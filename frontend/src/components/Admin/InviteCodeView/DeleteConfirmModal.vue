<template>
  <div class="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/60 backdrop-blur-sm p-4">
    <div class="bg-white rounded-3xl w-full max-w-md shadow-2xl overflow-hidden border border-slate-100">
      <div class="p-8">
        <div class="flex items-center gap-4 mb-6">
          <div class="size-12 bg-rose-50 rounded-2xl flex items-center justify-center">
            <span class="material-symbols-outlined text-rose-500 text-3xl">warning</span>
          </div>
          <div>
            <h3 class="text-xl font-black text-slate-900">초대코드를 삭제하시겠습니까?</h3>
            <p class="text-sm text-slate-400 font-medium">이 작업은 되돌릴 수 없습니다.</p>
          </div>
        </div>

        <div class="bg-slate-50/80 rounded-2xl p-4 mb-6 border border-slate-100 flex items-center gap-3">
          <div class="size-10 bg-white rounded-xl flex items-center justify-center shadow-sm">
             <span class="material-symbols-outlined text-amber-500">key</span>
          </div>
          <span class="text-base font-black text-slate-900 uppercase tracking-tight">{{ targetName }}</span>
        </div>

        <div class="bg-indigo-50/50 rounded-2xl p-4 mb-8 flex gap-3">
          <span class="material-symbols-outlined text-indigo-600">info</span>
          <p class="text-xs text-indigo-600 leading-relaxed font-bold">
            초대코드를 삭제하더라도 이미 해당 코드로 가입한 <strong class="underline underline-offset-2">기존 수강생의 학습 데이터는 그대로 유지</strong>됩니다. 하지만, 이후 해당 코드를 통한 신규 가입은 불가능합니다.
          </p>
        </div>

        <div class="flex gap-3">
          <button @click="$emit('close')" class="flex-1 py-3.5 border border-slate-200 rounded-2xl font-black text-slate-500 hover:bg-slate-50 transition-all">취소</button>
          <button @click="$emit('confirm')" class="flex-1 py-3.5 bg-rose-500 text-white rounded-2xl font-black shadow-lg shadow-rose-200 hover:bg-rose-600 transition-all">삭제하기</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{ targetName: string }>();
defineEmits(['close', 'confirm']);
</script>