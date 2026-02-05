import api from "./index";

export interface MockDataItem {
    timeline: number;
    empty: boolean;
    concentrateValue: number;
    fever: boolean;
    emotionIndex: number;
    emotionName: string;
}

export interface MockApiResponse {
    leanerTimelineData: MockDataItem[];
    TrainerTimelineData: MockDataItem[];
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

export interface MockScriptDataItem {
    timeline: number;
    message: string;
}

export interface MockScriptResponse {
    scriptData: MockScriptDataItem[];
}

export const fetchReviewData = async (sessionId: string | number, memberId: string | number): Promise<MockApiResponse> => {
    // 엔드포인트는 실제 백엔드 명세에 따라 수정 필요할 수 있음
    // 예: /sessions/{sessionId}/analysis 또는 /analysis/{sessionId}
    const response = await api.post<MockApiResponse>(`/review/test/${sessionId}/${memberId}`);

    // axios response.data가 MockApiResponse 구조를 가진다고 가정
    return response.data;
};

export const fetchScriptData = async (sessionId: string | number): Promise<MockScriptResponse> => {
    // 엔드포인트는 실제 백엔드 명세에 따라 수정 필요할 수 있음
    // 예: /sessions/{sessionId}/analysis 또는 /analysis/{sessionId}
    const response = await api.get<MockScriptResponse>(`/review/script/${sessionId}/test`);

    // axios response.data가 MockApiResponse 구조를 가진다고 가정
    return response.data;
};

export const fetchMoodData = async (sessionId: string | number): Promise<MoodGraph> => {
    // 엔드포인트는 실제 백엔드 명세에 따라 수정 필요할 수 있음
    // 예: /sessions/{sessionId}/analysis 또는 /analysis/{sessionId}
    const response = await api.get<MoodGraph>(`/review/emotion-graph/learner/${sessionId}`);

    // axios response.data가 MockApiResponse 구조를 가진다고 가정
    return response.data;
};
