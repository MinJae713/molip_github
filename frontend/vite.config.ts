/// <reference types="vitest/config" />
import { fileURLToPath, URL } from "node:url";
import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";
import tailwindcss from "@tailwindcss/vite";
import vueDevTools from "vite-plugin-vue-devtools";

// https://vite.dev/config/
import path from "node:path";
import { storybookTest } from "@storybook/addon-vitest/vitest-plugin";
import { playwright } from "@vitest/browser-playwright";

// 현재 실행 중인 명령어가 'storybook'인지 확인합니다.
const isStorybook =
  process.env.npm_lifecycle_event === "storybook" ||
  process.env.STORYBOOK === "true";
const dirname =
  typeof __dirname !== "undefined"
    ? __dirname
    : path.dirname(fileURLToPath(import.meta.url));

// More info at: https://storybook.js.org/docs/next/writing-tests/integrations/vitest-addon
export default defineConfig({
  plugins: [
    vue(),
    tailwindcss(),
    // Storybook 실행 중이 아닐 때만 vueDevTools를 포함합니다.
    // !isStorybook && vueDevTools(),
  ].filter(Boolean) as any,
  // false 값을 제거하여 플러그인 목록을 정제합니다.

  // [추가 1] janus-gateway 등 구형 라이브러리가 'global' 변수를 참조할 때 에러 방지
  define: {
    global: "window",
  },

  resolve: {
    alias: {
      "@": fileURLToPath(new URL("./src", import.meta.url)),
    },
  },

  // [추가 2] 개발 서버 설정 (WebRTC 테스트 및 API 프록시)
  server: {
    // 외부 기기(모바일 등)에서 IP로 접속 허용 (카메라 테스트 필수)
    host: true,
    proxy: {
      // 백엔드 API 요청 프록시
      "/api": {
        target: "http://localhost:8080",
        changeOrigin: true,
      },
      // Janus WebRTC 시그널링(웹소켓) 프록시
      "/janus_ws": {
        target: "http://localhost:8080", // 백엔드/Nginx 주소에 맞게 수정 필요
        ws: true,
        changeOrigin: true,
      },
    },
  },

  test: {
    projects: [
      {
        extends: true,
        plugins: [
          storybookTest({
            configDir: path.join(dirname, ".storybook"),
          }),
        ],
        test: {
          name: "storybook",
          browser: {
            enabled: true,
            headless: true,
            provider: playwright({}),
            instances: [
              {
                browser: "chromium",
              },
            ],
          },
          setupFiles: [".storybook/vitest.setup.ts"],
        },
      },
      {
        extends: true,
        plugins: [
          // The plugin will run tests for the stories defined in your Storybook config
          // See options at: https://storybook.js.org/docs/next/writing-tests/integrations/vitest-addon#storybooktest
          storybookTest({
            configDir: path.join(dirname, ".storybook"),
          }),
        ],
        test: {
          name: "storybook",
          browser: {
            enabled: true,
            headless: true,
            provider: playwright({}),
            instances: [
              {
                browser: "chromium",
              },
            ],
          },
          setupFiles: [".storybook/vitest.setup.ts"],
        },
      },
    ],
  },
});
