<template>
  <div class="p-8 text-slate-900 bg-slate-50/30 min-h-screen">
    <!-- 헤더 영역 -->
    <div class="flex justify-between items-start mb-8">
      <div>
        <h1 class="text-2xl font-bold mb-1">초대코드 센터</h1>
        <p class="text-slate-500 text-sm">워크스페이스 접근 제어 관리</p>
      </div>
      
      <!-- 신규 코드 발급 버튼 -->
      <button @click="isCreateModalOpen = true" class="flex items-center gap-2 px-4 py-2.5 bg-indigo-600 text-white rounded-xl font-bold shadow-lg shadow-indigo-200 hover:bg-indigo-700 transition-all">
        <span class="material-symbols-outlined text-xl">add_circle</span>
        신규 코드 발급하기
      </button>

      <!-- 생성 모달 -->
      <CreateCodeModal 
        v-if="isCreateModalOpen" 
        :createdCode="newlyCreatedCode"
        @close="closeCreateModal" 
        @create="onCreateCode" 
      />
    </div>

    <!-- 초대코드 테이블 영역 -->
    <div class="bg-white border border-slate-200 rounded-2xl shadow-sm overflow-hidden">
      <!-- 상단 필터 / 검색 영역 -->
      <div class="p-6 border-b border-slate-100 flex justify-between items-center bg-white">
        <!-- 탭 버튼 -->
        <div class="flex gap-2">
          <button 
            v-for="(tab, key) in tabs" 
            :key="key"
            @click="currentTab = key"
            :class="['px-4 py-2 rounded-lg text-sm font-bold transition-all flex items-center gap-2', 
                     currentTab === key ? 'bg-indigo-50 text-indigo-600' : 'text-slate-500 hover:bg-slate-50']"
          >
            {{ tab.name }}
            <span :class="['px-1.5 py-0.5 rounded-md text-[10px]', currentTab === key ? 'bg-indigo-100' : 'bg-slate-100 text-slate-400']">
              {{ tab.count }}
            </span>
          </button>
        </div>
        
        <!-- 검색 -->
        <div class="flex gap-2">
          <div class="relative">
            <span class="material-symbols-outlined absolute left-3 top-2.5 text-slate-400 text-xl font-light">search</span>
            <input v-model="searchText" type="text" placeholder="코드명 검색..." class="pl-10 pr-4 py-2 border border-slate-200 rounded-lg text-sm w-64 focus:outline-none focus:ring-2 focus:ring-indigo-500/20">
          </div>
        </div>
      </div>

      <!-- 테이블 -->
      <InviteCodeTable :codes="filteredCodes || []" @delete="openDeleteSingle"/>

      <div class="p-6 border-t border-slate-100 flex justify-between items-center bg-white text-sm"></div>
    </div>

    <!-- 삭제 확인 모달 -->
    <DeleteConfirmModal v-if="isDeleteModalOpen" :targetName="targetDeleteName" @close="isDeleteModalOpen = false" @confirm="handleDelete" />
  </div>
</template>

<script setup lang="ts">
import { onMounted, computed, ref } from 'vue';
import { useRoute } from 'vue-router';
import { useAdminJoinCodeStore } from '@/stores/adminJoinCode';
import type { CodeItem } from '@/types';
import InviteCodeTable from '@/components/Admin/InviteCodeView/InviteCodeTable.vue';
import DeleteConfirmModal from '@/components/Admin/InviteCodeView/DeleteConfirmModal.vue';
import CreateCodeModal from '@/components/Admin/InviteCodeView/CreateCodeModal.vue';

/* ==========================
 * STORE & ROUTE SETUP
 * ========================== */
const route = useRoute();
const joinCodeStore = useAdminJoinCodeStore();

/* ==========================
 * REACTIVE STATE
 * ========================== */
const currentTab = ref<'all' | 'active' | 'expiring' | 'expired'>('all');
const isDeleteModalOpen = ref(false);
const isCreateModalOpen = ref(false);
const newlyCreatedCode = ref<string | null>(null); // 생성된 코드 저장
const targetDeleteId = ref<number | null>(null);
const targetDeleteName = ref('');
const searchText = ref('');

