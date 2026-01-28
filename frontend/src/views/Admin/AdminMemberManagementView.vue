<script setup lang="ts">
import { ref } from 'vue';
import MemberTable from '@/components/Admin/MemberManagementView/MemberTable.vue';

// 통계 데이터 (이미지 하단 등에 활용 가능)
const totalMembers = ref(1284);
const displayedCount = ref(5);
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

      <footer class="mt-6 flex items-center justify-between">
        <div class="text-sm text-slate-500">
          전체 <span class="font-bold text-slate-900">{{ totalMembers.toLocaleString() }}명</span>의 참여자 중 
          {{ displayedCount }}명 표시됨
        </div>
        
        <nav class="flex items-center gap-1">
          <button class="p-2 text-slate-400 hover:text-slate-600 disabled:opacity-30" disabled>
            <span class="material-symbols-outlined">chevron_left</span>
          </button>
          <div class="flex items-center gap-1">
            <button class="size-8 rounded-lg bg-indigo-600 text-white text-xs font-bold">1</button>
            <button class="size-8 rounded-lg text-slate-600 text-xs font-medium hover:bg-slate-100">2</button>
            <button class="size-8 rounded-lg text-slate-600 text-xs font-medium hover:bg-slate-100">3</button>
            <span class="px-1 text-slate-400">...</span>
            <button class="size-8 rounded-lg text-slate-600 text-xs font-medium hover:bg-slate-100">257</button>
          </div>
          <button class="p-2 text-slate-400 hover:text-slate-600">
            <span class="material-symbols-outlined">chevron_right</span>
          </button>
        </nav>
      </footer>

    </div>
  </main>
</template>

<style scoped>
/* 페이지 전환 애니메이션 등을 추가할 수 있습니다 */
</style>