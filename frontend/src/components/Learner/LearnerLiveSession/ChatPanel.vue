<template>
    <div class="flex-1 bg-white border border-slate-200 rounded-2xl shadow-sm flex flex-col overflow-hidden min-h-0">
        <!-- Tabs -->
        <div class="flex border-b border-slate-100 bg-slate-50/80 shrink-0">
            <button @click="$emit('update:activeTab', 'chat')"
                :class="['flex-1 py-3 text-xs font-bold transition-colors border-b-2',
                    activeTab === 'chat' ? 'text-indigo-600 border-indigo-600 bg-white' : 'text-slate-400 hover:text-slate-600 border-transparent']">
                채팅
            </button>
            <button @click="$emit('update:activeTab', 'qa')"
                :class="['flex-1 py-3 text-xs font-bold transition-colors border-b-2 flex items-center justify-center gap-1.5',
                    activeTab === 'qa' ? 'text-indigo-600 border-indigo-600 bg-white' : 'text-slate-400 hover:text-slate-600 border-transparent']">
                질문하기
                <span v-if="activeTab === 'qa'" class="size-1.5 rounded-full bg-pink-500 animate-pulse"></span>
            </button>
        </div>

        <!-- Messages Area -->
        <div class="flex-1 overflow-y-auto p-4 space-y-4 bg-slate-50/30">
            <!-- Chat Tab -->
            <div v-if="activeTab === 'chat'" class="flex flex-col gap-3">
                <div v-for="msg in chatMessages" :key="msg.id" class="flex gap-2">
                    <div class="size-7 rounded-full bg-gradient-to-br from-indigo-400 to-cyan-400 shrink-0">
                    </div>
                    <div class="flex-1">
                        <div class="flex items-center gap-2 mb-1">
                            <span class="text-xs font-bold text-slate-800">{{ msg.name }}</span>
                            <span class="text-[10px] text-slate-400">{{ msg.time }}</span>
                        </div>
                        <p class="text-xs text-slate-600">{{ msg.message }}</p>
                    </div>
                </div>
            </div>

            <!-- Q&A Tab -->
            <div v-else class="flex flex-col gap-3">
                <div v-for="qa in qaMessages" :key="qa.id" :class="['bg-white border border-slate-200 p-3 rounded-xl shadow-sm',
                    qa.status === 'new' && 'border-l-4 border-l-pink-500']">
                    <div class="flex items-center gap-2 mb-2">
                        <span :class="['text-[10px] font-bold px-1.5 py-0.5 rounded',
                            qa.status === 'answered' ? 'bg-amber-100 text-amber-700' : 'bg-pink-100 text-pink-600']">
                            {{ qa.status === 'answered' ? '답변완료' : '새 질문' }}
                        </span>
                        <span class="text-[10px] text-slate-400">{{ qa.time }}</span>
                    </div>
                    <p class="text-xs text-slate-800 font-medium mb-2">Q: {{ qa.question }}</p>
                    <div v-if="qa.answer" class="bg-slate-50 rounded-lg p-2.5 border-l-2 border-indigo-600">
                        <p class="text-[11px] text-slate-600 leading-relaxed">
                            <span class="font-bold text-indigo-600 mr-1">A:</span> {{ qa.answer }}
                        </p>
                    </div>
                    <p v-else class="text-[10px] text-slate-400 mt-2">답변 대기 중...</p>
                </div>
            </div>
        </div>

        <!-- Input Area -->
        <div class="p-3 bg-white border-t border-slate-100 shrink-0">
            <div class="relative flex items-center">
                <input :value="modelValue"
                    @input="$emit('update:modelValue', ($event.target as HTMLInputElement).value)"
                    @keyup.enter="$emit('send-message')"
                    class="w-full bg-slate-50 border border-slate-200 rounded-full pl-4 pr-11 py-2.5 text-xs focus:ring-2 focus:ring-indigo-200 focus:border-indigo-600 transition-all outline-none text-slate-800 placeholder:text-slate-400"
                    :placeholder="activeTab === 'chat' ? '채팅 메시지 입력...' : '교수님께 질문하기...'" type="text" />
                <button @click="$emit('send-message')"
                    class="absolute right-1.5 p-1.5 bg-indigo-600 text-white rounded-full hover:bg-indigo-700 hover:shadow-lg hover:-translate-y-0.5 transition-all flex items-center justify-center group">
                    <span
                        class="material-symbols-outlined text-[16px] group-hover:rotate-12 transition-transform">send</span>
                </button>
            </div>
            <div v-if="activeTab === 'qa'" class="flex items-center gap-2 mt-2 px-1">
                <label class="flex items-center gap-1.5 cursor-pointer">
                    <input :checked="isAnonymous"
                        @change="$emit('update:isAnonymous', ($event.target as HTMLInputElement).checked)"
                        class="size-3 rounded border-slate-300 text-indigo-600 focus:ring-indigo-200" type="checkbox" />
                    <span class="text-[10px] text-slate-500">익명으로 질문하기</span>
                </label>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
defineProps<{
    activeTab: 'chat' | 'qa';
    chatMessages: any[];
    qaMessages: any[];
    modelValue: string;
    isAnonymous: boolean;
}>();

defineEmits<{
    (e: 'update:activeTab', value: 'chat' | 'qa'): void;
    (e: 'update:modelValue', value: string): void;
    (e: 'update:isAnonymous', value: boolean): void;
    (e: 'send-message'): void;
}>();
</script>
