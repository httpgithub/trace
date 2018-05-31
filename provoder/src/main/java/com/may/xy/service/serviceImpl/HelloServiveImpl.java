package com.may.xy.service.serviceImpl;

import com.may.xy.service.HelloServive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class HelloServiveImpl implements HelloServive {
    private static final Logger logger = LoggerFactory.getLogger(HelloServiveImpl.class);

    @Override
    public String sayHello(String name) {
        logger.info("HHHHHH");
        return null;
    }
}
