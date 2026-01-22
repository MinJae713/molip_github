<template>
    <div class="bg-slate-50 text-slate-900 min-h-screen flex flex-col overflow-hidden">
        <!-- Header -->
        <header
            class="flex items-center justify-between border-b border-slate-200 px-6 py-3 bg-white z-10 shadow-sm shrink-0">
            <div class="flex items-center gap-4">
                <div class="size-8 text-indigo-600">
                    <span class="material-symbols-outlined text-3xl">school</span>
                </div>
                <div>
                    <h2 class="text-slate-900 text-lg font-bold leading-tight">
                        AI 에듀테크
                        <span
                            class="bg-indigo-100 text-indigo-600 px-1.5 rounded text-[10px] font-bold ml-1 align-middle border border-indigo-200">LIVE</span>
                    </h2>
                    <p class="text-xs text-slate-500">디지털 비즈니스 전략 강의 (4주차)</p>
                </div>
            </div>
            <div class="flex items-center gap-4">
                <div class="flex items-center gap-2 px-3 py-1.5 rounded-full bg-slate-100 border border-slate-200">
                    <div class="size-2 rounded-full bg-red-500 animate-pulse"></div>
                    <span class="text-xs font-mono text-slate-600 font-medium">{{ formatTime(elapsedTime) }}</span>
                </div>
                <button
                    class="flex items-center justify-center rounded-full size-9 bg-white text-slate-400 hover:text-indigo-600 border border-slate-200 hover:border-indigo-300 transition-colors shadow-sm">
                    <span class="material-symbols-outlined text-[20px]">settings</span>
                </button>
                <div class="size-9 rounded-full bg-gradient-to-br from-indigo-600 to-cyan-400 p-[2px]">
                    <div class="size-full rounded-full bg-white p-0.5">
                        <img alt="Student Profile" class="rounded-full w-full h-full object-cover"
                            src="https://ui-avatars.com/api/?name=Student&background=4F46E5&color=fff" />
                    </div>
                </div>
            </div>
        </header>

        <!-- Main Content -->
        <main class="flex flex-1 overflow-hidden p-6 gap-6 relative">
            <!-- Video Area -->
            <div
                class="flex-1 flex flex-col relative rounded-2xl overflow-hidden border border-slate-200 shadow-xl bg-slate-900 group/video">
                <!-- Presentation Slides Background -->
                <div class="absolute inset-0 bg-slate-800 flex items-center justify-center">
                    <div class="text-center text-white">
                        <span class="material-symbols-outlined text-9xl opacity-20">slideshow</span>
                        <p class="text-lg mt-4">강의 화면</p>
                    </div>
                </div>

                <!-- AI Focus Tracker -->
                <div class="absolute top-6 left-6 flex items-center gap-3">
                    <div
                        class="bg-white/90 backdrop-blur-md px-4 py-2 rounded-full flex items-center gap-3 border-l-4 border-l-red-500 shadow-lg">
                        <div class="relative size-3 flex items-center justify-center">
                            <div class="absolute inset-0 rounded-full bg-red-500 animate-ping opacity-75"></div>
                            <div class="relative size-2 rounded-full bg-red-500"></div>
                        </div>
                        <div class="flex flex-col">
                            <span class="text-[10px] text-slate-500 font-bold uppercase tracking-wider">AI Focus
                                Tracker</span>
                            <span class="text-xs text-red-600 font-bold">{{ focusStatus }}</span>
                        </div>
                        <span class="material-symbols-outlined text-slate-400 text-lg ml-1">visibility</span>
                    </div>
                </div>

                <!-- Warning Alert (조건부 표시) -->
                <div v-if="showWarning"
                    class="absolute inset-0 z-50 flex items-center justify-center pointer-events-none">
                    <div
                        class="bg-white/95 backdrop-blur-md rounded-2xl shadow-2xl p-6 border-l-4 border-red-500 flex items-center gap-5 pointer-events-auto max-w-md animate-bounce ring-1 ring-black/5">
                        <div
                            class="size-16 rounded-full bg-red-50 text-red-500 flex items-center justify-center shrink-0 border border-red-100">
                            <span class="material-symbols-outlined text-4xl">accessibility_new</span>
                        </div>
                        <div class="flex flex-col gap-1.5">
                            <h3 class="text-slate-900 font-bold text-lg leading-tight">집중도가 매우 낮습니다!</h3>
                            <p class="text-slate-500 text-sm">잠시 스트레칭을 해보세요.</p>
                            <button @click="showWarning = false"
                                class="mt-2 text-xs font-bold bg-red-500 text-white px-4 py-1.5 rounded-lg hover:bg-red-600 transition-colors w-fit shadow-sm">
                                확인했습니다
                            </button>
                        </div>
                        <button @click="showWarning = false"
                            class="absolute top-3 right-3 text-slate-300 hover:text-slate-500 p-1">
                            <span class="material-symbols-outlined text-lg">close</span>
                        </button>
                    </div>
                </div>

                <!-- Instructor PIP (Picture-in-Picture) -->
                <div
                    class="absolute top-6 right-6 w-64 aspect-video bg-slate-800 rounded-xl border border-slate-600/50 overflow-hidden shadow-2xl cursor-move hover:ring-2 hover:ring-cyan-400/50 transition-all">
                    <div class="w-full h-full flex items-center justify-center">
                        <div class="text-center text-white">
                            <span class="material-symbols-outlined text-5xl opacity-50">person</span>
                            <p class="text-xs mt-2">김교수님</p>
                        </div>
                    </div>
                    <div class="absolute bottom-2 right-2 flex gap-1">
                        <span
                            class="material-symbols-outlined text-white text-[14px] bg-black/40 rounded p-0.5 backdrop-blur-sm">mic</span>
                    </div>
                </div>

                <!-- Control Dock -->
                <div class="absolute bottom-8 left-1/2 -translate-x-1/2 z-30">
                    <div
                        class="bg-white/90 backdrop-blur-md px-6 py-3 rounded-2xl flex items-center gap-5 shadow-2xl border border-white/40">
                        <button @click="toggleMic" class="flex flex-col items-center gap-1 group">
                            <div
                                class="size-10 rounded-xl bg-white border border-slate-200 flex items-center justify-center shadow-sm hover:bg-slate-50 hover:border-slate-300 transition-all">
                                <span class="material-symbols-outlined text-slate-600">{{ isMicOn ? 'mic' : 'mic_off'
                                    }}</span>
                            </div>
                            <span class="text-[10px] font-medium text-slate-500">{{ isMicOn ? '마이크 끄기' : '마이크 켜기'
                                }}</span>
                        </button>

                        <button @click="toggleCamera" class="flex flex-col items-center gap-1 group">
                            <div
                                class="size-10 rounded-xl bg-white border border-slate-200 flex items-center justify-center shadow-sm hover:bg-slate-50 hover:border-slate-300 transition-all">
                                <span class="material-symbols-outlined text-slate-600">{{ isCameraOn ? 'videocam' :
                                    'videocam_off' }}</span>
                            </div>
                            <span class="text-[10px] font-medium text-slate-500">{{ isCameraOn ? '카메라 끄기' : '카메라 켜기'
                                }}</span>
                        </button>

                        <div class="w-px h-8 bg-slate-300 mx-1"></div>

                        <button @click="raiseHand" class="flex flex-col items-center gap-1 group">
                            <div
                                :class="['size-10 rounded-xl flex items-center justify-center shadow-sm transition-all',
                                    isHandRaised ? 'bg-indigo-600 border-indigo-600 text-white' : 'bg-white border-slate-200 hover:bg-indigo-600 hover:border-indigo-600 hover:text-white hover:-translate-y-1']">
                                <span class="material-symbols-outlined">pan_tool</span>
                            </div>
                            <span
                                :class="['text-[10px] font-medium', isHandRaised ? 'text-indigo-600' : 'text-slate-500']">손들기</span>
                        </button>

                        <button class="flex flex-col items-center gap-1 group">
                            <div
                                class="size-10 rounded-xl bg-white border border-slate-200 flex items-center justify-center shadow-sm hover:bg-pink-500 hover:border-pink-500 hover:text-white hover:-translate-y-1 transition-all">
                                <span
                                    class="material-symbols-outlined text-slate-600 group-hover:text-white">add_reaction</span>
                            </div>
                            <span class="text-[10px] font-medium text-slate-500 group-hover:text-pink-500">반응</span>
                        </button>

                        <div class="w-px h-8 bg-slate-300 mx-1"></div>

                        <button class="flex flex-col items-center gap-1 group">
                            <div
                                class="size-10 rounded-xl bg-red-50 border border-red-100 text-red-500 flex items-center justify-center shadow-sm hover:bg-red-500 hover:text-white hover:-translate-y-1 transition-all">
                                <span class="material-symbols-outlined">logout</span>
                            </div>
                            <span class="text-[10px] font-medium text-slate-400 group-hover:text-red-500">나가기</span>
                        </button>
                    </div>
                </div>
            </div>

            <!-- Sidebar -->
            <aside class="w-80 flex flex-col gap-4 h-full overflow-hidden">
                <!-- Live Quiz Card -->
                <div
                    class="bg-white border border-slate-200 rounded-2xl overflow-hidden shadow-sm relative shrink-0 hover:border-indigo-300 transition-colors cursor-pointer">
                    <div class="absolute top-0 left-0 w-full h-1 bg-gradient-to-r from-indigo-600 to-cyan-400"></div>
                    <div class="p-4 flex items-center justify-between">
                        <div class="flex items-center gap-3">
                            <div
                                class="relative flex items-center justify-center size-9 rounded-full bg-pink-100 text-pink-500">
                                <span
                                    class="absolute flex size-2.5 rounded-full bg-pink-500 animate-pulse top-0 right-0 border-2 border-white"></span>
                                <span class="material-symbols-outlined text-lg">quiz</span>
                            </div>
                            <div class="flex flex-col">
                                <h3 class="font-bold text-indigo-600 text-xs tracking-wide">LIVE QUIZ 진행중</h3>
                                <p class="text-[10px] text-slate-500 truncate w-32">인공지능 윤리 가이드라인 핵심...</p>
                            </div>
                        </div>
                        <div
                            class="flex items-center gap-1 text-pink-500 font-mono text-xs font-bold bg-pink-50 px-2 py-1 rounded border border-pink-100">
                            <span class="material-symbols-outlined text-[14px]">timer</span>
                            <span>00:15</span>
                        </div>
                    </div>
                </div>

                <!-- Engagement Score -->
                <div class="bg-white border border-slate-200 rounded-2xl flex flex-col shadow-sm shrink-0">
                    <div class="p-3 border-b border-slate-100 bg-slate-50/50 flex justify-between items-center">
                        <h3 class="font-bold text-slate-800 text-xs flex items-center gap-2">
                            <span class="material-symbols-outlined text-indigo-600 text-[16px]">monitoring</span>
                            나의 참여도 (My Engagement)
                        </h3>
                    </div>
                    <div class="p-4 flex flex-col gap-4">
                        <div class="flex items-center gap-3">
                            <!-- Score Circle -->
                            <div class="relative size-16 flex-shrink-0">
                                <svg class="size-full -rotate-90" viewBox="0 0 36 36">
                                    <path class="text-slate-100"
                                        d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831"
                                        fill="none" stroke="currentColor" stroke-width="3"></path>
                                    <path class="text-red-500"
                                        d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831"
                                        fill="none" stroke="currentColor" :stroke-dasharray="`${engagementScore}, 100`"
                                        stroke-linecap="round" stroke-width="3"></path>
                                </svg>
                                <div class="absolute inset-0 flex flex-col items-center justify-center">
                                    <span class="text-lg font-bold text-slate-800 leading-none">{{ engagementScore
                                        }}</span>
                                    <span class="text-[8px] text-slate-400 font-bold">SCORE</span>
                                </div>
                            </div>

                            <!-- AI Sentiment -->
                            <div
                                class="flex-1 bg-indigo-50 border border-indigo-100 rounded-xl p-2.5 flex items-center gap-3">
                                <div class="flex-1">
                                    <div class="text-[10px] text-indigo-600 font-bold leading-tight mb-1">실시간 감정 분석
                                    </div>
                                    <div class="flex items-center gap-2">
                                        <span class="text-2xl">{{ currentEmotion.emoji }}</span>
                                        <div class="flex flex-col">
                                            <span class="text-[11px] font-bold text-slate-800">{{ currentEmotion.label
                                                }}</span>
                                            <span class="text-[9px] text-slate-500">AI 분석 결과</span>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <!-- Warning -->
                        <div v-if="focusStatus.includes('낮음')"
                            class="bg-red-50 rounded-lg p-2.5 border border-red-100 shadow-sm relative overflow-hidden flex items-start gap-2">
                            <div class="absolute top-0 left-0 w-0.5 h-full bg-red-400"></div>
                            <span
                                class="material-symbols-outlined text-red-500 text-base shrink-0 mt-0.5">warning</span>
                            <div class="flex flex-col">
                                <span class="text-[10px] font-bold text-red-600 mb-0.5">주의 필요</span>
                                <p class="text-[10px] text-red-600/90 leading-tight">집중도가 낮아지고 있습니다. 스트레칭이 필요합니다.</p>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Chat/Q&A Panel -->
                <div
                    class="flex-1 bg-white border border-slate-200 rounded-2xl shadow-sm flex flex-col overflow-hidden min-h-0">
                    <!-- Tabs -->
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
                            <span v-if="activeTab === 'qa'"
                                class="size-1.5 rounded-full bg-pink-500 animate-pulse"></span>
                        </button>
                    </div>

                    <!-- Messages Area -->
                    <div class="flex-1 overflow-y-auto p-4 space-y-4 bg-slate-50/30">
                        <!-- Chat Tab -->
                        <div v-if="activeTab === 'chat'" class="flex flex-col gap-3">
                            <div v-for="msg in chatMessages" :key="msg.id" class="flex gap-2">
                                <div class="size-7 rounded-full bg-gradient-to-br from-indigo-400 to-cyan-400 shrink-0">
                                </div>
                                <div class="flex-1">
                                    <div class="flex items-center gap-2 mb-1">
                                        <span class="text-xs font-bold text-slate-800">{{ msg.name }}</span>
                                        <span class="text-[10px] text-slate-400">{{ msg.time }}</span>
                                    </div>
                                    <p class="text-xs text-slate-600">{{ msg.message }}</p>
                                </div>
                            </div>
                        </div>

                        <!-- Q&A Tab -->
                        <div v-else class="flex flex-col gap-3">
                            <div v-for="qa in qaMessages" :key="qa.id" :class="['bg-white border border-slate-200 p-3 rounded-xl shadow-sm',
                                qa.status === 'new' && 'border-l-4 border-l-pink-500']">
                                <div class="flex items-center gap-2 mb-2">
                                    <span
                                        :class="['text-[10px] font-bold px-1.5 py-0.5 rounded',
                                            qa.status === 'answered' ? 'bg-amber-100 text-amber-700' : 'bg-pink-100 text-pink-600']">
                                        {{ qa.status === 'answered' ? '답변완료' : '새 질문' }}
                                    </span>
                                    <span class="text-[10px] text-slate-400">{{ qa.time }}</span>
                                </div>
                                <p class="text-xs text-slate-800 font-medium mb-2">Q: {{ qa.question }}</p>
                                <div v-if="qa.answer" class="bg-slate-50 rounded-lg p-2.5 border-l-2 border-indigo-600">
                                    <p class="text-[11px] text-slate-600 leading-relaxed">
                                        <span class="font-bold text-indigo-600 mr-1">A:</span> {{ qa.answer }}
                                    </p>
                                </div>
                                <p v-else class="text-[10px] text-slate-400 mt-2">답변 대기 중...</p>
                            </div>
                        </div>
                    </div>

                    <!-- Input Area -->
                    <div class="p-3 bg-white border-t border-slate-100 shrink-0">
                        <div class="relative flex items-center">
                            <input v-model="newMessage" @keyup.enter="sendMessage"
                                class="w-full bg-slate-50 border border-slate-200 rounded-full pl-4 pr-11 py-2.5 text-xs focus:ring-2 focus:ring-indigo-200 focus:border-indigo-600 transition-all outline-none text-slate-800 placeholder:text-slate-400"
                                :placeholder="activeTab === 'chat' ? '채팅 메시지 입력...' : '교수님께 질문하기...'" type="text" />
                            <button @click="sendMessage"
                                class="absolute right-1.5 p-1.5 bg-indigo-600 text-white rounded-full hover:bg-indigo-700 hover:shadow-lg hover:-translate-y-0.5 transition-all flex items-center justify-center group">
                                <span
                                    class="material-symbols-outlined text-[16px] group-hover:rotate-12 transition-transform">send</span>
                            </button>
                        </div>
                        <div v-if="activeTab === 'qa'" class="flex items-center gap-2 mt-2 px-1">
                            <label class="flex items-center gap-1.5 cursor-pointer">
                                <input v-model="isAnonymous"
                                    class="size-3 rounded border-slate-300 text-indigo-600 focus:ring-indigo-200"
                                    type="checkbox" />
                                <span class="text-[10px] text-slate-500">익명으로 질문하기</span>
                            </label>
                        </div>
                    </div>
                </div>
            </aside>
        </main>
    </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue';

