<template>
    <div class="space-y-8">
        <!-- Breadcrumb -->
        <nav aria-label="Breadcrumb" class="flex text-sm text-slate-500">
            <ol class="flex items-center space-x-2">
                <li><router-link :to="{ name: 'workspaceLearner' }" class="hover:text-indigo-600">홈</router-link></li>
                <li><span class="material-symbols-outlined text-sm text-slate-400">chevron_right</span></li>
                <li><router-link :to="{ name: 'workspaceLearnerCourses' }" class="hover:text-indigo-600">학습
                        관리</router-link></li>
                <li><span class="material-symbols-outlined text-sm text-slate-400">chevron_right</span></li>
                <li><router-link :to="{ name: 'workspaceLearnerCourses' }" class="hover:text-indigo-600">내
                        강의실</router-link></li>
                <li><span class="material-symbols-outlined text-sm text-slate-400">chevron_right</span></li>
                <li class="font-medium text-slate-800">{{ courseStore.courseInfo?.title }}</li>
                < </ol>
        </nav>
        <!-- Header -->
        <ClassroomHeader />
        <div class="grid grid-cols-1 xl:grid-cols-3 gap-6">
            <!-- Left Column: Curriculum -->
            <div class="xl:col-span-2 space-y-6">
                <div class="space-y-6">
                    <div class="flex items-center justify-between">
                        <h2 class="text-lg font-bold text-slate-900 flex items-center gap-2">
                            <span class="material-symbols-outlined text-indigo-600">toc</span>
                            커리큘럼
                        </h2>
                        <span
                            class="text-sm text-slate-500 bg-white px-3 py-1 rounded-full border border-slate-200 shadow-sm font-medium">
                            총 {{ sessionStore.sessionsList.length }}개 세션
                        </span>
                    </div>

                    <div class="space-y-4">
                        <CurriculumList v-for="(session, index) in sessionStore.sessionsList" :key="session.session_id"
                            :session="session" :index="index" />

                        <div v-if="sessionStore.sessionsList.length === 0" class="py-20 text-center text-slate-400">
                            등록된 커리큘럼이 없습니다.
                        </div>
                    </div>
                </div>
            </div>
            <!-- Right Column: Sidebar Widgets -->
            <div class="space-y-6">
                <ClassroomSidebar />
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import ClassroomHeader from '@/components/Learner/Classroom/ClassroomHeader.vue';
import CurriculumList from '@/components/Learner/Classroom/CurriculumItem.vue';
import ClassroomSidebar from '@/components/Learner/Classroom/ClassroomSidebar.vue';

import { useRoute } from 'vue-router';
import { useCourseStore } from '@/stores/course';
import { useSessionStore } from '@/stores/session';

import { onMounted } from 'vue';
import { ConsoleLogger } from 'storybook/internal/node-logger';

const courseStore = useCourseStore()
const sessionStore = useSessionStore()

const route = useRoute();

onMounted(() => {
    const courseId = route.params.courseId;
    const workspaceId = route.params.id;
    if (courseId && typeof courseId === 'string') {
        courseStore.fetchCourseInfo(courseId);

    }
    if ((courseId && typeof courseId === 'string') && (workspaceId && typeof workspaceId === 'string')) {
        sessionStore.fetchSessionsList(courseId, workspaceId);
        console.log("불러오기 성공~!");
    }
});

// In the future, we will fetch course details based on route.params.courseId
</script>
