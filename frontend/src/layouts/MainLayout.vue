<template>
    <div class="flex flex-col h-screen bg-slate-50">
        <Navbar :user="userStore.user" @notification-click="handleNotification" @profile-click="handleProfile" />
        <div class="flex flex-1 overflow-hidden">
            <!-- 조건부 사이드바 렌더링 -->
            <Sidebar current-active-id="dashboard" />

            <main class="flex-1 overflow-y-auto p-4 sm:p-8">
                <RouterView />
            </main>

        </div>
    </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { RouterView } from 'vue-router'; // 페이지 내용이 들어갈 곳
import Navbar from '@/components/common/Navbar.vue';
import Sidebar from '@/components/common/Sidebar.vue';

// 필요한 경우 헤더나 사이드바에 넘겨줄 데이터를 여기서 관리합니다.
const user = ref({
    name: '김지수',
    email: 'jisoo.kim@techcorp.com',
    profileImage: 'https://ui-avatars.com/api/?name=Jisoo+Kim&background=random'
});

const handleNotification = () => {
    console.log('알림 클릭됨');
};

const handleProfile = () => {
    console.log('프로필 클릭됨');
};
</script>

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

<style scoped>
/* 배경색이 Tailwind Config에 없다면 임시로 지정 */
.bg-slate-50 {
    background-color: #F8FAFC;
}
</style>