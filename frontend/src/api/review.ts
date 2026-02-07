import api from "./index";

export interface DataItem {
    timeline: number;
    empty: boolean;
    concentrateValue: number;
    fever: boolean;
    emotionIndex: number;
    emotionName: string;
}

export interface PauseElement {
    startTime: number;
    endTime: number;
}

export interface ApiResponse {
    deadzonePercent: number;
    pauseElementList: PauseElement[];
    learnerTimelineData: DataItem[];
    trainerTimelineData: DataItem[];
}

export interface MoodGraphDataItem {
    index: number;
    name: string; // Changed from emotion to name to match API
    value: number;
}

export interface MoodGraph {
    learnerEmotionGraph: MoodGraphDataItem[];
    trainerEmotionGraph: MoodGraphDataItem[];
}

export interface ScriptDataItem {
    timeline: number;
    message: string;
}

// API Response Type
export type ChatData = ChatDataItem[];

export interface ChatDataItem {
    timestamp: string;
    content: string;
    senderId: number;
}

export interface QuizData {
    exitQuizId: number;
    question: string;
    answerNumber: number;
    exitQuizAnswers: QuizAnswerData[];
}

export interface QuizAnswerData {
    exitQuizAnswerId: number;
    number: number;
    answer: string;
}

export const fetchReviewData = async (sessionId: string | number, memberId: string | number): Promise<ApiResponse> => {
    // 엔드포인트는 실제 백엔드 명세에 따라 수정 필요할 수 있음
    // 예: /sessions/{sessionId}/analysis 또는 /analysis/{sessionId}
    const response = await api.get<ApiResponse>(`/review/timeline/learner/${sessionId}`);
    // const response = await api.post<ApiResponse>(`/review/learner/${sessionId}/${memberId}`);

    // axios response.data가 ApiResponse 구조를 가진다고 가정
    return response.data;
};

export const fetchScriptData = async (sessionId: string | number): Promise<ScriptDataItem[]> => {
    // 엔드포인트는 실제 백엔드 명세에 따라 수정 필요할 수 있음
    // 예: /sessions/{sessionId}/analysis 또는 /analysis/{sessionId}
    const response = await api.get<ScriptDataItem[]>(`/review/script/${sessionId}`);

    return response.data;
};

export const fetchMoodData = async (sessionId: string | number): Promise<MoodGraph> => {
    // 엔드포인트는 실제 백엔드 명세에 따라 수정 필요할 수 있음
    // 예: /sessions/{sessionId}/analysis 또는 /analysis/{sessionId}
    const response = await api.get<MoodGraph>(`/review/emotion-graph/learner/${sessionId}`);

    // axios response.data가 ApiResponse 구조를 가진다고 가정
    return response.data;
};

export const fetchChatData = async (sessionId: string | number, memberId: string | number): Promise<ChatData> => {
    // 엔드포인트는 실제 백엔드 명세에 따라 수정 필요할 수 있음
    // 예: /sessions/{sessionId}/analysis 또는 /analysis/{sessionId}
    const response = await api.get<ChatData>(`/chatting/qna-room/${sessionId}/${memberId}`);

    // axios response.data가 ApiResponse 구조를 가진다고 가정
    return response.data;
};

export const fetchQuizData = async (sessionId: string | number) => {
    const response = await api.get<QuizData[]>(`/quiz/${sessionId}/exit`);

    return response.data;
}

export interface AiQuizResponse {
    question: string;
    answer1: string;
    answer2: string;
    answer3: string;
    answer4: string;
    correctAnswer: number;
    explanation: string;
}

export const fetchAiQuiz = async (sessionId: string | number, timeline: number) => {
    const textLength = 130;
    // URL: /quiz/db?session-id=...&timeline=...&text-length=130
    const response = await api.get<AiQuizResponse>(`/quiz/db`, {
        params: {
            'session-id': sessionId,
            'timeline': Math.floor(timeline),
            'text-length': textLength
        }
    });

    return response.data;
};
