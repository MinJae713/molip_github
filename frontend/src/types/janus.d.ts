// src/types/janus.d.ts

// janus-gateway 라이브러리에 대한 타입 검사를 무시합니다.
declare module "janus-gateway";

// webrtc-adapter 라이브러리에 대한 타입 검사를 무시합니다.
declare module "webrtc-adapter";

// window 객체에 adapter 속성을 추가합니다. (VideoRoom.vue에서 사용)
interface Window {
  adapter: any;
}
