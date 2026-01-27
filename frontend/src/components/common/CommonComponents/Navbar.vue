<script setup lang="ts">
/**
 * 사용자 정보와 헤더 설정을 위한 인터페이스 정의
 */
interface UserInfo {
    name: string;
    email: string;
    profile_image_url: string;
}

interface Props {
    workspaceName?: string;
    currentPathName?: string;
    user?: UserInfo;
    hasNotifications?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
    workspaceName: '',
    currentPathName: '',
    hasNotifications: false,
    user: () => ({
        name: '',
        email: '',
        profile_image_url: ''
    })
});

const emit = defineEmits<{
    (e: 'notification-click'): void;
    (e: 'profile-click'): void;
}>();

import { useRouter } from 'vue-router';
import { useUserStore } from '@/stores/user';

const router = useRouter();
const userStore = useUserStore();

const handleLogout = () => {
    if (confirm('로그아웃 하시겠습니까?')) {
        userStore.logout();
        router.push('/');
    }
};
</script>

<template>
    <header class="bg-dark-navy-900 text-white border-b border-dark-navy-700 h-16 flex-none z-50">
        <div class="w-full px-4 sm:px-6 lg:px-8 h-full flex items-center justify-between">

            <div class="flex items-center gap-12">
                <router-link to="/workspaces"
                    class="flex items-center gap-2 text-indigo-400 w-56 cursor-pointer hover:text-indigo-300 transition-colors">
                    <span class="material-symbols-outlined text-3xl">school</span>
                    <span class="text-xl font-bold font-display tracking-tight text-white">
                        Molip<span class="text-indigo-400 font-light"> Space</span>
                    </span>
                </router-link>

                <!-- Navigation Links -->
                <nav class="hidden md:flex items-center gap-6 border-l border-slate-700 pl-6">
                    <router-link to="/workspaces"
                        class="text-sm font-medium text-slate-300 hover:text-white transition-colors"
                        active-class="text-white font-bold">
                        워크스테이션
                    </router-link>
                    <router-link to="/mypage"
                        class="text-sm font-medium text-slate-300 hover:text-white transition-colors"
                        active-class="text-white font-bold">
                        마이페이지
                    </router-link>
                </nav>
            </div>

            <!-- 로그인 된 경우 -->
            <div v-if="userStore.isAuthenticated" class="flex items-center gap-4">
                <button @click="handleLogout"
                    class="text-sm font-medium text-slate-300 hover:text-white transition-colors mr-2">
                    로그아웃
                </button>

                <button class="text-white hover:text-indigo-200 transition-colors relative p-1 bg-white/5 rounded-full"
                    @click="emit('notification-click')">
                    <span class="material-symbols-outlined">notifications</span>
                    <span v-if="hasNotifications"
                        class="absolute top-1 right-1 size-2.5 bg-magenta rounded-full border-2 border-dark-navy-900"></span>
                </button>

                <div class="flex items-center gap-3 pl-4 border-l border-slate-700 cursor-pointer"
                    @click="emit('profile-click')">
                    <div class="text-right hidden sm:block">
                        <div class="text-sm font-bold text-white">{{ user.name }}</div>
                        <div class="text-xs text-slate-400">{{ user.email }}</div>
                    </div>

                    <div
                        class="size-9 rounded-full custom-gradient p-[2px] flex items-center justify-center bg-indigo-500 text-white">
                        <img v-if="user.profile_image_url" :src="user.profile_image_url" :alt="user.name"
                            class="rounded-full w-full h-full object-cover border-2 border-dark-navy-900" />
                        <span v-else class="material-symbols-outlined text-xl">person</span>
                    </div>
                </div>
            </div>

            <!-- 로그인 안 된 경우 -->
            <div v-else class="flex items-center gap-4">
                <router-link to="/" class="text-sm font-bold text-indigo-400 hover:text-indigo-300 transition-colors">
                    로그인
                </router-link>
            </div>
        </div>
    </header>
</template>

<style scoped>
.bg-dark-navy-900 {
    background-color: #0A0F1D;
}

.border-dark-navy-700 {
    border-color: #1F2937;
}

.bg-magenta {
    background-color: #D946EF;
}

.font-display {
    font-family: 'Lexend', 'Noto Sans KR', sans-serif;
}

/* 프로필 이미지 테두리 그라데이션 (indigo-brand -> magenta) */
.custom-gradient {
    background: linear-gradient(to top right, #4F46E5, #D946EF);
}

/* 이미지가 깨졌을 때 레이아웃 유지용 (선택 사항) */
img {
    background-color: #1F2937;
}
</style>