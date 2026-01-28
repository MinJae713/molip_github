<script setup lang="ts">
import { onMounted } from 'vue';
import { RouterView } from 'vue-router';
import Navbar from '@/components/common/CommonComponents/Navbar.vue';
import MypageSidebar from '@/components/common/MypageView/MypageSidebar.vue';
import { useUserStore } from '@/stores/user';
import { useWorkspaceStore } from '@/stores/workspace';

import { useLearnerCourseStore } from '@/stores/learnerCourse';

const userStore = useUserStore();
const workspaceStore = useWorkspaceStore();
const courseStore = useLearnerCourseStore();

onMounted(() => {
    userStore.fetchUserInfo();
    courseStore.fetchMyEnrolledCourses();
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
        <Navbar :user="userStore.user" @notification-click="handleNotification" @profile-click="handleProfile" />
        <div class="flex flex-1 overflow-hidden">
            <MypageSidebar />
            <main class="flex-1 overflow-y-auto p-4 sm:p-8">
                <RouterView />
            </main>

        </div>
    </div>
</template>

<style scoped>
.bg-slate-50 {
    background-color: #F8FAFC;
}
</style>