package com.alex.mallseckill.config;

import com.alex.common.utils.R;
import com.alibaba.csp.sentinel.adapter.spring.webflux.callback.BlockRequestHandler;
import com.alibaba.csp.sentinel.adapter.spring.webflux.callback.WebFluxCallbackManager;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.servlet.function.ServerResponse;
import reactor.core.publisher.Mono;

@Configuration
public class SentinelConfig {
    public SentinelConfig() {
        WebFluxCallbackManager.setBlockHandler(new BlockRequestHandler() {
            @Override
            public Mono<org.springframework.web.reactive.function.server.ServerResponse> handleRequest(ServerWebExchange serverWebExchange, Throwable throwable) {
                return Mono.just((org.springframework.web.reactive.function.server.ServerResponse) ServerResponse.ok().body(R.error(429, "Too many requests")));
            }
        });
    }
}
