<script setup lang="ts">
import { ref, watch } from 'vue';
import api from '@/api';
import { useWorkspaceStore } from '@/stores/workspace';
import WorkspaceCard from './WorkspaceCard.vue';

// [1] 데이터 인터페이스 정의
interface Workspace {
    id: string | number;
    name: string;
    logo_url?: string;
    location: string;
    description: string;
    memberCount: number;
    role: 'LEARNER' | 'TRAINER';
    status: number;
    typeIcon: string;
    themeColor: 'indigo' | 'blue' | 'amber';
    join_code?: string; // 초대 코드 필드 추가
}

interface Props {
    isOpen?: boolean;
    initialQuery?: string;
}

const props = withDefaults(defineProps<Props>(), {
    isOpen: false,
    initialQuery: '',
});

const workspaceStore = useWorkspaceStore();

const emit = defineEmits<{
    (e: 'close'): void;
}>();

// [2] 상태 관리
const searchQuery = ref(props.initialQuery);
const searchResults = ref<Workspace[]>([]);
const isLoading = ref(false);
const hasSearched = ref(false);
const errorMessage = ref('');

// [3] API 호출 및 필터링 함수
const fetchWorkspaces = async (keyword: string) => {
    // 공백 검색 방지
    const query = keyword.trim();
    if (!query) return;

    isLoading.value = true;
    hasSearched.value = true;
    errorMessage.value = '';
    searchResults.value = [];

    try {
        // [수정점] 검색 로직을 프론트엔드에서 처리하기 위해 전체 데이터를 가져옵니다.
        // 실제 운영 환경에서는 서버에서 필터링하는 파라미터(q, code 등)를 사용해야 합니다.
        // 하지만 목업/json-server 환경에서는 OR 조건(이름 포함 OR 코드 일치)을 
        // 쿼리 파라미터로 완벽히 구현하기 어렵거나 복잡하므로 클라이언트 필터링을 사용합니다.

        const response = await api.get('/workspaces');
        const allWorkspaces: Workspace[] = response.data;

        // [추가] 내 워크스페이스 상태 확인을 위해 Store 데이터 준비
        await workspaceStore.fetchCurrentWorkspace(); // 데이터가 없으면 로드

        // [검색 로직]
        // 1. 이름은 부분 일치 (includes)
        // 2. 초대 코드는 정확히 일치 (===)
        const filtered = allWorkspaces.filter(ws => {
            const nameMatch = ws.name.toLowerCase().includes(query.toLowerCase());
            // join_code가 있을 경우에만 비교
            const codeMatch = ws.join_code ? (ws.join_code === query) : false;

            return nameMatch || codeMatch;
        });

        // [상태 병합] 검색 결과의 워크스페이스가 내 워크스페이스 목록에 있다면 최신 상태(role, status)로 덮어씌움
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        searchResults.value = filtered.map((item: any) => {
            // eslint-disable-next-line @typescript-eslint/no-explicit-any
            const myWorkspace = workspaceStore.myWorkspaces.find((mw: any) => mw.id === item.id);
            if (myWorkspace) {
                return {
                    ...item,
                    role: myWorkspace.role,
                    status: myWorkspace.status
                };
            }
            return item;
        });

    } catch (error) {
        console.error('Failed to fetch workspaces:', error);
        errorMessage.value = '데이터를 불러오는 중 문제가 발생했습니다.';
    } finally {
        isLoading.value = false;
    }
};

