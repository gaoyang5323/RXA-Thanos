package com.kakuiwong.rxathanos.core.redis;

/**
 * @author gaoyang
 * @email 785175323@qq.com
 */
public interface RxaRedisSubscribe {

    String ONMESSAGE = "onMessage";

    public void onMessage(String message);
}
