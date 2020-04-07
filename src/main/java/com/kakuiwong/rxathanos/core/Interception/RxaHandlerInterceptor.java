package com.kakuiwong.rxathanos.core.Interception;

import com.kakuiwong.rxathanos.annotation.RxaThanosTransactional;
import com.kakuiwong.rxathanos.bean.RxaContextPO;
import com.kakuiwong.rxathanos.bean.enums.RxaContextStatusEnum;
import com.kakuiwong.rxathanos.contant.RxaContant;
import com.kakuiwong.rxathanos.util.RxaContext;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.invoke.MethodHandle;

/**
 * @author gaoyang
 * @email 785175323@qq.com
 */
public class RxaHandlerInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof MethodHandle) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            RxaThanosTransactional annotation = handlerMethod.getMethod().getAnnotation(RxaThanosTransactional.class);
            if (annotation == null) {
                return true;
            }
        }
        String rxaId = request.getHeader(RxaContant.RXA_ID_PREX);
        String rxaSub = request.getHeader(RxaContant.RXA_SUB);
        if (!StringUtils.isEmpty(rxaId)) {
            RxaContext.bindRxa(() -> RxaContextPO.create(rxaId, RxaContextStatusEnum.SUB));
        }
        if (!StringUtils.isEmpty(rxaSub)) {
            RxaContext.bindSub(rxaSub);
        }
        return true;
    }
}
