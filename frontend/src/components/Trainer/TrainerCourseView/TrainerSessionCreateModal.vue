<template>
    <div v-if="isOpen" class="relative z-[100]" aria-labelledby="modal-title" role="dialog" aria-modal="true">
        <!-- Background overlay -->
        <div class="fixed inset-0 bg-gray-900/75 backdrop-blur-sm transition-opacity" @click="closeModal"></div>

        <div class="fixed inset-0 z-10 w-screen overflow-y-auto">
            <div class="flex min-h-full items-center justify-center p-4 text-center sm:p-0">
                <!-- Modal panel -->
                <div
                    class="relative transform overflow-hidden rounded-2xl bg-white text-left shadow-2xl transition-all sm:my-8 sm:w-full sm:max-w-xl border border-gray-100">

                    <!-- Header -->
                    <div class="bg-indigo-600 px-4 py-4 sm:px-6">
                        <h3 class="text-base font-bold leading-6 text-white flex items-center gap-2" id="modal-title">
                            <span class="material-symbols-outlined text-[22px]">add_box</span>
                            새 세션 생성
                        </h3>
                        <p class="mt-1 text-sm text-indigo-100">새로운 라이브 세션을 위한 정보를 입력해주세요.</p>
                    </div>

                    <!-- Body -->
                    <div class="px-4 py-5 sm:p-6 space-y-5">
                        <div class="space-y-4">
                            <div>
                                <label class="block text-sm font-bold text-gray-700 mb-1">세션 제목 <span
                                        class="text-red-500">*</span></label>
                                <input v-model="form.title" type="text"
                                    class="block w-full rounded-lg border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm py-2.5"
                                    placeholder="예: 3주차 - 고객 데이터 분석 실습" />
                            </div>

                            <div>
                                <label class="block text-sm font-bold text-gray-700 mb-1">설명</label>
                                <textarea v-model="form.description" rows="3"
                                    class="block w-full rounded-lg border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm"
                                    placeholder="세션에 대한 간단한 설명을 입력하세요."></textarea>
                            </div>

                            <div class="grid grid-cols-2 gap-5">
                                <div>
                                    <label class="block text-sm font-bold text-gray-700 mb-1">날짜 <span
                                            class="text-red-500">*</span></label>
                                    <input v-model="form.date" type="date"
                                        class="block w-full rounded-lg border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm py-2.5 text-center" />
                                </div>
                                <div>
                                    <label class="block text-sm font-bold text-gray-700 mb-1">시간 (시작 - 종료) <span
                                            class="text-red-500">*</span></label>
                                    <div class="flex items-center gap-2">
                                        <input v-model="form.startAt" type="time"
                                            class="block w-full rounded-lg border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm py-2.5 text-center" />
                                        <span class="text-gray-400">-</span>
                                        <input v-model="form.endAt" type="time"
                                            class="block w-full rounded-lg border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm py-2.5 text-center" />
                                    </div>
                                </div>
                            </div>

                            <div class="pt-4 border-t border-gray-100">
                                <label class="block text-sm font-bold text-gray-700 mb-3">옵션 설정</label>
                                <div class="grid grid-cols-1 sm:grid-cols-3 gap-4">
                                    <div class="relative flex items-start p-3 rounded-lg border border-gray-200 hover:bg-gray-50 transition-colors cursor-pointer"
                                        @click="form.useFocus = !form.useFocus">
                                        <div class="flex h-5 items-center">
                                            <input v-model="form.useFocus" id="useFocus" type="checkbox"
                                                class="h-4 w-4 rounded border-gray-300 text-indigo-600 focus:ring-indigo-500"
                                                @click.stop />
                                        </div>
                                        <div class="ml-3 text-sm">
                                            <label for="useFocus"
                                                class="font-medium text-gray-700 select-none cursor-pointer">집중도</label>
                                        </div>
                                    </div>

                                    <div class="relative flex items-start p-3 rounded-lg border border-gray-200 hover:bg-gray-50 transition-colors cursor-pointer"
                                        @click="form.useEmotion = !form.useEmotion">
                                        <div class="flex h-5 items-center">
                                            <input v-model="form.useEmotion" id="useEmotion" type="checkbox"
                                                class="h-4 w-4 rounded border-gray-300 text-indigo-600 focus:ring-indigo-500"
                                                @click.stop />
                                        </div>
                                        <div class="ml-3 text-sm">
                                            <label for="useEmotion"
                                                class="font-medium text-gray-700 select-none cursor-pointer">감정
                                                분석</label>
                                        </div>
                                    </div>

                                    <div
                                        class="relative flex items-center p-3 rounded-lg border border-gray-200 hover:bg-gray-50 transition-colors">
                                        <div class="flex h-5 items-center">
                                            <input v-model="form.useQuiz" id="useQuiz" type="checkbox"
                                                class="h-4 w-4 rounded border-gray-300 text-indigo-600 focus:ring-indigo-500" />
                                        </div>
                                        <div class="ml-3 text-sm flex-1">
                                            <label for="useQuiz"
                                                class="font-medium text-gray-700 select-none cursor-pointer">퀴즈</label>
                                        </div>
                                        <input v-if="form.useQuiz" v-model.number="form.quizCount" type="number" min="1"
                                            class="w-16 ml-2 rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-xs py-1"
                                            placeholder="개수" @click.stop />
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Footer -->
                    <div class="bg-gray-50 px-4 py-3 sm:flex sm:flex-row-reverse sm:px-6 border-t border-gray-100">
                        <button @click="createSession" type="button" :disabled="isLoading"
                            class="inline-flex w-full justify-center rounded-lg bg-indigo-600 px-3 py-2.5 text-sm font-bold text-white shadow-sm hover:bg-indigo-500 sm:ml-3 sm:w-auto transition-all disabled:opacity-50 disabled:cursor-not-allowed">
                            <span v-if="isLoading"
                                class="mr-2 animate-spin material-symbols-outlined text-sm">progress_activity</span>
                            {{ isLoading ? '생성 중...' : '세션 생성하기' }}
                        </button>
                        <button @click="closeModal" type="button"
                            class="mt-3 inline-flex w-full justify-center rounded-lg bg-white px-3 py-2.5 text-sm font-bold text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 hover:bg-gray-50 sm:mt-0 sm:w-auto transition-all">
                            취소
                        </button>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue';
