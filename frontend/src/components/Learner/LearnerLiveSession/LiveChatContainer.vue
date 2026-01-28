<template>
    <div
        class="flex-1 bg-white border border-slate-200 rounded-2xl shadow-sm flex flex-col overflow-hidden min-h-0 h-full">
        <!-- 탭 선택 부분 -->
        <div class="flex border-b border-slate-100 bg-slate-50/80 shrink-0">
            <button @click="activeTab = 'chat'"
                :class="['flex-1 py-3 text-xs font-bold transition-colors border-b-2',
                    activeTab === 'chat' ? 'text-indigo-600 border-indigo-600 bg-white' : 'text-slate-400 hover:text-slate-600 border-transparent']">
                채팅
            </button>
            <button @click="activeTab = 'qa'"
                :class="['flex-1 py-3 text-xs font-bold transition-colors border-b-2 flex items-center justify-center gap-1.5',
                    activeTab === 'qa' ? 'text-indigo-600 border-indigo-600 bg-white' : 'text-slate-400 hover:text-slate-600 border-transparent']">
                질문하기
                <span v-if="hasNewQa" class="size-1.5 rounded-full bg-pink-500 animate-pulse"></span>
            </button>
        </div>
        <!-- 메시지 표시 부분 -->
        <div ref="scrollContainer" class="flex-1 overflow-y-auto p-4 space-y-4 bg-slate-50/30">
            <!-- 채팅 메시지 -->
            <div v-if="activeTab === 'chat'" class="flex flex-col gap-3">
                <div v-for="(msg, index) in chatList" :key="index" class="flex gap-2">
                    <div class="size-7 rounded-full bg-gradient-to-br from-indigo-400 to-cyan-400 shrink-0"></div>
                    <div class="flex-1">
                        <div class="flex items-center gap-2 mb-1">
                            <span class="text-xs font-bold text-slate-800">{{ msg.senderName }}</span>
                            <span class="text-[10px] text-slate-400">{{ formatTime(msg.createdAt) }}</span>
                        </div>
                        <p class="text-xs text-slate-600">{{ msg.content }}</p>
                    </div>
                </div>
                <div v-if="chatList.length === 0" class="text-center text-slate-400 text-xs py-10">
                    채팅이 시작되었습니다.
                </div>
            </div>
            <!-- 질문 메시지 -->
            <div v-else class="flex flex-col gap-3">
                <div v-for="(qa, index) in qaList" :key="index"
                    :class="['bg-white border border-slate-200 p-3 rounded-xl shadow-sm', qa.status === 'NEW' && 'border-l-4 border-l-pink-500']">
                    <div class="flex items-center gap-2 mb-2">
                        <span :class="['text-[10px] font-bold px-1.5 py-0.5 rounded',
                            qa.status === 'ANSWERED' ? 'bg-amber-100 text-amber-700' : 'bg-pink-100 text-pink-600']">
                            {{ qa.status === 'ANSWERED' ? '답변완료' : '새 질문' }}
                        </span>
                        <span class="text-[10px] text-slate-400">{{ formatTime(qa.createdAt) }}</span>
                    </div>
                    <p class="text-xs text-slate-800 font-medium mb-2">Q: {{ qa.content }}</p>

                    <div v-if="qa.answer" class="bg-slate-50 rounded-lg p-2.5 border-l-2 border-indigo-600">
                        <p class="text-[11px] text-slate-600 leading-relaxed">
                            <span class="font-bold text-indigo-600 mr-1">A:</span> {{ qa.answer }}
                        </p>
                    </div>
                    <p v-else class="text-[10px] text-slate-400 mt-2">답변 대기 중...</p>
                </div>
                <div v-if="qaList.length === 0" class="text-center text-slate-400 text-xs py-10">
                    궁금한 점을 질문해보세요!
                </div>
            </div>
        </div>
        <!-- 메시지 입력 부분 -->
        <div class="p-3 bg-white border-t border-slate-100 shrink-0">
            <div class="relative flex items-center">
                <input v-model="inputMessage" @keyup.enter="sendMessage"
                    class="w-full bg-slate-50 border border-slate-200 rounded-full pl-4 pr-11 py-2.5 text-xs focus:ring-2 focus:ring-indigo-200 focus:border-indigo-600 transition-all outline-none text-slate-800 placeholder:text-slate-400"
                    :placeholder="activeTab === 'chat' ? '채팅 메시지 입력...' : '교수님께 질문하기...'" type="text" />
                <button @click="sendMessage"
                    class="absolute right-1.5 p-1.5 bg-indigo-600 text-white rounded-full hover:bg-indigo-700 hover:shadow-lg hover:-translate-y-0.5 transition-all flex items-center justify-center group">
                    <span
                        class="material-symbols-outlined text-[16px] group-hover:rotate-12 transition-transform">send</span>
                </button>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick, watch } from 'vue';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';
