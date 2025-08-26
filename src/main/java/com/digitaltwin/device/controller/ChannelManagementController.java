package com.digitaltwin.device.controller;

import com.digitaltwin.device.entity.Channel;
import com.digitaltwin.device.service.ChannelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/channels")
@RequiredArgsConstructor
public class ChannelManagementController {
    
    private final ChannelService channelService;
    
    /**
     * 创建Channel
     */
    @PostMapping
    public ResponseEntity<Channel> createChannel(@RequestBody Channel channel) {
        try {
            Channel createdChannel = channelService.createChannel(channel);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdChannel);
        } catch (Exception e) {
            log.error("创建Channel失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(null); // 或者可以考虑抛出异常或返回特定错误对象
        }
    }
    
    /**
     * 根据ID获取Channel
     */
    @GetMapping("/{id}")
    public ResponseEntity<Channel> getChannelById(@PathVariable Long id) {
        try {
            return channelService.getChannelById(id)
                    .map(channel -> ResponseEntity.ok(channel))
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(null));
        } catch (Exception e) {
            log.error("查询Channel失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }
    
    /**
     * 根据名称获取Channel
     */
    @GetMapping("/name/{name}")
    public ResponseEntity<Channel> getChannelByName(@PathVariable String name) {
        try {
            return channelService.getChannelByName(name)
                    .map(channel -> ResponseEntity.ok(channel))
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(null));
        } catch (Exception e) {
            log.error("查询Channel失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }
    
    /**
     * 获取所有Channel
     */
    @GetMapping
    public ResponseEntity<List<Channel>> getAllChannels() {
        List<Channel> channels = channelService.getAllChannels();
        return ResponseEntity.ok(channels);
    }
    
    /**
     * 根据服务器URL获取Channel
     */
    @GetMapping("/server-url/{serverUrl}")
    public ResponseEntity<List<Channel>> getChannelsByServerUrl(@PathVariable String serverUrl) {
        try {
            List<Channel> channels = channelService.getChannelsByServerUrl(serverUrl);
            return ResponseEntity.ok(channels);
        } catch (Exception e) {
            log.error("查询Channel失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }
    
    /**
     * 更新Channel
     */
    @PutMapping("/{id}")
    public ResponseEntity<Channel> updateChannel(@PathVariable Long id, @RequestBody Channel channel) {
        try {
            Channel updatedChannel = channelService.updateChannel(id, channel);
            return ResponseEntity.ok(updatedChannel);
        } catch (Exception e) {
            log.error("更新Channel失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(null);
        }
    }
    
    /**
     * 删除Channel
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteChannel(@PathVariable Long id) {
        try {
            channelService.deleteChannel(id);
            return ResponseEntity.ok("删除成功");
        } catch (Exception e) {
            log.error("删除Channel失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("删除Channel失败: " + e.getMessage());
        }
    }
}