package com.webczw.my.netty.util;

import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;

import java.io.*;

/**
 * 字节转换工具类
 * <p>
 * 提供对象与字节数组之间的转换功能
 *
 * @author webczw
 */
@Slf4j
public final class ByteUtil {

    /**
     * 私有构造函数，防止实例化
     */
    private ByteUtil() {
        throw new AssertionError("ByteUtil 不支持实例化");
    }

    /**
     * 将对象转换为字节数组
     * <p>
     * 使用 Java 原生序列化方式，对象需实现 Serializable 接口
     *
     * @param obj 要转换的对象
     * @return 字节数组，转换失败返回 null
     */
    public static byte[] objectToByte(Object obj) {
        if (obj == null) {
            log.warn("objectToByte: 对象为 null");
            return null;
        }

        if (!(obj instanceof Serializable)) {
            log.error("objectToByte: 对象未实现 Serializable 接口");
            return null;
        }

        ByteArrayOutputStream baos = null;
        ObjectOutputStream oos = null;
        try {
            baos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(baos);
            oos.writeObject(obj);
            oos.flush();
            return baos.toByteArray();
        } catch (IOException e) {
            log.error("对象转换为字节数组失败", e);
            return null;
        } finally {
            try {
                if (oos != null) {
                    oos.close();
                }
                if (baos != null) {
                    baos.close();
                }
            } catch (IOException e) {
                log.error("关闭流失败", e);
            }
        }
    }

    /**
     * 将字节数组转换为对象
     *
     * @param bytes 字节数组
     * @param <T>   泛型类型
     * @return 转换后的对象，转换失败返回 null
     */
    @SuppressWarnings("unchecked")
    public static <T> T byteToObject(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            log.warn("byteToObject: 字节数组为空");
            return null;
        }

        ByteArrayInputStream bais = null;
        ObjectInputStream ois = null;
        try {
            bais = new ByteArrayInputStream(bytes);
            ois = new ObjectInputStream(bais);
            return (T) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            log.error("字节数组转换为对象失败", e);
            return null;
        } finally {
            try {
                if (ois != null) {
                    ois.close();
                }
                if (bais != null) {
                    bais.close();
                }
            } catch (IOException e) {
                log.error("关闭流失败", e);
            }
        }
    }

    /**
     * 将对象转换为字节数组（使用 JSON 序列化）
     * <p>
     * 使用 Jackson 将对象转换为 JSON 字符串的字节数组
     *
     * @param obj 要转换的对象
     * @return 字节数组，转换失败返回 null
     */
    public static byte[] objectToByteByJson(Object obj) {
        if (obj == null) {
            log.warn("objectToByteByJson: 对象为 null");
            return null;
        }

        try {
            String json = JsonUtil.toJson(obj);
            if (json == null) {
                log.error("objectToByteByJson: JSON 序列化失败");
                return null;
            }
            return json.getBytes();
        } catch (Exception e) {
            log.error("对象通过 JSON 转换为字节数组失败", e);
            return null;
        }
    }

    /**
     * 将字节数组转换为对象（使用 JSON 反序列化）
     *
     * @param bytes 字节数组
     * @param clazz 目标类型
     * @param <T>   泛型类型
     * @return 转换后的对象，转换失败返回 null
     */
    public static <T> T byteToObjectByJson(byte[] bytes, Class<T> clazz) {
        if (bytes == null || bytes.length == 0) {
            log.warn("byteToObjectByJson: 字节数组为空");
            return null;
        }

        try {
            String json = new String(bytes);
            return JsonUtil.fromJson(json, clazz);
        } catch (Exception e) {
            log.error("字节数组通过 JSON 转换为对象失败", e);
            return null;
        }
    }

    /**
     * 将 ByteBuf 转换为字节数组
     * <p>
     * 读取 ByteBuf 中的可读字节并转换为字节数组
     *
     * @param byteBuf Netty 的 ByteBuf 对象
     * @return 字节数组，转换失败返回 null
     */
    public static byte[] byteBufToByte(ByteBuf byteBuf) {
        if (byteBuf == null) {
            log.warn("byteBufToByte: ByteBuf 为 null");
            return null;
        }

        if (!byteBuf.isReadable()) {
            log.warn("byteBufToByte: ByteBuf 不可读");
            return null;
        }

        try {
            // 获取可读字节数
            int length = byteBuf.readableBytes();
            // 创建对应大小的字节数组
            byte[] bytes = new byte[length];
            // 将 ByteBuf 中的数据读取到字节数组
            byteBuf.readBytes(bytes);
            return bytes;
        } catch (Exception e) {
            log.error("ByteBuf 转换为字节数组失败", e);
            return null;
        }
    }

    /**
     * 将字节数组转换为 ByteBuf
     *
     * @param bytes  字节数组
     * @return ByteBuf 对象，转换失败返回 null
     */
    public static ByteBuf byteToByteBuf(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            log.warn("byteToByteBuf: 字节数组为空");
            return null;
        }

        try {
            // 使用 Netty 的 Unpooled 工具类创建 ByteBuf
            return io.netty.buffer.Unpooled.copiedBuffer(bytes);
        } catch (Exception e) {
            log.error("字节数组转换为 ByteBuf 失败", e);
            return null;
        }
    }
}