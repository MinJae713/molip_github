import type { Meta, StoryObj } from '@storybook/vue3';
import ApplyWorkspaceModal from './ApplyWorkspaceModal.vue';

const meta: Meta<typeof ApplyWorkspaceModal> = {
  title: 'Components/Modals/ApplyWorkspaceModal',
  component: ApplyWorkspaceModal,
  tags: ['autodocs'],
  argTypes: {
    isOpen: { control: 'boolean' },
    workspaceName: { control: 'text' },
    onClose: { action: 'closed' },
    onSubmit: { action: 'submitted' },
  },
};

export default meta;
type Story = StoryObj<typeof ApplyWorkspaceModal>;

export const Default: Story = {
  args: {
    isOpen: true,
    workspaceName: 'TechCorp HQ',
  },
};

export const Closed: Story = {
  args: {
    isOpen: false,
    workspaceName: 'TechCorp HQ',
  },
};