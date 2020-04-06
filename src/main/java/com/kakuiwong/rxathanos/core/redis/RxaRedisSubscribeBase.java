package com.kakuiwong.rxathanos.core.redis;

import com.kakuiwong.rxathanos.bean.enums.RxaTaskStatusEnum;
import com.kakuiwong.rxathanos.util.RxaContext;

/**
 * @author gaoyang
 * @email 785175323@qq.com
 */
public class RxaRedisSubscribeBase implements RxaRedisSubscribe {

    @Override
    public void onMessage(String message) {
        String[] split = message.split(":");
        if (split.length != 3) {
            return;
        }
        String rxaId = split[0];
        String subId = split[1];
        String status = split[2];
        RxaContext.changeSub(rxaId, subId, RxaTaskStatusEnum.of(status));
        if (RxaContext.isFail(rxaId) || RxaContext.isReady(rxaId)) {
            RxaContext.unParkThread(rxaId);
        }
    }
}
