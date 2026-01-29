import { ref } from "vue";
import { defineStore } from "pinia";

export const useTrainerLiveStore = defineStore("trainerlive", () => {
  const janusRoomId = ref(0);
  const pin = ref("");
  const sessionId = ref(0);
  return { janusRoomId, pin, sessionId };
});
