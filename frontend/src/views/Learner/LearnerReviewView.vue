<template>
    <div class="fixed inset-0 z-50 bg-white flex flex-col font-sans">
        <ReviewHeader :title="sessionStore.currentSessionInfo?.title" session-number="01" @close="$emit('close')" />
        <main class="flex-1 bg-white min-h-0 relative z-0">
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
                    :script-data="mockScriptData"
                    :qa-list="qaList"
                    @jump-to-time="handleJumpToTime"
                    @send-message="handleSendMessage"
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
import { useUserStore } from '@/stores/user';

// 웹소켓
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

// Mock Data Load & Processing
// import apiResponse from '../../../mockupdata/mockup_data.json';
import { fetchReviewData, fetchMoodData, fetchScriptData, fetchChatData, type MockDataItem, type MoodGraphDataItem, type MockScriptDataItem, type ChatDataItem } from '@/api/review';

// 라우터
const route = useRoute();
// 상태 관리
// 상태 관리
const sessionStore = useLearnerSessionStore();
const userStore = useUserStore();
// props
defineProps<{ title: string }>();
defineEmits(['close']);

// 비디오 플레이어 참조
const playerRef = ref<any>(null);
const stompClient = ref<Client | null>(null);
const qaList = ref<any[]>([]);

// 임시 쉬는 시간 데이터 (15분~20분, 45분~50분)
const mockRestPeriods = [
    { start: 15, end: 20 },
    { start: 45, end: 50 },
];

// 타임라인용 샘플 데이터 (내 집중도)
// 타임라인용 샘플 데이터 (내 집중도)
const mockTimelineData = ref<{ minute: number, value: number }[]>([]);

// 타임라인용 샘플 데이터 (클래스 평균)
const mockAverageData = ref<{ minute: number, value: number }[]>([]);

// 무드 차트용 개인 데이터
const mockPersonalMood = ref([0, 0, 0, 0, 0, 0, 0]);
// 무드 차트용 클래스 데이터
const mockClassMood = ref([0, 0, 0, 0, 0, 0, 0]);

// 스크립트 데이터
const mockScriptData = ref<MockScriptDataItem[]>([]);

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
        await loadReviewData("10002");
        connectStomp();
    }
});

const mockEmotionData = ref<{ start: number, end: number, type: 'happy' | 'surprise' | 'fear' | 'disgust' | 'sad' | 'anger' | 'neutral' }[]>([]);

// Data Processing Helper
function processTimelineData(data: MockDataItem[]) {
    // 1. Process Timeline (Concentration)
    const timeline: { minute: number, value: number }[] = [];
    data.forEach((item: MockDataItem) => {
        timeline.push({
            minute: item.timeline / 60,
            value: item.concentrateValue
        });
    });

    // 2. Process Emotion Distribution (for Radar Chart)
    const emotionCounts = {
        happy: 0, surprise: 0, fear: 0, disgust: 0, sad: 0, anger: 0, neutral: 0
    };
    let totalValid = 0;

    data.forEach((item: MockDataItem) => {
        let name = item.emotionName;
        if (name === 'angry') name = 'anger';
        
        if (name && name in emotionCounts) {
            emotionCounts[name as keyof typeof emotionCounts]++;
            totalValid++;
        }
    });

    const moodDistribution = totalValid > 0 ? [
        emotionCounts.happy / totalValid,
        emotionCounts.surprise / totalValid,
        emotionCounts.fear / totalValid,
        emotionCounts.disgust / totalValid,
        emotionCounts.sad / totalValid,
        emotionCounts.anger / totalValid,
        emotionCounts.neutral / totalValid
    ] : [0, 0, 0, 0, 0, 0, 0];

    return { timeline, moodDistribution };
}

// 2. Process Mood Graph Data (Radar Chart)
// Helper to normalize emotion graph data to [happy, surprise, fear, disgust, sad, anger, neutral] order
function processMoodGraphData(graphData: MoodGraphDataItem[]): number[] {
    if (!graphData || graphData.length === 0) return [0, 0, 0, 0, 0, 0, 0];

    // Initialize map
    const emotionMap: Record<string, number> = {
        happy: 0,
        surprise: 0,
        fear: 0,
        disgust: 0,
        sad: 0,
        angry: 0, // API returns 'angry'
        anger: 0, // Fallback
        neutral: 0
    };

    graphData.forEach(item => {
        // Safety check
        if (!item || !item.name) return;

        const name = item.name.toLowerCase();
        if (emotionMap[name] !== undefined) {
             emotionMap[name] += item.value;
        }
    });

    // Handle 'angry' -> 'anger' key consolidation if needed for our array map
    const angerVal = (emotionMap.angry || 0) + (emotionMap.anger || 0);

    // Normalize to 0~1 range if values are > 1 (assuming percentage like 60.5)
    // We will cap at 100 for normalization base.
    // Adjusted: Divide by 60 instead of 100 to make the shape appear larger (zoom in)
    const normalize = (val: number) => Math.min(val / 60, 1);

    return [
        normalize(emotionMap.happy || 0),
        normalize(emotionMap.surprise || 0),
        normalize(emotionMap.fear || 0),
        normalize(emotionMap.disgust || 0),
        normalize(emotionMap.sad || 0),
        normalize(angerVal),
        normalize(emotionMap.neutral || 0)
    ];
}

