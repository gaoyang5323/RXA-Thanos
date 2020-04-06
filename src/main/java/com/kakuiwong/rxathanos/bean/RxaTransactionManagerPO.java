package com.kakuiwong.rxathanos.bean;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

/**
 * @author gaoyang
 * @email 785175323@qq.com
 */
public class RxaTransactionManagerPO {

    private PlatformTransactionManager txManager;
    private TransactionStatus transaction;

    public RxaTransactionManagerPO() {
    }

    public RxaTransactionManagerPO(PlatformTransactionManager txManager, TransactionStatus transaction) {
        this.txManager = txManager;
        this.transaction = transaction;
    }

    public PlatformTransactionManager getTxManager() {
        return txManager;
    }

    public void setTxManager(PlatformTransactionManager txManager) {
        this.txManager = txManager;
    }

    public TransactionStatus getTransaction() {
        return transaction;
    }

    public void setTransaction(TransactionStatus transaction) {
        this.transaction = transaction;
    }

    public static RxaTransactionManagerPO create(PlatformTransactionManager txManager, TransactionStatus transaction) {
        return new RxaTransactionManagerPO(txManager, transaction);
    }
}
