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
<<<<<<< HEAD
      console.log("dd1-Token from localStorage:", token); // [Debug]
      const payload = parseJwt(token);
      console.log("dd2-Parsed Payload:", payload); // [Debug]
      // JWT payload에 memberId가 있으면 그것을 사용, 없으면 sub 사용
      const userId = payload?.memberId || payload?.sub;
      console.log("dd3-Extracted userId:", userId); // [Debug]
=======
      const payload = parseJwt(token);
      const userId = payload?.sub;
>>>>>>> 5a815b057488af54f810be9157509bf720ba7d6a

      if (!userId) {
        throw new Error("Invalid Token");
      }
      // DB에서 최신 유저 정보 조회
<<<<<<< HEAD
      // DB에서 최신 유저 정보 조회
      console.log(`dd4-Fetching info for userId: ${userId}`); // [Debug]
      const response = await api.get(`/members/me`);
      console.log("dd5-User Info Response:", response.data); // [Debug]
=======
      const response = await api.get(`/members/${userId}`);
>>>>>>> 5a815b057488af54f810be9157509bf720ba7d6a
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
<<<<<<< HEAD
      console.error("dd6-Fetch User Info Failed:", error);
=======
      console.error("Fetch User Info Failed:", error);
>>>>>>> 5a815b057488af54f810be9157509bf720ba7d6a
      logout();
    }
  }

<<<<<<< HEAD
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
=======
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
>>>>>>> 5a815b057488af54f810be9157509bf720ba7d6a
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

<<<<<<< HEAD
  // 로그아웃
=======
  // 3️⃣ 로그아웃
>>>>>>> 5a815b057488af54f810be9157509bf720ba7d6a
  function logout() {
    user.value = {
      memberId: 0,
      name: "",
      email: "",
      profileImageUrl: "",
    };
    isLoaded.value = false;
    localStorage.removeItem("accessToken");

<<<<<<< HEAD
    // 쿠키 삭제
    document.cookie =
      "accessToken=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;";
=======
    const cookies = document.cookie.split(";");
    for (let i = 0; i < cookies.length; i++) {
      const cookie = cookies[i];
      if (!cookie) continue; // 방어 코드
      const eqPos = cookie.indexOf("=");
      const name = eqPos > -1 ? cookie.substr(0, eqPos) : cookie;
      document.cookie = name + "=;expires=Thu, 01 Jan 1970 00:00:00 GMT;path=/";
    }
>>>>>>> 5a815b057488af54f810be9157509bf720ba7d6a
    window.location.href = "/";
  }
  const isAuthenticated = computed(() => !!user.value.memberId);
  return { user, fetchUserInfo, login, signup, logout, isAuthenticated };
});
