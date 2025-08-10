# Sa-Token 到 Casbin 迁移指南

## 概述

本项目已成功从 sa-token 认证授权框架迁移到 Casbin + JWT 的解决方案。本文档记录了迁移过程和新的使用方式。

## 迁移内容

### 1. 依赖变更

**移除的依赖：**
- `cn.dev33.sa-token:sa-token-spring-boot3-starter`
- `cn.dev33.sa-token:sa-token-redis-jackson`

**新增的依赖：**
- `org.casbin:jcasbin:1.55.0` - Casbin 核心库
- `org.casbin:casbin-spring-boot-starter:1.4.0` - Casbin Spring Boot 集成
- `io.jsonwebtoken:jjwt-api:0.12.3` - JWT API
- `io.jsonwebtoken:jjwt-impl:0.12.3` - JWT 实现
- `io.jsonwebtoken:jjwt-jackson:0.12.3` - JWT Jackson 支持

### 2. 核心组件变更

#### 认证服务
- **原来：** `SaTokenService`
- **现在：** `AuthService`

#### 权限检查
- **原来：** `StpUtil.checkPermission()`
- **现在：** `enforcer.enforce(account, permission, "allow")`

#### 用户上下文
- **原来：** `StpUtil.getSession()`
- **现在：** `UserContextHolder.getCurrentUser()`

### 3. 配置变更

#### application.yaml
```yaml
# 移除
sa-token:
  is-log: true
  is-color-log: true
  log-level: TRACE

# 新增
app:
  jwt:
    secret: ${JWT_SECRET:mySecretKeyForJwtTokenGeneration123456789}
    expiration: ${JWT_EXPIRATION:86400} # 24 hours
  interceptors:
    casbin-admin:
      enabled: true
```

### 4. 权限模型

#### Casbin 模型文件 (casbin/model.conf)
```ini
[request_definition]
r = sub, obj, act

[policy_definition]
p = sub, obj, act

[role_definition]
g = _, _
g2 = _, _

[policy_effect]
e = some(where (p.eft == allow))

[matchers]
m = g(r.sub, p.sub) && r.obj == p.obj && r.act == p.act
```

## 新的使用方式

### 1. 用户登录

```kotlin
// 设置用户登录状态
val token = authService.setUserLoginState(
    account = "user123",
    userId = userId,
    request = request,
    roles = setOf("USER", "ADMIN"),
    permissions = setOf("READ_USER", "WRITE_USER")
)

// 返回 JWT token 给客户端
```

### 2. 权限检查

```kotlin
// 检查用户权限
val hasPermission = aclService.hasPermission("user123", "READ_USER")

// 检查用户角色
val hasRole = aclService.hasRole("user123", "ADMIN")

// 获取当前用户
val currentUser = authService.getCurrentUser()
```

### 3. 用户登出

```kotlin
// 用户登出
authService.logout("user123")

// 禁用用户
authService.disableUser("user123", Duration.ofDays(1))
```

### 4. 权限管理

```kotlin
// 添加用户角色
aclService.addRoleForUser("user123", "ADMIN")

// 移除用户角色
aclService.removeRoleForUser("user123", "USER")

// 为角色添加权限
aclService.addPermissionForRole("ADMIN", "DELETE_USER")

// 重新加载权限策略
aclService.reloadPolicy()
```

## 客户端集成

### HTTP 请求头
客户端需要在请求头中携带 JWT token：

```
Authorization: Bearer <jwt_token>
```

### 前端示例
```javascript
// 登录后保存 token
localStorage.setItem('token', response.data.token);

// 请求时携带 token
axios.defaults.headers.common['Authorization'] = `Bearer ${localStorage.getItem('token')}`;
```

## 数据库变更

现有的权限数据结构保持不变：
- `UserAccount` - 用户账号
- `RoleGroup` - 角色组
- `Role` - 角色
- `Permissions` - 权限

Casbin 通过数据库适配器自动加载这些权限关系。

## 测试

运行测试确保迁移成功：

```bash
./gradlew test
```

主要测试类：
- `AuthServiceTest` - 认证服务测试
- 其他相关的集成测试

## 注意事项

1. **会话管理：** 现在使用 Redis + JWT 的方式管理用户会话
2. **权限缓存：** Casbin 内置权限缓存，可通过 `reloadPolicy()` 刷新
3. **Token 过期：** JWT token 有过期时间，需要客户端处理 token 刷新
4. **安全性：** JWT secret 应该使用强密码并定期更换

## 故障排除

### 常见问题

1. **Token 验证失败**
   - 检查 JWT secret 配置
   - 确认 token 格式正确
   - 检查 token 是否过期

2. **权限检查失败**
   - 确认 Casbin 策略已正确加载
   - 检查用户-角色-权限关系
   - 调用 `reloadPolicy()` 刷新策略

3. **Redis 连接问题**
   - 检查 Redis 配置
   - 确认 Redis 服务正常运行

## 性能优化

1. **权限缓存：** Casbin 自动缓存权限策略
2. **Redis 缓存：** 用户会话信息缓存在 Redis 中
3. **JWT 无状态：** JWT token 本身包含用户信息，减少数据库查询

## 后续计划

1. 考虑实现 JWT token 刷新机制
2. 添加更细粒度的权限控制
3. 集成 OAuth2 支持
4. 添加审计日志功能
