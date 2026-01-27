<!-- src/components/Learner/LearnerReviewView/ReviewSideBar/ReviewSideBar.vue -->
<template>
    <div class="w-full lg:w-[400px] flex-none border-l border-slate-200 flex flex-col bg-white h-full overflow-hidden">
        <!-- 1. 탭 메뉴 (통합됨) -->
        <div class="flex border-b border-slate-100 overflow-x-auto scrollbar-hide flex-none bg-white">
            <button v-for="tab in tabs" :key="tab.id" @click="currentTab = tab.id"
                class="flex-1 py-3 text-[11px] font-bold transition-all border-b-2 flex flex-col items-center justify-center gap-1 min-w-[60px]"
                :class="currentTab === tab.id
                    ? 'text-indigo-600 border-indigo-600 bg-indigo-50/50'
                    : 'text-slate-500 border-transparent hover:text-slate-700 hover:bg-slate-50'">
                <span class="material-symbols-outlined text-[18px]">{{ tab.icon }}</span>
                <span>{{ tab.label }}</span>
            </button>
        </div>
        <!-- 2. 선택된 탭별 콘텐츠 -->
        <SidebarMood v-if="currentTab === 'mood'" />
        <SidebarQuiz v-else-if="currentTab === 'quiz'" />
        <SidebarScript v-else-if="currentTab === 'script'" />
        <!-- 3. 하단 채팅 -->
        <SidebarChat />
    </div>
</template>
<script setup lang="ts">
import { ref } from 'vue';
// 하위 컴포넌트 임포트
import SidebarMood from './SidebarMood.vue';
import SidebarQuiz from './SidebarQuiz.vue';
import SidebarScript from './SidebarScript.vue';
import SidebarChat from './SidebarChat.vue';
const currentTab = ref('mood');
const tabs = [
    { id: 'mood', label: '세션 분위기', icon: 'mood' },
    { id: 'quiz', label: '퇴실 퀴즈', icon: 'quiz' },
    { id: 'script', label: '스크립트', icon: 'subject' },
];
</script>