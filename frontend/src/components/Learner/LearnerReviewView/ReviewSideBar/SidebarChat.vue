<template>
    <div class="h-[280px] flex-none border-t border-slate-200 bg-slate-50 flex flex-col">
        <div class="px-4 py-2 border-b border-slate-200 flex items-center justify-between bg-white flex-none">
            <div class="flex items-center gap-2">
                <span class="material-symbols-outlined text-indigo-600 text-[18px]">forum</span>
                <h3 class="font-bold text-slate-800 text-xs">AI Q&A</h3>
            </div>
            <span class="text-[9px] font-medium text-slate-500 bg-slate-100 px-2 py-0.5 rounded-full">STT 기반</span>
        </div>
        <div class="flex-1 overflow-y-auto p-4 space-y-4 custom-scrollbar">
            <!-- 질문 -->
            <div class="flex justify-end">
                <div
                    class="bg-indigo-600 text-white px-3 py-2 rounded-2xl rounded-tr-sm text-[11px] max-w-[85%] shadow-soft">
                    환각 현상이 발생하는 구체적인 이유가 뭐야?
                </div>
            </div>
            <!-- 답변 -->
            <div class="flex items-start gap-2">
                <div class="size-7 rounded-lg bg-indigo-600 flex-none flex items-center justify-center text-white">
                    <span class="material-symbols-outlined text-[14px]">smart_toy</span>
                </div>
                <div
                    class="bg-white border border-slate-200 text-slate-700 px-3 py-2 rounded-2xl rounded-tl-sm text-[11px] shadow-sm max-w-[90%]">
                    <p class="leading-relaxed text-slate-600">
                        환각(Hallucination)은 AI가 확률적으로 가장 그럴듯한 답변을 생성하는 과정에서 학습되지 않은 정보도 그럴듯하게 연결하여 발생합니다.
                    </p>
                </div>
            </div>
        </div>
        <!-- 입력창 -->
        <div class="p-3 bg-white border-t border-slate-100 flex-none">
            <div class="relative">
                <input
                    class="w-full pl-4 pr-10 py-2 bg-slate-50 border border-slate-200 rounded-xl text-[11px] focus:outline-none focus:border-indigo-500 transition-all placeholder:text-slate-400"
                    placeholder="강의 내용에 대해 질문하세요..." type="text" />
                <button
                    class="absolute right-1 top-1 bottom-1 aspect-square bg-indigo-600 text-white rounded-lg flex items-center justify-center hover:bg-indigo-700 transition-colors">
                    <span class="material-symbols-outlined text-[16px]">send</span>
                </button>
            </div>
        </div>
    </div>
</template>