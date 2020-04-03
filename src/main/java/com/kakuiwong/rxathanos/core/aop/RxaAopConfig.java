package com.kakuiwong.rxathanos.core.aop;

import com.kakuiwong.rxathanos.annotation.RxaThanosTransactional;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author gaoyang
 * @email 785175323@qq.com
 */
@Configuration
public class RxaAopConfig {

    @Bean
    public DefaultPointcutAdvisor rxaAdvisor() {
        AnnotationMatchingPointcut pointcut = new AnnotationMatchingPointcut(null, RxaThanosTransactional.class);
        DefaultPointcutAdvisor advisor = new DefaultPointcutAdvisor();
        advisor.setPointcut(pointcut);
        advisor.setAdvice(new RxaMethodInterceptor());
        return advisor;
    }
}
