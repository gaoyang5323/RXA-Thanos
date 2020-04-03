package com.kakuiwong.rxathanos.annotation;


import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * @author gaoyang
 * @email 785175323@qq.com
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface RxaThanosTransactional {

    @AliasFor("timeout")
    long value() default 60;

    @AliasFor("value")
    long timeout() default 60;

    TimeUnit timeUnit() default TimeUnit.SECONDS;
}
