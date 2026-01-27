import type { Meta, StoryObj } from '@storybook/vue3';
import AdminStatTable from './AdminStatTable.vue';

const meta: Meta<typeof AdminStatTable> = {
  title: 'Components/Dashboard/Admin/AdminStatTable',
  component: AdminStatTable,
  tags: ['autodocs'],
  parameters: {
    layout: 'fullscreen', // 전체 너비를 확인하기 위해 fullscreen 설정
  },
};

export default meta;
type Story = StoryObj<typeof AdminStatTable>;

// 1. 실제 대시보드 환경과 유사한 기본 상태
export const Default: Story = {
  render: () => ({
    components: { AdminStatTable },
    template: `
      <div class="p-8 bg-slate-50 min-h-screen">
        <div class="max-w-7xl mx-auto">
          <AdminStatTable />
        </div>
      </div>
    `,
  }),
};

// 2. 모바일/좁은 화면 테스트용
export const NarrowView: Story = {
  render: () => ({
    components: { AdminStatTable },
    template: `
      <div class="p-4 bg-slate-50 min-h-screen">
        <div class="max-w-md mx-auto">
          <AdminStatTable />
        </div>
      </div>
    `,
  }),
};