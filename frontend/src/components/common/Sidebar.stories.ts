import type { Meta, StoryObj } from '@storybook/vue3';
import AppSidebar from './Sidebar.vue';

const meta: Meta<typeof AppSidebar> = {
    title: 'Common/AppSidebar',
    component: AppSidebar,
};

export default meta;
type Story = StoryObj<typeof AppSidebar>;

export const Default: Story = {
    args: {
        workspaceName: 'TechCorp HQ',
        currentActiveId: 'courses',
    },
};

export const DashboardActive: Story = {
    args: {
        workspaceName: 'EduVerse Admin',
        currentActiveId: 'dashboard',
    },
};