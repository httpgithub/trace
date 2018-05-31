package com.may.xy.apiProvider;

import com.alibaba.dubbo.config.annotation.Service;
import com.may.xy.api.HelloApi;
import org.springframework.stereotype.Component;

@Service
@Component
public class HelloApiProvider implements HelloApi {
    @Override
    public String sayHello(String name) {
        return null;
    }
}
