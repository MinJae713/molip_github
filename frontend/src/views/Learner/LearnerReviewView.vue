<template>
    <div class="fixed inset-0 z-50 bg-white flex flex-col font-sans">
        <!-- 하드코딩된 제목 전달 -->
        <p>111 {{ sessionStore.currentSessionInfo[0].title }}</p>
        <ReviewHeader :title="sessionStore.currentSessionInfo[0].title" session-number="01" @close="$emit('close')" />
        <main class="flex-1 overflow-hidden bg-white">
            <div class="h-full flex flex-col lg:flex-row">
                <!-- 메인 콘텐츠 -->
                <div class="flex-1 overflow-y-auto p-4 custom-scrollbar">
                    <div class="max-w-6xl mx-auto space-y-4">
                        <ReviewVideoPlayer
                            thumbnail-url="https://lh3.googleusercontent.com/aida-public/AB6AXuBiolsf6prQfZQmoZ420xEZd441M2OqmX3ZkCgcZz6CiNM7dWL1j5LkuMWr7C98MkSlOn5khaGV_o6iP8HvgU5kV1ulrcFZ4M_s4sPvoltW4goOg9cU1JZ5MwNh5Jqp3a6O4sh35Gt_K8JOaiejQAWSGcZep5eH2o3TBxeCCemLid8l3JlLO5BXcjQhIR0LMMCRhZb7f4kbU6yd3jFL06u8vBS2gLEppd_BaZP7-ASlf6YuhAMP5EtdxZwJfdsRzYY0kqCpgq_Piw" />
                        <ConcentrationTimeline />
                    </div>
                </div>
                <!-- 사이드바 -->
                <ReviewSideBar class="w-full lg:w-[340px] xl:w-[380px]" />
            </div>
        </main>
    </div>
</template>
<script setup lang="ts">
// 컴포넌트
import ReviewHeader from '@/components/Learner/LearnerReviewView/ReviewHeader.vue';
import ReviewVideoPlayer from '@/components/Learner/LearnerReviewView/ReviewVideoPlayer.vue';
import ConcentrationTimeline from '@/components/Learner/LearnerReviewView/ConcentrationTimeline.vue';
import ReviewSideBar from '@/components/Learner/LearnerReviewView/ReviewSideBar/ReviewSideBar.vue';
//vue
import { onMounted } from 'vue';

// 라우터
import { useRoute } from 'vue-router';

// 상태 관리
import { useSessionStore } from '@/stores/session';
import { useWorkspaceStore } from '@/stores/workspace';

// 라우터
const route = useRoute();
// 상태 관리
const sessionStore = useSessionStore();
// props
defineProps<{ title: string }>();
defineEmits(['close']);
onMounted(async () => {
    const sessionId = route.params.sessionId as string;
    if (sessionId) {
        console.log(sessionId);
        await sessionStore.fetchSessionInfo(sessionId);
    }
});

</script>