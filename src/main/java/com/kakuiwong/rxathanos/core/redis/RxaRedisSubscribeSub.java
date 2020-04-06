package com.kakuiwong.rxathanos.core.redis;

import com.kakuiwong.rxathanos.bean.enums.RxaTaskStatusEnum;
import com.kakuiwong.rxathanos.util.RxaContext;

/**
 * @author gaoyang
 * @email 785175323@qq.com
 */
public class RxaRedisSubscribeSub implements RxaRedisSubscribe {

    @Override
    public void onMessage(String message) {
        String[] split = message.split(":");
        if (split.length != 2) {
            return;
        }
        String subId = split[0];
        String status = split[1];
        RxaTaskStatusEnum statusEnum = RxaTaskStatusEnum.of(status);
        if (statusEnum.equals(RxaTaskStatusEnum.FAIL)) {
            RxaContext.rollBackSub(subId);
        }
        if (statusEnum.equals(RxaTaskStatusEnum.READY)) {
            RxaContext.commitSub(subId);
        }
    }
}
