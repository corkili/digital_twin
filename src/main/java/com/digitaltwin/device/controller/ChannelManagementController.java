package com.digitaltwin.device.controller;

import com.digitaltwin.device.dto.ApiResponse;
import com.digitaltwin.device.entity.Channel;
import com.digitaltwin.device.service.ChannelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/channels")
@RequiredArgsConstructor
@Tag(name = "通道管理", description = "提供通道的增删改查管理接口")
public class ChannelManagementController {
    
    private final ChannelService channelService;
    
    /**
     * 创建Channel
     */
    @PostMapping
    public ResponseEntity<ApiResponse> createChannel(@RequestBody Channel channel) {
        try {
            Channel createdChannel = channelService.createChannel(channel);
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("创建成功", createdChannel));
        } catch (Exception e) {
            log.error("创建Channel失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("创建Channel失败: " + e.getMessage()));
        }
    }
    
    /**
     * 根据ID获取Channel
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getChannelById(@PathVariable Long id) {
        try {
            return channelService.getChannelById(id)
                    .map(channel -> ResponseEntity.ok(ApiResponse.success("查询成功", channel)))
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(ApiResponse.error("未找到ID为 " + id + " 的Channel")));
        } catch (Exception e) {
            log.error("查询Channel失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("查询Channel失败: " + e.getMessage()));
        }
    }
    
    /**
     * 根据名称获取Channel
     */
    @GetMapping("/name/{name}")
    public ResponseEntity<ApiResponse> getChannelByName(@PathVariable String name) {
        try {
            return channelService.getChannelByName(name)
                    .map(channel -> ResponseEntity.ok(ApiResponse.success("查询成功", channel)))
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(ApiResponse.error("未找到名称为 " + name + " 的Channel")));
        } catch (Exception e) {
            log.error("查询Channel失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("查询Channel失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取所有Channel（不带分页参数时返回全部数据）
     */
    @Operation(summary = "获取所有通道", description = "获取所有通道信息列表")
    @GetMapping
    public ResponseEntity<ApiResponse> getAllChannels() {
        try {
            // 返回全部数据
            List<Channel> channels = channelService.getAllChannels();
            return ResponseEntity.ok(ApiResponse.success("查询成功", channels));
        } catch (Exception e) {
            log.error("查询Channel失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("查询Channel失败: " + e.getMessage()));
        }
    }

    /**
     * 分页获取所有Channel（提供page和size参数时返回分页数据）
     *
     * @param pageable 分页参数
     */
    @Operation(summary = "分页获取通道列表", description = "分页查询通道信息")
    @GetMapping(params = {"page", "size"})
    public ResponseEntity<ApiResponse> getAllChannelsWithPagination(Pageable pageable) {
        try {
            // 使用分页查询
            Page<Channel> channelPage = channelService.getAllChannels(pageable);
            return ResponseEntity.ok(ApiResponse.success("分页查询成功", channelPage));
        } catch (Exception e) {
            log.error("分页查询Channel失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("分页查询Channel失败: " + e.getMessage()));
        }
    }
    
    /**
     * 根据服务器URL获取Channel
     */
    @GetMapping("/server-url/{serverUrl}")
    public ResponseEntity<ApiResponse> getChannelsByServerUrl(@PathVariable String serverUrl) {
        try {
            List<Channel> channels = channelService.getChannelsByServerUrl(serverUrl);
            return ResponseEntity.ok(ApiResponse.success("查询成功", channels));
        } catch (Exception e) {
            log.error("查询Channel失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("查询Channel失败: " + e.getMessage()));
        }
    }
    
    /**
     * 更新Channel
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateChannel(@PathVariable Long id, @RequestBody Channel channel) {
        try {
            Channel updatedChannel = channelService.updateChannel(id, channel);
            return ResponseEntity.ok(ApiResponse.success("更新成功", updatedChannel));
        } catch (Exception e) {
            log.error("更新Channel失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("更新Channel失败: " + e.getMessage()));
        }
    }

    /**
     * 删除Channel
     * 批量删除通道
     */
    @DeleteMapping("/{ids}")
    public ResponseEntity<ApiResponse> deleteChannel(@PathVariable List<Long> ids) {
        try {
            channelService.deleteChannels(ids);
            return ResponseEntity.ok(ApiResponse.success("批量删除成功"));
        } catch (Exception e) {
            log.error("批量删除Channel失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("批量删除Channel失败: " + e.getMessage()));
        }
    }

    /**
     * 批量删除通道
     */
    @Operation(summary = "批量删除通道", description = "根据通道ID列表批量删除通道信息")
    @PostMapping("/batch-delete")
    public ResponseEntity<ApiResponse> batchDeleteChannels(@Valid @RequestBody List<Long> ids) {
        try {
            channelService.deleteChannels(ids);
            return ResponseEntity.ok(ApiResponse.success("批量删除成功"));
        } catch (Exception e) {
            log.error("批量删除Channel失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("批量删除Channel失败: " + e.getMessage()));
        }
    }
}