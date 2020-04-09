package com.kakuiwong.rxathanos.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kakuiwong.rxathanos.core.Interception.RxaFeignRequestInterception;
import com.kakuiwong.rxathanos.core.Interception.RxaHandlerInterceptor;
import com.kakuiwong.rxathanos.core.Interception.RxaRequestInterception;
import com.kakuiwong.rxathanos.core.aop.RxaAdvisor;
import feign.Feign;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Collections;

/**
 * @author gaoyang
 * @email 785175323@qq.com
 */
@Configuration
public class RxaConfiguration implements WebMvcConfigurer, InitializingBean {

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private PlatformTransactionManager txManager;

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("__________ ____  ___   _____          ___________.__                                    \n" +
                "\\______   \\\\   \\/  /  /  _  \\         \\__    ___/|  |__  _____     ____    ____   ______\n" +
                " |       _/ \\     /  /  /_\\  \\   ______ |    |   |  |  \\ \\__  \\   /    \\  /  _ \\ /  ___/\n" +
                " |    |   \\ /     \\ /    |    \\ /_____/ |    |   |   Y  \\ / __ \\_|   |  \\(  <_> )\\___ \\ \n" +
                " |____|_  //___/\\  \\\\____|__  /         |____|   |___|  /(____  /|___|  / \\____//____  >\n" +
                "        \\/       \\_/        \\/                        \\/      \\/      \\/             \\/     1.0-SNAPSHOT by GY");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RxaHandlerInterceptor()).addPathPatterns("/**");
    }

    @ConditionalOnClass(Feign.class)
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
    @ConditionalOnMissingBean(ObjectMapper.class)
    public ObjectMapper jacksonObjectMapper(Jackson2ObjectMapperBuilder builder) {
        ObjectMapper objectMapper = builder.createXmlMapper(false).build();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        return objectMapper;
    }

    @Bean
    public RxaAdvisor rxaAdvisor() {
        return new RxaAdvisor(txManager);
    }
}
