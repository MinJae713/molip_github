<template>
    <div class="flex flex-col h-screen bg-slate-50">
        <Navbar :user="userStore.user" @notification-click="handleNotification" @profile-click="handleProfile" />
        <div class="flex flex-1 overflow-hidden">
            <!-- 조건부 사이드바 렌더링 -->
            <Sidebar :workspace-name="workspaceStore.currentWorkspace?.name || 'No Workspace'"
                :workspace-logo="workspaceStore.currentWorkspace?.logo_url" current-active-id="dashboard" />

            <main class="flex-1 overflow-y-auto p-4 sm:p-8">
                <RouterView />
            </main>

        </div>
    </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue';
import { RouterView } from 'vue-router'; // 페이지 내용이 들어갈 곳
import Navbar from '@/components/common/Navbar.vue';
import Sidebar from '@/components/common/Sidebar.vue';
import { useUserStore } from '@/stores/user';
import { useWorkspaceStore } from '@/stores/workspace';

// Store 사용
const userStore = useUserStore();
const workspaceStore = useWorkspaceStore();

onMounted(() => {
    // 데이터가 없으면 Fetch, 있으면 캐시 사용
    userStore.fetchUserInfo();
    workspaceStore.fetchMyWorkspaces();
});

const handleNotification = () => {
    console.log('알림 클릭됨');
};

const handleProfile = () => {
    console.log('프로필 클릭됨');
};
</script>

<style scoped>
/* 배경색이 Tailwind Config에 없다면 임시로 지정 */
.bg-slate-50 {
    background-color: #F8FAFC;
}
</style>