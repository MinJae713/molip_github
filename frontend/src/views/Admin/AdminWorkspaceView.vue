<script setup lang="ts">
import StatCard from '@/components/Admin/AdminWorkspaceView/StatCard.vue';
import InviteBanner from '@/components/Admin/AdminWorkspaceView/InviteCodeBanner.vue';
import AdminStatTable from '@/components/Admin/AdminWorkspaceView/AdminStatTable.vue';
import CourseDetailModal from '@/components/Admin/AdminWorkspaceView/CourseDetailModal.vue';
import CreateCodeModal from '@/components/Admin/InviteCodeView/CreateCodeModal.vue';
import { useWorkspaceAdminStore } from '@/stores/workspaceAdmin';
import { ref } from 'vue';
import { useRoute } from 'vue-router';

const route = useRoute();
const adminStore = useWorkspaceAdminStore();

// 모달 상태
const isCreateModalOpen = ref(false);
const newlyCreatedCode = ref<string | null>(null);

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
 * 관리자 대시보드용 요약 통계 데이터
 */
const adminStats = [
  {
    id: 'ongoing',
    title: '진행중인 강의',
    value: 42,
    icon: 'menu_book',
    variant: 'indigo',
    trend: '+12%'
  },
  {
    id: 'members',
    title: '워크스페이스 멤버',
    value: '1,284',
    icon: 'groups',
    variant: 'blue',
    trend: '+48'
  },
  {
    id: 'rate',
    title: '평균 수료율',
    value: '87.5%',
    icon: 'verified_user',
    variant: 'amber',
    trend: '평균'
  }
] as const;
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