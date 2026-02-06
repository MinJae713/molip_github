<template>
    <div class="h-full flex-none border-t border-slate-200 bg-slate-50 flex flex-col">
        <div class="px-4 py-2 border-b border-slate-200 flex items-center justify-between bg-white flex-none">
            <div class="flex items-center gap-2">
                <span class="material-symbols-outlined text-indigo-600 text-[18px]">help</span>
                <h3 class="font-bold text-slate-800 text-xs">AI Q&A</h3>
            </div>
            <span class="text-[9px] font-medium text-slate-500 bg-slate-100 px-2 py-0.5 rounded-full">실시간 질문</span>
        </div>

        <!-- Scrollable Area -->
        <div ref="scrollContainer" class="flex-1 overflow-y-auto p-4 space-y-4 custom-scrollbar bg-slate-50/50">
            <template v-for="msg in qaList" :key="msg.id || msg.timestamp">
                <!-- Question (User) -->
                <div v-if="msg.type === 'question'" class="flex justify-end mb-4">
                    <div class="flex flex-col items-end max-w-[85%]">
                        <div class="bg-indigo-600 text-white rounded-2xl rounded-tr-none px-4 py-2.5 shadow-sm">
                            <p class="text-[12px] leading-relaxed">{{ msg.content }}</p>
                        </div>
                        <span class="text-[10px] text-slate-400 mt-1 mr-1">{{ formatTime(msg.createdAt || msg.timestamp) }}</span>
                    </div>
                </div>

                <!-- Answer (AI) -->
                <div v-else class="flex justify-start mb-4">
                    <div class="flex gap-2 max-w-[90%]">
                        <div class="w-8 h-8 rounded-full bg-emerald-100 flex items-center justify-center shrink-0 border border-emerald-200">
                             <span class="material-symbols-outlined text-emerald-600 text-[18px]">smart_toy</span>
                        </div>
                        <div class="flex flex-col items-start">
                             <span class="text-[10px] text-slate-500 mb-1 ml-1 font-medium">AI Tutor</span>
                            <div class="bg-white border border-slate-200 text-slate-700 rounded-2xl rounded-tl-none px-4 py-2.5 shadow-sm">
                                <p class="text-[12px] leading-relaxed whitespace-pre-wrap">{{ msg.content }}</p>
                            </div>
                            <span class="text-[10px] text-slate-400 mt-1 ml-1">{{ formatTime(msg.createdAt || msg.timestamp) }}</span>
                        </div>
                    </div>
                </div>
            </template>

            <div v-if="qaList.length === 0" class="text-center text-slate-400 text-xs py-10 flex flex-col items-center gap-2">
                <span class="material-symbols-outlined text-3xl opacity-20">contact_support</span>
                <span>궁금한 점을 질문해보세요!</span>
            </div>
        </div>

        <!-- Input Area -->
        <div class="p-3 bg-white border-t border-slate-100 flex-none z-10">
            <div class="relative flex items-center">
                <input v-model="inputMessage" @keyup.enter="sendMessage"
                    class="w-full bg-slate-50 border border-slate-200 rounded-full pl-4 pr-11 py-2.5 text-xs focus:ring-2 focus:ring-indigo-100 focus:border-indigo-500 transition-all outline-none text-slate-800 placeholder:text-slate-400"
                    placeholder="AI 튜터에게 질문을 입력하세요..." type="text" />
                <button @click="sendMessage"
                    class="absolute right-1.5 p-1.5 bg-indigo-600 text-white rounded-full hover:bg-indigo-700 transition-all flex items-center justify-center shadow-sm disabled:opacity-50 disabled:cursor-not-allowed"
                    :disabled="!inputMessage.trim()">
                    <span class="material-symbols-outlined text-[16px]">send</span>
                </button>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, nextTick, watch } from 'vue';

// --- Types ---
// 메시지 타입 정의 (상위 컴포넌트와 일치)
interface QnaMessage {
    id?: string | number;
    questionId?: string | number | null;
    content: string;
    senderName?: string;
    senderId?: number;
    type: 'question' | 'answer';
    createdAt?: string;
    timestamp?: string;
}

const props = defineProps<{
    qaList: QnaMessage[]
}>();

const emit = defineEmits<{
    'send-message': [content: string, type: string, questionId?: number | string | null]
}>();

// --- State ---
const inputMessage = ref('');
const scrollContainer = ref<HTMLDivElement | null>(null);

// --- Actions ---
const sendMessage = () => {
    if (!inputMessage.value.trim()) return;
    
    // 단순화: 사용자는 항상 '새 질문'을 보냄. (questionId는 null로 시작)
    // 백엔드나 상위 컴포넌트가 알아서 처리
    emit('send-message', inputMessage.value, 'question', null);
    inputMessage.value = '';
};

const formatTime = (iso?: string) => {
    if (!iso) return '';
    try {
        return new Date(iso).toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit' });
    } catch (e) {
        return '';
    }
};

const scrollToBottom = async () => {
    await nextTick();
    if (scrollContainer.value) {
        scrollContainer.value.scrollTo({
            top: scrollContainer.value.scrollHeight,
            behavior: 'smooth'
        });
    }
};

// 리스트 변경 시 자동 스크롤
watch(() => props.qaList, (newVal) => {
    console.log("[SidebarChat] QA List Updated:", newVal);
    // 메시지 추가될 때만 스크롤하도록 조건 추가 가능하지만, 일단 항상 스크롤
    if (newVal && newVal.length > 0) {
        scrollToBottom();
    }
}, { deep: true, immediate: true });
</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar {
    width: 6px;
}
.custom-scrollbar::-webkit-scrollbar-track {
    background: transparent;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
    background-color: #cbd5e1;
    border-radius: 3px;
}
</style>