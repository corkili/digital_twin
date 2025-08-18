import axios from 'axios';
import { SensorData, SendStatus } from '../types';

class ApiService {
  private baseUrl: string;

  constructor(baseUrl: string = 'http://umi.xyz:8081') {
    this.baseUrl = baseUrl;
  }

  async sendToRabbitMQ(data: Partial<SensorData>): Promise<void> {
    const payload = {
      ...data,
      ID: data.ID || `sensor-${Date.now()}`,
      Timestamp: Date.now(),
    };

    try {
      const response = await axios.post(`${this.baseUrl}/api/sensor/send`, payload);
      return response.data;
    } catch (error) {
      console.error('Error sending to RabbitMQ:', error);
      throw error;
    }
  }

  async getHealthStatus(): Promise<{ status: string; timestamp: string }> {
    try {
      const response = await axios.get(`${this.baseUrl}/api/health`);
      return response.data;
    } catch (error) {
      console.error('Error checking health status:', error);
      throw error;
    }
  }

  async getSensorHistory(limit: number = 10): Promise<SensorData[]> {
    try {
      const response = await axios.get(`${this.baseUrl}/api/sensor/history`, {
        params: { limit },
      });
      return response.data;
    } catch (error) {
      console.error('Error fetching sensor history:', error);
      throw error;
    }
  }
}

export default ApiService;