<template>
    <div v-if="isOpen" aria-labelledby="modal-title" aria-modal="true" class="fixed inset-0 z-[100] overflow-y-auto"
        role="dialog">
        <div class="fixed inset-0 bg-slate-900/60 backdrop-blur-sm transition-opacity" @click="$emit('close')"></div>

        <div class="flex min-h-full items-center justify-center p-4 text-center sm:p-0">
            <div
                class="relative transform overflow-hidden rounded-2xl bg-white text-left shadow-2xl transition-all sm:my-8 sm:w-full sm:max-w-md border border-slate-200">
                <div
                    class="bg-white px-4 pb-4 pt-5 sm:p-6 sm:pb-4 border-b border-slate-100 flex justify-between items-center">
                    <div>
                        <h3 class="text-lg font-bold leading-6 text-slate-900" id="modal-title">워크스페이스 참여 신청</h3>
                        <p class="text-xs text-slate-500 mt-0.5">{{ workspaceName }}에 참여할 역할을 선택해 주세요.</p>
                    </div>
                    <button @click="$emit('close')"
                        class="text-slate-400 hover:text-slate-500 transition-colors p-1 rounded-full hover:bg-slate-100"
                        type="button">
                        <span class="material-symbols-outlined text-xl">close</span>
                    </button>
                </div>

                <div class="p-5 bg-slate-50">
                    <div class="grid grid-cols-2 gap-4">
                        <div @click="selectedRole = 'LEARNER'" :class="[
                            'relative flex cursor-pointer flex-col items-center rounded-xl border p-4 text-center transition-all',
                            selectedRole === 'LEARNER' ? 'border-indigo-600 bg-indigo-50/50 shadow-sm' : 'border-slate-200 bg-white hover:border-indigo-400 hover:shadow-md'
                        ]">
                            <div v-if="selectedRole === 'LEARNER'" class="absolute right-2 top-2 text-indigo-600">
                                <span class="material-symbols-outlined text-lg">check_circle</span>
                            </div>
                            <div
                                :class="['mb-3 flex h-10 w-10 items-center justify-center rounded-full transition-colors', selectedRole === 'LEARNER' ? 'bg-indigo-100 text-indigo-600' : 'bg-slate-100 text-slate-500']">
                                <span class="material-symbols-outlined">school</span>
                            </div>
                            <h4 class="text-sm font-bold text-slate-900">수강생</h4>
                            <p class="mt-1 text-[11px] text-slate-500">학습 및 역량 강화</p>
                        </div>

                        <div @click="selectedRole = 'TRAINER'" :class="[
                            'relative flex cursor-pointer flex-col items-center rounded-xl border p-4 text-center transition-all',
                            selectedRole === 'TRAINER' ? 'border-indigo-600 bg-indigo-50/50 shadow-sm' : 'border-slate-200 bg-white hover:border-indigo-400 hover:shadow-md'
                        ]">
                            <div v-if="selectedRole === 'TRAINER'" class="absolute right-2 top-2 text-indigo-600">
                                <span class="material-symbols-outlined text-lg">check_circle</span>
                            </div>
                            <div
                                :class="['mb-3 flex h-10 w-10 items-center justify-center rounded-full transition-colors', selectedRole === 'TRAINER' ? 'bg-indigo-100 text-indigo-600' : 'bg-slate-100 text-slate-500']">
                                <span class="material-symbols-outlined">cast_for_education</span>
                            </div>
                            <h4 class="text-sm font-bold text-slate-900">강사</h4>
                            <p class="mt-1 text-[11px] text-slate-500">강의 및 콘텐츠 관리</p>
                        </div>
                    </div>
                </div>

                <div class="bg-white px-4 py-4 sm:flex sm:flex-row-reverse sm:px-6 border-t border-slate-200 gap-2">
                    <button @click="$emit('submit', selectedRole)"
                        class="inline-flex w-full justify-center rounded-lg bg-indigo-600 px-5 py-2 text-sm font-bold text-white shadow-sm hover:bg-indigo-700 sm:w-auto transition-colors"
                        type="button">
                        참여 신청
                    </button>
                    <button @click="$emit('close')"
                        class="mt-2 inline-flex w-full justify-center rounded-lg bg-white px-5 py-2 text-sm font-bold text-slate-700 shadow-sm ring-1 ring-inset ring-slate-300 hover:bg-slate-50 sm:mt-0 sm:w-auto transition-colors"
                        type="button">
                        취소
                    </button>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';

interface Props {
    isOpen: boolean;
    workspaceName?: string;
}

const props = withDefaults(defineProps<Props>(), {
    isOpen: false,
    workspaceName: '선택한 워크스페이스'
});

defineEmits<{
    (e: 'close'): void;
    (e: 'submit', role: 'LEARNER' | 'TRAINER'): void;
}>();

const selectedRole = ref<'LEARNER' | 'TRAINER'>('LEARNER');
</script>