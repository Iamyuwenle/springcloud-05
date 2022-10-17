package com.le.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.reactive.ServerHttpRequest;
import reactor.core.publisher.Mono;

@Configuration
public class GatewayConfig {

    //编码方式的路由

    @Bean
    public KeyResolver urlKeyResolver() {
        /*
        方法中需要返回函数式接口KeyResolver的bean对象 -- Lambda表达式
        函数式接口KeyResolver只有一个接口抽象对象Mono<String> resolve(ServerWebExchange exchange) -- Lambda表达式用于实现该抽象方法
         */

        return exchange -> {
            //拿到了客户端请求的url地址
            ServerHttpRequest request = exchange.getRequest();
            String url = request.getPath().value();
            //Mono对象中放入的url,就表示url限流
            return Mono.just(url);
        };
    }

//    @Bean
//    public KeyResolver ipKeyResolver() {
//        /*
//        方法中需要返回函数式接口KeyResolver的bean对象 -- Lambda表达式
//        函数式接口KeyResolver只有一个接口抽象对象Mono<String> resolve(ServerWebExchange exchange) -- Lambda表达式用于实现该抽象方法
//         */
//
//        return exchange -> {
//            //拿到了客户端请求的ip地址
//            ServerHttpRequest request = exchange.getRequest();
//            String ip = request.getRemoteAddress().getAddress().getHostName();
//            return Mono.just(ip);
//        };
//    }
}
