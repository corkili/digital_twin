package com.digitaltwin.alarm.service;

import com.digitaltwin.device.entity.Point;
import com.digitaltwin.device.repository.PointRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 点位缓存服务
 * 提供基于内存的点位缓存，减少数据库查询压力
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PointCacheService {

    private final PointRepository pointRepository;
    
    // 点位缓存：key为Point.identity，value为List<Point>
    private volatile Map<String, List<Point>> pointCache = new HashMap<>();
    
    // 初始化完成标志
    private volatile boolean initialized = false;
    private final CountDownLatch initLatch = new CountDownLatch(1);

    /**
     * 应用启动时初始化缓存
     * 确保程序启动失败如果缓存初始化失败
     */
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void onApplicationReady() {
        try {
            log.info("开始初始化点位缓存...");
            refreshCache();
            initialized = true;
            initLatch.countDown();
            log.info("点位缓存初始化完成，共缓存 {} 个点位", pointCache.size());
        } catch (Exception e) {
            log.error("点位缓存初始化失败，程序启动终止", e);
            throw new RuntimeException("点位缓存初始化失败", e);
        }
    }

    /**
     * 定时刷新缓存，每30秒执行一次
     * 确保在刷新时加载Point的Device关联，避免懒加载问题
     */
    @Scheduled(fixedDelay = 30000)
    @Transactional
    public void refreshCache() {
        log.debug("开始刷新点位缓存...");
        try {
            // 查询所有点位并确保Device被加载
            List<Point> allPoints = pointRepository.findAll();
            
            // 强制初始化Device关联（解决懒加载问题）
            allPoints.forEach(point -> {
                if (point.getDevice() != null) {
                    point.getDevice().getId(); // 触发懒加载
                    point.getDevice().getName(); // 确保Device的其他属性也被加载
                }
            });
            
            // 在本地创建新的缓存Map，避免并发读写问题
            Map<String, List<Point>> newCache = new HashMap<>();
            allPoints.forEach(point -> {
                String identity = point.getIdentity();
                newCache.computeIfAbsent(identity, k -> new java.util.ArrayList<>()).add(point);
            });
            
            // 原子性更新缓存引用
            pointCache = newCache;
            
            log.debug("点位缓存刷新完成，共缓存 {} 个点位", pointCache.size());
            
        } catch (Exception e) {
            log.error("点位缓存刷新失败", e);
            // 如果缓存已初始化，则继续保留旧缓存
            // 如果缓存未初始化，则抛出异常阻止程序启动
            if (!initialized) {
                throw new RuntimeException("点位缓存刷新失败", e);
            }
        }
    }

    /**
     * 根据点位标识获取点位列表
     * 直接从缓存中获取，如果未找到则返回空列表（不会查询数据库）
     * 
     * @param identity 点位标识
     * @return 点位列表，可能为空
     */
    public List<Point> getPointsByIdentity(String identity) {
        if (!initialized) {
            try {
                // 等待初始化完成，最多等待30秒
                if (!initLatch.await(30, TimeUnit.SECONDS)) {
                    log.error("点位缓存尚未初始化完成");
                    return List.of();
                }
            } catch (InterruptedException e) {
                log.error("等待点位缓存初始化时发生中断", e);
                Thread.currentThread().interrupt();
                return List.of();
            }
        }
        
        return pointCache.getOrDefault(identity, List.of());
    }

    /**
     * 获取缓存大小
     * 
     * @return 缓存中的点位数量
     */
    public int getCacheSize() {
        return pointCache.size();
    }

    /**
     * 检查缓存是否已初始化
     * 
     * @return true如果缓存已初始化
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * 应用关闭时清理资源
     */
    @PreDestroy
    public void destroy() {
        log.info("正在清理点位缓存...");
        pointCache = new HashMap<>();
        log.info("点位缓存已清理");
    }
}