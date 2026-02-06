<template>
    <div class="w-full space-y-8">
        <!-- Hero Section -->
        <div class="relative bg-linear-to-br from-indigo-900 to-slate-900 rounded-3xl p-8 md:p-12 overflow-hidden shadow-2xl">
            <!-- Decorative Elements -->
            <div class="absolute top-0 right-0 w-64 h-64 bg-white/5 rounded-full blur-3xl -translate-y-1/2 translate-x-1/2"></div>
            <div class="absolute bottom-0 left-0 w-48 h-48 bg-indigo-500/10 rounded-full blur-3xl translate-y-1/2 -translate-x-1/2"></div>
            
            <div class="relative z-10 flex flex-col md:flex-row justify-between items-start md:items-center gap-8">
                <div class="max-w-2xl">
                    <h2 class="text-3xl md:text-4xl font-bold text-white mb-4 leading-tight">
                        워크스페이스 관리 및 생성
                    </h2>
                    <p class="text-indigo-200 text-lg leading-relaxed mb-8">
                        조직의 학습 환경을 체계적으로 관리하세요.<br class="hidden md:block"/>
                        부서별, 프로젝트별 전용 워크스페이스를 통해 맞춤형 교육 공간을 쉽고 빠르게 구축할 수 있습니다.
                    </p>
                    <button @click="openCreateModal" 
                        class="group bg-white text-indigo-900 hover:bg-indigo-50 px-8 py-4 rounded-xl font-bold text-lg flex items-center gap-3 transition-all shadow-lg hover:shadow-xl transform hover:-translate-y-0.5">
                        <span class="material-symbols-outlined text-2xl group-hover:scale-110 transition-transform">add_circle</span>
                        신규 워크스페이스 생성
                    </button>
                </div>
                
                <!-- Icon/Illustration -->
                <div class="hidden md:block opacity-80">
                    <span class="material-symbols-outlined text-9xl text-white/20">domain</span>
                </div>
            </div>
        </div>

        <!-- Filter & Search Bar -->
        <div class="mb-10 flex flex-col md:flex-row gap-4">
            <!-- Search Bar -->
            <form @submit.prevent class="relative flex-1 shadow-lg rounded-xl bg-white">
                <div class="absolute inset-y-0 left-0 pl-5 flex items-center pointer-events-none">
                    <span class="material-symbols-outlined text-indigo-600 text-2xl">search</span>
                </div>
                <input v-model="searchQuery" type="text"
                    class="block w-full pl-14 pr-24 py-5 bg-white border-0 ring-1 ring-slate-200 rounded-xl text-slate-900 placeholder:text-slate-400 focus:ring-2 focus:ring-indigo-600 text-lg shadow-sm"
                    placeholder="워크스페이스명 검색..." />
                <div class="absolute inset-y-0 right-2 flex items-center">
                    <button type="submit"
                        class="bg-indigo-600 hover:bg-indigo-700 text-white px-6 py-2.5 rounded-lg font-bold text-base transition-colors cursor-pointer">
                        검색
                    </button>
                </div>
            </form>

            <!-- Sort Dropdown -->
            <div class="relative min-w-[160px]">
                <select v-model="sortBy" 
                    class="w-full h-full pl-5 pr-10 py-5 bg-white border-0 ring-1 ring-slate-200 rounded-xl text-slate-900 text-base font-bold appearance-none cursor-pointer focus:ring-2 focus:ring-indigo-600 shadow-lg">
                    <option value="LATEST">최신순</option>
                    <option value="OLDEST">오래된순</option>
                </select>
                <div class="absolute inset-y-0 right-4 flex items-center pointer-events-none text-slate-500">
                    <span class="material-symbols-outlined">sort</span>
                </div>
            </div>
        </div>

        <!-- Workspace Grid -->
        <div v-if="isLoading" class="flex justify-center py-20">
             <div class="animate-spin rounded-full h-12 w-12 border-4 border-indigo-200 border-t-indigo-600"></div>
        </div>

        <div v-else-if="filteredWorkspaces.length === 0" 
            class="flex flex-col items-center justify-center py-32 bg-slate-50 rounded-3xl border-2 border-dashed border-slate-200">
            <span class="material-symbols-outlined text-6xl text-slate-300 mb-4">folder_off</span>
            <p class="text-slate-500 text-xl font-medium">검색된 워크스페이스가 없습니다.</p>
        </div>

        <div v-else class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-6">
            <div v-for="ws in filteredWorkspaces" :key="ws.workspaceId" 
                class="group bg-white rounded-2xl border border-slate-200 p-6 hover:shadow-xl hover:border-indigo-100 transition-all duration-300 flex flex-col justify-between">
                
                <div class="flex justify-between items-start mb-6">
                    <div class="flex items-center gap-4">
                        <div class="w-16 h-16 rounded-2xl bg-linear-to-br from-indigo-50 to-purple-50 flex items-center justify-center text-xl font-bold text-indigo-600 border border-slate-100 shadow-inner overflow-hidden">
                            <img v-if="ws.logoUrl" :src="ws.logoUrl" class="w-full h-full object-cover" alt="Logo">
                            <span v-else class="text-2xl">{{ (ws.name || '?').substring(0, 1).toUpperCase() }}</span>
                        </div>
                        <div>
                            <h3 class="text-lg font-bold text-slate-900 mb-1 group-hover:text-indigo-600 transition-colors line-clamp-1">{{ ws.name }}</h3>
                            <div class="flex items-center gap-2">
                                <span class="px-2 py-0.5 bg-slate-100 text-slate-600 text-xs font-bold rounded">관리자</span>
                                <span class="flex items-center gap-1 text-xs font-medium text-emerald-600">
                                    <span class="w-1.5 h-1.5 rounded-full bg-emerald-500"></span> Active
                                </span>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="grid grid-cols-3 gap-2 mb-4 border-t border-b border-slate-50 py-4">
                     <div class="flex flex-col gap-1 items-center justify-center">
                        <span class="text-xs text-slate-400 flex items-center gap-1">
                            <span class="material-symbols-outlined text-[14px]">group</span> 참여 인원
                        </span>
                        <span class="text-sm font-bold text-slate-700">{{ ws.memberCount || 0 }}명</span>
                     </div>
                     <div class="flex flex-col gap-1 items-center justify-center">
                        <span class="text-xs text-slate-400 flex items-center gap-1">
                            <span class="material-symbols-outlined text-[14px]">library_books</span> 활성 강의
                        </span>
                        <span class="text-sm font-bold text-slate-700">{{ ws.courseCount || 0 }}개</span>
                     </div>
                     <div class="flex flex-col gap-1 items-center justify-center">
                        <span class="text-xs text-slate-400 flex items-center gap-1">
                            <span class="material-symbols-outlined text-[14px]">calendar_today</span> 생성일
                        </span>
                        <span class="text-sm font-bold text-slate-700 whitespace-nowrap">{{ formatDate(ws.createdAt) }}</span>
                     </div>
                </div>

                <div class="flex items-center gap-3">
                    <button @click="handleEnter(ws.workspaceId)" 
                        class="cursor-pointer flex-1 bg-indigo-50 border border-indigo-200 hover:bg-indigo-200 text-indigo-600 font-bold py-3 rounded-xl transition-all flex items-center justify-center gap-2">
                        <span class="material-symbols-outlined text-[20px]">settings_suggest</span>
                        설정 및 관리
                    </button>
                </div>
            </div>
        </div>

        <!-- Create Modal -->
        <div v-if="isCreateModalOpen" class="fixed inset-0 z-50 flex items-center justify-center p-4">
            <div class="absolute inset-0 bg-slate-900/40 backdrop-blur-sm transition-opacity" @click="closeCreateModal"></div>
            
            <div class="relative bg-white rounded-3xl w-full max-w-lg shadow-2xl p-8 transform transition-all scale-100">
                <div class="flex justify-between items-center mb-8">
                    <h3 class="text-2xl font-bold text-slate-900">새 워크스페이스 생성</h3>
                    <button @click="closeCreateModal" class="p-2 hover:bg-slate-100 rounded-full text-slate-400 hover:text-slate-600 transition-colors">
                        <span class="material-symbols-outlined">close</span>
                    </button>
                </div>

                <form @submit.prevent="handleSubmitCreate" class="space-y-6">
                    <div>
                        <label class="block text-sm font-bold text-slate-700 mb-2">워크스페이스 이름</label>
                        <input v-model="createForm.name" type="text" required
                            class="w-full px-4 py-3 rounded-xl bg-slate-50 border-slate-200 focus:bg-white focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 transition-all font-medium"
                            placeholder="예: 개발팀 교육 허브" />
                    </div>
                    
                    <div>
                        <label class="block text-sm font-bold text-slate-700 mb-2">설명</label>
                        <textarea v-model="createForm.description" rows="4" required
                            class="w-full px-4 py-3 rounded-xl bg-slate-50 border-slate-200 focus:bg-white focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 transition-all resize-none"
                            placeholder="워크스페이스의 목적이나 대상을 간단히 설명해주세요."></textarea>
                    </div>

                    <div class="flex gap-3 mt-8">
                        <button type="button" @click="closeCreateModal"
                            class="flex-1 px-6 py-3.5 rounded-xl border border-slate-200 font-bold text-slate-600 hover:bg-slate-50 transition-colors">
                            취소
                        </button>
                        <button type="submit" :disabled="isSubmitting"
                            class="flex-1 px-6 py-3.5 rounded-xl bg-indigo-600 text-white font-bold hover:bg-indigo-700 transition-all transform hover:scale-[1.02] disabled:opacity-70 disabled:hover:scale-100 flex justify-center items-center gap-2">
                            <span v-if="isSubmitting" class="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin"></span>
                            <span>워크스페이스 생성</span>
                        </button>
                    </div>
                </form>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useWorkspaceStore } from '@/stores/workspace';
