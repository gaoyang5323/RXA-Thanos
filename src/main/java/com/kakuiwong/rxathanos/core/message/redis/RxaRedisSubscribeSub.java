package com.kakuiwong.rxathanos.core.message.redis;

import com.kakuiwong.rxathanos.bean.RxaRedisMessage;
import com.kakuiwong.rxathanos.bean.enums.RxaTaskStatusEnum;
import com.kakuiwong.rxathanos.core.message.RxaSubscribe;
import com.kakuiwong.rxathanos.util.RxaContext;

/**
 * @author gaoyang
 * @email 785175323@qq.com
 */
public class RxaRedisSubscribeSub implements RxaSubscribe {

    @Override
    public void onMessage(String message) {
        RxaRedisMessage serialize = RxaRedisMessage.serialize(message);
        if (serialize.getStatusEnum().equals(RxaTaskStatusEnum.READY)) {
            RxaContext.subReady(serialize.getSubId());
        }
        RxaContext.unParkThread(serialize.getSubId());
    }
}
