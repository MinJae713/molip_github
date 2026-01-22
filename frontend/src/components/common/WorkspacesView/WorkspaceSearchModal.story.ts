import type { Meta, StoryObj } from '@storybook/vue3';
import WorkspaceSearchModal from './WorkspaceSearchModal.vue';

// 메타 설정
const meta = {
  title: 'Project/WorkspaceSearchModal',
  component: WorkspaceSearchModal,
  tags: ['autodocs'],
  parameters: {
    // 배경색을 어둡게 설정하여 흰색 모달이 잘 보이게 함
    layout: 'fullscreen',
    backgrounds: {
      default: 'dark',
    },
  },
  // Props 컨트롤 패널 설정
  argTypes: {
    isOpen: {
      control: 'boolean',
      description: '모달 표시 여부',
    },
    // 이벤트는 Actions 패널에 로그가 찍히도록 설정
    onClose: { action: 'closed' },
  },
} satisfies Meta<typeof WorkspaceSearchModal>;

export default meta;
type Story = StoryObj<typeof meta>;

// 1. 기본 스토리 (Default)
export const Default: Story = {
  args: {
    isOpen: true,
  },
};

// 2. 닫힌 상태 (Closed)
export const Closed: Story = {
  args: {
    isOpen: false,
  },
};