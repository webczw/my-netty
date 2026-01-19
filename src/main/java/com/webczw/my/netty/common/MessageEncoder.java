package com.webczw.my.netty.common;

import com.webczw.my.netty.dto.MessageDTO;
import com.webczw.my.netty.util.ByteUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class MessageEncoder extends MessageToByteEncoder<MessageDTO> {
    @Override
    protected void encode(ChannelHandlerContext ctx, MessageDTO messageDTO, ByteBuf byteBuf) throws Exception {
        // 将MessageDTO对象序列化为字节数组并写入ByteBuf
         byte[] bytes = ByteUtil.objectToByte(messageDTO);
        byteBuf.writeBytes(bytes);
        ctx.flush();
    }
}