// State
const elapsedTime = ref(2712); // seconds (00:45:12)
const isMicOn = ref(false);
const isCameraOn = ref(false);
const isHandRaised = ref(false);
const showWarning = ref(true);
const engagementScore = ref(35);
const focusStatus = ref('집중도 낮음 (Warning)');
const activeTab = ref<'chat' | 'qa'>('qa');
const newMessage = ref('');
const isAnonymous = ref(false);

const currentEmotion = ref({
    emoji: '🤩',
    label: '흥미로움'
});

// Mock Data
const chatMessages = ref([
    { id: 1, name: '김민수', time: '10:25', message: '오늘 강의 정말 유익하네요!' },
    { id: 2, name: '이지은', time: '10:27', message: '질문 있습니다~' },
]);

const qaMessages = ref([
    {
        id: 1,
        status: 'answered',
        time: '오전 10:30',
        question: 'AI 윤리 파트에서 \'책임성\'의 주체는 개발자인가요, 기업인가요?',
        answer: '가이드라인에 따르면 개발자, 운영자, 사용자 모두가 각자의 역할에 따른 책임을 집니다.'
    },
    {
        id: 2,
        status: 'new',
        time: '방금 전',
        question: '실습 데이터셋의 출처가 궁금합니다.',
        answer: null
    },
]);