const handleApply = async (id: string | number) => {
    if (!confirm('이 워크스페이스에 참여 신청을 하시겠습니까?')) return;

    // 선택된 워크스페이스 정보 찾기
    const targetWorkspace = searchResults.value.find(ws => ws.id === id);
    if (!targetWorkspace) return;

    try {
        // [API 명세 - Mock]
        // POST /workspaces/{id}/apply
        // 실제로는 별도의 엔드포인트이겠으나, Json-Server에서는 /my_workspaces에 데이터를 추가하는 것으로 시뮬레이션

        // 1. 이미 존재하는지 확인 (Mock Server 한계상 중복 방지 로직 필요)
        const checkRes = await api.get(`/my_workspaces?id=${id}`);
        if (checkRes.data.length > 0) {
            alert('이미 가입되었거나 신청 대기중인 워크스페이스입니다.');
            return;
        }

        // 2. 신청 데이터 생성
        const applyData = {
            ...targetWorkspace,
            status: 1, // 대기 상태
            role: 'LEARNER', // 기본 역할
            // 필요한 경우 user_id 등 추가
        };

        await api.post('/my_workspaces', applyData);

        alert('참여 신청이 완료되었습니다.');

        // 검색 결과에서 상태 업데이트 (UI 반영)
        targetWorkspace.status = 1;

    } catch (error) {
        console.error('Application failed:', error);
        alert('신청 처리 중 오류가 발생했습니다.');
    }
};

const handleClose = () => {
    emit('close');
};

const handleSearch = () => {
    fetchWorkspaces(searchQuery.value);
};

// [4] Watcher
watch(() => props.isOpen, (newVal) => {
    if (newVal) {
        searchQuery.value = props.initialQuery;
        if (props.initialQuery) {
            fetchWorkspaces(props.initialQuery);
        } else {
            // 초기화
            searchResults.value = [];
            hasSearched.value = false;
        }
    }
});
</script>

<template>
    <div v-if="isOpen"
        class="modal-backdrop fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm p-4">

        <div class="bg-white w-full max-w-2xl rounded-2xl shadow-2xl overflow-hidden flex flex-col max-h-[90vh]">

            <div class="px-6 py-5 border-b border-slate-100 flex items-center justify-between shrink-0">
                <h2 class="text-xl font-bold text-slate-900">워크스페이스 검색 및 코드 입력</h2>
                <button @click="handleClose"
                    class="text-slate-400 hover:text-slate-600 transition-colors rounded-full p-1 hover:bg-slate-100">
                    <span class="material-symbols-outlined">close</span>
                </button>
            </div>

            <div class="p-6 overflow-y-auto custom-scrollbar flex-1">
                <div class="relative mb-8">
                    <span class="material-symbols-outlined absolute left-4 top-1/2 -translate-y-1/2 text-slate-400">
                        search
                    </span>
                    <input v-model="searchQuery" autofocus :disabled="isLoading"
                        class="w-full pl-12 pr-4 py-4 bg-slate-50 border-slate-200 rounded-xl focus:ring-2 focus:ring-indigo-brand focus:border-indigo-brand text-slate-900 placeholder:text-slate-400 outline-none transition-all disabled:opacity-60 disabled:cursor-not-allowed"
                        placeholder="이름 또는 초대 코드를 입력하세요" type="text" @keyup.enter="handleSearch" />
                </div>

                <div class="min-h-[200px]">

                    <div v-if="isLoading"
                        class="flex flex-col items-center justify-center h-48 space-y-4 text-slate-400">
                        <span
                            class="material-symbols-outlined animate-spin text-4xl text-indigo-brand">progress_activity</span>
                        <span class="text-sm font-medium">검색 중입니다...</span>
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
                        <WorkspaceCard v-for="item in searchResults" :key="item.id" v-bind="item"
                            @apply="handleApply" />
                    </div>

                </div>
            </div>

            <div
                class="px-6 py-5 bg-slate-50 flex flex-col sm:flex-row-reverse gap-3 shrink-0 border-t border-slate-100">
                <button @click="handleSearch" :disabled="isLoading"
                    class="w-full sm:w-auto px-8 py-3 bg-deep-indigo hover:bg-indigo-900 disabled:bg-slate-300 disabled:cursor-not-allowed text-white font-bold rounded-xl transition-colors flex items-center justify-center gap-2">
                    <span v-if="isLoading"
                        class="material-symbols-outlined animate-spin text-sm">progress_activity</span>
                    <span>검색하기</span>
                </button>
                <button @click="handleClose"
                    class="w-full sm:w-auto px-8 py-3 bg-white border border-slate-200 text-slate-600 hover:bg-slate-100 font-bold rounded-xl transition-colors">
                    닫기
                </button>
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