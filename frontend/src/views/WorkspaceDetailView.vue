<template>
    <div>
        <h1>workspaceDetail</h1>
    </div>
</template>

<script setup lang="ts">
import { onMounted, watch } from 'vue';
import { useRoute } from 'vue-router';
import { useWorkspaceStore } from '@/stores/workspace';

const route = useRoute();
const workspaceStore = useWorkspaceStore();

const updateCurrentWorkspace = async () => {
    const workspaceId = route.params.id;
    if (workspaceId) {
        // 스토어에 새로 만들 갱신 함수를 호출합니다.
        const id = Array.isArray(workspaceId) ? workspaceId[0] : workspaceId;
        if (id) {
            await workspaceStore.setCurrentWorkspaceById(id);
        }
    }
};

onMounted(() => {
    updateCurrentWorkspace();
});

// 동일한 상세 페이지 내에서 ID만 바뀌는 경우(예: 사이드바에서 다른 워크스페이스 클릭)를 대비합니다.
watch(() => route.params.id, () => {
    updateCurrentWorkspace();
});
</script>