// 3. Process Emotion Segments (Timeline bars) - Only for Learner
function processEmotionSegments(data: MockDataItem[]) {
    const segments: { start: number, end: number, type: 'happy' | 'surprise' | 'fear' | 'disgust' | 'sad' | 'anger' | 'neutral' }[] = [];
    if (!data || data.length === 0 || !data[0]) return segments;

    const firstItem = data[0];
    const lastItem = data[data.length - 1];
    
    let currentType = firstItem.emotionName;
    if (currentType === 'angry') currentType = 'anger';

    let startTime = firstItem.timeline / 60;
    const maxTime = lastItem?.timeline || 0;

    for (let i = 1; i < data.length; i++) {
        const item = data[i];
        if (!item) continue;
        
        let type = item.emotionName;
        if (type === 'angry') type = 'anger'; 

        const prevItem = data[i-1];
        let prevType = prevItem ? prevItem.emotionName : currentType;
        if (prevType === 'angry') prevType = 'anger';

        if (type !== prevType) {
            const endTime = item.timeline / 60;
            segments.push({
                start: startTime,
                end: endTime,
                type: prevType as any
            });
            currentType = type;
            startTime = endTime;
        }
    }
    
    segments.push({
        start: startTime,
        end: maxTime / 60,
        type: currentType as any
    });
    
    return segments;
}

async function loadReviewData(sessionId: string) {
    try {
        if (!userStore.user.memberId) {
            console.warn("[Review] No memberId found, attempting to fetch user info...");
            await userStore.fetchUserInfo();
        }
        
        const memberId = "10002";
        console.log(`[Review] Fetching data for session: ${sessionId}, member: ${memberId}`);
        
        if (!memberId) {
            console.error("[Review] Failed to get memberId even after fetch.");
            return;
        }

        const response = await fetchReviewData(sessionId, memberId);
        console.log("[Review] Raw API Response:", response);
        
        if (response) {
            console.log("[Review] Response Keys:", Object.keys(response));
        }

        // Response format assumed: { learnerTimelineData: [], trainerTimelineData: [] }
         // 주의: 백엔드에서 'leaner'(오타)로 오는지 'learner'(정상)로 오는지 확인 필요
        const { learnerTimelineData, trainerTimelineData } = response;
        console.log("[Review] Destructured Data - learnerTimelineData:", learnerTimelineData);
        console.log("[Review] Destructured Data - trainerTimelineData:", trainerTimelineData);

        // Process Learner Data
        if (learnerTimelineData && learnerTimelineData.length > 0) {
            const leanerProcessed = processTimelineData(learnerTimelineData);
            console.log("[Review] Processed Leaner Data:", leanerProcessed);

            mockTimelineData.value = leanerProcessed.timeline;
            mockPersonalMood.value = leanerProcessed.moodDistribution;
            
            // Emotion segments for timeline
            const segments = processEmotionSegments(learnerTimelineData);
            console.log("[Review] Leaner Emotion Segments:", segments);
            mockEmotionData.value = segments;
        } else {
            console.warn("[Review] No learnerTimelineData found.");
        }

        // Process Trainer Data (Class Average)
        if (trainerTimelineData && trainerTimelineData.length > 0) {
            const trainerProcessed = processTimelineData(trainerTimelineData);
            console.log("[Review] Processed Trainer Data:", trainerProcessed);

            mockAverageData.value = trainerProcessed.timeline;
            mockClassMood.value = trainerProcessed.moodDistribution;
        } else {
            console.warn("[Review] No trainerTimelineData found.");
        }

        const moodData = await fetchMoodData(1);
        console.log("[Review] Mood Data:", moodData);

        if (moodData) {
            // API Response: { learnerEmotionGraph: [], trainerEmotionGraph: [] }
            // Each graph is array of { index, name, value }
            
            if (moodData.learnerEmotionGraph) {
                mockPersonalMood.value = processMoodGraphData(moodData.learnerEmotionGraph);
                console.log("[Review] Processed Personal Mood:", mockPersonalMood.value);
            }

            if (moodData.trainerEmotionGraph) {
                mockClassMood.value = processMoodGraphData(moodData.trainerEmotionGraph);
                console.log("[Review] Processed Class Mood:", mockClassMood.value);
            }
        }

        // Script Data 
        const scriptData = await fetchScriptData(1);
        console.log("[Review] Script Data:", scriptData);

        if (scriptData && Array.isArray(scriptData)) {
            mockScriptData.value = scriptData.map((item: any) => ({
                timeline: item.timeline,
                message: item.message || item.script || "내용 없음"
            }));
            console.log("[Review] Processed Script Data:", mockScriptData.value);
        } else {
             console.warn("[Review] Script data is empty or invalid structure", scriptData);
        }

        const chatData = await fetchChatData(1, 2);
        console.log("[Review] Chat Data:", chatData);

    } catch (error) {
        console.error("[Review] Failed to load review data:", error);
    }
}

