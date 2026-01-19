package com.webczw.my.netty.server;

import com.webczw.my.netty.dto.MessageDTO;
import com.webczw.my.netty.util.JsonUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.UUID;

/**
 * netty服务端处理器
 **/
@Slf4j
public class NettyServerHandler extends ChannelInboundHandlerAdapter {
    /**
     * 客户端连接会触发
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("Channel active......");
    }

    /**
     * 客户端发消息会触发
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        MessageDTO dto = (MessageDTO) msg;
        dto.setUuid(UUID.randomUUID().toString());
        dto.setServerTime(new Date());
        dto.setServerMsg("你也好哦!");
        log.info("服务器收到消息: {}", JsonUtil.toJson(dto));
        ctx.writeAndFlush(dto);
    }

    /**
     * 发生异常触发
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}