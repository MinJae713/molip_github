<template>
    <div class="fixed inset-0 z-50 bg-white flex flex-col font-sans">
        <ReviewHeader :title="sessionStore.currentSessionInfo?.title" session-number="01" @close="$emit('close')" />
        <main class="flex-1 overflow-hidden bg-white">
            <div class="h-full flex flex-col lg:flex-row">
                <!-- 메인 콘텐츠 -->
                <div class="flex-1 overflow-y-auto p-4 custom-scrollbar">
                    <div class="max-w-6xl mx-auto space-y-4">
                        <ReviewVideoPlayer ref="playerRef"
                            thumbnail-url="https://lh3.googleusercontent.com/aida-public/AB6AXuBiolsf6prQfZQmoZ420xEZd441M2OqmX3ZkCgcZz6CiNM7dWL1j5LkuMWr7C98MkSlOn5khaGV_o6iP8HvgU5kV1ulrcFZ4M_s4sPvoltW4goOg9cU1JZ5MwNh5Jqp3a6O4sh35Gt_K8JOaiejQAWSGcZep5eH2o3TBxeCCemLid8l3JlLO5BXcjQhIR0LMMCRhZb7f4kbU6yd3jFL06u8vBS2gLEppd_BaZP7-ASlf6YuhAMP5EtdxZwJfdsRzYY0kqCpgq_Piw" />
                        <ConcentrationTimeline 
                            :data="mockTimelineData"
                            :average-data="mockAverageData"
                            :rest-periods="mockRestPeriods"
                            :emotion-data="mockEmotionData"
                            @jump-to-time="handleJumpToTime" 
                        />
                    </div>
                </div>
                <!-- 사이드바 -->
                <ReviewSideBar class="w-full lg:w-[340px] xl:w-[380px]" 
                    :personal-mood-data="mockPersonalMood"
                    :class-mood-data="mockClassMood"
                />
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
import { onMounted, ref } from 'vue';

// 라우터
import { useRoute } from 'vue-router';

// 상태 관리
import { useLearnerSessionStore } from '@/stores/learnerSession';
import { useWorkspaceStore } from '@/stores/workspace';

// 라우터
const route = useRoute();
// 상태 관리
const sessionStore = useLearnerSessionStore();
// props
defineProps<{ title: string }>();
defineEmits(['close']);

// 비디오 플레이어 참조
const playerRef = ref<any>(null);

// 임시 쉬는 시간 데이터 (15분~20분, 45분~50분)
const mockRestPeriods = [
    { start: 15, end: 20 },
    { start: 45, end: 50 },
];

// 타임라인용 샘플 데이터 (내 집중도)
const mockTimelineData = ref([
    { minute: 0, value: 50 },
    { minute: 5, value: 55 },
    { minute: 10, value: 70 },
    { minute: 15, value: 85 },
    { minute: 20, value: 60 },
    { minute: 25, value: 45 },
    { minute: 28, value: 20 }, // 데드존 진입
    { minute: 30, value: 15 }, // 저점
    { minute: 35, value: 25 },
    { minute: 38, value: 40 }, // 탈출
    { minute: 40, value: 75 },
    { minute: 45, value: 90 },
    { minute: 50, value: 80 },
    { minute: 55, value: 65 },
    { minute: 60, value: 55 },
]);

// 타임라인용 샘플 데이터 (클래스 평균)
const mockAverageData = [
    { minute: 0, value: 45 },
    { minute: 5, value: 50 },
    { minute: 10, value: 60 },
    { minute: 15, value: 65 },
    { minute: 20, value: 70 },
    { minute: 25, value: 65 },
    { minute: 30, value: 60 },
    { minute: 35, value: 55 },
    { minute: 40, value: 60 },
    { minute: 45, value: 70 },
    { minute: 50, value: 65 },
    { minute: 55, value: 60 },
    { minute: 60, value: 55 },
];

