import { defineStore } from "pinia";
import { ref } from "vue";
import api from "@/api";

/**
 * 초대코드 단건 정보
 */
export interface JoinCode {
  joinCodeId: number;
  identifyValue: string; // 실제 초대 코드 값
  joinCodeName: string;  // 관리용 이름
  endedDate: string;     // 만료일
  createdAt?: string;
}

/**
 * 초대코드 생성 요청 payload
 */
export interface CreateJoinCodePayload {
  workspaceId: number;
  endedDate: string;
  joinCodeName: string;
}

export const useAdminJoinCodeStore = defineStore("adminJoinCode", () => {
  /* =======================
   * STATE
   * ======================= */

  /** 워크스페이스 초대코드 목록 */
  const joinCodes = ref<JoinCode[]>([]);

  /** 공통 로딩 상태 */
  const isLoading = ref(false);

  /** 사용자에게 보여줄 에러 메시지 */
  const error = ref<string | null>(null);

  /* =======================
   * ACTIONS
   * ======================= */

  /**
   * 초대코드 목록 조회 (ADMIN 전용)
   */
  const fetchJoinCodes = async (workspaceId: number) => {
    isLoading.value = true;
    error.value = null;

    try {
      const { data } = await api.get<JoinCode[]>(
        `/workspaces/${workspaceId}/joinCode`
      );

      joinCodes.value = data;
    } catch (e) {
      console.error("초대코드 목록 조회 실패", e);

      error.value = "초대코드 목록을 불러오지 못했습니다.";
      throw e; // 👉 403 포함, View에서 판단하도록 위임
    } finally {
      isLoading.value = false;
    }
  };

  /**
   * 초대코드 생성 응답 (identifyValue만 반환됨)
   */
  interface CreateJoinCodeResponse {
    identifyValue: string;
  }

  /**
   * 초대코드 생성
   * - 생성 후 목록을 다시 조회하는 방식 사용
   */
  const createJoinCode = async (payload: CreateJoinCodePayload): Promise<CreateJoinCodeResponse> => {
    isLoading.value = true;
    error.value = null;

    try {
      // API 스펙에 맞춰 날짜 형식이 필요하다면 변환 (사용자가 준 예시는 ISO String)
      // payload.endedDate가 "YYYY-MM-DD"라면 이를 해당 일의 23:59:59로 설정하여 전송
      const date = new Date(payload.endedDate);
      date.setHours(23, 59, 59, 999);

      const formattedPayload = {
        ...payload,
        endedDate: date.toISOString()
      };

      const { data } = await api.post<CreateJoinCodeResponse>(
        "/workspaces/joinCode",
        formattedPayload
      );

      // 생성 후 목록 동기화
      await fetchJoinCodes(payload.workspaceId);

      return data;
    } catch (e) {
      console.error("초대코드 생성 실패", e);

      error.value = "초대코드 생성에 실패했습니다.";
      throw e;
    } finally {
      isLoading.value = false;
    }
  };

  /**
   * 초대코드 삭제
   */
  const deleteJoinCode = async (
    joinCodeId: number,
    workspaceId: number
  ) => {
    isLoading.value = true;
    error.value = null;

    try {
      await api.delete(`/workspaces/joinCode/${joinCodeId}`);

      // 삭제 후 목록 재조회
      await fetchJoinCodes(workspaceId);
    } catch (e) {
      console.error("초대코드 삭제 실패", e);

      error.value = "초대코드 삭제에 실패했습니다.";
      throw e;
    } finally {
      isLoading.value = false;
    }
  };

  /**
   * 페이지 이탈 시 상태 초기화
   */
  const clear = () => {
    joinCodes.value = [];
    error.value = null;
    isLoading.value = false;
  };

  return {
    // state
    joinCodes,
    isLoading,
    error,

    // actions
    fetchJoinCodes,
    createJoinCode,
    deleteJoinCode,
    clear,
  };
});
