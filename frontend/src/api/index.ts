// src/api/index.ts
import axios from "axios";

const api = axios.create({
  // .env 파일의 VITE_API_BASE_URL 값
  // 없을 경우 기본값으로 localhost:3000
  baseURL: import.meta.env.VITE_API_BASE_URL || "http://localhost:3000",
  timeout: 5000,
});

// 인터셉터 추가: 요청 시 토큰 주입
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem("accessToken");
    // const token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJzYWZmeUBhLmNvbSIsImlhdCI6MTc3MDE4MzU2NCwiZXhwIjoxNzcwNzgzNTY0LCJtZW1iZXJJZCI6NCwibmFtZSI6Iuq1rOyEoOyDneyDneyDnSIsInR5cGUiOiJhY2Nlc3MifQ.BVcX12lUPfl-SKF0dRiAcuymMN6jcvLPA7betwDQB6U"
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  },
);

// 인터셉터 추가: 에러를 명시적으로 전달합니다.
api.interceptors.response.use(
  (response) => response,
  (error) => {
    // 404 에러 등을 콘솔에 찍기
    console.error("API 인터셉터 에러:", error.response?.status, error.message);
    return Promise.reject(error);
  },
);

export default api;
