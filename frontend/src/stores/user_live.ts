import { ref, computed } from "vue";
import { defineStore } from "pinia";
import api from "@/api";

import type { User } from "@/types";

export const useUserStore = defineStore("user", () => {
  const user = ref<User>({
    member_id: 0,
    name: "",
    email: "",
    profile_image_url: "",
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

  // 목업용 가짜 jwt 생성기
  function createMockJwt(userId: number, role: string) {
    const header = btoa(JSON.stringify({ alg: "HS256", typ: "JWT" }));
    const payload = btoa(
      JSON.stringify({
        sub: userId,
        role: role,
        exp: Math.floor(Date.now() / 1000) + 60 * 60, // 1시간 만료
      }),
    );
    const signature = "mock-signature";
    return `${header}.${payload}.${signature}`;
  }

  // 사용자 정보 가져오기 (새로고침 시)
  async function fetchUserInfo() {
    if (isLoaded.value) return;

    const token = localStorage.getItem("accessToken");
    if (!token) {
      isLoaded.value = true;
      return;
    }

    try {
      // 토큰 파싱해서 ID 추출
      console.log("Token from localStorage:", token); // [Debug]
      const payload = parseJwt(token);
      console.log("Parsed Payload:", payload); // [Debug]

      // JWT payload에 memberId가 있으면 그것을 사용, 없으면 sub 사용
      const userId = payload?.memberId || payload?.sub;
      console.log("Extracted userId:", userId); // [Debug]

      if (!userId) {
        throw new Error("Invalid Token: No userId found");
      }

      // DB에서 최신 유저 정보 조회
      console.log(`Fetching info for userId: ${userId}`); // [Debug]
      const response = await api.get(`/members/${userId}`);
      console.log("User Info Response:", response.data); // [Debug]
      const userData = response.data;

      user.value = {
        member_id: userData.member_id,
        name: userData.name,
        email: userData.email,
        profile_image_url: userData.profile_image_url,
      };
      isLoaded.value = true;
    } catch (error) {
      console.error("Fetch User Info Failed:", error);
      // API 호출 실패 시 (토큰 만료, 서버 오류 등) 로그아웃 처리
      // 단, 네트워크 오류 등 일시적 문제일 경우를 구분할 필요가 있음
      // 현재는 단순하게 오류 발생 시 로그아웃으로 처리

      // [Debug] 디버깅을 위해 자동 로그아웃 잠시 주석 처리
      // logout();
    }
  }

  // ✅ [Helper] 쿠키 설정
  function setCookie(name: string, value: string, days: number) {
    let expires = "";
    if (days) {
      const date = new Date();
      date.setTime(date.getTime() + days * 24 * 60 * 60 * 1000);
      expires = "; expires=" + date.toUTCString();
    }
    document.cookie = name + "=" + (value || "") + expires + "; path=/";
  }

  // 쿠키 가져오기
  function getCookie(name: string) {
    const value = "; " + document.cookie;
    const parts = value.split("; " + name + "=");
    if (parts.length === 2) return parts.pop()?.split(";").shift();
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
        member_id: memberId,
        name: displayName,
        email: userEmail,
        profile_image_url: profileImageUrl || "", // null일 경우 빈 문자열
      };

      isLoaded.value = true;
      return true;
    } catch (error) {
      console.error("Login Failed:", error);
      return false;
    }
  }

  // 4️⃣ 로그아웃
  function logout() {
    user.value = {
      member_id: 0,
      name: "",
      email: "",
      profile_image_url: "",
    };
    isLoaded.value = false;

    // 토큰 삭제
    localStorage.removeItem("accessToken");

    // 쿠키 삭제
    document.cookie =
      "accessToken=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;";

    window.location.href = "/";
  }

  const isAuthenticated = computed(() => !!user.value.member_id);

  return { user, fetchUserInfo, login, logout, isAuthenticated };
});
