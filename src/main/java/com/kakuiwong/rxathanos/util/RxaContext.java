package com.kakuiwong.rxathanos.util;

import com.kakuiwong.rxathanos.bean.RxaContextPO;
import com.kakuiwong.rxathanos.bean.enums.RxaContextStatusEnum;
import com.kakuiwong.rxathanos.bean.enums.RxaTaskStatusEnum;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author gaoyang
 * @email 785175323@qq.com
 */
public class RxaContext {

    private final static int INITIALCAPACITY = 4;
    private final static ThreadLocal<RxaContextPO> local = new ThreadLocal();
    private final static ThreadLocal<String> localSub = new ThreadLocal();
    private final static ConcurrentHashMap<String, ConcurrentHashMap<String, RxaTaskStatusEnum>> taskMap = new ConcurrentHashMap(INITIALCAPACITY);
    private final static ConcurrentHashMap<String, Thread> baseThreadMap = new ConcurrentHashMap(INITIALCAPACITY);
    private final static ConcurrentHashMap<String, Boolean> subStatusMap = new ConcurrentHashMap(INITIALCAPACITY);


    public static void subReady(String subId) {
        subStatusMap.put(subId, true);
    }

    public static void subBegin(String subId) {
        subStatusMap.put(subId, false);
    }

    public static void subStatusClean(String subId) {
        subStatusMap.remove(subId);
    }

    public static boolean subIsReady(String subId) {
        Boolean aBoolean = subStatusMap.get(subId);
        return aBoolean == null ? false : aBoolean;
    }

    private static void removeBaseThreadMap(String rxaId) {
        baseThreadMap.remove(rxaId);
    }

    public static List<String> subIds(String rxaId) {
        ConcurrentHashMap<String, RxaTaskStatusEnum> tasks = taskMap.get(rxaId);
        if (tasks == null) {
            return Collections.emptyList();
        }
        return tasks.entrySet().stream().map(k -> k.getKey()).collect(Collectors.toList());
    }


    public static void bindThread(String rxaId) {
        baseThreadMap.put(rxaId, Thread.currentThread());
    }

    public static void unParkThread(String rxaId) {
        Thread thread = baseThreadMap.get(rxaId);
        if (thread != null) {
            LockSupport.unpark(thread);
        }
    }

    private static void beginMap(String rxaId) {
        taskMap.put(rxaId, new ConcurrentHashMap<>(INITIALCAPACITY));
    }

    public static void changeSub(String rxaId, String subId, RxaTaskStatusEnum statusEnum) {
        ConcurrentHashMap<String, RxaTaskStatusEnum> map = taskMap.get(rxaId);
        if (map != null) {
            map.put(subId, statusEnum);
        }
    }

    public static boolean isReady(String rxaId) {
        ConcurrentHashMap<String, RxaTaskStatusEnum> map = taskMap.get(rxaId);
        if (map == null) {
            return false;
        }
        if (map.size() == 0) {
            return true;
        }
        return map.values().stream().allMatch(rxa -> rxa.equals(RxaTaskStatusEnum.READY));
    }

    public static boolean isFail(String rxaId) {
        ConcurrentHashMap<String, RxaTaskStatusEnum> map = taskMap.get(rxaId);
        if (map == null) {
            return false;
        }
        if (map.size() == 0) {
            return true;
        }
        return map.values().stream().anyMatch(rxa -> rxa.equals(RxaTaskStatusEnum.FAIL));
    }

    private static void removeBaseTaskMap(String rxaId) {
        taskMap.remove(rxaId);
    }

    public static String getRxaId() {
        return local.get().getRxaId();
    }

    public static void cleanCurrentContext() {
        RxaContextPO rxaContextPO = local.get();
        if (rxaContextPO.getRxaContextStatusEnum().equals(RxaContextStatusEnum.BASE)) {
            removeBaseTaskMap(rxaContextPO.getRxaId());
        }
        removeBaseThreadMap(rxaContextPO.getRxaId());
        localSub.remove();
        local.remove();
    }

    private static void setRxa(RxaContextPO po) {
        local.set(po);
    }

    public static boolean isBase() {
        return local.get().getRxaContextStatusEnum().equals(RxaContextStatusEnum.BASE);
    }

    public static void bindRxa(Supplier<RxaContextPO> supplier) {
        if (StringUtils.isEmpty(local.get())) {
            RxaContextPO rxaContextPO = supplier.get();
            setRxa(rxaContextPO);
            if (rxaContextPO.getRxaContextStatusEnum().equals(RxaContextStatusEnum.BASE)) {
                beginMap(rxaContextPO.getRxaId());
            }
        }
    }

    public static void bindSub(String rxaSub) {
        localSub.set(rxaSub);
    }

    public static String getSubId() {
        return localSub.get();
    }
}
