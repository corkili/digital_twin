import React, { useState, useEffect } from 'react';
import WebSocketService from '../services/WebSocketService';
import ApiService from '../services/ApiService';
import {
  SensorData,
  TestPhaseResponse,
  AlarmData,
  HistoryData,
  ConnectionStatus,
  SendStatus,
  TopicData
} from '../types';

const Dashboard: React.FC = () => {
  const [webSocketService] = useState(() => new WebSocketService());
  const [apiService] = useState(() => new ApiService());

  const [connectionStatus, setConnectionStatus] = useState<ConnectionStatus>({ isConnected: false });
  const [topicData, setTopicData] = useState<TopicData>({
    sensorData: [],
    testPhase: null,
    alarms: [],
    historyData: []
  });
  const [sendStatus, setSendStatus] = useState<SendStatus>({ isSending: false });

  const [formData, setFormData] = useState({
    HeatFlux: 113.93,
    CoolingWater_In_Temp: 39.77,
    TestPhase: '模型安装', // 新增 TestPhase 字段
  });

  useEffect(() => {
    // 设置传感器数据回调
    webSocketService.setSensorDataCallback((data: SensorData) => {
      console.log('收到传感器数据:', data);
      setTopicData(prev => ({
        ...prev,
        sensorData: [data, ...prev.sensorData].slice(0, 50)
      }));
    });

    // 设置测试阶段数据回调
    webSocketService.setTestPhaseCallback((data: TestPhaseResponse) => {
      console.log('收到测试阶段数据:', data);
      setTopicData(prev => ({
        ...prev,
        testPhase: data
      }));
    });

    // 设置告警数据回调
    webSocketService.setAlarmCallback((data: AlarmData) => {
      console.log('收到告警数据:', data);
      setTopicData(prev => ({
        ...prev,
        alarms: [data, ...prev.alarms].slice(0, 20)
      }));
    });

    // 设置历史数据回调
    webSocketService.setHistoryDataCallback((data: HistoryData) => {
      console.log('收到历史数据:', data);
      setTopicData(prev => ({
        ...prev,
        historyData: [data, ...prev.historyData].slice(0, 20)
      }));
    });

    // 设置连接状态回调
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
        TestPhase: formData.TestPhase, // 包含 TestPhase 字段
        Timestamp: 0, // 服务器会自动填充当前时间
      });
      setSendStatus({ isSending: false, success: true });
      setTimeout(() => setSendStatus({ isSending: false }), 2000);
    } catch (error) {
      setSendStatus({ isSending: false, error: '发送失败' });
    }
  };

  const testPhases = [
    '模型安装',
    '打开电源',
    '注入氛围气体',
    '开启冷却水/气阀门',
    '石英灯阵加热',
    '实时监控试验过程',
    '石英灯阵加热停止',
    '关闭冷却水/气阀门'
  ];

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

        {/* 数据发送表单 */}
        <div className="card" style={{ marginBottom: '1rem' }}>
          <h2 style={{ fontSize: '1.25rem', fontWeight: '600', marginBottom: '1rem' }}>发送传感器数据</h2>

          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '1rem' }}>
            <div className="form-group">
              <label className="form-label">热通量 (W/m²)</label>
              <input
                type="number"
                step="0.1"
                value={formData.HeatFlux}
                onChange={(e) => setFormData({...formData, HeatFlux: parseFloat(e.target.value)})}
                className="form-input"
              />
            </div>

            <div className="form-group">
              <label className="form-label">温度 (°C)</label>
              <input
                type="number"
                step="0.1"
                value={formData.CoolingWater_In_Temp}
                onChange={(e) => setFormData({...formData, CoolingWater_In_Temp: parseFloat(e.target.value)})}
                className="form-input"
              />
            </div>

            <div className="form-group">
              <label className="form-label">测试阶段</label>
              <select
                value={formData.TestPhase}
                onChange={(e) => setFormData({...formData, TestPhase: e.target.value})}
                className="form-input"
              >
                {testPhases.map(phase => (
                  <option key={phase} value={phase}>{phase}</option>
                ))}
              </select>
            </div>
          </div>

          <div style={{ marginTop: '1rem' }}>
            <button
              onClick={handleSendRabbitMQ}
              disabled={sendStatus.isSending}
              className="btn btn-purple"
              style={{ width: '100%' }}
            >
              {sendStatus.isSending ? '发送中...' : '通过RabbitMQ发送'}
            </button>

            {sendStatus.success && (
              <div className="text-success" style={{ marginTop: '0.5rem' }}>发送成功！</div>
            )}
            {sendStatus.error && (
              <div className="text-error" style={{ marginTop: '0.5rem' }}>{sendStatus.error}</div>
            )}
          </div>
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: '1rem', marginBottom: '1rem' }}>
          {/* 实时传感器数据 */}
          <div className="card">
            <h2 style={{ fontSize: '1.25rem', fontWeight: '600', marginBottom: '1rem' }}>
              实时传感器数据 ({topicData.sensorData.length})
            </h2>

            <div style={{ maxHeight: '300px', overflowY: 'auto' }}>
              {topicData.sensorData.length === 0 ? (
                <div style={{ textAlign: 'center', color: '#6b7280', padding: '2rem 0' }}>
                  暂无数据，等待接收...
                </div>
              ) : (
                topicData.sensorData.map((data, index) => (
                  <div key={`${data.ID || index}-${index}`} style={{
                    border: '1px solid #e5e7eb',
                    borderRadius: '4px',
                    padding: '0.5rem',
                    marginBottom: '0.5rem'
                  }}>
                    <div style={{ fontSize: '0.875rem', color: '#6b7280' }}>
                      {data.ID} - {data.Timestamp ? new Date(data.Timestamp).toLocaleTimeString() : '时间未知'}
                    </div>
                    <div style={{ fontSize: '0.875rem' }}>
                      热通量: {data.HeatFlux?.toFixed(2) || 'N/A'} W/m² |
                      温度: {data.CoolingWater_In_Temp?.toFixed(2) || 'N/A'}°C
                    </div>
                  </div>
                ))
              )}
            </div>
          </div>

          {/* 测试阶段数据 */}
          <div className="card">
            <h2 style={{ fontSize: '1.25rem', fontWeight: '600', marginBottom: '1rem' }}>
              测试阶段信息
            </h2>

            {topicData.testPhase ? (
              <div>
                <div style={{ marginBottom: '1rem', padding: '0.5rem', backgroundColor: '#f3f4f6', borderRadius: '4px' }}>
                  <strong>当前阶段: {topicData.testPhase.currentPhase}</strong>
                  <div style={{ fontSize: '0.875rem', color: '#6b7280' }}>
                    更新时间: {new Date(topicData.testPhase.timestamp).toLocaleTimeString()}
                  </div>
                </div>

                <div style={{ display: 'grid', gap: '0.25rem' }}>
                  {topicData.testPhase.phases.map((phase, index) => (
                    <div
                      key={index}
                      style={{
                        padding: '0.25rem 0.5rem',
                        fontSize: '0.875rem',
                        backgroundColor: phase.current ? '#dbeafe' : '#f9fafb',
                        border: phase.current ? '2px solid #3b82f6' : '1px solid #e5e7eb',
                        borderRadius: '4px',
                        fontWeight: phase.current ? '600' : 'normal'
                      }}
                    >
                      {phase.current ? '▶ ' : ''}{phase.key}
                    </div>
                  ))}
                </div>
              </div>
            ) : (
              <div style={{ textAlign: 'center', color: '#6b7280', padding: '2rem 0' }}>
                暂无测试阶段数据，等待接收...
              </div>
            )}
          </div>
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: '1rem' }}>
          {/* 告警数据 */}
          <div className="card">
            <h2 style={{ fontSize: '1.25rem', fontWeight: '600', marginBottom: '1rem' }}>
              告警信息 ({topicData.alarms.length})
            </h2>

            <div style={{ maxHeight: '300px', overflowY: 'auto' }}>
              {topicData.alarms.length === 0 ? (
                <div style={{ textAlign: 'center', color: '#6b7280', padding: '2rem 0' }}>
                  暂无告警数据，等待接收...
                </div>
              ) : (
                topicData.alarms.map((alarm, index) => (
                  <div key={index} style={{
                    border: `2px solid ${alarm.alarmLevel === 'CRITICAL' ? '#ef4444' : '#f59e0b'}`,
                    borderRadius: '4px',
                    padding: '0.5rem',
                    marginBottom: '0.5rem',
                    backgroundColor: alarm.alarmLevel === 'CRITICAL' ? '#fef2f2' : '#fffbeb'
                  }}>
                    <div style={{ fontWeight: '600', color: alarm.alarmLevel === 'CRITICAL' ? '#dc2626' : '#d97706' }}>
                      {alarm.alarmType} - {alarm.alarmLevel}
                    </div>
                    <div style={{ fontSize: '0.875rem' }}>
                      设备: {alarm.deviceName} | 点位: {alarm.pointIdentity}
                    </div>
                    <div style={{ fontSize: '0.875rem' }}>
                      当前值: {alarm.currentValue} | 阈值: {alarm.threshold || 'N/A'}
                    </div>
                    <div style={{ fontSize: '0.75rem', color: '#6b7280' }}>
                      {alarm.timestamp}
                    </div>
                  </div>
                ))
              )}
            </div>
          </div>

          {/* 历史数据 */}
          <div className="card">
            <h2 style={{ fontSize: '1.25rem', fontWeight: '600', marginBottom: '1rem' }}>
              历史数据 ({topicData.historyData.length})
            </h2>

            <div style={{ maxHeight: '300px', overflowY: 'auto' }}>
              {topicData.historyData.length === 0 ? (
                <div style={{ textAlign: 'center', color: '#6b7280', padding: '2rem 0' }}>
                  暂无历史数据，等待接收...
                </div>
              ) : (
                topicData.historyData.map((history, index) => (
                  <div key={index} style={{
                    border: '1px solid #e5e7eb',
                    borderRadius: '4px',
                    padding: '0.5rem',
                    marginBottom: '0.5rem'
                  }}>
                    {history.timestamp === -1 ? (
                      <div style={{ color: '#059669', fontWeight: '600' }}>
                        ✓ 历史数据推送完成 - {history.subscribeId}
                      </div>
                    ) : (
                      <div>
                        <div style={{ fontSize: '0.875rem', color: '#6b7280' }}>
                          订阅ID: {history.subscribeId}
                        </div>
                        <div style={{ fontSize: '0.875rem' }}>
                          时间: {new Date(history.timestamp).toLocaleTimeString()}
                        </div>
                        {history.pointDataMap && (
                          <div style={{ fontSize: '0.75rem', color: '#6b7280' }}>
                            数据点: {Object.keys(history.pointDataMap).length} 个
                          </div>
                        )}
                      </div>
                    )}
                  </div>
                ))
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;