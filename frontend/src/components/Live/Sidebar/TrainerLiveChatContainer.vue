<template>
    <div
        class="flex flex-col gap-4 p-5 bg-white rounded-[32px] border border-slate-100 shadow-sm h-full overflow-hidden">
        <!-- 탭 버튼 제거됨 (부모 컴포넌트에서 제어) -->

        <div ref="scrollContainer" class="flex-1 overflow-y-auto p-4 space-y-4 bg-slate-50/30">

            <div v-if="viewType === 'chat'" class="flex flex-col gap-3">
                <div v-for="(msg, index) in chatList" :key="index" class="flex gap-2"
                    :class="{ 'flex-row-reverse': isMyMessage(msg.senderName || msg.name) }">

                    <!-- 아바타 영역 -->
                    <div class="size-7 rounded-full shrink-0 overflow-hidden shadow-sm flex items-center justify-center text-[10px] font-bold text-white relative"
                        :class="[
                            getAvatar(msg.senderName || msg.name, msg.profileImageUrl).type === 'initial'
                                ? `bg-gradient-to-br ${getAvatar(msg.senderName || msg.name, msg.profileImageUrl).gradient}`
                                : 'bg-slate-200'
                        ]">

                        <!-- 1. 이미지일 경우 -->
                        <img v-if="getAvatar(msg.senderName || msg.name, msg.profileImageUrl).type === 'image'"
                            :src="getAvatar(msg.senderName || msg.name, msg.profileImageUrl).src"
                            class="size-full object-cover" />

                        <!-- 2. 이니셜일 경우 -->
                        <span v-else>
                            {{ getAvatar(msg.senderName || msg.name, msg.profileImageUrl).initial }}
                        </span>
                    </div>
                    <div class="flex-1" :class="{ 'text-right': isMyMessage(msg.senderName || msg.name) }">
                        <div class="flex items-center gap-2 mb-1"
                            :class="{ 'flex-row-reverse': isMyMessage(msg.senderName || msg.name) }">
                            <span class="text-xs font-bold text-slate-800">{{ msg.senderName || msg.name }}</span>
                            <span class="text-[10px] text-slate-400">{{ formatTime(msg.createdAt || msg.timestamp)
                            }}</span>
                        </div>
                        <p class="text-xs p-2 rounded-lg inline-block shadow-sm text-left"
                            :class="isMyMessage(msg.senderName || msg.name) ? 'bg-indigo-100 text-indigo-900' : 'bg-white text-slate-600'">
                            {{ msg.content }}</p>
                    </div>
                </div>
                <div v-if="chatList.length === 0" class="text-center text-slate-400 text-xs py-10">
                    채팅이 시작되었습니다.
                </div>
            </div>

            <div v-else class="flex flex-col gap-4">
                <template v-for="group in groupedQaList" :key="group.questionId">
                    <div @click="toggleSelectQuestion(group.questionId)"
                        class="bg-white border rounded-xl shadow-sm overflow-hidden cursor-pointer transition-all duration-200"
                        :class="selectedQuestionId === group.questionId ? 'border-indigo-600 ring-2 ring-indigo-100' : 'border-slate-200 hover:border-indigo-300'">

                        <div class="p-3 border-l-4"
                            :class="selectedQuestionId === group.questionId ? 'border-l-indigo-600' : 'border-l-slate-400'">
                            <div class="flex justify-between items-start mb-2">
                                <div class="flex items-center gap-2">
                                    <span class="text-[10px] font-bold px-1.5 py-0.5 rounded"
                                        :class="selectedQuestionId === group.questionId ? 'bg-indigo-600 text-white' : 'bg-slate-100 text-slate-600'">
                                        {{ selectedQuestionId === group.questionId ? '답변 작성중' : '질문' }}
                                    </span>
                                    <span class="text-[10px] text-slate-400">{{ formatTime(group.createdAt ||
                                        group.timestamp) }}</span>
                                </div>
                            </div>
                            <p class="text-xs text-slate-800 font-bold">Q: {{ group.content }}</p>
                            <div class="text-[10px] text-slate-400 mt-1">작성자: {{ group.senderName || group.name }}</div>
                        </div>

                        <div v-if="group.replies.length > 0"
                            class="bg-slate-50/50 p-3 border-t border-slate-100 space-y-3">
                            <div v-for="reply in group.replies" :key="reply.id" class="flex gap-2">
                                <span class="text-emerald-500 font-bold text-[11px] shrink-0">A.</span>
                                <div class="flex-1">
                                    <p class="text-[11px] text-slate-700 leading-relaxed">{{ reply.content }}</p>
                                    <div class="flex justify-between mt-1">
                                        <span class="text-[9px] text-slate-400">{{ reply.senderName || reply.name
                                            }}</span>
                                        <span class="text-[9px] text-slate-400">{{ formatTime(reply.createdAt ||
                                            reply.timestamp) }}</span>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </template>

                <div v-if="groupedQaList.length === 0" class="text-center text-slate-400 text-xs py-10">
                    질문에 답변을 달아주세요!
                </div>
            </div>
        </div>

        <div class="p-3 bg-white border-t border-slate-100 shrink-0">
            <div v-if="viewType === 'qa' && selectedQuestionId"
                class="flex justify-between items-center mb-2 px-3 py-1 bg-indigo-50 rounded text-[10px] text-indigo-600">
                <span class="font-bold">선택한 질문에 답변하는 중입니다.</span>
                <button @click.stop="selectedQuestionId = null" class="underline">선택 해제</button>
            </div>

            <div class="relative flex items-center">
                <input v-model="inputMessage" @keyup.enter="sendMessage"
                    class="w-full bg-slate-50 border border-slate-200 rounded-full pl-4 pr-11 py-2.5 text-xs focus:ring-2 focus:ring-indigo-200 focus:border-indigo-600 transition-all outline-none text-slate-800"
                    :placeholder="getPlaceholder()" type="text" />
                <button @click="sendMessage"
                    class="absolute right-1.5 p-1.5 bg-indigo-600 text-white rounded-full hover:bg-indigo-700 transition-all flex items-center justify-center">
                    <span class="material-symbols-outlined text-[16px]">send</span>
                </button>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick } from 'vue';
