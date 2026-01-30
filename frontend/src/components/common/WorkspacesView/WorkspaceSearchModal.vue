<script setup lang="ts">
import { ref, watch, computed } from 'vue';
import { useWorkspaceStore } from '@/stores/workspace';
import { useUserStore } from '@/stores/user';
import WorkspaceCard from './WorkspaceCard.vue';
import type { Workspace } from '@/types';

interface Props {
    isOpen?: boolean;
    initialQuery?: string;
}

const props = withDefaults(defineProps<Props>(), {
    isOpen: false,
    initialQuery: '',
});

const emit = defineEmits<{
    (e: 'close'): void;
}>();

const workspaceStore = useWorkspaceStore();
const userStore = useUserStore();

const user = computed(() => userStore.user);

// 상태 관리
const searchQuery = ref('');
const searchResults = ref<Workspace[]>([]);
const isLoading = ref(false);
const hasSearched = ref(false);
const errorMessage = ref('');

// Step 관리
const step = ref(1);
const selectedWorkspace = ref<Workspace | null>(null);
const selectedRole = ref<'LEARNER' | 'TRAINER'>('LEARNER');

// [상태 리셋 함수]
const resetModalState = () => {
    step.value = 1;
    searchQuery.value = '';
    searchResults.value = [];
    hasSearched.value = false;
    selectedWorkspace.value = null;
    selectedRole.value = 'LEARNER';
    errorMessage.value = '';
};

// [검색 함수]
const handleSearch = async () => {
    const query = searchQuery.value.trim();
    if (!query) return;

    isLoading.value = true;
    hasSearched.value = true;
    errorMessage.value = '';

    try {
        // 서버에서 가입되지 않은 워크스페이스만 검색 결과로 반환한다고 가정
        const results = await workspaceStore.searchWorkspaces(query);
        searchResults.value = results;
    } catch (error) {
        console.error('검색 실패:', error);
        errorMessage.value = '검색 중 오류가 발생했습니다.';
    } finally {
        isLoading.value = false;
    }
};

// [신청 단계로 이동]
const handleApply = (id: string | number) => {
    const target = searchResults.value.find(ws => String(ws.workspaceId) === String(id));
    if (!target) return;

    selectedWorkspace.value = target;
    selectedRole.value = 'LEARNER';
    step.value = 2;
};

// [최종 신청 완료]
const confirmApplication = async () => {
    if (!selectedWorkspace.value || !user.value) return;

    if (!confirm(`${selectedRole.value === 'LEARNER' ? '수강생' : '강사'} 권한으로 신청하시겠습니까?`)) return;

    isLoading.value = true;
    try {
        const success = await workspaceStore.joinWorkspace(
            selectedWorkspace.value.workspaceId,
            selectedRole.value,
            user.value.memberId,
            "PENDING",
            new Date().toISOString()
        );

        if (success) {
            alert('참여 신청이 완료되었습니다.\n관리자 승인 후 입장 가능합니다.');
            handleClose();
        } else {
            alert('신청 처리 중 문제가 발생했습니다.');
        }
    } catch (error) {
        console.error('Join failed:', error);
        alert('신청 중 오류가 발생했습니다.');
    } finally {
        isLoading.value = false;
    }
};

const handleClose = async () => {
    emit('close');
    // 다시 검색
    await workspaceStore.fetchMyWorkspaces(true);
};

// [와치] 모달이 닫힐 때 상태를 초기화하고, 열릴 때 초기 검색어를 처리합니다.
watch(() => props.isOpen, (newVal) => {
    if (newVal) {
        // 모달이 열릴 때 초기 검색어가 있으면 세팅하고 검색 실행
        if (props.initialQuery) {
            searchQuery.value = props.initialQuery;
            handleSearch();
        }
    } else {
        resetModalState();
    }
});
</script>

