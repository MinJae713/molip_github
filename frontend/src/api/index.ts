// src/api/index.ts
import axios from 'axios';

const api = axios.create({
  // .env 파일의 VITE_API_BASE_URL 값을 가져옵니다. 
  // 없을 경우 기본값으로 localhost:3000을 사용합니다.
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:3000',
  timeout: 5000,
});

export default api;
