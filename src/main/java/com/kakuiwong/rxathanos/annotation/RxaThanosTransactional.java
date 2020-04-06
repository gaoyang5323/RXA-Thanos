package com.kakuiwong.rxathanos.annotation;


import org.springframework.core.annotation.AliasFor;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;

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
    long value() default 30;

    @AliasFor("value")
    long timeout() default 30;

    TimeUnit timeUnit() default TimeUnit.SECONDS;

    Propagation propagation() default Propagation.REQUIRED;

    Isolation isolation() default Isolation.DEFAULT;

    Class<? extends Throwable>[] rollbackFor() default {};
}
