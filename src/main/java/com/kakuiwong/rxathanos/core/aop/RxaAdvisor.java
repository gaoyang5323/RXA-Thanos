package com.kakuiwong.rxathanos.core.aop;

import com.kakuiwong.rxathanos.annotation.RxaThanosTransactional;
import com.kakuiwong.rxathanos.bean.RxaContextPO;
import com.kakuiwong.rxathanos.bean.enums.RxaContextStatusEnum;
import com.kakuiwong.rxathanos.bean.enums.RxaTaskStatusEnum;
import com.kakuiwong.rxathanos.contant.RxaContant;
import com.kakuiwong.rxathanos.core.redis.RxaRedisPublisher;
import com.kakuiwong.rxathanos.exception.RxaThanosException;
import com.kakuiwong.rxathanos.util.IdGenerateUtil;
import com.kakuiwong.rxathanos.util.RxaContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.Arrays;
import java.util.concurrent.locks.LockSupport;

/**
 * @author gaoyang
 * @email 785175323@qq.com
 */
@Aspect
public class RxaAdvisor {

    @Autowired
    private RxaRedisPublisher rxaRedisPub;

    private PlatformTransactionManager txManager;

    public RxaAdvisor(PlatformTransactionManager txManager) {
        this.txManager = txManager;
    }


    @Pointcut(value = "@annotation(annotation)")
    public void pointcut(RxaThanosTransactional annotation) {
    }

    //TODO log
    @Around(value = "pointcut(annotation)")
    public Object around(ProceedingJoinPoint joinPoint, RxaThanosTransactional annotation) throws Throwable {
        Object proceed = null;
        boolean isRollbacked = false;
        RxaContext.bindRxa(() -> RxaContextPO.create(IdGenerateUtil.nextId(RxaContant.RXA_HEADER), RxaContextStatusEnum.BASE));
        String rxaId = RxaContext.getRxaId();
        String subId = RxaContext.getSubId();
        TransactionStatus transaction = getTransactionStatus(annotation);
        RxaContext.changeSub(rxaId, "", RxaTaskStatusEnum.BEGIN);
        try {
            proceed = joinPoint.proceed();
            boolean baseTransaction = RxaContext.isBase();
            boolean subTransaction = !baseTransaction;
            if (baseTransaction) {
                if (!RxaContext.isReady(rxaId)) {
                    if (RxaContext.isFail(rxaId)) {
                        isRollbacked = true;
                        baseRollbackAndsendSubsThrow(transaction, rxaId, "other services failed");
                    }
                    RxaContext.bindBaseThread(RxaContext.getRxaId());
                    park(annotation);
                    boolean fail = RxaContext.isFail(rxaId);
                    if (fail || !RxaContext.isReady(rxaId)) {
                        isRollbacked = true;
                        baseRollbackAndsendSubsThrow(transaction, rxaId, fail ? "other services failed" : "other services timed out");
                    }
                }
                baseCommitAndSendSubs(transaction, rxaId);
            }
            if (subTransaction) {
                RxaContext.bindSubTransaction(subId, txManager, transaction);
                RxaContext.SubTransactionSchedule(subId, annotation);
            }
        } catch (Throwable ex) {
            rollback(annotation, ex, transaction, rxaId, subId, isRollbacked);
        } finally {
            RxaContext.cleanCurrentContext();
        }
        return proceed;
    }

    private void rollback(RxaThanosTransactional annotation, Throwable ex,
                          TransactionStatus transaction, String rxaId, String subId, boolean isRollbacked) throws Throwable {
        Class<? extends Throwable>[] classes = annotation.rollbackFor();
        if (classes.length > 0) {
            boolean isRollback = Arrays.stream(classes).anyMatch(cla -> cla.isAssignableFrom(ex.getClass()));
            if (!isRollback) {
                throw ex;
            }
        }
        if (!isRollbacked) {
            if (RxaContext.isBase()) {
                baseRollbackAndsendSubsThrow(transaction, rxaId, null);
            } else {
                subRollbackAndSendBase(subId, rxaId);
            }
        }
        throw ex;
    }

    //-----------------------------------------------------start-sub----------------------------------------------------
    public void subRollbackAndSendBase(String subId, String rxaId) {
        RxaContext.rollBackSub(subId);
        rxaRedisPub.pub(RxaContextStatusEnum.BASE.rxaType(), rxaId + ":" + RxaTaskStatusEnum.FAIL.status());
    }
    //-----------------------------------------------------end-sub------------------------------------------------------


    //-----------------------------------------------------start-base---------------------------------------------------
    private void park(RxaThanosTransactional annotation) {
        LockSupport.parkNanos(annotation.timeUnit().toNanos(annotation.timeout()));
    }

    private void baseCommitAndSendSubs(TransactionStatus transaction, String rxaId) {
        commitBase(transaction);
        sendCommitToSubs(rxaId);
    }

    private void baseRollbackAndsendSubsThrow(TransactionStatus transaction, String rxaId, String throwMessage) {
        rollbackBase(transaction);
        sendRollbackToSubs(rxaId);
        if (throwMessage != null) {
            throw new RxaThanosException(throwMessage);
        }
    }

    private void commitBase(TransactionStatus transaction) {
        txManager.commit(transaction);
    }

    private void rollbackBase(TransactionStatus transaction) {
        txManager.rollback(transaction);
    }

    private void sendCommitToSubs(String rxaId) {
        RxaContext.subIds(rxaId).stream().forEach(id -> {
            rxaRedisPub.pub(RxaContextStatusEnum.SUB.rxaType(), id + ":" + RxaTaskStatusEnum.READY.status());
        });
    }

    private void sendRollbackToSubs(String rxaId) {
        RxaContext.subIds(rxaId).stream().forEach(id -> {
            rxaRedisPub.pub(RxaContextStatusEnum.SUB.rxaType(), id + ":" + RxaTaskStatusEnum.FAIL.status());
        });
    }
    //-----------------------------------------------------end-base-----------------------------------------------------

    private TransactionStatus getTransactionStatus(RxaThanosTransactional annotation) {
        DefaultTransactionDefinition definition = new DefaultTransactionDefinition(annotation.propagation().value());
        definition.setIsolationLevel(annotation.isolation().value());
        return txManager.getTransaction(definition);
    }
}
