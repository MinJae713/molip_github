<template>
  <table class="w-full text-left border-collapse">
    <thead>
      <tr class="bg-slate-50/50 border-b border-slate-100">
          <th class="p-4 w-12 text-center text-xs font-bold text-slate-500 uppercase tracking-wider">
            No.
          </th>
          <th v-for="col in columns" :key="col.key" 
              :class="['p-4 text-xs font-bold text-slate-500 uppercase tracking-wider', col.align === 'center' ? 'text-center' : '']">
            {{ col.label }}
          </th>
      </tr>
    </thead>
    <tbody class="divide-y divide-slate-50">
      <tr v-if="requests.length === 0">
        <td :colspan="(columns?.length || 4) + 1" class="p-8 text-center text-slate-400 text-sm">
          해당 조건에 맞는 신청자가 없습니다.
        </td>
      </tr>
      <tr v-for="(req, index) in requests" :key="req.workspaceRegisterId" class="hover:bg-slate-50/30 transition-colors group">
          <td class="p-4 text-center text-sm text-slate-500">
            {{ index + 1 }}
          </td>
          
          <td v-for="col in columns" :key="col.key" :class="['p-4 border-b border-slate-50', col.align === 'center' ? 'text-center' : '']">
            <!-- 사용자 정보 (신청자) -->
            <template v-if="col.key === 'info'">
              <div class="flex items-center gap-3">
                <div class="size-9 rounded-full bg-slate-100 flex items-center justify-center border border-slate-200 overflow-hidden">
                   <img v-if="req.profileImageUrl" :src="req.profileImageUrl" class="size-full object-cover">
                   <span v-else class="material-symbols-outlined text-slate-400 text-lg">person</span>
                </div>
                <div>
                  <div class="text-sm font-bold text-slate-900">{{ req.name }}</div>
                  <div class="text-xs text-slate-400">{{ req.email }}</div>
                </div>
              </div>
            </template>

            <!-- 신청일 -->
            <template v-else-if="col.key === 'applied_at'">
              <span class="text-sm text-slate-500">{{ formatDate(req.createdAt) }}</span>
            </template>
            
            <!-- 승인일 -->
            <template v-else-if="col.key === 'approved_at'">
              <span class="text-sm text-slate-500">{{ formatDate(req.approvedAt) }}</span>
            </template>

            <!-- 반려일 -->
            <template v-else-if="col.key === 'rejected_at'">
              <span class="text-sm text-slate-500">{{ formatDate(req.updatedAt) }}</span>
            </template>

            <!-- 반려 사유 -->
             <template v-else-if="col.key === 'reject_reason'">
              <span class="text-sm text-slate-500">{{ req.rejectReason || '-' }}</span> 
            </template>

            <!-- 상태 -->
            <template v-else-if="col.key === 'status'">
               <span :class="['px-2.5 py-1 rounded-md text-[10px] font-bold inline-block', statusStyles?.[req.status]?.bgClass || 'bg-slate-100 text-slate-500']">
                  {{ statusStyles?.[req.status]?.label || req.status }}
               </span>
            </template>

            <!-- 관리 -->
            <template v-else-if="col.key === 'manage'">
              <div v-if="req.status === 'PENDING'" class="flex items-center justify-center gap-2">
                <button 
                  @click="$emit('approve', req.workspaceRegisterId)" 
                  class="px-3 py-1.5 rounded-lg bg-emerald-50 text-emerald-600 hover:bg-emerald-100 transition-colors text-xs font-bold"
                >
                  승인
                </button>
                <button 
                  @click="$emit('reject', req.workspaceRegisterId)" 
                  class="px-3 py-1.5 rounded-lg bg-rose-50 text-rose-600 hover:bg-rose-100 transition-colors text-xs font-bold"
                >
                  반려
                </button>
              </div>
              <span v-else class="text-xs text-slate-400">-</span>
            </template>
          </td>
      </tr>
    </tbody>
  </table>
</template>

<script setup lang="ts">
import type { JoinRequest } from '@/stores/workspaceAdmin';

interface Props {
  requests: JoinRequest[];
  columns?: any[];
  statusStyles?: Record<string, { label: string; bgClass: string }>;
}

const props = defineProps<Props>();

defineEmits<{
  (e: 'approve', id: number): void;
  (e: 'reject', id: number): void;
}>();

// 날짜 포맷팅 (YYYY-MM-DD HH:mm:ss)
const formatDate = (dateStr?: string) => {
  if (!dateStr) return '-';
  const date = new Date(dateStr);
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  const hours = String(date.getHours()).padStart(2, '0');
  const minutes = String(date.getMinutes()).padStart(2, '0');
  
  return `${year}-${month}-${day} ${hours}:${minutes}`;
};
</script>