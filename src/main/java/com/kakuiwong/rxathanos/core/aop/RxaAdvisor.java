package com.kakuiwong.rxathanos.core.aop;

import com.kakuiwong.rxathanos.annotation.RxaThanosTransactional;
import com.kakuiwong.rxathanos.bean.RxaContextPO;
import com.kakuiwong.rxathanos.bean.RxaContextStatusEnum;
import com.kakuiwong.rxathanos.util.IdGenerateUtil;
import com.kakuiwong.rxathanos.util.RxaContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;

/**
 * @author gaoyang
 * @email 785175323@qq.com
 */
@Aspect
public class RxaAdvisor {

    @Pointcut(value = "@annotation(annotation)")
    public void Pointcut(RxaThanosTransactional annotation) {
    }

    @Before(value = "Pointcut(annotation)")
    public void before(RxaThanosTransactional annotation) {
        RxaContext.bindRxa(() -> RxaContextPO.create(IdGenerateUtil.nextId(), RxaContextStatusEnum.BASE));
    }

    //Todo
    @Around(value = "Pointcut(annotation)")
    public Object around(RxaThanosTransactional annotation, ProceedingJoinPoint joinPoint) {

        //从事务相当于加入其他事务组,去redis查看事务执行,然后等待进行同时回滚或提交

        //主事务则相当于是事务主动发起者


        return null;
    }

    @AfterReturning(value = "Pointcut(annotation)")
    public void afterReturning(RxaThanosTransactional annotation) {
        RxaContext.cleanCurrentContext();
    }
}
