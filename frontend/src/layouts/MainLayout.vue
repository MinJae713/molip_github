<template>
    <div class="flex flex-col h-screen bg-slate-50">
        <Navbar workspace-name={{ workspaceStore.currentWorkspace?.name }} current-path-name="Workspace Home"
            :user="userStore.user" @notification-click="handleNotification" @profile-click="handleProfile" />
        <div class="flex flex-1 overflow-hidden">

            <Sidebar workspace-name={{ workspaceStore.currentWorkspace?.name }} current-active-id="dashboard" />

            <main class="flex-1 overflow-y-auto p-4 sm:p-8">
                <RouterView />
            </main>

        </div>
    </div>
</template>

<script setup lang="ts">
import { RouterView, useRouter } from 'vue-router';
import { useUserStore } from '@/stores/user';
import { useWorkspaceStore } from '@/stores/workspace';
import Navbar from '@/components/common/CommonComponents/Navbar.vue';
import Sidebar from '@/components/common/CommonComponents/Sidebar.vue';

import { onMounted } from 'vue';

const userStore = useUserStore();
const workspaceStore = useWorkspaceStore();
const router = useRouter();

onMounted(() => {
    userStore.fetchUserInfo();
});

const handleNotification = () => {
    console.log('알림 클릭됨');
};

const handleProfile = () => {
    console.log('프로필 클릭됨');
    router.push('/mypage');
};
</script>

<style scoped>
/* 배경색이 Tailwind Config에 없다면 임시로 지정 */
.bg-slate-50 {
    background-color: #F8FAFC;
}
</style>