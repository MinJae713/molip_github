<template>
  <div class="p-8 text-slate-900 bg-slate-50/30 min-h-screen">
    <div class="flex justify-between items-start mb-8">
      <div>
        <h1 class="text-2xl font-bold mb-1">가입 신청 관리</h1>
        <p class="text-slate-500 text-sm">워크스페이스 가입 요청에 대한 승인 및 반려 내역을 관리합니다.</p>
      </div>
      <div class="bg-white border border-slate-200 rounded-xl p-4 flex items-center gap-4 shadow-sm">
        <div class="size-10 bg-indigo-50 rounded-lg flex items-center justify-center">
          <span class="material-symbols-outlined text-indigo-600 font-variation-fill">assignment_ind</span>
        </div>
        <div>
          <div class="text-xs text-slate-500 font-medium">승인 대기 중</div>
          <div class="text-xl font-bold text-slate-900">{{ pendingCount }}건</div>
        </div>
      </div>
    </div>

    <div class="bg-white border border-slate-200 rounded-2xl shadow-sm overflow-hidden">
      <div class="p-6 border-b border-slate-100 flex justify-between items-center bg-white">
        <div class="flex gap-2">
          <button v-for="(label, status) in tabs" :key="status"
            @click="currentFilter = status"
            :class="['px-4 py-2 rounded-lg text-sm font-bold transition-all flex items-center gap-2', 
                     currentFilter === status ? 'bg-indigo-50 text-indigo-600' : 'text-slate-500 hover:bg-slate-50']">
            {{ label }}
            <span :class="['px-1.5 py-0.5 rounded-md text-[10px]', currentFilter === status ? 'bg-indigo-100' : 'bg-slate-100 text-slate-400']">
              {{ getCountByStatus(String(status)) }}
            </span>
          </button>
        </div>
        <div class="relative">
          <span class="material-symbols-outlined absolute left-3 top-2.5 text-slate-400 text-xl font-light">search</span>
          <input v-model="searchQuery" type="text" placeholder="이름 또는 이메일 검색..." 
                 class="pl-10 pr-4 py-2 border border-slate-200 rounded-lg text-sm w-64 focus:outline-none focus:ring-2 focus:ring-indigo-500/20">
        </div>
      </div>

      <JoinRequestTable 
        :requests="filteredRequests" 
        :columns="currentColumns"
        :statusStyles="statusStyles"
        @approve="handleApprove"
        @reject="handleReject"
      />

      <div class="p-6 border-t border-slate-100 flex justify-between items-center bg-white">
        <p class="text-sm text-slate-500">
          <span v-if="searchQuery">전체 {{ getCountByStatus(currentFilter) }}건 중 </span>
          <span v-else>전체 </span>
          <span class="font-bold text-slate-900">{{ filteredRequests.length }}건</span>
          <span v-if="searchQuery"> 표시됨</span>
        </p>
        <!-- Pagination Removed -->
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRoute } from 'vue-router';
import { useWorkspaceAdminStore } from '@/stores/workspaceAdmin';
import JoinRequestTable from '@/components/Admin/JoinRequestView/JoinRequestTable.vue';
import type { JoinRequest } from '@/types';

const route = useRoute();
const adminStore = useWorkspaceAdminStore();

const currentFilter = ref('PENDING'); // 기본값: 승인 대기
const searchQuery = ref('');
const tabs = { PENDING: '승인 대기', APPROVE: '승인 완료', REJECTED: '반려' };

const statusStyles = {
  PENDING: { label: '승인 대기', bgClass: 'bg-amber-50 text-amber-600 border border-amber-100' },
  APPROVE: { label: '승인 완료', bgClass: 'bg-emerald-50 text-emerald-600 border border-emerald-100' },
  REJECTED: { label: '반려됨', bgClass: 'bg-rose-50 text-rose-600 border border-rose-100' }
};

const columnConfigs: Record<string, any[]> = {
  PENDING: [{ key: 'info', label: '신청자 정보' }, { key: 'applied_at', label: '신청일', align: 'center' }, { key: 'status', label: '상태', align: 'center' }, { key: 'manage', label: '관리', align: 'center' }],
  APPROVE: [{ key: 'info', label: '신청자 정보' }, { key: 'applied_at', label: '신청일', align: 'center' }, { key: 'approved_at', label: '승인 완료일', align: 'center' }, { key: 'status', label: '상태', align: 'center' }],
  REJECTED: [{ key: 'info', label: '신청자 정보' }, { key: 'applied_at', label: '신청일', align: 'center' }, { key: 'rejected_at', label: '반려일', align: 'center' }, { key: 'status', label: '상태', align: 'center' }, { key: 'manage', label: '관리', align: 'center' }]
};

// [TypeScript 해결] undefined 방지를 위해 ?? [] 추가
const currentColumns = computed(() => columnConfigs[currentFilter.value] ?? []);

onMounted(() => {
  const workspaceId = Number(route.params.id);
  adminStore.fetchJoinRequests(workspaceId);
});

const filteredRequests = computed(() => {
  return adminStore.joinRequests.filter(req => {
    const matchesStatus = req.status === currentFilter.value;
    const matchesSearch = (req.name || '').toLowerCase().includes(searchQuery.value.toLowerCase()) || 
                          (req.email || '').toLowerCase().includes(searchQuery.value.toLowerCase());
    return matchesStatus && matchesSearch;
  });
});

const pendingCount = computed(() => adminStore.joinRequests.filter(r => r.status === 'PENDING').length);
const getCountByStatus = (status: string) => adminStore.joinRequests.filter(r => r.status === status).length;

// [에러 해결] 이벤트 핸들러 추가
const handleApprove = async (req: JoinRequest) => {
  if (confirm(`${req.name}님을 승인하시겠습니까?`)) {
     const workspaceId = Number(route.params.id);
     await adminStore.updateJoinRequestStatus(workspaceId, req.workspaceRegisterId, 'APPROVE');
  }
};
const handleReject = async (req: JoinRequest) => {
  if (confirm(`${req.name}님을 반려하시겠습니까?`)) {
     const workspaceId = Number(route.params.id);
     await adminStore.updateJoinRequestStatus(workspaceId, req.workspaceRegisterId, 'REJECTED');
  }
};
</script>