import { useUserStore } from '@/stores/user';

// ... (existing imports)

const userStore = useUserStore();

const isMyMessage = (name?: string) => {
    return name === userStore.user.name;
};


// --- Types ---
interface Message {
    id?: number;
    type?: string;
    questionId?: number | string;
    content: string;
    senderName?: string;
    name?: string;
    profileImageUrl?: string; // 추가됨
    createdAt?: string;
    timestamp?: string;
}

// ... (GroupedQa interface remains same)

// 아바타 그라데이션 컬러셋 (Tailwind 클래스)
const gradients = [
    'from-red-400 to-orange-400',
    'from-amber-400 to-yellow-400',
    'from-lime-400 to-green-400',
    'from-emerald-400 to-teal-400',
    'from-cyan-400 to-sky-400',
    'from-blue-400 to-indigo-400',
    'from-violet-400 to-purple-400',
    'from-fuchsia-400 to-pink-400',
    'from-rose-400 to-red-400',
];

// 유저 이름별 그라데이션 매핑 (세션 동안 유지)
const userGradientMap = ref<Record<string, string>>({});

const getAvatar = (name?: string, url?: string) => {
    // 1. 프로필 이미지가 있으면 이미지 사용
    if (url) return { type: 'image', src: url };

    // 2. 이름이 없으면 기본값
    const userName = name || 'User';

    // 3. 이미 할당된 색상이 없으면 랜덤 할당 후 저장
    if (!userGradientMap.value[userName]) {
        const randomIndex = Math.floor(Math.random() * gradients.length);
        userGradientMap.value[userName] = gradients[randomIndex]!;
    }

    return {
        type: 'initial',
        gradient: userGradientMap.value[userName],
        initial: userName.charAt(0).toUpperCase()
    };
};
interface GroupedQa extends Message {
    replies: Message[];
}

const props = defineProps<{
    chatList: Message[],
    qaList: Message[],
    viewType: 'chat' | 'qa'
}>();

const emit = defineEmits<{
    'send-message': [content: string, type: string, questionId?: number | string | null]
}>();
const scrollContainer = ref<HTMLDivElement | null>(null);
const selectedQuestionId = ref<number | string | null>(null); // 클릭된 질문의 ID

// --- 핵심 로직: questionId로 그룹화 ---
const groupedQaList = computed(() => {
    const groups: Record<string, GroupedQa> = {};
    // 1. 모든 QA 메시지를 순회하며 그룹핑
    props.qaList.forEach((msg: Message) => {
        if (!msg.questionId) return; // 안전장치
        const qId = String(msg.questionId);

        // 아직 그룹이 없으면 생성 (이게 질문 본문이 됨)
        if (!groups[qId]) {
            groups[qId] = { ...msg, replies: [] };
        } else {
            // 이미 그룹이 있으면 이건 답변임 (replies에 추가)
            groups[qId].replies.push(msg);
        }
    });
    // 배열로 변환하여 반환 (최신 질문이 아래로 오게 하려면 순서 유지)
    return Object.values(groups);
});

// --- 메시지 전송 ---
const inputMessage = ref('');

const sendMessage = () => {
    if (!inputMessage.value.trim()) return;

    let finalQuestionId = null;
    let messageType = 'chat';

    // 질문 탭일 경우 ID 계산 로직
    if (props.viewType === 'qa') {
        messageType = 'question';
        if (selectedQuestionId.value) {
            // 1. 카드를 선택한 상태 -> 답변 (해당 ID 사용)
            finalQuestionId = selectedQuestionId.value;
        } else {
            // 2. 선택 안 함 -> 새 질문 (임의의 난수 ID 생성)
            finalQuestionId = Math.floor(Date.now() + Math.random() * 1000);
        }
    }

    // 부모 컴포넌트로 메시지 전송 요청
    emit('send-message', inputMessage.value, messageType, finalQuestionId);
    inputMessage.value = '';
};

// --- UI 헬퍼 함수 ---
const toggleSelectQuestion = (id: number | string | null | undefined) => {
    // 이미 선택된 걸 누르면 해제, 아니면 선택
    if (selectedQuestionId.value === id) {
        selectedQuestionId.value = null;
    } else {
        selectedQuestionId.value = id || null;
    }
};

const getPlaceholder = () => {
    if (props.viewType === 'chat') return '채팅 메시지를 입력하세요...';
    if (selectedQuestionId.value) return '선택한 질문에 답변을 입력하세요...';
    return '질문에 대한 답변을 달아주세요...';
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
        scrollContainer.value.scrollTop = scrollContainer.value.scrollHeight;
    }
};

watch(() => props.viewType, () => {
    scrollToBottom();
});

watch([() => props.chatList, () => props.qaList], () => {
    scrollToBottom();
}, { deep: true });
</script>
