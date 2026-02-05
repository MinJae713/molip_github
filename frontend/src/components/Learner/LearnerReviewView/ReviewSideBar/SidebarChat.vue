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
            <template v-for="group in groupedQaList" :key="group.questionId">
                <div @click="toggleSelectQuestion(group.questionId)"
                    class="bg-white border rounded-xl shadow-sm overflow-hidden cursor-pointer transition-all duration-200"
                    :class="selectedQuestionId === group.questionId ? 'border-indigo-600 ring-2 ring-indigo-100' : 'border-slate-200 hover:border-indigo-300'">

                    <!-- Question -->
                    <div class="p-3 border-l-4"
                        :class="selectedQuestionId === group.questionId ? 'border-l-indigo-600' : 'border-l-slate-400'">
                        <div class="flex justify-between items-start mb-2">
                            <div class="flex items-center gap-2">
                                <span class="text-[10px] font-bold px-1.5 py-0.5 rounded"
                                    :class="selectedQuestionId === group.questionId ? 'bg-indigo-600 text-white' : 'bg-slate-100 text-slate-600'">
                                    {{ selectedQuestionId === group.questionId ? '답변 작성중' : '질문' }}
                                </span>
                                <span class="text-[10px] text-slate-400">{{ formatTime(group.createdAt || group.timestamp) }}</span>
                            </div>
                        </div>
                        <p class="text-xs text-slate-800 font-bold leading-relaxed">Q: {{ group.content }}</p>
                        <div class="text-[10px] text-slate-400 mt-1 flex justify-between">
                            <span>작성자: {{ group.senderName || group.name }}</span>
                        </div>
                    </div>

                    <!-- Replies -->
                    <div v-if="group.replies.length > 0"
                        class="bg-slate-50/50 p-3 border-t border-slate-100 space-y-3">
                        <div v-for="reply in group.replies" :key="reply.id" class="flex gap-2">
                            <span class="text-emerald-600 font-bold text-[11px] shrink-0 mt-0.5">A.</span>
                            <div class="flex-1">
                                <p class="text-[11px] text-slate-700 leading-relaxed">{{ reply.content }}</p>
                                <div class="flex justify-between mt-1">
                                    <span class="text-[9px] text-slate-400 font-medium">{{ reply.senderName || reply.name }}</span>
                                    <span class="text-[9px] text-slate-400">{{ formatTime(reply.createdAt || reply.timestamp) }}</span>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </template>

            <div v-if="groupedQaList.length === 0" class="text-center text-slate-400 text-xs py-10 flex flex-col items-center gap-2">
                <span class="material-symbols-outlined text-3xl opacity-20">contact_support</span>
                <span>궁금한 점을 질문해보세요!</span>
            </div>
        </div>

        <!-- Input Area -->
        <div class="p-3 bg-white border-t border-slate-100 flex-none z-10">
            <div v-if="selectedQuestionId"
                class="flex justify-between items-center mb-2 px-3 py-1.5 bg-indigo-50 rounded-lg text-[10px] text-indigo-700 border border-indigo-100">
                <span class="font-bold flex items-center gap-1">
                    <span class="material-symbols-outlined text-[12px]">reply</span>
                    선택한 질문에 답변하는 중입니다.
                </span>
                <button @click.stop="selectedQuestionId = null" class="underline hover:text-indigo-900">취소</button>
            </div>

            <div class="relative flex items-center">
                <input v-model="inputMessage" @keyup.enter="sendMessage"
                    class="w-full bg-slate-50 border border-slate-200 rounded-full pl-4 pr-11 py-2.5 text-xs focus:ring-2 focus:ring-indigo-100 focus:border-indigo-500 transition-all outline-none text-slate-800 placeholder:text-slate-400"
                    :placeholder="getPlaceholder()" type="text" />
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
import { ref, computed, nextTick, watch } from 'vue';

// --- Types ---
interface Message {
    id?: number;
    type?: string;
    questionId: number | string;
    content: string;
    senderName?: string;
    name?: string;
    createdAt?: string;
    timestamp?: string;
}

