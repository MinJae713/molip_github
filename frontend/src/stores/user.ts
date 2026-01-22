import { ref } from "vue";
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
      // 1. 쿠키에서 JWT 토큰 가져오기 (예: 'accessToken')
      const token = getCookie("accessToken");

      // 토큰이 없으면 로그인되지 않은 것으로 간주
      // 여기서는 1번 사용자로 강제 지정. 서비스 단계 가면 수정해야함.
      let userId = 1;

      if (token) {
        const payload = parseJwt(token);
        if (payload && payload.sub) {
          userId = payload.sub; // payload의 sub 필드를 user_id로 가정
        } else if (payload && payload.id) {
          userId = payload.id;
        }
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

  // Helper: Cookie Getter
  function getCookie(name: string): string | null {
    const match = document.cookie.match(
      new RegExp("(^| )" + name + "=([^;]+)"),
    );
    if (match && match[2]) return match[2];
    return null;
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

  return { user, fetchUserInfo };
});
