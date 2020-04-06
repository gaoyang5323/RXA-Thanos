package com.kakuiwong.rxathanos.core.Interception;

import com.kakuiwong.rxathanos.bean.enums.RxaTaskStatusEnum;
import com.kakuiwong.rxathanos.contant.RxaContant;
import com.kakuiwong.rxathanos.util.IdGenerateUtil;
import com.kakuiwong.rxathanos.util.RxaContext;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.util.StringUtils;

/**
 * @author gaoyang
 * @email 785175323@qq.com
 */
public class RxaFeignRequestInterception implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate requestTemplate) {
        String rxaId = RxaContext.getRxaId();
        if (!StringUtils.isEmpty(rxaId)) {
            String subId = IdGenerateUtil.nextId(RxaContant.RXA_SUB);
            requestTemplate.header(RxaContant.RXA_HEADER, rxaId);
            requestTemplate.header(RxaContant.RXA_SUB, subId);
            RxaContext.changeSub(rxaId, subId, RxaTaskStatusEnum.BEGIN);
        }
    }
}
