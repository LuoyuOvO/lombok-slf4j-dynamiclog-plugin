# lombok-slf4j-dynamiclog-plugin

动态日志级别插件，扩展 `lombok.extern.slf4j.Slf4j`，支持从 Nacos 动态读取配置，为指定接口动态调整日志级别，并支持代码执行时间统计（类似 Arthas trace 命令）。

## 功能特性

- ✅ 从 Nacos 读取配置判断框架是否启用
- ✅ 动态读取配置，为指定接口设置 N 次动态日志级别打印
- ✅ 匹配成功后的接口打印记录日志
- ✅ 支持 RPC 和 Feign 远程调用的日志级别动态调整（只能选择一种）
- ✅ 支持 Maven 引入，自动集成
- ✅ 支持 Java Agent 扩展
- ✅ 支持配置动态指定日志输出路径和文件
- ✅ 所有配置使用 `luoyu` 开头
- ✅ 支持请求方法调用时长统计，支持每一行代码的执行时间统计（类似 Arthas trace）

## 快速开始

### 1. Maven 依赖

```xml
<dependency>
    <groupId>com.luoyu</groupId>
    <artifactId>lombok-slf4j-dynamiclog-plugin</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### 2. 配置文件

在 `application.yml` 中添加配置：

```yaml
luoyu:
  dynamic-log:
    enabled: true
    nacos-server-addr: 127.0.0.1:8848
    nacos-data-id: luoyu-dynamic-log-config
    nacos-group: DEFAULT_GROUP
    remote-call-type: feign  # 或 rpc，只能选择一个
    log-output-path: /var/log/dynamic-log
    log-file-name: dynamic-log.log
```

### 3. Nacos 配置

在 Nacos 中创建配置，DataId: `luoyu-dynamic-log-config`，Group: `DEFAULT_GROUP`，内容如下：

```json
{
  "enabled": true,
  "remoteCallType": "feign",
  "logOutputPath": "/var/log/dynamic-log",
  "logFileName": "dynamic-log.log",
  "interfaces": [
    {
      "path": "/api/user/*",
      "method": "POST",
      "logLevel": "DEBUG",
      "count": 5
    },
    {
      "path": "/api/order/**",
      "method": "GET",
      "logLevel": "INFO",
      "count": 3
    }
  ]
}
```

### 4. Java Agent 方式（可选）

如果需要使用 Java Agent 方式，在启动时添加参数：

```bash
-javaagent:lombok-slf4j-dynamiclog-plugin-1.0.0-SNAPSHOT.jar
```

## 配置说明

### 应用配置（application.yml）

| 配置项 | 说明 | 必填 | 默认值 |
|--------|------|------|--------|
| `luoyu.dynamic-log.enabled` | 是否启用动态日志插件 | 是 | false |
| `luoyu.dynamic-log.nacos-server-addr` | Nacos 服务器地址 | 是 | - |
| `luoyu.dynamic-log.nacos-namespace` | Nacos 命名空间 | 否 | - |
| `luoyu.dynamic-log.nacos-data-id` | Nacos 配置 DataId | 否 | luoyu-dynamic-log-config |
| `luoyu.dynamic-log.nacos-group` | Nacos 配置 Group | 否 | DEFAULT_GROUP |
| `luoyu.dynamic-log.remote-call-type` | 远程调用类型：rpc 或 feign | 否 | - |
| `luoyu.dynamic-log.log-output-path` | 日志输出路径 | 否 | - |
| `luoyu.dynamic-log.log-file-name` | 日志文件名 | 否 | dynamic-log.log |

### Nacos 配置（JSON 格式）

| 字段 | 说明 | 类型 | 必填 |
|------|------|------|------|
| `enabled` | 是否启用 | Boolean | 是 |
| `remoteCallType` | 远程调用类型：rpc 或 feign | String | 否 |
| `logOutputPath` | 日志输出路径 | String | 否 |
| `logFileName` | 日志文件名 | String | 否 |
| `interfaces` | 接口配置列表 | Array | 否 |

#### 接口配置（interfaces）

| 字段 | 说明 | 类型 | 必填 |
|------|------|------|------|
| `path` | 接口路径（支持 Ant 风格匹配） | String | 是 |
| `method` | HTTP 方法（GET, POST, PUT, DELETE 等） | String | 否 |
| `logLevel` | 动态日志级别（TRACE, DEBUG, INFO, WARN, ERROR） | String | 是 |
| `count` | 需要打印的次数 | Integer | 否，默认 1 |

## 工作原理

1. **启动时**：从 Nacos 读取配置，判断插件是否启用，打印启动日志
2. **请求匹配**：当请求到达时，检查是否匹配配置的接口路径
3. **日志级别调整**：如果匹配，动态调整日志级别（仅针对该接口相关的 Logger）
4. **计数管理**：记录已打印次数，达到配置的次数后自动停止并恢复日志级别
5. **代码追踪**：使用 Java Agent 或 AOP 方式追踪方法调用，统计执行时间
6. **远程调用**：如果配置了 RPC 或 Feign，同时调整远程调用的日志级别

## 使用示例

### 示例 1：基本使用

1. 在 Nacos 中配置接口 `/api/user/list`，日志级别为 `DEBUG`，打印 3 次
2. 调用该接口 3 次，前 3 次会打印 DEBUG 级别日志
3. 第 4 次调用时，会打印：`动态接口已经调试完毕，如果需要再生效请修改nacos配置，从新设置值`
4. 之后调用不再打印 DEBUG 日志

### 示例 2：代码追踪

当接口匹配时，会自动追踪方法调用，输出类似 Arthas trace 的结果：

```
========== 动态日志追踪结果 ==========
接口: POST /api/user/list
总耗时: 125ms
----------------------------------------
`---HTTP.POST /api/user/list (125ms)
  `---[45] com.luoyu.user.controller.UserController.list (120ms)
    `---[23] com.luoyu.user.service.UserService.listUsers (115ms)
      `---[12] com.luoyu.user.dao.UserDao.selectList (100ms)
