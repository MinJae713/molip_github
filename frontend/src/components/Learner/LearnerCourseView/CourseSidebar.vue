<template>
    <div class="space-y-6">
        <!-- My Learning Stats -->
        <div class="bg-white border border-slate-200 rounded-xl p-5 shadow-sm">
            <h3 class="font-bold text-slate-900 mb-4 flex items-center gap-2">
                <span class="material-symbols-outlined text-indigo-600">bar_chart</span>
                나의 학습 통계
            </h3>
            <div class="space-y-4">
                <div class="flex justify-between items-center text-sm">
                    <span class="text-slate-500">진도율</span>
                    <span class="font-bold text-indigo-600">40%</span>
                </div>
                <div class="w-full bg-slate-100 rounded-full h-2">
                    <div class="bg-indigo-600 h-2 rounded-full" style="width: 40%"></div>
                </div>
                <div class="grid grid-cols-2 gap-2 mt-2">
                    <div class="bg-slate-50 p-3 rounded-lg text-center">
                        <div class="text-xs text-slate-500 mb-1">학습 시간</div>
                        <div class="font-bold text-slate-900">12h 30m</div>
                    </div>
                    <div class="bg-slate-50 p-3 rounded-lg text-center">
                        <div class="text-xs text-slate-500 mb-1">최근 학습</div>
                        <div class="font-bold text-slate-900">2일 전</div>
                    </div>
                </div>
            </div>
        </div>

        <!-- Instructor Info -->
        <div class="bg-white border border-slate-200 rounded-xl p-5 shadow-sm">
            <h3 class="font-bold text-slate-900 mb-4 flex items-center gap-2">
                <span class="material-symbols-outlined text-indigo-600">face</span>
                담당 교수
            </h3>
            <div class="flex items-start gap-4">
                <img src="https://i.pravatar.cc/150?u=instructor1" alt="Instructor"
                    class="size-12 rounded-full object-cover border border-slate-200">
                <div>
                    <h4 class="font-bold text-slate-900">박민우 박사</h4>
                    <p class="text-xs text-indigo-600 font-medium mb-1">AI Ethics Lab 소장</p>
                    <p class="text-xs text-slate-500 leading-snug">현재 ERD에 해당 내용 없음! 컴포넌트 제거? 추가?</p>
                </div>
            </div>
            <button
                class="w-full mt-4 py-2 border border-slate-200 rounded-lg text-sm font-bold text-slate-600 hover:bg-slate-50 transition-colors flex items-center justify-center gap-2">
                <span class="material-symbols-outlined text-[18px]">mail</span>
                문의하기
            </button>
        </div>

        <!-- Completion Criteria -->
        <div class="bg-white border border-slate-200 rounded-xl p-5 shadow-sm">
            <h3 class="font-bold text-slate-900 mb-4 flex items-center gap-2">
                <span class="material-symbols-outlined text-indigo-600">verified</span>
                수료 기준
            </h3>
            <ul class="space-y-3">
                <li class="flex items-start gap-2 text-sm text-slate-600">
                    <span class="material-symbols-outlined text-emerald-500 text-[18px] mt-0.5">check_circle</span>
                    <span>진도율 80% 이상 달성</span>
                </li>
                <li class="flex items-start gap-2 text-sm text-slate-600">
                    <span class="material-symbols-outlined text-emerald-500 text-[18px] mt-0.5">check_circle</span>
                    <span>최종 과제 70점 이상 (Pass)</span>
                </li>
                <li class="flex items-start gap-2 text-sm text-slate-600">
                    <span
                        class="material-symbols-outlined text-slate-300 text-[18px] mt-0.5">radio_button_unchecked</span>
                    <span>실시간 강의 2회 이상 참여</span>
                </li>
            </ul>
        </div>
    </div>
</template>
<script setup lang="ts">
// Logic can be added later
</script>
