package com.digitaltwin.device.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class OpcUaConfigData {
    public static final String DeviceName = "MyDevice";

    /**
     * 创建一个默认的OPC UA配置实例
     * 根据提供的JSON数据初始化实体类
     *
     * @return 初始化的OpcUaConfigData实例
     */
    public static OpcUaConfigData createDefaultConfig(String connectorName) {
        OpcUaConfigData configData = new OpcUaConfigData();

        // 初始化tby2对象
        configData.setMode("basic");
        configData.setName(connectorName);
        configData.setType("opcua");
        configData.setEnableRemoteLogging(true);
        configData.setLogLevel("INFO");
        configData.setConfiguration(connectorName + ".json");
        configData.setConfigVersion("3.7.6");
        configData.setTs(System.currentTimeMillis());

        // 初始化configurationJson对象
        ConfigurationJson configurationJson = new ConfigurationJson();
        configurationJson.setLogLevel("INFO");
        configurationJson.setName(connectorName);
        configurationJson.setEnableRemoteLogging(false);
        configurationJson.setId(UUID.randomUUID().toString());
        configurationJson.setConfigVersion("3.7.6");

        // 初始化server对象
        Server server = new Server();
        server.setUrl("localhost:4840/freeopcua/server/");
        server.setTimeoutInMillis(5000);
        server.setScanPeriodInMillis(3600000);
        server.setPollPeriodInMillis(5000);
        server.setEnableSubscriptions(true);
        server.setSubCheckPeriodInMillis(100);
        server.setShowMap(false);
        server.setSecurity("Basic128Rsa15");

        // 初始化identity对象
        Identity identity = new Identity();
        identity.setType("anonymous");
        server.setIdentity(identity);
        configurationJson.setServer(server);

        // 初始化mapping列表
        List<Mapping> mappingList = new ArrayList<>();
        Mapping mapping = new Mapping();
        mapping.setDeviceNodeSource("path");
        mapping.setDeviceNodePattern("Root\\.Objects\\." + DeviceName);

        // 初始化deviceInfo对象
        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.setDeviceNameExpression(connectorName);
        deviceInfo.setDeviceNameExpressionSource("constant");
        deviceInfo.setDeviceProfileExpression(connectorName);
        deviceInfo.setDeviceProfileExpressionSource("constant");
        mapping.setDeviceInfo(deviceInfo);

        // 初始化attributes列表
        List<Attribute> attributes = new ArrayList<>();
//        Attribute attribute = new Attribute();
//        attribute.setKey("Info");
//        attribute.setType("path");
//        attribute.setValue("${Root\\.Objects\\.MyDevice\\.Info}");
//        attributes.add(attribute);
        mapping.setAttributes(attributes);

        // 初始化timeseries列表
        List<Timeseries> timeseriesList = new ArrayList<>();

//        Timeseries timeseries1 = new Timeseries();
//        timeseries1.setKey("CoolingWater_In_Temp");
//        timeseries1.setType("path");
//        timeseries1.setValue("${Root\\.Objects\\.MyDevice\\.Temperature\\.CoolingWater_In_Temp}");
//        timeseriesList.add(timeseries1);
//
//        Timeseries timeseries2 = new Timeseries();
//        timeseries2.setKey("HeatFlux");
//        timeseries2.setType("path");
//        timeseries2.setValue("${Root\\.Objects\\.MyDevice\\.HeatFlux\\.HeatFlux}");
//        timeseriesList.add(timeseries2);
//
//        Timeseries timeseries3 = new Timeseries();
//        timeseries3.setKey("AirPressure1");
//        timeseries3.setType("path");
//        timeseries3.setValue("${Root\\.Objects\\.MyDevice\\.Pressure\\.AirPressure1}");
//        timeseriesList.add(timeseries3);

        mapping.setTimeseries(timeseriesList);

        // 初始化rpcMethods和attributesUpdates为空列表
        mapping.setRpcMethods(new ArrayList<>());
        mapping.setAttributesUpdates(new ArrayList<>());

        mappingList.add(mapping);
        configurationJson.setMapping(mappingList);

        configData.setConfigurationJson(configurationJson);
        return configData;
    }

    private String mode;
    private String name;
    private String type;

    @JsonProperty("enableRemoteLogging")
    private Boolean enableRemoteLogging;

    @JsonProperty("logLevel")
    private String logLevel;

    private String configuration;

    @JsonProperty("configurationJson")
    private ConfigurationJson configurationJson;

    @JsonProperty("configVersion")
    private String configVersion;

    private Long ts;

    @Data
    public static class ConfigurationJson {
        private Server server;
        private List<Mapping> mapping;

        @JsonProperty("logLevel")
        private String logLevel;

        private String name;

        @JsonProperty("enableRemoteLogging")
        private Boolean enableRemoteLogging;

        private String id;

        @JsonProperty("configVersion")
        private String configVersion;
    }

    @Data
    public static class Server {
        private String url;

        @JsonProperty("timeoutInMillis")
        private Integer timeoutInMillis;

        @JsonProperty("scanPeriodInMillis")
        private Integer scanPeriodInMillis;

        @JsonProperty("pollPeriodInMillis")
        private Integer pollPeriodInMillis;

        @JsonProperty("enableSubscriptions")
        private Boolean enableSubscriptions;

        @JsonProperty("subCheckPeriodInMillis")
        private Integer subCheckPeriodInMillis;

        @JsonProperty("showMap")
        private Boolean showMap;

        private String security;
        private Identity identity;
    }

    @Data
    public static class Identity {
        private String type;
    }

    @Data
    public static class Mapping {
        @JsonProperty("deviceNodeSource")
        private String deviceNodeSource;

        @JsonProperty("deviceNodePattern")
        private String deviceNodePattern;

        @JsonProperty("deviceInfo")
        private DeviceInfo deviceInfo;

        private List<Attribute> attributes;
        private List<Timeseries> timeseries;

        @JsonProperty("rpc_methods")
        private List<Object> rpcMethods;

        @JsonProperty("attributes_updates")
        private List<Object> attributesUpdates;
    }

    @Data
    public static class DeviceInfo {
        @JsonProperty("deviceNameExpression")
        private String deviceNameExpression;

        @JsonProperty("deviceNameExpressionSource")
        private String deviceNameExpressionSource;

        @JsonProperty("deviceProfileExpression")
        private String deviceProfileExpression;

        @JsonProperty("deviceProfileExpressionSource")
        private String deviceProfileExpressionSource;
    }

    @Data
    public static class Attribute {
        private String key;
        private String type;
        private String value;
    }

    @Data
    public static class Timeseries {
        private String key;
        private String type;
        private String value;
    }
}