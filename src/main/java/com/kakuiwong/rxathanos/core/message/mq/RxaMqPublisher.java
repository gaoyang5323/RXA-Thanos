package com.kakuiwong.rxathanos.core.message.mq;

import com.kakuiwong.rxathanos.bean.enums.RxaTaskStatusEnum;
import com.kakuiwong.rxathanos.config.RxaMqConfiguration;
import com.kakuiwong.rxathanos.contant.RxaContant;
import com.kakuiwong.rxathanos.core.message.RxaPublisher;
import com.kakuiwong.rxathanos.util.RxaContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

/**
 * @author gaoyang
 * @email 785175323@qq.com
 */
public class RxaMqPublisher implements RxaPublisher {

    @Autowired
    RxaMqConfiguration.MqttGatewayBase mqttGatewayBase;
    @Autowired
    RxaMqConfiguration.MqttGatewaySub mqttGatewaySub;

    @Override
    public void subRollbackAndSendBase(PlatformTransactionManager txManager, TransactionStatus transaction) {
        txManager.rollback(transaction);
        sendToBase(RxaContant.RXA_BASE_TOPIC, RxaContext.getRxaId() +
                RxaContant.RXA_PUBSUB_SPLIT + RxaContext.getSubId() +
                RxaContant.RXA_PUBSUB_SPLIT + RxaTaskStatusEnum.FAIL.status());
    }

    @Override
    public void subReadyAndSendBase() {
        sendToBase(RxaContant.RXA_BASE_TOPIC, RxaContext.getRxaId() +
                RxaContant.RXA_PUBSUB_SPLIT + RxaContext.getSubId() +
                RxaContant.RXA_PUBSUB_SPLIT + RxaTaskStatusEnum.READY.status());
    }

    @Override
    public void baseCommitAndSendSubs(PlatformTransactionManager txManager, TransactionStatus transaction) {
        txManager.commit(transaction);
        RxaContext.subIds(RxaContext.getRxaId()).stream().forEach(id -> {
            sendToSub(RxaContant.RXA_SUB_TOPIC, id + RxaContant.RXA_PUBSUB_SPLIT + RxaTaskStatusEnum.READY.status());
        });
    }

    @Override
    public void baseRollbackAndSendSubs() {
        RxaContext.subIds(RxaContext.getRxaId()).stream().forEach(id -> {
            sendToSub(RxaContant.RXA_SUB_TOPIC, id + RxaContant.RXA_PUBSUB_SPLIT + RxaTaskStatusEnum.FAIL.status());
        });
    }

    private void sendToBase(String topic, String message) {
        mqttGatewayBase.sendToBase(message, topic);
    }

    private void sendToSub(String topic, String message) {
        mqttGatewaySub.sendToSub(message, topic);
    }
}
