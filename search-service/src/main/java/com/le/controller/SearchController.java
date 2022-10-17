package com.le.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SearchController {

    //url接口/doSearch
    @RequestMapping("/doSearch")
    public String doSearch() {

        return "查询到---传家宝小米6";
    }
}
