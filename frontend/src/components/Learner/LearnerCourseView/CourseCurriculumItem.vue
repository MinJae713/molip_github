<template>
    <div class="group bg-white border rounded-xl p-5 transition-all flex flex-col sm:flex-row gap-4 relative overflow-hidden"
        :class="[
            session.state === 'live' ? 'border-2 border-indigo-500/20 shadow-soft ring-1 ring-indigo-500/10' : 'border-slate-200 hover:border-indigo-300'
        ]">

        <div v-if="session.state === 'live'" class="absolute top-0 right-0 p-3">
            <span class="relative flex h-3 w-3">
                <span class="animate-ping absolute inline-flex h-full w-full rounded-full bg-red-400 opacity-75"></span>
                <span class="relative inline-flex rounded-full h-3 w-3 bg-red-500"></span>
            </span>
        </div>

        <div class="flex-none flex flex-col items-center justify-center w-12 pt-1 sm:pt-0">
            <div v-if="session.state === 'live'"
                class="size-8 rounded-full bg-red-500 text-white flex items-center justify-center font-bold text-[10px] shadow-md animate-pulse">
                LIVE
            </div>
            <div v-else-if="session.state === 'ended'"
                class="size-8 rounded-full bg-indigo-100 text-indigo-600 flex items-center justify-center font-bold text-sm">
                <span class="material-symbols-outlined text-[18px]">check</span>
            </div>
            <div v-else
                class="size-8 rounded-full bg-slate-100 text-slate-500 flex items-center justify-center font-bold text-sm border border-slate-200">
                {{ String(index + 1).padStart(2, '0') }}
            </div>
            <div class="h-full w-0.5 bg-slate-100 my-2 hidden sm:block group-last:hidden"></div>
        </div>

        <div class="flex-1">
            <div class="flex items-center gap-2 mb-1">
                <span class="text-xs font-bold uppercase tracking-wider"
                    :class="session.state === 'live' ? 'text-red-500' : 'text-slate-400'">
                    SESSION {{ String(index + 1).padStart(2, '0') }}
                </span>
            </div>
            <h3 class="font-bold text-lg" :class="session.state === 'ended' ? 'text-slate-900' : 'text-slate-800'">
                {{ session.title }}
            </h3>
            <p v-if="session.description" class="text-sm text-slate-600 mt-1 mb-2">{{ session.description }}</p>

            <div class="flex flex-wrap items-center gap-x-4 gap-y-2 mt-2 text-sm text-slate-500">
                <div class="flex items-center gap-1"
                    :class="{ 'text-slate-800 font-medium': session.state === 'live' }">
                    <span class="material-symbols-outlined text-[16px]"
                        :class="{ 'text-red-500': session.state === 'live' }">
                        {{ session.state === 'live' ? 'videocam' : 'calendar_today' }}
                    </span>
                    {{ session.date }} {{ session.state === 'live' ? '(진행 중)' : '' }}
                </div>
                <div v-if="session.duration" class="flex items-center gap-1">
                    <span class="material-symbols-outlined text-[16px]">schedule</span> {{ session.duration }}
                </div>
            </div>
        </div>

        <!-- Action Buttons Group -->
        <div class="flex flex-col gap-2 min-w-[140px] sm:self-center items-stretch">
            <button v-if="session.state === 'live'" @click="enterSession"
                class="w-full px-6 py-2.5 bg-indigo-600 hover:bg-indigo-700 text-white text-sm font-bold rounded-lg shadow-md transition-all flex items-center justify-center gap-2 group">
                <span class="material-symbols-outlined text-[18px]">login</span> 입장하기
                <span
                    class="material-symbols-outlined text-[16px] group-hover:translate-x-1 transition-transform">arrow_forward</span>
            </button>

            <button v-else-if="session.state === 'ended'" @click="enterReview"
                class="w-full px-4 py-2 bg-white border border-slate-300 hover:bg-slate-50 text-slate-700 text-sm font-bold rounded-lg flex items-center justify-center gap-2">
                <span class="material-symbols-outlined text-[18px]">history</span>
                복습하기
            </button>

            <button v-else-if="session.state === 'upcoming'"
                class="w-full px-4 py-2 bg-slate-100 text-slate-400 text-sm font-bold rounded-lg flex items-center justify-center gap-2 cursor-not-allowed">
                <span class="material-symbols-outlined text-[18px]">calendar_today</span>강의예정
            </button>

            <button @click="openModal"
                class="w-full px-4 py-2 bg-indigo-50 hover:bg-indigo-100 text-indigo-700 text-sm font-bold rounded-lg flex items-center justify-center gap-2 transition-colors">
                <span class="material-symbols-outlined text-[18px]">description</span>학습자료 확인하기
            </button>

            <MaterialDownloadModal v-model="isModalOpen" :courseId="session.courseId" :sessionId="session.sessionId" />
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import api from '@/api';
import MaterialDownloadModal from '@/components/Learner/LearnerCourseView/MaterialDownloadModal.vue';

const props = defineProps<{
    session: any; // 스토어에서 가공된 세션 객체
    index: number;
}>();

const router = useRouter();
const isModalOpen = ref(false);

const emit = defineEmits<{
    (e: 'close'): void;
    (e: 'update:modelValue', value: boolean): void;
}>();

const openModal = () => { isModalOpen.value = true; };

// 라이브 세션 입장 (API 체크 포함)
const enterSession = async () => {
    try {
        const sessionId = props.session.sessionId;
        const response = await api.get(`/session/${sessionId}/connection`);
        if (response.data && response.data.janusRoomId) {
            const route = router.resolve({
                name: 'learnerLiveSession',
                params: { sessionId: sessionId }
            });
            window.open(route.href, '_blank', 'noopener,noreferrer');
        } else {
            alert("현재 라이브 강의가 진행 중이지 않습니다.");
        }
    } catch (error) {
        console.error("세션 연결 정보 조회 실패:", error);
        alert("강의에 입장할 수 없습니다. (연결 정보 조회 실패)");
    }
}

// 복습하기 입장
const enterReview = () => {
    const route = router.resolve({
        name: 'learnerReview',
        params: { sessionId: props.session.sessionId }
    });
    window.open(route.href, '_blank', 'noopener,noreferrer');
};
</script>