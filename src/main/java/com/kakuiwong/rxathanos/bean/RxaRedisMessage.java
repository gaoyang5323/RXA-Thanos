package com.kakuiwong.rxathanos.bean;

import com.kakuiwong.rxathanos.bean.enums.RxaTaskStatusEnum;
import com.kakuiwong.rxathanos.contant.RxaContant;

/**
 * @author gaoyang
 * @email 785175323@qq.com
 */
public class RxaRedisMessage {

    private String rxaId;
    private String subId;
    private RxaTaskStatusEnum statusEnum;

    public String getRxaId() {
        return rxaId;
    }

    public String getSubId() {
        return subId;
    }

    public RxaTaskStatusEnum getStatusEnum() {
        return statusEnum;
    }

    public RxaRedisMessage(String rxaId, String subId, RxaTaskStatusEnum statusEnum) {
        this.rxaId = rxaId;
        this.subId = subId;
        this.statusEnum = statusEnum;
    }

    public static RxaRedisMessage serialize(String message) {
        String[] split = message.split(RxaContant.RXA_PUBSUB_SPLIT);
        String rxaId = null;
        String subId = null;
        String status = null;
        switch (split.length) {
            case 2:
                subId = split[0];
                status = split[1];
                break;
            case 3:
                rxaId = split[0];
                subId = split[1];
                status = split[2];
        }
        RxaTaskStatusEnum statusEnum = RxaTaskStatusEnum.of(status);
        return new RxaRedisMessage(rxaId, subId, statusEnum);
    }
}
