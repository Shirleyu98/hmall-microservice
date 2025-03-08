package com.hmall.gateway.filter;

import cn.hutool.core.text.AntPathMatcher;
import com.hmall.gateway.config.AuthProperties;
import com.hmall.gateway.util.JwtTool;
import com.hmall.common.exception.UnauthorizedException;
import com.hmall.common.utils.CollUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@EnableConfigurationProperties(AuthProperties.class)
@RequiredArgsConstructor
public class AuthGlobalFilter implements GlobalFilter, Ordered {
    private final JwtTool jwtTool;

    private final AuthProperties authProperties;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
//        1.获取request
        ServerHttpRequest request = exchange.getRequest();
//        2.判断是否需要拦截
             //无需拦截，直接放行
         if(isExclude(request.getPath().toString())){
             return chain.filter(exchange);
         }
//        3.获取请求头的token
        String token = null;
        List<String> headers = request.getHeaders().get("authorization");
        if(!CollUtils.isEmpty(headers)){
            token = headers.get(0);
        }
//        4.校验并解析token
        Long userId = null;
        try {
            userId = jwtTool.parseToken(token);
        } catch (UnauthorizedException e) {
            //如果无效，拦截
            ServerHttpResponse response = exchange.getResponse();
            response.setRawStatusCode(401);
            return response.setComplete();
        }
//        5.如果有效，传递用户信息
        System.out.println("userId = " + userId);
        String userInfo = userId.toString();
        ServerWebExchange ex = exchange.mutate()
                .request(builder -> builder.header("user-info", userInfo))
                .build();
//        6.放行

        return chain.filter(ex);
    }

    private boolean isExclude(String antPath) {
        for(String pathPattern: authProperties.getExcludePaths()){
            if(antPathMatcher.match(pathPattern, antPath)){
                return true;
            }
        }

        return false;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
