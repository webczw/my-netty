# CLAUDE.md

本文件为 Claude Code (claude.ai/code) 在此仓库中工作时提供指导。

## 项目概述

这是一个 Spring Boot + Netty 混合应用，演示了在 Spring Boot Web 应用中集成 Netty 的高性能 TCP 网络。项目通过 REST 接口暴露服务，内部通过 TCP 与 Netty 服务器通信。

**核心技术栈：**
- Java 17
- Spring Boot 4.0.1
- Netty 4.1.17.Final
- Maven
- Lombok
- Spring AOP

## 构建和运行命令

```bash
# 编译项目
mvn compile

# 运行测试
mvn test

# 运行应用（启动 Spring Boot 在 8080 端口和 Netty 服务器在 8082 端口）
mvn spring-boot:run

# 构建可执行 JAR
mvn package

# 运行打包的 JAR
java -jar target/my-netty-0.0.1-SNAPSHOT.jar
```

## 架构

### 双层通信架构

应用结合了两个独立的网络层：

1. **Spring Boot Web 层** (端口 8080)
   - 入口：`MyNettyApplication.java` - 在 Spring 上下文初始化后同步启动 Netty 服务器
   - REST 接口：`TestController.java` - 暴露 POST `/helloNetty?msg=<message>`
   - AOP：`MethodLogPrintAspect.java` - 记录带注解方法的执行耗时

2. **Netty TCP 层** (端口 8082)
   - 服务器：`NettyServer.java` - TCP 服务器，Boss(1) + Worker(200) 事件循环组
   - 服务端处理器：`NettyServerHandler.java` - 处理传入消息，返回 UUID 响应
   - 服务端管道：StringDecoder → StringEncoder → NettyServerHandler
   - 客户端工具：`NettyClientUtil.java` - 与服务器通信的同步客户端
   - 客户端处理器：`NettyClientHandler.java` - 捕获服务器响应

### 请求流程

```
HTTP POST /helloNetty?msg=foo
  → TestController.helloNetty()
  → NettyClientUtil.helloNetty()
  → Netty 客户端连接到 127.0.0.1:8082
  → NettyServerHandler 接收消息
  → 服务器返回 UUID
  → 响应返回到 HTTP 调用者
```

### 重要设计特性

**同步 Netty 客户端**：`NettyClientUtil` 中的 Netty 客户端采用同步设计 - 创建连接、发送消息，然后通过 `closeFuture().sync()` 等待响应后返回。这是演示架构的故意设计。

**阻塞式服务器启动**：Netty 服务器在 `MyNettyApplication.main()` 中 Spring Boot 初始化后同步启动。服务器的 `closeFuture().sync()` 会无限阻塞，保持应用运行。

**Netty 服务器配置**：
- 绑定地址：`127.0.0.1:8082`
- Boss 组：1 个线程（接受连接）
- Worker 组：200 个线程（处理 I/O）
- SO_BACKLOG：1024
- SO_KEEPALIVE：启用（每 2 小时 TCP keepalive 探测）

**Netty 客户端配置**：
- TCP_NODELAY：true（禁用 Nagle 算法，立即传输）
- 管道：StringDecoder → StringEncoder → NettyClientHandler

## 代码组织

```
src/main/java/com/webczw/my/netty/
├── MyNettyApplication.java          # 主入口，启动 Spring + Netty
├── controller/
│   └── TestController.java          # 调用 Netty 客户端的 REST 接口
├── server/
│   ├── NettyServer.java             # Netty TCP 服务器
│   ├── NettyServerHandler.java      # 服务端消息处理器
│   └── ServerChannelInitializer.java # 服务端管道设置
├── client/
│   ├── NettyClientHandler.java      # 客户端响应处理器
│   └── ResponseResult.java          # 标准响应包装器
├── util/
│   └── NettyClientUtil.java         # 同步客户端工具
└── aop/
    ├── MethodLogPrint.java          # 自定义日志注解
    └── MethodLogPrintAspect.java    # 方法耗时的 AOP 切面
```

## 关键文件

- **pom.xml:30** - Java 17 版本要求
- **MyNettyApplication.java:15-16** - Netty 服务器在 main() 中 Spring Boot 之后启动
- **NettyServer.java:16-20** - 服务器配置（端口、线程组）
- **NettyClientUtil.java:41** - 响应的同步等待模式

## 开发注意事项

- Lombok 在 `maven-compiler-plugin` 中配置了注解处理器路径 (pom.xml:67-73)
- 应用日志输出到 `log/application.log`
- Netty 服务器仅运行在本地主机 (127.0.0.1)，用于本地演示
- 测试时两层必须同时运行（它们在主应用中一起启动）
