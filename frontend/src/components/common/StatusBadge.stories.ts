import type { Meta, StoryObj } from '@storybook/vue3';
import StatusBadge from './StatusBadge.vue';

const meta: Meta<typeof StatusBadge> = {
  title: 'Components/Common/StatusBadge',
  component: StatusBadge,
  tags: ['autodocs'],
  argTypes: {
    variant: {
      control: 'select',
      options: ['ongoing', 'completed', 'upcoming'],
      description: '상태 유형에 따라 색상과 텍스트가 자동으로 결정됩니다.',
    },
  },
};

export default meta;
type Story = StoryObj<typeof StatusBadge>;

// 1. 모든 상태 한눈에 보기
export const AllVariants: Story = {
  render: () => ({
    components: { StatusBadge },
    template: `
      <div class="flex flex-col gap-4">
        <div class="flex items-center gap-2">
          <span class="text-sm text-slate-400 w-20">진행 중:</span>
          <StatusBadge variant="ongoing" />
        </div>
        <div class="flex items-center gap-2">
          <span class="text-sm text-slate-400 w-20">종료됨:</span>
          <StatusBadge variant="completed" />
        </div>
        <div class="flex items-center gap-2">
          <span class="text-sm text-slate-400 w-20">예정됨:</span>
          <StatusBadge variant="upcoming" />
        </div>
      </div>
    `,
  }),
};

// 2. 개별 상태 확인
export const Ongoing: Story = {
  args: {
    variant: 'ongoing',
  },
};

export const Completed: Story = {
  args: {
    variant: 'completed',
  },
};

export const Upcoming: Story = {
  args: {
    variant: 'upcoming',
  },
};