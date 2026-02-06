export type MediaDeviceType = "mic" | "video" | "screen" | "record";

export interface MediaStatus {
  isEnabled: boolean;
  type: MediaDeviceType;
}

export interface Participant {
  id: string;
  memberId: string;
  name: string;
  avatar: string;
  isCameraOn: boolean;
  isMicOn: boolean;
  concentrationScore: number; // 0 ~ 100
  status:
    | "EMPTY" // 자리비움
    | "FEVER" // 집중
    | "NORMAL"; // 일반
}

// 자누스
interface JanusConnectionInfo {
  janusRoomId: number;
  pin: string;
  sessionId: number;
}
