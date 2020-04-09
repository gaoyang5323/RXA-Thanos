package com.kakuiwong.rxathanos.config;

import com.kakuiwong.rxathanos.contant.RxaContant;
import com.kakuiwong.rxathanos.core.message.RxaPublisher;
import com.kakuiwong.rxathanos.core.message.RxaSubscribe;
import com.kakuiwong.rxathanos.core.message.rabbitmq.RxaMqPublisher;
import com.kakuiwong.rxathanos.core.message.rabbitmq.RxaMqSubscribeBase;
import com.kakuiwong.rxathanos.core.message.rabbitmq.RxaMqSubscribeSub;
import org.springframework.amqp.core.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author gaoyang
 * @email 785175323@qq.com
 */
@ConditionalOnProperty(prefix = RxaContant.RXA_CONFIG_PREFIX,
        name = RxaContant.RXA_CONFIG_MESSSAGE,
        havingValue = RxaContant.MQ,
        matchIfMissing = false)
@Configuration
public class RxaMqConfiguration {

    @Bean
    DirectExchange directExchange() {
        return new DirectExchange(RxaContant.RXA_TOPICEXCHANGE);
    }

    @Bean
    public Queue queueRxaBase() {
        return new Queue(RxaContant.RXA_BASE_QUEUE);
    }

    @Bean
    public Queue queueRxaSub() {
        return new Queue(RxaContant.RXA_SUB_QUEUE);
    }

    @Bean
    public Binding bindingExchangeWithRxaBase() {
        return BindingBuilder.bind(queueRxaBase()).to(directExchange()).with(RxaContant.RXA_BASE_QUEUE);
    }

    @Bean
    public Binding bindingExchangeWithRxaSub() {
        return BindingBuilder.bind(queueRxaSub()).to(directExchange()).with(RxaContant.RXA_SUB_QUEUE);
    }

    @Bean
    public RxaPublisher rxaMqPublisher() {
        return new RxaMqPublisher();
    }

    @Bean
    public RxaSubscribe rxaMqSubscribeBase() {
        return new RxaMqSubscribeBase();
    }

    @Bean
    public RxaSubscribe rxaMqSubscribeSub() {
        return new RxaMqSubscribeSub();
    }
}
