package com.kakuiwong.rxathanos.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

/**
 * @author gaoyang
 * @email 785175323@qq.com
 */
public class RxaLogUtil {

    private final static Logger LOG = LoggerFactory.getLogger(RxaLogUtil.class);

    public void debug(Supplier<String> msg) {
        if (LOG.isDebugEnabled()) {
            LOG.debug(msg.get());
        }
    }

    public void warn(Supplier<String> msg) {
        if (LOG.isWarnEnabled()) {
            LOG.warn(msg.get());
        }
    }
}
