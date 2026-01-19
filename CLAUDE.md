# CLAUDE.md

本文件为 Claude Code (claude.ai/code) 在此仓库中工作时提供指导。

## 项目概述

Spring Boot + Netty 混合应用，演示了在 Spring Boot Web 应用中集成 Netty 的高性能 TCP 网络。REST API 通过 TCP 与 Netty 服务器进行内部通信。

**核心技术栈：**
- Java 17
- Spring Boot 4.0.1
- Netty 4.1.17.Final
- Jackson（用于 JSON 操作）
- Lombok
- Spring AOP

## 构建和运行命令

```bash
# 编译项目
mvn compile

# 运行测试
mvn test

# 运行应用（Spring Boot 在 8080 端口，Netty 服务器在 8082 端口）
mvn spring-boot:run

# 构建可执行 JAR
mvn package

# 运行打包的 JAR
java -jar target/my-netty-0.0.1-SNAPSHOT.jar
```

## 架构

### 双层通信架构

1. **Spring Boot Web 层**（端口 8080）
   - 入口：`MyNettyApplication.java` - Spring 上下文初始化后启动 Netty 服务器
   - REST：`NettyController.java` - 暴露调用 Netty 客户端的 POST 接口
   - AOP：`MethodLogPrintAspect.java` - 记录 `@MethodLogPrint` 注解方法的执行时间

2. **Netty TCP 层**（端口 8082）
   - 服务器：`NettyServer.java` - TCP 服务器，Boss(1) + Worker(200) 事件循环组
   - 管道：`ServerChannelInitializer.java` - MessageDecoder → MessageEncoder → NettyServerHandler
   - 处理器：`NettyServerHandler.java` - 接收 `MessageDTO`，添加 uuid/时间戳/响应，写回
   - 客户端：`NettyClientUtil.java` - 同步客户端，每次请求创建新连接

### 消息流程

```
HTTP POST /helloNetty?msg=foo
  → NettyController.helloNetty() [@MethodLogPrint]
  → NettyClientUtil.helloNetty() [创建新的 Netty 客户端]
  → TCP 连接到 127.0.0.1:8082
  → MessageDecoder: bytes → MessageDTO
  → NettyServerHandler.channelRead() 接收 MessageDTO
  → Handler 设置 uuid, serverTime, serverMsg
  → MessageEncoder: MessageDTO → bytes
  → NettyClientHandler 捕获响应
  → closeFuture().sync() 等待响应
  → HTTP 返回 ResponseResult<MessageDTO>
```

### 序列化策略

**主要（数据传输）：** Java 原生序列化
- `ByteUtil.objectToByte(obj)` - 使用 `ObjectOutputStream`
- `ByteUtil.byteToObject(bytes)` - 使用 `ObjectInputStream`
- 所有 DTO 必须实现 `Serializable`
- 用于实际的 Netty 消息传输

**次要（日志/调试）：** 通过 Jackson 实现 JSON
- `JsonUtil.toJson(obj)` - 对象转 JSON 字符串
- `JsonUtil.fromJson(json, clazz)` - JSON 转对象
- 用于处理器的日志记录

**Netty 集成：**
- `ByteBuf byteBufToByte(byteBuf)` - 从 Netty ByteBuf 读取可读字节
- `ByteBuf byteToByteBuf(bytes)` - 从字节数组创建 ByteBuf

### Netty 管道配置

**服务端管道**（`ServerChannelInitializer.java:15-17`）：
```java
pipeline.addLast("decoder", new MessageDecoder());  // ByteToMessageDecoder
pipeline.addLast("encoder", new MessageEncoder());  // MessageToByteEncoder<MessageDTO>
pipeline.addLast(new NettyServerHandler());
```

**客户端管道**（`NettyClientUtil.java`）：
- 相同：MessageDecoder → MessageEncoder → NettyClientHandler
- 两者都使用相同的 `ByteUtil` 进行序列化

### Netty 服务器配置

- 绑定地址：`127.0.0.1:8082`
- Boss 组：1 个线程（接受连接）
- Worker 组：200 个线程（处理 I/O）
- SO_BACKLOG：1024
- SO_KEEPALIVE：启用
- TCP_NODELAY：true（仅客户端，禁用 Nagle 算法以立即传输）

## 关键文件

- `pom.xml:30` - Java 17 版本要求
- `pom.xml:44-56` - Jackson 依赖
- `MyNettyApplication.java` - Netty 服务器在 Spring Boot 后启动，通过 `closeFuture().sync()` 阻塞
- `NettyServer.java:16-20` - 服务器端口和线程组配置
- `ServerChannelInitializer.java` - 管道设置
- `MessageDecoder.java:13` - 解码：`ByteBuf → byte[] → MessageDTO`
- `MessageEncoder.java:13` - 编码：`MessageDTO → byte[] → ByteBuf`
- `NettyServerHandler.java:31` - 接收 `MessageDTO`（不是 String），设置响应字段
- `NettyClientUtil.java:41` - 同步等待：`closeFuture().sync()`

## 开发注意事项

- Lombok 注解处理器配置在 `maven-compiler-plugin:82-86`
- 日志输出到 `log/application.log`（在 `application.yml` 中配置）
- Netty 服务器仅运行在本地主机（127.0.0.1）
- 客户端每次请求创建新连接（非连接池）
- 所有通过 Netty 传输的 DTO 必须实现 `Serializable`
- 使用 Java 序列化进行传输；Jackson 用于 JSON 操作
- `NettyServerHandler` 直接接收 `MessageDTO`（管道处理反序列化）