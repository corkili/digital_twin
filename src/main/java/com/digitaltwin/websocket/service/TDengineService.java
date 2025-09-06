package com.digitaltwin.websocket.service;

import com.digitaltwin.websocket.config.TDengineConfig;
import com.digitaltwin.websocket.model.SensorData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class TDengineService {

    private final TDengineConfig tdengineConfig;

    private Connection connection;

    // 定义不需要存储的字段集合
    private static final Set<String> EXCLUDED_FIELDS = Set.of("deviceType", "deviceName", "ts");

    @PostConstruct
    public void init() {
        try {
            Class.forName("com.taosdata.jdbc.TSDBDriver");
            connection = DriverManager.getConnection(
                    tdengineConfig.getUrl(),
                    tdengineConfig.getUsername(),
                    tdengineConfig.getPassword()
            );
            log.info("TDengine数据库连接成功: {}", tdengineConfig.getUrl());

            // 创建数据库和表（如果不存在）
            initDatabaseAndTable();
        } catch (Exception e) {
            log.error("TDengine数据库连接失败: {}", e.getMessage(), e);
        }
    }

    private void initDatabaseAndTable() throws SQLException {
        // 创建数据库
        try (PreparedStatement stmt = connection.prepareStatement("CREATE DATABASE IF NOT EXISTS digital_twin")) {
            stmt.execute();
        }

        // 使用数据库
        try (PreparedStatement stmt = connection.prepareStatement("USE digital_twin")) {
            stmt.execute();
        }

        // 创建表，使用point_key作为标签(TAG)以支持更好的查询性能
        // TDengine中第一列必须是时间戳，且作为主键
        String createTableSQL = "CREATE TABLE IF NOT EXISTS sensor_data (" +
                "ts TIMESTAMP, " +
                "point_value BINARY(2000)) " +
                "TAGS (point_key BINARY(255), deviceName BINARY(255)" +
                ")";

        try (PreparedStatement stmt = connection.prepareStatement(createTableSQL)) {
            stmt.execute();
        }
    }

    public void saveSensorData(SensorData sensorData) {
        if (connection == null) {
            log.error("TDengine连接未初始化");
            return;
        }

        try {
            // 关闭自动提交
            connection.setAutoCommit(false);
            
            // 直接从sensorData对象获取deviceName
            String deviceName = sensorData.getDeviceName();
            
            // 直接从sensorData对象获取ts字段作为时间戳
            Long timestamp = null;
            if (sensorData.getTs() != null) {
                try {
                    timestamp = Long.parseLong(sensorData.getTs());
                } catch (NumberFormatException e) {
                    log.warn("无法解析ts字段为时间戳: {}", sensorData.getTs());
                }
            }
            
            // 如果无法从SensorData获取ts，则使用SensorData的Timestamp字段
            if (timestamp == null) {
                timestamp = sensorData.getTimestamp();
            }
            
            // 遍历所有点位数据
            if (sensorData.getPointDataMap() != null && timestamp != null) {
                int dataCount = 0;
                for (Map.Entry<String, Object> entry : sensorData.getPointDataMap().entrySet()) {
                    // 跳过不需要存储的字段
                    if (EXCLUDED_FIELDS.contains(entry.getKey())) {
                        continue;
                    }
                    
                    // 为每个点位创建子表
                    String subTableName = "sensor_data_" + entry.getKey().replaceAll("[^a-zA-Z0-9_]", "_");
                    String createSubTableSQL = "CREATE TABLE IF NOT EXISTS " + subTableName + 
                            " USING sensor_data TAGS (?, ?)";
                    
                    try (PreparedStatement createStmt = connection.prepareStatement(createSubTableSQL)) {
                        createStmt.setString(1, entry.getKey());
                        createStmt.setString(2, deviceName);
                        createStmt.execute();
                    }
                    
                    // 插入数据到子表
                    String insertSQL = "INSERT INTO " + subTableName + " (ts, point_value) VALUES (?, ?)";
                    try (PreparedStatement insertStmt = connection.prepareStatement(insertSQL)) {
                        insertStmt.setTimestamp(1, new java.sql.Timestamp(timestamp));
                        // 截断过长的值以避免超出字段长度限制
                        String valueStr = String.valueOf(entry.getValue());
                        if (valueStr.length() > 1999) {
                            valueStr = valueStr.substring(0, 1999);
                        }
                        insertStmt.setString(2, valueStr);
                        insertStmt.execute();
                    }
                    
                    dataCount++;
                }
                
                connection.commit();
                log.debug("传感器数据已保存到TDengine，共处理{}条记录: {}", dataCount, sensorData);
            }
            
        } catch (SQLException e) {
            log.error("保存传感器数据到TDengine时出错: {}", e.getMessage(), e);
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                log.error("保存传感器数据到TDengine时回滚失败: {}", rollbackEx.getMessage(), rollbackEx);
            }
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                log.error("恢复自动提交模式失败: {}", e.getMessage(), e);
            }
        }
    }
    
    /**
     * 查询指定时间点的所有点位数据
     * 
     * @param timestamp 时间戳
     * @return 该时间点的所有点位数据
     */
    public void querySensorDataByTimestamp(long timestamp) {
        if (connection == null) {
            log.error("TDengine连接未初始化");
            return;
        }
        
        String querySQL = "SELECT ts, point_key, point_value, deviceName FROM sensor_data WHERE ts = ?";
        try (PreparedStatement stmt = connection.prepareStatement(querySQL)) {
            stmt.setTimestamp(1, new java.sql.Timestamp(timestamp));
            // 实际使用中，您需要处理查询结果
            log.info("查询SQL: {}", stmt.toString());
        } catch (SQLException e) {
            log.error("查询传感器数据时出错: {}", e.getMessage(), e);
        }
    }
}