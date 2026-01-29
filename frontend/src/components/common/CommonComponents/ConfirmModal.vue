<script setup lang="ts">
import BaseModal from '@/components/common/CommonComponents/BaseModal.vue';

interface Props {
  isOpen: boolean;
  title: string;
  message?: string;
  confirmText?: string;
  cancelText?: string;
  variant?: 'danger' | 'primary'; // 위험(빨강) 또는 일반(파랑)
  loading?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  variant: 'primary',
  confirmText: '확인',
  cancelText: '취소',
  loading: false
});

const emit = defineEmits(['close', 'confirm']);
</script>

<template>
  <BaseModal :open="isOpen" @close="emit('close')" maxWidth="max-w-md">
    <div class="flex flex-col items-center text-center py-4">
      <div 
        class="size-16 rounded-full flex items-center justify-center mb-6"
        :class="variant === 'danger' ? 'bg-red-50 text-red-500' : 'bg-indigo-50 text-indigo-600'"
      >
        <span class="material-symbols-outlined text-4xl">
          {{ variant === 'danger' ? 'report' : 'info' }}
        </span>
      </div>

      <h3 class="text-xl font-bold text-slate-900 mb-2">{{ title }}</h3>
      
      <div class="text-slate-500 leading-relaxed mb-8">
        <slot>
          {{ message }}
        </slot>
      </div>

      <div class="flex items-center gap-3 w-full">
        <button 
          @click="emit('close')"
          class="flex-1 py-3.5 rounded-xl font-bold text-slate-600 bg-slate-100 hover:bg-slate-200 transition-all"
        >
          {{ cancelText }}
        </button>
        <button 
          @click="emit('confirm')"
          :disabled="loading"
          class="flex-1 py-3.5 rounded-xl font-bold text-white transition-all shadow-lg flex items-center justify-center gap-2"
          :class="variant === 'danger' 
            ? 'bg-red-500 hover:bg-red-600 shadow-red-100' 
            : 'bg-indigo-600 hover:bg-indigo-700 shadow-indigo-100'"
        >
          <span v-if="loading" class="size-4 border-2 border-white/30 border-t-white rounded-full animate-spin"></span>
          {{ confirmText }}
        </button>
      </div>
    </div>
  </BaseModal>
</template>