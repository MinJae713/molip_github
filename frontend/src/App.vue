<script setup lang="ts">
import { RouterView } from 'vue-router';
import { onMounted } from 'vue';
import { useUserStore } from '@/stores/user';

const userStore = useUserStore();

onMounted(async () => {
  await userStore.fetchUserInfo();
});
</script>

<template>
  <RouterView />
</template>

<style>
/* 전역 스타일이 필요하면 여기에 작성 */
body {
  margin: 0;
  font-family: 'Noto Sans KR', sans-serif;
}
</style>