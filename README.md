RXA-Thanos是一个SpringCloud 分布式事务解决方案,基于消息通信,汲取了XA两阶段提交和最大努力一阶段提交等思想,倾向于强一致性事务.目前处于完成功能阶段,等待进一步测试优化及重构;

1.集成方式:


            <dependency>
                <groupId>com.github.785175323</groupId>
                <artifactId>rxa-thanos</artifactId>
                <version>1.0-SNAPSHOT</version>
            </dependency>

2.使用:

直接引入注解到业务方法,其中注解提供了一系列默认及可选填参数
       
    配置文件application.yml:
    rxa:
      message:
        type: redis #默认   mq:使用mqtt
    
    @RxaThanosTransactional
    
        //超时时间      
        @AliasFor("timeout")
        long value() default 10;
        @AliasFor("value")
        long timeout() default 10;
        //超时时间单位
        TimeUnit timeUnit() default TimeUnit.SECONDS;
        //传播级别
        Propagation propagation() default Propagation.REQUIRED;
        //隔离级别
        Isolation isolation() default Isolation.DEFAULT;
        //事务回滚异常级别
        Class<? extends Throwable>[] rollbackFor() default {};

3.执行流程:

一. 未调用其他微服务,未涉及跨库查询

1. 执行本地业务提交或回滚;

二. 调用其他微服务

1. 主服务执行业务代码,调用其他服务;
2. 从服务执行本地业务代码并响应给主服务,挂起本地事务并等待超时;
3. 主服务执行完全部业务代码尝试提交事务,确认所有从服务就绪情况:

   1)全部就绪,则进行提交事务,并通知从服务就绪;
   
   2)从服务失败,回滚事务,并通知从服务回滚;
   
   3)从服务未就绪,等待就绪,当前线程处于等待唤醒,并等待超时;
   
4. 主服务的唤醒取决于超时或某一从服务的失败或全部就绪,然后进行相应操作;
    
    
