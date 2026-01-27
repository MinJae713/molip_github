<template>
    <div v-if="isOpen" class="fixed inset-0 z-[100] overflow-y-auto" aria-labelledby="modal-title" role="dialog"
        aria-modal="true">
        <!-- Backdrop -->
        <div class="fixed inset-0 bg-slate-900/60 backdrop-blur-sm transition-opacity" @click="$emit('close')"></div>

        <div class="flex min-h-full items-center justify-center p-4 text-center sm:p-0">
            <div
                class="relative transform overflow-hidden rounded-2xl bg-white text-left shadow-2xl transition-all sm:my-8 sm:w-full sm:max-w-3xl border border-slate-200">

                <!-- Header -->
                <div
                    class="bg-white px-6 py-5 border-b border-slate-100 flex items-center justify-between sticky top-0 z-10">
                    <div>
                        <h3 class="text-xl font-bold text-slate-900 font-display" id="modal-title">교육과정 커리큘럼 상세</h3>
                        <p class="mt-1 text-xs text-slate-500">실시간 강의 세션 정보를 미리 확인하세요.</p>
                    </div>
                    <button @click="$emit('close')"
                        class="rounded-full p-2 bg-slate-50 text-slate-400 hover:text-indigo-600 hover:bg-indigo-50 transition-colors"
                        type="button">
                        <span class="material-symbols-outlined text-xl">close</span>
                    </button>
                </div>

                <!-- Course Info Summary -->
                <div class="bg-indigo-50/40 px-6 py-5 border-b border-indigo-100 flex items-start gap-5">
                    <div
                        class="size-14 rounded-xl bg-white border border-indigo-100 flex items-center justify-center text-indigo-brand shadow-sm shrink-0">
                        <span class="material-symbols-outlined text-3xl">psychology</span>
                    </div>
                    <div class="flex-1">
                        <div class="flex items-center gap-2 mb-1.5">
                            <span
                                class="px-2 py-0.5 bg-white text-indigo-700 text-[10px] font-bold rounded border border-indigo-200 uppercase tracking-wide shadow-sm">
                                {{ course.status === 'recruiting' ? '모집 중' : '진행 중' }}
                            </span>
                            <span class="text-xs font-bold text-indigo-900/60 flex items-center gap-1">
                                <span class="size-1 rounded-full bg-indigo-400"></span>
                                {{ course.sub_category || '심화과정' }}
                            </span>
                        </div>
                        <h2 class="text-lg font-bold text-slate-900 mb-1">{{ course.title }}</h2>
                        <p class="text-sm text-slate-600 line-clamp-1">{{ course.description }}</p>
                    </div>
                </div>

                <!-- Session List -->
                <div class="px-6 py-6 max-h-[55vh] overflow-y-auto custom-scrollbar bg-white">
                    <div class="mb-4">
                        <div class="flex items-center gap-2 mb-4">
                            <span
                                class="flex items-center justify-center size-8 rounded-lg bg-indigo-100 text-indigo-700">
                                <span class="material-symbols-outlined text-lg">videocam</span>
                            </span>
                            <h4 class="text-base font-bold text-slate-900">실시간 강의 (Live Lecture)</h4>
                        </div>
                        <div class="space-y-3 pl-3 border-l-2 border-slate-100 ml-4">
                            <div v-for="session in sessions" :key="session.session_id"
                                class="relative group bg-white hover:bg-slate-50 rounded-xl p-4 border border-slate-200 hover:border-indigo-300 transition-all ml-4">
                                <div
                                    class="absolute w-4 h-4 bg-white border-2 border-indigo-200 rounded-full -left-[33px] top-6 group-hover:border-indigo-500 transition-colors">
                                </div>
                                <div class="flex flex-col sm:flex-row gap-4 sm:items-start">
                                    <div class="min-w-[80px]">
                                        <span class="text-xs font-bold text-indigo-600 bg-indigo-50 px-2 py-1 rounded">
                                            Session {{ String(session.order).padStart(2, '0') }}
                                        </span>
                                    </div>
                                    <div class="flex-1">
                                        <h5 class="text-sm font-bold text-slate-900 mb-1 group-hover:text-indigo-700">
                                            {{ session.title }}
                                        </h5>
                                        <p class="text-xs text-slate-500 leading-relaxed">
                                            {{ session.description }}
                                        </p>
                                    </div>
                                    <div class="shrink-0" v-if="session.type === 'quiz'">
                                        <span
                                            class="inline-flex items-center gap-1 px-2 py-1 rounded bg-slate-100 text-slate-600 text-[10px] font-bold border border-slate-200">
                                            <span class="material-symbols-outlined text-[12px]">quiz</span> 퇴실 퀴즈 포함
                                        </span>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Footer Actions -->
                <div
                    class="bg-slate-50 px-6 py-4 flex flex-row-reverse sm:px-6 border-t border-slate-200 rounded-b-2xl gap-3">
                    <button @click="$emit('close')"
                        class="mt-3 inline-flex w-full justify-center rounded-lg bg-white px-5 py-2.5 text-sm font-bold text-slate-600 shadow-sm ring-1 ring-inset ring-slate-300 hover:bg-slate-50 sm:mt-0 sm:w-auto transition-colors"
                        type="button">
                        닫기
                    </button>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import type { Course, Session } from '@/types';

defineProps<{
    isOpen: boolean;
    course: Course;
    sessions: Session[];
}>();

defineEmits<{
    (e: 'close'): void;
}>();
</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar {
    width: 6px;
}

.custom-scrollbar::-webkit-scrollbar-track {
    background: #f1f5f9;
    border-radius: 4px;
}

.custom-scrollbar::-webkit-scrollbar-thumb {
    background: #cbd5e1;
    border-radius: 4px;
}

.custom-scrollbar::-webkit-scrollbar-thumb:hover {
    background: #94a3b8;
}
</style>
