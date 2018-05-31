package com.may.xy.controller;

import com.may.xy.api.HelloApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
    @Autowired
    private HelloApi helloApi;

    @RequestMapping(value = "/hello", method = RequestMethod.GET)
    public String sayHello() {
        helloApi.sayHello("K");
        return "hello";
    }
}
