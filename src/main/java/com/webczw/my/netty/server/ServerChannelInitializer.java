package com.webczw.my.netty.server;

import com.webczw.my.netty.common.MessageDecoder;
import com.webczw.my.netty.common.MessageEncoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

/**
 * netty服务初始化器
 **/
public class ServerChannelInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        //添加编解码
        socketChannel.pipeline().addLast("decoder", new MessageDecoder());
        socketChannel.pipeline().addLast("encoder", new MessageEncoder());
        socketChannel.pipeline().addLast(new NettyServerHandler());
    }
}