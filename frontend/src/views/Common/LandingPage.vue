<template>
  <div class="min-h-screen bg-slate-900 text-white overflow-x-hidden font-sans selection:bg-indigo-500 selection:text-white">
    <!-- Navbar (Simple) -->
    <nav class="fixed top-0 w-full z-50 transition-all duration-300" :class="isScrolled ? 'bg-slate-900/80 backdrop-blur-md border-b border-white/10' : 'bg-transparent'">
      <div class="max-w-7xl mx-auto px-6 h-20 flex items-center justify-between">
        <div class="flex items-center gap-2 cursor-pointer" @click="scrollToTop">
          <span class="material-symbols-outlined text-3xl text-indigo-500">school</span>
          <span class="text-xl font-bold tracking-tight">Molip<span class="text-indigo-400 font-light"> Space</span></span>
        </div>
        <router-link to="/login" class="px-6 py-2 rounded-full bg-white/10 hover:bg-white/20 border border-white/10 transition-all text-sm font-bold backdrop-blur-sm">
          로그인 / 시작하기
        </router-link>
      </div>
    </nav>
    <!-- Hero Section -->
    <section class="relative h-screen flex items-center justify-center overflow-hidden">
      <!-- Background Effects -->
      <div class="absolute inset-0 bg-gradient-to-b from-indigo-900/20 to-slate-900 z-0"></div>
      <div class="absolute top-1/4 left-1/4 w-96 h-96 bg-indigo-600/20 rounded-full blur-3xl animate-pulse-slow"></div>
      <div class="absolute bottom-1/4 right-1/4 w-96 h-96 bg-purple-600/20 rounded-full blur-3xl animate-pulse-slow delay-1000"></div>

      <div class="relative z-10 text-center px-6 max-w-5xl mx-auto">
        <div class="inline-block mb-6 px-4 py-1.5 rounded-full border border-indigo-500/30 bg-indigo-500/10 text-indigo-300 text-sm font-medium tracking-wide animate-fade-in-up">
          모두를 위한 몰입의 과학
        </div>
        <h1 class="text-5xl md:text-7xl font-extrabold tracking-tight mb-8 leading-tight animate-fade-in-up delay-100">
          비대면 교육의 한계,<br />
          <span class="text-transparent bg-clip-text bg-gradient-to-r from-indigo-400 via-purple-400 to-pink-400">기술로 극복하다.</span>
        </h1>
        <p class="text-lg md:text-xl text-slate-300 max-w-2xl mx-auto mb-10 leading-relaxed animate-fade-in-up delay-200">
          연결은 되었으나 소통과 증명은 끊겼습니다.<br/>
          Molip은 진정한 <strong>교육적 몰입</strong>을 만들어냅니다.
        </p>
        <div class="flex flex-col sm:flex-row items-center justify-center gap-4 animate-fade-in-up delay-300">
          <router-link to="/login" class="px-8 py-4 bg-indigo-600 hover:bg-indigo-700 text-white rounded-full font-bold text-lg transition-all shadow-lg shadow-indigo-600/25 hover:scale-105">
            Molip 시작하기
          </router-link>
          <button @click="scrollToSection('problem')" class="px-8 py-4 bg-white/5 hover:bg-white/10 border border-white/10 text-white rounded-full font-bold text-lg transition-all hover:scale-105 backdrop-blur-sm">
            더 알아보기
          </button>
        </div>
      </div>
      
      <!-- Scroll Down Indicator -->
      <div class="absolute bottom-10 left-1/2 -translate-x-1/2 animate-bounce opacity-50">
        <span class="material-symbols-outlined text-3xl">keyboard_arrow_down</span>
      </div>
    </section>

    <!-- Problem Section -->
    <section id="problem" class="py-32 px-6 relative bg-slate-900 border-t border-slate-800">
      <div class="max-w-7xl mx-auto">
        <div class="text-center mb-20 scroll-reveal">
          <h2 class="text-3xl md:text-5xl font-bold mb-6">비대면 교육, <span class="italic text-indigo-400">만족하셨나요?</span></h2>
          <p class="text-xl text-slate-400">우리는 화면 너머의 <strong>단절</strong>을 경험했습니다.</p>
        </div>

        <div class="grid md:grid-cols-3 gap-8">
          <!-- Card 1 -->
          <div class="bg-slate-800/50 p-8 rounded-3xl border border-slate-700 hover:border-indigo-500/50 transition-all duration-300 hover:-translate-y-2 scroll-reveal delay-100">
            <div class="size-14 bg-rose-500/10 rounded-2xl flex items-center justify-center mb-6 text-rose-500">
              <span class="material-symbols-outlined text-3xl">videocam_off</span>
            </div>
            <h3 class="text-xl font-bold mb-3">강사의 답답함</h3>
            <p class="text-slate-400 leading-relaxed">
              "벽에 대고 혼자 떠드는 기분입니다."<br/>
              학생들의 표정도, 반응도 알 수 없어 소통이 끊겼습니다.
            </p>
          </div>

          <!-- Card 2 -->
          <div class="bg-slate-800/50 p-8 rounded-3xl border border-slate-700 hover:border-indigo-500/50 transition-all duration-300 hover:-translate-y-2 scroll-reveal delay-200">
            <div class="size-14 bg-amber-500/10 rounded-2xl flex items-center justify-center mb-6 text-amber-500">
              <span class="material-symbols-outlined text-3xl">sleep</span>
            </div>
            <h3 class="text-xl font-bold mb-3">수강생의 딴짓</h3>
            <p class="text-slate-400 leading-relaxed">
              "잠깐 졸았는데 진도가 끝나있어요."<br/>
              흐름을 끊을까 봐 질문조차 하기 힘든 환경입니다.
            </p>
          </div>

          <!-- Card 3 -->
          <div class="bg-slate-800/50 p-8 rounded-3xl border border-slate-700 hover:border-indigo-500/50 transition-all duration-300 hover:-translate-y-2 scroll-reveal delay-300">
            <div class="size-14 bg-purple-500/10 rounded-2xl flex items-center justify-center mb-6 text-purple-500">
              <span class="material-symbols-outlined text-3xl">analytics</span>
            </div>
            <h3 class="text-xl font-bold mb-3">관리자의 불안</h3>
            <p class="text-slate-400 leading-relaxed">
              "출석률은 100%인데 성과는 왜 이러죠?"<br/>
              교육비를 투자했지만 효과를 증명할 방법이 없습니다.
            </p>
          </div>
        </div>
      </div>
    </section>

    <!-- Introduction Section -->
    <section class="py-32 px-6 bg-gradient-to-b from-slate-900 to-indigo-950/30 text-center relative overflow-hidden">
      <div class="absolute inset-0 bg-[url('https://www.transparenttextures.com/patterns/cubes.png')] opacity-5"></div>
      <div class="max-w-4xl mx-auto relative z-10 scroll-reveal">
        <h2 class="text-4xl md:text-6xl font-bold mb-8 leading-tight">
          그래서 만들었습니다.<br/>
          오직 <span class="text-indigo-400">교육에 특화된</span> 플랫폼,
        </h2>
        <div class="inline-block">
          <h1 class="text-6xl md:text-8xl font-black tracking-tighter text-transparent bg-clip-text bg-gradient-to-r from-indigo-400 to-purple-400 drop-shadow-2xl">
            Molip Space
          </h1>
        </div>
      </div>
    </section>

    <!-- Features Section 1: Instructor -->
    <section class="py-32 px-6 bg-slate-900 relative">
      <div class="max-w-7xl mx-auto flex flex-col md:flex-row items-center gap-16">
        <div class="flex-1 scroll-reveal md:text-left text-center">
          <div class="inline-block px-4 py-1 rounded-full bg-indigo-500/20 text-indigo-400 font-bold text-sm mb-4">Instructor</div>
          <h2 class="text-3xl md:text-5xl font-bold mb-6 leading-normal">추측이 아닌,
            <br/>
            <span class="text-indigo-400">데이터</span>로 소통하세요.</h2>
          <p class="text-lg text-slate-400 mb-8 leading-relaxed">
            Molip은 <strong>MediaPipe</strong>와 <strong>Deepface</strong> 모델을 활용해 실시간으로 학생들의 표정과 집중도를 분석합니다.
            이제 대시보드를 통해 "지금 내용이 어렵구나", "지루해하는구나"를 직관적으로 파악하고 수업의 흐름을 조절하세요.
          </p>
          <ul class="space-y-4 text-left inline-block">
            <li class="flex items-center gap-3 text-slate-300">
              <span class="material-symbols-outlined text-indigo-500">check_circle</span>
              실시간 감정 및 집중도 그래프
            </li>
            <li class="flex items-center gap-3 text-slate-300">
              <span class="material-symbols-outlined text-indigo-500">check_circle</span>
              음성 인식(STT) 기반 실시간 퀴즈 생성
            </li>
            <li class="flex items-center gap-3 text-slate-300">
              <span class="material-symbols-outlined text-indigo-500">check_circle</span>
              Redis 기반 초고속 데이터 처리
            </li>
          </ul>
        </div>
        <div class="flex-1 relative w-full h-96 bg-slate-800 rounded-3xl border border-slate-700 overflow-hidden shadow-2xl scroll-reveal delay-200">
          <div class="absolute inset-0 bg-gradient-to-br from-slate-800 to-slate-900 flex items-center justify-center group">
              <!-- Placeholder for Dashboard UI -->
              <div class="text-center">
                  <span class="material-symbols-outlined text-8xl text-indigo-500/30 group-hover:text-indigo-500/50 transition-all duration-500">bar_chart</span>
                  <p class="mt-4 text-slate-500 font-mono text-sm">Real-time Analytics Dashboard</p>
              </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Features Section 2: Student -->
    <section class="py-32 px-6 bg-slate-900/50 relative border-t border-slate-800">
      <div class="max-w-7xl mx-auto flex flex-col md:flex-row-reverse items-center gap-16">
        <div class="flex-1 scroll-reveal md:text-left text-center">
          <div class="inline-block px-4 py-1 rounded-full bg-purple-500/20 text-purple-400 font-bold text-sm mb-4">Learner</div>
          <h2 class="text-3xl md:text-5xl font-bold mb-6">감시자가 아닌, <span class="text-purple-400">러닝 메이트</span>.</h2>
          <p class="text-lg text-slate-400 mb-8 leading-relaxed">
            나의 집중도와 감정 상태를 피드백 받으며 스스로 학습 태도를 교정합니다. 
            특히 <strong>AI 기반 복습</strong> 기능으로, 내가 졸았던 순간과 집중하지 못한 순간을 타임라인에서 바로 찾아보세요.
          </p>
          <ul class="space-y-4 text-left inline-block">
            <li class="flex items-center gap-3 text-slate-300">
              <span class="material-symbols-outlined text-purple-500">check_circle</span>
              스마트 시선 보정 및 태도 피드백
            </li>
            <li class="flex items-center gap-3 text-slate-300">
              <span class="material-symbols-outlined text-purple-500">check_circle</span>
              집중도 기반 스마트 타임라인 복습
            </li>
            <li class="flex items-center gap-3 text-slate-300">
              <span class="material-symbols-outlined text-purple-500">check_circle</span>
              AI 챗봇을 통한 즉각적인 질문 해결
            </li>
          </ul>
        </div>
        <div class="flex-1 relative w-full h-96 bg-slate-800 rounded-3xl border border-slate-700 overflow-hidden shadow-2xl scroll-reveal delay-200">
             <div class="absolute inset-0 bg-gradient-to-bl from-slate-800 to-slate-900 flex items-center justify-center group">
                <div class="text-center">
                    <span class="material-symbols-outlined text-8xl text-purple-500/30 group-hover:text-purple-500/50 transition-all duration-500">history_edu</span>
                    <p class="mt-4 text-slate-500 font-mono text-sm">Smart Review Timeline</p>
                </div>
            </div>
        </div>
      </div>
    </section>

    <!-- Features Section 3: Manager -->
    <section class="py-32 px-6 bg-slate-900 relative border-t border-slate-800">
      <div class="max-w-7xl mx-auto flex flex-col md:flex-row items-center gap-16">
        <div class="flex-1 scroll-reveal md:text-left text-center">
          <div class="inline-block px-4 py-1 rounded-full bg-emerald-500/20 text-emerald-400 font-bold text-sm mb-4">Manager</div>
          <h2 class="text-3xl md:text-5xl font-bold mb-6 leading-normal">운영은 간편하게,
            <br/>
            <span class="text-emerald-400">지표는 확실하게</span>.</h2>
          <p class="text-lg text-slate-400 mb-8 leading-relaxed">
            복잡한 설치 없이 워크스페이스 생성만으로 준비 끝.
            단순한 출석률을 넘어, AI가 분석한 집중도와 감정 데이터를 바탕으로 <strong>정량적인 교육 지표</strong>를 제공합니다.
          </p>
          <ul class="space-y-4 text-left inline-block">
            <li class="flex items-center gap-3 text-slate-300">
              <span class="material-symbols-outlined text-emerald-500">check_circle</span>
              원클릭 워크스페이스 생성 및 초대
            </li>
            <li class="flex items-center gap-3 text-slate-300">
              <span class="material-symbols-outlined text-emerald-500">check_circle</span>
              수강생 통합 관리 시스템
            </li>
            <li class="flex items-center gap-3 text-slate-300">
              <span class="material-symbols-outlined text-emerald-500">check_circle</span>
              객관적인 성취도 및 집중도 리포트
            </li>
          </ul>
        </div>
        <div class="flex-1 relative w-full h-96 bg-slate-800 rounded-3xl border border-slate-700 overflow-hidden shadow-2xl scroll-reveal delay-200">
            <div class="absolute inset-0 bg-gradient-to-br from-slate-800 to-slate-900 flex items-center justify-center group">
                <div class="text-center">
                    <span class="material-symbols-outlined text-8xl text-emerald-500/30 group-hover:text-emerald-500/50 transition-all duration-500">admin_panel_settings</span>
                    <p class="mt-4 text-slate-500 font-mono text-sm">Admin Workspace & Report</p>
                </div>
            </div>
        </div>
      </div>
    </section>

    <!-- Closing Section -->
    <section class="py-40 px-6 text-center bg-gradient-to-b from-slate-900 to-indigo-950 relative overflow-hidden">
      <div class="absolute inset-0 bg-[url('https://www.transparenttextures.com/patterns/stardust.png')] opacity-10"></div>
      
      <div class="max-w-4xl mx-auto relative z-10 scroll-reveal">
        <h2 class="text-4xl md:text-5xl font-bold mb-8">기술은 사람을 향해야 합니다.</h2>
        <p class="text-xl text-slate-300 mb-12 max-w-2xl mx-auto">
          Molip은 차가운 모니터 너머의 사람을 잇고,<br/>
          끊겨버린 소통을 다시 연결하여 <strong>비대면 교육의 미래</strong>를 엽니다.
        </p>
        <router-link to="/login" class="inline-block px-10 py-5 bg-white text-indigo-900 rounded-full font-bold text-xl transition-all hover:bg-indigo-50 hover:scale-105 shadow-2xl shadow-indigo-500/20">
            지금 바로 시작하기
        </router-link>
      </div>
    </section>

    <!-- Footer -->
    <footer class="py-12 px-6 border-t border-slate-800 bg-slate-950 text-slate-500 text-center">
      <div class="max-w-7xl mx-auto flex flex-col md:flex-row justify-between items-center gap-4">
        <div class="flex items-center gap-2">
          <span class="material-symbols-outlined text-2xl text-slate-600">school</span>
          <span class="font-bold text-slate-400">Molip Space</span>
        </div>
        <p class="text-sm">© 2026 Molip Team. All rights reserved.</p>
        <div class="flex gap-6 text-sm">
          <a href="#" class="hover:text-white transition-colors">Privacy Policy</a>
          <a href="#" class="hover:text-white transition-colors">Terms of Service</a>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';

