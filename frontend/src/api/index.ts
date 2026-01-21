// src/api/index.ts
import axios from 'axios';

const api = axios.create({
  // .env 파일의 VITE_API_BASE_URL 값을 가져옵니다. 
  // 없을 경우 기본값으로 localhost:3000을 사용합니다.
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:3000',
  timeout: 5000,
});

// 인터셉터 추가: 에러를 명시적으로 전달합니다.
api.interceptors.response.use(
  (response) => response,
  (error) => {
    // 404 에러 등을 콘솔에 명확히 찍어줌
    console.error('API 인터셉터 에러:', error.response?.status, error.message);
    return Promise.reject(error); // 이 줄이 있어야 Store의 catch가 실행됩니다.
  }
);

export default api;
