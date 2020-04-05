package com.kakuiwong.rxathanos.core.redis;

import com.kakuiwong.rxathanos.bean.RxaContextStatusEnum;
import com.kakuiwong.rxathanos.bean.RxaTaskStatusEnum;
import com.kakuiwong.rxathanos.util.RxaContext;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;

/**
 * @author gaoyang
 * @email 785175323@qq.com
 */
public class RxaRedisSub implements MessageListener {

    @Override
    public void onMessage(Message message, byte[] bytes) {
        String channel = new String(message.getChannel(), StandardCharsets.UTF_8);
        if (!channel.equals(RxaContextStatusEnum.SUB.rxaType()) &&
                !channel.equals(RxaContextStatusEnum.BASE.rxaType())) {
            return;
        }
        String body = new String(message.getBody(), StandardCharsets.UTF_8);
        if (StringUtils.isEmpty(body)) {
            return;
        }
        String[] split = body.split(":");
        if (channel.equals(RxaContextStatusEnum.BASE.rxaType())) {
            if (split.length != 3) {
                return;
            }
            String rxaId = split[0];
            String subId = split[1];
            String status = split[2];
            //主事务:
            //判断从事务发来的信息,是否ready全,如果是则唤醒主事务的线程
            RxaContext.changeSub(rxaId, subId, RxaTaskStatusEnum.of(status));
            if (RxaContext.isFail(rxaId) || RxaContext.isReady(rxaId)) {
                RxaContext.unParkThread(rxaId);
            }
        }
        if (channel.equals(RxaContextStatusEnum.SUB.rxaType())) {
            if (split.length != 2) {
                return;
            }
            //从事务
            //判断主事务是回滚还是进行提交
            String subId = split[0];
            String status = split[1];
            RxaTaskStatusEnum statusEnum = RxaTaskStatusEnum.of(status);
            if (statusEnum.equals(RxaTaskStatusEnum.FAIL)) {
                RxaContext.rollBackSub(subId);
            }
            if (statusEnum.equals(RxaTaskStatusEnum.READY)) {
                RxaContext.commitSub(subId);
            }
        }
    }
}
