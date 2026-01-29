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
      console.log("dd1-Token from localStorage:", token); // [Debug]
      const payload = parseJwt(token);
      console.log("dd2-Parsed Payload:", payload); // [Debug]
      // JWT payload에 memberId가 있으면 그것을 사용, 없으면 sub 사용
      const userId = payload?.memberId || payload?.sub;
      console.log("dd3-Extracted userId:", userId); // [Debug]

      if (!userId) {
        throw new Error("Invalid Token: No userId found");
      }
      // DB에서 최신 유저 정보 조회
      // DB에서 최신 유저 정보 조회
      console.log(`dd4-Fetching info for userId: ${userId}`); // [Debug]
      const response = await api.get(`/members/me`);
      console.log("dd5-User Info Response:", response.data); // [Debug]
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
    } catch (error) {
      console.error("dd6-Fetch User Info Failed:", error);
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
  return { user, fetchUserInfo, login, signup, logout, isAuthenticated };
});