// 무드 차트용 개인 데이터
const mockPersonalMood = ref([0.8, 0.6, 0.4, 0.3, 0.2, 0.5, 0.7]);
// 무드 차트용 클래스 데이터
const mockClassMood = [0.5, 0.5, 0.6, 0.4, 0.4, 0.5, 0.5];

// 타임라인 점프 핸들러
const handleJumpToTime = (timeInSeconds: number) => {
    if (playerRef.value) {
        playerRef.value.seekTo(timeInSeconds);
    }
};

onMounted(async () => {
    const sessionId = route.params.sessionId as string;
    if (sessionId) {
        console.log(sessionId);
        await sessionStore.fetchSessionInfo(sessionId);
        await loadReviewData(sessionId);
    }
});

// Mock Data Load & Processing
// import apiResponse from '../../../mockupdata/mockup_data.json';
import { fetchReviewData, type MockDataItem } from '@/api/review';

const mockEmotionData = ref<{ start: number, end: number, type: 'happy' | 'surprise' | 'fear' | 'disgust' | 'sad' | 'anger' | 'neutral' }[]>([]);

async function loadReviewData(sessionId: string) {
    try {
        const response = await fetchReviewData(sessionId);
        const rawData = response.message;
        
        if (!rawData || rawData.length === 0) return;

        // 1. Process Timeline Data (Concentration)
        const processedTimeline: { minute: number, value: number }[] = [];
        const lastItem = rawData[rawData.length - 1];
        
        if (lastItem) {
            rawData.forEach((item: MockDataItem) => {
                processedTimeline.push({
                    minute: item.timeline / 60, // Convert seconds to minutes
                    value: item.concentrateValue
                });
            });
            
            mockTimelineData.value = processedTimeline;

            // 2. Process Emotion Segments
            const segments: { start: number, end: number, type: 'happy' | 'surprise' | 'fear' | 'disgust' | 'sad' | 'anger' | 'neutral' }[] = [];
            const firstItem = rawData[0];

            if (firstItem) {
                let currentType = firstItem.emotionName;
                if (currentType === 'angry') currentType = 'anger';

                let startTime = firstItem.timeline / 60;
                const maxTime = lastItem.timeline;

                for (let i = 1; i < rawData.length; i++) {
                    const item = rawData[i];
                    if (!item) continue;
                    
                    let type = item.emotionName;
                    if (type === 'angry') type = 'anger'; 

                    const prevItem = rawData[i-1];
                    let prevType = prevItem ? prevItem.emotionName : currentType;
                    if (prevType === 'angry') prevType = 'anger';

                    if (type !== prevType) {
                        // End current segment
                        const endTime = item.timeline / 60;
                        segments.push({
                            start: startTime,
                            end: endTime,
                            type: prevType as any
                        });
                        // Start new segment
                        currentType = type;
                        startTime = endTime;
                    }
                }
                // Add last segment
                segments.push({
                    start: startTime,
                    end: maxTime / 60,
                    type: currentType as any
                });
                
                mockEmotionData.value = segments;
            }

            // 3. Process Personal Mood (Radar Chart)
            const emotionCounts = {
                happy: 0, surprise: 0, fear: 0, disgust: 0, sad: 0, anger: 0, neutral: 0
            };
            let totalValid = 0;

            rawData.forEach((item: MockDataItem) => {
                let name = item.emotionName;
                if (name === 'angry') name = 'anger';
                
                if (name && name in emotionCounts) {
                    emotionCounts[name as keyof typeof emotionCounts]++;
                    totalValid++;
                }
            });

            if (totalValid > 0) {
                mockPersonalMood.value = [
                    emotionCounts.happy / totalValid,
                    emotionCounts.surprise / totalValid,
                    emotionCounts.fear / totalValid,
                    emotionCounts.disgust / totalValid,
                    emotionCounts.sad / totalValid,
                    emotionCounts.anger / totalValid,
                    emotionCounts.neutral / totalValid
                ];
            }
        }
    } catch (error) {
        console.error("Failed to load review data:", error);
    }
}


</script>