========================================
```

### 示例 3：远程调用

如果配置了 `remoteCallType: feign`，当接口匹配时，会自动调整 Feign 调用的日志级别。

## 注意事项

1. **日志框架兼容性**：项目已使用 `log4j-to-slf4j`，不能引入 `log4j-slf4j-impl`，避免死循环
2. **RPC 和 Feign**：`remoteCallType` 只能选择 `rpc` 或 `feign` 中的一个，不能同时选择
3. **配置更新**：修改 Nacos 配置后，会自动重新加载配置
4. **性能影响**：代码追踪功能会增加一定的性能开销，建议仅在调试时使用

## 开发说明

### 项目结构

```
src/main/java/com/luoyu/dynamiclog/
├── agent/              # Java Agent 相关
│   ├── DynamicLogAgent.java
│   └── MethodInterceptor.java
├── autoconfigure/      # Spring Boot 自动配置
│   └── DynamicLogAutoConfiguration.java
├── config/             # 配置类
│   ├── DynamicLogConfig.java
│   └── InterfaceLogConfig.java
├── interceptor/        # HTTP 拦截器
│   └── DynamicLogInterceptor.java
├── log/                # 日志管理
│   ├── LoggerLevelManager.java
│   └── DynamicLogFileAppender.java
├── nacos/              # Nacos 配置管理
│   └── NacosConfigManager.java
├── remote/             # 远程调用拦截
│   ├── RpcInterceptor.java
│   ├── FeignInterceptor.java
│   └── RemoteCallInterceptor.java
├── trace/              # 代码追踪
│   ├── TraceContext.java
│   ├── TraceNode.java
│   └── TraceManager.java
└── util/               # 工具类
    └── ThreadLocalUtil.java
```

## 许可证

[LICENSE](LICENSE)
