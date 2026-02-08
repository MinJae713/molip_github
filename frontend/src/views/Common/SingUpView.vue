<template>
    <div class="bg-light-bg text-slate-800 min-h-screen flex flex-col font-body">
        <!-- 헤더 -->
        <header class="bg-white/80 backdrop-blur-md border-b border-slate-200 sticky top-0 z-40">
            <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
                <div class="flex items-center gap-2 text-indigo-brand">
                    <span class="material-symbols-outlined text-3xl">school</span>
                    <span class="text-xl font-bold font-display tracking-tight text-slate-900">EduVerse <span
                            class="text-indigo-brand font-light">Work</span></span>
                </div>
                <div class="flex items-center gap-4 text-sm font-medium">
                    <span class="hidden sm:inline text-slate-500">이미 계정이 있으신가요?</span>
                    <router-link :to="{ name: 'loginPage' }"
                        class="text-indigo-brand hover:text-indigo-dark transition-colors font-bold">로그인</router-link>
                </div>
            </div>
        </header>
        <!-- 메인 컨텐츠 -->
        <main
            class="flex-1 w-full max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 flex flex-col items-center justify-center">
            <div class="w-full max-w-2xl">
                <div class="text-center mb-6">
                    <h1 class="text-3xl font-bold text-slate-900 mb-2">회원가입</h1>
                    <p class="text-slate-500 text-base">
                        EduVerse Work의 멤버가 되어 AI 기반 교육 플랫폼을 경험하세요.
                    </p>
                </div>
                <div class="bg-white border border-slate-200 shadow-card rounded-xl p-8 sm:p-10">
                    <form @submit.prevent="handleSignup" class="space-y-5">
                        <!-- 이름 -->
                        <div>
                            <label class="block text-sm font-bold text-slate-700 mb-1.5" for="name">이름 <span
                                    class="text-red-500">*</span></label>
                            <input v-model="form.name"
                                class="block w-full rounded-lg border-slate-300 shadow-sm focus:border-indigo-brand focus:ring-indigo-brand sm:text-sm py-2.5 px-3 transition-colors placeholder:text-slate-400 text-slate-900"
                                id="name" placeholder="실명을 입력하세요" required type="text" />
                        </div>
                        <!-- 이메일 -->
                        <div>
                            <label class="block text-sm font-bold text-slate-700 mb-1.5" for="email">이메일 <span
                                    class="text-red-500">*</span></label>
                            <input v-model="form.email"
                                class="block w-full rounded-lg border-slate-300 shadow-sm focus:border-indigo-brand focus:ring-indigo-brand sm:text-sm py-2.5 px-3 transition-colors placeholder:text-slate-400 text-slate-900"
                                id="email" placeholder="example@company.com" required type="email" />
                        </div>
                        <!-- 비밀번호 -->
                        <div class="grid grid-cols-1 sm:grid-cols-2 gap-5">
                            <div>
                                <label class="block text-sm font-bold text-slate-700 mb-1.5" for="password">비밀번호 <span
                                        class="text-red-500">*</span></label>
                                <input v-model="form.password"
                                    class="block w-full rounded-lg border-slate-300 shadow-sm focus:border-indigo-brand focus:ring-indigo-brand sm:text-sm py-2.5 px-3 transition-colors placeholder:text-slate-400 text-slate-900"
                                    id="password" placeholder="8자 이상 영문, 숫자, 특수문자" required type="password" />
                            </div>
                            <div>
                                <label class="block text-sm font-bold text-slate-700 mb-1.5" for="confirm-password">비밀번호
                                    확인 <span class="text-red-500">*</span></label>
                                <input v-model="form.confirmPassword"
                                    class="block w-full rounded-lg border-slate-300 shadow-sm focus:border-indigo-brand focus:ring-indigo-brand sm:text-sm py-2.5 px-3 transition-colors placeholder:text-slate-400 text-slate-900"
                                    id="confirm-password" placeholder="비밀번호를 다시 입력하세요" required type="password" />
                            </div>
                        </div>
                        <!-- 나이 & 주소 -->
                        <div class="grid grid-cols-1 sm:grid-cols-3 gap-5">
                            <div class="sm:col-span-1">
                                <label class="block text-sm font-bold text-slate-700 mb-1.5" for="age">나이</label>
                                <input v-model="form.age"
                                    class="block w-full rounded-lg border-slate-300 shadow-sm focus:border-indigo-brand focus:ring-indigo-brand sm:text-sm py-2.5 px-3 transition-colors placeholder:text-slate-400 text-slate-900"
                                    id="age" placeholder="숫자만 입력" type="number" />
                            </div>
                            <div class="sm:col-span-2">
                                <label class="block text-sm font-bold text-slate-700 mb-1.5" for="address">주소</label>
                                <input v-model="form.address"
                                    class="block w-full rounded-lg border-slate-300 shadow-sm focus:border-indigo-brand focus:ring-indigo-brand sm:text-sm py-2.5 px-3 transition-colors placeholder:text-slate-400 text-slate-900"
                                    id="address" placeholder="시/구/동 입력" type="text" />
                            </div>
                        </div>
                        <!-- 성별 -->
                        <div>
                            <label class="block text-sm font-bold text-slate-700 mb-1.5">성별</label>
                            <div class="grid grid-cols-3 gap-3">
                                <label>
                                    <input v-model="form.gender" class="peer sr-only" name="gender" type="radio"
                                        value="남성" />
                                    <div
                                        class="flex items-center justify-center px-4 py-2 border border-slate-200 rounded-lg cursor-pointer hover:bg-slate-50 hover:border-indigo-200 transition-colors peer-checked:bg-indigo-50 peer-checked:border-indigo-brand peer-checked:text-indigo-brand text-slate-600 text-sm font-medium">
                                        남성</div>
                                </label>
                                <label>
                                    <input v-model="form.gender" class="peer sr-only" name="gender" type="radio"
                                        value="여성" />
                                    <div
                                        class="flex items-center justify-center px-4 py-2 border border-slate-200 rounded-lg cursor-pointer hover:bg-slate-50 hover:border-indigo-200 transition-colors peer-checked:bg-indigo-50 peer-checked:border-indigo-brand peer-checked:text-indigo-brand text-slate-600 text-sm font-medium">
                                        여성</div>
                                </label>
                                <label>
                                    <input v-model="form.gender" class="peer sr-only" name="gender" type="radio"
                                        value="기타" />
                                    <div
                                        class="flex items-center justify-center px-4 py-2 border border-slate-200 rounded-lg cursor-pointer hover:bg-slate-50 hover:border-indigo-200 transition-colors peer-checked:bg-indigo-50 peer-checked:border-indigo-brand peer-checked:text-indigo-brand text-slate-600 text-sm font-medium">
                                        기타</div>
                                </label>
                            </div>
                        </div>
                        <!-- 제출 버튼 -->
                        <div class="pt-4 space-y-4">
                            <button type="submit"
                                class="w-full flex justify-center items-center gap-2 py-3 px-4 border border-transparent rounded-lg shadow-lg shadow-indigo-brand/30 text-base font-bold text-white bg-indigo-brand hover:bg-indigo-dark focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 transition-all">
                                회원가입 완료
                                <span class="material-symbols-outlined text-sm">arrow_forward</span>
                            </button>
                            <div class="relative">
                                <div aria-hidden="true" class="absolute inset-0 flex items-center">
                                    <div class="w-full border-t border-slate-200"></div>
                                </div>
                                <div class="relative flex justify-center text-sm">
                                    <span class="px-4 bg-white text-slate-500 font-medium">또는</span>
                                </div>
                            </div>
                        </div>
                    </form>
                    <div
                        class="mt-6 pt-6 border-t border-slate-100 flex flex-col sm:flex-row justify-between items-center gap-4 text-sm">
                        <router-link :to="{ name: 'loginPage' }"
                            class="text-slate-500 hover:text-indigo-brand transition-colors flex items-center gap-1 group font-medium">
                            <span
                                class="material-symbols-outlined text-lg group-hover:-translate-x-1 transition-transform">arrow_back</span>
                            로그인 화면으로 돌아가기
                        </router-link>
                        <div class="flex items-center gap-4 text-slate-400">
                            <a class="text-slate-500 hover:text-indigo-brand transition-colors" href="#">아이디 찾기</a>
                            <span>|</span>
                            <a class="text-slate-500 hover:text-indigo-brand transition-colors" href="#">비밀번호 찾기</a>
                        </div>
                    </div>
                </div>
            </div>
        </main>
    </div>
