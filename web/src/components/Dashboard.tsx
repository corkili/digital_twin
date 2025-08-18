import React, { useState, useEffect } from 'react';
import WebSocketService from '../services/WebSocketService';
import ApiService from '../services/ApiService';
import { SensorData, ConnectionStatus, SendStatus } from '../types';

const Dashboard: React.FC = () => {
  const [webSocketService] = useState(() => new WebSocketService());
  const [apiService] = useState(() => new ApiService());
  
  const [connectionStatus, setConnectionStatus] = useState<ConnectionStatus>({ isConnected: false });
  const [sensorData, setSensorData] = useState<SensorData[]>([]);
  const [sendStatus, setSendStatus] = useState<SendStatus>({ isSending: false });
  
  const [formData, setFormData] = useState({
    HeatFlux: 113.93,
    CoolingWater_In_Temp: 39.77,
  });

  useEffect(() => {
    webSocketService.setDataCallback((data: SensorData) => {
      console.log('收到传感器数据:', data);
      setSensorData(prev => [data, ...prev].slice(0, 50)); // Keep last 50 records
    });

    webSocketService.setStatusCallback((status: ConnectionStatus) => {
      setConnectionStatus(status);
    });

    return () => {
      webSocketService.disconnect();
    };
  }, [webSocketService]);

  const handleConnect = () => {
    webSocketService.connect();
  };

  const handleDisconnect = () => {
    webSocketService.disconnect();
  };



  const handleSendRabbitMQ = async () => {
    try {
      setSendStatus({ isSending: true });
      await apiService.sendToRabbitMQ({
        ...formData,
        ID: '', // 服务器会自动生成
        HeatFlux: Number(formData.HeatFlux),
        CoolingWater_In_Temp: Number(formData.CoolingWater_In_Temp),
        Timestamp: 0, // 服务器会自动填充当前时间
      });
      setSendStatus({ isSending: false, success: true });
      setTimeout(() => setSendStatus({ isSending: false }), 2000);
    } catch (error) {
      setSendStatus({ isSending: false, error: '发送失败' });
    }
  };

  const getStatusColor = () => {
    return connectionStatus.isConnected ? 'text-green-600' : 'text-red-600';
  };

  const getStatusText = () => {
    return connectionStatus.isConnected ? '已连接' : '未连接';
  };

  return (
    <div className="dashboard-container">
      <div className="dashboard-content">
        <div className="card">
          <h1 className="header">
            数字孪生WebSocket测试平台
          </h1>
          
          <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', marginBottom: '1rem' }}>
            <div className={`status-indicator ${connectionStatus.isConnected ? 'status-connected' : 'status-disconnected'}`}>
              状态: {getStatusText()}
            </div>
            
            <div style={{ display: 'flex', gap: '0.5rem' }}>
              {!connectionStatus.isConnected ? (
                <button
                  onClick={handleConnect}
                  className="btn btn-blue"
                >
                  连接
                </button>
              ) : (
                <button
                  onClick={handleDisconnect}
                  className="btn btn-red"
                >
                  断开连接
                </button>
              )}
            </div>
          </div>
        </div>

        <div className="grid grid-cols-2">
          {/* 数据发送表单 */}
          <div className="card">
            <h2 style={{ fontSize: '1.25rem', fontWeight: '600', marginBottom: '1rem' }}>发送传感器数据</h2>
            
            <div>
              <div className="form-group">
                <label className="form-label">
                  热通量 (W/m²)
                </label>
                <input
                  type="number"
                  step="0.1"
                  value={formData.HeatFlux}
                  onChange={(e) => setFormData({...formData, HeatFlux: parseFloat(e.target.value)})}
                  className="form-input"
                />
              </div>
              
              <div className="form-group">
                <label className="form-label">
                  温度 (°C)
                </label>
                <input
                  type="number"
                  step="0.1"
                  value={formData.CoolingWater_In_Temp}
                  onChange={(e) => setFormData({...formData, CoolingWater_In_Temp: parseFloat(e.target.value)})}
                  className="form-input"
                />
              </div>
              
              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                <button
                  onClick={handleSendRabbitMQ}
                  disabled={sendStatus.isSending}
                  className="btn btn-purple"
                  style={{ width: '100%' }}
                >
                  {sendStatus.isSending ? '发送中...' : '通过RabbitMQ发送'}
                </button>
              </div>
              
              {sendStatus.success && (
                <div className="text-success">发送成功！</div>
              )}
              {sendStatus.error && (
                <div className="text-error">{sendStatus.error}</div>
              )}
            </div>
          </div>

          {/* 实时数据展示 */}
          <div className="card">
            <h2 style={{ fontSize: '1.25rem', fontWeight: '600', marginBottom: '1rem' }}>
              实时传感器数据
              <span style={{ fontSize: '0.875rem', fontWeight: 'normal', color: '#6b7280', marginLeft: '0.5rem' }}>
                ({sensorData.length} 条记录)
              </span>
            </h2>
            
            <div className="max-h-96">
              {sensorData.length === 0 ? (
                <div className="text-center" style={{ color: '#6b7280', padding: '2rem 0' }}>
                  暂无数据，等待接收...
                </div>
              ) : (
                <div>
                  {sensorData.map((data, index) => (
                    <div key={`${data.ID || index}-${index}`} className="data-item">
                      <div className="data-header">
                        <div className="data-device">
                          {data.ID}
                        </div>
                        <div className="data-time">
                          {data.Timestamp ? new Date(data.Timestamp).toLocaleTimeString() : '时间未知'}
                        </div>
                      </div>
                      
                      <div className="data-grid">
                        <div>
                          <span className="data-label">热通量:</span>
                          <span className="data-value">{data.HeatFlux !== null && data.HeatFlux !== undefined ? data.HeatFlux.toFixed(2) : 'N/A'} W/m²</span>
                        </div>
                        <div>
                          <span className="data-label">温度:</span>
                          <span className="data-value">{data.CoolingWater_In_Temp !== null && data.CoolingWater_In_Temp !== undefined ? data.CoolingWater_In_Temp.toFixed(2) : 'N/A'}°C</span>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;