# My Netty

Spring Boot + Netty 混合应用，配备 Vue 3 + Element Plus 前端。REST API 与 Netty TCP 服务器内部通信，前端提供基于表单的用户界面用于提交消息和显示响应。

## 技术栈

**后端：**
- Java 17
- Spring Boot 4.0.1
- Netty 4.1.17.Final
- Jackson (JSON 序列化)
- Lombok
- Spring AOP

**前端：**
- Vue 3.4.0
- Element Plus 2.5.0
- Vite 5.0.0
- Axios 1.6.0

## 项目架构

### 三层通信架构

```
┌─────────────┐         HTTP POST         ┌─────────────┐
│   前端       │ ──────────────────────────▶│  REST API   │
│  (Vue 3)    │      /netty/helloNetty     │  (8080)     │
└─────────────┘                            └──────┬──────┘
                                                    │
                                                    ▼
                                              ┌──────────┐
                                              │ Netty    │
                                              │ Client   │
                                              └────┬─────┘
                                                   │ TCP
                                                   ▼
                                              ┌──────────┐
                                              │ Netty    │
                                              │ Server   │
                                              │ (8082)   │
                                              └──────────┘
```

### 消息流程

1. **前端**：用户通过 Vue 3 表单提交消息
2. **REST 层**（端口 8080）：
    - `NettyController` 暴露 `POST /netty/helloNetty`
    - `@MethodLogPrint` 注解触发 AOP 计时切面
3. **Netty Client**：
    - 每次请求创建新的 Netty 客户端
    - TCP 连接到 `127.0.0.1:8082`
4. **Netty Server**（端口 8082）：
    - Boss 组：1 个线程（接受连接）
    - Worker 组：200 个线程（处理 I/O）
    - `MessageDecoder`：ByteBuf → byte[] → MessageDTO
    - `NettyServerHandler`：接收 MessageDTO，设置响应字段（uuid, serverTime, serverMsg）
    - `MessageEncoder`：MessageDTO → byte[] → ByteBuf
5. **响应返回**：
    - NettyClientHandler 捕获响应
    - HTTP 返回 `ResponseResult<MessageDTO>`
    - 前端显示响应

### 序列化策略

**Netty 传输（主要）：** 使用 `ByteUtil` 实现 Java 原生序列化
- `ByteUtil.objectToByte(obj)` - 使用 `ObjectOutputStream`
- `ByteUtil.byteToObject(bytes)` - 使用 `ObjectInputStream`
- 所有 DTO 必须实现 `Serializable`

**日志/调试（次要）：** 使用 Jackson `JsonUtil` 实现 JSON
- 用于处理器中的日志记录

### Netty 管道配置

**服务端管道：**
```java
MessageDecoder → MessageEncoder → NettyServerHandler
```

**客户端管道：**
```java
MessageDecoder → MessageEncoder → NettyClientHandler
```

## 快速开始

### 后端

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

### 前端

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

## API 接口

### POST /netty/helloNetty

提交消息到 Netty 服务器并获取响应。

**请求体：**
```json
{
  "clientMsg": "Hello Netty",
  "msgSwitch": true
}
```

**响应：**
```json
{
  "success": true,
  "data": {
    "uuid": "550e8400-e29b-41d4-a716-446655440000",
    "serverTime": "2026-01-20 12:00:00",
    "serverMsg": "Server received your message",
    "clientMsg": "Hello Netty",
    "msgSwitch": true
  },
  "message": "Success"
}
```

## 关键文件

**后端：**
- `pom.xml` - Maven 依赖配置
- `application.yml` - 应用配置（上下文路径 `/netty`）
- `MyNettyApplication.java` - Spring Boot 启动类，启动 Netty 服务器
- `NettyServer.java` - Netty 服务器配置（端口 8082，Boss+Worker 线程组）
- `ServerChannelInitializer.java` - Netty 管道初始化
- `NettyController.java` - REST API 控制器
- `NettyServerHandler.java` - Netty 服务端处理器
- `NettyClientUtil.java` - Netty 客户端工具类
- `MessageDecoder.java` - 消息解码器
- `MessageEncoder.java` - 消息编码器
- `MessageDTO.java` - 消息数据传输对象
- `ResponseResult.java` - 通用 API 响应包装器

**前端：**
- `frontend/package.json` - NPM 依赖配置
- `frontend/vite.config.js` - Vite 配置（代理、端口）
- `frontend/src/main.js` - Vue 应用入口
- `frontend/src/App.vue` - 主组件（表单和响应显示）

## 开发注意事项

- Netty 服务器仅绑定到本地主机 (127.0.0.1)
- 客户端每次请求创建新连接（无连接池）
- 所有通过 Netty 传输的 DTO 必须实现 `Serializable`
- NettyServerHandler 直接接收 `MessageDTO`（管道处理反序列化）
- 前端表单验证通过 `.catch(() => false)` 捕获错误
- 后端上下文路径为 `/netty`
- 前端代理将 `/netty/*` 请求路由到 `http://localhost:8080`

## 参考文档

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/4.0.1/maven-plugin)
* [Spring Web](https://docs.spring.io/spring-boot/4.0.1/reference/web/servlet.html)
* [Netty Documentation](https://netty.io/wiki/user-guide.html)
* [Vue 3 Documentation](https://vuejs.org/)
* [Element Plus Documentation](https://element-plus.org/)