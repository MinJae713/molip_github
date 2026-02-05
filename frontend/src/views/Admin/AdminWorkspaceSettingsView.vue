<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useWorkspaceStore } from '@/stores/workspace';
import { useRoute, useRouter } from 'vue-router';

const store = useWorkspaceStore();
const route = useRoute();
const router = useRouter();

// 로컬 폼 상태 관리
const workspaceId = route.params.id as string;
const form = ref({
  name: '',
  description: '',
  logo_url: ''
});

// 변경 사항 체크를 위한 원본 데이터 복사본
const originalData = ref({ name: '', description: '', logo_url: '' });

// 데이터 로드
onMounted(async () => {
  if (workspaceId) {
    await store.setCurrentWorkspaceById(workspaceId);
    if (store.currentWorkspace) {
      const { name, description, logoUrl } = store.currentWorkspace;
      form.value = { name: name || '', description: description || '', logo_url: logoUrl || '' };
      originalData.value = { ...form.value };
    }
  }
});

// 변경 사항 여부 (저장 버튼 활성화)
const isDirty = computed(() => {
  return JSON.stringify(form.value) !== JSON.stringify(originalData.value);
});

// 저장 로직
const handleSave = async () => {
  if (!isDirty.value) return;
  try {
    const payload = {
      name: form.value.name,
      description: form.value.description,
      logoUrl: form.value.logo_url
    };
    const success = await store.updateWorkspace(Number(workspaceId), payload);
    if (success) {
      originalData.value = { ...form.value };
      alert('변경사항이 저장되었습니다.');
    }
  } catch (error) {
    alert('저장 중 오류가 발생했습니다.');
  }
};

// 취소 로직
const handleCancel = () => {
  form.value = { ...originalData.value };
};

// 삭제 로직
const handleDelete = async () => {
  if (confirm('정말로 이 워크스페이스를 삭제하시겠습니까? 모든 데이터가 영구 삭제됩니다.')) {
    const success = await store.deleteWorkspace(workspaceId);
    if (success) {
      router.push('/workspaces');
    }
  }
};

// 로고 업로드 처리
const onLogoUpload = (event: Event) => {
  const file = (event.target as HTMLInputElement).files?.[0];
  if (file) {
    const reader = new FileReader();
    reader.onload = (e) => {
      form.value.logo_url = e.target?.result as string;
    };
    reader.readAsDataURL(file);
  }
};
</script>

