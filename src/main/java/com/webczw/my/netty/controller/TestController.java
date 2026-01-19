package com.webczw.my.netty.controller;

import com.webczw.my.netty.aop.MethodLogPrint;
import com.webczw.my.netty.client.ResponseResult;
import com.webczw.my.netty.util.NettyClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class TestController {
    @PostMapping("/helloNetty")
    @MethodLogPrint
    public ResponseResult helloNetty(@RequestParam String msg) {
        return NettyClientUtil.helloNetty(msg);
    }
}