import { useRoute } from 'vue-router';
import { useTrainerSessionStore } from '@/stores/trainerSession';
import { useWorkspaceStore } from '@/stores/workspace';

const props = defineProps<{
    isOpen: boolean;
}>();

const emit = defineEmits(['close', 'created']);

const route = useRoute();
const sessionStore = useTrainerSessionStore();
const workspaceStore = useWorkspaceStore();
const isLoading = ref(false);

const form = reactive({
    title: '',
    description: '',
    date: '',
    startAt: '',
    endAt: '',
    useFocus: true,
    useEmotion: true,
    useQuiz: true,
    quizCount: 1,
});

const closeModal = () => {
    emit('close');
};

const createSession = async () => {
    if (!form.title || !form.date || !form.startAt || !form.endAt) {
        alert('필수 정보를 모두 입력해주세요.');
        return;
    }

    isLoading.value = true;
    const courseId = Array.isArray(route.params.courseId) ? route.params.courseId[0] : route.params.courseId;
    const workspaceId = workspaceStore.currentWorkspace?.workspaceId;

    if (courseId && workspaceId) {
        const success = await sessionStore.createSession(workspaceId, courseId, { ...form });
        if (success) {
            alert('세션이 생성되었습니다.');
            emit('created');
            closeModal();
            // Reset form
            Object.assign(form, {
                title: '',
                description: '',
                date: '',
                startAt: '',
                endAt: '',
                useFocus: true,
                useEmotion: true,
                useQuiz: true,
                quizCount: 1,
            });
        } else {
            alert('세션 생성에 실패했습니다.');
        }
    }
    isLoading.value = false;
};
</script>
