package com.le.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeUnit;

@Configuration
public class GatewayConfig {

    @Autowired
    private StringRedisTemplate redisTemplate;

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
                            .filters(f -> {
                                return f.modifyResponseBody(String.class, String.class, (exchange, jsonstr) -> {
                                    JSONObject jsonObj = JSON.parseObject(jsonstr);

                                    if (jsonObj.containsKey("token")) { // 路由到服务器的url接口的响应的json是认证通过后的json串
                                        String token = jsonObj.getString("token"); // 存到redis的键，token
                                        String data = jsonObj.getString("data"); // 存到redis的值，User对象的tostring()
                                        //想redis保存token

                                        redisTemplate.opsForValue().set(token, data,15*60,TimeUnit.SECONDS);
                                    }

                                    //不管认证是否通过，都将路由到服务的url接口响应的json串返还响应给客户端
                                    return Mono.just(jsonstr);
                                });
                            })
                    .uri("lb://login-service");
                })
                .build();
    }
}
