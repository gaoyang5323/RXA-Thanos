package com.kakuiwong.rxathanos.util;

import com.kakuiwong.rxathanos.bean.RxaRedisMessage;
import com.kakuiwong.rxathanos.contant.RxaContant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

/**
 * @author gaoyang
 * @email 785175323@qq.com
 */
public class RxaLogUtil {

    private final static Logger LOG = LoggerFactory.getLogger(RxaContant.RXA_CONFIG_PREFIX);

    public static void debug(Supplier<String> msg) {
        if (LOG.isDebugEnabled()) {
            LOG.debug(msg.get());
        }
    }

    public static void warn(Supplier<String> msg) {
        if (LOG.isWarnEnabled()) {
            LOG.warn(msg.get());
        }
    }

    public static String logMessage(RxaRedisMessage serialize) {
        StringBuilder append = new StringBuilder().
                append("rxaId: ").
                append(serialize.getRxaId()).
                append("-").
                append("subId: ").
                append(serialize.getSubId()).
                append("-").
                append("status: ").
                append(serialize.getStatusEnum());
        return append.toString();
    }
}
