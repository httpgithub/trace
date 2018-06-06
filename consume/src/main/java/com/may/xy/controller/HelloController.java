package com.may.xy.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.may.xy.api.HelloApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
    @Reference
    private HelloApi helloApi;

    @RequestMapping(value = "/hello", method = RequestMethod.GET)
    public String sayHello() {
        return helloApi.sayHello("K");
    }
}
