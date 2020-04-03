package com.kakuiwong.rxathanos.exception;

/**
 * @author gaoyang
 * @email 785175323@qq.com
 */
public class RxaThanosException extends RuntimeException {

    public RxaThanosException() {
        super();
    }

    public RxaThanosException(String message) {
        super(message);
    }

    public RxaThanosException(String message, Throwable t) {
        super(message, t);
    }
}
