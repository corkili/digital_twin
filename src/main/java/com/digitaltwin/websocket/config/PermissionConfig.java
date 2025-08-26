package com.digitaltwin.websocket.config;

import com.digitaltwin.websocket.model.Permission;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.yaml.snakeyaml.Yaml;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 权限配置类
 * 负责加载和管理系统中的权限配置
 */
@Slf4j
@Configuration
@EnableConfigurationProperties
public class PermissionConfig {

    private final Map<String, Permission> permissionMap = new HashMap<>();

    /**
     * 初始化权限配置
     */
    @PostConstruct
    public void init() {
        try {
            loadPermissionsFromYaml();
            log.info("成功加载权限配置，共加载 {} 个权限", permissionMap.size());
        } catch (IOException e) {
            log.error("加载权限配置文件失败", e);
        }
    }

    /**
     * 从YAML文件加载权限配置
     */
    private void loadPermissionsFromYaml() throws IOException {
        Yaml yaml = new Yaml();
        try (InputStream inputStream = new ClassPathResource("permissions.yml").getInputStream()) {
            Map<String, LinkedHashMap<String, String>> permissions = yaml.load(inputStream);
            if (permissions != null) {
                permissions.forEach((code, props) -> {
                    Permission permission = new Permission();
                    permission.setCode(code);
                    permission.setName(props.get("name"));
                    permission.setDescription(props.get("desc"));
                    permissionMap.put(code, permission);
                });
            }
        }
    }

    /**
     * 获取所有权限列表
     */
    @Bean
    public List<Permission> allPermissions() {
        return permissionMap.values().stream().collect(Collectors.toList());
    }

    /**
     * 根据权限标识获取权限对象
     */
    public Permission getPermission(String code) {
        return permissionMap.get(code);
    }

    /**
     * 检查权限是否存在
     */
    public boolean hasPermission(String code) {
        return permissionMap.containsKey(code);
    }
}