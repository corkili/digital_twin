# 点位缓存系统文档

## 概述

为了解决告警分析服务频繁查询数据库导致的性能问题，我们实现了一个基于内存的点位缓存系统。

## 架构设计

### 1. PointCacheService

- **位置**: `com.digitaltwin.alarm.service.PointCacheService`
- **功能**: 提供点位数据的内存缓存，减少数据库查询压力
- **存储结构**: 
  - 使用 `ConcurrentHashMap<String, List<Point>>` 存储点位数据
  - Key: Point.identity (点位标识)
  - Value: List<Point> (具有相同标识的点位列表)

### 2. 缓存刷新机制

- **刷新频率**: 每30秒自动刷新一次
- **刷新方式**: 全量刷新，从数据库查询所有点位数据
- **数据加载**: 确保在刷新时加载Point的Device关联，避免懒加载问题
- **并发安全**: 使用ConcurrentHashMap保证线程安全

### 3. 初始化机制

- **启动时初始化**: 使用 `@EventListener(ApplicationReadyEvent.class)` 确保应用启动时完成缓存初始化
- **失败处理**: 如果缓存初始化失败，应用启动将终止
- **等待机制**: 提供CountDownLatch确保缓存初始化完成后再提供服务

### 4. 查询机制

- **缓存查询**: 所有点位查询都通过缓存进行
- **未命中处理**: 如果缓存中未找到点位，直接返回空列表，不会查询数据库
- **日志级别**: 将"未找到点位"的日志级别从WARN调整为DEBUG，减少日志噪音

## 使用说明

### 1. 依赖注入

在需要使用点位数据的服务中，注入 `PointCacheService`：

```java
@Service
public class YourService {
    private final PointCacheService pointCacheService;
    
    public YourService(PointCacheService pointCacheService) {
        this.pointCacheService = pointCacheService;
    }
    
    public void yourMethod() {
        List<Point> points = pointCacheService.getPointsByIdentity("point.identity");
        // 处理点位数据
    }
}
```

### 2. 缓存状态监控

- **缓存大小**: 使用 `pointCacheService.getCacheSize()` 获取当前缓存的点位数量
- **初始化状态**: 使用 `pointCacheService.isInitialized()` 检查缓存是否已初始化

### 3. 配置要求

确保在主应用类中启用了调度功能：

```java
@SpringBootApplication
@EnableScheduling  // 必须启用
@EnableAsync       // 已启用
public class Application {
    // ...
}
```

## 性能优化

### 1. 数据库查询优化

- **减少查询**: 从每次告警分析都查询数据库改为每30秒查询一次
- **批量加载**: 一次性加载所有点位数据，避免N+1查询问题
- **关联加载**: 确保Device关联在刷新时就被加载，避免懒加载导致的额外查询

### 2. 内存使用

- **内存占用**: 缓存所有点位数据，适合点位数量在万级以内的场景
- **并发访问**: 使用ConcurrentHashMap支持高并发读取
- **内存释放**: 应用关闭时自动清理缓存资源

## 监控和日志

### 1. 日志级别

- **INFO**: 缓存初始化完成、缓存刷新完成
- **DEBUG**: 缓存刷新开始、点位未找到等详细信息
- **ERROR**: 缓存初始化失败、刷新失败等错误信息

### 2. 监控指标

- **缓存命中率**: 通过日志分析点位查询的缓存命中率
- **刷新耗时**: 监控每次全量刷新的时间消耗
- **内存使用**: 监控缓存占用的内存大小

## 测试

运行缓存服务测试：

```bash
mvn test -Dtest=PointCacheServiceTest
```

## 故障排除

### 1. 缓存未初始化

- **症状**: 应用启动失败，日志显示"点位缓存初始化失败"
- **解决**: 检查数据库连接和点位表数据

### 2. 点位数据不一致

- **症状**: 新增或修改的点位在缓存中不可见
- **解决**: 等待30秒缓存自动刷新，或重启应用强制刷新

### 3. 内存占用过高

- **症状**: 应用内存使用异常增加
- **解决**: 监控点位数量，考虑使用LRU等淘汰策略