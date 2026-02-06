<script setup lang="ts">
import { ref, computed } from 'vue';
import BaseModal from '@/components/common/CommonComponents/BaseModal.vue';

interface Props {
  isOpen: boolean;
  selectedMembers: any[]; // 선택된 멤버 객체 배열
}

const props = defineProps<Props>();
const emit = defineEmits(['close', 'confirm']);

const selectedRole = ref('Learner'); // 기본 선택값

// 제목 분기 처리
const modalTitle = computed(() => {
  return props.selectedMembers.length === 1 
    ? `${props.selectedMembers[0].name}님 역할 변경`
    : `참여진 ${props.selectedMembers.length}명 역할 일괄 변경`;
});

const handleConfirm = () => {
  // 부모에게 선택된 ID들과 변경할 역할을 보냄
  const ids = props.selectedMembers.map(m => m.workspaceMemberId);
  emit('confirm', { ids, role: selectedRole.value });
};
</script>

<template>
  <BaseModal :open="isOpen" @close="emit('close')" maxWidth="max-w-md">
    <template #header>
      <div class="flex items-center gap-2">
        <span class="material-symbols-outlined text-indigo-600">admin_panel_settings</span>
        <h3 class="text-lg font-bold text-slate-900">{{ modalTitle }}</h3>
      </div>
    </template>

    <div class="py-4">
      <p class="text-sm text-slate-500 mb-6">
        선택한 참여진에게 부여할 새로운 역할을 선택해주세요.
      </p>

      <div class="space-y-3">
        <label v-for="role in ['Trainer', 'Learner']" :key="role" 
               class="flex items-center justify-between p-4 border rounded-xl cursor-pointer transition-all"
               :class="selectedRole === role ? 'border-indigo-600 bg-indigo-50/50' : 'border-slate-100 hover:bg-slate-50'">
          <div class="flex items-center gap-3">
            <span class="text-sm font-bold text-slate-800">{{ role }}</span>
          </div>
          <input type="radio" :value="role" v-model="selectedRole" class="accent-indigo-600 size-4" />
        </label>
      </div>
    </div>

    <template #footer>
      <div class="flex gap-2">
        <button @click="emit('close')" class="flex-1 py-3 text-sm font-bold text-slate-500 bg-slate-100 rounded-xl hover:bg-slate-200 transition-colors">취소</button>
        <button @click="handleConfirm" class="flex-1 py-3 text-sm font-bold text-white bg-indigo-600 rounded-xl hover:bg-indigo-700 shadow-lg shadow-indigo-200 transition-all">변경 완료</button>
      </div>
    </template>
  </BaseModal>
</template>