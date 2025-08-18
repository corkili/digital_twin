package com.digitaltwin.websocket.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * RabbitMQ配置类
 * 配置队列、交换器、绑定和消息转换器
 */
@Configuration
public class RabbitMQConfig {

    // // 队列名称
    // public static final String SENSOR_DATA_QUEUE = "sensor.data.queue";
    
    // // 交换器名称
    // public static final String SENSOR_DATA_EXCHANGE = "sensor.data.exchange";
    
    // // 路由键
    // public static final String SENSOR_DATA_ROUTING_KEY = "sensor.data";

    // 队列名称
    public static final String SENSOR_DATA_QUEUE = "tbyqueue";
    
    // 交换器名称
    public static final String SENSOR_DATA_EXCHANGE = "tby";
    
    // 路由键
    public static final String SENSOR_DATA_ROUTING_KEY = "tby";

    /**
     * 声明队列
     */
    @Bean
    public Queue sensorDataQueue() {
        return QueueBuilder.durable(SENSOR_DATA_QUEUE)
                .build();
    }

    /**
     * 声明交换器
     */
    @Bean
    public TopicExchange sensorDataExchange() {
        return ExchangeBuilder.topicExchange(SENSOR_DATA_EXCHANGE)
                .durable(true)
                .build();
    }

    /**
     * 绑定队列到交换器
     */
    @Bean
    public Binding sensorDataBinding() {
        return BindingBuilder
                .bind(sensorDataQueue())
                .to(sensorDataExchange())
                .with(SENSOR_DATA_ROUTING_KEY);
    }

    /**
     * 配置消息转换器为JSON格式
     */
    @Bean
    public MessageConverter messageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    /**
     * 配置RabbitTemplate使用JSON消息转换器
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        return rabbitTemplate;
    }

    /**
     * 配置RabbitListener容器工厂
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter());
        factory.setConcurrentConsumers(1); // 确保按顺序处理消息
        factory.setMaxConcurrentConsumers(1);
        return factory;
    }
}