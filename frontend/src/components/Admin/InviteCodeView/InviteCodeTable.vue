<template>
  <table class="w-full text-left border-collapse">
    <thead>
      <tr class="bg-slate-50/50 border-b border-slate-100">
        <th class="p-4 w-12 text-center text-xs font-bold text-slate-400 uppercase tracking-wider">No.</th>
        <th class="p-4 text-xs font-bold text-slate-400 uppercase tracking-wider w-[30%]">코드명 / 식별값</th>
        <th class="p-4 text-xs font-bold text-slate-400 uppercase tracking-wider w-[20%]">발급일</th>
        <th class="p-4 text-xs font-bold text-slate-400 uppercase tracking-wider w-[20%]">만료일</th>
        <th class="p-4 text-xs font-bold text-slate-400 uppercase tracking-wider text-center w-[10%]">상태</th>
        <th class="p-4 text-xs font-bold text-slate-400 uppercase tracking-wider text-center w-[10%]">관리</th>
      </tr>
    </thead>
    <tbody class="divide-y divide-slate-50">
      <tr v-if="codes.length === 0">
        <td colspan="6" class="p-12 text-center text-slate-400">
            <div class="flex flex-col items-center justify-center">
                <span class="material-symbols-outlined text-5xl text-slate-200 mb-3">folder_off</span>
                <p class="text-sm font-medium">발급된 초대코드가 없습니다.</p>
            </div>
        </td>
      </tr>
      <tr v-else v-for="(item, index) in codes" :key="item.joinCodeId" class="hover:bg-slate-50/30 transition-colors group">
        <!-- 인덱스 -->
        <td class="p-4 text-center text-sm text-slate-500">
          {{ index + 1 }}
        </td>

        <!-- 코드명 / 식별값 -->
        <td class="p-4">
          <div class="flex items-center gap-3">
            <div :class="['size-9 rounded-lg flex items-center justify-center', statusInfo[item.status].bg]">
              <span class="material-symbols-outlined text-lg" :class="statusInfo[item.status].iconColor">key</span>
            </div>
            <div class="flex flex-col">
              <span class="text-sm font-black text-slate-900 leading-tight">{{ item.joinCodeName }}</span>
              <button 
                @click.stop="copyCode(item.joinCodeId, item.identifyValue)" 
                class="group/copy flex items-center gap-1 text-[11px] font-bold text-slate-400 hover:text-indigo-500 transition-colors text-left"
                title="클릭하여 복사"
              >
                  <span>{{ item.identifyValue }}</span>
                  <span class="material-symbols-outlined text-[12px] opacity-0 group-hover/copy:opacity-100 transition-opacity" :class="{ 'opacity-100 text-emerald-500': copiedId === item.joinCodeId }">
                      {{ copiedId === item.joinCodeId ? 'check' : 'content_copy' }}
                  </span>
              </button>
            </div>
          </div>
        </td>

        <!-- 발급일 -->
        <td class="p-4 text-sm text-slate-500 font-medium">{{ formatDate(item.createdAt) }}</td>

        <!-- 만료일 -->
        <td class="p-4 text-sm font-bold" :class="item.status === '만료임박' ? 'text-rose-500' : 'text-slate-500'">
          {{ formatDate(item.endedDate, true) }}
        </td>

        <!-- 상태 -->
        <td class="p-4 text-center">
          <span :class="['px-2.5 py-1 rounded-md text-[10px] font-bold inline-block', statusInfo[item.status].badge]">
            {{ item.status }}
          </span>
        </td>

        <!-- 관리 버튼 -->
        <td class="p-4 text-center">
          <button 
            @click="$emit('delete', item.joinCodeId, item.joinCodeName)" 
            class="material-symbols-outlined text-slate-300 hover:text-rose-500 transition-colors invisible group-hover:visible">
            delete
          </button>
        </td>
      </tr>
    </tbody>
  </table>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import type { CodeItem } from '@/types'; // 공용 타입 정의

const props = defineProps<{
  codes: CodeItem[];
}>();

const emit = defineEmits<{
  (e: 'delete', joinCodeId: number, joinCodeName: string): void;
}>();

const copiedId = ref<number | null>(null);

const copyCode = async (id: number, code: string) => {
  try {
    await navigator.clipboard.writeText(code);
    copiedId.value = id;
    setTimeout(() => {
        if (copiedId.value === id) copiedId.value = null;
    }, 2000);
  } catch (err) {
    console.error('Copy failed', err);
  }
};

// 날짜 포맷팅 (YYYY-MM-DD HH:mm:ss)
const formatDate = (dateStr?: string, forceEndOfDay: boolean = false) => {
  if (!dateStr) return '-';
  const date = new Date(dateStr);
  
  // 만료일의 경우 23:59:59로 표시
  if (forceEndOfDay) {
    date.setHours(23, 59, 59);
  }

  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  const hours = String(date.getHours()).padStart(2, '0');
  const minutes = String(date.getMinutes()).padStart(2, '0');
  const seconds = String(date.getSeconds()).padStart(2, '0');
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
};

// 상태별 스타일
const statusInfo: Record<CodeItem['status'], { bg: string; iconColor: string; badge: string }> = reactive({
  '만료임박': { bg: 'bg-amber-50', iconColor: 'text-amber-500', badge: 'bg-amber-500 text-white' },
  '활성': { bg: 'bg-indigo-50', iconColor: 'text-indigo-500', badge: 'bg-emerald-50 text-emerald-600 border border-emerald-100' },
  '만료': { bg: 'bg-slate-50', iconColor: 'text-slate-300', badge: 'bg-slate-100 text-slate-400' }
});
</script>
