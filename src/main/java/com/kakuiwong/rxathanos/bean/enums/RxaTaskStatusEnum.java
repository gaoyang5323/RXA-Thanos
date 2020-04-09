package com.kakuiwong.rxathanos.bean.enums;

import java.util.Arrays;

/**
 * @author gaoyang
 * @email 785175323@qq.com
 */
public enum RxaTaskStatusEnum {
    BEGIN("准备"), READY("就绪"), FAIL("失败");
    private String status;

    RxaTaskStatusEnum(String status) {
        this.status = status;
    }

    public String status() {
        return status;
    }

    public static RxaTaskStatusEnum of(String status) {
        return Arrays.stream(RxaTaskStatusEnum.values()).
                filter(s -> s.status().equals(status)).
                findFirst().orElse(RxaTaskStatusEnum.FAIL);
    }
}
