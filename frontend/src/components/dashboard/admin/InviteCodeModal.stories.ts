import type { Meta, StoryObj } from '@storybook/vue3';
import BaseModal from '@/components/common/BaseModal.vue';
import InviteCodeModalContent from './InviteCodeModal.vue';
import { ref } from 'vue';

const meta: Meta = {
  title: 'Dashboard/Admin/InviteCodeModal',
  component: InviteCodeModalContent,
};

export default meta;

export const FullModal: StoryObj = {
  render: () => ({
    components: { BaseModal, InviteCodeModalContent },
    setup() {
      const isOpen = ref(true);
      return { isOpen };
    },
    // BaseModal의 title과 maxWidth를 이미지와 맞춤
    template: `
    <BaseModal 
        v-model:open="isOpen" 
        title="신규 워크스페이스 초대코드 발급" 
        maxWidth="max-w-[540px]"
    >
        <InviteCodeModalContent inviteCode="TC-WKS-7284-91" @close="isOpen = false" />
    </BaseModal>
    `,
  }),
};