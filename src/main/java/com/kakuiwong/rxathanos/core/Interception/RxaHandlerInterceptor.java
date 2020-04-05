package com.kakuiwong.rxathanos.core.Interception;

import com.kakuiwong.rxathanos.bean.RxaContextPO;
import com.kakuiwong.rxathanos.bean.RxaContextStatusEnum;
import com.kakuiwong.rxathanos.contant.RxaContant;
import com.kakuiwong.rxathanos.util.RxaContext;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author gaoyang
 * @email 785175323@qq.com
 */
public class RxaHandlerInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String rxaId = request.getHeader(RxaContant.RXA_HEADER);
        if (!StringUtils.isEmpty(rxaId)) {
            RxaContext.bindRxa(() -> RxaContextPO.create(rxaId, RxaContextStatusEnum.SUB));
        }
        return true;
    }
}