/* ==========================
 * STATUS 계산
 * ========================== */
const calculateStatus = (endedDate: string): '활성' | '만료임박' | '만료' => {
  const now = new Date();
  const end = new Date(endedDate);

  // 만료일의 23:59:59까지 유효한 것으로 처리
  end.setHours(23, 59, 59, 999);

  if (now > end) return '만료';
  
  const diffDays = (end.getTime() - now.getTime()) / (1000 * 60 * 60 * 24);
  if (diffDays <= 7) return '만료임박';
  return '활성';
};

/* ==========================
 * CODES COMPUTED
 * - 상태(status) 계산
 * - 탭 & 검색 필터 적용
 * ========================== */
const codesWithStatus = computed<CodeItem[]>(() =>
  joinCodeStore.joinCodes.map(code => ({
    ...code,
    createdAt: code.createdAt || '-', // undefined 방지
    status: calculateStatus(code.endedDate)
  }))
);

const filteredCodes = computed(() => {
  // 상태별 필터링
  const statusFiltered = currentTab.value === 'all'
    ? codesWithStatus.value
    : codesWithStatus.value.filter(c =>
        (currentTab.value === 'active' && c.status === '활성') ||
        (currentTab.value === 'expiring' && c.status === '만료임박') ||
        (currentTab.value === 'expired' && c.status === '만료')
      );

  // 검색어가 있으면 추가 필터링
  if (searchText.value) {
    return statusFiltered.filter(c =>
      c.joinCodeName.toLowerCase().includes(searchText.value.toLowerCase()) ||
      c.identifyValue.toLowerCase().includes(searchText.value.toLowerCase())
    );
  }

  return statusFiltered; // 검색어 없으면 상태 필터만 적용
});

/* ==========================
 * TABS 카운트
 * ========================== */
const tabs = computed(() => ({
  all: { name: '전체 코드', count: codesWithStatus.value.length },
  active: { name: '활성', count: codesWithStatus.value.filter(c => c.status === '활성').length },
  expiring: { name: '만료 임박', count: codesWithStatus.value.filter(c => c.status === '만료임박').length },
  expired: { name: '만료/중단', count: codesWithStatus.value.filter(c => c.status === '만료').length },
}));

/* ==========================
 * INITIAL DATA FETCH
 * ========================== */
onMounted(() => {
  const workspaceId = Number(route.params.id);
  joinCodeStore.fetchJoinCodes(workspaceId);
});

/* ==========================
 * ACTIONS
 * ========================== */
const openDeleteSingle = (id: number, name: string) => {
  targetDeleteId.value = id;
  targetDeleteName.value = name;
  isDeleteModalOpen.value = true;
};

const handleDelete = async () => {
  if (!targetDeleteId.value) return;
  const workspaceId = Number(route.params.id);
  try {
    await joinCodeStore.deleteJoinCode(targetDeleteId.value, workspaceId);
  } finally {
    isDeleteModalOpen.value = false;
    targetDeleteId.value = null;
    targetDeleteName.value = '';
  }
};

const onCreateCode = async (newData: { joinCodeName: string; endedDate: string }) => {
  const workspaceId = Number(route.params.id);
  try {
    const response = await joinCodeStore.createJoinCode({
      workspaceId,
      joinCodeName: newData.joinCodeName,
      endedDate: newData.endedDate,
    });
    
    // 성공 시 모달에 결과 전달 (모달은 닫히지 않음)
    newlyCreatedCode.value = response.identifyValue;
    
  } catch (e) {
    alert('코드 생성 중 오류가 발생했습니다.');
  }
};

const closeCreateModal = () => {
  isCreateModalOpen.value = false;
  newlyCreatedCode.value = null; // 초기화
};

/* ==========================
 * TABLE 전달
 * ========================== */
const codes = filteredCodes;
</script>
