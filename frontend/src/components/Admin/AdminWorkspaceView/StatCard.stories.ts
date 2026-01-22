import type { Meta, StoryObj } from '@storybook/vue3';
import StatCard from './StatCard.vue';

const meta: Meta<typeof StatCard> = {
  title: 'Dashboard/Shared/StatCard',
  component: StatCard,
  tags: ['autodocs'],
  argTypes: {
    variant: {
      control: 'select',
      options: ['indigo', 'blue', 'amber'],
    },
  },
};

export default meta;
type Story = StoryObj<typeof StatCard>;

/**
 * 1. 관리자 대시보드 케이스
 * 우측 상단(#badge)에 증감 수치 배지를 넣습니다.
 */
export const AdminCase: Story = {
  args: {
    title: '진행중인 강의',
    value: '42',
    icon: 'menu_book',
    variant: 'indigo',
  },
  render: (args) => ({
    components: { StatCard },
    setup() { return { args }; },
    template: `
      <StatCard v-bind="args">
        <template #badge>
          <div class="bg-emerald-50 text-emerald-600 px-2 py-1 rounded-md text-[11px] font-bold">
            +12%
          </div>
        </template>
      </StatCard>
    `,
  }),
};

/**
 * 2. 수강생 대시보드 케이스
 * 하단(#footer)에 지난 주 대비 증감 텍스트를 넣습니다.
 */
export const StudentCase: Story = {
  args: {
    title: '나의 학습 진행률',
    value: '15%',
    icon: 'refresh',
    variant: 'blue',
  },
  render: (args) => ({
    components: { StatCard },
    setup() { return { args }; },
    template: `
      <StatCard v-bind="args">
        <template #footer>
          <div class="text-emerald-500 text-xs font-medium mt-1">
            ↗ 지난 주 대비 +5%
          </div>
        </template>
      </StatCard>
    `,
  }),
};

/**
 * 3. 강사 대시보드 케이스
 * 하단(#footer)에 액션 버튼을 넣습니다.
 */
export const InstructorCase: Story = {
  args: {
    title: '운영 중인 교육과정',
    value: '4',
    icon: 'school',
    variant: 'amber',
  },
  render: (args) => ({
    components: { StatCard },
    setup() { return { args }; },
    template: `
      <StatCard v-bind="args">
        <template #footer>
          <button class="text-indigo-600 text-xs font-bold flex items-center gap-1 mt-2 hover:underline">
            <span class="material-symbols-outlined text-sm">add_circle</span>
            신규 개설 가능
          </button>
        </template>
      </StatCard>
    `,
  }),
};