package com.kakuiwong.rxathanos.core.message.redis;

import com.kakuiwong.rxathanos.bean.enums.RxaContextStatusEnum;
import com.kakuiwong.rxathanos.bean.enums.RxaTaskStatusEnum;
import com.kakuiwong.rxathanos.contant.RxaContant;
import com.kakuiwong.rxathanos.core.message.RxaPublisher;
import com.kakuiwong.rxathanos.util.RxaContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

/**
 * @author gaoyang
 * @email 785175323@qq.com
 */
public class RxaRedisPublisher implements RxaPublisher {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public void subRollbackAndSendBase(PlatformTransactionManager txManager, TransactionStatus transaction) {
        txManager.rollback(transaction);
        send(RxaContextStatusEnum.BASE.rxaType(), RxaContext.getRxaId() +
                RxaContant.RXA_PUBSUB_SPLIT + RxaContext.getSubId() +
                RxaContant.RXA_PUBSUB_SPLIT + RxaTaskStatusEnum.FAIL.status());
    }

    @Override
    public void subReadyAndSendBase() {
        send(RxaContextStatusEnum.BASE.rxaType(), RxaContext.getRxaId() +
                RxaContant.RXA_PUBSUB_SPLIT + RxaContext.getSubId() +
                RxaContant.RXA_PUBSUB_SPLIT + RxaTaskStatusEnum.READY.status());
    }

    @Override
    public void baseCommitAndSendSubs(PlatformTransactionManager txManager, TransactionStatus transaction) {
        txManager.commit(transaction);
        RxaContext.subIds(RxaContext.getRxaId()).stream().forEach(id -> {
            send(RxaContextStatusEnum.SUB.rxaType(), id + RxaContant.RXA_PUBSUB_SPLIT + RxaTaskStatusEnum.READY.status());
        });
    }

    @Override
    public void baseRollbackAndSendSubs() {
        RxaContext.subIds(RxaContext.getRxaId()).stream().forEach(id -> {
            send(RxaContextStatusEnum.SUB.rxaType(), id + RxaContant.RXA_PUBSUB_SPLIT + RxaTaskStatusEnum.FAIL.status());
        });
    }

    private void send(String channel, Object message) {
        redisTemplate.convertAndSend(channel, message);
    }
}