const socketUrl = import.meta.env.VITE_WEBSOCKET_URL;
// --- Types (실제 백엔드 DTO와 맞춰야 함) ---

interface LiveChatMessage {
    senderName: string;
    qaId: string;
    msgType: string;
    content: string;
    createdAt: string; // ISO String

}
interface ChatMessage {
    senderName: string;
    content: string;
    createdAt: string; // ISO String
}

interface QaMessage {
    id: number;
    content: string;
    answer?: string;
    status: 'NEW' | 'ANSWERED';
    createdAt: string;
}

// 방 번호 부모한테 뜯어오기
const props = defineProps<{
    roomId?: string | number // 방 번호
}>();
``
// 상태정보들
const roomId = props.roomId || '1'; // 기본값 1
const activeTab = ref<'chat' | 'qa'>('chat');
const inputMessage = ref('');
const isAnonymous = ref(false);
const hasNewQa = ref(false); // 새 질문 알림용
const scrollContainer = ref<HTMLDivElement | null>(null);

// 데이터 저장소
const chatList = ref<ChatMessage[]>([]);
const qaList = ref<QaMessage[]>([]);

// STOMP Client
const stompClient = ref<Client | null>(null);

// 웹 소켓 연결
const connect = () => {
    stompClient.value = new Client({
        webSocketFactory: () => new SockJS(socketUrl),
        onConnect: (frame) => {
            console.log('Connected: ' + frame);
            // 일반 채팅방 구독
            stompClient.value?.subscribe(`/topic/chat-room/${roomId}`, (res) => {
                const msg: ChatMessage = JSON.parse(res.body);
                chatList.value.push(msg);
                scrollToBottom();
            });
            // Q&A 채팅방 구독
            stompClient.value?.subscribe(`/topic/qa-room/${roomId}`, (res) => {
                const qa: QaMessage = JSON.parse(res.body);
                qaList.value.push(qa);
                // 내가 보고 있는 탭이 Q&A가 아니면 알림 표시
                if (activeTab.value !== 'qa') {
                    hasNewQa.value = true;
                }
                scrollToBottom();
            });
        },
        onStompError: (frame) => {
            console.error('Broker reported error: ' + frame.headers['message']);
            console.error('Additional details: ' + frame.body);
        },
    });
    stompClient.value.activate();
};

// 메시지 전송
const sendMessage = () => {
    if (!inputMessage.value.trim() || !stompClient.value?.connected) return;
    const content = inputMessage.value;
    // 채팅 채널일때 메시지 전달
    if (activeTab.value === 'chat') {
        const payload = {
            content: content,
            // senderName 등은 보통 토큰(헤더)에 있어서 백엔드가 알아서 처리함
        };
        stompClient.value.publish({
            destination: `/app/chat-room/${roomId}`,
            body: JSON.stringify(payload),
        });

    } else {
        // 질문 채널일때 메시지 전달
        const payload = {
            content: content,
            isAnonymous: isAnonymous.value
        };
        stompClient.value.publish({
            destination: `/app/qa-room/${roomId}`,
            body: JSON.stringify(payload),
        });
    }

    // 초기화
    inputMessage.value = '';
    isAnonymous.value = false;
};

// 시간 포멧팅
const formatTime = (isoString: string) => {
    if (!isoString) return '';
    const date = new Date(isoString);
    return date.toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit' });
};

// 스크롤 최하단으로 이동
const scrollToBottom = async () => {
    await nextTick();
    if (scrollContainer.value) {
        scrollContainer.value.scrollTop = scrollContainer.value.scrollHeight;
    }
};

// 탭 변경 시 알림 끄기
watch(activeTab, (newTab) => {
    if (newTab === 'qa') {
        hasNewQa.value = false;
        scrollToBottom();
    }
});

// 시작하자마자 웹소켓 연결
onMounted(() => {
    connect();
});

onUnmounted(() => {
    if (stompClient.value) {
        stompClient.value.deactivate();
        console.log('연결 해제');
    }
});
</script>