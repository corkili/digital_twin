import SockJS from 'sockjs-client';
import { Client, Message } from '@stomp/stompjs';
import {
  SensorData,
  TestPhaseResponse,
  AlarmData,
  HistoryData,
  WebSocketResponse,
  ConnectionStatus,
  TopicData
} from '../types';

class WebSocketService {
  private client: Client | null = null;
  private connectionStatus: ConnectionStatus = { isConnected: false };
  private topicCallbacks: {
    sensorData: ((data: SensorData) => void) | null;
    testPhase: ((data: TestPhaseResponse) => void) | null;
    alarms: ((data: AlarmData) => void) | null;
    historyData: ((data: HistoryData) => void) | null;
  } = {
    sensorData: null,
    testPhase: null,
    alarms: null,
    historyData: null,
  };
  private statusCallback: ((status: ConnectionStatus) => void) | null = null;

  constructor(private url: string = 'http://localhost:8081/api/ws') {}

  connect() {
    if (this.client && this.client.active) {
      return;
    }

    this.client = new Client({
      webSocketFactory: () => new SockJS(this.url),
      debug: (str) => console.log(str),
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });

    this.client.onConnect = (frame) => {
      console.log('Connected: ' + frame);
      this.connectionStatus = {
        isConnected: true,
        connectionId: frame.headers['user-name'],
        lastActivity: new Date().toISOString(),
      };
      
      if (this.statusCallback) {
        this.statusCallback(this.connectionStatus);
      }

      // Subscribe to all topics
      this.subscribeToAllTopics();
    };

    this.client.onDisconnect = () => {
      console.log('Disconnected');
      this.connectionStatus = { isConnected: false };
      if (this.statusCallback) {
        this.statusCallback(this.connectionStatus);
      }
    };

    this.client.onStompError = (frame) => {
      console.error('Broker reported error: ' + frame.headers['message']);
      console.error('Additional details: ' + frame.body);
    };

    this.client.activate();
  }

  disconnect() {
    if (this.client) {
      this.client.deactivate();
      this.client = null;
    }
  }



  private subscribeToAllTopics() {
    if (!this.client) return;

    // 订阅传感器数据主题
    this.client.subscribe('/topic/sensor-data', (message: Message) => {
      if (message.body) {
        try {
          const response: WebSocketResponse<SensorData> = JSON.parse(message.body);
          console.log('收到传感器数据:', response);
          if (this.topicCallbacks.sensorData && response.data) {
            this.topicCallbacks.sensorData(response.data);
          }
        } catch (error) {
          console.error('Error parsing sensor data:', error);
        }
      }
    });

    // 订阅测试阶段数据主题
    this.client.subscribe('/topic/test-phase', (message: Message) => {
      if (message.body) {
        try {
          const response: WebSocketResponse<TestPhaseResponse> = JSON.parse(message.body);
          console.log('收到测试阶段数据:', response);
          if (this.topicCallbacks.testPhase && response.data) {
            this.topicCallbacks.testPhase(response.data);
          }
        } catch (error) {
          console.error('Error parsing test phase data:', error);
        }
      }
    });

    // 订阅告警数据主题
    this.client.subscribe('/topic/alarm-data', (message: Message) => {
      if (message.body) {
        try {
          const response: WebSocketResponse<AlarmData> = JSON.parse(message.body);
          console.log('收到告警数据:', response);
          if (this.topicCallbacks.alarms && response.data) {
            this.topicCallbacks.alarms(response.data);
          }
        } catch (error) {
          console.error('Error parsing alarm data:', error);
        }
      }
    });

    // 订阅历史数据主题
    this.client.subscribe('/topic/history_data', (message: Message) => {
      if (message.body) {
        try {
          const response: WebSocketResponse<HistoryData> = JSON.parse(message.body);
          console.log('收到历史数据:', response);
          if (this.topicCallbacks.historyData && response.data) {
            this.topicCallbacks.historyData(response.data);
          }
        } catch (error) {
          console.error('Error parsing history data:', error);
        }
      }
    });
  }

  // 设置回调函数
  setSensorDataCallback(callback: (data: SensorData) => void) {
    this.topicCallbacks.sensorData = callback;
  }

  setTestPhaseCallback(callback: (data: TestPhaseResponse) => void) {
    this.topicCallbacks.testPhase = callback;
  }

  setAlarmCallback(callback: (data: AlarmData) => void) {
    this.topicCallbacks.alarms = callback;
  }

  setHistoryDataCallback(callback: (data: HistoryData) => void) {
    this.topicCallbacks.historyData = callback;
  }

  setStatusCallback(callback: (status: ConnectionStatus) => void) {
    this.statusCallback = callback;
  }

  getConnectionStatus(): ConnectionStatus {
    return this.connectionStatus;
  }
}

export default WebSocketService;