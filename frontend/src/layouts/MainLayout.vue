<template>
    <div class="flex flex-col h-screen bg-slate-50">

        <Navbar workspace-name="TechCorp HQ" current-path-name="Workspace Home" :user="user"
            @notification-click="handleNotification" @profile-click="handleProfile" />

        <div class="flex flex-1 overflow-hidden">

            <Sidebar workspace-name="TechCorp HQ" current-active-id="dashboard" />

            <main class="flex-1 overflow-y-auto p-4 sm:p-8">
                <RouterView />
            </main>

        </div>
    </div>
</template>

<script setup lang="ts">
import { RouterView } from 'vue-router';
import { useUserStore } from '@/stores/user';
import Navbar from '@/components/common/Navbar.vue';
import Sidebar from '@/components/common/Sidebar.vue';

const userStore = useUserStore();

// Mock user data - useUserStore에서 가져올 수 있도록 구성
const user = {
    name: '김지수',
    email: 'jisoo.kim@techcorp.com',
    profileImage: 'https://ui-avatars.com/api/?name=Jisoo+Kim&background=random'
};

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