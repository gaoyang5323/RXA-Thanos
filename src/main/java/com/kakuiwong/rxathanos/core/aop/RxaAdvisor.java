package com.kakuiwong.rxathanos.core.aop;

import com.kakuiwong.rxathanos.annotation.RxaThanosTransactional;
import com.kakuiwong.rxathanos.bean.RxaContextPO;
import com.kakuiwong.rxathanos.bean.RxaContextStatusEnum;
import com.kakuiwong.rxathanos.bean.RxaTaskStatusEnum;
import com.kakuiwong.rxathanos.contant.RxaContant;
import com.kakuiwong.rxathanos.core.redis.RxaRedisPub;
import com.kakuiwong.rxathanos.exception.RxaThanosException;
import com.kakuiwong.rxathanos.util.IdGenerateUtil;
import com.kakuiwong.rxathanos.util.RxaContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.concurrent.locks.LockSupport;

/**
 * @author gaoyang
 * @email 785175323@qq.com
 */
@Aspect
public class RxaAdvisor {

    @Autowired
    private RxaRedisPub rxaRedisPub;

    @Pointcut(value = "@annotation(com.kakuiwong.rxathanos.annotation.RxaThanosTransactional)")
    public void Pointcut() {
    }

    @Before(value = "Pointcut()")
    public void before() {
        RxaContext.bindRxa(() -> RxaContextPO.create(IdGenerateUtil.nextId(RxaContant.RXA_HEADER), RxaContextStatusEnum.BASE));
    }

    //Todo
    @Around(value = "@annotation(annotation)")
    public Object around(RxaThanosTransactional annotation, ProceedingJoinPoint joinPoint) throws Throwable {
        Object proceed = null;
        String rxaId = RxaContext.getRxaId();
        String subId = RxaContext.getSubId();
        try {
            proceed = joinPoint.proceed();

            boolean base = RxaContext.isBase();
            if (base) {
                //主事务
                //先判断一次ready,如果false,挂起线程
                //异步执行,如果所有从事务准备就绪,发送完成到其他 redis 到 从事务,并且当前释放业务线程
                boolean ready = RxaContext.isReady(rxaId);
                if (!ready) {
                    boolean fail = RxaContext.isFail(rxaId);
                    if (fail) {
                        throw new RxaThanosException("从事务失败");
                    }
                    RxaContext.storeThreadAndSchedule(RxaContext.getRxaId(), annotation.timeout(), annotation.timeUnit());
                    LockSupport.park();
                    boolean failTwo = RxaContext.isFail(rxaId);
                    if (failTwo) {
                        throw new RxaThanosException("从事务失败回滚");
                    }
                    boolean readyTwo = RxaContext.isReady(rxaId);
                    if (!readyTwo) {
                        throw new RxaThanosException("超时回滚");
                    }
                }
            }
            if (!base) {
                //从事务
                //直接返回结果,异步挂起当前事务,等待异步提交
                RxaContext.bindSubTransaction(subId, "当前事务");
                RxaContext.executor.schedule(() -> {
                    //超时回滚
                }, annotation.timeout(), annotation.timeUnit());
            }
        } catch (Throwable ex) {
            if (RxaContext.isBase()) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                //发送到其他从事务进行回滚
                RxaContext.subIds(rxaId).stream().forEach(id -> {
                    rxaRedisPub.pub(RxaContextStatusEnum.SUB.rxaType(), id + ":" + RxaTaskStatusEnum.FAIL.status());
                });
            } else {
                RxaContext.rollBackSub(subId);
                //发送失败到主事务回滚
                rxaRedisPub.pub(RxaContextStatusEnum.BASE.rxaType(), rxaId + ":" + RxaTaskStatusEnum.FAIL.status());
            }
            throw ex;
        } finally {
            if (RxaContext.isBase()) {
                //提交当前主事务
            }
            RxaContext.cleanCurrentContext();
        }
        return proceed;
    }
}
