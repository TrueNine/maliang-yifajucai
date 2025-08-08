# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

这是一个名为 maliang-yifajucai 的全栈就业服务平台项目，包含后端服务和前端管理系统。

## 架构结构

### 后端架构 (backend/)
- **分层架构**：采用DDD（领域驱动设计）分层架构
  - application/ - 应用层（Spring Boot应用入口）
  - domain/ - 领域层（领域模型和业务逻辑）
  - infrastructure/ - 基础设施层
  - build-logic/ - Gradle构建脚本

- **技术栈**：
  - Spring Boot 3.x + Kotlin 
  - JDK 24 (启用 Java 预览特性)
  - Jimmer ORM 框架
  - PostgreSQL + Flyway数据库迁移
  - Redis缓存 + MinIO对象存储
  - Sa-Token安全框架
  - LiteFlow流程引擎
  - Testcontainers测试

### 前端架构 (frontend/)
- **技术栈**：Vue 3 + TypeScript + Vite
- **UI框架**：Vuetify + Element Plus + Naive UI
- **状态管理**：Pinia
- **构建工具**：Vite + pnpm

## 关键开发命令

### 后端 (backend/)
```bash
# 构建项目
./gradlew build

# 运行应用
./gradlew bootRun

# 运行所有测试
./gradlew test

# 运行单个测试类
./gradlew test --tests "com.tnmaster.application.service.CertServiceTest"

# 运行单个测试方法（使用JUnit 5格式）
./gradlew test --tests "com.tnmaster.application.service.CertServiceTest.正常 上传单个有效证件附件时，应成功保存并返回正确结果"

# 代码格式化检查
./gradlew spotlessCheck

# 应用代码格式化
./gradlew spotlessApply

# 构建JAR包
./gradlew bootJar

# 清理构建输出
./gradlew clean
```

### 前端 (frontend/)
```bash
# 开发环境运行 (端口3000)
pnpm dev

# 测试环境运行 (端口80)
pnpm test

# 生产构建
pnpm build

# 完整构建流程（包含API生成、类型检查、代码检查）
pnpm build

# 类型检查
pnpm type-check

# 代码检查和修复
pnpm lint

# 同时运行类型检查和代码检查
pnpm scan

# 生成API客户端
pnpm api

# 上传构建产物到服务器
pnpm upload-dist
```

## 数据库架构

- 使用Flyway进行版本化数据库迁移
- 迁移脚本位于：`backend/src/main/resources/db/migration/`
- 支持PostgreSQL特性和扩展
- Flyway配置：基线版本3000，支持乱序迁移，自动基线创建
- 数据库连接池：HikariCP，最小空闲连接5，最大连接数15
- 事务管理：默认关闭自动提交，使用手动事务控制

## 开发规范和约束

### 后端开发规范
- JDK 24 + 启用预览特性（--enable-preview）
- Kotlin编译目标：JVM_24，启用JSR305严格模式
- 使用虚拟线程：Spring Boot已启用虚拟线程支持
- ORM框架：Jimmer（类型安全的查询构建）
- 事务管理：使用@Transactional + @ACID注解
- 测试：Testcontainers + PostgreSQL + MinIO容器化测试
- 代码格式化：Spotless插件强制执行

### 测试组织规范
- 使用@Nested内部类组织相关测试
- 测试方法名使用反引号包围的中文描述：`fun \`测试场景描述\`()`
- 测试分类：正常、边界、异常用例
- 每个测试必须包含断言，不允许空测试
- 数据库测试使用@RDBRollback自动回滚

### 前端开发规范
- Node.js 24+ + pnpm 10.12.1+
- 组件自动导入：支持Element Plus、Naive UI、Vuetify、Quasar
- 路由：基于文件系统的自动路由生成
- 构建优化：Terser压缩、Gzip压缩、文件名哈希
- 开发工具：Vue DevTools、ESLint、TypeScript严格模式

### 环境配置
- 后端支持.env文件自动加载（项目根目录../.env）
- 前端环境配置位于./src/config/env/
- 文件上传限制：2GB
- 数据库：PostgreSQL（开发/测试使用Testcontainers）
- 缓存：Redis（开发环境database 0）
- 对象存储：MinIO
