package com.kakuiwong.rxathanos.core.config;

import com.kakuiwong.rxathanos.core.Interception.RxaHandlerInterceptor;
import com.kakuiwong.rxathanos.core.Interception.RxaRequesstInterception;
import com.kakuiwong.rxathanos.core.aop.RxaAdvisor;
import com.kakuiwong.rxathanos.core.redis.RxaRedisPub;
import com.kakuiwong.rxathanos.core.redis.RxaRedisSub;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Collections;

/**
 * @author gaoyang
 * @email 785175323@qq.com
 */
@Configuration
public class RxaConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RxaHandlerInterceptor()).addPathPatterns("/**");
    }

    @Bean
    public RestTemplate rxaRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setInterceptors(Collections.singletonList(new RxaRequesstInterception()));
        return restTemplate;
    }

    @Bean
    public RxaAdvisor rxaAdvisor() {
        return new RxaAdvisor();
    }

    @Bean
    public RxaRedisPub rxaRedisPub() {
        return new RxaRedisPub();
    }

    @Bean
    public RxaRedisSub rxaRedisSub() {
        return new RxaRedisSub();
    }
}
