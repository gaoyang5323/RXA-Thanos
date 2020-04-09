package com.kakuiwong.rxathanos.core.aop;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kakuiwong.rxathanos.annotation.RxaThanosTransactional;
import com.kakuiwong.rxathanos.bean.RxaContextPO;
import com.kakuiwong.rxathanos.bean.enums.RxaContextStatusEnum;
import com.kakuiwong.rxathanos.contant.RxaContant;
import com.kakuiwong.rxathanos.core.message.RxaPublisher;
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
import org.springframework.http.MediaType;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;

/**
 * @author gaoyang
 * @email 785175323@qq.com
 */
@Aspect
public class RxaAdvisor {

    @Autowired
    private RxaPublisher rxaPublisher;
    @Autowired
    private ObjectMapper objectMapper;

    private PlatformTransactionManager txManager;

    public RxaAdvisor(PlatformTransactionManager txManager) {
        this.txManager = txManager;
    }

    @Pointcut(value = RxaContant.RXATHANOSTRANSACTIONAL)
    public void pointcut() {
    }

    //TODO log
    @Around(value = "pointcut()")
    public void around(ProceedingJoinPoint joinPoint) throws Throwable {
        RxaContext.bindRxa(() -> RxaContextPO.create(IdGenerateUtil.nextId(RxaContant.RXA_ID_PREFIX),
                RxaContextStatusEnum.BASE));
        RxaThanosTransactional annotation = annotation(joinPoint);
        Object result = null;
        AtomicBoolean isRollbacked = new AtomicBoolean(false);
        TransactionStatus currentTransaction = getTransactionStatus(annotation);
        try {
            result = joinPoint.proceed();
            boolean baseTransaction = RxaContext.isBase();
            boolean subTransaction = !baseTransaction;
            if (baseTransaction) {
                handleBaseTransaction(currentTransaction, annotation, result, isRollbacked);
            }
            if (subTransaction) {
                handleSubTransaction(currentTransaction, annotation, result);
            }
        } catch (Throwable ex) {
            rollbackByAnno(annotation, ex, currentTransaction, isRollbacked);
        } finally {
            RxaContext.cleanCurrentContext();
        }
    }

    private void handleSubTransaction(TransactionStatus transaction,
                                      RxaThanosTransactional annotation, Object result) throws IOException {
        RxaContext.bindThread(RxaContext.getSubId());
        RxaContext.subBegin(RxaContext.getSubId());
        flush(result);
        rxaPublisher.subReadyAndSendBase();
        park(annotation);
        if (RxaContext.subIsReady(RxaContext.getSubId())) {
            subCommit(transaction);
        } else {
            rxaPublisher.subRollbackAndSendBase(txManager, transaction);
        }
    }

    private void handleBaseTransaction(TransactionStatus transaction,
                                       RxaThanosTransactional annotation, Object result,
                                       AtomicBoolean isRollbacked) throws IOException {
        if (!RxaContext.isReady(RxaContext.getRxaId())) {
            if (RxaContext.isFail(RxaContext.getRxaId())) {
                isRollbacked.set(true);
                baseRollbackAndsendSubsThrow(transaction, "other services failed");
            }
            RxaContext.bindThread(RxaContext.getRxaId());
            park(annotation);
            boolean fail = RxaContext.isFail(RxaContext.getRxaId());
            if (fail || !RxaContext.isReady(RxaContext.getRxaId())) {
                isRollbacked.set(true);
                baseRollbackAndsendSubsThrow(transaction, fail ? "other services failed" : "other services timed out");
            }
        }
        rxaPublisher.baseCommitAndSendSubs(txManager, transaction);
        flush(result);
    }

    private void rollbackByAnno(RxaThanosTransactional annotation, Throwable ex,
                                TransactionStatus transaction, AtomicBoolean isRollbacked) throws Throwable {
        Class<? extends Throwable>[] classes = annotation.rollbackFor();
        if (classes.length > 0) {
            boolean isRollback = Arrays.stream(classes).anyMatch(cla -> cla.isAssignableFrom(ex.getClass()));
            if (!isRollback) {
                throw ex;
            }
        }
        if (!isRollbacked.get()) {
            if (RxaContext.isBase()) {
                baseRollbackAndsendSubsThrow(transaction, null);
            } else {
                rxaPublisher.subRollbackAndSendBase(txManager, transaction);
            }
        }
        throw ex;
    }

    private void flush(Object result) throws IOException {
        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        PrintWriter writer = response.getWriter();
        writer.print(objectMapper.writeValueAsString(result));
        writer.flush();
        writer.close();
    }

    private void subCommit(TransactionStatus transaction) {
        txManager.commit(transaction);
        RxaContext.subStatusClean(RxaContext.getSubId());
    }

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

    private void baseRollbackAndsendSubsThrow(TransactionStatus transaction, String throwMessage) {
        txManager.rollback(transaction);
        rxaPublisher.baseRollbackAndSendSubs();
        if (throwMessage != null) {
            throw new RxaThanosException(throwMessage);
        }
    }
}
