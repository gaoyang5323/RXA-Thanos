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
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
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

    private final static String RXATHANOSTRANSACTIONAL = "@annotation(com.kakuiwong.rxathanos.annotation.RxaThanosTransactional)";

    @Autowired
    private RxaRedisPublisher rxaRedisPub;

    private PlatformTransactionManager txManager;

    public RxaAdvisor(PlatformTransactionManager txManager) {
        this.txManager = txManager;
    }


    @Pointcut(value = RXATHANOSTRANSACTIONAL)
    public void pointcut() {
    }

    //TODO log
    @Around(value = "pointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        RxaThanosTransactional annotation = annotation(joinPoint);
        Object result = null;
        boolean isRollbacked = false;
        RxaContext.bindRxa(() -> RxaContextPO.create(IdGenerateUtil.nextId(RxaContant.RXA_HEADER), RxaContextStatusEnum.BASE));
        TransactionStatus transaction = getTransactionStatus(annotation);
        try {
            result = joinPoint.proceed();
            boolean baseTransaction = RxaContext.isBase();
            boolean subTransaction = !baseTransaction;
            if (baseTransaction) {
                if (!RxaContext.isReady(RxaContext.getRxaId())) {
                    if (RxaContext.isFail(RxaContext.getRxaId())) {
                        isRollbacked = true;
                        baseRollbackAndsendSubsThrow(transaction, "other services failed");
                    }
                    RxaContext.bindBaseThread(RxaContext.getRxaId());
                    park(annotation);
                    boolean fail = RxaContext.isFail(RxaContext.getRxaId());
                    if (fail || !RxaContext.isReady(RxaContext.getRxaId())) {
                        isRollbacked = true;
                        baseRollbackAndsendSubsThrow(transaction, fail ? "other services failed" : "other services timed out");
                    }
                }
                baseCommitAndSendSubs(transaction);
            }
            if (subTransaction) {
                RxaContext.bindSubTransaction(txManager, transaction);
                RxaContext.SubTransactionSchedule(annotation);
            }
        } catch (Throwable ex) {
            rollback(annotation, ex, transaction, isRollbacked);
        } finally {
            RxaContext.cleanCurrentContext();
        }
        return result;
    }

    private void rollback(RxaThanosTransactional annotation, Throwable ex,
                          TransactionStatus transaction, boolean isRollbacked) throws Throwable {
        Class<? extends Throwable>[] classes = annotation.rollbackFor();
        if (classes.length > 0) {
            boolean isRollback = Arrays.stream(classes).anyMatch(cla -> cla.isAssignableFrom(ex.getClass()));
            if (!isRollback) {
                throw ex;
            }
        }
        if (!isRollbacked) {
            if (RxaContext.isBase()) {
                baseRollbackAndsendSubsThrow(transaction, null);
            } else {
                subRollbackAndSendBase(RxaContext.getSubId(), RxaContext.getRxaId());
            }
        }
        throw ex;
    }

    //-----------------------------------------------------start-sub----------------------------------------------------
    public void subRollbackAndSendBase(String subId, String rxaId) {
        RxaContext.rollBackSub(subId);
        rxaRedisPub.pub(RxaContextStatusEnum.BASE.rxaType(), rxaId + RxaContant.RXA_PUBSUB_SPLIT + RxaTaskStatusEnum.FAIL.status());
    }
    //-----------------------------------------------------end-sub------------------------------------------------------


    //-----------------------------------------------------start-base---------------------------------------------------
    private TransactionStatus getTransactionStatus(RxaThanosTransactional annotation) {
        DefaultTransactionDefinition definition = new DefaultTransactionDefinition(annotation.propagation().value());
        definition.setIsolationLevel(annotation.isolation().value());
        return txManager.getTransaction(definition);
    }

    private RxaThanosTransactional annotation(ProceedingJoinPoint joinPoint) {
        RxaThanosTransactional annotation = AnnotationUtils.
                findAnnotation(((MethodSignature) joinPoint.getSignature()).getMethod(),
                        RxaThanosTransactional.class);
        return annotation;
    }

    private void park(RxaThanosTransactional annotation) {
        LockSupport.parkNanos(annotation.timeUnit().toNanos(annotation.timeout()));
    }

    private void baseCommitAndSendSubs(TransactionStatus transaction) {
        commitBase(transaction);
        sendCommitToSubs(RxaContext.getRxaId());
    }

    private void baseRollbackAndsendSubsThrow(TransactionStatus transaction, String throwMessage) {
        rollbackBase(transaction);
        sendRollbackToSubs(RxaContext.getRxaId());
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
            rxaRedisPub.pub(RxaContextStatusEnum.SUB.rxaType(), id + RxaContant.RXA_PUBSUB_SPLIT + RxaTaskStatusEnum.READY.status());
        });
    }

    private void sendRollbackToSubs(String rxaId) {
        RxaContext.subIds(rxaId).stream().forEach(id -> {
            rxaRedisPub.pub(RxaContextStatusEnum.SUB.rxaType(), id + RxaContant.RXA_PUBSUB_SPLIT + RxaTaskStatusEnum.FAIL.status());
        });
    }
    //-----------------------------------------------------end-base-----------------------------------------------------
}
