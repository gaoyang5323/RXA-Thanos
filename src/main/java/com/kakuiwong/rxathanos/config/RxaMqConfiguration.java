package com.kakuiwong.rxathanos.config;

import com.kakuiwong.rxathanos.contant.RxaContant;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.handler.annotation.Header;

/**
 * @author gaoyang
 * @email 785175323@qq.com
 */
@ConditionalOnProperty(prefix = RxaContant.RXA_CONFIG_PREFIX,
        name = RxaContant.RXA_CONFIG_MESSSAGE,
        havingValue = RxaContant.MQ,
        matchIfMissing = false)
@Configuration
@ConfigurationProperties(prefix = "rxa.mqtt")
@IntegrationComponentScan
public class RxaMqConfiguration {

    private String username;

    private String password;

    private String[] hostUrls;

    private String clientId = RxaContant.RXA_CONFIG_PREFIX;

    private int completionTimeout = 3000;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String[] getHostUrls() {
        return hostUrls;
    }

    public void setHostUrls(String[] hostUrls) {
        this.hostUrls = hostUrls;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public int getCompletionTimeout() {
        return completionTimeout;
    }

    public void setCompletionTimeout(int completionTimeout) {
        this.completionTimeout = completionTimeout;
    }

    @Bean
    public MqttConnectOptions getMqttConnectOptions() {
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setCleanSession(true);
        mqttConnectOptions.setConnectionTimeout(10);
        mqttConnectOptions.setKeepAliveInterval(90);
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setUserName(username);
        mqttConnectOptions.setPassword(password.toCharArray());
        mqttConnectOptions.setServerURIs(hostUrls);
        mqttConnectOptions.setKeepAliveInterval(2);
        return mqttConnectOptions;
    }

    @Bean
    public MqttPahoClientFactory mqttClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        factory.setConnectionOptions(getMqttConnectOptions());
        return factory;
    }

    @Bean
    public MessageProducer inboundBase() {
        MqttPahoMessageDrivenChannelAdapter adapter =
                new MqttPahoMessageDrivenChannelAdapter(clientId + "_inboundBase", mqttClientFactory(),
                        RxaContant.RXA_BASE_TOPIC);
        adapter.setCompletionTimeout(completionTimeout);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(3);
        adapter.setOutputChannel(mqttOutboundChanneBase());
        return adapter;
    }

    @Bean
    public MessageProducer inboundSub() {
        MqttPahoMessageDrivenChannelAdapter adapter =
                new MqttPahoMessageDrivenChannelAdapter(clientId + "_inboundSub", mqttClientFactory(),
                        RxaContant.RXA_SUB_TOPIC);
        adapter.setCompletionTimeout(completionTimeout);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(3);
        adapter.setOutputChannel(mqttOutboundChannelSub());
        return adapter;
    }

    @Bean
    public MessageChannel mqttOutboundChanneBase() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel mqttOutboundChannelSub() {
        return new DirectChannel();
    }


    @Bean
    @ServiceActivator(inputChannel = "mqttOutboundChanneBase")
    public MessageHandler mqttOutboundBase() {
        MqttPahoMessageHandler messageHandler = new MqttPahoMessageHandler(clientId, mqttClientFactory());
        messageHandler.setAsync(true);
        //messageHandler.setDefaultTopic(defaultTopic);
        return messageHandler;
    }

    @Bean
    @ServiceActivator(inputChannel = "mqttOutboundChannelSub")
    public MessageHandler mqttOutboundSub() {
        MqttPahoMessageHandler messageHandler = new MqttPahoMessageHandler(clientId, mqttClientFactory());
        messageHandler.setAsync(true);
        //messageHandler.setDefaultTopic(defaultTopic);
        return messageHandler;
    }


    @MessagingGateway(defaultRequestChannel = "mqttOutboundChanneBase")
    public interface MqttGatewayBase {
        void sendToBase(String data, @Header(MqttHeaders.TOPIC) String topic);
    }

    @MessagingGateway(defaultRequestChannel = "mqttOutboundChannelSub")
    public interface MqttGatewaySub {
        void sendToSub(String data, @Header(MqttHeaders.TOPIC) String topic);
    }

}
