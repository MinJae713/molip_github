<template>
  <div class="p-8 font-sans text-slate-900 bg-slate-50/30 min-h-screen">
    <div class="flex justify-between items-start mb-8">
      <div>
        <h1 class="text-2xl font-bold mb-1">초대코드 센터</h1>
        <p class="text-slate-500 text-sm">워크스페이스 접근 제어 관리 (다중 선택 및 삭제 지원)</p>
      </div>
      
      <button @click="isCreateModalOpen = true" class="flex items-center gap-2 px-4 py-2.5 bg-indigo-600 text-white rounded-xl font-bold shadow-lg shadow-indigo-200 hover:bg-indigo-700 transition-all">
        <span class="material-symbols-outlined text-xl">add_circle</span>
        신규 코드 발급하기
      </button>

      <CreateCodeModal 
        v-if="isCreateModalOpen" 
        @close="isCreateModalOpen = false" 
        @confirm="onCreateCode"
      />
    </div>

    <div class="bg-white border border-slate-200 rounded-2xl shadow-sm overflow-hidden">
      <div class="p-6 border-b border-slate-100 flex justify-between items-center bg-white">
        <div class="flex gap-2">
          <button v-for="(label, key) in tabs" :key="key"
            @click="currentTab = key"
            :class="['px-4 py-2 rounded-lg text-sm font-bold transition-all flex items-center gap-2', 
                     currentTab === key ? 'bg-indigo-50 text-indigo-600' : 'text-slate-500 hover:bg-slate-50']"
          >
            {{ label.name }}
            <span :class="['px-1.5 py-0.5 rounded-md text-[10px]', currentTab === key ? 'bg-indigo-100' : 'bg-slate-100 text-slate-400']">
              {{ label.count }}
            </span>
          </button>
        </div>
        
        <div class="flex gap-2">
          <button v-if="selectedIds.length > 0" @click="isDeleteModalOpen = true"
            class="flex items-center gap-1.5 px-3 py-2 border border-rose-200 text-rose-500 rounded-lg text-sm font-bold hover:bg-rose-50 transition-colors">
            <span class="material-symbols-outlined text-lg">delete</span>
            선택 삭제
          </button>
          <div class="relative">
            <span class="material-symbols-outlined absolute left-3 top-2.5 text-slate-400 text-xl font-light">search</span>
            <input type="text" placeholder="코드명 검색..." 
                   class="pl-10 pr-4 py-2 border border-slate-200 rounded-lg text-sm w-64 focus:outline-none focus:ring-2 focus:ring-indigo-500/20">
          </div>
          <button class="p-2 border border-slate-200 rounded-lg text-slate-600 hover:bg-slate-50">
            <span class="material-symbols-outlined text-lg">tune</span>
          </button>
        </div>
      </div>

      <InviteCodeTable 
        :codes="mockCodes" 
        v-model:selectedIds="selectedIds"
        @delete="openDeleteSingle"
      />

      <div class="p-6 border-t border-slate-100 flex justify-between items-center bg-white text-sm">
        </div>
    </div>

    <DeleteConfirmModal 
      v-if="isDeleteModalOpen" 
      :targetName="targetDeleteName"
      @close="isDeleteModalOpen = false" 
      @confirm="handleDelete" 
    />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import InviteCodeTable from '@/components/Admin/InviteCodeView/InviteCodeTable.vue';
import DeleteConfirmModal from '@/components/Admin/InviteCodeView/DeleteConfirmModal.vue';
import CreateCodeModal from '@/components/Admin/InviteCodeView/CreateCodeModal.vue';

const currentTab = ref('all');
const selectedIds = ref<number[]>([]);
const isDeleteModalOpen = ref(false);
const isCreateModalOpen = ref(false);
const targetDeleteName = ref('');

const tabs = {
  all: { name: '전체 코드', count: 142 },
  active: { name: '활성', count: 98 },
  expiring: { name: '만료 임박', count: 12 },
  expired: { name: '만료/중단', count: 32 }
};

const mockCodes = [
  { id: 1, name: 'MKT_ONBOARDING', code: '#2394-1102', created: '2023.12.01', expiry: '2024.03.15', status: '만료임박' },
  { id: 2, name: 'TEMP_INTERN_Q1', code: '#5512-3321', created: '2024.01.01', expiry: '2024.03.31', status: '만료임박' },
  { id: 3, name: 'DEV_WINTER_2024', code: '#6821-4590', created: '2024.01.15', expiry: '2024.12.31', status: '활성' },
  { id: 4, name: 'HR_GENERAL_2023', code: '#9012-7742', created: '2023.06.20', expiry: '2023.12.31', status: '만료' },
];

const openDeleteSingle = (name: string) => {
  targetDeleteName.value = name;
  isDeleteModalOpen.value = true;
};

const handleDelete = () => {
  console.log('삭제 실행');
  isDeleteModalOpen.value = false;
};

const onCreateCode = async (newData: any) => {
  console.log('서버로 보낼 데이터:', newData);
  isCreateModalOpen.value = false;
  alert('새로운 초대코드가 발급되었습니다!');
};
</script>