<template>
    <div class="lg:col-span-2 bg-white rounded-xl border border-slate-200 shadow-sm flex flex-col h-full">
        <div class="px-5 py-4 border-b border-slate-100 flex items-center justify-between">
            <h3 class="font-bold text-slate-800 flex items-center gap-2">
                참가 신청 현황
                <span v-if="requests.length > 0"
                    class="bg-red-100 text-red-600 text-[11px] px-2 py-0.5 rounded-full font-bold">
                    {{ requests.length }}건 대기
                </span>
            </h3>
        </div>
        <div class="divide-y divide-slate-100 flex-1 overflow-y-auto max-h-[400px]">
            <div v-if="requests.length === 0" class="p-8 text-center text-slate-500 text-sm">
                대기 중인 참가 신청이 없습니다.
            </div>
            <div v-for="req in requests" :key="req.id"
                class="p-4 flex items-center justify-between hover:bg-slate-50 transition-colors">
                <div class="flex items-center gap-3">
                    <div
                        class="size-10 rounded-full bg-orange-100 text-orange-600 flex items-center justify-center font-bold text-sm">
                        {{ getInitials(req.name) }}
                    </div>
                    <div>
                        <p class="text-sm font-bold text-slate-900">{{ req.name }}</p>
                        <p class="text-xs text-slate-500">{{ req.department }} / {{ req.position }}</p>
                    </div>
                </div>
                <div class="flex items-center gap-2">
                    <span class="text-[11px] text-slate-400 mr-2">{{ formatTime(req.requestedAt) }}</span>
                    <button @click="handleReject(req.id)"
                        class="px-3 py-1.5 border border-slate-200 rounded-lg text-xs font-bold text-slate-600 hover:bg-red-50 hover:text-red-600 hover:border-red-200 transition-colors">
                        거절
                    </button>
                    <button @click="handleApprove(req.id)"
                        class="px-3 py-1.5 bg-indigo-600 rounded-lg text-xs font-bold text-white hover:bg-indigo-700 shadow-sm transition-colors">
                        승인
                    </button>
                </div>
            </div>
        </div>
        <div class="p-3 border-t border-slate-100 text-center" v-if="requests.length > 5">
            <button class="text-xs font-bold text-slate-500 hover:text-indigo-600 transition-colors">더 보기</button>
        </div>
    </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useTrainerCourseStore } from '@/stores/trainerCourse';
import { useRoute } from 'vue-router';
import dayjs from 'dayjs';
import relativeTime from 'dayjs/plugin/relativeTime';
import 'dayjs/locale/ko';

dayjs.extend(relativeTime);
dayjs.locale('ko');

const route = useRoute();
const store = useTrainerCourseStore();
const requests = computed(() => store.studentRequests);
const courseId = route.params.courseId as string;

const getInitials = (name: string) => {
    return name.slice(0, 2); // Simple initials
};

const formatTime = (time: string) => {
    return dayjs(time).fromNow();
};

const handleApprove = async (id: number) => {
    if (confirm('승인하시겠습니까?')) {
        await store.approveStudent(courseId, id);
    }
};

const handleReject = async (id: number) => {
    if (confirm('거절하시겠습니까?')) {
        await store.rejectStudent(courseId, id);
    }
};
</script>
