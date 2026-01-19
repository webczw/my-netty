package com.webczw.my.netty;

import com.webczw.my.netty.server.NettyServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class MyNettyApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyNettyApplication.class, args);
        //开启Netty服务
        NettyServer nettyServer = new NettyServer ();
        nettyServer.start();
        log.info("======服务已经启动========");
    }

}
