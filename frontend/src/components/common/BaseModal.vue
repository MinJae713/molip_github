<script setup lang="ts">
/**
 * BaseModal
 * - 서비스 전역에서 사용하는 공통 모달 컴포넌트
 * - UI 뼈대 + 스크롤 락 + 키보드(ESC) 제어 포함
 */
import { watch, onMounted, onUnmounted } from 'vue';

interface Props {
  /** v-model:open으로 제어되는 열림 상태 */
  open: boolean;
  /** 단순한 제목 (복잡한 구성은 header 슬롯 사용) */
  title?: string;
  /** Tailwind max-width 클래스 (ex: max-w-md, max-w-xl) */
  maxWidth?: string;
  /** 배경(Dim) 클릭 시 닫기 허용 여부 */
  closeOnBackdrop?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  open: false,
  title: '',
  maxWidth: 'max-w-xl',
  closeOnBackdrop: true
});

const emit = defineEmits<{
  (e: 'update:open', value: boolean): void;
  (e: 'close'): void;
}>();

/**
 * 모달 닫기 공통 함수
 */
const close = () => {
  emit('update:open', false);
  emit('close');
};

/**
 * 배경 클릭 시 닫기 처리
 */
const handleBackdropClick = () => {
  if (props.closeOnBackdrop) close();
};

/**
 * 1. 스크롤 락 (Body Scroll Lock)
 * 모달이 열릴 때 배경 스크롤을 방지합니다.
 */
watch(() => props.open, (newVal) => {
  if (newVal) {
    document.body.style.overflow = 'hidden';
  } else {
    document.body.style.overflow = '';
  }
}, { immediate: true });

/**
 * 2. ESC 키 이벤트 처리
 */
const handleEsc = (e: KeyboardEvent) => {
  if (e.key === 'Escape' && props.open) close();
};

onMounted(() => window.addEventListener('keydown', handleEsc));
onUnmounted(() => {
  window.removeEventListener('keydown', handleEsc);
  // 컴포넌트가 갑자기 사라질 경우를 대비해 스크롤 복구
  document.body.style.overflow = ''; 
});
</script>

<template>
  <Teleport to="body">
    <Transition name="fade">
      <div
        v-if="open"
        class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-sm"
        role="dialog"
        aria-modal="true"
        :aria-labelledby="title ? 'modal-title' : undefined"
        @click.self="handleBackdropClick"
      >
        <Transition name="slide-up">
          <div
            v-if="open"
            class="w-full bg-white rounded-3xl shadow-2xl overflow-hidden"
            :class="maxWidth"
          >
            <header
              v-if="$slots.header || title"
              class="flex items-center justify-between px-8 py-6 border-b border-slate-100"
            >
              <slot name="header">
                <h3
                  v-if="title"
                  id="modal-title"
                  class="text-xl font-bold text-slate-900 font-display"
                >
                  {{ title }}
                </h3>
              </slot>

              <button
                type="button"
                @click="close"
                class="text-slate-400 hover:text-slate-600 transition-colors flex items-center justify-center p-1 cursor-pointer"
                aria-label="모달 닫기"
              >
                <span class="material-symbols-outlined">close</span>
              </button>
            </header>

            <main class="p-8">
              <slot />
            </main>

            <footer 
              v-if="$slots.footer" 
              class="px-8 py-6 border-t border-slate-100 bg-slate-50/30"
            >
              <slot name="footer" />
            </footer>
          </div>
        </Transition>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
/* 배경 페이드 애니메이션 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.25s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 모달 슬라이드 & 스케일 애니메이션 */
.slide-up-enter-active,
.slide-up-leave-active {
  transition: all 0.4s cubic-bezier(0.16, 1, 0.3, 1);
}
.slide-up-enter-from {
  opacity: 0;
  transform: translateY(24px) scale(0.96);
}
.slide-up-leave-to {
  opacity: 0;
  transform: translateY(12px) scale(0.98);
}

/* 폰트 설정 (전역 CSS에 설정되어 있다면 제거 가능) */
.font-display {
  font-family: 'Lexend', 'Noto Sans KR', sans-serif;
}
</style>