<template>
  <div class="flex flex-col items-center justify-center h-full min-h-[60vh] text-center p-8">
    <div class="mb-6 bg-indigo-50 p-6 rounded-full inline-flex items-center justify-center">
      <span class="material-symbols-outlined text-6xl text-indigo-500">construction</span>
    </div>
    
    <h2 class="text-2xl font-bold text-slate-800 mb-3">페이지 준비 중입니다</h2>
    <p class="text-slate-500 max-w-md mx-auto leading-relaxed mb-8">
      현재 서비스 이용을 위해 페이지를 제작하고 있습니다.<br/>
      더 나은 서비스를 제공하기 위해 노력하겠습니다.
    </p>

    <router-link to="/mypage" 
      class="px-6 py-2.5 bg-indigo-600 text-white rounded-lg font-bold hover:bg-indigo-700 transition-colors shadow-lg shadow-indigo-200">
      마이페이지 홈으로 돌아가기
    </router-link>
  </div>
</template>
