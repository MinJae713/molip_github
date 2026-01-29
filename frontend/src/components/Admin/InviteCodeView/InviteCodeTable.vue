<template>
  <table class="w-full text-left border-collapse">
    <thead>
      <tr class="bg-slate-50/50 border-b border-slate-100">
        <th class="p-4 w-12 text-center">
          <input type="checkbox" class="size-4 rounded border-slate-300 text-indigo-600 focus:ring-indigo-500 cursor-pointer">
        </th>
        <th class="p-4 text-xs font-bold text-slate-400 uppercase tracking-wider">코드명 / 식별값</th>
        <th class="p-4 text-xs font-bold text-slate-400 uppercase tracking-wider">발급일</th>
        <th class="p-4 text-xs font-bold text-slate-400 uppercase tracking-wider">만료일</th>
        <th class="p-4 text-xs font-bold text-slate-400 uppercase tracking-wider">상태</th>
        <th class="p-4 text-xs font-bold text-slate-400 uppercase tracking-wider text-right">관리</th>
      </tr>
    </thead>
    <tbody class="divide-y divide-slate-50">
      <tr v-for="item in codes" :key="item.id" class="hover:bg-slate-50/30 transition-colors group">
        <td class="p-4 text-center">
          <input type="checkbox" :value="item.id" v-model="selectedProxy" class="size-4 rounded border-slate-300 text-indigo-600 focus:ring-indigo-500 cursor-pointer">
        </td>
        <td class="p-4">
          <div class="flex items-center gap-3">
            <div :class="['size-9 rounded-lg flex items-center justify-center', statusInfo[item.status].bg]">
              <span class="material-symbols-outlined text-lg" :class="statusInfo[item.status].iconColor">key</span>
            </div>
            <div class="flex flex-col">
              <span class="text-sm font-black text-slate-900 leading-tight">{{ item.name }}</span>
              <span class="text-[11px] font-bold text-slate-400">{{ item.code }}</span>
            </div>
          </div>
        </td>
        <td class="p-4 text-sm text-slate-500 font-medium">{{ item.created }}</td>
        <td class="p-4 text-sm font-bold" :class="item.status === '만료임박' ? 'text-rose-500' : 'text-slate-500'">
          {{ item.expiry }}
        </td>
        <td class="p-4">
          <span :class="['px-2.5 py-1 rounded-md text-[10px] font-bold inline-block', statusInfo[item.status].badge]">
            {{ item.status }}
          </span>
        </td>
        <td class="p-4 text-right">
          <button v-if="item.status === '만료'" class="text-xs font-bold text-indigo-600 hover:underline">재발급</button>
          <button v-else @click="$emit('delete', item.name)" class="material-symbols-outlined text-slate-300 hover:text-rose-500 transition-colors invisible group-hover:visible">delete</button>
        </td>
      </tr>
    </tbody>
  </table>
</template>

<script setup lang="ts">
import { computed } from 'vue';

const props = defineProps<{ codes: any[], selectedIds: number[] }>();
const emit = defineEmits(['update:selectedIds', 'delete']);

const selectedProxy = computed({
  get: () => props.selectedIds,
  set: (val) => emit('update:selectedIds', val)
});

const statusInfo: any = {
  '만료임박': { bg: 'bg-amber-50', iconColor: 'text-amber-500', badge: 'bg-amber-500 text-white' },
  '활성': { bg: 'bg-indigo-50', iconColor: 'text-indigo-500', badge: 'bg-emerald-50 text-emerald-600 border border-emerald-100' },
  '만료': { bg: 'bg-slate-50', iconColor: 'text-slate-300', badge: 'bg-slate-100 text-slate-400' }
};
</script>