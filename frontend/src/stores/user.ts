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

  async function fetchUserInfo() {
    // 이미 데이터가 있다면 요청하지 않음 (캐싱 효과)
    if (isLoaded.value) return;

    try {
      // 1. localStorage에서 JWT 토큰 가져오기
      const token = localStorage.getItem("accessToken");

      if (!token) {
        isLoaded.value = true;
        return;
      }

      let userId = 0;

      const payload = parseJwt(token);
      if (payload && payload.sub) {
        userId = payload.sub;
      } else if (payload && payload.id) {
        userId = payload.id;
      }

      if (!userId) {
        // 토큰은 있지만 ID를 파싱할 수 없는 경우
        console.warn("Invalid token payload");
        isLoaded.value = true;
        return;
      }

      // 2. User ID로 사용자 정보 요청 (/user/:id -> /users/:id via routes.json)
      const response = await api.get(`/members/${userId}`);
      // Map response to User interface
      user.value = {
        member_id: response.data.member_id || response.data.id,
        name: response.data.name,
        email: response.data.email,
        profile_image_url:
          response.data.profile_image_url || response.data.profileImage,
      };
      isLoaded.value = true;
    } catch (error) {
      console.error("Failed to fetch user info:", error);
    }
  }

  // Helper: JWT Parser
  function parseJwt(token: string) {
    try {
      const parts = token.split(".");
      if (parts.length < 2) return null;
      const base64Url = parts[1];
      if (!base64Url) return null;
      const base64 = base64Url.replace(/-/g, "+").replace(/_/g, "/");
      const jsonPayload = decodeURIComponent(
        atob(base64)
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

  async function login(email: string, pass: string) {
    // Mock Login Logic
    return new Promise<boolean>((resolve) => {
      setTimeout(() => {
        if (email === "student" && pass === "1234") {
          // 학생 로그인 성공
          user.value = {
            member_id: 1, // Mock ID
            name: "학생 김철수",
            email: "student@example.com",
            profile_image_url:
              "https://cdn-icons-png.flaticon.com/512/257/257685.png",
          };
          localStorage.setItem("accessToken", "mock-access-token-12345");
          isLoaded.value = true;
          resolve(true);
        } else if (email === "teacher" && pass === "1234") {
          // 강사 로그인 성공
          user.value = {
            member_id: 2, // Mock ID
            name: "강사 이영희",
            email: "teacher@example.com",
            profile_image_url:
              "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcT5byKin5Im5nKcFce1aqq13DJSJsOGI0n7BA&s",
          } as any;
          localStorage.setItem("accessToken", "mock-trainer-token-67890");
          isLoaded.value = true;
          resolve(true);
        } else {
          resolve(false);
        }
      }, 500); // 0.5초 딜레이
    });
  }

  function logout() {
    // 1. 상태 초기화
    user.value = {
      member_id: 0,
      name: "",
      email: "",
      profile_image_url: "",
    };
    isLoaded.value = false;

    // 2. LocalStorage 삭제
    localStorage.removeItem("accessToken");

    // 3. 모든 쿠키 삭제 (도메인/경로 고려하여 만료 처리)
    const cookies = document.cookie.split(";");
    for (let i = 0; i < cookies.length; i++) {
      const cookie = cookies[i];
      if (!cookie) continue;
      const eqPos = cookie.indexOf("=");
      const name = eqPos > -1 ? cookie.substr(0, eqPos) : cookie;
      document.cookie = name + "=;expires=Thu, 01 Jan 1970 00:00:00 GMT;path=/";
    }

    // 4. 앱 리셋 (상태 잔여물 제거를 위해 새로고침하며 로그인 페이지로 이동)
    window.location.href = "/";
  }

  const isAuthenticated = computed(
    () => !!localStorage.getItem("accessToken") || !!user.value.member_id,
  );

  return { user, fetchUserInfo, login, logout, isAuthenticated };
});
