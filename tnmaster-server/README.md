# TNMaster 后端服务

## 项目介绍
TNMaster 是一个基于 Spring Boot 3.x 和 Kotlin 开发的后端服务项目，采用现代化的技术栈和最佳实践构建。

## 技术栈
- 核心框架：Spring Boot 3.x
- 开发语言：Kotlin
- 数据库：PostgreSQL
- 缓存：Redis
- 对象存储：MinIO
- 安全框架：Sa-Token
- ORM框架：Jimmer
- 数据库迁移：Flyway
- 构建工具：Gradle
- 代码格式化：Spotless

## 环境要求
- JDK 24+
- Gradle 8.x
- PostgreSQL 数据库
- Redis 服务
- MinIO 服务

## 项目结构
```
tnmaster-server/
├── src/                    # 源代码目录
├── build/                  # 构建输出目录
├── gradle/                 # Gradle 包装器配置
├── .github/               # GitHub 相关配置
├── .vscode/               # VS Code 配置
└── build.gradle.kts       # Gradle 构建配置
```

## 快速开始

### 1. 环境准备
- 确保已安装 JDK 24 或更高版本
- 安装并配置 PostgreSQL 数据库
- 安装并配置 Redis 服务
- 安装并配置 MinIO 服务

### 2. 配置
1. 在项目根目录创建 `.env` 文件
2. 配置必要的环境变量，包括：
   - 数据库连接信息
   - Redis 连接信息
   - MinIO 配置信息
   - 其他必要的配置项

### 3. 构建和运行
```bash
# 构建项目
./gradlew build

# 运行项目
./gradlew bootRun
```

## 开发指南

### 代码规范
- 使用 Spotless 进行代码格式化
- 遵循 Kotlin 编码规范
- 使用 JPA 注解进行数据库映射

### 数据库迁移
- 使用 Flyway 进行数据库版本控制
- 迁移脚本位于 `src/main/resources/db/migration`

### 测试
```bash
# 运行测试
./gradlew test
```

## 部署
项目支持 Docker 部署，可以使用 Docker Compose 进行容器化部署。

## 许可证
本项目采用 MIT 许可证，详见 [LICENSE](LICENSE) 文件。
