package com.kakuiwong.rxathanos.config;

import com.kakuiwong.rxathanos.bean.enums.RxaContextStatusEnum;
import com.kakuiwong.rxathanos.core.Interception.RxaFeignRequestInterception;
import com.kakuiwong.rxathanos.core.Interception.RxaHandlerInterceptor;
import com.kakuiwong.rxathanos.core.Interception.RxaRequestInterception;
import com.kakuiwong.rxathanos.core.aop.RxaAdvisor;
import com.kakuiwong.rxathanos.core.redis.RxaRedisPublisher;
import com.kakuiwong.rxathanos.core.redis.RxaRedisSubscribe;
import com.kakuiwong.rxathanos.core.redis.RxaRedisSubscribeBase;
import com.kakuiwong.rxathanos.core.redis.RxaRedisSubscribeSub;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Collections;

/**
 * @author gaoyang
 * @email 785175323@qq.com
 */
//@ConditionalOnClass({TransactionManager.class})
@Configuration
public class RxaConfig implements WebMvcConfigurer, InitializingBean {

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private PlatformTransactionManager txManager;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RxaHandlerInterceptor()).addPathPatterns("/**");
    }

    @Bean
    public RxaFeignRequestInterception rxaFeignRequestInterception() {
        return new RxaFeignRequestInterception();
    }

    @Bean
    public RestTemplate rxaRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setInterceptors(Collections.singletonList(new RxaRequestInterception()));
        return restTemplate;
    }

    @Bean
    public RxaAdvisor rxaAdvisor() {
        return new RxaAdvisor(txManager);
    }

    @Bean
    public RxaRedisPublisher rxaRedisPub() {
        return new RxaRedisPublisher();
    }

    @Bean
    public RxaRedisSubscribe rxaRedisSub() {
        return new RxaRedisSubscribeSub();
    }

    @Bean
    public RxaRedisSubscribe rxaRedisBase() {
        return new RxaRedisSubscribeBase();
    }

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Bean
    RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(listenerAdapterSub(), new PatternTopic(RxaContextStatusEnum.SUB.rxaType()));
        container.addMessageListener(listenerAdapterBase(), new PatternTopic(RxaContextStatusEnum.BASE.rxaType()));
        return container;
    }

    @Bean
    MessageListenerAdapter listenerAdapterBase() {
        return new MessageListenerAdapter(rxaRedisBase(), RxaRedisSubscribe.ONMESSAGE);
    }

    @Bean
    MessageListenerAdapter listenerAdapterSub() {
        return new MessageListenerAdapter(rxaRedisSub(), RxaRedisSubscribe.ONMESSAGE);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("__________ ____  ___   _____          ___________.__                                    \n" +
                "\\______   \\\\   \\/  /  /  _  \\         \\__    ___/|  |__  _____     ____    ____   ______\n" +
                " |       _/ \\     /  /  /_\\  \\   ______ |    |   |  |  \\ \\__  \\   /    \\  /  _ \\ /  ___/\n" +
                " |    |   \\ /     \\ /    |    \\ /_____/ |    |   |   Y  \\ / __ \\_|   |  \\(  <_> )\\___ \\ \n" +
                " |____|_  //___/\\  \\\\____|__  /         |____|   |___|  /(____  /|___|  / \\____//____  >\n" +
                "        \\/       \\_/        \\/                        \\/      \\/      \\/             \\/     1.0-SNAPSHOT by GY");
    }
}
