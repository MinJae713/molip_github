<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { useWorkspaceAdminStore } from '@/stores/workspaceAdmin';
import MemberTable from '@/components/Admin/MemberManagementView/MemberTable.vue';

const route = useRoute();
const adminStore = useWorkspaceAdminStore();

// 초기 데이터 로드
onMounted(() => {
  const workspaceId = Number(route.params.id);
  if (workspaceId) {
    adminStore.fetchMembers(workspaceId);
  }
});

// 통계 데이터

</script>

<template>
  <main class="flex-1 bg-[#F8FAFC] min-h-screen p-8">
    <div class="max-w-6xl mx-auto">
      
      <header class="mb-8 flex flex-col gap-1">
        <h1 class="text-2xl font-bold text-slate-900 tracking-tight">참여진 관리</h1>
        <p class="text-slate-500 text-sm">
          워크스페이스에 참여 중인 모든 멤버의 역할과 상태를 관리합니다.
        </p>
      </header>

      <section>
        <MemberTable />
      </section>

    </div>
  </main>
</template>

<style scoped>
/* 페이지 전환 애니메이션 등을 추가할 수 있습니다 */
</style>