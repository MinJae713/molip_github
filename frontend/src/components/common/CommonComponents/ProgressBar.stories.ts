import type { Meta, StoryObj } from '@storybook/vue3';
import ProgressBar from './ProgressBar.vue';

const meta: Meta<typeof ProgressBar> = {
  title: 'Components/Common/ProgressBar',
  component: ProgressBar,
  tags: ['autodocs'],
  argTypes: {
    progress: {
      control: { type: 'range', min: 0, max: 100, step: 1 },
      description: '0에서 100 사이의 진행률 수치입니다.',
    },
  },
};

export default meta;
type Story = StoryObj<typeof ProgressBar>;

// 1. 기본 상태 (중간 진행)
export const Default: Story = {
  args: {
    progress: 30,
  },
};

// 2. 시작 전 (0%)
export const NotStarted: Story = {
  args: {
    progress: 0,
  },
};

// 3. 완료 (100%) - 인디고 색상으로 변하는지 확인!
export const Completed: Story = {
  args: {
    progress: 100,
  },
};

// 4. 여러 개를 한 번에 비교 (리스트 상황 시뮬레이션)
export const ListComparison: Story = {
  render: (args) => ({
    components: { ProgressBar },
    setup() {
      return { args };
    },
    template: `
      <div class="flex flex-col gap-6 p-6 bg-white border rounded-xl w-100">
        <div>
          <p class="text-xs text-slate-400 mb-2 font-medium underline">시작 전</p>
          <ProgressBar :progress="0" />
        </div>
        <div>
          <p class="text-xs text-slate-400 mb-2 font-medium underline">진행 중</p>
          <ProgressBar :progress="65" />
        </div>
        <div>
          <p class="text-xs text-slate-400 mb-2 font-medium underline">완료 (색상 변경)</p>
          <ProgressBar :progress="100" />
        </div>
      </div>
    `,
  }),
};