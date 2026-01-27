import type { Meta, StoryObj } from '@storybook/vue3';
import RoleChangeModal from './RoleChangeModal.vue';

const meta: Meta<typeof RoleChangeModal> = {
  title: 'Admin/MemberManagement/RoleChangeModal',
  component: RoleChangeModal,
  tags: ['autodocs'],
  argTypes: {
    isOpen: { control: 'boolean' },
    selectedMembers: { control: 'object' },
    onClose: { action: 'close' },
    onConfirm: { action: 'confirm' },
  },
  // 모달이 Teleport(body)를 사용하므로 Storybook 환경에서 잘 보이도록 높이 설정
  decorators: [
    () => ({
      template: '<div style="height: 500px;"><story /></div>',
    }),
  ],
};

export default meta;
type Story = StoryObj<typeof RoleChangeModal>;

/**
 * 1. 한 명의 역할을 변경할 때 (단일 변경 모드)
 */
export const SingleSelection: Story = {
  args: {
    isOpen: true,
    selectedMembers: [
      { id: 1, name: '이정훈', role: 'Admin', email: 'jh.lee@techcorp.com' }
    ],
  },
};

/**
 * 2. 여러 명의 역할을 한 번에 변경할 때 (일괄 변경 모드)
 */
export const MultipleSelection: Story = {
  args: {
    isOpen: true,
    selectedMembers: [
      { id: 1, name: '이정훈', role: 'Admin' },
      { id: 2, name: '김지수', role: 'Learner' },
      { id: 3, name: '박영희', role: 'Learner' },
    ],
  },
};

/**
 * 3. 닫힌 상태
 */
export const Closed: Story = {
  args: {
    isOpen: false,
    selectedMembers: [],
  },
};