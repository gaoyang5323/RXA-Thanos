package com.kakuiwong.rxathanos.bean;

/**
 * @author gaoyang
 * @email 785175323@qq.com
 */
public enum RxaContextStatusEnum {
    BASE("RXA_BASE"), SUB("RXA_SUB");

    private String type;

    RxaContextStatusEnum(String type) {
        this.type = type;
    }

    public String rxaType() {
        return type;
    }
}
