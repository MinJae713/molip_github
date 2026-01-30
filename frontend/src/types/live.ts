export type MediaDeviceType = 'mic' | 'video' | 'screen' | 'record';

export interface MediaStatus {
  isEnabled: boolean;
  type: MediaDeviceType;
}

export interface Participant {
  id: string;
  name: string;
  avatar: string;
  isCameraOn: boolean;
  isMicOn: boolean;
  concentrationScore: number; // 0 ~ 100
  status: 'focus' | 'distracted' | 'away'; // 집중, 주의, 부재
}

// 자누스
interface JanusConnectionInfo {
  janusRoomId: number;
  pin: string;
  sessionId: number;
}