interface GroupedQa extends Message {
    replies: Message[];
}

const props = defineProps<{
    qaList: any[]
}>();

const emit = defineEmits<{
    'send-message': [content: string, type: string, questionId?: number | string | null]
}>();

// --- State ---
const inputMessage = ref('');
const scrollContainer = ref<HTMLDivElement | null>(null);
const selectedQuestionId = ref<number | string | null>(null);

// --- Logic: Grouping QnA ---
const groupedQaList = computed(() => {
    const groups: Record<string, GroupedQa> = {};

    props.qaList.forEach((msg) => {
        // questionId가 없으면 무시 (안전장치)
        if (!msg.questionId) return;

        const qId = String(msg.questionId);

        // 질문 메시지 (보통 type이 'question'이거나, 맨 처음 온 메시지로 판단 가능하지만, 여기선 questionId로 묶음)
        // 로직: 그룹이 없으면 생성(질문으로 간주), 있으면 replies에 추가(답변으로 간주)
        // [주의] 실제로는 메시지 타입이나 순서에 따라 '질문'과 '답변'을 명확히 구분해야 함.
        // 여기서는 간단히: "해당 ID의 첫 메시지를 질문, 이후를 답변"으로 처리하거나,
        // BE에서 type을 주면 type === 'question'을 헤더로 씀.
        
        // 간단한 가정: 이 리스트에 들어오는건 모두 QnA 관련 메시지.
        // 만약 msg.type === 'question' 이면 그룹 생성 우선권.
        
        if (!groups[qId]) {
            // 아직 그룹이 없으면 현재 메시지로 초기화
            groups[qId] = { ...msg, replies: [] };
        } else {
            // 이미 그룹이 있으면
            if (msg.type === 'question' && groups[qId].type !== 'question') {
                 // 혹시 순서가 꼬여서 질문이 나중에 왔다면 교체 (드물겠지만)
                 const old = { ...groups[qId] };
                 groups[qId] = { ...msg, replies: [...old.replies] };
                 // 기존 꺼는 답변으로? (애매함, 일단 무시)
            } else {
                groups[qId].replies.push(msg);
            }
        }
    });

    return Object.values(groups).sort((a, b) => {
         // 최신 질문이 아래로? 위로? -> 보통 채팅은 아래가 최신
         const timeA = new Date(a.createdAt || a.timestamp || 0).getTime();
         const timeB = new Date(b.createdAt || b.timestamp || 0).getTime();
         return timeA - timeB;
    });
});

// --- Actions ---
const sendMessage = () => {
    if (!inputMessage.value.trim()) return;

    let finalQuestionId: number | string | null = null;
    let messageType = 'question';

    if (selectedQuestionId.value) {
        // 답변 작성
        finalQuestionId = selectedQuestionId.value;
        // 답변은 type을 구분할 수 있으면 좋음, 일단 BE 스펙에 따름 (여기선 questionId 유무로 판단한다고 가정)
    } else {
        // 새 질문
        // 프론트에서 ID 생성? 아니면 null로 보내서 BE가 생성?
        // 기존 로직(LiveChatContainer) 참고: 새 질문일 때 questionId 생성해서 보냄
        finalQuestionId = Math.floor(Date.now() + Math.random() * 1000); 
    }

    emit('send-message', inputMessage.value, messageType, finalQuestionId);
    inputMessage.value = '';
};

const toggleSelectQuestion = (id: number | string | null) => {
    if (selectedQuestionId.value === id) {
        selectedQuestionId.value = null;
    } else {
        selectedQuestionId.value = id;
    }
};

const getPlaceholder = () => {
    if (selectedQuestionId.value) return '선택한 질문에 답변을 입력하세요...';
    return '새로운 질문을 입력하세요...';
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
        // 부드럽게 스크롤
        scrollContainer.value.scrollTo({
            top: scrollContainer.value.scrollHeight,
            behavior: 'smooth'
        });
    }
};

// 리스트 변경 시 자동 스크롤
watch(() => props.qaList, (newVal) => {
    console.log("[SidebarChat] QA List Updated:", newVal);
    scrollToBottom();
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