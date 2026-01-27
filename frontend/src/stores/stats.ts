import { ref, computed } from "vue";
import { defineStore } from "pinia";
import api from "@/api";

import type { LearnerWorkspaceTopCardStats } from "@/types";

export const useStatsStore = defineStore("stats", () => {
  const learnerWorkspaceTopCardStats = ref<LearnerWorkspaceTopCardStats | null>(
    null,
  );

  async function fetchLearnerWorkspaceTopCardStats(
    workspaceId: string,
    user_id: string,
  ) {
    try {
      const response = await api.get(
        `workspaces/${workspaceId}/stats/${user_id}`,
      );
      learnerWorkspaceTopCardStats.value = response.data;
    } catch (error) {
      console.error("Failed to fetch learner workspace top card stats:", error);
    }
  }

  return {
    learnerWorkspaceTopCardStats,
    fetchLearnerWorkspaceTopCardStats,
  };
});
