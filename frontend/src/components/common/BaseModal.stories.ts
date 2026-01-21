import type { Meta, StoryObj } from '@storybook/vue3';
import BaseModal from './BaseModal.vue';
import { ref } from 'vue';

const meta: Meta<typeof BaseModal> = {
  title: 'Common/BaseModal',
  component: BaseModal,
  tags: ['autodocs'],
  argTypes: {
    open: {
      control: 'boolean',
      description: '모달의 표시 여부',
    },
    title: {
      control: 'text',
      description: '모달 상단 제목',
    },
    maxWidth: {
      control: 'select',
      options: ['max-w-md', 'max-w-lg', 'max-w-xl', 'max-w-2xl'],
    },
    closeOnBackdrop: {
      control: 'boolean',
    },
  },
};

export default meta;
type Story = StoryObj<typeof BaseModal>;

/**
 * 1. 기본형 모달
 */
export const Default: Story = {
  args: {
    open: true,
    title: '시스템 알림',
    maxWidth: 'max-w-md',
  },
  render: (args) => ({
    components: { BaseModal },
    setup() {
      return { args };
    },
    template: `
      <BaseModal v-bind="args" v-model:open="args.open">
        <p class="text-slate-600">
          가장 기본적인 형태의 모달입니다. 본문 슬롯에 텍스트나 간단한 요소를 배치합니다.
        </p>
      </BaseModal>
    `,
  }),
};

/**
 * 2. 인터랙션 테스트 (동작 검증용)
 */
export const Interactive: Story = {
  render: (args) => ({
    components: { BaseModal },
    setup() {
      const modalState = ref(false);
      return { args, modalState };
    },
    template: `
      <div class="p-10">
        <button 
          @click="modalState = true"
          class="px-6 py-3 bg-slate-900 text-white rounded-xl font-bold"
        >
          공통 모달 열기
        </button>
        
        <BaseModal 
          v-bind="args" 
          v-model:open="modalState"
        >
          <div class="py-4">
            <h4 class="font-bold mb-2">공통 모달 컴포넌트 테스트</h4>
            <p class="text-sm text-slate-500">
              이 모달은 특정 기능에 종속되지 않은 공통 컴포넌트입니다. 
              내용은 슬롯을 통해 주입받습니다.
            </p>
          </div>
          
          <template #footer>
            <div class="flex justify-end gap-2">
              <button @click="modalState = false" class="px-4 py-2 text-sm font-bold text-slate-500">닫기</button>
              <button @click="modalState = false" class="px-4 py-2 text-sm font-bold bg-blue-600 text-white rounded-lg">확인</button>
            </div>
          </template>
        </BaseModal>
      </div>
    `,
  }),
  args: {
    title: '테스트용 제목',
    maxWidth: 'max-w-lg',
  },
};