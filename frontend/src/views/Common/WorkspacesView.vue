<template>
    <main class="flex-1 max-w-7xl mx-auto w-full px-4 sm:px-6 lg:px-8 py-10">
        <div class="mb-12">
            <h1 class="text-3xl font-bold text-slate-900 mb-3">내 워크스페이스</h1>
            <p class="text-slate-500 mb-8 text-lg">
                초대 코드를 입력해 워크스페이스에 참여하거나, 참여한 워크스페이스로 이동하세요.
            </p>

            <form @submit.prevent="handleSearch" class="relative max-w-3xl shadow-lg rounded-xl">
                <div class="absolute inset-y-0 left-0 pl-5 flex items-center pointer-events-none">
                    <span class="material-symbols-outlined text-indigo-600 text-2xl">search</span>
                </div>
                <input v-model="searchQuery" type="text" @click="handleSearch"
                    class="block w-full pl-14 pr-24 py-5 bg-white border-0 ring-1 ring-slate-200 rounded-xl text-slate-900 placeholder:text-slate-400 focus:ring-2 focus:ring-indigo-600 text-lg shadow-sm cursor-pointer"
                    placeholder="워크스페이스 이름 또는 초대 코드를 입력하세요" />
                <div class="absolute inset-y-0 right-2 flex items-center">
                    <button type="submit" :disabled="isLoading"
                        class="bg-indigo-600 hover:bg-indigo-700 disabled:bg-slate-300 text-white px-6 py-2.5 rounded-lg font-bold text-base transition-colors">
                        검색
                    </button>
                </div>
            </form>
        </div>
        <div v-if="isLoading" class="flex justify-center py-20">
            <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-600"></div>
        </div>

        <div v-else-if="workspaceStore.myWorkspaces.length === 0"
            class="text-center py-20 bg-white rounded-2xl border border-dashed border-slate-200">
            <span class="material-symbols-outlined text-6xl text-slate-300 mb-4">folder_off</span>
            <p class="text-slate-500 text-lg font-medium">참여 중인 워크스페이스가 없습니다.</p>
            <p class="text-slate-400 text-sm mt-1">위 검색창을 통해 새로운 워크스페이스를 찾아보세요.</p>
        </div>

        <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            <WorkspaceCard v-for="workspace in workspaceStore.myWorkspaces" :key="workspace.workspace_id"
                :workspace="workspace" @enter="handleEnter" />
        </div>

        <!-- 검색 모달 -->
        <WorkspaceSearchModal :is-open="isSearchModalOpen" :initial-query="searchQuery" @close="closeSearchModal" />
    </main>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
// import api from '@/api'; // Removed
import { useWorkspaceStore } from '@/stores/workspace'; // Added
import WorkspaceCard from '@/components/common/WorkspacesView/WorkspaceCard.vue';
import WorkspaceSearchModal from '@/components/common/WorkspacesView/WorkspaceSearchModal.vue';

import type { Workspace } from '@/types';

const router = useRouter();
const workspaceStore = useWorkspaceStore(); // Added
// const myWorkspaces = ref<Workspace[]>([]); // Removed local ref
const searchQuery = ref('');
const isLoading = ref(false);
const isSearchModalOpen = ref(false);

// 2. 데이터 호출 로직 (AJAX) - 전체 목록 로드
const fetchMyWorkspaces = async () => {
    try {
        isLoading.value = true;
        // Store를 통해 데이터 로드 (강제 업데이트)
        await workspaceStore.fetchMyWorkspaces(true);

    } catch (error) {
        console.error('데이터 로드 실패:', error);
    } finally {
        isLoading.value = false;
    }
};

const handleSearch = () => {
    isSearchModalOpen.value = true;
};

const closeSearchModal = () => {
    isSearchModalOpen.value = false;
    // 모달에서 가입 신청 등을 했을 수 있으므로 목록 갱신
    fetchMyWorkspaces();
};

const handleEnter = (id: string | number, role?: string) => {
    const userRole = role || 'LEARNER'; // Fallback to 'LEARNER' if role is undefined

    if (userRole === 'TRAINER') {
        router.push({ name: 'workspaceTrainer', params: { id } });
    } else if (userRole === 'LEARNER') {
        router.push({ name: 'workspaceLearner', params: { id } });
    } else if (userRole === 'ADMIN') {
        router.push({ name: 'workspaceAdmin', params: { id } });
    }
};

onMounted(() => {
    fetchMyWorkspaces();
});
</script>

<style scoped>
/* 기존 브랜드 색상 유지 */
.text-indigo-brand {
    color: #4F46E5;
}

.bg-indigo-brand {
    background-color: #4F46E5;
}
</style>