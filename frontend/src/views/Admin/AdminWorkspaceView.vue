<script setup lang="ts">
import StatCard from '@/components/Admin/AdminWorkspaceView/StatCard.vue';
import InviteBanner from '@/components/Admin/AdminWorkspaceView/InviteCodeBanner.vue';
import AdminStatTable from '@/components/Admin/AdminWorkspaceView/AdminStatTable.vue';
import CourseDetailModal from '@/components/Admin/AdminWorkspaceView/CourseDetailModal.vue';
import CreateCodeModal from '@/components/Admin/InviteCodeView/CreateCodeModal.vue';
import { useWorkspaceStore } from '@/stores/workspace';
import { useWorkspaceAdminStore } from '@/stores/workspaceAdmin';
import { useRoute } from 'vue-router';
import { ref, computed, onMounted } from 'vue';

const route = useRoute();
const adminStore = useWorkspaceAdminStore();
const workspaceStore = useWorkspaceStore(); // Added store instance

// 날짜 포맷 함수
const formatDate = (dateString?: string) => {
  if (!dateString) return '-';
  const date = new Date(dateString);
  return `${date.getFullYear()}.${String(date.getMonth() + 1).padStart(2, '0')}.${String(date.getDate()).padStart(2, '0')}`;
};

// 모달 상태
const isCreateModalOpen = ref(false);
const newlyCreatedCode = ref<string | null>(null);

// 워크스페이스 정보 로드
onMounted(async () => {
    const wId = route.params.id;
    if (wId) {
        await workspaceStore.setCurrentWorkspaceById(Number(wId));
        
        // [Fix] 생성일(createdAt)이 없는 경우 (User API 캐시 사용 시 발생)
        // Admin API 리스트를 호출하여 정보를 보강함
        if (!workspaceStore.currentWorkspace?.createdAt) {
            console.log("생성일 정보 누락 -> 관리자 목록 조회 시도");
            await workspaceStore.fetchAllWorkspaces();
            await workspaceStore.setCurrentWorkspaceById(Number(wId));
        }
    }
});

// 코드 생성 핸들러
const onCreateCode = async (newData: { joinCodeName: string; endedDate: string }) => {
  const workspaceId = Number(route.params.id);
  try {
    const response = await adminStore.createJoinCode({
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

/**
 * 관리자 대시보드용 요약 통계 데이터 (실제 데이터 연동)
 */
const adminStats = computed(() => {
    const ws = workspaceStore.currentWorkspace;
    return [
    {
        id: 'members', // ID 변경 (순서상 멤버 먼저)
        title: '워크스페이스 멤버',
        value: ws?.memberCount ? `${ws.memberCount.toLocaleString()}명` : '0명',
        icon: 'groups',
        variant: 'blue' as const,
        trend: '전체'
    },
    {
        id: 'ongoing',
        title: '진행 중인 강의', // 이름 변경
        value: ws?.courseCount ? `${ws.courseCount}개` : '0개',
        icon: 'menu_book',
        variant: 'indigo' as const,
        trend: '전체'
    },
    {
        id: 'created',
        title: '워크스페이스 생성일', // 지표 변경
        value: formatDate(ws?.createdAt),
        icon: 'calendar_today', // 아이콘 변경
        variant: 'emerald' as const, // 색상 변경
        trend: '생성일'
    }
    ];
});
</script>

<template>
  <div class="min-h-screen bg-[#F8FAFC] px-6 lg:px-10 pt-4 pb-10 space-y-6">

    <section>
      <InviteBanner @generate="isCreateModalOpen = true" />
      <!-- 생성 모달 -->
      <CreateCodeModal 
        v-if="isCreateModalOpen" 
        :createdCode="newlyCreatedCode"
        @close="closeCreateModal" 
        @create="onCreateCode" 
      />
    </section>

    <section>
      <div class="mb-5 px-1 flex items-center gap-2">
        <span class="material-symbols-outlined text-indigo-500 font-bold">analytics</span>
        <h2 class="text-lg font-bold text-slate-800">워크스페이스 현황 요약</h2>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
        <StatCard v-for="stat in adminStats" :key="stat.id" v-bind="stat">
          <template #badge>
            <div :class="[
              'px-2 py-1 rounded-md text-[11px] font-bold',
              stat.trend.includes('+') ? 'bg-emerald-50 text-emerald-600' : 'bg-slate-100 text-slate-400'
            ]">
              {{ stat.trend }}
            </div>
          </template>
        </StatCard>
      </div>
    </section>

    <section>
      <AdminStatTable />
      <CourseDetailModal />
    </section>

  </div>
</template>

<style scoped>
/* 구글 머티리얼 아이콘 폰트 최적화 */
.material-symbols-outlined {
  font-variation-settings: 'FILL' 1, 'wght' 400, 'GRAD' 0, 'opsz' 24;
}
</style>