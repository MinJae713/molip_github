import type { Meta, StoryObj } from '@storybook/vue3';
import AppSidebar from './Sidebar.vue';

const meta: Meta<typeof AppSidebar> = {
    title: 'Common/Sidebar',
    component: AppSidebar,
};

export default meta;
type Story = StoryObj<typeof AppSidebar>;

export const Default: Story = {
    args: {
        currentActiveId: 'courses',
    },
};

export const DashboardActive: Story = {
    args: {
        currentActiveId: 'dashboard',
    },
};