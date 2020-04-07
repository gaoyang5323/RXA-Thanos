package com.kakuiwong.rxathanos.config;

import com.kakuiwong.rxathanos.bean.enums.RxaContextStatusEnum;
import com.kakuiwong.rxathanos.contant.RxaContant;
import com.kakuiwong.rxathanos.core.message.RxaPublisher;
import com.kakuiwong.rxathanos.core.message.RxaSubscribe;
import com.kakuiwong.rxathanos.core.message.redis.RxaRedisPublisher;
import com.kakuiwong.rxathanos.core.message.redis.RxaRedisSubscribeBase;
import com.kakuiwong.rxathanos.core.message.redis.RxaRedisSubscribeSub;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

/**
 * @author gaoyang
 * @email 785175323@qq.com
 */
@Configuration
public class RxaRedisConfiguration {

    @Bean
    public RxaPublisher rxaRedisPub() {
        return new RxaRedisPublisher();
    }

    @Bean
    public RxaSubscribe rxaRedisSub() {
        return new RxaRedisSubscribeSub();
    }

    @Bean
    public RxaSubscribe rxaRedisBase() {
        return new RxaRedisSubscribeBase();
    }

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Bean
    RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(listenerAdapterRxaSub(), new PatternTopic(RxaContextStatusEnum.SUB.rxaType()));
        container.addMessageListener(listenerAdapterRxaBase(), new PatternTopic(RxaContextStatusEnum.BASE.rxaType()));
        return container;
    }

    @Bean
    MessageListenerAdapter listenerAdapterRxaBase() {
        return new MessageListenerAdapter(rxaRedisBase(), RxaContant.REDIS_ONMESSAGE);
    }

    @Bean
    MessageListenerAdapter listenerAdapterRxaSub() {
        return new MessageListenerAdapter(rxaRedisSub(), RxaContant.REDIS_ONMESSAGE);
    }
}
