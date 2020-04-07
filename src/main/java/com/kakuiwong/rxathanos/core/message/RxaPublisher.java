package com.kakuiwong.rxathanos.core.message;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

/**
 * @author gaoyang
 * @email 785175323@qq.com
 */
public interface RxaPublisher {

    /**
     * 从事务回滚并告知主事务
     *
     * @param txManager
     * @param transaction
     */
    void subRollbackAndSendBase(PlatformTransactionManager txManager, TransactionStatus transaction);

    /**
     * 从事务就绪并告知主事务
     */
    void subReadyAndSendBase();

    /**
     * 主事务提交并告知从事务
     *
     * @param txManager
     * @param transaction
     */
    void baseCommitAndSendSubs(PlatformTransactionManager txManager, TransactionStatus transaction);

    /**
     * 主事务回滚并告知从事务
     */
    void baseRollbackAndSendSubs();
}
