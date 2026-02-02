<template>
  <div class="p-8 text-slate-900 bg-slate-50/30 min-h-screen">
    <div class="flex justify-between items-start mb-8">
      <div>
        <h1 class="text-2xl font-bold mb-1">가입 신청 관리</h1>
        <p class="text-slate-500 text-sm">워크스페이스 가입 요청에 대한 승인 및 거절 내역을 관리합니다.</p>
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
            @click="currentFilter = Number(status)"
            :class="['px-4 py-2 rounded-lg text-sm font-bold transition-all', 
                     currentFilter === Number(status) ? 'bg-indigo-600 text-white shadow-md' : 'text-slate-500 hover:bg-slate-50']">
            {{ label }} ({{ getCountByStatus(Number(status)) }})
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
          전체 <span class="font-bold text-slate-900">{{ getCountByStatus(currentFilter) }}건</span> 중 5건 표시됨
        </p>
        <div class="flex items-center gap-1">
          <button class="p-2 text-slate-400 hover:bg-slate-50 rounded-lg"><span class="material-symbols-outlined">chevron_left</span></button>
          <button class="size-9 rounded-lg bg-indigo-600 text-white font-bold text-sm">1</button>
          <button class="size-9 rounded-lg text-slate-500 font-medium text-sm hover:bg-slate-50">2</button>
          <button class="size-9 rounded-lg text-slate-500 font-medium text-sm hover:bg-slate-50">3</button>
          <button class="p-2 text-slate-400 hover:bg-slate-50 rounded-lg"><span class="material-symbols-outlined">chevron_right</span></button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRoute } from 'vue-router';
import { useMemberStore } from '@/stores/member';
import JoinRequestTable from '@/components/Admin/JoinRequestView/JoinRequestTable.vue';

const route = useRoute();
const memberStore = useMemberStore();

const currentFilter = ref(0);
const searchQuery = ref('');
const tabs = { 0: '승인 대기', 1: '승인 완료', 2: '반려' };

const statusStyles = {
  0: { label: '승인 대기', bgClass: 'bg-amber-50 text-amber-600' },
  1: { label: 'APPROVED', bgClass: 'bg-emerald-50 text-emerald-600' },
  2: { label: 'REJECTED', bgClass: 'bg-rose-50 text-rose-600' }
};

const columnConfigs: Record<number, any[]> = {
  0: [{ key: 'info', label: '신청자 정보' }, { key: 'applied_at', label: '신청일', align: 'center' }, { key: 'status', label: '상태', align: 'center' }, { key: 'manage', label: '관리', align: 'center' }],
  1: [{ key: 'info', label: '신청자 정보' }, { key: 'applied_at', label: '신청일', align: 'center' }, { key: 'approved_at', label: '승인 완료일', align: 'center' }, { key: 'status', label: '상태', align: 'center' }, { key: 'manage', label: '관리', align: 'center' }],
  2: [{ key: 'info', label: '신청자 정보' }, { key: 'applied_at', label: '신청일', align: 'center' }, { key: 'rejected_at', label: '반려일', align: 'center' }, { key: 'reject_reason', label: '반려 사유', align: 'center' }, { key: 'status', label: '상태', align: 'center' }]
};

// [TypeScript 해결] undefined 방지를 위해 ?? [] 추가
const currentColumns = computed(() => columnConfigs[currentFilter.value] ?? []);

onMounted(() => {
  const workspaceId = route.params.id as string;
  memberStore.fetchJoinRequests(workspaceId);
});

const filteredRequests = computed(() => {
  return memberStore.joinRequests.filter(req => {
    const matchesStatus = req.status === currentFilter.value;
    const matchesSearch = (req.user?.name || '').toLowerCase().includes(searchQuery.value.toLowerCase()) || 
                          (req.user?.email || '').toLowerCase().includes(searchQuery.value.toLowerCase());
    return matchesStatus && matchesSearch;
  });
});

const pendingCount = computed(() => memberStore.joinRequests.filter(r => r.status === 0).length);
const getCountByStatus = (status: number) => memberStore.joinRequests.filter(r => r.status === status).length;

// [에러 해결] 이벤트 핸들러 추가
const handleApprove = async (id: number) => {
  if (confirm('이 신청자를 승인하시겠습니까?')) await memberStore.updateRequestStatus(id, 1);
};
const handleReject = async (id: number) => {
  if (confirm('이 신청자를 반려하시겠습니까?')) await memberStore.updateRequestStatus(id, 2);
};
</script>