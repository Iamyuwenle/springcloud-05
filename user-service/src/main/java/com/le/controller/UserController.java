package com.le.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    //url接口/info
    @RequestMapping("info")
    public String info() {

        return "User-Service访问成功";
    }
}