<template>
    <div v-if="isOpen"
        class="modal-backdrop fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm p-4">

        <div class="bg-white w-full max-w-2xl rounded-2xl shadow-2xl overflow-hidden flex flex-col max-h-[90vh]">

            <!-- Header -->
            <div class="px-6 py-5 border-b border-slate-100 flex items-center justify-between shrink-0">
                <h2 class="text-xl font-bold text-slate-900">
                    {{ step === 1 ? '워크스페이스 검색' : '참여 역할 선택' }}
                </h2>
                <button @click="handleClose"
                    class="text-slate-400 hover:text-slate-600 transition-colors rounded-full p-1 hover:bg-slate-100">
                    <span class="material-symbols-outlined">close</span>
                </button>
            </div>

            <!-- Content Area -->
            <div class="flex-1 overflow-y-auto custom-scrollbar">

                <!-- Step 1: Search -->
                <div v-if="step === 1" class="p-6">
                    <div class="relative mb-8">
                        <span class="material-symbols-outlined absolute left-4 top-1/2 -translate-y-1/2 text-slate-400">
                            search
                        </span>
                        <input v-model="searchQuery" autofocus :disabled="isLoading"
                            class="w-full pl-12 pr-4 py-4 bg-slate-50 border-slate-200 rounded-xl focus:ring-2 focus:ring-indigo-600 focus:border-indigo-600 text-slate-900 placeholder:text-slate-400 outline-none transition-all disabled:opacity-60"
                            placeholder="이름 또는 초대 코드를 입력하세요" type="text" @keyup.enter="handleSearch" />
                    </div>

                    <div class="min-h-[200px]">
                        <div v-if="isLoading"
                            class="flex flex-col items-center justify-center h-48 space-y-4 text-indigo-600">
                            <span class="material-symbols-outlined animate-spin text-4xl">progress_activity</span>
                            <span class="text-sm font-medium">검색 중...</span>
                        </div>

                        <div v-else-if="errorMessage"
                            class="flex flex-col items-center justify-center h-48 space-y-2 text-rose-500">
                            <span class="material-symbols-outlined text-4xl">error</span>
                            <p>{{ errorMessage }}</p>
                        </div>

                        <div v-else-if="hasSearched && searchResults.length === 0"
                            class="flex flex-col items-center justify-center h-48 space-y-2 text-slate-500">
                            <span class="material-symbols-outlined text-4xl text-slate-300">search_off</span>
                            <p>검색 결과가 없습니다.</p>
                        </div>

                        <div v-else class="space-y-4">
                            <!-- WorkspaceCard에 status=0(Guest)을 강제하여 신청 버튼이 나오도록 유도 -->
                            <WorkspaceCard v-for="item in searchResults" :key="item.workspaceId"
                                :workspace="{ ...item, myStatus: 0 }" @apply="handleApply" />
                        </div>
                    </div>
                </div>

                <!-- Step 2: Role Selection -->
                <div v-else class="p-8 flex flex-col items-center justify-center space-y-8">
                    <div class="text-center space-y-2">
                        <div
                            class="inline-flex items-center justify-center size-16 rounded-full bg-indigo-50 text-indigo-600 mb-2">
                            <span class="material-symbols-outlined text-3xl">domain</span>
                        </div>
                        <h3 class="text-2xl font-bold text-slate-900">{{ selectedWorkspace?.name }}</h3>
                        <p class="text-slate-500">어떤 역할로 참여하시겠습니까?</p>
                    </div>

                    <div class="grid grid-cols-1 md:grid-cols-2 gap-4 w-full max-w-lg">
                        <button @click="selectedRole = 'LEARNER'"
                            :class="['relative p-6 rounded-xl border-2 text-left transition-all',
                                selectedRole === 'LEARNER' ? 'border-indigo-600 bg-indigo-50/50' : 'border-slate-100 hover:border-indigo-200 hover:bg-slate-50']">
                            <div class="flex items-start justify-between mb-4">
                                <div class="p-2 bg-white rounded-lg shadow-sm text-indigo-600">
                                    <span class="material-symbols-outlined text-2xl">school</span>
                                </div>
                                <span v-if="selectedRole === 'LEARNER'"
                                    class="material-symbols-outlined text-indigo-600">check_circle</span>
                            </div>
                            <h4 class="font-bold text-slate-900 mb-1">수강생 (Learner)</h4>
                            <p class="text-sm text-slate-500">강의를 수강하고 과제를 제출하며 학습을 진행합니다.</p>
                        </button>

                        <button @click="selectedRole = 'TRAINER'"
                            :class="['relative p-6 rounded-xl border-2 text-left transition-all',
                                selectedRole === 'TRAINER' ? 'border-emerald-600 bg-emerald-50/50' : 'border-slate-100 hover:border-emerald-200 hover:bg-slate-50']">
                            <div class="flex items-start justify-between mb-4">
                                <div class="p-2 bg-white rounded-lg shadow-sm text-emerald-600">
                                    <span class="material-symbols-outlined text-2xl">cast_for_education</span>
                                </div>
                                <span v-if="selectedRole === 'TRAINER'"
                                    class="material-symbols-outlined text-emerald-600">check_circle</span>
                            </div>
                            <h4 class="font-bold text-slate-900 mb-1">강사 (Trainer)</h4>
                            <p class="text-sm text-slate-500">강의를 개설하고 수강생들의 진도와 성취도를 관리합니다.</p>
                        </button>
                    </div>
                </div>
            </div>

            <!-- Footer -->
            <div
                class="px-6 py-5 bg-slate-50 flex flex-col sm:flex-row-reverse gap-3 shrink-0 border-t border-slate-100">
                <template v-if="step === 1">
                    <button @click="handleSearch" :disabled="isLoading"
                        class="w-full sm:w-auto px-8 py-3 bg-indigo-600 hover:bg-indigo-700 disabled:bg-slate-300 text-white font-bold rounded-xl transition-colors flex items-center justify-center gap-2">
                        <span v-if="isLoading"
                            class="material-symbols-outlined animate-spin text-sm">progress_activity</span>
                        <span>검색하기</span>
                    </button>
                    <button @click="handleClose"
                        class="w-full sm:w-auto px-8 py-3 bg-white border border-slate-200 text-slate-600 hover:bg-slate-100 font-bold rounded-xl transition-colors">
                        취소
                    </button>
                </template>

                <template v-else>
                    <button @click="confirmApplication" :disabled="isLoading"
                        class="w-full sm:w-auto px-8 py-3 bg-indigo-600 hover:bg-indigo-700 disabled:bg-slate-300 text-white font-bold rounded-xl transition-colors flex items-center justify-center gap-2">
                        <span v-if="isLoading"
                            class="material-symbols-outlined animate-spin text-sm">progress_activity</span>
                        <span>신청 완료</span>
                    </button>
                    <button @click="step = 1" :disabled="isLoading"
                        class="w-full sm:w-auto px-8 py-3 bg-white border border-slate-200 text-slate-600 hover:bg-slate-100 font-bold rounded-xl transition-colors">
                        이전으로
                    </button>
                </template>
            </div>
        </div>
    </div>
</template>

<style scoped>
/* 커스텀 스크롤바 스타일 */
.custom-scrollbar::-webkit-scrollbar {
    width: 6px;
}

.custom-scrollbar::-webkit-scrollbar-track {
    background: transparent;
}

.custom-scrollbar::-webkit-scrollbar-thumb {
    background-color: #cbd5e1;
    border-radius: 20px;
}

.custom-scrollbar::-webkit-scrollbar-thumb:hover {
    background-color: #94a3b8;
}

/* Tailwind Config에 정의되지 않은 컬러 임시 대응 */
.bg-deep-indigo {
    background-color: #4338ca;
    /* indigo-700 예시 */
}

.text-indigo-brand {
    color: #4f46e5;
    /* indigo-600 예시 */
}
</style>