package com.kakuiwong.rxathanos.config;

import com.kakuiwong.rxathanos.contant.RxaContant;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
}
