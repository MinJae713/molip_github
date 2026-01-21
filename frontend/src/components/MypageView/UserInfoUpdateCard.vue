<template>
    <section class="glass-panel p-5">
        <div class="flex flex-col md:flex-row gap-8 items-start">
            <div class="flex flex-col items-center gap-4 w-full md:w-auto">
                <div class="relative group cursor-pointer" @click="triggerFileInput">
                    <div
                        class="size-28 rounded-full bg-gradient-to-tr from-indigo-500 via-purple-500 to-magenta p-1 shadow-md transition-transform active:scale-95">
                        <img :src="previewImage || props.user.profileImage" alt="Profile"
                            class="rounded-full w-full h-full object-cover border-4 border-white bg-white" />
                    </div>
                    <div
                        class="absolute inset-0 bg-black/40 rounded-full flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity">
                        <span class="material-symbols-outlined text-white text-3xl">photo_camera</span>
                    </div>
                </div>

                <button @click="triggerFileInput"
                    class="text-sm text-indigo-600 font-bold hover:text-indigo-800 transition-colors">
                    프로필 사진 변경
                </button>

                <input type="file" ref="fileInput" class="hidden" accept="image/*" @change="handleFileChange" />
            </div>

            <div class="flex-1 w-full grid grid-cols-1 gap-6">
                <div class="space-y-2">
                    <label class="block text-sm font-medium text-slate-700">이름</label>
                    <input :value="props.user.name"
                        class="block w-full rounded-lg border-slate-300 bg-slate-100 text-slate-500 sm:text-sm p-2.5 border cursor-not-allowed"
                        readonly />
                </div>

                <div class="space-y-2">
                    <label class="block text-sm font-medium text-slate-700">이메일 계정</label>
                    <div class="relative">
                        <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                            <span class="material-symbols-outlined text-slate-400 text-lg">mail</span>
                        </div>
                        <input :value="props.user.email"
                            class="block w-full pl-10 rounded-lg border-slate-300 bg-slate-100 text-slate-500 sm:text-sm p-2.5 border cursor-not-allowed"
                            readonly />
                    </div>
                    <p class="text-[11px] text-slate-400 mt-1"> 이름과 이메일 변경은 관리자에게 문의하세요.</p>
                    <p class="text-[11px] text-slate-400 mt-1"> 프로필 사진은 변경 즉시 시스템에 적용됩니다.</p>
                </div>
            </div>
        </div>
    </section>
</template>

<script setup lang="ts">
import { ref } from 'vue';

import type { User } from '@/types';

const props = defineProps<{ user: User }>();

// 사진이 변경되었음을 부모에게 즉시 알리는 emit
const emit = defineEmits<{
    (e: 'update-image', file: File): void;
}>();

const fileInput = ref<HTMLInputElement | null>(null);
const previewImage = ref<string | null>(null);

const triggerFileInput = () => fileInput.value?.click();

const handleFileChange = (event: Event) => {
    const target = event.target as HTMLInputElement;
    const file = target.files?.[0];

    if (file) {
        const reader = new FileReader();
        reader.onload = (e) => {
            previewImage.value = e.target?.result as string;
        };
        reader.readAsDataURL(file);
        emit('update-image', file);
        target.value = '';
    }
};
</script>