<template>
    <div class="bg-light-bg text-slate-800 min-h-screen flex flex-col font-body">
        <main
            class="flex-1 w-full max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12 flex flex-col items-center justify-center">
            <div class="w-full max-w-md">
                <div class="text-center mb-10">
                    <div class="flex items-center justify-center gap-2 text-indigo-brand mb-4">
                        <span class="material-symbols-outlined text-4xl">school</span>
                        <span class="text-2xl font-bold font-display tracking-tight text-slate-900">Molip<span
                                class="text-indigo-brand font-light"> Space</span></span>
                    </div>
                    <h1 class="text-3xl font-bold text-slate-900 mb-3">로그인</h1>
                    <p class="text-slate-500">
                        서비스 이용을 위해 계정 정보를 입력해주세요.
                    </p>
                </div>
                <div class="glass-panel bg-white border border-slate-200 rounded-xl p-8 sm:p-10 shadow-card">
                    <form class="space-y-6">
                        <div>
                            <label class="block text-sm font-bold text-slate-700 mb-1.5" for="email">이메일</label>
                            <input
                                class="block w-full rounded-lg border-slate-300 shadow-sm sm:text-sm py-3 px-3 transition-colors placeholder:text-slate-400 text-slate-900 focus:outline-none focus:ring-2 focus:ring-indigo-brand focus:border-indigo-brand"
                                id="email" v-model="email" placeholder="example@company.com" type="text"
                                @keyup.enter="handleLogin" />
                        </div>
                        <div>
                            <label class="block text-sm font-bold text-slate-700 mb-1.5" for="password">비밀번호</label>
                            <input
                                class="block w-full rounded-lg border-slate-300 shadow-sm sm:text-sm py-3 px-3 transition-colors placeholder:text-slate-400 text-slate-900 focus:outline-none focus:ring-2 focus:ring-indigo-brand focus:border-indigo-brand"
                                id="password" v-model="password" placeholder="비밀번호를 입력하세요" type="password"
                                @keyup.enter="handleLogin" />
                        </div>
                        <div class="flex items-center justify-between">
                            <div class="flex items-center">
                                <input
                                    class="h-4 w-4 text-indigo-brand focus:ring-indigo-brand border-slate-300 rounded cursor-pointer"
                                    id="remember-me" name="remember-me" type="checkbox" />
                                <label class="ml-2 block text-sm text-slate-700 cursor-pointer" for="remember-me">
                                    로그인 상태 유지
                                </label>
                            </div>
                            <div class="text-sm">
                                <a class="font-medium text-indigo-brand hover:text-indigo-dark transition-colors"
                                    href="#">아이디/비밀번호 찾기</a>
                            </div>
                        </div>
                        <div>
                            <button @click="handleLogin" :disabled="isLoading"
                                class="w-full flex justify-center items-center gap-2 py-3.5 px-4 border border-transparent rounded-lg shadow-lg shadow-indigo-brand/30 text-base font-bold text-white bg-indigo-brand hover:bg-indigo-dark focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 transition-all disabled:opacity-50 disabled:cursor-not-allowed"
                                type="button">
                                <span v-if="isLoading">로그인 중...</span>
                                <span v-else>로그인</span>
                            </button>
                        </div>
                        <div class="relative mt-6">
                            <div aria-hidden="true" class="absolute inset-0 flex items-center">
                                <div class="w-full border-t border-slate-200"></div>
                            </div>
                            <div class="relative flex justify-center text-sm">
                                <span class="px-2 bg-white text-slate-500 font-medium">또는</span>
                            </div>
                        </div>
                    </form>
                    <div class="mt-8 pt-6 border-t border-slate-100 text-center">
                        <p class="text-sm text-slate-500">
                            계정이 없으신가요?
                            <router-link to="/signup"
                                class="ml-1 font-bold text-indigo-brand hover:text-indigo-dark transition-colors">
                                회원가입
                            </router-link>
                        </p>
                    </div>
                </div>
            </div>
        </main>
        <footer class="py-8 text-center text-slate-400 text-xs">
            <p>© 2024 Molip Space. All rights reserved.</p>
        </footer>
    </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/stores/user';

const router = useRouter();
const userStore = useUserStore();

const email = ref('');
const password = ref('');
const isLoading = ref(false);

const handleLogin = async () => {
    if (!email.value || !password.value) {
        alert('이메일과 비밀번호를 입력해주세요.');
        return;
    }

    isLoading.value = true;

    try {
        const success = await userStore.login(email.value, password.value);

        if (success) {
            console.log('로그인 성공');
            // 로그인 후 워크스페이스 목록으로 이동
            router.push('/workspaces');
        } else {
            alert('이메일 또는 비밀번호가 일치하지 않습니다. (목업: student / 1234)');
        }
    } catch (error) {
        console.error('로그인 에러:', error);
        alert('로그인 중 오류가 발생했습니다.');
    } finally {
        isLoading.value = false;
    }
};
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Lexend:wght@100..900&family=Noto+Sans+KR:wght@100..900&display=swap');
@import url('https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:wght,FILL@100..700,0..1&display=swap');

/* Tailwind Config Custom Colors - Inline for this component */
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
