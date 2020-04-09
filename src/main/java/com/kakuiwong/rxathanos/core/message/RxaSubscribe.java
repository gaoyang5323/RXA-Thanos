package com.kakuiwong.rxathanos.core.message;

/**
 * @author gaoyang
 * @email 785175323@qq.com
 */
public interface RxaSubscribe {
    /**
     * 监听消息
     *
     * @param message
     */
    void onMessage(String message);
}
