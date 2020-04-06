package com.kakuiwong.rxathanos.util;

import com.kakuiwong.rxathanos.annotation.RxaThanosTransactional;
import com.kakuiwong.rxathanos.bean.RxaContextPO;
import com.kakuiwong.rxathanos.bean.RxaTransactionManagerPO;
import com.kakuiwong.rxathanos.bean.enums.RxaContextStatusEnum;
import com.kakuiwong.rxathanos.bean.enums.RxaTaskStatusEnum;
import com.kakuiwong.rxathanos.contant.RxaContant;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.LongAdder;
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
    private final static ConcurrentHashMap<String, RxaTransactionManagerPO> subTransactionMap = new ConcurrentHashMap(INITIALCAPACITY);
    private final static LongAdder adder = new LongAdder();
    private static ScheduledExecutorService executor = Executors.newScheduledThreadPool(20, runnable -> {
        adder.increment();
        return new Thread(runnable, RxaContant.SCHEDULED_THREAD_NAME + adder.intValue());
    });

    private static void removeBaseThreadMap(String rxaId) {
        baseThreadMap.remove(rxaId);
    }

    public static void bindSubTransaction(PlatformTransactionManager txManager, TransactionStatus transaction) {
        subTransactionMap.put(RxaContext.getSubId(), RxaTransactionManagerPO.create(txManager, transaction));
    }

    public static void SubTransactionSchedule(RxaThanosTransactional annotation) {
        executor.schedule(() -> {
            RxaContext.rollBackSub(RxaContext.getSubId());
        }, annotation.timeout(), annotation.timeUnit());
    }

    public static void commitSub(String subId) {
        RxaTransactionManagerPO rxaTransactionManagerPO = subTransactionMap.get(subId);
        rxaTransactionManagerPO.getTxManager().commit(rxaTransactionManagerPO.getTransaction());
        removeSubTransactionMap(subId);
    }

    public static void rollBackSub(String subId) {
        RxaTransactionManagerPO rxaTransactionManagerPO = subTransactionMap.get(subId);
        if (rxaTransactionManagerPO == null) {
            return;
        }
        rxaTransactionManagerPO.getTxManager().rollback(rxaTransactionManagerPO.getTransaction());
        removeSubTransactionMap(subId);
    }

    private static void removeSubTransactionMap(String subId) {
        subTransactionMap.remove(subId);
    }

    public static List<String> subIds(String rxaId) {
        ConcurrentHashMap<String, RxaTaskStatusEnum> tasks = taskMap.get(rxaId);
        if (tasks == null) {
            return Collections.emptyList();
        }
        return tasks.entrySet().stream().map(k -> k.getKey()).collect(Collectors.toList());
    }


    public static void bindBaseThread(String rxaId) {
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
