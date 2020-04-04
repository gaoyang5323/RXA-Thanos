package com.kakuiwong.rxathanos.core.Interception;

import com.kakuiwong.rxathanos.contant.RxaContant;
import com.kakuiwong.rxathanos.util.RxaContext;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.support.HttpRequestWrapper;
import org.springframework.util.StringUtils;

import java.io.IOException;

/**
 * @author gaoyang
 * @email 785175323@qq.com
 */
public class RxaRequesstInterception implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest httpRequest, byte[] bytes, ClientHttpRequestExecution clientHttpRequestExecution) throws IOException {
        HttpRequestWrapper requestWrapper = new HttpRequestWrapper(httpRequest);
        String rxaId = RxaContext.getRxaId();
        if (!StringUtils.isEmpty(rxaId)) {
            requestWrapper.getHeaders().add(RxaContant.RXA_HEADER, rxaId);
        }
        return clientHttpRequestExecution.execute(requestWrapper, bytes);
    }
}
