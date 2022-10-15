package com.le.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
  定义gateway网关的全局过滤:
  1.定义GlobalFilter的实现类并重写filter()方法:
    filter()方法就是过滤器拦截到请求执行的内容;
  2.将自定义的全局过滤器加入IOC容器
  3.再实现Ordered接口并重写getOrder()方法,用于指定当前过滤器在过滤器链中的优先级,
    getOrder()的返回值越小优先级越高。
 */
@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    //注入Redis模板
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //拿到请求路径
        ServerHttpRequest request = exchange.getRequest();//请求对象(ServerHttpRequest等价于HttpServletRequest)
        String path = request.getPath().value();//请求路径

        //表示访问的是login-service的/doLogin接口做认证请求
        if(path.contains("/doLogin")){
            //放行
            return chain.filter(exchange);
        }

        /*
          表示访问的不是login-service的/doLogin接口,就校验token:
          前端向后台一般都是通过名称叫Authorization的请求头来归还token,值的格式一般是bearer token;
         */
        List<String> authList = request.getHeaders().get("Authorization");
        if(!CollectionUtils.isEmpty(authList)){
            String auth = authList.get(0);
            if(StringUtils.hasText(auth)){
                //截取token
                String token = auth.replaceAll("bearer ", "");
                System.out.println(token);
                //判断redis中是否能存在该token的键,以此判断是否存在该token
                if(StringUtils.hasText(token)&&redisTemplate.hasKey(token)){
                    //已经认证通过,放行
                    return chain.filter(exchange);
                }
            }
        }

        /*
          表示访问的不是login-service的/doLogin接口,且没有携带token,或者携带token了但校验失败 --- 认证失败
         */
        ServerHttpResponse response = exchange.getResponse();//拿到响应对象(等效于HttpServletResponse对象)
        //设置响应的正文类型(json)及编码(UTF-8)
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
        //将响应数据封装到map
        Map<String, Object> data = new HashMap<>();
        data.put("code", 401);
        data.put("msg", "认证是失败");
        //将map转出字节形式的json串 --- 先将map转出json串,再将json串转出字节形式
        ObjectMapper objMapper = new ObjectMapper();
        byte[] bytes = new byte[0];
        try {
            bytes = objMapper.writeValueAsBytes(data);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        //拿到数据缓冲区工厂
        DataBufferFactory dataBufferFactory = response.bufferFactory();
        //数据缓冲区 --- 将字节形式的json串放入数据缓存区
        DataBuffer dataBuffer = dataBufferFactory.wrap(bytes);

        //将数据缓冲区中放入的字节形式的json串响应给客户端 --- 还是给客户端响应了个json串
        return response.writeWith(Mono.just(dataBuffer));
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