<template>
  <main class="flex-1 bg-[#F8FAFC] min-h-screen p-8">
    <div class="max-w-5xl mx-auto">
      
      <header class="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-8">
        <div class="flex items-center gap-4">
          <div class="w-12 h-12 bg-[#6366F1] rounded-xl flex items-center justify-center text-white font-bold text-lg shadow-lg shadow-indigo-100">
            {{ form.name ? form.name.substring(0, 2).toUpperCase() : 'TC' }}
          </div>
          <div>
            <div class="flex items-center gap-2">
              <h1 class="text-2xl font-bold text-slate-900 tracking-tight">워크스페이스 설정</h1>
              <span class="px-2 py-0.5 bg-indigo-50 text-indigo-600 text-[10px] font-bold rounded uppercase tracking-wider border border-indigo-100">Enterprise</span>
            </div>
            <p class="text-slate-500 text-sm mt-0.5">워크스페이스의 대표 프로필 정보와 기본 설정을 관리합니다.</p>
          </div>
        </div>
        
        <div class="flex items-center gap-3">
          <button 
            @click="handleCancel"
            class="px-5 py-2.5 bg-white border border-slate-200 rounded-lg text-sm font-semibold text-slate-600 hover:bg-slate-50 transition-all"
          >
            취소
          </button>
          <button 
            @click="handleSave"
            :disabled="!isDirty"
            :class="[
              'px-6 py-2.5 rounded-lg text-sm font-semibold transition-all shadow-sm',
              isDirty ? 'bg-[#6366F1] text-white hover:bg-indigo-700' : 'bg-slate-200 text-slate-400 cursor-not-allowed'
            ]"
          >
            변경사항 저장
          </button>
        </div>
      </header>

      <div class="space-y-6">
        <section class="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
          <div class="p-8">
            <h3 class="text-lg font-bold text-slate-900 mb-8">워크스페이스 프로필</h3>
            
            <div class="grid grid-cols-1 md:grid-cols-12 gap-10">
              <div class="md:col-span-3">
                <label class="block text-sm font-bold text-slate-700 mb-4">워크스페이스 로고</label>
                <div class="relative group w-36 h-36">
                  <div class="w-full h-full rounded-2xl border-2 border-dashed border-slate-200 bg-slate-50 flex items-center justify-center overflow-hidden transition-all group-hover:border-indigo-300">
                    <template v-if="form.logo_url">
                      <img :src="form.logo_url" alt="Workspace Logo" class="w-full h-full object-cover" />
                    </template>
                    <div v-else class="flex flex-col items-center text-slate-300">
                      <span class="material-symbols-outlined text-4xl">image</span>
                    </div>
                  </div>
                  <label class="absolute -bottom-2 -right-2 w-10 h-10 bg-white border border-slate-200 rounded-full flex items-center justify-center text-slate-600 shadow-md cursor-pointer hover:bg-indigo-50 hover:text-[#6366F1] transition-all">
                    <span class="material-symbols-outlined text-xl font-light">upload</span>
                    <input type="file" class="hidden" accept="image/*" @change="onLogoUpload" />
                  </label>
                </div>
                <p class="mt-4 text-[11px] text-slate-400 leading-relaxed">
                  권장 사이즈: 512x512px<br/>지원 형식: PNG, JPG, GIF
                </p>
              </div>

              <div class="md:col-span-9 space-y-6">
                <div>
                  <label class="block text-sm font-bold text-slate-700 mb-2" for="ws-name">워크스페이스 이름</label>
                  <input 
                    v-model="form.name" 
                    id="ws-name" 
                    type="text" 
                    class="w-full px-4 py-3 rounded-xl border border-slate-200 text-slate-800 focus:outline-none focus:ring-2 focus:ring-indigo-500/10 focus:border-indigo-500 transition-all placeholder:text-slate-400"
                    placeholder="워크스페이스 이름을 입력하세요." 
                  />
                  <p class="mt-2 text-xs text-slate-400">워크스페이스를 식별하는 공식 이름입니다.</p>
                </div>

                <div>
                  <label class="block text-sm font-bold text-slate-700 mb-2" for="ws-desc">워크스페이스 설명</label>
                  <textarea 
                    v-model="form.description" 
                    id="ws-desc" 
                    class="w-full px-4 py-3 rounded-xl border border-slate-200 text-slate-800 focus:outline-none focus:ring-2 focus:ring-indigo-500/10 focus:border-indigo-500 transition-all h-32 resize-none placeholder:text-slate-400"
                    placeholder="워크스페이스에 대한 설명을 입력하세요."
                  ></textarea>
                  <p class="mt-2 text-xs text-slate-400">학습자들이 워크스페이스 대시보드에서 확인할 수 있는 설명입니다.</p>
                </div>
              </div>
            </div>
          </div>
        </section>

        <section class="bg-[#FEF2F2]/50 rounded-2xl border border-red-100 overflow-hidden">
          <div class="p-8">
            <div class="flex flex-col md:flex-row md:items-center justify-between gap-6">
              <div class="flex-1">
                <h3 class="text-lg font-bold text-red-600 mb-2 flex items-center gap-2">
                  <span class="material-symbols-outlined text-red-500">warning</span>
                  워크스페이스 삭제
                </h3>
                <p class="text-sm text-slate-600 leading-relaxed max-w-2xl">
                  워크스페이스를 삭제하면 모든 데이터(학습 이력, 참여자 정보, 업로드된 콘텐츠 등)가 복구 불가능하게 삭제됩니다. 이 작업은 되돌릴 수 없으므로 신중하게 진행해 주세요.
                </p>
              </div>
              <div class="shrink-0">
                <button 
                  @click="handleDelete"
                  class="px-6 py-3 bg-[#EF4444] text-white rounded-xl text-sm font-bold hover:bg-red-700 transition-all shadow-sm shadow-red-100"
                >
                  워크스페이스 삭제
                </button>
              </div>
            </div>
          </div>
        </section>
      </div>
    </div>
  </main>
</template>

<style scoped>
/* Google Material Symbols CDN이 index.html에 포함되어 있어야 합니다. */
@import url('https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@20..48,100..700,0..1,-50..200');

/* 추가적인 미세 조정 */
.material-symbols-outlined {
  font-variation-settings: 'FILL' 0, 'wght' 400, 'GRAD' 0, 'opsz' 24;
}
</style>