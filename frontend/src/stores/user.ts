import { ref, computed } from "vue";
import { defineStore } from "pinia";
import api from "@/api";

import type { User } from "@/types";

export const useUserStore = defineStore("user", () => {
  const user = ref<User>({
    memberId: 0,
    name: "",
    email: "",
    profileImageUrl: "",
  });

  // 데이터 로딩 여부 (선택 사항)
  const isLoaded = ref(false);
  // JWT 파싱기
  function parseJwt(token: string) {
    try {
      const parts = token.split(".");
      if (parts.length < 2) return null; // 길이 체크 추가
      const base64Url = parts[1];
      if (!base64Url) return null; // 존재 여부 체크
      const base64 = base64Url.replace(/-/g, "+").replace(/_/g, "/");
      const jsonPayload = decodeURIComponent(
        window
          .atob(base64)
          .split("")
          .map(function (c) {
            return "%" + ("00" + c.charCodeAt(0).toString(16)).slice(-2);
          })
          .join(""),
      );
      return JSON.parse(jsonPayload);
    } catch (e) {
      return null;
    }
  }
  // 사용자 정보 가져오기 (새로고침 시)
  async function fetchUserInfo() {
    if (isLoaded.value) return;

    const token = localStorage.getItem("accessToken");

    // 토큰이 없으면 굳이 API를 호출하지 않고 종료
    if (!token) {
      isLoaded.value = true;
      return;
    }

    try {
      // 1. 서버에 내 정보 요청 (토큰은 보통 interceptor에서 자동으로 담긴다고 가정)
      const response = await api.get(`/members/me`);
      // Axios 기준 응답 데이터 추출
      const userData = response.data;
      // 2. 데이터 검증 (백엔드 구조에 따라 userData.data 등으로 수정 필요할 수 있음)
      if (!userData || !userData.id) {
        console.warn(
          "API 응답에 memberId가 없습니다. 구조를 확인하세요:",
          userData,
        );
        throw new Error("Invalid User Data");
      }
      // 3. 상태 업데이트
      user.value = {
        memberId: userData.id,
        name: userData.name,
        email: userData.email,
        profileImageUrl: userData.profileImageUrl,
      };
      isLoaded.value = true;
    } catch (error) {
      // 401 에러(토큰 만료 등) 시 처리 로직을 추가하면 더 좋습니다.
      console.error("유저 정보 조회 실패:", error);
      // 에러 발생 시 무한 루프 방지를 위해 로딩 완료 처리
      isLoaded.value = true;
      user.value = {
        memberId: 0,
        name: "",
        email: "",
        profileImageUrl: "",
      };
    }
  }
  // 쿠키 설정
  function setCookie(name: string, value: string, days: number) {
    let expires = "";
    if (days) {
      const date = new Date();
      date.setTime(date.getTime() + days * 24 * 60 * 60 * 1000);
      expires = "; expires=" + date.toUTCString();
    }
    document.cookie = name + "=" + (value || "") + expires + "; path=/";
  }
  // 로그인
  async function login(email: string, pass: string) {
    try {
      const response = await api.post("/members/login", {
        email: email,
        password: pass,
      });

      const {
        accessToken,
        memberId,
        displayName,
        email: userEmail,
        profileImageUrl,
      } = response.data;
      // LocalStorage 저장
      localStorage.setItem("accessToken", accessToken);
      // Cookie 저장
      setCookie("accessToken", accessToken, 1); // 1일 유지
      // 3. 상태 업데이트
      user.value = {
        memberId: memberId,
        name: displayName,
        email: userEmail,
        profileImageUrl: profileImageUrl || "", // null일 경우 빈 문자열
      };

      isLoaded.value = true;
      return true;
    } catch (error) {
      console.error("Login Failed:", error);
      return false;
    }
  }

  // 로그아웃
  function logout() {
    user.value = {
      memberId: 0,
      name: "",
      email: "",
      profileImageUrl: "",
    };
    isLoaded.value = false;
    // 토큰 삭제
    localStorage.removeItem("accessToken");
    // 쿠키 삭제
    document.cookie =
      "accessToken=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;";

    window.location.href = "/";
  }

  const isAuthenticated = computed(() => !!user.value.memberId);

  return { user, fetchUserInfo, login, logout, isAuthenticated };
});