</template>
<script setup lang="ts">
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/stores/user';

const router = useRouter();
const userStore = useUserStore();

const form = reactive({
    name: '',
    email: '',
    password: '',
    confirmPassword: '',
    age: '',
    address: '',
    gender: ''
});

const handleSignup = async () => {
    if (!form.name || !form.email || !form.password || !form.confirmPassword) {
        alert('모든 필수 항목을 입력해주세요.');
        return;
    }

    if (form.password !== form.confirmPassword) {
        alert("비밀번호가 일치하지 않습니다.");
        return;
    }

    const success = await userStore.signup(form);

    if (success) {
        alert("회원가입이 완료되었습니다! 로그인해주세요.");
        router.push({ name: "loginPage" });
    } else {
        alert("회원가입에 실패했습니다.");
    }
};
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Lexend:wght@100..900&family=Noto+Sans+KR:wght@100..900&display=swap');
@import url('https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:wght,FILL@100..700,0..1&display=swap');



/* Tailwind Config Custom Colors - Inline for this component (Legacy support) */
.bg-light-bg {
    background-color: #F8FAFC;
}

.text-indigo-brand {
    color: #4F46E5;
}

.bg-indigo-brand {
    background-color: #4F46E5;
}

.hover\:bg-indigo-dark:hover {
    background-color: #3730A3;
}

.hover\:text-indigo-dark:hover {
    color: #3730A3;
}

.focus\:ring-indigo-brand:focus {
    --tw-ring-color: #4F46E5;
}

.font-display {
    font-family: 'Lexend', 'Noto Sans KR', sans-serif;
}

.font-body {
    font-family: 'Noto Sans KR', sans-serif;
}
</style>
