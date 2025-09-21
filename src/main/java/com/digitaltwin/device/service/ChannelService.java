package com.digitaltwin.device.service;

import com.digitaltwin.device.dto.OpcUaConfigData;
import com.digitaltwin.device.entity.Channel;
import com.digitaltwin.device.repository.ChannelRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import com.digitaltwin.system.util.SecurityContext;
import com.digitaltwin.system.entity.User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ChannelService {

    private final static ObjectMapper ObjectMapper = new ObjectMapper();
    private final ChannelRepository channelRepository;
    private final OpcUaConfigService opcUaConfigService;

    public ChannelService(ChannelRepository channelRepository, OpcUaConfigService opcUaConfigService) {
        this.channelRepository = channelRepository;
        this.opcUaConfigService = opcUaConfigService;
    }

    /**
     * 创建Channel
     *
     * @param channel Channel实体
     * @return 保存后的Channel实体
     */
    public Channel createChannel(Channel channel) {
        if (channelRepository.existsByName(channel.getName())) {
            throw new RuntimeException("Channel名称已存在: " + channel.getName());
        }

        List<String> totalConnectorNames = channelRepository.findAll().stream()
                .map(Channel::getName)
                .collect(Collectors.toList());

        totalConnectorNames.add(channel.getName());
        // 创建默认配置
        OpcUaConfigData configData = OpcUaConfigData.createDefaultConfig(channel.getName(), channel.getServerUrl());

        // 更新服务器URL为用户提供的URL
        configData.getConfigurationJson().getServer().setUrl(channel.getServerUrl());
        opcUaConfigService.activeConnectors(totalConnectorNames);
        // 发送配置到目标URL
        String result = opcUaConfigService.sendOpcUaConfig(configData);

        String opcUaConfigString = null;
        try {
            opcUaConfigString = ObjectMapper.writeValueAsString(configData);
        } catch (JsonProcessingException e) {
            log.error("ThingsBoard配置保存失败： ", e);
        }
        channel.setOpcUaConfig(opcUaConfigString);

        // 设置审计创建人/修改人
        User currentUser = SecurityContext.getCurrentUser();
        if (currentUser != null) {
            channel.setCreatedBy(currentUser.getId());
            channel.setUpdatedBy(currentUser.getId());
        }
        Channel savedChannel = channelRepository.save(channel);


        log.info("创建Channel成功，ID: {}", savedChannel.getId());
        return savedChannel;
    }

    /**
     * 根据ID获取Channel
     *
     * @param id Channel ID
     * @return Channel实体
     */
    public Optional<Channel> getChannelById(Long id) {
        return channelRepository.findById(id);
    }

    /**
     * 根据名称获取Channel
     *
     * @param name Channel名称
     * @return Channel实体
     */
    public Optional<Channel> getChannelByName(String name) {
        return channelRepository.findByName(name);
    }

    /**
     * 获取所有Channel
     *
     * @return Channel列表
     */
    public List<Channel> getAllChannels() {
        return channelRepository.findAll();
    }

    /**
     * 更新Channel
     *
     * @param id      Channel ID
     * @param channel 更新的Channel信息
     * @return 更新后的Channel实体
     */
    public Channel updateChannel(Long id, Channel channel) {
        if (!channelRepository.existsById(id)) {
            throw new RuntimeException("Channel不存在，ID: " + id);
        }

        Optional<Channel> channelOptional = channelRepository.findById( id);
        Channel oldChannel = channelOptional.get();
        oldChannel.setName(channel.getName());
        oldChannel.setServerUrl(channel.getServerUrl());
        oldChannel.setDescription(channel.getDescription());
        // 设置审计修改人
        User currentUser = SecurityContext.getCurrentUser();
        if (currentUser != null) {
            oldChannel.setUpdatedBy(currentUser.getId());
        }
        Channel updatedChannel = channelRepository.save(oldChannel);
        log.info("更新Channel成功，ID: {}", id);
        return updatedChannel;
    }

    /**
     * 删除Channel
     *
     * @param id Channel ID
     */
    public void deleteChannel(Long id) {
        if (!channelRepository.existsById(id)) {
            throw new RuntimeException("Channel不存在，ID: " + id);
        }

        channelRepository.deleteById(id);
        log.info("删除Channel成功，ID: {}", id);
    }

    /**
     * 批量删除Channel
     *
     * @param ids Channel ID列表
     */
    public void deleteChannels(List<Long> ids) {
        List<Channel> channels = channelRepository.findAllById(ids);
        
        if (channels.size() != ids.size()) {
            // 找出不存在的ID
            List<Long> existingIds = channels.stream().map(Channel::getId).collect(Collectors.toList());
            List<Long> notFoundIds = ids.stream()
                    .filter(id -> !existingIds.contains(id))
                    .collect(Collectors.toList());
            throw new RuntimeException("以下Channel不存在: " + notFoundIds);
        }
        
        channelRepository.deleteAll(channels);
        log.info("批量删除Channel成功，IDs: {}", ids);
    }

    /**
     * 根据服务器URL查找Channel
     *
     * @param serverUrl 服务器URL
     * @return Channel列表
     */
    public List<Channel> getChannelsByServerUrl(String serverUrl) {
        return channelRepository.findByServerUrl(serverUrl);
    }
}