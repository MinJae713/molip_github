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

  const token = ref<string | null>(null);

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

    const savedToken = localStorage.getItem("accessToken");
    if (!savedToken) {
      isLoaded.value = true;
      return;
    }

    token.value = savedToken;

    try {
      console.log("dd1-Token from localStorage:", token.value);
      const payload = parseJwt(token.value);
      console.log("dd2-Parsed Payload:", payload);

      const userId = payload?.memberId || payload?.sub;
      if (!userId) throw new Error("Invalid Token");

      const response = await api.get(`/members/me`);
      const userData = response.data;

      user.value = {
        memberId: userData.member_id || userData.memberId || userData.id,
        name: userData.name,
        email: userData.email,
        profileImageUrl:
          userData.profile_image_url || userData.profileImageUrl || "",
        role: userData.role,
        auth: userData.auth,
      };

      isLoaded.value = true;
    } catch (e) {
      logout();
    }
  }


  // [Helper] 쿠키 설정
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
      token.value = accessToken;
      localStorage.setItem("accessToken", accessToken);
      // 상태 업데이트
      user.value = {
        memberId: memberId || response.data.id || response.data.member_id,
        name: displayName || response.data.name,
        email: userEmail || response.data.email,
        profileImageUrl:
          profileImageUrl || response.data.profile_image_url || "", // null일 경우 빈 문자열
      };
      isLoaded.value = true;
      return true;
    } catch (error) {
      console.error("Login Failed:", error);
      return false;
    }
  }
  async function signup(form: any) {
    try {
      await api.post("/members/register", {
        email: form.email,
        password: form.password,
        name: form.name,
        age: form.age,
        address: form.address,
        gender: form.gender,
      });
      // 회원가입 성공 시 별도 처리 없이 true 반환 (자동 로그인 아님)
      return true;
    } catch (error) {
      console.error("Signup Failed:", error);
      return false;
    }
  }

  // 로그아웃
  function logout() {
    token.value = null;
    user.value = {
      memberId: 0,
      name: "",
      email: "",
      profileImageUrl: "",
    };
    isLoaded.value = false;
    localStorage.removeItem("accessToken");

    // 쿠키 삭제
    document.cookie =
      "accessToken=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;";
    window.location.href = "/";
  }
  const isAuthenticated = computed(() => !!user.value.memberId);
  return { user, token, fetchUserInfo, login, signup, logout, isAuthenticated };
});
