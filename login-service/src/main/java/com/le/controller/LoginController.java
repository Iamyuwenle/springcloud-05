package com.le.controller;

import com.le.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
public class LoginController {

    //注入redis模板
    @Autowired
    private StringRedisTemplate redisTemplate;
    /*
        前后端分离校验token的方案二：
        在login-service的/doLogin接口中:
        1）认证通过生成token 2）再将token发送给局部过滤器 3）将token保存在redis中 4)再响应给客户端
        服务单独配置的路由中配置的后置过滤器
     */

    //认证接口/doLogin
    @RequestMapping("/doLogin")
    public Map<String, Object> doLogin(String username, String password){

        Map<String, Object> data = new HashMap<>();

        if("admin".equals(username)&&"123".equals(password)){//username为admin,密码为123则认证通过
            //生成token
            String token = UUID.randomUUID().toString();

            //封装用户信息,然后以user对象的toString()为redis的值
            User user = new User(null, username, password);
            String value = user.toString();

            //保存token到redis
            redisTemplate.opsForValue().set(token, value, 15*60, TimeUnit.SECONDS);

            //单独向客户端响应数据 -- 会响应给login-service服务单独配置的路由中配置的后置过滤器
            data.put("code", 200);//状态码
            data.put("msg", "登录成功");//信息
            data.put("token", token);//token
            data.put("data", value);//认证通过的用户数据
            data.put("expire_in", 15*60);//token的过期时间
            data.put("type", "bearer");//token的类型

        }else{//反之认证失败
            data.put("code", 401);
            data.put("msg", "认证失败");
        }

        //无论认证成功与否，响应数据都会响应给后置过滤器
        return data;
    }

    /*//认证接口/doLogin
    @RequestMapping("/doLogin")
    public Map<String, Object> doLogin(String username, String password){

        Map<String, Object> data = new HashMap<>();

        if("admin".equals(username)&&"123".equals(password)){//username为admin,密码为123则认证通过
            //生成token
            String token = UUID.randomUUID().toString();

            //封装用户信息,然后以user对象的toString()为redis的值
            User user = new User(null, username, password);
            String value = user.toString();

            //保存token到redis
            redisTemplate.opsForValue().set(token, value, 15*60, TimeUnit.SECONDS);

            //向客户端响应数据
            data.put("code", 200);//状态码
            data.put("msg", "登录成功");//信息
            data.put("token", token);//token
            data.put("data", value);//认证通过的用户数据
            data.put("expire_in", 15*60);//token的过期时间
            data.put("type", "bearer");//token的类型

        }else{//反之认证失败
            data.put("code", 401);
            data.put("msg", "认证失败");
        }

        return data;
    }*/
}
