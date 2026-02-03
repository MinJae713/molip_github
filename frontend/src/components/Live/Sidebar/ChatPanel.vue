<template>
    <div class="flex flex-col h-full bg-white">
        <!-- 메시지 목록 -->
        <div class="flex-1 overflow-y-auto p-4 space-y-4 custom-scrollbar">
            <div v-for="msg in messages" :key="msg.id" class="flex flex-col gap-1">
                <div class="flex items-center gap-2">
                    <span class="text-xs font-bold text-slate-700">{{ msg.sender }}</span>
                    <span class="text-[10px] text-slate-400 font-medium">{{ msg.time }}</span>
                </div>
                <div class="bg-slate-50 p-3 rounded-2xl rounded-tl-none border border-slate-100 max-w-[90%]">
                    <p class="text-sm text-slate-600 leading-relaxed">{{ msg.text }}</p>
                </div>
            </div>
        </div>

        <!-- 입력창 -->
        <div class="p-4 border-t border-slate-50">
            <div class="relative">
                <input v-model="newMessage" type="text" placeholder="메시지를 입력하세요..."
                    class="w-full bg-slate-50 border border-slate-100 rounded-2xl py-3 px-4 pr-12 text-sm focus:outline-none focus:border-indigo-200 transition-all font-medium" />
                <button
                    class="absolute right-2 top-1.5 size-8 bg-indigo-600 text-white rounded-xl flex items-center justify-center hover:bg-indigo-700 transition-colors">
                    <span class="material-symbols-outlined text-lg">send</span>
                </button>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';

const messages = ref([
    { id: 1, sender: '이영희', text: '오늘 강의 정말 유익하네요!', time: '10:25' },
    { id: 2, sender: '김민수', text: '혹시 실습 예제 파일은 어디서 받나요?', time: '10:27' },
]);
const newMessage = ref('');
</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar {
    width: 4px;
}

.custom-scrollbar::-webkit-scrollbar-track {
    background: transparent;
}

.custom-scrollbar::-webkit-scrollbar-thumb {
    background: #f1f5f9;
    border-radius: 10px;
}
</style>
