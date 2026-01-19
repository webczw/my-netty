package com.webczw.my.netty.controller;

import com.webczw.my.netty.aop.MethodLogPrint;
import com.webczw.my.netty.common.ResponseResult;
import com.webczw.my.netty.dto.MessageDTO;
import com.webczw.my.netty.util.NettyClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class NettyController {
    @PostMapping("/helloNetty")
    @MethodLogPrint
    public ResponseResult<MessageDTO> helloNetty(@RequestBody MessageDTO msgDTO) {
        MessageDTO messageDTO = NettyClientUtil.helloNetty(msgDTO);
        return ResponseResult.success(messageDTO, "操作成功");
    }
}
