package com.webczw.my.netty.util;

import com.webczw.my.netty.client.NettyClientHandler;
import com.webczw.my.netty.common.MessageDecoder;
import com.webczw.my.netty.common.MessageEncoder;
import com.webczw.my.netty.dto.MessageDTO;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * Netty客户端
 **/
@Slf4j
public class NettyClientUtil {
    public static MessageDTO helloNetty(MessageDTO msgDTO) {
        NettyClientHandler nettyClientHandler = new NettyClientHandler();
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap().group(group)
                //该参数的作用就是禁止使用Nagle算法，使用于小数据即时传输
                .option(ChannelOption.TCP_NODELAY, true).channel(NioSocketChannel.class).handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast("decoder", new MessageDecoder());
                        socketChannel.pipeline().addLast("encoder", new MessageEncoder());
                        socketChannel.pipeline().addLast(nettyClientHandler);
                    }
                });
        try {
            ChannelFuture future = bootstrap.connect("127.0.0.1", 8082).sync();
            log.info("客户端发送成功....");
            //发送消息
            future.channel().writeAndFlush(msgDTO);
            // 等待连接被关闭
            future.channel().closeFuture().sync();
            return nettyClientHandler.getMsgDTO();
        } catch (Exception e) {
            log.error("客户端Netty失败", e);
            throw new RuntimeException("服务异常");
        } finally {
            //以一种优雅的方式进行线程退出
            group.shutdownGracefully();
        }
    }
}