package com.webczw.my.netty.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 用户消息数据传输对象
 * <p>
 * 用于封装用户通过网络发送的消息及其开关配置
 *
 * @author webczw
 */
@Getter
@Setter
public class MessageDTO implements Serializable {
    /**
     * 序列化版本号
     */
    @Serial
    private static final long serialVersionUID = 9210993312558287973L;
    /**
     * 唯一标识符
     * <p>
     * 用于标识每次通信会话的唯一 ID，通常为 UUID 格式
     * </p>
     */
    private String uuid;

    /**
     * 服务器时间
     * <p>
     * 记录服务器接收到请求或处理请求的时间戳
     * </p>
     */
    private Date serverTime;

    /**
     * 服务端消息
     * <p>
     * 服务器处理完成后返回给客户端的消息内容
     * </p>
     */
    private String serverMsg;
    /**
     * 客户端消息内容
     */
    private String clientMsg;

    /**
     * 消息开关标识
     * 用于控制消息的处理方式或路由策略
     */
    private String msgSwitch;
}