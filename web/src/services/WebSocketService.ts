import SockJS from 'sockjs-client';
import { Client, Message } from '@stomp/stompjs';
import { SensorData, WebSocketResponse, ConnectionStatus } from '../types';

class WebSocketService {
  private client: Client | null = null;
  private connectionStatus: ConnectionStatus = { isConnected: false };
  private dataCallback: ((data: SensorData) => void) | null = null;
  private statusCallback: ((status: ConnectionStatus) => void) | null = null;

  constructor(private url: string = 'http://umi.xyz:8081/api/ws') {}

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

      // Subscribe to sensor data topic
      this.client?.subscribe('/topic/sensor-data', (message: Message) => {
        if (message.body) {
          try {
            const response = JSON.parse(message.body);
            // 处理嵌套的数据格式
            const sensorData = response.data || response;
            if (this.dataCallback) {
              this.dataCallback(sensorData);
            }
          } catch (error) {
            console.error('Error parsing sensor data:', error);
          }
        }
      });
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



  setDataCallback(callback: (data: SensorData) => void) {
    this.dataCallback = callback;
  }

  setStatusCallback(callback: (status: ConnectionStatus) => void) {
    this.statusCallback = callback;
  }

  getConnectionStatus(): ConnectionStatus {
    return this.connectionStatus;
  }
}

export default WebSocketService;