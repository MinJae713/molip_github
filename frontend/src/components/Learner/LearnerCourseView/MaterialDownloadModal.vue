<template>
    <div v-if="modelValue"
        class="fixed inset-0 bg-slate-900/60 backdrop-blur-sm z-[60] flex items-center justify-center p-4">
        <div class="bg-white w-full max-w-3xl max-h-[90vh] rounded-2xl shadow-2xl flex flex-col overflow-hidden">
            <div class="flex items-center justify-between px-6 py-4 border-b border-slate-100 flex-none">
                <div class="flex items-center gap-2">
                    <span class="material-symbols-outlined text-indigo-brand">folder_open</span>
                    <h2 class="text-xl font-bold text-slate-900">학습 자료실</h2>
                </div>
                <button @click="closeModal"
                    class="size-8 flex items-center justify-center rounded-full hover:bg-slate-100 text-slate-400 transition-colors">
                    <span class="material-symbols-outlined">close</span>
                </button>
            </div>
            <div class="flex-1 overflow-y-auto p-6 space-y-8 scrollbar-hide">
                <!-- 공통 자료 -->
                <section v-if="courseStore.courseMaterials.length > 0">
                    <div class="flex items-center justify-between mb-4">
                        <h3 class="text-sm font-bold text-slate-900 flex items-center gap-2">
                            <span class="size-1.5 rounded-full bg-indigo-brand"></span>
                            공통 자료
                            <span class="text-xs font-normal text-slate-400 ml-1">{{ courseStore.courseMaterials.length
                            }}개 파일</span>
                        </h3>
                    </div>
                    <div class="space-y-2">
                        <DownloadCard v-for="file in courseStore.courseMaterials" :key="file.fileId" :file="file" />
                    </div>
                </section>

                <!-- 세션별 자료 -->
                <section>
                    <div class="flex items-center justify-between mb-4">
                        <h3 class="text-sm font-bold text-slate-900 flex items-center gap-2">
                            <span class="size-1.5 rounded-full bg-indigo-brand"></span>
                            세션별 자료
                            <span class="text-xs font-normal text-slate-400 ml-1">{{
                                sessionStore.currentSessionMaterials.length }}개 파일</span>
                        </h3>
                    </div>
                    <div class="space-y-6">
                        <div v-if="sessionStore.currentSessionMaterials.length > 0"
                            class="bg-indigo-50/30 rounded-2xl p-4 border border-indigo-50">
                            <div class="text-[11px] font-bold text-indigo-600 uppercase tracking-wider mb-3">Current
                                Session</div>
                            <div class="space-y-2">
                                <DownloadCard v-for="file in sessionStore.currentSessionMaterials" :key="file.fileId"
                                    :file="file" />
                            </div>
                        </div>
                        <div v-else class="text-center py-8 text-slate-400 text-sm">
                            이 세션에 등록된 자료가 없습니다.
                        </div>
                    </div>
                </section>
            </div>
            <div class="p-4 border-t border-slate-100 bg-slate-50/50 flex justify-end">
                <button @click="closeModal"
                    class="px-8 py-2.5 bg-slate-800 text-white text-sm font-bold rounded-xl hover:bg-slate-900 transition-colors">
                    확인
                </button>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { onMounted, watch } from 'vue';
import { useLearnerCourseStore } from '@/stores/learnerCourse';
import { useLearnerSessionStore } from '@/stores/learnerSession';
import DownloadCard from './DownloadCard.vue';

const props = defineProps<{
    modelValue: boolean;
    courseId: number | string;
    sessionId: number | string;
    // fileList prop removed
}>();

const emit = defineEmits<{
    (e: 'close'): void;
    (e: 'update:modelValue', value: boolean): void;
}>();

const courseStore = useLearnerCourseStore();
const sessionStore = useLearnerSessionStore();

// Load data when modal opens
watch(() => props.modelValue, async (newVal) => {
    if (newVal) {
        await courseStore.fetchCourseMaterials(props.courseId);
        await sessionStore.fetchSessionMaterials(props.sessionId);
    }
});

const closeModal = () => {
    emit('close');
    emit('update:modelValue', false);
};
</script>