package com.kakuiwong.rxathanos.bean;

/**
 * @author gaoyang
 * @email 785175323@qq.com
 */
public class RxaContextPO {

    private String rxaId;

    private RxaContextStatusEnum rxaContextStatusEnum;

    public RxaContextPO(String rxaId, RxaContextStatusEnum rxaContextStatusEnum) {
        this.rxaId = rxaId;
        this.rxaContextStatusEnum = rxaContextStatusEnum;
    }

    public String getRxaId() {
        return rxaId;
    }

    public void setRxaId(String rxaId) {
        this.rxaId = rxaId;
    }

    public RxaContextStatusEnum getRxaContextStatusEnum() {
        return rxaContextStatusEnum;
    }

    public void setRxaContextStatusEnum(RxaContextStatusEnum rxaContextStatusEnum) {
        this.rxaContextStatusEnum = rxaContextStatusEnum;
    }

    public static RxaContextPO create(String rxaId, RxaContextStatusEnum rxaContextStatusEnum) {
        return new RxaContextPO(rxaId, rxaContextStatusEnum);
    }
}
