package com.kakuiwong.rxathanos.util;

import com.kakuiwong.rxathanos.bean.RxaContextPO;
import org.springframework.util.StringUtils;

import java.util.function.Supplier;

/**
 * @author gaoyang
 * @email 785175323@qq.com
 */
public class RxaContext {

    private final static ThreadLocal<RxaContextPO> local = new ThreadLocal();

    public static String getRxaId() {
        return local.get().getRxaId();
    }

    public static void cleanCurrentContext() {
        local.remove();
    }

    private static void setRxa(RxaContextPO po) {
        local.set(po);
    }

    public static void bindRxa(Supplier<RxaContextPO> supplier) {
        if (StringUtils.isEmpty(local.get())) {
            setRxa(supplier.get());
        }
    }
}
