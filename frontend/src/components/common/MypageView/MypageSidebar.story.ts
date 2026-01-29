import type { Meta, StoryObj } from '@storybook/vue3';
import Sidebar from '@/components/MypageView/MypageSidebar.vue'; // 파일 경로가 다르면 수정해주세요.

const meta: Meta<typeof Sidebar> = {
  title: 'Components/Navigation/Sidebar',
  component: Sidebar,
  // 컴포넌트를 중앙이 아닌 왼쪽에 붙여서 실제 사이드바처럼 보이게 설정
  parameters: {
    layout: 'fullscreen',
  },
  tags: ['autodocs'],
  argTypes: {
    workspaceName: { 
      control: 'text',
      description: '워크스페이스 이름' 
    },
    workspaceLogo: { 
      control: 'text',
      description: '워크스페이스 로고 URL' 
    },
    currentActiveId: {
      control: 'select',
      options: ['profile', 'certificates', 'verify', 'settings', 'help'],
      description: '현재 활성화된 메뉴 아이템의 ID'
    },
    onNavigate: { action: 'navigated' }
  },
};

export default meta;
type Story = StoryObj<typeof Sidebar>;

// 1. 기본 상태 (네이버 워크스페이스 예시)
export const Default: Story = {
  args: {
    workspaceName: 'Naver Cloud',
    workspaceLogo: 'https://placehold.co/100x100/03c75a/ffffff?text=Naver',
    currentActiveId: 'profile',
  },
};

// 2. 다른 워크스페이스 예시
export const TechCorp: Story = {
  args: {
    workspaceName: 'TechCorp HQ',
    workspaceLogo: 'https://placehold.co/100x100/4f46e5/ffffff?text=Tech',
    currentActiveId: 'certificates',
  },
};

// 3. 로고가 없는 경우 (기본 아이콘 노출 확인용)
export const NoLogo: Story = {
  args: {
    workspaceName: 'Private Workspace',
    workspaceLogo: '',
    currentActiveId: 'verify',
  },
};