import type { Workspace } from '@/types';

const router = useRouter();
const workspaceStore = useWorkspaceStore();

const isLoading = ref(false);
const isSubmitting = ref(false);
const isCreateModalOpen = ref(false);
const searchQuery = ref('');
const sortBy = ref<'LATEST' | 'OLDEST'>('LATEST');

const createForm = ref({
    name: '',
    description: ''
});

// 워크스페이스 목록: 관리자 전용 목록(adminWorkspaces) 사용
const workspaces = computed(() => workspaceStore.adminWorkspaces);

const filteredWorkspaces = computed(() => {
    let result = [...workspaces.value]; // Copy array

    // 검색 필터
    if (searchQuery.value) {
        const query = searchQuery.value.toLowerCase();
        result = result.filter(ws => 
            ws.name.toLowerCase().includes(query)
        );
    }

    // 정렬 로직 (생성일 기준)
    result.sort((a, b) => {
        const dateA = new Date(a.createdAt || 0).getTime();
        const dateB = new Date(b.createdAt || 0).getTime();
        return sortBy.value === 'LATEST' ? dateB - dateA : dateA - dateB;
    });

    return result;
});

const fetchWorkspaces = async () => {
    isLoading.value = true;
    try {
        await workspaceStore.fetchAllWorkspaces();
    } catch (e) {
        console.error("목록 로드 실패");
    } finally {
        isLoading.value = false;
    }
};