const lastQuestionId = ref<number | string | null>(null);

// --- WebSocket 연결 ---
const connectStomp = () => {
    const sessionId = route.params.sessionId as string;
    const memberId = userStore.user.memberId || "10002"; // Fallback for testing

    const socketUrl = import.meta.env.VITE_CHAT_SOCKET_URL;

    stompClient.value = new Client({
        webSocketFactory: () => new SockJS(socketUrl),
        connectHeaders: {
            Authorization: `Bearer ${localStorage.getItem('accessToken')}`
        },
        reconnectDelay: 5000,
        onConnect: () => {
            console.log('[Learner] WebSocket Connected ✅');

            // 1. 일반 채팅 구독 (방송용) - 현재 QnA 모드에서는 사용 안 할 수 있음
             stompClient.value?.subscribe(`/topic/chat-room/${sessionId}`, (res: any) => {
                // ... (Existing chat logic if needed, or ignore if pure QnA)
            });

            // 2. QnA room 구독 (AI 답변 수신)
            stompClient.value?.subscribe(`/topic/qna-room/${sessionId}/${memberId}`, (res: any) => {
                console.log('[Learner] QA Answer Message:', res.body);
                try {
                    const msg = JSON.parse(res.body);
                    if (msg) {
                        // AI 답변에는 questionId가 없으므로, 프론트에서 저장해둔 ID를 매핑
                        // 답변 메시지 처리
                        const answerMsg = {
                            ...msg,
                            questionId: lastQuestionId.value, // 질문했을 때 저장해둔 ID
                            type: 'answer', // UI에서 답변으로 인식하도록
                            senderName: 'AI Tutor', // 상세 정보가 문자로만 오면 이름 부여
                            profileImageUrl: null // AI 아이콘 표시용
                        };
                        
                        console.log('[Learner] QA Answer Mapped:', answerMsg);
                        qaList.value.push(answerMsg);
                    }
                } catch (e) {
                    console.error("[Learner] Failed to parse QA answer message:", e);
                }
            })
        },
        onStompError: (frame) => {
            console.error('[Learner] Broker error:', frame.headers['message']);
        },
        onWebSocketClose: () => {
            console.log('[Learner] Connection Closed ❌');
        }
    });

    stompClient.value.activate();
};

// 메시지 전송
const sendChatMessage = (content: string, type: string, questionId?: number | string | null) => {
    if (!content.trim() || !stompClient.value?.connected) return;
    const sessionId = route.params.sessionId as string;
    const memberId = userStore.user.memberId || "10002";

    if (type === 'question') {
        // 1. QnA 전송 (AI에게 질문)
        // 화면에 내 질문 즉시 표시 (낙관적 업데이트)
        const myQuestion = {
            questionId: questionId,
            content: content,
            senderName: userStore.user.name || '나',
            timestamp: new Date().toISOString(),
            type: 'question'
        };
        console.log('[Learner] My Question:', myQuestion);
        qaList.value.push(myQuestion);
        
        // 마지막 질문 ID 저장 (답변 매핑용)
        lastQuestionId.value = questionId || null;

        const payload = {
            content: content
            // Backend Record: QnaMessage(String content)
        };

        stompClient.value.publish({
            destination: `/app/qna-room/${sessionId}/${memberId}`,
            body: JSON.stringify(payload)
        });

    } else {
        // 2. 일반 채팅 (기존 로직 유지)
        const payload = {
            type,
            content,
            questionId: questionId || null
        };

        stompClient.value.publish({
            destination: `/app/chat-room/${sessionId}`,
            body: JSON.stringify(payload)
        });
    }
};

// 메시지 전송 핸들러 (Sidebar에서 emit됨)
const handleSendMessage = (content: string, type: string, questionId?: number | string | null) => {
    // SidebarChat에서 'question' 타입으로 보냄
    sendChatMessage(content, type, questionId);
};
</script>