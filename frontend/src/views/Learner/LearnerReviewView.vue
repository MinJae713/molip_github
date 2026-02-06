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
                            :data="TimelineData"
                            :average-data="AverageData"
                            :rest-periods="RestPeriods"
                            :emotion-data="EmotionData"
                            :deadzone-threshold="deadzoneThreshold"
                            @jump-to-time="handleJumpToTime" 
                        />
                    </div>
                </div>
                <!-- 사이드바 -->
                <ReviewSideBar class="w-full lg:w-[340px] xl:w-[380px]" 
                    :personal-mood-data="PersonalMood"
                    :class-mood-data="ClassMood"
                    :script-data="ScriptData"
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

//  Data Load & Processing
// import apiResponse from '../../../updata/up_data.json';
import { fetchReviewData, fetchMoodData, fetchScriptData, fetchChatData, type DataItem, type MoodGraphDataItem, type ScriptDataItem, type ChatDataItem } from '@/api/review';

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
const RestPeriods = ref<{ start: number, end: number }[]>([]);

// 집중도 데드존 기준값
const deadzoneThreshold = ref(30);

// 타임라인용 샘플 데이터 (내 집중도)
const TimelineData = ref<{ minute: number, value: number, empty?: boolean, fever?: boolean }[]>([]);

// 타임라인용 샘플 데이터 (클래스 평균)
const AverageData = ref<{ minute: number, value: number }[]>([]);

// 무드 차트용 개인 데이터
const PersonalMood = ref([0, 0, 0, 0, 0, 0, 0]);
// 무드 차트용 클래스 데이터
const ClassMood = ref([0, 0, 0, 0, 0, 0, 0]);

// 스크립트 데이터
const ScriptData = ref<ScriptDataItem[]>([]);

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
        connectStomp();
    }
});

const EmotionData = ref<{ start: number, end: number, type: 'happy' | 'surprise' | 'fear' | 'disgust' | 'sad' | 'anger' | 'neutral', count?: number }[]>([]);

