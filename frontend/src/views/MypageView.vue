<template>
    <div class="flex-1 max-w-7xl mx-auto w-full px-4 sm:px-6 lg:px-8 py-10 space-y-12">

        <header>
            <h1 class="text-3xl font-bold text-slate-900">마이페이지</h1>
        </header>
        <!-- 내 정보 -->
        <section class="space-y-6">
            <div class="flex items-center gap-2">
                <span class="material-symbols-outlined text-indigo-brand">badge</span>
                <h2 class="text-xl font-bold text-slate-900">내 정보</h2>
            </div>

            <UserInfoUpdateCard :user="userStore.user" @update-image="handleProfileImageUpdate" />
        </section>
        <!-- 내가 참가한 워크스페이스 -->
        <section class="space-y-6">
            <div class="flex items-center gap-2">
                <span class="material-symbols-outlined text-indigo-brand">domain_verification</span>
                <h3 class="text-xl font-bold text-slate-900">내가 참가한 워크스페이스</h3>
            </div>
            <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">

                <WorkSpaceCardSmall v-for="workspace in workspaceStore.myWorkspaces" :key="workspace.id"
                    :workspace="workspace" @click="goToWorkspace(workspace.id)" />
                <div class="glass-panel border-dashed border-2 border-slate-300 bg-slate-50 p-4 flex items-center justify-center gap-2 hover:border-indigo-300 hover:text-indigo-600 text-slate-500 cursor-pointer transition-colors group"
                    @click="openSearchModal">
                    <span class="material-symbols-outlined group-hover:animate-bounce text-indigo-400">add_circle</span>
                    <span class="font-bold text-sm">새 워크스페이스 찾기</span>
                </div>
            </div>
        </section>
        <!-- 내가 듣고 있는 강의 -->
        <section v-if="ongoingCourses.length > 0" class="space-y-6">
            <div class="flex items-center gap-2">
                <span class="material-symbols-outlined text-indigo-brand">play_circle</span>
                <h3 class="text-xl font-bold text-slate-900">내가 듣고 있는 강의</h3>
            </div>
            <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
                <CourseProgressCard v-for="course in ongoingCourses" :key="course.id" :course="course" />
            </div>
        </section>
    </div>
</template>

<script setup lang="ts">
import { onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/stores/user';
import { useWorkspaceStore } from '@/stores/workspace';
import { useCourseStore } from '@/stores/course';

import UserInfoUpdateCard from '@/components/MypageView/UserInfoUpdateCard.vue';
import WorkSpaceCardSmall from '@/components/MypageView/WorkSpaceCardSmall.vue';
import CourseProgressCard from '@/components/MypageView/CourseProgressCard.vue';

const router = useRouter();
const userStore = useUserStore();
const workspaceStore = useWorkspaceStore();
const courseStore = useCourseStore();

onMounted(() => {
    courseStore.fetchMyCourses();
});

// 진행률이 100%보다 작은 것들만 필터링.
const ongoingCourses = computed(() => {
    return courseStore.myCourses.filter(course => (course.progress || course.progress) < 100);
});

// 진행률이 100인 것들만 필터링.
const completedCourses = computed(() => {
    return courseStore.myCourses.filter(course => (course.progress || course.progress) === 100);
});

const handleProfileImageUpdate = async (file: File) => {
    console.log('서버로 전송할 이미지 파일:', file);
    alert('프로필 이미지가 변경되었습니다.');
};

const goToWorkspace = (id: number | string) => {
    router.push({ name: 'workspaceDetail', params: { id } });
};

const openSearchModal = () => {
    // 워크스페이스 찾기 모달 오픈 로직 또는 페이지 이동
    router.push({ name: 'workspaces' });
};
</script>