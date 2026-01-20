import type { Meta, StoryObj } from '@storybook/vue3';
import WorkspaceCard from './WorkspaceCard.vue';

const meta: Meta<typeof WorkspaceCard> = {
  title: 'HomeView/WorkspaceCard',
  component: WorkspaceCard,
};

export default meta;
type Story = StoryObj<typeof WorkspaceCard>;

export const Default: Story = {
  args: {
    id: 2,
    name: 'Molip Research Lab',
    logo_url: 'https://img.icons8.com/color/512/google-logo.png',
    location: 'Pangyo, Korea',
    description: '실시간 모니터링 및 AI 기술 연구를 위한 워크스페이스입니다.',
    memberCount: 85,
    role: 'TRAINER',
    status: 2,
    typeIcon: 'science',
    themeColor: 'blue'
  },
};

export const InstructorView: Story = {
  args: {
    id: 2,
    name: 'Molip Research Lab',
    logo_url: 'https://img.icons8.com/color/512/google-logo.png',
    location: 'Pangyo, Korea',
    description: '실시간 모니터링 및 AI 기술 연구를 위한 워크스페이스입니다.',
    memberCount: 85,
    role: 'TRAINER',
    status: 2,
    typeIcon: 'science',
    themeColor: 'blue'
  },
};