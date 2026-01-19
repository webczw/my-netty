# CLAUDE.md

本文件为 Claude Code (claude.ai/code) 在此仓库中工作时提供指导。

## 项目概述

Spring Boot + Netty 混合应用，配备 Vue 3 + Element Plus 前端。REST API 与 Netty TCP 服务器内部通信。前端提供基于表单的用户界面用于提交消息和显示响应。

**核心技术栈：**
- Java 17, Spring Boot 4.0.1, Netty 4.1.17.Final
- Jackson (JSON), Lombok, Spring AOP
- Vue 3.4.0, Element Plus 2.5.0, Vite 5.0.0

## 构建和运行命令

### 后端 (Maven)
```bash
# 编译
mvn compile

# 运行测试
mvn test

# 运行 Spring Boot（REST 在 8080 端口，Netty TCP 在 8082 端口）
mvn spring-boot:run

# 构建 JAR
mvn package

# 运行 JAR
java -jar target/my-netty-0.0.1-SNAPSHOT.jar
```

### 前端 (npm/Vite)
```bash
cd frontend

# 安装依赖
npm install

# 开发服务器（端口 3000，代理到 8080）
npm run dev

# 生产构建
npm run build

# 预览生产构建
npm run preview
```

## 架构

### 三层通信架构

**1. REST 层**（端口 8080）
- 上下文路径：`/netty`
- `NettyController.java` 暴露 POST `/netty/helloNetty`
- `@MethodLogPrint` 注解触发 AOP 计时切面

**2. Netty TCP 层**（端口 8082）
- Boss 组：1 个线程（接受连接）
- Worker 组：200 个线程（处理 I/O）
- 服务器仅绑定到 `127.0.0.1:8082`

**3. 前端层**（端口 3000）
- Vue 3 Composition API 配合 Element Plus UI
- Vite 开发服务器将 `/netty/*` 代理到 `http://localhost:8080`

### 消息流程

```
前端：用户提交表单
  → HTTP POST /netty/helloNetty (axios)
  → NettyController.helloNetty() [@MethodLogPrint 记录日志]
  → NettyClientUtil.helloNetty() [每次请求创建新的 Netty 客户端]
  → TCP 连接到 127.0.0.1:8082
  → MessageDecoder: ByteBuf → byte[] → MessageDTO
  → NettyServerHandler.channelRead() 接收 MessageDTO
  → Handler 设置：uuid, serverTime, serverMsg
  → MessageEncoder: MessageDTO → byte[] → ByteBuf
  → NettyClientHandler 捕获响应
  → closeFuture().sync() 等待响应
  → HTTP 返回 ResponseResult<MessageDTO>
  → 前端显示响应
```

### 序列化策略

**主要（Netty 传输）：** 通过 `ByteUtil` 实现 Java 原生序列化
- `ByteUtil.objectToByte(obj)` - 使用 `ObjectOutputStream`
- `ByteUtil.byteToObject(bytes)` - 使用 `ObjectInputStream`
- 所有 DTO 必须实现 `Serializable`
- 用于实际的 Netty 消息传输

**次要（日志/调试）：** 通过 Jackson `JsonUtil` 实现 JSON
- `JsonUtil.toJson(obj)` - 对象转 JSON 字符串
- `JsonUtil.fromJson(json, clazz)` - JSON 转对象
- 用于处理器中的日志记录

### Netty 管道配置

**服务端管道**（`ServerChannelInitializer.java`）：
```java
pipeline.addLast("decoder", new MessageDecoder());   // ByteToMessageDecoder
pipeline.addLast("encoder", new MessageEncoder());   // MessageToByteEncoder<MessageDTO>
pipeline.addLast(new NettyServerHandler());
```

**客户端管道**（`NettyClientUtil.java`）：
- 相同：MessageDecoder → MessageEncoder → NettyClientHandler
- 两者都使用 `ByteUtil` 进行序列化

### 前端架构

**关键组件：**
- `App.vue`：主组件，包含表单（clientMsg, msgSwitch）和响应显示
- 表单验证：Element Plus 规则，clientMsg 为必填项
- 提交处理器：在 API 调用前验证表单，验证失败时阻止后端调用
- `main.js`：全局注册 Element Plus 和所有图标

**Vite 配置：**
- 开发服务器运行在 3000 端口
- 代理：`/netty/*` → `http://localhost:8080`

## 关键文件

**后端：**
- `pom.xml` - Java 17, Spring Boot 4.0.1, Netty 4.1.17.Final
- `application.yml` - 上下文路径 `/netty`，日志文件 `log/application.log`
- `MyNettyApplication.java` - 在 Spring 上下文初始化后启动 Netty 服务器，阻塞在 `closeFuture().sync()`
- `NettyServer.java` - 端口 8082，Boss(1) + Worker(200) 线程组
- `ServerChannelInitializer.java` - 管道设置
- `NettyController.java` - POST `/netty/helloNetty` 端点
- `NettyServerHandler.java` - 接收 MessageDTO，设置响应字段
- `NettyClientUtil.java` - 同步客户端，`closeFuture().sync()` 等待响应
- `MessageDecoder.java` - 解码：ByteBuf → byte[] → MessageDTO
- `MessageEncoder.java` - 编码：MessageDTO → byte[] → ByteBuf
- `MessageDTO.java` - 可序列化 DTO，包含：uuid, serverTime, serverMsg, clientMsg, msgSwitch
- `ResponseResult.java` - 通用 API 响应包装器：success, data, message

**前端：**
- `frontend/package.json` - Vue 3.4.0, Element Plus 2.5.0, axios 1.6.0, Vite 5.0.0
- `frontend/vite.config.js` - 端口 3000，代理配置
- `frontend/src/main.js` - Vue 应用入口，Element Plus 设置
- `frontend/src/App.vue` - 主组件，包含表单和响应显示

## 开发注意事项

- Lombok 注解处理器配置在 `maven-compiler-plugin:82-86`
- Netty 服务器仅绑定到本地主机 (127.0.0.1)
- 客户端每次请求创建新连接（无连接池）
- 所有通过 Netty 传输的 DTO 必须实现 `Serializable`
- NettyServerHandler 直接接收 `MessageDTO`（管道处理反序列化）
- 前端表单验证通过 `.catch(() => false)` 捕获错误，防止在无效输入时调用后端
- 后端上下文路径为 `/netty`（在 `application.yml` 中配置）
- 前端代理将 `/netty/*` 请求路由到 `http://localhost:8080`
