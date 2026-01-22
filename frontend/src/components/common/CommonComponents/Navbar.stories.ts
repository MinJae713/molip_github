import type { Meta, StoryObj } from '@storybook/vue3';
import Navbar from './Navbar.vue';

const meta: Meta<typeof Navbar> = {
  title: 'Common/Navbar',
  component: Navbar,
};

export default meta;
type Story = StoryObj<typeof Navbar>;

export const Default: Story = {
  args: {
    workspaceName: 'TechCorp HQ',
    currentPathName: 'Workspace Home',
    hasNotifications: true,
  },
};

export const InstructorView: Story = {
  args: {
    workspaceName: 'TechCorp HQ',
    currentPathName: '강사 대시보드',
    user: {
      name: '이준혁',
      email: 'junhyeok.lee@techcorp.com',
      profileImage: 'https://lh3.googleusercontent.com/aida-public/AB6AXuBiolsf6prQfZQmoZ420xEZd441M2OqmX3ZkCgcZz6CiNM7dWL1j5LkuMWr7C98MkSlOn5khaGV_o6iP8HvgU5kV1ulrcFZ4M_s4sPvoltW4goOg9cU1JZ5MwNh5Jqp3a6O4sh35Gt_K8JOaiejQAWSGcZep5eH2o3TBxeCCemLid8l3JlLO5BXcjQhIR0LMMCRbZb7f4kbU6yd3jFL06u8vBS2gLEppd_BaZP7-ASlf6YuhAMP5EtdxZwJfdsRzYY0kqCpgq_PiY'
    }
  },
};