const openCreateModal = () => {
    createForm.value = { name: '', description: '' };
    isCreateModalOpen.value = true;
};

const closeCreateModal = () => {
    isCreateModalOpen.value = false;
};

const handleSubmitCreate = async () => {
    if (!createForm.value.name) return;
    
    try {
        isSubmitting.value = true;
        await workspaceStore.createWorkspace({
            name: createForm.value.name,
            description: createForm.value.description
        });
        closeCreateModal();
        // createWorkspace 내부에서 fetchAllWorkspaces 호출하지 않는다면 여기서 호출
        await workspaceStore.fetchAllWorkspaces(); 
    } catch (error) {
        alert('워크스페이스 생성에 실패했습니다.');
    } finally {
        isSubmitting.value = false;
    }
};

const handleEnter = (id: string | number) => {
    // 관리자 모드로 진입
    router.push({ name: 'workspaceAdmin', params: { id } });
};

const formatDate = (dateString?: string) => {
    if (!dateString) return '-';
    const date = new Date(dateString);
    return `${date.getFullYear()}.${String(date.getMonth() + 1).padStart(2, '0')}.${String(date.getDate()).padStart(2, '0')}`;
};

onMounted(() => {
    fetchWorkspaces();
});
</script>

<style scoped>
/* 필요한 경우 추가 스타일 */
</style>
