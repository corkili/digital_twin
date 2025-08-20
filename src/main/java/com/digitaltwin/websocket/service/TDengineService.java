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

@Slf4j
@Service
@RequiredArgsConstructor
public class TDengineService {

    private final TDengineConfig tdengineConfig;

    private Connection connection;

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

        // 创建表
        String createTableSQL = "CREATE TABLE IF NOT EXISTS sensor_data (" +
                "ts TIMESTAMP, " +
                "id BINARY(64), " +
                "heat_flux DOUBLE, " +
                "cooling_water_in_temp DOUBLE" +
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

        String insertSQL = "INSERT INTO sensor_data (ts, id, heat_flux, cooling_water_in_temp) VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(insertSQL)) {
            stmt.setTimestamp(1, new java.sql.Timestamp(sensorData.getTimestamp()));
            stmt.setString(2, sensorData.getID());
            stmt.setDouble(3, sensorData.getHeatFlux());
            stmt.setDouble(4, sensorData.getCoolingWaterInTemp());
            stmt.execute();
            log.debug("传感器数据已保存到TDengine: {}", sensorData);
        } catch (SQLException e) {
            log.error("保存传感器数据到TDengine时出错: {}", e.getMessage(), e);
        }
    }
}