// Timer
let timerInterval: number | null = null;

onMounted(() => {
    timerInterval = window.setInterval(() => {
        elapsedTime.value++;
    }, 1000);
});

onUnmounted(() => {
    if (timerInterval) clearInterval(timerInterval);
});

// Methods
const formatTime = (seconds: number) => {
    const h = Math.floor(seconds / 3600);
    const m = Math.floor((seconds % 3600) / 60);
    const s = seconds % 60;
    return `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`;
};

const toggleMic = () => {
    isMicOn.value = !isMicOn.value;
};

const toggleCamera = () => {
    isCameraOn.value = !isCameraOn.value;
};

const raiseHand = () => {
    isHandRaised.value = !isHandRaised.value;
};

const sendMessage = () => {
    if (!newMessage.value.trim()) return;

    if (activeTab.value === 'chat') {
        chatMessages.value.push({
            id: Date.now(),
            name: '나',
            time: new Date().toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit' }),
            message: newMessage.value
        });
    } else {
        qaMessages.value.unshift({
            id: Date.now(),
            status: 'new',
            time: '방금 전',
            question: newMessage.value,
            answer: null
        });
    }

    newMessage.value = '';
};
</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar {
    width: 4px;
}

.custom-scrollbar::-webkit-scrollbar-track {
    background: transparent;
}

.custom-scrollbar::-webkit-scrollbar-thumb {
    background: #CBD5E1;
    border-radius: 10px;
}
</style>
