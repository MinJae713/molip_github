<template>
    <div
        class="flex items-center justify-between p-3 rounded-xl border border-slate-100 bg-slate-50/50 hover:bg-white hover:border-indigo-100 transition-all group">
        <div class="flex items-center gap-3">
            <div
                class="size-10 bg-white border border-slate-200 rounded-lg flex items-center justify-center text-red-500 shadow-sm">
                <span class="material-symbols-outlined">{{ fileIcon }}</span>
            </div>
            <div>
                <div class="text-sm font-bold text-slate-700">{{ file.fileName }}</div>
                <div class="text-[11px] text-slate-400">{{ file.fileSize }}</div>
            </div>
        </div>
        <button
            class="px-3 py-1.5 bg-white border border-slate-200 rounded-lg text-xs font-bold text-slate-600 hover:bg-slate-50 hover:text-indigo-600 hover:border-indigo-200 active:scale-95 transition-all flex items-center gap-1.5">
            <span class="material-symbols-outlined text-[16px]">download</span>
            다운로드
        </button>
    </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';

const props = defineProps<{
    file: {
        fileId: number | string;
        fileName: string;
        fileSize: string;
        fileUrl: string;
        fileType: string;
    }
}>();

const fileIcon = computed(() => {
    switch (props.file.fileType?.toLowerCase()) {
        case 'pdf': return 'picture_as_pdf';
        case 'pptx':
        case 'ppt': return 'present_to_all';
        case 'xlsx':
        case 'xls': return 'table_chart';
        case 'doc':
        case 'docx': return 'description';
        case 'txt': return 'terminal';
        default: return 'draft';
    }
});
</script>