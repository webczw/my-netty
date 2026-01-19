package com.webczw.my.netty.common;

import com.webczw.my.netty.util.ByteUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class MessageDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf in, List<Object> out) throws Exception {
        Object obj = ByteUtil.byteToObject(ByteUtil.byteBufToByte(in));
        out.add(obj);
    }
}
