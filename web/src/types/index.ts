export interface SensorData {
  ID: string;
  HeatFlux: number;
  CoolingWater_In_Temp: number;
  TestPhase: string;
  Timestamp: number;
}

export interface TestPhaseItem {
  key: string;
  current: boolean;
}

export interface TestPhaseResponse {
  phases: TestPhaseItem[];
  currentPhase: string;
  timestamp: number;
}

export interface AlarmData {
  id: number;
  deviceName: string;
  alarmType: string;
  pointIdentity: string;
  alarmLevel: string;
  alarmMessage: string;
  currentValue: any;
  threshold?: number;
  timestamp: string;
  acknowledged: boolean;
}

export interface HistoryData {
  id?: string;
  timestamp: number;
  subscribeId: string;
  pointDataMap?: Record<string, any>;
}

export interface WebSocketResponse<T = any> {
  code: number;
  message: string;
  timestamp: string;
  data: T;
}

export interface ConnectionStatus {
  isConnected: boolean;
  connectionId?: string;
  lastActivity?: string;
}

export interface SendStatus {
  isSending: boolean;
  success?: boolean;
  error?: string;
}

export interface TopicData {
  sensorData: SensorData[];
  testPhase: TestPhaseResponse | null;
  alarms: AlarmData[];
  historyData: HistoryData[];
}