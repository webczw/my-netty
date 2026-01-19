# Netty 前端项目

基于 Vue 3 + Element Plus 的前端页面，用于调用后端 Netty 通信服务。

## 功能特性

- 必填的客户端消息内容字段
- 消息开关标识（开启/关闭）
- 提交按钮，调用 `/netty/helloNetty` 接口
- 实时显示后端返回的结果

## 快速开始

### 安装依赖

```bash
npm install
```

### 启动开发服务器

```bash
npm run dev
```

开发服务器运行在 `http://localhost:3000`

### 构建生产版本

```bash
npm run build
```

### 预览生产构建

```bash
npm run preview
```

## 后端服务要求

后端 Spring Boot 服务需要运行在 `http://localhost:8080`

启动后端：
```bash
# 在项目根目录下
mvn spring-boot:run
```

## 接口说明

### POST /netty/helloNetty

**请求体：**
```json
{
  "clientMsg": "消息内容",
  "msgSwitch": "1" 或 "0"
}
```

**响应：**
```json
{
  "success": true,
  "data": {
    "uuid": "...",
    "serverTime": "...",
    "serverMsg": "...",
    "clientMsg": "...",
    "msgSwitch": "1" 或 "0"
  },
  "message": "操作成功"
}
```

## 项目结构

```
frontend/
├── src/
│   ├── App.vue          # 主组件
│   ├── main.js          # 入口文件
│   ├── assets/          # 静态资源
│   └── components/      # 组件
├── public/              # 公共静态文件
├── index.html           # HTML 模板
├── vite.config.js       # Vite 配置
├── package.json         # 项目配置
└── README.md            # 项目说明
```
