<template>
    <div class="w-full lg:w-[400px] flex-none border-l border-slate-200 flex flex-col bg-white h-full relative z-10">
        <!-- 1. 탭 메뉴 (통합됨) -->
        <div class="flex border-b border-slate-100 flex-none bg-white relative z-20">
            <button v-for="tab in tabs" :key="tab.id" @click="currentTab = tab.id"
                class="flex-1 py-3 text-[11px] font-bold transition-all border-b-2 flex flex-col items-center justify-center gap-1 min-w-[60px] relative"
                :class="currentTab === tab.id
                    ? 'text-indigo-600 border-indigo-600 bg-indigo-50/50'
                    : 'text-slate-500 border-transparent hover:text-slate-700 hover:bg-slate-50'">
                <span class="material-symbols-outlined text-[18px]">{{ tab.icon }}</span>
                <span>{{ tab.label }}</span>
                
                <!-- Red Notification Dot -->
                <div v-if="tab.id === 'quiz' && unsubmittedQuizCount > 0"
                     class="absolute top-2 right-[20%] w-4 h-4 rounded-full bg-rose-500 text-white text-[9px] flex items-center justify-center animate-bounce shadow-sm ring-2 ring-white z-20">
                    {{ unsubmittedQuizCount }}
                </div>

                <!-- Speech Bubble Notification (Above Tab) -->
                <div v-if="tab.id === 'quiz' && unsubmittedQuizCount > 0 && currentTab !== 'quiz'"
                     class="absolute bottom-full mb-3 left-1/2 -translate-x-1/2 w-32 bg-slate-800 text-white text-[10px] py-1.5 px-2 rounded-lg shadow-xl z-50 animate-in fade-in slide-in-from-bottom-2 duration-300 pointer-events-none">
                    <div class="text-center font-medium leading-tight">
                        <span class="text-amber-300 font-bold">{{ unsubmittedQuizCount }}건</span>의 미제출 퀴즈!
                    </div>
                    <!-- Arrow Pointing Down -->
                    <div class="absolute top-full left-1/2 -translate-x-1/2 border-4 border-transparent border-t-slate-800"></div>
                </div>
            </button>
        </div>
        <!-- 2. 선택된 탭별 콘텐츠 -->
        <div class="flex-1 overflow-y-auto relative custom-scrollbar">
            <SidebarMood v-if="currentTab === 'mood'" 
                :personal-mood-data="personalMoodData"
                :class-mood-data="classMoodData" 
            />
            <SidebarQuiz v-else-if="currentTab === 'quiz'" 
                :quiz-list="quizList"
                @update:quizList="(newList) => quizList = newList"
            />
            <SidebarScript v-else-if="currentTab === 'script'" 
                :script-list="scriptData"
                @jump-to-time="$emit('jump-to-time', $event)"
            />
        </div>

        <!-- 3. 채팅창 -->
        <div class="flex-none border-t border-slate-200 transition-all duration-300 ease-in-out relative flex flex-col"
             :style="{ height: `${chatHeight}px` }">
            
            <!-- Resizer Handle -->
            <div class="absolute top-0 left-0 right-0 h-1 cursor-ns-resize hover:bg-indigo-300 z-10 group flex items-center justify-center -translate-y-1/2"
                 @mousedown="startResize">
                 <div class="w-10 h-1 bg-slate-300 rounded-full group-hover:bg-indigo-500 transition-colors"></div>
            </div>

            <SidebarChat 
                :qa-list="qaList || []"
                @send-message="(...args) => $emit('send-message', ...args)"
            />
        </div>
    </div>
</template>
<script setup lang="ts">
import { ref, onUnmounted, computed } from 'vue';
// 하위 컴포넌트 임포트
import SidebarMood from './SidebarMood.vue';
import SidebarQuiz from './SidebarQuiz.vue';
import SidebarScript from './SidebarScript.vue';
import SidebarChat from './SidebarChat.vue';

// Props 정의 (상위에서 데이터 받음)
interface ScriptItem {
    timeline: number; 
    message: string;
}

const props = defineProps<{
    personalMoodData?: number[];
    classMoodData?: number[];
    scriptData?: ScriptItem[];
    qaList?: any[]; // Chat list is not needed for QnA only sidebar
}>();

const emit = defineEmits(['jump-to-time', 'send-message']);

const currentTab = ref('mood');

// Interfaces
interface Quiz {
    id: number;
    question: string;
    options: string[];
    answer: number; // 0-indexed correct option
    submittedAnswer: number | null; // null if not submitted
}

// State: Quiz List (Mock Data Hoisted)
const quizList = ref<Quiz[]>([
    {
        id: 1,
        question: "다음 중 Vue.js의 라이프사이클 훅이 아닌 것은?",
        options: ["onMounted", "onCreated", "onUpdate", "onRender"],
        answer: 3,
        submittedAnswer: 3 // Correct
    },
    {
        id: 2,
        question: "반응형 상태를 선언하기 위해 사용하는 API는?",
        options: ["reactive", "response", "state", "redux"],
        answer: 0,
        submittedAnswer: 0 // Correct
    },
    {
        id: 3,
        question: "Pinia는 무엇을 위한 라이브러리인가?",
        options: ["라우팅", "상태 관리", "HTTP 통신", "UI 컴포넌트"],
        answer: 1,
        submittedAnswer: 2 // Incorrect (wrong answer)
    },
    {
        id: 4,
        question: "v-bind 디렉티브의 약어는?",
        options: ["@", ":", "#", "$"],
        answer: 1,
        submittedAnswer: null // Unsubmitted
    },
    {
        id: 5,
        question: "SPA의 약자는?",
        options: ["Single Page Application", "Simple Page Action", "Server Protocol API", "Standard Process App"],
        answer: 0,
        submittedAnswer: 0 // Correct
    }
]);

const unsubmittedQuizCount = computed(() => 
    quizList.value.filter(q => q.submittedAnswer === null).length
);

const tabs = [
    { id: 'mood', label: '세션 분위기', icon: 'mood' },
    { id: 'quiz', label: '퇴실 퀴즈', icon: 'quiz' },
    { id: 'script', label: '스크립트', icon: 'subject' },
];

// --- 채팅창 높이 조절 로직 ---
const chatHeight = ref(280);
const isResizing = ref(false);
let startY = 0;
let startHeight = 0;

const startResize = (e: MouseEvent) => {
    isResizing.value = true;
    startY = e.clientY;
    startHeight = chatHeight.value;
    
    document.addEventListener('mousemove', handleResize);
    document.addEventListener('mouseup', stopResize);
    document.body.style.cursor = 'row-resize';
    document.body.style.userSelect = 'none';
};

const handleResize = (e: MouseEvent) => {
    if (!isResizing.value) return;
    const dy = startY - e.clientY; // 위로 드래그하면 높이 증가
    chatHeight.value = Math.max(150, Math.min(600, startHeight + dy));
};

const stopResize = () => {
    isResizing.value = false;
    document.removeEventListener('mousemove', handleResize);
    document.removeEventListener('mouseup', stopResize);
    document.body.style.cursor = '';
    document.body.style.userSelect = '';
};

// 컴포넌트 해제 시 이벤트 정리
onUnmounted(() => {
    stopResize();
});
</script>