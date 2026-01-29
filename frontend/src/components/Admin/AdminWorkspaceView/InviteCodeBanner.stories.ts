import type { Meta, StoryObj } from '@storybook/vue3';
import InviteCodeBanner from './InviteCodeBanner.vue';

const meta: Meta<typeof InviteCodeBanner> = {
  title: 'Dashboard/Admin/InviteCodeBanner',
  component: InviteCodeBanner,
  tags: ['autodocs'],
  argTypes: {
    onGenerate: { action: 'generate-clicked' },
  },
};

export default meta;
type Story = StoryObj<typeof InviteCodeBanner>;

export const Default: Story = {
  args: {
    title: '워크스페이스 초대코드 발급',
    description: '새로운 팀원을 초대하기 위한 고유 코드를 즉시 생성하고 배포하세요.',
  },
};

export const CustomText: Story = {
  args: {
    title: '강사 전용 초대 발급',
    description: '외부 강사님들을 위한 특별 접속 코드를 생성합니다.',
  },
};