package com.hmall.config;

import com.hmall.client.fallback.ItemClientFallback;
import com.hmall.client.fallback.PayClientFallback;
import com.hmall.common.utils.UserContext;
import feign.Logger;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;

public class DefaultFeignConfig {
    @Bean
    public Logger.Level feignLogLevel(){
        return Logger.Level.FULL;
    }

    @Bean
    public RequestInterceptor userInfoRequestInterceptor(){
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate requestTemplate) {
                //获取登录用户
                Long userId = UserContext.getUser();
                //如果不为空则放入请求头中，传递给下游微服务
                if(userId == null){
                    return;
                }
                requestTemplate.header("user-info", userId.toString());
            }
        };
    }

    @Bean
    public ItemClientFallback itemClientFallback(){
        return new ItemClientFallback();
    }

    @Bean
    public PayClientFallback payClientFallback(){
        return new PayClientFallback();
    }
}