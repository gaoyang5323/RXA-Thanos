package com.kakuiwong.rxathanos.core.message.mqtt;

import com.kakuiwong.rxathanos.bean.RxaMessage;
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
public class RxaMqttSubscribeBase implements RxaSubscribe, MessageHandler {

    @Override
    public void onMessage(String message) {
        RxaMessage serialize = RxaMessage.serialize(message);
        RxaLogUtil.debug(() -> RxaLogUtil.logMessage(serialize));
        RxaContext.changeSub(serialize.getRxaId(), serialize.getSubId(), serialize.getStatusEnum());
        if (RxaContext.isFail(serialize.getRxaId()) || RxaContext.isReady(serialize.getRxaId())) {
            RxaContext.unParkThread(serialize.getRxaId());
        }
    }

    @Override
    public void handleMessage(Message<?> message) throws MessagingException {
        onMessage(message.getPayload().toString());
    }
}
