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

  // ✅ [Helper] Base64 Url Decode (JWT 파싱용)
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

  // 1️⃣ 사용자 정보 가져오기 (새로고침 시)
  async function fetchUserInfo() {
    if (isLoaded.value) return;

    const token = localStorage.getItem("accessToken");
    if (!token) {
      isLoaded.value = true;
      return;
    }

    try {
      // 토큰 파싱해서 ID 추출
      const payload = parseJwt(token);
      const userId = payload?.sub;

      if (!userId) {
        throw new Error("Invalid Token");
      }

      // DB에서 최신 유저 정보 조회
      const response = await api.get(`/members/${userId}`);
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
      logout();
    }
  }

  // 2️⃣ 로그인 (비밀번호 검증 없이 ID 매핑만 수행)
  async function login(emailId: string, pass: string) {
    try {
      // ID에 따라 타켓 유저 매핑 (비밀번호 검증 로직 제거됨)
      let targetMemberId = 0;
      let role = "student";

      if (emailId === "student") {
        targetMemberId = 1; // 나학생
      } else if (emailId === "teacher") {
        targetMemberId = 2; // 나강사
        role = "trainer";
      } else if (emailId === "admin") {
        targetMemberId = 3; // 나관리자
        role = "admin";
      } else {
        // 매핑되지 않은 ID면 실패 처리 (혹은 기본값 1로 처리할 수도 있음)
        console.warn("Unknown ID, login failed");
        return false;
      }

      // 해당 유저 정보 조회 (존재 여부 확인)
      const response = await api.get(`/members/${targetMemberId}`);
      const userData = response.data;

      if (!userData) return false;

      // 정보가 맞으면 토큰 생성 및 저장
      const token = createMockJwt(userData.member_id, role);
      localStorage.setItem("accessToken", token);

      // 상태 업데이트 (API 데이터 사용)
      user.value = {
        member_id: userData.member_id,
        name: userData.name,
        email: userData.email,
        profile_image_url: userData.profile_image_url,
      };

      isLoaded.value = true;
      return true;
    } catch (error) {
      console.error("Login Failed:", error);
      return false;
    }
  }

  // 3️⃣ 로그아웃
  function logout() {
    user.value = {
      member_id: 0,
      name: "",
      email: "",
      profile_image_url: "",
    };
    isLoaded.value = false;
    localStorage.removeItem("accessToken");

    const cookies = document.cookie.split(";");
    for (let i = 0; i < cookies.length; i++) {
      const cookie = cookies[i];
      if (!cookie) continue; // 방어 코드
      const eqPos = cookie.indexOf("=");
      const name = eqPos > -1 ? cookie.substr(0, eqPos) : cookie;
      document.cookie = name + "=;expires=Thu, 01 Jan 1970 00:00:00 GMT;path=/";
    }
    window.location.href = "/";
  }

  const isAuthenticated = computed(() => !!user.value.member_id);

  return { user, fetchUserInfo, login, logout, isAuthenticated };
});
