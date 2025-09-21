package com.digitaltwin.trial.service;

import com.digitaltwin.trial.config.TrailHistoryCacheConfig;
import com.digitaltwin.trial.dto.TrailHistoryData;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrailHistoryCacheService {
    
    private final TrailHistoryCacheConfig cacheConfig;
    
    private Cache<Long, List<TrailHistoryData>> cache;
    
    @PostConstruct
    public void init() {
        cache = Caffeine.newBuilder()
                .maximumSize(cacheConfig.getMaximumSize())
                .expireAfterWrite(cacheConfig.getExpireAfterWrite(), TimeUnit.MILLISECONDS)
                .recordStats()
                .build();
        
        log.info("TrailHistory缓存服务初始化完成，最大条目数: {}, 过期时间: {}ms", 
                cacheConfig.getMaximumSize(), cacheConfig.getExpireAfterWrite());
    }
    
    /**
     * 将TrailHistoryData列表放入缓存
     * @param trialId 试验ID
     * @param data TrailHistoryData列表
     */
    public void put(Long trialId, List<TrailHistoryData> data) {
        // 创建不包含uniqueId的新列表
        List<TrailHistoryData> cachedData = data.stream()
                .map(item -> {
                    TrailHistoryData cachedItem = new TrailHistoryData();
                    cachedItem.setTimestamp(item.getTimestamp());
                    cachedItem.setPointsData(item.getPointsData());
                    // 不设置subscribeId（uniqueId）
                    return cachedItem;
                })
                .collect(java.util.stream.Collectors.toList());
        
        cache.put(trialId, cachedData);
        log.debug("缓存已更新，trialId: {}", trialId);
    }
    
    /**
     * 从缓存中获取TrailHistoryData列表
     * @param trialId 试验ID
     * @return TrailHistoryData列表，如果缓存中不存在则返回null
     */
    public List<TrailHistoryData> getIfPresent(Long trialId) {
        List<TrailHistoryData> data = cache.getIfPresent(trialId);
        if (data != null) {
            log.debug("缓存命中，trialId: {}", trialId);
        } else {
            log.debug("缓存未命中，trialId: {}", trialId);
        }
        return data;
    }
    
    /**
     * 从缓存中移除指定trialId的数据
     * @param trialId 试验ID
     */
    public void invalidate(Long trialId) {
        cache.invalidate(trialId);
        log.debug("缓存已移除，trialId: {}", trialId);
    }
    
    /**
     * 获取缓存统计信息
     * @return 缓存统计信息字符串
     */
    public String getCacheStats() {
        return cache.stats().toString();
    }
}