// Data Processing Helper
function processTimelineData(data: DataItem[]) {
    // 1. Process Timeline (Concentration)
    // 1. Process Timeline (Concentration)
    const timeline: { minute: number, value: number, empty?: boolean, fever?: boolean }[] = [];
    data.forEach((item: DataItem) => {
        timeline.push({
            minute: item.timeline / 60,
            value: item.concentrateValue,
            empty: item.empty,
            fever: item.fever
        });
    });

    // 2. Process Emotion Distribution (for Radar Chart)
    const emotionCounts = {
        happy: 0, surprise: 0, fear: 0, disgust: 0, sad: 0, anger: 0, neutral: 0
    };
    let totalValid = 0;

    data.forEach((item: DataItem) => {
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

// 3. Process Emotion Segments for Timeline (1분 단위 집계 + 중요 감정 우선)
function processEmotionSegments(data: DataItem[]) {
    // Explicitly define the return type to match the union type expected by the component
    const segments: { start: number, end: number, type: 'happy' | 'surprise' | 'fear' | 'disgust' | 'sad' | 'anger' | 'neutral', count: number }[] = [];
    
    if (!data || data.length === 0) return segments;

    const DURATION_PER_BLOCK = 60; // 1분 단위로 집계
    const totalSeconds = data.length;
    
    // 데이터를 1분 단위로 순회
    for (let i = 0; i < totalSeconds; i += DURATION_PER_BLOCK) {
        const chunk = data.slice(i, i + DURATION_PER_BLOCK);
        if (chunk.length === 0) continue;

        // 감정 빈도 계산
        const counts: Record<string, number> = {};
        let nonNeutralTotal = 0;

        chunk.forEach(item => {
            let emotion = item.emotionName || 'neutral';
            if (emotion === 'angry') emotion = 'anger';
            
            counts[emotion] = (counts[emotion] || 0) + 1;
            
            if (emotion !== 'neutral') {
                nonNeutralTotal++;
            }
        });

        // 대표 감정 선정 로직
        // 1. 중립이 아닌 감정들의 합이 전체의 30% 이상이면, 중립 제외 최빈값 선택
        // 2. 그렇지 않으면, 전체 최빈값 선택
        const THRESHOLD_RATIO = 0.3;
        const totalInChunk = chunk.length;
        let dominantEmotion = 'neutral';
        let dominantCount = 0;

        if (nonNeutralTotal / totalInChunk >= THRESHOLD_RATIO) {
            // 중립 제외하고 가장 빈도 높은 감정 찾기
            let maxCount = -1;
            Object.entries(counts).forEach(([emotion, count]) => {
                if (emotion !== 'neutral' && count > maxCount) {
                    maxCount = count;
                    dominantEmotion = emotion;
                }
            });
            dominantCount = maxCount;
        } else {
            // 전체에서 가장 빈도 높은 감정 찾기
            let maxCount = -1;
            Object.entries(counts).forEach(([emotion, count]) => {
                if (count > maxCount) {
                    maxCount = count;
                    dominantEmotion = emotion;
                }
            });
            dominantCount = maxCount;
        }

        // 세그먼트 추가 (Type assertion needed)
        segments.push({
            start: i / 60, // 분 단위
            end: (i + chunk.length) / 60,
            type: dominantEmotion as any,
            count: dominantCount
        });
    }

    return segments;
}

async function loadReviewData(sessionId: string) {
    try {
        if (!userStore.user.memberId) {
            console.warn("[Review] No memberId found, attempting to fetch user info...");
            await userStore.fetchUserInfo();
        }
        
        let memberId: string | number | undefined = userStore.user.memberId;
        if (!memberId) {
             console.warn("[Review] No memberId in store, using fallback '10002'");
        }
        
        console.log(`[Review] Fetching data for session: ${sessionId}, member: ${memberId}`);

        const response = await fetchReviewData(sessionId, memberId);
        console.log("[Review] Raw API Response:", response);
        
        if (response) {
            console.log("[Review] Response Keys:", Object.keys(response));
        }

        // Response format assumed: { learnerTimelineData: [], trainerTimelineData: [] }
         // 주의: 백엔드에서 'learner'(오타)로 오는지 'learner'(정상)로 오는지 확인 필요
        const learnerData = response.learnerTimelineData || (response as any).learnerTimelineData || [];
        const trainerData = response.trainerTimelineData || [];
        const { deadzonePercent, pauseElementList } = response;
        
        console.log("[Review] Learner Data (Checked typo):", learnerData);
        console.log("[Review] Destructured Data - trainerTimelineData:", trainerData);
        console.log("[Review] Destructured Data - deadzonePercent:", deadzonePercent);
        console.log("[Review] Destructured Data - pauseElementList:", pauseElementList);

        // 0. Set Deadzone Threshold & Rest Periods
        if (typeof deadzonePercent === 'number') {
            deadzoneThreshold.value = deadzonePercent;
        }

        if (pauseElementList && Array.isArray(pauseElementList)) {
            RestPeriods.value = pauseElementList.map((p: any) => ({
                start: p.startTime / 60, // 초 -> 분 변환
                end: p.endTime / 60
            }));
        }

        // Process Learner Data
        if (learnerData && learnerData.length > 0) {
            const learnerProcessed = processTimelineData(learnerData);
            console.log("[Review] Processed learner Data:", learnerProcessed);

            TimelineData.value = learnerProcessed.timeline;
            PersonalMood.value = learnerProcessed.moodDistribution;
            
            // Emotion segments for timeline
            const segments = processEmotionSegments(learnerData);
            console.log("[Review] learner Emotion Segments:", segments);
            EmotionData.value = segments;
        } else {
            console.warn("[Review] No learnerTimelineData found.");
        }

        // Process Trainer Data (Class Average)
        if (trainerData && trainerData.length > 0) {
            const trainerProcessed = processTimelineData(trainerData);
            console.log("[Review] Processed Trainer Data:", trainerProcessed);

            AverageData.value = trainerProcessed.timeline;
            ClassMood.value = trainerProcessed.moodDistribution;
        } else {
            console.warn("[Review] No trainerTimelineData found.");
        }

        const moodData = await fetchMoodData(1);
        console.log("[Review] Mood Data:", moodData);

        if (moodData) {
            // API Response: { learnerEmotionGraph: [], trainerEmotionGraph: [] }
            // Each graph is array of { index, name, value }
            
            if (moodData.learnerEmotionGraph) {
                PersonalMood.value = processMoodGraphData(moodData.learnerEmotionGraph);
                console.log("[Review] Processed Personal Mood:", PersonalMood.value);
            }

            if (moodData.trainerEmotionGraph) {
                ClassMood.value = processMoodGraphData(moodData.trainerEmotionGraph);
                console.log("[Review] Processed Class Mood:", ClassMood.value);
            }
        }

        // Script Data 
        const scriptData = await fetchScriptData(1);
        console.log("[Review] Script Data:", scriptData);

        if (scriptData && Array.isArray(scriptData)) {
            ScriptData.value = scriptData.map((item: any) => ({
                timeline: item.timeline,
                message: item.message || item.script || "내용 없음"
            }));
            console.log("[Review] Processed Script Data:", ScriptData.value);
        } else {
             console.warn("[Review] Script data is empty or invalid structure", scriptData);
        }

        const chatDataResponse = await fetchChatData(1, 2);
        console.log("[Review] Chat Data:", chatDataResponse);


        if (chatDataResponse && Array.isArray(chatDataResponse)) {
            // QnA 데이터 처리
            // senderId === 0 : AI 답변
            // 그 외 : 사용자 질문

            const mappedChat: any[] = [];
            
            // 시간순 정렬 보장
            chatDataResponse.sort((a, b) => new Date(a.timestamp).getTime() - new Date(b.timestamp).getTime());

            chatDataResponse.forEach((item, index) => {
                const isAnswer = item.senderId === 0;
                
                // 각 메시지를 독립적인 객체로 매핑하지만, UI에서 그룹화하기 쉽도록 구조화
                // 실제 질문 ID가 없으므로 임시 ID 생성
                // 답변은 직전 질문에 종속적이라고 가정하거나, 시간 흐름대로 나열
                
                const msgObj = {
                    id: `msg-${index}-${Date.now()}`, // Unique ID for key
                    questionId: isAnswer ? null : `q-${index}`, // 질문일 경우 ID 부여
                    content: item.content,
                    senderName: isAnswer ? 'AI Tutor' : (userStore.user.name || '나'),
                    timestamp: item.timestamp,
                    createdAt: item.timestamp,
                    type: isAnswer ? 'answer' : 'question',
                    senderId: item.senderId
                };
                
                mappedChat.push(msgObj);
            });
            
            console.log("[Review] Mapped Chat Data:", mappedChat);
            qaList.value = mappedChat;
        }

    } catch (error) {
        console.error("[Review] Failed to load review data:", error);
    }
}

const lastQuestionId = ref<number | string | null>(null);

// --- WebSocket 연결 ---
const connectStomp = () => {
    const sessionId = route.params.sessionId as string;
    const memberId = userStore.user.memberId; // Fallback for testing

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
    const memberId = userStore.user.memberId;

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