package com.le.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    /*
        使用编码方式给login-service的/doLogin接口配置一个单独的路由：
     */
    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder routeLocatorBuilder) {

        //拿到路由构建器
        RouteLocatorBuilder.Builder builder = routeLocatorBuilder.routes();

        return builder
                .route("login-service-router", t -> {
                    return t.path("/doLogin")//url接口的断言
                    .uri("lb://login-service");
                })
                .build();
    }
}
