package com.kakuiwong.rxathanos.core.message.mqtt;

import com.kakuiwong.rxathanos.bean.RxaMessage;
import com.kakuiwong.rxathanos.bean.enums.RxaTaskStatusEnum;
import com.kakuiwong.rxathanos.core.message.RxaSubscribe;
import com.kakuiwong.rxathanos.util.RxaContext;
import com.kakuiwong.rxathanos.util.RxaLogUtil;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;

/**
 * @author gaoyang
 * @email 785175323@qq.com
 */
public class RxaMqttSubscribeSub implements RxaSubscribe, MessageHandler {
    @Override
    public void onMessage(String message) {
        RxaMessage serialize = RxaMessage.serialize(message);
        RxaLogUtil.debug(() -> RxaLogUtil.logMessage(serialize));
        if (serialize.getStatusEnum().equals(RxaTaskStatusEnum.READY)) {
            RxaContext.subReady(serialize.getSubId());
        }
        RxaContext.unParkThread(serialize.getSubId());
    }


    @Override
    public void handleMessage(Message<?> message) throws MessagingException {
        onMessage(message.getPayload().toString());
    }
}
