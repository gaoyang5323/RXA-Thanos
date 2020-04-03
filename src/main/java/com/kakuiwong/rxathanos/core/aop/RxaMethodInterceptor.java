package com.kakuiwong.rxathanos.core.aop;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * @author gaoyang
 * @email 785175323@qq.com
 */
public class RxaMethodInterceptor implements MethodInterceptor {

    // Todo
    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {

        //判断当前是否有xid

        //有的话相当于加入其他事务组,去redis查看事务执行,然后等待进行同时回滚或提交

        //没有的话则相当于是事务主动发起者,则生成xid到当先线程变量中,通过feign传递出去


        return null;
    }
}
