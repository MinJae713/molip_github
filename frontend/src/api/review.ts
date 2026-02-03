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
    message: MockDataItem[];
}

export const fetchReviewData = async (sessionId: string | number): Promise<MockApiResponse> => {
    // 엔드포인트는 실제 백엔드 명세에 따라 수정 필요할 수 있음
    // 예: /sessions/{sessionId}/analysis 또는 /analysis/{sessionId}
    const response = await api.get<{ message: MockDataItem[] }>(`/analysis/sessions/${sessionId}`);

    // axios response.data가 MockApiResponse 구조를 가진다고 가정
    return response.data;
};
