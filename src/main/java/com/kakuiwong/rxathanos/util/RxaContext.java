package com.kakuiwong.rxathanos.util;

import com.sun.istack.internal.NotNull;

/**
 * @author gaoyang
 * @email 785175323@qq.com
 */
public class RxaContext {

    private final static ThreadLocal<String> local = new ThreadLocal();

    public static String getRxaId() {
        return local.get();
    }

    public static void bindRxaId(@NotNull String id){
        local.set(id);
    }

    public static void cleanCurrentContext(){
        local.remove();
    }
}
