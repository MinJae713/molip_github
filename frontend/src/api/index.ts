// src/api/index.ts
import axios from "axios";
import { toast } from 'vue3-toastify'; // toast 불러오기

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
    const serverDetail = error.response?.data?.body?.detail;
    const status = error.response?.status;

    if (serverDetail) {
      // 💡 alert 대신 toast 사용!
      toast.error(`[${status}] ${serverDetail}`, {
        transition: toast.TRANSITIONS.BOUNCE, // 애니메이션 효과 추가
      });
      
      error.message = serverDetail;
    } else {
      // 서버 메시지가 없는 일반적인 에러 처리
      toast.warning(error.message || "연결이 원활하지 않습니다.");
    }

    return Promise.reject(error);
  },
);

export default api;