const isScrolled = ref(false);

const handleScroll = () => {
  isScrolled.value = window.scrollY > 50;
};

const scrollToTop = () => {
    window.scrollTo({ top: 0, behavior: 'smooth' });
};

const scrollToSection = (id: string) => {
    const el = document.getElementById(id);
    if (el) {
        el.scrollIntoView({ behavior: 'smooth' });
    }
};

// Intersection Observer for scroll animations
let observer: IntersectionObserver | null = null;

onMounted(() => {
  window.addEventListener('scroll', handleScroll);

  const options = {
    threshold: 0.1,
    rootMargin: "0px"
  };

  observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        entry.target.classList.add('visible');
        observer?.unobserve(entry.target); // Animate only once
      }
    });
  }, options);

  const elements = document.querySelectorAll('.scroll-reveal');
  elements.forEach(el => observer?.observe(el));
});

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll);
  if (observer) observer.disconnect();
});
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Pretendard:wght@300;400;600;700;900&display=swap');

.font-sans {
  font-family: 'Pretendard', sans-serif;
}

/* Custom Animations */
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.animate-fade-in-up {
  opacity: 0;
  animation: fadeInUp 0.8s ease-out forwards;
}

.delay-100 { animation-delay: 0.1s; }
.delay-200 { animation-delay: 0.2s; }
.delay-300 { animation-delay: 0.3s; }
.delay-1000 { animation-delay: 1s; }

.animate-pulse-slow {
  animation: pulse 4s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}

/* Scroll Reveal Base Styles */
.scroll-reveal {
  opacity: 0;
  transform: translateY(30px);
  transition: all 1s ease-out;
}

.scroll-reveal.visible {
  opacity: 1;
  transform: translateY(0);
}
</style>
