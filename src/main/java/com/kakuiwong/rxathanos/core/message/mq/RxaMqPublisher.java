package com.kakuiwong.rxathanos.core.message.mq;

import com.kakuiwong.rxathanos.bean.enums.RxaTaskStatusEnum;
import com.kakuiwong.rxathanos.contant.RxaContant;
import com.kakuiwong.rxathanos.core.message.RxaPublisher;
import com.kakuiwong.rxathanos.util.RxaContext;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

/**
 * @author gaoyang
 * @email 785175323@qq.com
 */
public class RxaMqPublisher implements RxaPublisher {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public void subRollbackAndSendBase(PlatformTransactionManager txManager, TransactionStatus transaction) {
        txManager.rollback(transaction);
        send(RxaContant.RXA_BASE_QUEUE, RxaContext.getRxaId() +
                RxaContant.RXA_PUBSUB_SPLIT + RxaContext.getSubId() +
                RxaContant.RXA_PUBSUB_SPLIT + RxaTaskStatusEnum.FAIL.status());
    }

    @Override
    public void subReadyAndSendBase() {
        send(RxaContant.RXA_BASE_QUEUE, RxaContext.getRxaId() +
                RxaContant.RXA_PUBSUB_SPLIT + RxaContext.getSubId() +
                RxaContant.RXA_PUBSUB_SPLIT + RxaTaskStatusEnum.READY.status());
    }

    @Override
    public void baseCommitAndSendSubs(PlatformTransactionManager txManager, TransactionStatus transaction) {
        txManager.commit(transaction);
        RxaContext.subIds(RxaContext.getRxaId()).stream().forEach(id -> {
            send(RxaContant.RXA_SUB_QUEUE, id + RxaContant.RXA_PUBSUB_SPLIT + RxaTaskStatusEnum.READY.status());
        });
    }

    @Override
    public void baseRollbackAndSendSubs() {
        RxaContext.subIds(RxaContext.getRxaId()).stream().forEach(id -> {
            send(RxaContant.RXA_SUB_QUEUE, id + RxaContant.RXA_PUBSUB_SPLIT + RxaTaskStatusEnum.FAIL.status());
        });
    }

    private void send(String routingKey, Object message) {
        rabbitTemplate.convertAndSend(RxaContant.RXA_TOPICEXCHANGE, routingKey, message);
    }
}
