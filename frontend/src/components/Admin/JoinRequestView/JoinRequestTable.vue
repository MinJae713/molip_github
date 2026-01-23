<template>
  <div class="overflow-x-auto">
    <table class="w-full text-left border-collapse">
      <thead>
        <tr class="bg-slate-50/50 border-b border-slate-100">
          <th class="p-4 w-12 text-center">
            <input type="checkbox" class="size-4 rounded border-slate-300 text-indigo-600 focus:ring-indigo-500 cursor-pointer">
          </th>
          <th v-for="col in columns" :key="col.key" 
              :class="['p-4 text-xs font-bold text-slate-500 uppercase tracking-wider', col.align === 'center' ? 'text-center' : '']">
            {{ col.label }}
          </th>
        </tr>
      </thead>
      
      <tbody class="divide-y divide-slate-50">
        <tr v-for="req in requests" :key="req.id" class="hover:bg-slate-50/30 transition-colors group">
          <td class="p-4 text-center">
            <input type="checkbox" class="size-4 rounded border-slate-300 text-indigo-600 focus:ring-indigo-500 cursor-pointer">
          </td>
          
          <td v-for="col in columns" :key="col.key" 
              :class="['p-4', col.align === 'center' ? 'text-center' : '']">
            
            <div v-if="col.key === 'info'" class="flex items-center gap-3 text-left">
              <img :src="req.user?.profileImage || 'https://ui-avatars.com/api/?name=User'" 
                   class="size-10 rounded-full border border-slate-100 object-cover shadow-sm" alt="profile" />
              <div>
                <div class="text-sm font-bold text-slate-900">{{ req.user?.name || '정보 없음' }}</div>
                <div class="text-xs text-slate-500">{{ req.user?.email || '-' }}</div>
              </div>
            </div>

            <span v-else-if="['applied_at', 'approved_at', 'rejected_at'].includes(col.key)" class="text-sm text-slate-600">
              {{ req[col.key] || '-' }}
            </span>

            <span v-else-if="col.key === 'status'" 
                  :class="['px-2.5 py-1 rounded-full text-[11px] font-bold inline-block leading-none', 
                           statusStyles[req.status]?.bgClass ?? 'bg-slate-100 text-slate-600']">
              {{ statusStyles[req.status]?.label ?? '미정' }}
            </span>

            <p v-else-if="col.key === 'reject_reason'" class="text-xs text-slate-500 max-w-50 truncate mx-auto" :title="req.reject_reason">
              {{ req.reject_reason || '사내 보안 정책상 사유로 인한 반려' }}
            </p>

            <div v-else-if="col.key === 'manage'" class="flex justify-center gap-2">
              <template v-if="req.status === 0">
                <button @click="$emit('approve', req.id)" class="px-3 py-1.5 bg-indigo-600 text-white text-[12px] font-bold rounded-lg hover:bg-indigo-700 active:scale-95 shadow-sm transition-all">승인</button>
                <button @click="$emit('reject', req.id)" class="px-3 py-1.5 border border-slate-200 text-slate-600 text-[12px] font-bold rounded-lg hover:bg-slate-50 active:scale-95">반려</button>
              </template>
              <button v-else-if="req.status === 1" @click="$emit('view-profile', req.id)" class="text-xs font-bold text-indigo-600 hover:underline">프로필 보기</button>
              <span v-else class="text-xs font-medium text-slate-400">처리 완료</span>
            </div>
          </td>
        </tr>
      </tbody>
    </table>

    <div v-if="requests.length === 0" class="p-24 text-center flex flex-col items-center bg-white">
      <span class="material-symbols-outlined text-slate-200 text-6xl mb-4">search_off</span>
      <p class="text-slate-900 font-bold text-lg mb-1">신청 내역이 없습니다</p>
      <p class="text-slate-400 text-sm">해당 조건에 맞는 신청자가 없습니다.</p>
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  requests: any[];
  columns: any[];
  statusStyles: Record<number, { label: string; bgClass: string }>;
}>();

defineEmits(['approve', 'reject', 'view-profile']);
</script>