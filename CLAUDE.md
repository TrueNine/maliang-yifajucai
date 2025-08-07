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
  - buildSrc/ - Gradle构建脚本

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

# 运行测试
./gradlew test

# 代码格式化检查
./gradlew spotlessCheck

# 应用代码格式化
./gradlew spotlessApply
```

### 前端 (frontend/)
```bash
# 开发环境运行
pnpm dev

# 生产构建
pnpm build

# 类型检查
pnpm type-check

# 代码检查和修复
pnpm lint

# 生成API客户端
pnpm api
```

## 数据库架构

- 使用Flyway进行版本化数据库迁移
- 迁移脚本位于：`backend/application/src/main/resources/db/migration/`
- 支持PostgreSQL特性和扩展

## 关键业务领域

- 用户管理 (UserAccount, UserInfo)
- 就业服务 (Job, JobSeeker)
- 证件管理 (Cert, BankCard)  
- 地址服务 (Address, AddressDetails)
- 黑名单管理 (BlackList)
- 审核流程 (Audit相关实体)
- 权限控制 (Role, RoleGroup, Permissions)

## 开发注意事项

- 严格遵循CLAUDE.md中的Kotlin和Java编码规范
- 使用TDD开发方式，测试优先
- 数据库操作通过Jimmer ORM进行，支持类型安全的查询构建
- 所有异步操作使用Spring的虚拟线程支持
- 文件上传限制为2GB
