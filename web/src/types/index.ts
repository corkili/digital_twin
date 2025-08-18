export interface SensorData {
  ID: string;
  HeatFlux: number;
  CoolingWater_In_Temp: number;
  Timestamp: number;
}

export interface WebSocketResponse {
  type: 'data' | 'status' | 'error';
  payload: any;
  timestamp: string;
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